package com.lvshop.inventory.service.impl;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lvshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.lvshop.inventory.request.ProductInventoryDBUpdateRequest;
import com.lvshop.inventory.request.Request;
import com.lvshop.inventory.request.RequestQueue;
import com.lvshop.inventory.service.RequestAsyncProcessService;

/**
 * 实现请求异步处理的service
 * @author Galliano
 * 将读请求去重后，按id加入到内存队列中
 */
@Service("requestAsyncProcessService")  
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventoryServiceImpl.class);

	@Override
	public void process(Request request) {
		// 做读请求的去重
		try {
			RequestQueue requestQueue = RequestQueue.getInstance();
			Map<Long, Boolean> flagMap = requestQueue.getFlagMap();
			
			if(request instanceof ProductInventoryDBUpdateRequest) {

				// 如果该请求是一个更新MySQL中商品的请求，那么就将那个productId对应的标识设置为true
				flagMap.put(request.getProductId(), true);


			} else if(request instanceof ProductInventoryCacheRefreshRequest) {
				// 如果该请求是一个刷新商品库存的Redis缓存的请求，则进入这个if块
				Boolean flag = flagMap.get(request.getProductId());
				
				// 如果flag是null，说明是第一次关于这个id的第一次请求。
				if(flag == null) {
					// 此处注意，改变了map中的值，但此时的flag并没有被改变，还是null，所以下面两个if都会跳过
					flagMap.put(request.getProductId(), false);
				}

				// 只有更新MySQL中商品的请求，才会将flag设置为true
				// 所以如果flag是true，就说明之前有一个这个更新MySQL中商品的请求
				if(flag != null && flag) {
					flagMap.put(request.getProductId(), false);
				}
				
				// 如果是Redis缓存刷新的请求，而且发现标识不为null，但为false
				// 说明上一个请求是Redis缓存刷新请求了，而不是更新MySQL中商品的请求，所以没有必要去MySQL中重新获取。
				if(flag != null && !flag) {
					// 对于这种请求，直接就抛弃，因为没有必要再次刷新
					return;
				}
			}
			
			// 根据请求的商品id，获取应路由到的内存队列
			ArrayBlockingQueue<Request> queue = getRoutingQueue(request.getProductId());

			// 将请求放入对应的队列中，完成路由操作
			queue.put(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 获取路由到的内存队列
	 * @param productId 商品id
	 * @return 内存队列
	 */
	private ArrayBlockingQueue<Request> getRoutingQueue(Long productId) {
		RequestQueue requestQueue = RequestQueue.getInstance();
		
		// 先获取productId的hash值
		String key = String.valueOf(productId);
		int hash = hash(key);

		// 路由算法：
		// 对hash值取模，将hash值路由到指定的内存队列中
		// 同一个商品id的所有相关请求，都会被路由到同一个内存队列中
		int index = (requestQueue.queueSize() - 1) & hash;

		LOGGER.info("=====日志=====: 路由到内存队列，商品id=" + productId + ", 队列索引=" + index);
		
		return requestQueue.getQueue(index);
	}

	private int hash(String key) {
		int h;
		// 除默认的hash函数外，添加这个扰动函数，目的是让key的hash值的高16位也参与hash运算
		return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

	}
}
