package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 审批比较符枚举
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 */
public enum ApprovalComparatoreEnum {

	EQUAL(1, "等于"),

	NOT_EQUAL(2, "不等于");

//	NON_CHANGE(3, "大于等于"),
//
//	NON_CHANGE(4, "小于等于"),
//
//	NON_CHANGE(5, "大于"),
//
//	CHANGED(6,"小于");

	private final Integer code;

	private final String desc;

	ApprovalComparatoreEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ApprovalComparatoreEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (ApprovalComparatoreEnum enmuTemp : ApprovalComparatoreEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		ApprovalComparatoreEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ApprovalComparatoreEnum enmuTemp : ApprovalComparatoreEnum.values()) {
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
