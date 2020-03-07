package com.lvshop.cache.rebuild;

import com.lvshop.cache.zookeeper.ZooKeeperSession;
import com.lvshop.common.pojo.ShopInfo;
import com.lvshop.common.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 重建店铺信息缓存的线程
 * @author Galliano
 */
public class RebuildShopInfoCacheThread implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(RebuildShopInfoCacheThread.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private CacheService cacheService;

	public void run() {
		RebuildShopInfoCacheQueue rebuildShopInfoCacheQueue = RebuildShopInfoCacheQueue.getInstance();
		ZooKeeperSession zkSession = ZooKeeperSession.getInstance();
		
		while(true) {
			ShopInfo shopInfo = rebuildShopInfoCacheQueue.takeShopInfo();

			// 对这个 shopId 上锁
			String shopLockPath = "/shop-lock-" + shopInfo.getId();
			zkSession.acquireDistributedLock(shopLockPath);

			// 获取已存在的ShopInfo
			ShopInfo existedShopInfo = cacheService.getShopInfoFromRedisCache(shopInfo.getId());

			if(existedShopInfo != null) {
				// 比较当前数据的时间版本比已有数据的时间版本是新还是旧
				try {
					Date date = sdf.parse(shopInfo.getModifiedTime());
					Date existedDate = sdf.parse(existedShopInfo.getModifiedTime());
					
					if(date.before(existedDate)) {
						LOGGER.info("[ 当前ShopInfo的日期( {} ) before 于已存在ShopInfo的日期( {} ) ]",
								shopInfo.getModifiedTime(), existedShopInfo.getModifiedTime());
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				LOGGER.info("[ 当前ShopInfo的日期( {} ) after 于已存在的ShopInfo的日期( {} ) ]",
						shopInfo.getModifiedTime(), existedShopInfo.getModifiedTime());
			} else {
				LOGGER.info("[ Redis中已存在的shopInfo为null...... ]");
			}

			// 将商品信息写到本地EhCache缓存和Redis缓存中
			cacheService.saveShopInfo2LocalCache(shopInfo);
			cacheService.saveShopInfo2ReidsCache(shopInfo);  

			// 解锁
			zkSession.releaseDistributedLock(shopLockPath);
		}
	}
}
