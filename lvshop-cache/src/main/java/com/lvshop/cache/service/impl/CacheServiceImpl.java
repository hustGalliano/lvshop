package com.lvshop.cache.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.pojo.ShopInfo;
import com.lvshop.common.service.CacheService;


/**
 * 缓存Service实现类
 * @author Galliano
 */
@Service("cacheService")
public class CacheServiceImpl implements CacheService {
	private final static Logger LOGGER = LoggerFactory.getLogger(CacheServiceImpl.class);
	
	public static final String CACHE_NAME = "local";

	@Autowired
	private StringRedisTemplate redisTemplate;


// 用做测试
//	将商品信息保存到本地缓存中
//	@CachePut(value = CACHE_NAME, key = "'key_'+#productInfo.getId()")
//	public ProductInfo saveLocalCache(ProductInfo productInfo) {
//		return productInfo;
//	}
//	从本地缓存中获取商品信息
//	@Cacheable(value = CACHE_NAME, key = "'key_'+#id")
//	public ProductInfo getLocalCache(Long id) {
//		return null;
//	}
//		SaveProductInfo2ReidsCacheCommand command = new SaveProductInfo2ReidsCacheCommand(productInfo);
//		command.execute();



	/**
	 * EhCache相关
	 */
	// 将ProductInfo保存到本地的ehcache缓存中
	@CachePut(value = CACHE_NAME, key = "'product_info_'+#productInfo.getId()")
	public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
		return productInfo;
	}

	// 从本地ehcache缓存中获取ProductInfo
	@Cacheable(value = CACHE_NAME, key = "'product_info_'+#productId")
	public ProductInfo getProductInfoFromLocalCache(Long productId) {
		return null;
	}

	// 从本地ehcache缓存中删除ProductInfo
	@CacheEvict(value = CACHE_NAME, key = "'product_info_'+#productId", beforeInvocation = true)
	public Boolean removeProductInfoFromLocalCache(Long productId) { return true; }


	// 将ShopInfo保存到本地的ehcache缓存中
	@CachePut(value = CACHE_NAME, key = "'shop_info_'+#shopInfo.getId()")
	public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo) {
		return shopInfo;
	}

	// 从本地ehcache缓存中获取ShopInfo
	@Cacheable(value = CACHE_NAME, key = "'shop_info_'+#shopId")
	public ShopInfo getShopInfoFromLocalCache(Long shopId) {
		return null;
	}

	// 从本地ehcache缓存中删除ShopInfo
	@CacheEvict(value = CACHE_NAME, key = "'shop_info_'+#shopId", beforeInvocation = true)
	public Boolean removeShopInfoFromLocalCache(Long shopId) { return true; }




	/**
	 * Redis相关
	 */
	// 将ProductInfo保存到Redis中
	@HystrixCommand(fallbackMethod = "saveProductInfo2ReidsCacheFallback",
			groupKey = "RedisGroup",
			commandProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "100"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = "1000"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, value = "70"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, value = "60000"),
			})
	public Boolean saveProductInfo2ReidsCache(ProductInfo productInfo) {
		String key = "product_info_" + productInfo.getId();
		redisTemplate.opsForValue().set(key, JSONObject.toJSONString(productInfo));
		return true;
	}


	// 从Redis中获取ProductInfo
	@HystrixCommand(fallbackMethod = "getProductInfoFromRedisCacheFallback",
			groupKey = "RedisGroup",
			commandProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "100"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = "1000"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, value = "70"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, value = "60000"),
			})
	public ProductInfo getProductInfoFromRedisCache(Long productId) {
		try {
//			Thread.sleep(10000);

			String key = "product_info_" + productId;
			String json = redisTemplate.opsForValue().get(key);

			if(json != null) {
				return JSONObject.parseObject(json, ProductInfo.class);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 将ShopInfo保存到Redis中
	@HystrixCommand(fallbackMethod = "saveShopInfo2ReidsCacheFallback",
			groupKey = "RedisGroup",
			commandProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "100"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = "1000"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, value = "70"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, value = "60000"),
			})
	public Boolean saveShopInfo2ReidsCache(ShopInfo shopInfo) {
		String key = "shop_info_" + shopInfo.getId();
		redisTemplate.opsForValue().set(key, JSONObject.toJSONString(shopInfo));
		return true;
	}

	// 从Redis中获取ShopInfo
	@HystrixCommand(fallbackMethod = "getShopInfoFromRedisCacheFallback",
			groupKey = "RedisGroup",
			commandProperties = {
					@HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "100"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = "1000"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, value = "70"),
					@HystrixProperty(name = HystrixPropertiesManager.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, value = "60000"),
			})
	public ShopInfo getShopInfoFromRedisCache(Long shopId) {
		try {
			String key = "shop_info_" + shopId;
			String json = redisTemplate.opsForValue().get(key);

			if(json != null) {
				return JSONObject.parseObject(json, ShopInfo.class);
			}
			return null;
		} catch (Exception e) {
			LOGGER.error("[ 从Redis中获取shopInfo出现异常 ] e = " + e.toString());
			return null;
		}
	}

	// 从Redis中删除shopInfo
	public Boolean removeShopInfoFromRedisCache(Long shopId) {
		try {
			String key = "shop_info_" + shopId;

			return redisTemplate.delete(key);
		} catch (Exception e) {
			LOGGER.error("[ 从Redis中删除shopInfo出现异常 ] e = " + e.toString());
			return false;
		}
	}
	// 从Redis中删除productInfo
	public Boolean removeProductInfoFromRedisCache(Long productId) {
		try {
			String key = "product_info_" + productId;

			return redisTemplate.delete(key);
		} catch (Exception e) {
			LOGGER.error("[ 从Redis中删除productInfo出现异常 ] e = " + e.toString());
			return false;
		}
	}






	/**
	 * 以下为降级方法
	 */
	public boolean saveProductInfo2ReidsCacheFallback(ProductInfo productInfo) {
		return false;
	}

	public ProductInfo getProductInfoFromRedisCacheFallback(Long productId) {
//		System.out.println("======== 进入降级方法中 ========");
		return null;
	}

	public boolean saveShopInfo2ReidsCacheFallback(ShopInfo shopInfo) {
		return false;
	}

	public ShopInfo getShopInfoFromRedisCacheFallback(Long shopId) {
		return null;
	}
}
