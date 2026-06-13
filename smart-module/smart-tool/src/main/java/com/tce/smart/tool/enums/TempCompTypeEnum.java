package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 临时单位类型枚举
 * @date: 2020-07-21 13:57
 * @author: wuling
 * @version: 1.0
 */
public enum TempCompTypeEnum {

	WAI_XIE(1, "外包单位"),

	PAI_QIAN(2,"派遣");

	private final Integer code;

	private final String desc;

	TempCompTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static TempCompTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (TempCompTypeEnum enmuTemp : TempCompTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		TempCompTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (TempCompTypeEnum enmuTemp : TempCompTypeEnum.values()) {
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
