package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 宿舍维修状态枚举
 * @date: 2020-07-21 13:57
 * @author: wuling
 * @version: 1.0
 */
public enum DormitoryRepairStatusEnum {

	WAIT_APPROVAL(0, "待审批"),

	WAIT_CONFIRM(1, "待确认"),

	WAIT_REPAIR(2,"已安排维修"),

	REPAIR_SUCCESS(3,"维修成功"),

	CLOSED(4,"无法维修"),

	PASS(5,"已通过"),

	REFUSE(6,"已拒绝");

	private final Integer code;

	private final String desc;

	DormitoryRepairStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DormitoryRepairStatusEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (DormitoryRepairStatusEnum enmuTemp : DormitoryRepairStatusEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		DormitoryRepairStatusEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DormitoryRepairStatusEnum enmuTemp : DormitoryRepairStatusEnum.values()) {
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
