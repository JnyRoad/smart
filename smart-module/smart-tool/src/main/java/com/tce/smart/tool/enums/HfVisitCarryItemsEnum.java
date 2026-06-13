package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * HF访客预约OA携带物品枚举
 * @author fushiping
 * @date
 *
 */

public enum HfVisitCarryItemsEnum {
	ITEM_0(0, "智能手机"),
	ITEM_1(1, "智能手机+相机"),
	ITEM_2(2, "智能手机+相机+U盘"),
	ITEM_3(3, "智能手机+相机+U盘+笔记本电脑"),
	ITEM_4(4, "智能手机+U盘"),
	ITEM_5(5, "相机+U盘+笔记本电脑"),
	ITEM_6(6, "智能手机+笔记本电脑"),
	ITEM_7(7, "其他");

	private final Integer code;

	private final String desc;

	HfVisitCarryItemsEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (HfVisitCarryItemsEnum enmuType : HfVisitCarryItemsEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (HfVisitCarryItemsEnum typeEnmu : HfVisitCarryItemsEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (HfVisitCarryItemsEnum t : HfVisitCarryItemsEnum.values()) {
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
