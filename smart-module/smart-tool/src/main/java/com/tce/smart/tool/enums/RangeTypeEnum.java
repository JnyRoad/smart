package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Title: RangeTypeEnum
 * @Descripition: 报修范围
 * @Auther: guohongtai
 * @Date: 2020-11-21 12:31
 */
@Getter
@AllArgsConstructor
public enum RangeTypeEnum {
	DORMITORY(1, "宿舍"),

	OFFICE(2, "办公室"),

	WORKSHOP(3,"车间"),

	AROUND(4,"园区周边");

	private Integer code;

	private String desc;

	public static RangeTypeEnum getEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (RangeTypeEnum tempEnum : RangeTypeEnum.values()) {
				if (tempEnum.getCode().equals(code)) {
					return tempEnum;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		return getEnum(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (RangeTypeEnum tempEnum : RangeTypeEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (RangeTypeEnum t : RangeTypeEnum.values()) {
			if (t.code != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

	public static List<Integer> codelist() {
		List<Integer> list = new ArrayList<>();
		for (RangeTypeEnum t : RangeTypeEnum.values()) {
			if (t.code != null) {
				list.add( t.code);
			}
		}
		return list;
	}
}
