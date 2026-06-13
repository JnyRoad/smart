package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 员工关联权限类型枚举
 *
 * @author fushiping
 * @date
 */
public enum RelationAuthTypeEnum {
	BASE_AUTH(1, "基础权限"),
	SECURITY_AUTH(2, "保密区权限");

	private final Integer code;

	private final String desc;

	RelationAuthTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static RelationAuthTypeEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (RelationAuthTypeEnum enmuType : RelationAuthTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (RelationAuthTypeEnum typeEnmu : RelationAuthTypeEnum.values()) {
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
