package com.lvshop.inventory.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 请求内存队列
 * @author Galliano
 *
 */
public class RequestQueue {

	// 内存队列List
	private List<LinkedBlockingQueue<Request>> queues = new ArrayList<>();

	// 标识位map
	private Map<Long, Boolean> flagMap = new ConcurrentHashMap<>(); // 线程安全

	private RequestQueue() {
	}

	/**
	 * 单例模式
	 * @author Galliano
	 * 使用静态内部类的方式(线程绝对安全)，去初始化单例
	 */
	private static class Singleton {
		
		private static RequestQueue instance;

		static {
			instance = new RequestQueue();
		}
		
		public static RequestQueue getInstance() {
			return instance;
		}
		
	}
	
	/**
	 * 获取一个RequestQueue实例
	 * @return
	 * 内部类的初始化中，不管多少个线程并发去初始化，一定只会发生一次。
	 */
	public static RequestQueue getInstance() {
		return Singleton.getInstance();
	}
	
	/**
	 * 添加一个内存队列
	 * @param queue
	 */
	public void addQueue(LinkedBlockingQueue<Request> queue) {
		this.queues.add(queue);
	}
	
	/**
	 * 获取内存队列的数量
	 * @return
	 */
	public int queueSize() {
		return queues.size();
	}
	
	/**
	 * 获取对应索引的内存队列
	 * @param index
	 * @return
	 */
	public LinkedBlockingQueue<Request> getQueue(int index) {
		return queues.get(index);
	}

	/**
	 * 获取标志位map
	 * @return
	 */
	public Map<Long, Boolean> getFlagMap() {
		return flagMap;
	}
	
}
