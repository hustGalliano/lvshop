package com.lvshop.inventory.service;

import com.lvshop.inventory.pojo.ProductInventory;

/**
 * 商品库存Service接口
 * @author Galliano
 *
 */
public interface ProductInventoryService {

	/**
	 * 新增商品库存
	 * @param productInventory
	 */
	public void saveProductInventory(ProductInventory productInventory);

	/**
	 * 更新商品库存
	 * @param productInventory 商品库存
	 */
	void updateProductInventory(ProductInventory productInventory);
	
	/**
	 * 根据商品id查询商品库存
	 * @param productId 商品id 
	 * @return 商品库存
	 */
	ProductInventory findProductInventory(Long productId);

	/**
	 * 设置商品库存的缓存
	 * @param productInventory 商品库存
	 */
	void setProductInventoryCache(ProductInventory productInventory);
	
	/**
	 * 获取商品库存的缓存
	 * @param productId
	 * @return
	 */
	ProductInventory getProductInventoryCache(Long productId);

	/**
	 * 删除Redis中的商品库存的缓存
	 * @param productInventory 商品库存
	 */
	void removeProductInventoryCache(ProductInventory productInventory);
	
}
