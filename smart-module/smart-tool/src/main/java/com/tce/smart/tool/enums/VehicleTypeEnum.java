package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * 车辆类型
 * @author Lenovo
 *
 */
public enum VehicleTypeEnum {


	OTHER(0, "其他车"),

	LARGE_VEHICLE(1, "大型车"),

	SMALL_VEHICLE(2, "小型车");

    private final Integer code;

    private final String desc;

    VehicleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VehicleTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VehicleTypeEnum alarmType : VehicleTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VehicleTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VehicleTypeEnum deviceAuthority : VehicleTypeEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (VehicleTypeEnum t : VehicleTypeEnum.values()) {
			if (Objects.nonNull(t.code)) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(VehicleTypeEnum alarmType : VehicleTypeEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
