package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @Descripition:
 * @Auther: guohongtai
 * @Date: 2020-11-14 14:56
 */

public enum FormStateEnum {
	DRAFT(0, "草稿"),
	REVOKE(1, "撤销"),
	APPROVAL(2, "审批中"),
	RETURN(3, "退回"),
	PASS(4, "归档通过"),
	APPLY(5, "申请"),
	FAILED(6, "归档未通过"),
	UNKNOWN(7, "未知");

	private final Integer code;

	private final String desc;

	FormStateEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static FormStateEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (FormStateEnum enmuType : FormStateEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (FormStateEnum typeEnmu : FormStateEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
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
