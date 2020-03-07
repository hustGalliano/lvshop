/*
package com.lvshop.cache.kafka;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.pojo.ShopInfo;
import com.lvshop.common.service.CacheService;
import com.lvshop.cache.spring.SpringContext;
import com.lvshop.cache.zk.ZooKeeperSession;

import com.lvshop.common.service.ProductInfoService;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

*/
/**
 * kafka消息处理线程
 * @author Galliano
 *  两种服务会发送来数据变更消息：商品信息服务、店铺信息服务，
 *  每个消息都包含serviceId以及商品id或店铺id。
 *
 *  该类的工作流程：
 *      从KafkaBroker中获取Message，然后对Message进行解析，根据serviceId找到对应的微服务；
 *      拉取到了Message之后，将Message组织成json串，然后可以存储到EhCache或Redis中。
 *//*


@SuppressWarnings("rawtypes")
public class KafkaMessageProcessor implements Runnable {

	// 从Dubbo注入实例
	@Reference(loadbalance="random",timeout=1000)
	private ProductInfoService productInfoService;

	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private KafkaStream kafkaStream;
	private CacheService cacheService;


	
	public KafkaMessageProcessor(KafkaStream kafkaStream) {
		this.kafkaStream = kafkaStream;
		this.cacheService = (CacheService) SpringContext.getApplicationContext()
				.getBean("cacheService"); 
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();

		// 若能从kafka中获取到message
        while (it.hasNext()) {
        	String message = new String(it.next().message());

			// 将字符串转换成Json对象
        	JSONObject messageJSONObject = JSONObject.parseObject(message);
        	
        	// 从这里提取出消息对应的服务的标识
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
	
	*/
/**
	 * 处理商品信息变更的消息，包含分布式锁的逻辑
	 * @param messageJSONObject
	 *//*

	private void processProductInfoChangeMessage(JSONObject messageJSONObject) {
		// 提取出商品id
		Long productId = messageJSONObject.getLong("productId");

		// 利用Dubbo来RPC数据源服务的接口
		ProductInfo productInfo = productInfoService.findProductInfoById(productId);

		// 测试所用
//		String productInfoJSON = HttpClientUtils.sendGetRequest("localhost:8081/getProductInfo?productId=" + productId);
//		String productInfoJSON = "{\"id\": 1, \"name\": \"iphone7手机\", \"price\": 5599, " +
//				"\"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", " +
//				"\"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", " +
//				"\"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";
//		ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

		// 在将数据直接写入redis缓存之前，应该先获取一个zk的分布式锁
		ZooKeeperSession zkSession = ZooKeeperSession.getInstance();

		// 上锁
		zkSession.acquireDistributedLock(productId);  
		
		// 走到这步证明获取到了锁
		// 先从redis中获取数据
		ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productId);
		
		if(existedProductInfo != null) {
			// 比较当前数据的时间版本比已有数据的时间版本是新还是旧
			try {
				Date date = sdf.parse(productInfo.getModifiedTime());
				Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());
				
				if(date.before(existedDate)) {
					System.out.println("=====日志=====：当前ProductInfo的日期[" + productInfo.getModifiedTime()
							+ "] before 于已存在ProductInfo的日期[" + existedProductInfo.getModifiedTime() + "]");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("=====日志=====：当前ProductInfo的日期[" + productInfo.getModifiedTime()
					+ "] after 于已存在ProductInfo的日期[" + existedProductInfo.getModifiedTime() + "]");
		} else {
			System.out.println("=====日志=====：Redis中已存在的productInfo为null......");
		}
		
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		cacheService.saveProductInfo2LocalCache(productInfo);
		System.out.println("=====日志=====：获取刚保存到本地缓存的商品信息：" + cacheService.getProductInfoFromLocalCache(productId));
		cacheService.saveProductInfo2ReidsCache(productInfo);  
		
		// 释放分布式锁
		zkSession.releaseDistributedLock(productId); 
	}
	
	*/
/**
	 * 处理店铺信息变更的消息
	 * @param messageJSONObject
	 *//*

	@SuppressWarnings("unused")
	private void processShopInfoChangeMessage(JSONObject messageJSONObject) {
		// 提取出店铺id
		Long shopId = messageJSONObject.getLong("shopId");
		
		// 调用数据源服务的接口
		// 直接用注释模拟：getShopInfo?shopId=1，传递过去
		// 然后数据源服务就会去查询数据库，去获取shopId=1的店铺信息，然后获取返回数据
		String shopInfoJSON = "{\"id\": 1, \"name\": \"小王的手机店\", \"level\": 5, \"goodCommentRate\":0.99}";
		ShopInfo shopInfo = JSONObject.parseObject(shopInfoJSON, ShopInfo.class);

		cacheService.saveShopInfo2LocalCache(shopInfo);

		System.out.println("=====日志=====：获取刚保存到本地缓存的店铺信息：" + cacheService.getShopInfoFromLocalCache(shopId));

		cacheService.saveShopInfo2ReidsCache(shopInfo);  
	}

}
*/
