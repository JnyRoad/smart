package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * OA携带物品枚举
 * @author fushiping
 * @date
 *
 *       默认智能手机，智能手机=0，智能手机+相机=8，智能手机+相机+U盘=7，
 * 		智能手机+相机+U盘+笔记本电脑=6，智能手机+U盘=12，
 * 		智能手机+U盘+笔记本电脑=14，智能手机+笔记本电脑=13，相机=1，
 * 		相机+U盘=10，相机+U盘+笔记本电脑=9，相机+笔记本电脑=15，
 * 		U盘=2，U盘+笔记本电脑=11，笔记本电脑=3，无=4,
 */

public enum AdmittanceCarryItemsEnum {
	ITEM_0(0, "智能手机"),
	ITEM_13(13, "智能手机+笔记本电脑"),
	ITEM_4(4, "无");

	private final Integer code;

	private final String desc;

	AdmittanceCarryItemsEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceCarryItemsEnum enmuType : AdmittanceCarryItemsEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceCarryItemsEnum typeEnmu : AdmittanceCarryItemsEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceCarryItemsEnum t : AdmittanceCarryItemsEnum.values()) {
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
