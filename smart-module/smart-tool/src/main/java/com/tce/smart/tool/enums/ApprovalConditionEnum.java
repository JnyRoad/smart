package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.tool.constant.ApproveListTypeConstants;

import java.util.Objects;

/**
 * @description: 审批触发条件枚举
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 */
public enum ApprovalConditionEnum {

	REPAIR_AREA(1,  "维修区域"),

	REPAIR_TYPE(2, "维修类型"),

	ITEM_TYPE(3,  "物品类型"),

	DORMITORY_NAME(4,  "楼栋名称"),

	REPAIR_DORMITORY_NAME(5,"楼栋名称"),

	QUIT_DORMITORY_REASON(6, "退宿原因"),

	QUIT_DORMITORY_NAME(7, "楼栋名称");

	private final Integer code;

	private final String desc;


	ApprovalConditionEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ApprovalConditionEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (ApprovalConditionEnum enmuTemp : ApprovalConditionEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static ApprovalConditionEnum getEnmu(Integer code, Integer eventCode) {
		if (Objects.nonNull(code)) {
			for (ApprovalConditionEnum enmuTemp : ApprovalConditionEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		ApprovalConditionEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ApprovalConditionEnum enmuTemp : ApprovalConditionEnum.values()) {
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
