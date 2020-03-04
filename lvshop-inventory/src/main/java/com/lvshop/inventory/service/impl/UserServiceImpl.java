package com.lvshop.inventory.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.lvshop.inventory.dao.RedisDAO;
import com.lvshop.inventory.mapper.UserMapper;
import com.lvshop.inventory.pojo.User;
import com.lvshop.inventory.service.UserService;

/**
 * 用户Service实现类
 * @author Galliano
 *
 */
@Service("userService")  
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RedisDAO redisDAO;
	

	@Override
	public void saveUserInfo2DB(User user) {
		userMapper.saveUserInfo(user);
	}

	@Override
	public void removeUserInfoFromDB(Long userId) {
		userMapper.removeUserInfo(userId);
	}

	@Override
	public void updateUserInfo2DB(User user) {
		userMapper.updateUserInfo(user);
	}

	@Override
	public User findUserInfoFromDB(Long userId) {
		return userMapper.findUserInfo(userId);
	}

	@Override
	public User getCachedUserInfo() {
		redisDAO.set("cached_user_lisi", "{\"name\": \"lisi\", \"age\":28}");
		
		String userJSON = redisDAO.get("cached_user_lisi");  
		JSONObject userJSONObject = JSONObject.parseObject(userJSON);
		
		User user = new User();
		user.setName(userJSONObject.getString("name"));   
		user.setAge(userJSONObject.getInteger("age"));  
		
		return user;
	}

}
