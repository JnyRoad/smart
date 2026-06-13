package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;


public enum NewsPublicTypeEnum {
	VIDEO(0, "视频"),
	IMAGE(1, "图片"),
	PPT(2, "PPT"),
	TEXT(3, "文本"),
	URL(4, "网页");

	private final Integer code;

	private final String desc;

	NewsPublicTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (NewsPublicTypeEnum enmuType : NewsPublicTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (NewsPublicTypeEnum typeEnum : NewsPublicTypeEnum.values()) {
				if (typeEnum.desc.equals(desc)) {
					return typeEnum.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (NewsPublicTypeEnum t : NewsPublicTypeEnum.values()) {
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
