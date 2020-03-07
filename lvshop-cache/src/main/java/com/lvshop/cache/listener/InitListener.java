package com.lvshop.cache.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.lvshop.cache.rebuild.RebuildShopInfoCacheThread;
import com.lvshop.cache.zookeeper.ZooKeeperSession;
import com.lvshop.cache.rebuild.RebuildProductInfoCacheThread;

//import com.lvshop.cache.kafka.KafkaConsumer;
//import com.lvshop.cache.spring.SpringContext;


/**
 * 系统初始化的监听器
 * @author Galliano
 */
public class InitListener implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent sce) {

		// 开启ProductInfo和ShopInfo的缓存重建线程
		new Thread(new RebuildProductInfoCacheThread()).start();
		new Thread(new RebuildShopInfoCacheThread()).start();

		// 初始化 ZK 实例
		ZooKeeperSession.init();
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
	}

}
