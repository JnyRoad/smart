package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-28 21:43
 */
@Getter
@AllArgsConstructor
public enum ArticlesReleaseTypeEnum {
	SUPPLIER(1, "供应商物品"),

	OUTSIDERS(2, "外来人员物品"),

	DORMITORY(3, "宿舍生活物品"),
	/**
	 * 石岩办公区类型
	 */
	OFFICE_ZONE(4, "办公区物品"),
	/**
	 * 许昌办公区类型
	 */
	XC_OFFICE_ZONE(5, "办公区物品");

	private Integer code;
	private String desc;

	public static ArticlesReleaseTypeEnum getEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (ArticlesReleaseTypeEnum tempEnum : ArticlesReleaseTypeEnum.values()) {
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
			for (ArticlesReleaseTypeEnum tempEnum : ArticlesReleaseTypeEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	/**
	 * type 1:许昌办公
	 * type 2:其他园区
	 * @param type
	 * @return
	 */
	public static List<Map<String, Object>> list(Integer type) {
		List<Map<String, Object>> list = new ArrayList<>();
		if(OneOrZeroEnum.ONE.getCode().equals(type)) {
			for (ArticlesReleaseTypeEnum t : ArticlesReleaseTypeEnum.values()) {
				if (t.code != null && t.code != 4) {
					Map<String, Object> map = new HashMap<>();
					map.put("code", t.code);
					map.put("desc", t.desc);
					list.add(map);
				}
			}
			return list;
		}
		for (ArticlesReleaseTypeEnum t : ArticlesReleaseTypeEnum.values()) {
			if (t.code != null && t.code != 5) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}
}
