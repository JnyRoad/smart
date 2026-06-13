package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 外部组织来源枚举
 * @date: 2020-07-27 14:34
 * @author: wuling
 * @version: 1.0
 */
public enum OrganizeSourceEnum {

	EHR_SYNC(1, "EHR同步"),

	MANUAL(2,"手动添加");

	private final Integer code;

	private final String desc;

	OrganizeSourceEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static OrganizeSourceEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (OrganizeSourceEnum enmuTemp : OrganizeSourceEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		OrganizeSourceEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (OrganizeSourceEnum enmuTemp : OrganizeSourceEnum.values()) {
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
