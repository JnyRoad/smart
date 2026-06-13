package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 审批连接符枚举
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 */
public enum ApprovalConnectorEnum {

	AND(1, "&&"),

	OR(2, "II"),

	NULL(3,"无");

	private final Integer code;

	private final String desc;

	ApprovalConnectorEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ApprovalConnectorEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (ApprovalConnectorEnum enmuTemp : ApprovalConnectorEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		ApprovalConnectorEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ApprovalConnectorEnum enmuTemp : ApprovalConnectorEnum.values()) {
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
