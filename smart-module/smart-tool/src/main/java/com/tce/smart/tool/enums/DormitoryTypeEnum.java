package com.tce.smart.tool.enums;

/**
 * 是否为寝室
 */
public enum DormitoryTypeEnum {
	IS_DORMITIRY(0,"是"),
	NOT_DORMITORY(1,"否");
	private final Integer code;
	private final String desc;

	DormitoryTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public Integer getCode(String desc) {
		return this.code;
	}
	public String getDesc(Integer code) {
		return this.desc;
	}
}
