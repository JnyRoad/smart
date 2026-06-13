package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 内宿申请状态枚举
 * @date: 2020-07-21 13:57
 * @author: wuling
 * @version: 1.0
 */
public enum DormitoryApplyStatusEnum {

	NO_APPLYING(0, "未申请"),

	APPLYING(1, "申请中"),

	SUCCESS(2,"申请成功"),

	FAILBACK(3,"退回"),

	CANCEL(4,"撤销");

	private final Integer code;

	private final String desc;

	DormitoryApplyStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DormitoryApplyStatusEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (DormitoryApplyStatusEnum enmuTemp : DormitoryApplyStatusEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		DormitoryApplyStatusEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DormitoryApplyStatusEnum enmuTemp : DormitoryApplyStatusEnum.values()) {
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
