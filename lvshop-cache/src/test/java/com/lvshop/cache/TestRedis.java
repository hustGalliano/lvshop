package com.lvshop.cache;

import com.alibaba.fastjson.JSONObject;
import com.lvshop.common.pojo.ProductInfo;
import com.lvshop.common.service.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Galliano
 */


@RunWith(SpringRunner.class)
@SpringBootTest(classes = LvshopCacheApplication.class)
public class TestRedis {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("lv", "hang");
        System.out.println(redisTemplate.opsForValue().get("lv"));

        System.out.println("========================");

        String json = "{\"color\":\"红色,白色,黑色\",\"id\":3,\"modifiedTime\":\"2017-01-01 12:01:00\",\"name\":\"iphone7手机\",\"pictureList\":\"a.jpg,b.jpg\",\"price\":5599,\"service\":\"iphone7的售后服务\",\"shopId\":1,\"size\":\"5.5\",\"specification\":\"iphone7的规格\"}";
        ProductInfo productInfo = JSONObject.parseObject(json, ProductInfo.class);


        cacheService.saveProductInfo2ReidsCache(productInfo);
        System.out.println(cacheService.getProductInfoFromRedisCache(productInfo.getId()));
    }
}
