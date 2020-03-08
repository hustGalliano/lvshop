package com.lvshop.cache.prewarm;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONArray;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.service.CacheService;
//import com.lvshop.cache.spring.SpringContext;
import com.lvshop.cache.zookeeper.ZooKeeperSession;
import com.lvshop.common.service.ProductInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 缓存预热线程
 * @author Galliano
 */
public class CachePrewarmThread implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(CachePrewarmThread.class);

	// 从Dubbo注入实例
	@Reference(loadbalance="random",timeout=1000)
	private ProductInfoService productInfoService;

	@Autowired
	private CacheService cacheService;

	@Override
	public void run() {
		ZooKeeperSession zkSession = ZooKeeperSession.getInstance();
		
		// 获取storm taskid列表
		String taskidList = zkSession.getNodeData("/taskid-list");

		LOGGER.info("[ 获取到taskid列表 ] taskidList = {}", taskidList);

		// 为什么要设置预热状态？：确保一个taskid对应的热点商品数据只会被缓存服务实例预热一次，避免重复预热。
		if(taskidList != null && !"".equals(taskidList)) {
			// 将原来组装的列表切分为原本的taskid数组
			String[] taskidListSplited = taskidList.split(",");  
			for(String taskid : taskidListSplited) {
				// 上锁
				// 只尝试获取锁一次，成功则true，失败则false
				String taskidLockPath = "/taskid-lock-" + taskid;
				boolean result = zkSession.acquireFastFailedDistributedLock(taskidLockPath);
				if(!result) {
					// 若失败了，证明有其他服务实例线程正在预热该taskid，所以跳过该taskid
					continue;
				}
				// 代码运行到这，说明上面已经获取到 taskid 锁了

				// 获取预热状态值
				String taskidStatusPath = "/taskid-status-" + taskid;
				String taskidStatus = zkSession.getNodeData(taskidStatusPath);
				LOGGER.info("[ 获取task的预热状态 ] taskid = {}, status = {}", taskid, taskidStatus);

				// 如果为空，说明这个taskid对应的热点数据列表没有被预热过，所以需要预热
				if("".equals(taskidStatus)) {
					// 获取storm的taskid对应的zk节点存储的热点商品列表
					String productidList = zkSession.getNodeData("/task-hot-product-list-" + taskid);

					LOGGER.info("[ 获取到task的热门商品列表 ] productidList = {}", productidList);

					JSONArray productidJSONArray = JSONArray.parseArray(productidList);
					
					for(int i = 0; i < productidJSONArray.size(); i++) {
						Long productId = productidJSONArray.getLong(i);

						// 利用Dubbo来远程调用数据源服务的方法
						ProductInfo productInfo = productInfoService.findProductInfoById(productId);

//						String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", " +
//								"\"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", " +
//								"\"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", " +
//								"\"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1," +
//								" \"modifiedTime\": \"2017-01-01 12:00:00\"}";
//						ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);

						// 缓存预热：将热点数据存入EhCache和Redis中
						cacheService.saveProductInfo2LocalCache(productInfo);
						LOGGER.info("[ 将商品数据设置到本地缓存中 ] productInfo = {}", productInfo);
						cacheService.saveProductInfo2ReidsCache(productInfo);
						LOGGER.info("[ 将商品数据设置到redis缓存中 ] productInfo = {}", productInfo);
					}

					// 预热结束了，将状态值改为success。
					// 下次若再获取到这个taskid，taskidStatus就不为空，就不会进入到这个if块中。
					// 避免了重复预热某个taskid上的数据。
					if (!zkSession.existNode(taskidStatusPath)) {
						zkSession.createNode(taskidStatusPath);
					}
					zkSession.setNodeData(taskidStatusPath, "success");
				}

				zkSession.releaseDistributedLock(taskidLockPath);
			}
		}
	}
}
