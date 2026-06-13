package com.tce.smart.tool.enums;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * @date
 * 1 新工厂
 * 2 老工厂
 */

public enum SecurityOaAreaEnum {
	ITEM_0(0, "jj",  "F1", 1),
	ITEM_1(1, "kk","F2", 1),
	ITEM_2(2, "ll","F3", 1),
	ITEM_3(3, "qq","外围", 1),
	ITEM_4(4, "ww","前台展厅", 1),
	ITEM_5(5, "rr","北门接待室", 1),
	ITEM_6(6, "tt","其他区域", 1),
	ITEM_7(7, "aa", "A栋", 2),
	ITEM_8(8, "bb", "B栋", 2),
	ITEM_9(9, "ff", "C栋", 2),
	ITEM_10(10, "cc", "D栋", 2),
	ITEM_11(11, "dd", "E栋", 2),
	ITEM_12(12, "ee", "宿舍/餐厅", 2),
	ITEM_13(13, "gg", "外围联办", 2),
	ITEM_14(14, "hh", "其它区域", 2),
	ITEM_27(27, "tiantai", "天台", 1),
	ITEM_28(28, "lianban", "联办", 1),
	ITEM_29(29, "twoe", "E2", 2),
	ITEM_30(30, "threee", "E3", 2),
	ITEM_31(31, "foure", "E4", 2),
	ITEM_32(32, "fivee", "E5", 2),
	ITEM_33(33, "sixe", "E6", 2),
	ITEM_34(34, "seven", "E7", 2),
	ITEM_35(35, "eighte", "E8", 2);

	private final Integer code;

	private final String type;

	private final String desc;

	private final Integer factoryType;


	SecurityOaAreaEnum(Integer code, String type, String desc, Integer factoryType) {
		this.code = code;
		this.desc = desc;
		this.type = type;
		this.factoryType = factoryType;
	}

	public String getType() {
		return type;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (SecurityOaAreaEnum enmuType : SecurityOaAreaEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static SecurityOaAreaEnum getEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (SecurityOaAreaEnum enmuTemp : SecurityOaAreaEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String type(Integer code) {
		if (Objects.nonNull(code)) {
			for (SecurityOaAreaEnum enmuType : SecurityOaAreaEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.type;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SecurityOaAreaEnum typeEnmu : SecurityOaAreaEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static Integer getByType(String type) {
		if (StringUtils.isNotEmpty(type)) {
			for (SecurityOaAreaEnum typeEnmu : SecurityOaAreaEnum.values()) {
				if (typeEnmu.type.equals(type)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (SecurityOaAreaEnum t : SecurityOaAreaEnum.values()) {
			if (Objects.nonNull(t.code)) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("type", t.type);
				map.put("desc", t.desc);
				map.put("factoryDesc", t.factoryType);
				list.add(map);
			}
		}
		return list;
	}

	public static List<Map<String, Object>> getType(Integer flag) {
		List<Map<String, Object>> list = new ArrayList<>();
		if(Objects.isNull(flag)) {
			return getTypeList();
		}
		if(OneOrZeroEnum.ZERO.getCode().equals(flag)) {
			for (SecurityOaAreaEnum t : SecurityOaAreaEnum.values()) {
				if (Objects.nonNull(t.factoryType) && t.factoryType == 2 ) {
					Map<String, Object> map = new HashMap<>();
					map.put("code", t.code);
					map.put("type", t.type);
					map.put("desc", t.desc);
					map.put("factoryDesc", t.factoryType);
					list.add(map);
				}
			}
		}else {
			for (SecurityOaAreaEnum t : SecurityOaAreaEnum.values()) {
				if (Objects.nonNull(t.factoryType) && t.factoryType == 1 ) {
					Map<String, Object> map = new HashMap<>();
					map.put("code", t.code);
					map.put("type", t.type);
					map.put("desc", t.desc);
					map.put("factoryDesc", t.factoryType);
					list.add(map);
				}
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
