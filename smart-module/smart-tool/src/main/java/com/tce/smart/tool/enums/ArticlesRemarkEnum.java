package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Descripition: 物品放行备注
 * @Auther: guohongtai
 * @Date: 2020-11-19 20:13
 */
@Getter
@AllArgsConstructor
public enum ArticlesRemarkEnum {
	REMARK1(1, "实际物品与图片不符"),

	REMARK2(2, "物品数量不符"),

	REMARK3(3, "物品外观不符");

	private Integer code;
	private String desc;

	public static ArticlesRemarkEnum getEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (ArticlesRemarkEnum tempEnum : ArticlesRemarkEnum.values()) {
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
			for (ArticlesRemarkEnum tempEnum : ArticlesRemarkEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ArticlesRemarkEnum t : ArticlesRemarkEnum.values()) {
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
