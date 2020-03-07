package com.lvshop.inventory.controller;

import com.lvshop.inventory.pojo.User;
import com.lvshop.inventory.service.UserService;
import com.lvshop.inventory.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户Controller
 * @author Galliano
 *
 */
@RestController
public class UserController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;


	@RequestMapping("/saveUserInfo")
	public Response saveUserInfo(User user) {
		LOGGER.info("[ 接收到新增用户信息的请求 ] 用户id = {}, 用户姓名 = {}, 用户年龄 = {}",
				user.getUserId(), user.getName(), user.getAge());

		try {
			userService.saveUserInfo2DB(user);
			return new Response(Response.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Response.FAILURE);
		}
	}

	@RequestMapping("/removeUserInfo")
	public Response removeUserInfo(Long userId) {
		LOGGER.info("[ 接收到删除用户信息的请求 ] 用户id = {}", userId);

		try {
			userService.removeUserInfoFromDB(userId);
			return new Response(Response.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Response.FAILURE);
		}
	}

	@RequestMapping("/updateUserInfo")
	public Response updateUserInfo(User user) {
		LOGGER.info("[ 接收到更新用户信息的请求 ] 用户id = {}, 用户姓名 = {}, 用户年龄 = {}",
				user.getUserId(), user.getName(), user.getAge());

		try {
			userService.updateUserInfo2DB(user);
			return new Response(Response.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			return new Response(Response.FAILURE);
		}
	}

	@RequestMapping("/getUserInfo")
	public User getUserInfo(Long userId) {
		LOGGER.info("[ 接收到查询用户信息的请求 ] 用户id = {}", userId);
		User user = null;

		try {
			user = userService.findUserInfoFromDB(userId);
			user.setUserId(userId);

			return user;
		} catch (Exception e) {
			return null;
		}
	}

	
//	@RequestMapping("/getCachedUserInfo")
//	public User getCachedUserInfo() {
//		User user = userService.getCachedUserInfo();
//		return user;
//	}
	
}
