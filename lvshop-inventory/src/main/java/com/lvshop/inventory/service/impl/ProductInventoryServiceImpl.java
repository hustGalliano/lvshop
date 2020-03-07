package com.lvshop.inventory.service.impl;

import com.lvshop.inventory.mapper.ProductInventoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lvshop.inventory.dao.RedisDAO;
import com.lvshop.inventory.pojo.ProductInventory;
import com.lvshop.inventory.service.ProductInventoryService;

/**
 * 商品库存Service实现类
 * @author Galliano
 *
 */
@Service("productInventoryService")  
public class ProductInventoryServiceImpl implements ProductInventoryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventoryServiceImpl.class);

	@Autowired
	private ProductInventoryMapper productInventoryMapper;

	@Autowired
	private RedisDAO redisDAO;


	// 新增商品库存
	@Override
	public void saveProductInventory(ProductInventory productInventory) {
		productInventoryMapper.saveProductInventory(productInventory);
		LOGGER.info("[ 已在数据库新增商品库存 ] 商品id = {}, 商品库存数量 = {}",
				productInventory.getProductId(), productInventory.getInventoryCnt());
	}

	// 更新商品库存
	@Override
	public void updateProductInventory(ProductInventory productInventory) {
		productInventoryMapper.updateProductInventory(productInventory);
		LOGGER.info("[ 已修改数据库中的库存 ] 商品id = {}, 商品库存数量 = {}",
				productInventory.getProductId(), productInventory.getInventoryCnt());
	}
	
	/**
	 * 根据商品id查询商品库存
	 * @param productId 商品id 
	 * @return 商品库存
	 */
	public ProductInventory findProductInventory(Long productId) {
		return productInventoryMapper.findProductInventory(productId);
	}


	/**
	 * 设置商品库存的缓存
	 * @param productInventory 商品库存
	 */
	public void setProductInventoryCache(ProductInventory productInventory) {

		String key = "product:inventory:" + productInventory.getProductId();

		redisDAO.set(key, String.valueOf(productInventory.getInventoryCnt()));

		LOGGER.info("[ 已更新商品库存的缓存 ] 商品id = {}, 商品库存数量 = {}, key = {}",
				productInventory.getProductId(), productInventory.getInventoryCnt(), key);
	}
	
	/**
	 * 获取商品库存的缓存
	 * @param productId
	 * @return
	 */
	public ProductInventory getProductInventoryCache(Long productId) {
		Long inventoryCnt = 0L;
		
		String key = "product:inventory:" + productId;
		String result = redisDAO.get(key);

		if(result != null && !"".equals(result)) {
			try {
				inventoryCnt = Long.valueOf(result);
				return new ProductInventory(productId, inventoryCnt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// 移除商品库存的Redis缓存
	@Override
	public void removeProductInventoryCache(ProductInventory productInventory) {
		String key = "product:inventory:" + productInventory.getProductId();
		redisDAO.delete(key);
		LOGGER.info("[ 已删除redis中的缓存 ] key = {}", key);
	}
}
