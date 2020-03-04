package com.lvshop.inventory.service;

import com.lvshop.inventory.request.Request;

/**
 * 请求异步执行的service
 * @author Galliano
 *
 */
public interface RequestAsyncProcessService {

	void process(Request request);
	
}
