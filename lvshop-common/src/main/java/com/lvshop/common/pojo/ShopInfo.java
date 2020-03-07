package com.lvshop.common.pojo;

/**
 * 店铺信息pojo类
 * @author Galliano
 */
public class ShopInfo {

	private Long id;
	private String name;
	private Integer level;
	private Double goodCommentRate;
	private String modifiedTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Double getGoodCommentRate() {
		return goodCommentRate;
	}

	public void setGoodCommentRate(Double goodCommentRate) {
		this.goodCommentRate = goodCommentRate;
	}

	public String getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(String modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	@Override
	public String toString() {
		return "ShopInfo{" +
				"id=" + id +
				", name='" + name + '\'' +
				", level=" + level +
				", goodCommentRate=" + goodCommentRate +
				", modifiedTime='" + modifiedTime + '\'' +
				'}';
	}
}
