package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 审批通过规则枚举
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 */
public enum ApprovalPersonPassEnum {

	ALL(1, "审批人全部通过，进入下个流程"),

	ONLY_ONE(2,"审批人任其一通过，进入下个流程");

	private final Integer code;

	private final String desc;

	ApprovalPersonPassEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ApprovalPersonPassEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (ApprovalPersonPassEnum enmuTemp : ApprovalPersonPassEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		ApprovalPersonPassEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ApprovalPersonPassEnum enmuTemp : ApprovalPersonPassEnum.values()) {
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
