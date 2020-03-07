package com.lvshop.cache.hystrix.command.out;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.lvshop.common.pojo.ProductInfo;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * 将商品信息存到Redis中的command
 * @author Galliano
 */
public class SaveProductInfo2ReidsCacheCommand extends HystrixCommand<Boolean> {

	@Autowired
	private StringRedisTemplate redisTemplate;

	private ProductInfo productInfo;
	
	public SaveProductInfo2ReidsCacheCommand(ProductInfo productInfo) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(100)
						.withCircuitBreakerRequestVolumeThreshold(1000)
						.withCircuitBreakerErrorThresholdPercentage(70)
						.withCircuitBreakerSleepWindowInMilliseconds(60 * 1000))
				);  
		this.productInfo = productInfo;
	}
	
	@Override
	protected Boolean run() throws Exception {
		String key = "product_info_" + productInfo.getId();
		redisTemplate.opsForValue().set(key, JSONObject.toJSONString(productInfo));
		return true;
	}  
	
	@Override
	protected Boolean getFallback() {
		return false;
	}
	
}
