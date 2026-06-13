package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-14 16:40
 */
@Getter
@AllArgsConstructor
public enum EvwHortationsAllKindEnum {
	GREATER_THAN(1, "大过"),
	MINOR_MISTAKES(2, "小过"),
	WARNING(3, "警告"),
	COMMENDATION(4, "嘉奖"),
	MOURNING_DRESS(5, "小功"),
	GREAT_ACHIEVEMENTS(6, "大功"),
	EXPEL(7, "开除"),
	REPORT_CRITICISM(5, "通报批评"),
	ORAL_WARNING(6, "口头警告"),
	OUTSTANDING_EMPLOYEE_OF_THE_MONTH(10, "月度优秀员工"),
	OUTSTANDING_EMPLOYEE_OF_THE_YEAR(11, "年度优秀员工"),
	WRITTEN_PRAISE(12, "书面表扬");

	private Integer code;
	private String desc;

	public static EvwHortationsAllKindEnum evwHortationsAllKind(Integer code) {
		if (Objects.nonNull(code)) {
			for (EvwHortationsAllKindEnum tempEnum : EvwHortationsAllKindEnum.values()) {
				if (tempEnum.getCode().equals(code)) {
					return tempEnum;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		return evwHortationsAllKind(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (EvwHortationsAllKindEnum tempEnum : EvwHortationsAllKindEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (EvwHortationsAllKindEnum t : EvwHortationsAllKindEnum.values()) {
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
