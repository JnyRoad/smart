package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 审批人设置
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 */
public enum ApprovalPersonRuleEnum {

	NONE(0, "无指定审批人"),

	EXIST(1,"指定审批人"),

	ROOMMATE(2, "室友"),

	LEADER(3, "上级领导");

	private final Integer code;

	private final String desc;

	ApprovalPersonRuleEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ApprovalPersonRuleEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (ApprovalPersonRuleEnum enmuTemp : ApprovalPersonRuleEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		ApprovalPersonRuleEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ApprovalPersonRuleEnum enmuTemp : ApprovalPersonRuleEnum.values()) {
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
