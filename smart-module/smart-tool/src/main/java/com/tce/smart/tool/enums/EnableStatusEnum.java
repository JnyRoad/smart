package com.tce.smart.tool.enums;

/**
 * 删除状态枚举是否启用
 *
 * @author wuling
 *
 */
public enum EnableStatusEnum {
	ENABLE(1, "启用"), NOT_ENABLE(0, "禁用");
	private final Integer code;
	private final String desc;

	EnableStatusEnum(Integer code, String desc) {
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
