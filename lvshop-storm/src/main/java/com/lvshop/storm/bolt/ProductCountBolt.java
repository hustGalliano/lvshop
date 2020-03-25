package com.lvshop.storm.bolt;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import com.lvshop.storm.http.HttpClientUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.storm.shade.org.json.simple.JSONArray;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvshop.storm.zk.ZooKeeperSession;
import scala.util.parsing.combinator.testing.Str;

/**
 * 商品访问次数统计的bolt
 * @author Galliano
 */
public class ProductCountBolt extends BaseRichBolt {
	private static final long serialVersionUID = -8761807561458126413L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductCountBolt.class);
	private static final String[] allAppNginxs = new String[]{
			"http://hang2",
			"http://hang3"
	};

	// 只保存最近经常使用的数据  <productId, accessCount>
	private LRUMap<Long, Long> productCountMap = new LRUMap<>(1000);
	private ZooKeeperSession zkSession;
	private int taskid;

	@SuppressWarnings("rawtypes")
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		this.zkSession = ZooKeeperSession.getInstance();
		this.taskid = context.getThisTaskId();

		new Thread(new ProductCountThread(), "ProductCountThread").start();
		new Thread(new HotProductFindThread(), "HotProductFindThread").start();

		// 1、将自己的taskid写入一个zookeeper node中，形成taskid的列表
		// 2、然后每次都将自己的热门商品列表，写入自己的taskid对应的zookeeper节点
		// 3、这样的话，并行预热程序才能从第一步中知道，有哪些taskid
		// 4、然后并行预热程序根据每个taskid去获取一个锁，然后再从对应的znode中拿到热门商品列表
		initTaskId(context.getThisTaskId());
	}

	private void initTaskId(int taskid) {
		// ProductCountBolt所有的task启动的时候，都会基于分布式锁将将自己的taskid写到同一个node的值中
		// 格式是逗号分隔，拼接成一个像这样的列表（11,13,22）

		// 因为多个task可能并发执行，所以需要加 zk 分布式锁
		// 上锁
		String taskidListLockPath = "/taskid-list-lock";
		zkSession.acquireDistributedLock(taskidListLockPath);

		zkSession.createNode("/taskid-list");

		// 获取节点"/taskid-list"的数据
		String taskidList = zkSession.getNodeData("/taskid-list");

		LOGGER.info("[ ProductCountBolt获取到taskid list ] taskidList = {}", taskidList);

		if(!"".equals(taskidList)) {
			taskidList += "," + taskid;
		} else {
			taskidList += taskid;
		}

		zkSession.setNodeData("/taskid-list", taskidList);
		LOGGER.info("[ ProductCountBolt设置taskid list ] taskidList = {}", taskidList);

		// 解锁
		zkSession.releaseDistributedLock(taskidListLockPath);
	}

	/**
	 * 开启一个单独的后台线程，算出热点数据
	 * @author Galliano
	 */
	private class HotProductFindThread implements Runnable {
		private final int Threshold = 10;

		@SuppressWarnings("deprecation")
		public void run() {
			// <productId, count>
			List<Map.Entry<Long, Long>> productCountList;
			// 热点商品id的列表
			List<Long> hotProductIdList = new ArrayList<>();
			// 上次算出的热点商品id的列表
			List<Long> lastTimeHotProductIdList = new ArrayList<>();

			for (;;) {
				// 1、将LRUMap中的数据按照访问次数，进行全局的排序
				// 2、计算95%的商品的访问次数的平均值
				// 3、遍历排序后的商品访问次数，从最大的开始
				// 4、如果某个商品比如它的访问量是平均值的10倍，就认为是缓存的热点
				try {
					if(productCountMap.size() == 0) {
						Utils.sleep(200);
						continue;
					}
					LOGGER.info("[ 打印productCountMap的长度 ] size = {}", productCountMap.size());

					// 1、先做全局的降序排序
					productCountList = new ArrayList<>(productCountMap.entrySet());
					productCountList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
					LOGGER.info("[ 全局排序后的结果 ] productCountList = {}", productCountList);

					// 2、计算出后 95% 的商品访问次数的平均值
					int calculateCount = (int)Math.floor(productCountList.size() * 0.95);
					if (calculateCount == 0) calculateCount = 1;

					Long totalCount = 0L;

					int size = productCountList.size();
					if (size > 1) {
						for(int i = size - 1; i >= size - calculateCount; i--) {
							totalCount += productCountList.get(i).getValue();
						}
					} else {
						totalCount = productCountList.get(0).getValue();
					}

					Long avgCount = totalCount / calculateCount;
					LOGGER.info("[ 计算出后95%的商品的访问次数平均值 ] avgCount = {}", avgCount);


					// 3、从第一个元素开始遍历，判断是否是平均值的10倍
					for(Map.Entry<Long, Long> productCountEntry : productCountList) {
						if(productCountEntry.getValue() > Threshold * avgCount) {
							Long hotKey = productCountEntry.getKey();
							Long hotValue = productCountEntry.getValue();
							LOGGER.info("[ 发现一个热点 ] productCountEntry = {{} : {}}", hotKey, hotValue);

							// 将热点商品的id加入到热点id列表中
							hotProductIdList.add(hotKey);

							// 进入此if块。说明这个热点数据是新增的热点数据
							if(!lastTimeHotProductIdList.contains(hotKey)) {
								// 将缓存热点反向推送到分发层Nginx中
								String distributeNginxURL = "http://hang1/hot?productId=" + hotKey;
								HttpClientUtils.sendGetRequest(distributeNginxURL);

								// 将缓存热点商品id对应的完整商品数据，发送请求到缓存服务去获取，反向推送到所有的后端应用nginx服务器上去
								String cacheServiceURL = "http://hang:8080/getProductInfo?productId=" + productCountEntry.getKey();
								String response = HttpClientUtils.sendGetRequest(cacheServiceURL);

								List<NameValuePair> params = new ArrayList<>();
								params.add(new BasicNameValuePair("productInfo", response));
								String productInfo = URLEncodedUtils.format(params, HTTP.UTF_8);

								// 向每一个应用层Nginx发送要缓存的热点数据
								for (String appNginx : allAppNginxs) {
									String appNginxURL = appNginx + "/hot?productId=" + hotKey + "&" + productInfo;
									HttpClientUtils.sendGetRequest(appNginxURL);
								}
							}
						}
					}

					// 4、实时感知热点数据的消失
					// 这个for循环的作用：在Nginx中删除不再是热点数据的缓存
					for (Long productId : lastTimeHotProductIdList) {
						if (!hotProductIdList.contains(productId)) {
							// 说明上次是热点的商品id，不再是热点了
							// 发送一个http请求给到流量分发的nginx中，取消热点缓存的标识
							LOGGER.info("[ 发现一个热点消失了 ] productId = {}", productId);
							String cancelHotURL = "http://hang1/cancel_hot?productId=" + productId;
							HttpClientUtils.sendGetRequest(cancelHotURL);
						}
					}

					// 将这次热点数据 全盘copy到 上次热点数据列表中。
					lastTimeHotProductIdList.clear();
					if (hotProductIdList.size() > 0) {
						lastTimeHotProductIdList.addAll(hotProductIdList);
						LOGGER.info("[ 保存上次热点数据 ] lastTimeHotProductIdList = {}", lastTimeHotProductIdList);
					}

					// 清零，准备开始新一轮的计算
					productCountList.clear();
					hotProductIdList.clear();
					Utils.sleep(60 * 1000);
				} catch (Exception e) {
					LOGGER.info(e.getMessage());
				}
			}
		}
	}

	/**
	 * 开启一个单独的后台线程，每隔1分钟算出热数据（top3热门商品list）
	 * @author Galliano
	 */
	private class ProductCountThread implements Runnable {
		public void run() {
			// 要计算的热数据的数量
			int topn = 3;

			// 使用小根堆 (以Entry的value为准)，存储以商品访问量降序的热数据(排名前N)   <productid, 商品访问量>
			PriorityQueue<Map.Entry<Long, Long>> topnProductHeap =
					new PriorityQueue<>(topn, Comparator.comparing(Map.Entry::getValue));

			// 降序的热数据id列表，就是将 topnProductList 的数据的key依次取了出来。
			List<Long> productidList = new ArrayList<>();

			for (;;) {
				try {
					if(productCountMap.size() == 0) {
						Utils.sleep(200);
						continue;
					}
					LOGGER.info("[ 打印 productCountMap的长度 ] size = {}", productCountMap.size());

					// productCountEntry 是 LRUMap 中的 Entry
					for(Map.Entry<Long, Long> productCountEntry : productCountMap.entrySet()) {
						// 先将堆填满
						if (topnProductHeap.size() < topn ) {
							topnProductHeap.add(productCountEntry);
						}
						// 只有比堆顶大的元素才加入堆，这样才能选出最大的topn元素
						else if (topnProductHeap.peek().getValue() < productCountEntry.getValue()) {
							topnProductHeap.poll();
							topnProductHeap.add(productCountEntry);
						}
					}

					// 获取到一个topn list
					topnProductHeap.forEach((topnProductEntry) -> productidList.add(topnProductEntry.getKey()));

					// 每个storm task将自己统计出的热数据list写入自己对应的znode中
					String topnProductListJSON = JSONArray.toJSONString(productidList);

					String hotPath = "/task-hot-product-list-" + taskid;

					// 若节点不存在，则创建节点
					if (!zkSession.existNode(hotPath)) {
						zkSession.createNode(hotPath);
					}
					zkSession.setNodeData(hotPath, topnProductListJSON);

					LOGGER.info("[ 计算出一份top3热门商品列表 ] zk path = {}, topnProductListJSON = {}",
							hotPath, topnProductListJSON);

					// 清零，准备开始新一轮的计算
					topnProductHeap.clear();
					productidList.clear();

					// 1 分钟算 1 轮
					Utils.sleep(60 * 1000);
				} catch (Exception e) {
					LOGGER.info(e.getMessage());
				}
			}
		}
	}

	public void execute(Tuple tuple) {
		Long productId = tuple.getLongByField("productId");

		LOGGER.info("[ ProductCountBolt接收到一个商品id ] productId = {}", productId);

		Long count = productCountMap.getOrDefault(productId, 0L) + 1L;
		// 往 LRU Map 中更新数据
		productCountMap.put(productId, count);

		LOGGER.info("[ ProductCountBolt完成商品访问次数统计 ] productId = {}, count = {}", productId, count);
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}
}
