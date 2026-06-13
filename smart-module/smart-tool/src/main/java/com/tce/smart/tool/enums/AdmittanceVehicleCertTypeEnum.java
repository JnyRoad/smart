package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 *
 * @description: OA证件类型
 * @date: 2020-07-21 14:03
 * @author: fushiping
 * @version: 1.0
 * 默认身份证复印件
 * 驾驶证复印件-0，行驶证复印件=1，身份证复印件=2，港澳台通行证复印件=3
 */

public enum AdmittanceVehicleCertTypeEnum {

	DRIVER_LICENSE(0,"驾驶证复印件"),

	DRIVING_LICENSE(1, "行驶证复印件"),

	ID_CARD(2,"身份证复印件"),

	PASS_CHECK(3,  "港澳台通行证复印件");

	private final Integer code;

	private final String desc;


	AdmittanceVehicleCertTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}


	public static AdmittanceVehicleCertTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceVehicleCertTypeEnum enmuTemp : AdmittanceVehicleCertTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		AdmittanceVehicleCertTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceVehicleCertTypeEnum enmuTemp : AdmittanceVehicleCertTypeEnum.values()) {
				if (enmuTemp.desc.equals(desc)) {
					return enmuTemp.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceVehicleCertTypeEnum t : AdmittanceVehicleCertTypeEnum.values()) {
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
