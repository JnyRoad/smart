package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;


public enum NewsTextMoveTypeEnum {
	LEFT_RIGHT(1, "左右方向轮播"),
	UP_DOWN(2, "上下方向轮播");

	private final Integer code;

	private final String desc;

	NewsTextMoveTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (NewsTextMoveTypeEnum enmuType : NewsTextMoveTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (NewsTextMoveTypeEnum typeEnum : NewsTextMoveTypeEnum.values()) {
				if (typeEnum.desc.equals(desc)) {
					return typeEnum.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (NewsTextMoveTypeEnum t : NewsTextMoveTypeEnum.values()) {
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
