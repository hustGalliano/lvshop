package com.lvshop.cache.rebuild;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.service.CacheService;
//import com.lvshop.cache.spring.SpringContext;
import com.lvshop.cache.zookeeper.ZooKeeperSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 重建商品信息缓存的线程
 * @author Galliano
 */
public class RebuildProductInfoCacheThread implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(RebuildProductInfoCacheThread.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private CacheService cacheService;

//	@Autowired
//	private StringRedisTemplate redisTemplate;


	public void run() {
		// 初始化
		RebuildProductInfoCacheQueue rebuildProductInfoCacheQueue = RebuildProductInfoCacheQueue.getInstance();
		ZooKeeperSession zkSession = ZooKeeperSession.getInstance();
		
		for (;;) {
			ProductInfo productInfo = rebuildProductInfoCacheQueue.takeProductInfo();

			// 对这个 productId 上锁
			String productLockPath = "/product-lock-" + productInfo.getId();
			zkSession.acquireDistributedLock(productLockPath);

			// 获取已存在的ProductInfo
			ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productInfo.getId());

//			String json = redisTemplate.opsForValue().get("product_info_" + productInfo.getId());
//			ProductInfo existedProductInfo = JSONObject.parseObject(json, ProductInfo.class);

			if(existedProductInfo != null) {
				// 比较当前数据的时间版本比已有数据的时间版本是新还是旧
				try {
					Date date = sdf.parse(productInfo.getModifiedTime());
					Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());
					
					if(date.before(existedDate)) {
						LOGGER.info("[ 当前ProductInfo的日期( {} ) before 于已存在ProductInfo的日期( {} ) ]",
								productInfo.getModifiedTime(), existedProductInfo.getModifiedTime());
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				LOGGER.info("[ 当前ProductInfo的日期( {} ) after 于已存在的ProductInfo的日期( {} ) ]", productInfo.getModifiedTime(), existedProductInfo.getModifiedTime());
			} else {
				LOGGER.info("[ Redis中已存在的productInfo为null...... ]");
			}

			// 将商品信息写到本地EhCache缓存和Redis缓存中
			cacheService.saveProductInfo2LocalCache(productInfo);
			cacheService.saveProductInfo2ReidsCache(productInfo);  

			// 解锁
			zkSession.releaseDistributedLock(productLockPath);
		}
	}
}
