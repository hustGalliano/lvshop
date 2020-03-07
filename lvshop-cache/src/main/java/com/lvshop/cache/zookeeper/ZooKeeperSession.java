package com.lvshop.cache.zookeeper;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZooKeeperSession
 * @author Galliano
 */
public class ZooKeeperSession {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperSession.class);
	
	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	private ZooKeeper zookeeper;

	public ZooKeeperSession() {
		try {
			this.zookeeper = new ZooKeeper(
//					"hang1:2181, hang2:2181, hang3:2181",  // 构建集群
					"hang1:2181",
					5000,
					new ZooKeeperWatcher() // 监听器，监听何时完成与 zk server 的连接
			);
			// 状态应为：CONNECTING(连接中)
			LOGGER.info("[ ZooKeeper的状态为 : {} ]", zookeeper.getState());
			
			try {
				// CountDownLatch的构造参数是(int)count
				// 其他线程每次调用coutnDown()，count都会减1
				// await()指：等待，直到count减到 0 为止
				connectedSemaphore.await();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}

			LOGGER.info("[ ZooKeeper Session 建立完成 ]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * 获取分布式锁
	 * @param path
	 * 轮询，不可重入
	 */
	public void acquireDistributedLock(String path) {
		try {
			zookeeper.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			LOGGER.info("[ 获取分布式锁成功 ] path = {}", path);
		} catch (Exception e) {
			// 如果对应的锁的node已存在，说明已被别人加锁了，zk会产生异常:NodeExistsException
			// 重试次数计数器
			int count = 1;
			for (;;) {
				try {
					Thread.sleep(100);
					zookeeper.create(path, "".getBytes(), 
							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				} catch (Exception e2) {
					LOGGER.info("[ 第 {} 次重试获取分布式锁失败 ] path = {}", count, path);
					count++;
					continue;
				}
				LOGGER.info("[ 重试 {} 次后，成功获取分布式锁 ] path = {}", count, path);
				break;
			}
		}
	}
	
	/**
	 * FastFail 获取分布式锁，若获取失败，则不重试，直接返回
	 * @param path
	 */
	public boolean acquireFastFailedDistributedLock(String path) {
		try {
			zookeeper.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			LOGGER.info("[ 获取分布式锁成功 ] path = {}", path);
			return true;
		} catch (Exception e) {
			LOGGER.info("[ 获取分布式锁失败 ] path = {}", path);
		}
		return false;
	}
	
	/**
	 * 释放分布式锁
	 * @param path
	 * path 的取值：
	 * 		"/product-lock-" + productId、
	 * 		"/shop-lock-" + shopId、
	 * 		"/taskid-status-lock-" + taskid、
	 * 		"/taskid-lock-" + taskid。
	 */
	public void releaseDistributedLock(String path) {
		try {
			zookeeper.delete(path, -1);
			LOGGER.info("[ 成功释放分布式锁 ] path = {}", path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



    // =========== 创建、删除、设置、获取节点 ===========
	/**
	 * 创建节点
	 * @param path
	 */
	public void createNode(String path) {
		try {
			zookeeper.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			LOGGER.info("[ 创建节点成功 ] path = {}", path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除节点
	 * @param path
	 */
	public void removeNode(String path) {
		try {
			zookeeper.delete(path, -1);
			LOGGER.info("[ 删除节点成功 ] path = {}", path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置节点数据
	 * @param path
	 * @param data
	 */
	public void setNodeData(String path, String data) {
		try {
			zookeeper.setData(path, data.getBytes(), -1);
			LOGGER.info("[ 设置节点数据成功 ]path = {}, date = {}", path , data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取节点数据
	 * @param path
	 */
	public String getNodeData(String path) {
		try {
			return new String(zookeeper.getData(path, false, new Stat()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}





	/**
	 * 建立 ZooKeeper 会话的 watcher
	 * @author Galliano
	 */
	private class ZooKeeperWatcher implements Watcher {

		public void process(WatchedEvent event) {
			System.out.println("接受到监听事件 : " + event.getState());
			if(KeeperState.SyncConnected == event.getState()) {
				connectedSemaphore.countDown();
			} 
		}
	}
	
	/**
	 * 封装 ZKSession 单例的静态内部类
	 * @author Galliano
	 */
	private static class Singleton {
		private static ZooKeeperSession zkSession;
		static {
			zkSession = new ZooKeeperSession();
		}
		public static ZooKeeperSession getInstance() {
			return zkSession;
		}
	}

	// 获取单例
	public static ZooKeeperSession getInstance() {
		return Singleton.getInstance();
	}

	// 快速初始化单例
	public static void init() {
		getInstance();
	}
}
