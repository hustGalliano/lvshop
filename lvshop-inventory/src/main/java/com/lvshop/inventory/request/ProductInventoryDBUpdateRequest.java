package com.lvshop.inventory.request;

import com.lvshop.inventory.pojo.ProductInventory;
import com.lvshop.inventory.service.ProductInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 比如说一个商品发生了交易，那么就要修改这个商品对应的库存
 * 此时就会发送请求过来，要求修改库存，那么这个可能就是所谓的data update request，数据更新请求
 * 
 * cache aside pattern
 * （1）删除缓存
 * （2）更新数据库
 * @author Galliano
 *
 */
public class ProductInventoryDBUpdateRequest implements Request {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventoryDBUpdateRequest.class);

	// 商品库存
	private ProductInventory productInventory;

	// 商品库存Service
	private ProductInventoryService productInventoryService;
	
	public ProductInventoryDBUpdateRequest(ProductInventory productInventory,
			ProductInventoryService productInventoryService) {
		this.productInventory = productInventory;
		this.productInventoryService = productInventoryService;
	}


	@Override
	public void process() {
		LOGGER.info("[ 数据库更新请求开始执行 ] 商品id = {}, 商品库存数量 = {}",
				productInventory.getProductId(), productInventory.getInventoryCnt());

		// 先删除redis中的缓存
		productInventoryService.removeProductInventoryCache(productInventory);

		// 为了演示先删除了redis缓存，然后还没更新数据库的时候，
		// 而来了一个读请求，又重新写了缓存，
		// 造成Redis和MySQL数据不一致现象
		// 模拟这个情况 ：先sleep一下，然后手动打一个读请求
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		// 修改数据库中的库存
		productInventoryService.updateProductInventory(productInventory);  
	}
	

	public Long getProductId() {
		return productInventory.getProductId();
	}
	
}
