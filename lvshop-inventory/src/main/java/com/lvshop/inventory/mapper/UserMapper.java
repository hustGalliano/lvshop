package com.lvshop.inventory.mapper;

import com.lvshop.inventory.pojo.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {
	/**
	 * 新增用户信息
	 * @param user
	 */
	void saveUserInfo(User user);

	/**
	 * 删除用户信息
	 * @param userId
	 */
	void removeUserInfo(Long userId);

	/**
	 * 修改用户信息
	 * @param user
	 */
	void updateUserInfo(User user);

	/**
	 * 查询用户信息
	 * @return
	 */
	User findUserInfo(Long userId);

}
