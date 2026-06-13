package com.tce.smart.data.api.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-07-03 13:43
 **/

public enum DHREmpTypeEnum {
	//01-正式工，06-劳务用工，03-劳务派遣工，07-退休返聘，05-实习生，02-裕备生，04-自招挂派遣

	FORMAL_EMP(1, "正式工"),

	YB_EMP(2, "裕备生"),

	NWPQ_EMP(3, "劳务派遣工"),

	ZZ_EMP(4, "自招挂派遣"),

	SX_EMP(5, "实习生"),

	NW_EMP(6,"劳务用工"),

	FP_EMP(7,"退休返聘");

	private final Integer code;

	private final String desc;

	DHREmpTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DHREmpTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (DHREmpTypeEnum enmuTemp : DHREmpTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		DHREmpTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DHREmpTypeEnum enmuTemp : DHREmpTypeEnum.values()) {
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
