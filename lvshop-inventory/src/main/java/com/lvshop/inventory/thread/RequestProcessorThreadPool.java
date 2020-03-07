package com.lvshop.inventory.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.lvshop.inventory.request.Request;
import com.lvshop.inventory.request.RequestQueue;

/**
 * 请求处理线程池：单例
 * @author Galliano
 */
public class RequestProcessorThreadPool {

	// 本来线程池大小、每个线程监控的那个内存队列的大小 都应该写到配置文件中
	// 内存队列大小
	private int queueSize = 100;
	// 线程池大小
	private int poolSize = 10;

	// 线程池
	private ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
	
	public RequestProcessorThreadPool() {

		RequestQueue requestQueue = RequestQueue.getInstance();

		for(int i = 0; i < poolSize; i++) {

			LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<>(queueSize);

			// 将 内存队列 加入到 内存队列List 中
			requestQueue.addQueue(queue);

			// 用一个线程专门处理一个内存队列，并将这个线程加入到线程池中，执行这个线程
			threadPool.submit(new RequestProcessorThread(queue));  
		}
	}


	/**
	 * 单例模式
	 * @author Galliano
	 * 使用静态内部类的方式(线程绝对安全)，去初始化单例
	 */
	private static class Singleton {
		private static RequestProcessorThreadPool instance;
		static {
			instance = new RequestProcessorThreadPool();
		}
		public static RequestProcessorThreadPool getInstance() {
			return instance;
		}
		
	}
	
	/**
	 * @return
	 * 内部类的初始化中，不管多少个线程并发去初始化，一定只会发生一次。
	 */
	public static RequestProcessorThreadPool getInstance() {
		return Singleton.getInstance();
	}

	// 快速初始化
	public static void init() {
		getInstance();
	}
	
}
