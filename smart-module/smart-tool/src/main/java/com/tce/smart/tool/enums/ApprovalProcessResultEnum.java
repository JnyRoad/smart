package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 审批流程结果
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 */
public enum ApprovalProcessResultEnum {

	ALL_PASS(1, "本审批全部通过"),

	PART_PASS(2, "本审批部分节点通过"),

	NODE_PART_PASS(3, "本节点部分审批人通过"),

	ALL_REFUSE(4,"本审批全部拒绝"),

	PART_REFUSE(5, "本节点部分审批人拒绝");

	private final Integer code;

	private final String desc;

	ApprovalProcessResultEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ApprovalProcessResultEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (ApprovalProcessResultEnum enmuTemp : ApprovalProcessResultEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		ApprovalProcessResultEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ApprovalProcessResultEnum enmuTemp : ApprovalProcessResultEnum.values()) {
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
