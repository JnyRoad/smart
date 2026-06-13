package com.tce.smart.tool.enums;

import java.util.Objects;

/**
 * 员工充值名单类型
 *
 * @author mckaywu
 * @date 2019-06-02 16:25:02
 */
public enum RechargeTypeEnum {

	NEW_EMPLOYEE(1, "新员工充值名单"),
	SENIOR_EMPLOYEE(2, "在职员工充值名单");

	private final Integer code;
	private final String desc;

	RechargeTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static RechargeTypeEnum code(Integer code) {
		if (Objects.nonNull(code)) {
			for (RechargeTypeEnum t : RechargeTypeEnum.values()) {
				if (Objects.nonNull(t.code) && t.code.equals(code)) {
					return t;
				}
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
