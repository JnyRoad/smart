package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/***
 * description: 保密区供应商状态枚举
 * date: 2020/07/21 9:39
 * author: wuling
 * version: 1.0
 */
public enum SecurityAreaSupplierEnum {

	ENABLE(1, "启用"),

	DISABLE(2,"停用");

	private final Integer code;

	private final String desc;

	SecurityAreaSupplierEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SecurityAreaSupplierEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (SecurityAreaSupplierEnum enmuTemp : SecurityAreaSupplierEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		SecurityAreaSupplierEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : getEnmu(code).desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SecurityAreaSupplierEnum enmuTemp : SecurityAreaSupplierEnum.values()) {
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
