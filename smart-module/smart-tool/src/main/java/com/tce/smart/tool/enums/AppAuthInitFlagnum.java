package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * App权限预置标识枚举
 *
 * @author mckaywu
 * @date 2019-06-12 17:53:08
 */
public enum AppAuthInitFlagnum {
	INIT(0, "预置权限"),
	ADD_NEW(1, "新增权限");

	private final Integer code;

	private final String desc;

	AppAuthInitFlagnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static AppAuthInitFlagnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AppAuthInitFlagnum enmuType : AppAuthInitFlagnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AppAuthInitFlagnum typeEnmu : AppAuthInitFlagnum.values()) {
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
