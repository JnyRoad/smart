package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 保密区签署状态
 * @date: 2020-07-21 14:03
 * @author:
 * @version: 1.0
 */
public enum SecuritySignStatusEnum {

	NON_SING(0, "未签署"),

	SIGN(1,"已签署");

	private final Integer code;

	private final String desc;

	SecuritySignStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SecuritySignStatusEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (SecuritySignStatusEnum enmuTemp : SecuritySignStatusEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		SecuritySignStatusEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SecuritySignStatusEnum enmuTemp : SecuritySignStatusEnum.values()) {
				if (enmuTemp.desc.equals(desc)) {
					return enmuTemp.code;
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
