
package com.lvshop.cache.hystrix.command.out;


import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.lvshop.common.pojo.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * 从Redis中获取商品信息的command
 * @author Galliano
 */

public class GetProductInfoFromReidsCacheCommand extends HystrixCommand<ProductInfo> {

	@Autowired
	private StringRedisTemplate redisTemplate;

	private Long productId;


	public GetProductInfoFromReidsCacheCommand(Long productId) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(100)
						.withCircuitBreakerRequestVolumeThreshold(1000)
						.withCircuitBreakerErrorThresholdPercentage(70)
						.withCircuitBreakerSleepWindowInMilliseconds(60 * 1000))
				);  
		this.productId = productId;
	}
	
	@Override
	protected ProductInfo run(){
		try {

            Thread.sleep(10000);

			String key = "product_info_" + productId;
			String json = redisTemplate.opsForValue().get(key);

			if(json != null) {
				return JSONObject.parseObject(json, ProductInfo.class);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected ProductInfo getFallback() {
		System.out.println("=================== 哈哈哈哈哈哈哈哈哈哈哈哈哈 ===================");
		return null;
	}

}

