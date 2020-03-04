package com.lvshop.common.service;

import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.pojo.ShopInfo;

/**
 * 缓存service接口
 * @author Galliano
 */
public interface CacheService {
	/**
	 * EhCache相关的方法
	 */
	// 将 productInfo 保存到本地EhCache缓存中
	ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo);
	// 从本地EhCache缓存中获取 productInfo
	ProductInfo getProductInfoFromLocalCache(Long productId);


	// 将 shopInfo 保存到本地EhCache缓存中
	ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo);
	// 从本地EhCache缓存中获取 shopInfo
	ShopInfo getShopInfoFromLocalCache(Long shopId);

	// 从本地ehcache缓存中删除 productInfo
	Boolean removeProductInfoFromLocalCache(Long productId);
	// 从本地ehcache缓存中删除 shopInfo
	Boolean removeShopInfoFromLocalCache(Long shopId);




	/**
	 * Redis相关的方法
	 */
	// 将商品信息保存到redis中
	Boolean saveProductInfo2ReidsCache(ProductInfo productInfo);
	// 从Redis缓存中获取商品信息
	ProductInfo getProductInfoFromRedisCache(Long productId);
	// 从Redis中删除productInfo
	Boolean removeProductInfoFromRedisCache(Long productId);

	// 将店铺信息保存到redis中
	Boolean saveShopInfo2ReidsCache(ShopInfo shopInfo);
	// 从Redis缓存中获取店铺信息
	ShopInfo getShopInfoFromRedisCache(Long shopId);
	// 从Redis中删除shopInfo
	Boolean removeShopInfoFromRedisCache(Long shopId);
}
