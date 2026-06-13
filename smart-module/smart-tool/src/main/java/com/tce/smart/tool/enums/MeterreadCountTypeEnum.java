package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;


/**
 * 水电结算日类型
 */
public enum MeterreadCountTypeEnum {
	FIXED(1, "固定日期"),
	DYNAMIC(2, "动态日期");

	private final Integer code;

	private final String desc;

	MeterreadCountTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (MeterreadCountTypeEnum enmuType : MeterreadCountTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (MeterreadCountTypeEnum typeEnum : MeterreadCountTypeEnum.values()) {
				if (typeEnum.desc.equals(desc)) {
					return typeEnum.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (MeterreadCountTypeEnum t : MeterreadCountTypeEnum.values()) {
			if (Objects.nonNull(t.code)) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
