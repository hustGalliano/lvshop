package com.lvshop.inventory.request;

/**
 * 请求接口
 * @author Galliano
 *
 */
public interface Request {

	/**
	 * 执行
	 */
	void process();

	/**
	 * 获取商品Id
	 * @return
	 */
	Long getProductId();
	
}
