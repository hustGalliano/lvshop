package com.lvshop.inventory.mapper;


import com.lvshop.inventory.pojo.ProductInventory;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存数量Mapper
 * @author Galliano
 */
@Mapper
public interface ProductInventoryMapper {

	/**
	 * 新增商品库存
	 * @param productInventory 商品库存信息
	 */
	void saveProductInventory(ProductInventory productInventory);

	/**
	 * 删除商品库存
	 * @param productId
	 */
	void removeProductInventory(@Param("productId") Long productId);

	/**
	 * 更新库存数量
	 * @param productInventory 商品库存信息
	 */
	void updateProductInventory(ProductInventory productInventory);
	
	/**
	 * 根据商品id查询商品库存信息
	 * @param productId 商品id
	 * @return 商品库存信息
	 */
	ProductInventory findProductInventory(@Param("productId") Long productId);

}
