package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * OA入厂申请车辆类型
 * @author fushiping
 * @date
 * 默认外部车辆
 *  外部车辆=0，厂租车辆=1，员工私家车=2
 */

public enum AdmittanceVehicleTypeEnum {
	OUTSIDE_VEHICLE(0, "外部车辆"),
	FACTORY_VEHICLE(1, "厂租车辆"),
	PRIVATE_VEHICLE(2, "员工私家车");

	private final Integer code;

	private final String desc;

	AdmittanceVehicleTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceVehicleTypeEnum enmuType : AdmittanceVehicleTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceVehicleTypeEnum typeEnmu : AdmittanceVehicleTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceVehicleTypeEnum t : AdmittanceVehicleTypeEnum.values()) {
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
