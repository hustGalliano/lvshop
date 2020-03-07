package com.lvshop.cache.kafka;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.lvshop.cache.zookeeper.ZooKeeperSession;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.pojo.ShopInfo;
import com.lvshop.common.service.CacheService;
import com.lvshop.common.service.ProductInfoService;
import com.lvshop.common.service.ShopInfoService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * kafka消费者线程
 * @author Galliano
 *
 *  两种服务(商品信息服务、店铺信息服务)会发送来数据变更消息：
 *      每个消息都包含serviceId以及商品id或店铺id。
 *
 *  该类的工作流程：
 *      从KafkaBroker中获取Message，然后对Message进行解析:
 *          根据serviceId找到对应的微服务；
 *          根据productId或shopId，RPC数据源服务，查询对应Id的数据
 *      将查到的数据组织成json串，然后可以存储到EhCache或Redis中。
 */
@Component
public class KafkaConsumer {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    private static final String TOPIC = "data_change";

    // 从Dubbo注入实例
    @Reference
    private ProductInfoService productInfoService;
    @Reference
    private ShopInfoService shopInfoService;

    @Autowired
    private CacheService cacheService;


/*    @SuppressWarnings("rawtypes")
    public void run() {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =
                consumerConnector.createMessageStreams(topicCountMap);

        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        for (KafkaStream stream : streams) {
            new Thread(new KafkaMessageProcessor(stream)).start();
        }
    }*/

    @KafkaListener(topics = {TOPIC})
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            String message = (String) kafkaMessage.get();
            LOGGER.info("[ Consumer接收到消息 ] message = {}", message);

            JSONObject messageJSONObject = JSONObject.parseObject(message);
            String serviceId = messageJSONObject.getString("serviceId");

            if("productInfoService".equals(serviceId)) {
                // 如果是商品信息服务
                processProductInfoChangeMessage(messageJSONObject);
            } else if("shopInfoService".equals(serviceId)) {
                // 如果是店铺信息服务
                processShopInfoChangeMessage(messageJSONObject);
            }
        }
    }

    /**
     * 处理商品信息变更的消息，包含分布式锁的逻辑
     * @param messageJSONObject
     */
    private void processProductInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出商品id
        Long productId = messageJSONObject.getLong("productId");

        // 利用Dubbo来RPC数据源服务的接口
        ProductInfo productInfo = productInfoService.findProductInfoById(productId);

        // 测试所用
//		String productInfoJSON = HttpClientUtils.sendGetRequest("localhost:8081/getProductInfo?productId=" + productId);
//		String productInfoJSON = "{\"id\": 1, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";
//		ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

        // 在将数据直接写入redis缓存之前，应该先获取一个zk的分布式锁
        ZooKeeperSession zkSession = ZooKeeperSession.getInstance();

        // 对这个 productId 上锁
        String productLockPath = "/product-lock-" + productId;
        zkSession.acquireDistributedLock(productLockPath);

        // 走到这步证明获取到了锁
        // 先从redis中获取数据
        ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productId);

        if(existedProductInfo != null) {
            // 比较当前数据的时间版本比已有数据的时间版本是新还是旧
            try {
                Date date = sdf.parse(productInfo.getModifiedTime());
                Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());

                if(date.before(existedDate)) {
                    LOGGER.info("[ 当前ProductInfo的日期 ( {} ) before 于已存在ProductInfo的日期 ( {} ) ]",
                            productInfo.getModifiedTime(),existedProductInfo.getModifiedTime());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info("[ 当前ProductInfo的日期( {} ) after 于已存在ProductInfo的日期( {} ) ]",
                    productInfo.getModifiedTime(), existedProductInfo.getModifiedTime());
        } else {
            LOGGER.info("[ Redis中已存在的productInfo为null...... ]");
        }

        // 测试用
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cacheService.saveProductInfo2LocalCache(productInfo);
        LOGGER.info("[ 成功保存 ProductInfo 到 EhCache 本地缓存中！ ]");

        LOGGER.info("[ 获取刚保存到本地缓存的商品信息 ] productInfo = {}",
                cacheService.getProductInfoFromLocalCache(productId));

        cacheService.saveProductInfo2ReidsCache(productInfo);
        LOGGER.info("[ 成功保存 ProductInfo 到 Redis 缓存中！ ]");

        // 释放分布式锁
        zkSession.releaseDistributedLock(productLockPath);
    }

    /**
     * 处理店铺信息变更的消息
     * @param messageJSONObject
     */
    @SuppressWarnings("unused")
    private void processShopInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出店铺id
        Long shopId = messageJSONObject.getLong("shopId");

        // 利用Dubbo来RPC数据源服务的接口
        ShopInfo shopInfo = shopInfoService.findShopInfoById(shopId);
        
        // 在将数据直接写入redis缓存之前，应该先获取一个zk的分布式锁
        ZooKeeperSession zkSession = ZooKeeperSession.getInstance();

        // 对这个 shopId 上锁
        String shopLockPath = "/shop-lock-" + shopId;
        zkSession.acquireDistributedLock(shopLockPath);

        // 走到这步证明获取到了锁
        // 先从redis中获取数据
        ShopInfo existedShopInfo = cacheService.getShopInfoFromRedisCache(shopId);

        if(existedShopInfo != null) {
            // 比较当前数据的时间版本比已有数据的时间版本是新还是旧
            try {
                Date date = sdf.parse(shopInfo.getModifiedTime());
                Date existedDate = sdf.parse(existedShopInfo.getModifiedTime());

                if(date.before(existedDate)) {
                    LOGGER.info("[ 当前ShopInfo的日期( {} ) before 于已存在ShopInfo的日期( {} ) ]",
                            shopInfo.getModifiedTime(), existedShopInfo.getModifiedTime());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOGGER.info("[ 当前ShopInfo的日期( {} ) after 于已存在ShopInfo的日期( {} ) ]",
                    shopInfo.getModifiedTime(), existedShopInfo.getModifiedTime());
        } else {
            LOGGER.info("[ Redis中已存在的shopInfo为null...... ]");
        }

        // 测试用
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cacheService.saveShopInfo2LocalCache(shopInfo);
        LOGGER.info("[ 成功保存 ShopInfo 到 EhCache 本地缓存中！ ]");
        cacheService.saveShopInfo2ReidsCache(shopInfo);
        LOGGER.info("[ 成功保存 ShopInfo 到 Redis 缓存中！ ]");

        // 释放分布式锁
        zkSession.releaseDistributedLock(shopLockPath);
    }
}
