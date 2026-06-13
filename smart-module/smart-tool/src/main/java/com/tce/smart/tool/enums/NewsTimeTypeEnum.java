package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import java.util.*;


public enum NewsTimeTypeEnum {
	IMMEDIATELY(1, "实时发布"),
	TIME_SLOT(2, "定时发布");

	private final Integer code;

	private final String desc;

	NewsTimeTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (NewsTimeTypeEnum enmuType : NewsTimeTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (NewsTimeTypeEnum typeEnum : NewsTimeTypeEnum.values()) {
				if (typeEnum.desc.equals(desc)) {
					return typeEnum.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (NewsTimeTypeEnum t : NewsTimeTypeEnum.values()) {
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
