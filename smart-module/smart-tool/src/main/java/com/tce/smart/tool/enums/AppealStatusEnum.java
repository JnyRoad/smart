package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 员工申诉状态类型
 * @date: 2020-07-21 14:03
 * @author: wuling
 * @version: 1.0
 */
public enum AppealStatusEnum {

	APPEAL(1, "已申诉"),

	REPLY(2,"已回复");

	private final Integer code;

	private final String desc;

	AppealStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static AppealStatusEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (AppealStatusEnum enmuTemp : AppealStatusEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		AppealStatusEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AppealStatusEnum enmuTemp : AppealStatusEnum.values()) {
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
