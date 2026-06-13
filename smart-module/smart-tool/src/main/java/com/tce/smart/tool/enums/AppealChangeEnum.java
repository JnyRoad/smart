package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 员工申诉转交状态枚举
 * @date: 2020-07-21 14:03
 * @author: wuling
 * @version: 1.0
 */
public enum AppealChangeEnum {

	NON_CHANGE(0, "未转交"),

	CHANGED(1,"已转交");

	private final Integer code;

	private final String desc;

	AppealChangeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static AppealChangeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (AppealChangeEnum enmuTemp : AppealChangeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		AppealChangeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AppealChangeEnum enmuTemp : AppealChangeEnum.values()) {
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
