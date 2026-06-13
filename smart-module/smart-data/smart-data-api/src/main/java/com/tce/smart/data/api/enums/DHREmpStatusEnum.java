package com.tce.smart.data.api.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-07-03 13:43
 **/

public enum DHREmpStatusEnum {
	//1-在职/2-试用/3-实习/4-离职

	ON_JOB(1, "在职"),

	TRY_JOB(2, "试用"),

	SHIP_JOB(3, "实习"),

	OUT_JOB(4, "离职");

	private final Integer code;

	private final String desc;

	DHREmpStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DHREmpStatusEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (DHREmpStatusEnum enmuTemp : DHREmpStatusEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		DHREmpStatusEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DHREmpStatusEnum enmuTemp : DHREmpStatusEnum.values()) {
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
