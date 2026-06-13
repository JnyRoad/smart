package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-14 16:39
 */
@Getter
@AllArgsConstructor
public enum EvwHortationsAllJchenEnum {
	CEO(2, "总裁"),
	VICE_PRESIDENT(3, "副总裁"),
	CHIEF_INSPECTOR(12, "总监"),
	GENERAL_MANAGER(4, "总经理"),
	MANAGER(5, "经理"),
	SECTION_CHIEF(6, "科长"),
	STAFF_MEMBER(7, "职员"),
	GROUP_LEADERS(11, "班组长"),
	MECHANIC(8, "技工"),
	STAFF(9, "员工");

	private Integer code;
	private String desc;

	public static EvwHortationsAllJchenEnum evwHortationsAllJchen(Integer code) {
		if (Objects.nonNull(code)) {
			for (EvwHortationsAllJchenEnum tempEnum : EvwHortationsAllJchenEnum.values()) {
				if (tempEnum.getCode().equals(code)) {
					return tempEnum;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		return evwHortationsAllJchen(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (EvwHortationsAllJchenEnum tempEnum : EvwHortationsAllJchenEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (EvwHortationsAllJchenEnum t : EvwHortationsAllJchenEnum.values()) {
			if (t.code != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}
}
