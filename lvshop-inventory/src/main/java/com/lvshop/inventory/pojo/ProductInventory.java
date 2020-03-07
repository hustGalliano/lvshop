package com.lvshop.inventory.pojo;

/**
 * 库存数量pojo
 * @author Galliano
 */
public class ProductInventory {

	// 商品id
	private Long productId;
	// 库存数量
	private Long inventoryCnt;
	
	public ProductInventory() {
	}
	
	public ProductInventory(Long productId, Long inventoryCnt) {
		this.productId = productId;
		this.inventoryCnt = inventoryCnt;
	}
	
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Long getInventoryCnt() {
		return inventoryCnt;
	}
	public void setInventoryCnt(Long inventoryCnt) {
		this.inventoryCnt = inventoryCnt;
	}
	
}
