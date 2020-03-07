package com.lvshop.inventory.request;

import com.lvshop.inventory.pojo.ProductInventory;
import com.lvshop.inventory.service.ProductInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重新加载商品库存的缓存
 * @author Galliano
 *
 */
public class ProductInventoryCacheRefreshRequest implements Request {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventoryCacheRefreshRequest.class);

	// 商品id
	private Long productId;

	// 商品库存Service
	private ProductInventoryService productInventoryService;
	
	public ProductInventoryCacheRefreshRequest(Long productId,
			ProductInventoryService productInventoryService) {
		this.productId = productId;
		this.productInventoryService = productInventoryService;
	}
	
	@Override
	public void process() {
		// 先从MySQL中查询最新的商品库存数量
		ProductInventory productInventory = productInventoryService.findProductInventory(productId);

		LOGGER.info("[ 已查询到商品最新的库存数量 ] 商品id = {}, 商品库存数量 = {}", productId, productInventory.getInventoryCnt());

		// 将最新的商品库存数量，刷新到redis缓存中去
		productInventoryService.setProductInventoryCache(productInventory); 
	}
	
	public Long getProductId() {
		return productId;
	}
	
}
