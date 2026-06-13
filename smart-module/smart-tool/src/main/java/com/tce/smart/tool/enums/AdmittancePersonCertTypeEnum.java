package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 *
 * @description: OA证件类型
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 *   默认身份证，身份证=0，护照=1，
 *  驾驶证=2，行驶证=3，港澳台通行证=4，其他=5
 */

public enum AdmittancePersonCertTypeEnum {

	ID_CARD(0,"身份证"),

	PASSPORT(1, "护照"),

	DRIVER_LICENSE(2,"驾驶证"),

	DRIVING_LICENSE(3,  "行驶证"),

	PASS_CHECK(4,  "港澳台通行证"),

	OTHER(5, "其他");

	private final Integer code;

	private final String desc;


	AdmittancePersonCertTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}


	public static AdmittancePersonCertTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittancePersonCertTypeEnum enmuTemp : AdmittancePersonCertTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		AdmittancePersonCertTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittancePersonCertTypeEnum enmuTemp : AdmittancePersonCertTypeEnum.values()) {
				if (enmuTemp.desc.equals(desc)) {
					return enmuTemp.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittancePersonCertTypeEnum t : AdmittancePersonCertTypeEnum.values()) {
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
