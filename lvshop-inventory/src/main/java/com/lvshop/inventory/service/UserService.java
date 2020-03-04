package com.lvshop.inventory.service;

import com.lvshop.inventory.pojo.User;

/**
 * 用户Service接口
 * @author Galliano
 *
 */
public interface UserService {

	/**
	 * 往MySQL中新增用户信息
	 * @param user
	 */
	void saveUserInfo2DB(User user);

	/**
	 * 在MySQL中删除用户信息
	 * @param userId
	 */
	void removeUserInfoFromDB(Long userId);

	/**
	 * 在MySQL中修改用户信息
	 * @param user
	 */
	void updateUserInfo2DB(User user);

	/**
	 * 根据id在MySQL中查询用户信息
	 * @return 用户信息
	 */
	User findUserInfoFromDB(Long userId);
	
	/**
	 * 查询redis中缓存的用户信息
	 * @return
	 */
	User getCachedUserInfo();
	
}
