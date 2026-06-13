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
public enum EvwHortationsAllTypeEnum {
	REWARD(1, "奖励"),
	PUNISHMENT(2, "惩罚"),
	HONOR_REGISTRATION(3, "荣誉登记");

	private Integer code;
	private String desc;

	public static EvwHortationsAllTypeEnum evwHortationsAllType(Integer code) {
		if (Objects.nonNull(code)) {
			for (EvwHortationsAllTypeEnum tempEnum : EvwHortationsAllTypeEnum.values()) {
				if (tempEnum.getCode().equals(code)) {
					return tempEnum;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		return evwHortationsAllType(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (EvwHortationsAllTypeEnum tempEnum : EvwHortationsAllTypeEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (EvwHortationsAllTypeEnum t : EvwHortationsAllTypeEnum.values()) {
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
