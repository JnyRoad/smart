package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.tool.constant.SymbolConstants;

import java.util.*;

/**
 * OA工厂区域
 * @author fushiping
 * 1 新厂区：1.1F   2.2F   3.3F 4.外围  5.前台展厅  6.北门接待室 7.其他区域
 * 2 老厂区：1.A栋  2.B栋  3.C栋   4.D栋    5.E栋  6.宿舍/餐厅  7.外围联办  8.其它区域
 * @date
 */

public enum AdmittanceOaAreaEnum {
	ITEM_0(0, "i", "1F",1),
	ITEM_1(1, "gg","2F",1),
	ITEM_2(2, "k","3F",1),
	ITEM_3(3, "l","外围",1),
	ITEM_6(6, "m","其他区域",1),
	ITEM_7(7, "a", "A栋", 2),
	ITEM_8(8, "b", "B栋", 2),
	ITEM_9(9, "c", "C栋", 2),
	ITEM_10(10, "d", "D栋", 2),
	ITEM_11(11, "e", "E栋", 2),
	ITEM_12(12, "f", "宿舍/餐厅", 2),
	ITEM_13(13, "g", "外围联办", 2),
	ITEM_14(14, "h", "其它区域", 2);

	private final Integer code;

	private final String type;

	private final String desc;

	private final Integer factoryType;


	AdmittanceOaAreaEnum(Integer code, String type, String desc, Integer factoryType) {
		this.code = code;
		this.desc = desc;
		this.type = type;
		this.factoryType = factoryType;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceOaAreaEnum enmuType : AdmittanceOaAreaEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static AdmittanceOaAreaEnum getEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceOaAreaEnum enmuTemp : AdmittanceOaAreaEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String type(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceOaAreaEnum enmuType : AdmittanceOaAreaEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.type;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceOaAreaEnum typeEnmu : AdmittanceOaAreaEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static Integer getByType(String type) {
		if (StringUtils.isNotEmpty(type)) {
			for (AdmittanceOaAreaEnum typeEnmu : AdmittanceOaAreaEnum.values()) {
				if (typeEnmu.type.equals(type)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceOaAreaEnum t : AdmittanceOaAreaEnum.values()) {
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
			for (AdmittanceOaAreaEnum t : AdmittanceOaAreaEnum.values()) {
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
			for (AdmittanceOaAreaEnum t : AdmittanceOaAreaEnum.values()) {
				if (Objects.nonNull(t.factoryType) && t.factoryType == 1) {
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

	public Integer getFactoryType() {
		return factoryType;
	}
}
