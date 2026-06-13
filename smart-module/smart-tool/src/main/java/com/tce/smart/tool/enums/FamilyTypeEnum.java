package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 家属类型枚举
 * @date: 2020-07-27 14:34
 * @author: fushiping
 * @version: 1.0
 */
public enum FamilyTypeEnum {

	SPOUSE(1, "夫妻"),

	LINEAL_CONSANGUINITY(2,"直系血亲"),

	COLLATERAL_CONSANGUINITY(3, "旁系血亲"),

	CLOSE_IN_LAWS(4,"近姻亲"),

	OTHER(5,"其他");

	private final Integer code;

	private final String desc;

	FamilyTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static FamilyTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (FamilyTypeEnum enmuTemp : FamilyTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		FamilyTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (FamilyTypeEnum enmuTemp : FamilyTypeEnum.values()) {
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
