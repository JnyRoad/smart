package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * OA入厂申请事由
 * @author fushiping
 * @date
 */

public enum AdmittanceCarCauseEnum {
	CAUSE_13(13, "新厂装货"),
	CAUSE_14(14, "新厂卸货"),
	CAUSE_15(15, "老厂装货"),
	CAUSE_16(16, "老厂卸货");

	private final Integer code;

	private final String desc;

	AdmittanceCarCauseEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceCarCauseEnum enmuType : AdmittanceCarCauseEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceCarCauseEnum typeEnmu : AdmittanceCarCauseEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceCarCauseEnum t : AdmittanceCarCauseEnum.values()) {
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
