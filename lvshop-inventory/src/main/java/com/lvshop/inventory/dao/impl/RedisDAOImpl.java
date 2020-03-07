package com.lvshop.inventory.dao.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.lvshop.inventory.dao.RedisDAO;


@Repository("redisDAO")
public class RedisDAOImpl implements RedisDAO {
	@Autowired
	private StringRedisTemplate redisTemplate;


	@Override
	public void set(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}

	@Override
	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public void delete(String key) {
		redisTemplate.delete(key);
	}

	@Override
	public boolean expire(String key, long expire) {
		return false;
	}

	@Override
	public Long increment(String key, long delta) {
		return null;
	}
}
