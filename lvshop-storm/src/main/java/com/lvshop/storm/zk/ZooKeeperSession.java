package com.lvshop.storm.zk;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
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
					50000,
					new ZooKeeperWatcher() // 监听器，监听何时完成与 zk server 的连接
			);
			// 状态应为：CONNECTING(连接中)
			LOGGER.info("ZooKeeper的状态为 : " + zookeeper.getState());

			try {
				// CountDownLatch的构造参数是(int)count
				// 其他线程每次调用coutnDown()，count都会减1
				// await()指：等待，直到count减到 0 为止
				connectedSemaphore.await();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}

			LOGGER.info("ZooKeeper Session 建立完成");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 获取分布式锁
	 */
	public void acquireDistributedLock() {
		String path = "/taskid-list-lock";
	
		try {
			zookeeper.create(path, "".getBytes(), 
					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			LOGGER.info("获取分布式锁 taskid-list-lock");
		} catch (Exception e) {
			// 如果那个商品对应的锁的node已经存在了，就是已经被别人加锁了，那么就会报错:NodeExistsException
			int count = 0;
			while(true) {
				try {
					Thread.sleep(1000); 
					zookeeper.create(path, "".getBytes(), 
							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				} catch (Exception e2) {
					count++;
					LOGGER.info("第" + count + "次尝试获取分布式锁 taskid-list-lock ......");
					continue;
				}
				LOGGER.info("尝试" + count + "次后，成功获取分布式锁 taskid-list-lock");
				break;
			}
		}
	}
	
	/**
	 * 释放分布式锁
	 */
	public void releaseDistributedLock() {
		String path = "/taskid-list-lock";
		try {
			zookeeper.delete(path, -1);
			LOGGER.info("释分布式锁 taskid-list-lock ......");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取节点"/taskid-list"的数据
	public String getNodeData() {
		try {
			return new String(zookeeper.getData("/taskid-list", false, new Stat()));  
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}




	// =========== 创建、删除、设置、获取节点 ===========
	/**
	 * 创建节点
	 * @param path
	 */
	public void createNode(String path) {
		try {
			zookeeper.create(path, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			LOGGER.info("创建节点成功 [path = " + path + "]");
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
			LOGGER.info("删除节点成功 [path = " + path + "]");
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
			LOGGER.info("设置节点数据成功 [path = " + path + ", date = " + data +"]");
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
	 * 建立zk session 的 watcher
	 * @author Administrator
	 *
	 */
	private class ZooKeeperWatcher implements Watcher {

		public void process(WatchedEvent event) {
			LOGGER.info("接收到监听事件: " + event.getState());
			if(KeeperState.SyncConnected == event.getState()) {
				connectedSemaphore.countDown(); // 就是减1
			} 
		}
	}


	/**
	 * 封装单例的静态内部类
	 * @author Galliano
	 */
	private static class Singleton {
		private static ZooKeeperSession instance;
		static {
			instance = new ZooKeeperSession();
		}
		public static ZooKeeperSession getInstance() {
			return instance;
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
