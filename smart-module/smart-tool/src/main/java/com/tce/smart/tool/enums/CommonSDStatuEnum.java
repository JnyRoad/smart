package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 公共水电记录状态枚举
 * @date: 2020-07-30 10:07
 * @author: wuling
 * @version: 1.0
 */
public enum CommonSDStatuEnum {

	ENABLE(1, "可用"),

	DISABLE(0,"不可用");


	private final Integer code;

	private final String desc;

	CommonSDStatuEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static CommonSDStatuEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (CommonSDStatuEnum enmuTemp : CommonSDStatuEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		CommonSDStatuEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (CommonSDStatuEnum enmuTemp : CommonSDStatuEnum.values()) {
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
