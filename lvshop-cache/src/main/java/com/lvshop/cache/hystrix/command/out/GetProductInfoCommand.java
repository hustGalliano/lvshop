package com.lvshop.cache.hystrix.command.out;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lvshop.common.service.ProductInfoService;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.lvshop.common.pojo.ProductInfo;

/**
 *
 * @author Galliano
 */
public class GetProductInfoCommand extends HystrixCommand<ProductInfo> {

	// 从Dubbo注入实例
	@Reference(version = "1.0", url = "dubbo://hang:20880")
	private ProductInfoService productInfoService;

	private Long productId;
	
	public GetProductInfoCommand(Long productId) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ProductService"))
				.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
						.withCoreSize(10)
						.withMaximumSize(30)
						.withAllowMaximumSizeToDivergeFromCoreSize(true)
						.withKeepAliveTimeMinutes(1)
						.withMaxQueueSize(50)
						.withQueueSizeRejectionThreshold(100))
		);
		this.productId = productId;
	}
	
	@Override
	protected ProductInfo run() throws Exception {

		/*
		String productInfoJSON = HttpClientUtils.sendGetRequest("localhost:8081/getProductInfo?productId=" + productId);
		if (productInfoJSON == null || productInfoJSON == "") {
			// 若某个商品id在数据源服务中没有查询到数据，则直接返回一个只有id的实例
			ProductInfo productInfo = new ProductInfo();
			productInfo.setId(productId);
			return productInfo;
		} else {
			// 发送http或rpc接口调用，去调用数据源服务的接口
//			String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:01:00\"}";
			return JSONObject.parseObject(productInfoJSON, ProductInfo.class);
		}
		*/

		// 利用Dubbo来RPC数据源服务的接口
		ProductInfo productInfo = productInfoService.findProductInfoById(productId);

		// 若某个商品id在数据源服务中没有查询到数据，则直接返回一个只有id的实例
		if (productInfo == null) {
			productInfo = new ProductInfo();
			productInfo.setId(productId);
		}

		return productInfo;
	}

	/**
	 * 此方法包含多层嵌套降级
	 * @return
	 */
	@Override
	protected ProductInfo getFallback() {
		HBaseColdDataCommand command = new HBaseColdDataCommand(productId);
		return command.execute();
	}



/*	*//**
	 * 用HBase做冷备份
	 * HBase中的数据，都是过时的数据，但可以用来撑撑场面
	 *//*
	private class HBaseColdDataCommand extends HystrixCommand<ProductInfo> {

		private Long productId;
		
		public HBaseColdDataCommand(Long productId) {
			super(HystrixCommandGroupKey.Factory.asKey("HBaseGroup"));
			this.productId = productId;
		}
		
		@Override
		protected ProductInfo run() throws Exception {
			// 假装这个是查询hbase得来的结果
			String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:01:00\"}";
			return JSONObject.parseObject(productInfoJSON, ProductInfo.class);
		}
		
		@Override
		protected ProductInfo getFallback() {
			ProductInfo productInfo = new ProductInfo();
			productInfo.setId(productId);
			// 从内存中找一些残缺的数据拼装进去
			return productInfo;
		}
	}*/
}
