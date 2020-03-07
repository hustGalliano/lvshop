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
 * 从Redis中获取店铺信息的command
 * @author Galliano
 */
public class GetShopInfoFromReidsCacheCommand extends HystrixCommand<ShopInfo> {

	@Autowired
	private StringRedisTemplate redisTemplate;

	private Long shopId;
	
	public GetShopInfoFromReidsCacheCommand(Long shopId) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionTimeoutInMilliseconds(100)
						.withCircuitBreakerRequestVolumeThreshold(1000)
						.withCircuitBreakerErrorThresholdPercentage(70)
						.withCircuitBreakerSleepWindowInMilliseconds(60 * 1000))
				);  
		this.shopId = shopId;
	}
	
	@Override
	protected ShopInfo run() throws Exception {
		String key = "shop_info_" + shopId;
		String json = redisTemplate.opsForValue().get(key);

		if(json != null) {
			return JSONObject.parseObject(json, ShopInfo.class);
		}
		return null;
	} 
	
	@Override
	protected ShopInfo getFallback() {
		return null;
	}

}
