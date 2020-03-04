package com.lvshop.inventory.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.lvshop.inventory.thread.RequestProcessorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统初始化监听器
 * @author Galliano
 *
 */
public class InitListener implements ServletContextListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(InitListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// 初始化请求处理工作线程池和内存队列
		RequestProcessorThreadPool.init();
		LOGGER.info("[已初始化请求处理线程池和内存队列]");
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
	}

}
