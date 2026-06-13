package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 保密区预约状态枚举
 * @date: 2020-07-30 10:07
 * @author: wuling
 * @version: 1.0
 */
public enum SecurityAreaVisitStatusEnum {

	APPLY(1, "已申请"),

	PASSED(2,"已通过"),

	RETURNED(3,"已退回");

	private final Integer code;

	private final String desc;

	SecurityAreaVisitStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SecurityAreaVisitStatusEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (SecurityAreaVisitStatusEnum enmuTemp : SecurityAreaVisitStatusEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		SecurityAreaVisitStatusEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SecurityAreaVisitStatusEnum enmuTemp : SecurityAreaVisitStatusEnum.values()) {
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
