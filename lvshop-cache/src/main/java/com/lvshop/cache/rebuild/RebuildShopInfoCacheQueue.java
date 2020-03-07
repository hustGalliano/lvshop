package com.lvshop.cache.rebuild;

import com.alibaba.fastjson.JSON;

import com.lvshop.common.pojo.ShopInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 重建店铺信息缓存的内存队列
 * @author Galliano
 */
public class RebuildShopInfoCacheQueue {
	private static final Logger LOGGER = LoggerFactory.getLogger(RebuildShopInfoCacheQueue.class);

	private static final int rebuildShopInfoCacheQueueSize = 1000;

	private LinkedBlockingQueue<ShopInfo> shopInfoQueue = new LinkedBlockingQueue<>(rebuildShopInfoCacheQueueSize);
	
	public void putShopInfo(ShopInfo shopInfo) {
		try {
			shopInfoQueue.put(shopInfo);
			LOGGER.info("[ 缓存重建内存队列中入队一个shopInfo ] shopId = {}", shopInfo.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ShopInfo takeShopInfo() {
		try {
			ShopInfo shopInfo = shopInfoQueue.take();
			LOGGER.info("[ 缓存重建内存队列中出队一个shopInfo ] shopId = {}", shopInfo.getId());

//			LOGGER.info("[ 缓存重建内存队列中出队一个shopInfo ] shopInfo = {}", JSON.toJSONString(shopInfo));
			return shopInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 内部单例类
	 * @author Galliano
	 */
	private static class Singleton {
		private static RebuildShopInfoCacheQueue instance;
		static {
			instance = new RebuildShopInfoCacheQueue();
		}
		public static RebuildShopInfoCacheQueue getInstance() {
			return instance;
		}
	}
	
	public static RebuildShopInfoCacheQueue getInstance() {
		return Singleton.getInstance();
	}
	
	public static void init() {
		getInstance();
	}
}
