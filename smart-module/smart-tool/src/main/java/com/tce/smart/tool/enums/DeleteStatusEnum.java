package com.tce.smart.tool.enums;

/**
 * 删除状态枚举0-否（未删除） 1-是 （已删除）
 *
 * @author 齐佩
 *
 */
public enum DeleteStatusEnum {
	IS_DELETE(1, "是"), NOT_DELETE(0, "否");
	private final Integer code;
	private final String desc;

	DeleteStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public Integer getCode(String desc) {
		return this.code;
	}

	public String getDesc(Integer code) {
		return this.desc;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
