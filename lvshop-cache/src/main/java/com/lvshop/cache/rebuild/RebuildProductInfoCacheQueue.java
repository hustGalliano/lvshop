package com.lvshop.cache.rebuild;

import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSON;
import com.lvshop.common.pojo.ProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重建商品信息缓存的内存队列
 * @author Galliano
 */
public class RebuildProductInfoCacheQueue {
	private static final Logger LOGGER = LoggerFactory.getLogger(RebuildProductInfoCacheQueue.class);

	private static final int rebuildProductInfoCacheQueueSize = 1000;

	private LinkedBlockingQueue<ProductInfo> productInfoQueue = new LinkedBlockingQueue<>(rebuildProductInfoCacheQueueSize);
	
	public void putProductInfo(ProductInfo productInfo) {
		try {
			productInfoQueue.put(productInfo);
			LOGGER.info("[ 缓存重建内存队列中入队一个productInfo ] productId = {}", productInfo.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ProductInfo takeProductInfo() {
		try {
			ProductInfo productInfo = productInfoQueue.take();
			LOGGER.info("[ 缓存重建内存队列中出队一个productInfo ] productId = {}", productInfo.getId());
//			LOGGER.info("[ 缓存重建内存队列中出队一个productInfo ] productInfo = {}", JSON.toJSONString(productInfo));
			return productInfo;
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
		private static RebuildProductInfoCacheQueue instance;
		static {
			instance = new RebuildProductInfoCacheQueue();
		}
		public static RebuildProductInfoCacheQueue getInstance() {
			return instance;
		}
	}
	
	public static RebuildProductInfoCacheQueue getInstance() {
		return Singleton.getInstance();
	}
}
