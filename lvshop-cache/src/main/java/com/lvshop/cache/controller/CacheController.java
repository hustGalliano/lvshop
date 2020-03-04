package com.lvshop.cache.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.lvshop.cache.rebuild.RebuildShopInfoCacheQueue;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.pojo.ShopInfo;
import com.lvshop.cache.prewarm.CachePrewarmThread;
import com.lvshop.cache.rebuild.RebuildProductInfoCacheQueue;
import com.lvshop.common.service.CacheService;
import com.lvshop.common.service.ProductInfoService;
import com.lvshop.common.service.ShopInfoService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 缓存的Controller
 * @author Galliano
 */
@RestController
public class CacheController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

	@Autowired
	private CacheService cacheService;

	// 从Dubbo注入实例
	@Reference(version = "1.0", url = "dubbo://hang:20880")
	private ProductInfoService productInfoService;
	@Reference(version = "1.0", url = "dubbo://hang:20880")
	private ShopInfoService shopInfoService;


	// 测试
//	@RequestMapping("/testPutCache")
//	public String testPutCache(ProductInfo productInfo) {
//		cacheService.saveLocalCache(productInfo);
//		return "success";
//	}
//
//	@RequestMapping("/testGetCache")
//	public ProductInfo testGetCache(Long id) {
//		return cacheService.getLocalCache(id);
//	}
	
	@RequestMapping("/getProductInfo")
	public ProductInfo getProductInfo(Long productId) {
		ProductInfo productInfo = cacheService.getProductInfoFromRedisCache(productId);

		if(productInfo != null) {
			LOGGER.info("[从Redis中获取缓存] productInfo = " + productInfo);
		}
		
		if(productInfo == null) {
			LOGGER.info("[Redis缓存中无此数据] productId = " + productId);

			productInfo = cacheService.getProductInfoFromLocalCache(productId);
			if(productInfo != null) {
				LOGGER.info("[从EhCache中获取缓存] productInfo = " + productInfo);
			}
		}
		
		if(productInfo == null) {
			LOGGER.info("[EhCache缓存中无此数据] productId = " + productId);
			LOGGER.info("[即将从数据源服务拉取数据，并重建缓存] productId = " + productId);

			// 从数据源重新拉取数据，重建缓存
//			GetProductInfoCommand command = new GetProductInfoCommand(productId);
//			productInfo = command.execute();

			productInfo = getProductInfoCommand(productId);

			// 将数据推送到一个内存队列中
			RebuildProductInfoCacheQueue rebuildProductInfoCacheQueue = RebuildProductInfoCacheQueue.getInstance();
			rebuildProductInfoCacheQueue.putProductInfo(productInfo);
		}
		return productInfo;
	}
	
	@RequestMapping("/getShopInfo")
	public ShopInfo getShopInfo(Long shopId) {
		ShopInfo shopInfo = cacheService.getShopInfoFromRedisCache(shopId);

		if(shopInfo != null) {
			LOGGER.info("[从Redis中获取缓存] shopInfo = " + shopInfo);
		}

		if(shopInfo == null) {
			LOGGER.info("[Redis缓存中无此数据] shopId = " + shopId);

			shopInfo = cacheService.getShopInfoFromLocalCache(shopId);
			if(shopInfo != null) {
				LOGGER.info("[从EhCache中获取缓存] shopInfo = " + shopInfo);
			}
		}

		if(shopInfo == null) {
			LOGGER.info("[EhCache缓存中无此数据] shopId = " + shopId);
			LOGGER.info("[即将从数据源服务拉取数据，并重建缓存] shopId = " + shopId);

			// 从数据源重新拉取数据，重建缓存
			shopInfo = getShopInfoCommand(shopId);

			// 将数据推送到一个内存队列中
			RebuildShopInfoCacheQueue rebuildShopInfoCacheQueue = RebuildShopInfoCacheQueue.getInstance();
			rebuildShopInfoCacheQueue.putShopInfo(shopInfo);
		}
		return shopInfo;
	}
	
	@RequestMapping("/prewarmCache")
	public void prewarmCache() {
		new CachePrewarmThread().start();
	}




	@HystrixCommand(fallbackMethod = "getProductInfoCommandFallback",
			groupKey = "ProductService",
			threadPoolProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.CORE_SIZE, value = "10"),
					@HystrixProperty(name = HystrixPropertiesManager.MAXIMUM_SIZE, value = "30"),
					@HystrixProperty(name = HystrixPropertiesManager.ALLOW_MAXIMUM_SIZE_TO_DIVERGE_FROM_CORE_SIZE, value = "true"),
					@HystrixProperty(name = HystrixPropertiesManager.KEEP_ALIVE_TIME_MINUTES, value = "1"),
					@HystrixProperty(name = HystrixPropertiesManager.MAX_QUEUE_SIZE, value = "50"),
					@HystrixProperty(name = HystrixPropertiesManager.QUEUE_SIZE_REJECTION_THRESHOLD, value = "100"),

			})
	private ProductInfo getProductInfoCommand(Long productId) {
		// 利用Dubbo来RPC数据源服务的接口
		ProductInfo productInfo = productInfoService.findProductInfoById(productId);

		// 若某个商品id在数据源服务中没有查询到数据，则直接返回一个只有id的实例
		if (productInfo == null) {
			productInfo = new ProductInfo();
			productInfo.setId(productId);
		}
		return productInfo;
	}

	@HystrixCommand(fallbackMethod = "getShopInfoCommandFallback",
			groupKey = "ShopService",
			threadPoolProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.CORE_SIZE, value = "10"),
					@HystrixProperty(name = HystrixPropertiesManager.MAXIMUM_SIZE, value = "30"),
					@HystrixProperty(name = HystrixPropertiesManager.ALLOW_MAXIMUM_SIZE_TO_DIVERGE_FROM_CORE_SIZE, value = "true"),
					@HystrixProperty(name = HystrixPropertiesManager.KEEP_ALIVE_TIME_MINUTES, value = "1"),
					@HystrixProperty(name = HystrixPropertiesManager.MAX_QUEUE_SIZE, value = "50"),
					@HystrixProperty(name = HystrixPropertiesManager.QUEUE_SIZE_REJECTION_THRESHOLD, value = "100"),

			})
	private ShopInfo getShopInfoCommand(Long shopId) {
		// 利用Dubbo来RPC数据源服务的接口
		ShopInfo shopInfo = shopInfoService.findShopInfoById(shopId);

		// 若某个商品id在数据源服务中没有查询到数据，则直接返回一个只有id的实例
		if (shopInfo == null) {
			shopInfo = new ShopInfo();
			shopInfo.setId(shopId);
		}
		return shopInfo;
	}


	/**
	 * 降级方法
	 * @param productId
	 * @return
	 */
	private ProductInfo getProductInfoCommandFallback(Long productId) {
		ProductInfo productInfo = new ProductInfo();
		productInfo.setId(productId);
		// 从内存中找一些残缺的数据拼装进去
		return productInfo;
	}

	/**
	 * 降级方法
	 * @param shopId
	 * @return
	 */
	private ShopInfo getShopInfoCommandFallback(Long shopId) {
		ShopInfo shopInfo = new ShopInfo();
		shopInfo.setId(shopId);
		// 从内存中找一些残缺的数据拼装进去
		return shopInfo;
	}
}
