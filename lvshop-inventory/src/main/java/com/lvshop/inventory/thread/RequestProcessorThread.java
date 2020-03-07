package com.lvshop.inventory.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import com.lvshop.inventory.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行请求的工作线程
 * @author Galliano
 */
public class RequestProcessorThread implements Callable<Boolean> {
	private final static Logger LOGGER = LoggerFactory.getLogger(RequestProcessorThread.class);

	// 阻塞式内存队列(线程安全，容量不限)，如果队列满了、或是空的，都会在执行操作的时候，阻塞住。
	private LinkedBlockingQueue<Request> queue;

	public RequestProcessorThread(LinkedBlockingQueue<Request> queue) {
		this.queue = queue;
	}
	
	@Override
	public Boolean call() {
		try {
			while (true) {
				Thread.sleep(10);

				// 从内存队列中拉取请求
				Request request = queue.take();

				LOGGER.info("[ 工作线程处理请求 ] 商品id = {}", request.getProductId());

				// 执行这个请求操作
				request.process();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
