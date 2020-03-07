package com.lvshop;

import com.lvshop.inventory.LvshopInventoryApplication;
import com.lvshop.inventory.dao.RedisDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;

/**
 * @author Galliano
 */


@RunWith(SpringRunner.class)
@SpringBootTest(classes = LvshopInventoryApplication.class)
public class TestRedis {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisDAO redisDAO;

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("lv", "hang");
        System.out.println(redisTemplate.opsForValue().get("lv"));

        System.out.println("========================");

        redisDAO.set("rui", "zhen");
        System.out.println(redisDAO.get("rui"));

    }
}
