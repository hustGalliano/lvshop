package com.lvshop.inventory.pojo;

/**
 * 用户pojo类
 * @author Galliano
 */
public class User {

	// 用户id
	private Long userId;
	// 用户姓名
	private String name;
	// 用户年龄
	private Integer age;

	public User() {
	}

	public User(String name, Integer age) {
		this.name = name;
		this.age = age;
	}

	public User(Long userId, String name, Integer age) {
		this.userId = userId;
		this.name = name;
		this.age = age;
	}

	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	
}
