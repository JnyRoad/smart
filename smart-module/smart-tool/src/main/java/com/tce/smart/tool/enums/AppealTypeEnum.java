package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 申诉类型枚举
 * @date: 2020-07-27 14:34
 * @author: wuling
 * @version: 1.0
 */
public enum AppealTypeEnum {

	PERSONNEL_SERVICE(1, "人事服务"),

	DORMITORY_SERVICE(2,"宿舍服务"),

	WORKSHOP_MANAGE(3,"车间管理");

	private final Integer code;

	private final String desc;

	AppealTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static AppealTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (AppealTypeEnum enmuTemp : AppealTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		AppealTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AppealTypeEnum enmuTemp : AppealTypeEnum.values()) {
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
