package com.lvshop.cache.hystrix.command.out;


import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.lvshop.common.pojo.ShopInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;


//import com.lvshop.cache.spring.SpringContext;
//import redis.clients.jedis.JedisCluster;


/**
 * 将店铺信息存到Redis中的command
 * @author Galliano
 */
public class SaveShopInfo2ReidsCacheCommand extends HystrixCommand<Boolean> {

	@Autowired
	private StringRedisTemplate redisTemplate;

	private ShopInfo shopInfo;
	
	public SaveShopInfo2ReidsCacheCommand(ShopInfo shopInfo) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(100)
						.withCircuitBreakerRequestVolumeThreshold(1000)
						.withCircuitBreakerErrorThresholdPercentage(70)
						.withCircuitBreakerSleepWindowInMilliseconds(60 * 1000))
				);  
		this.shopInfo = shopInfo;
	}
	
	@Override
	protected Boolean run() throws Exception {
		String key = "shop_info_" + shopInfo.getId();
		redisTemplate.opsForValue().set(key, JSONObject.toJSONString(shopInfo));
		return true;
	}
	
	@Override
	protected Boolean getFallback() {
		return false;
	}

}
