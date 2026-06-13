package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 入住记录是否参与统计标识枚举
 *
 * @author wuling
 * @date 2021-06-22 17:53:08
 */
public enum DormitoryStatisFlagnum {
	NO_STATIS(0, "不参与"),
	STATIS(1, "参与");

	private final Integer code;

	private final String desc;

	DormitoryStatisFlagnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DormitoryStatisFlagnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (DormitoryStatisFlagnum enmuType : DormitoryStatisFlagnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DormitoryStatisFlagnum typeEnmu : DormitoryStatisFlagnum.values()) {
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
