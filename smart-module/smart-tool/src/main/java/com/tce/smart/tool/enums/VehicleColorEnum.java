package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * 车辆颜色
 * @author Lenovo
 *
 */
public enum VehicleColorEnum {


	OTHER(0, "其他"),

	WHITE(1, "白"),

	SILVER(2, "银"),

	ASH(3,"灰"),

	BLACK(4,"黑"),

	RED(5,"红"),

	DARK_BLUE(6,"深蓝"),

	BLUE(7,"蓝"),

	YELLOW(8,"黄"),

	GREEN(9,"绿"),

	BROWN(10,"棕"),

	POWDER(11,"粉"),

	PURPLE(12,"紫"),

	DARK_GREY(13,"深灰"),

	CYAN(14,"青色");

    private final Integer code;

    private final String desc;

    VehicleColorEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VehicleColorEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VehicleColorEnum alarmType : VehicleColorEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VehicleColorEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VehicleColorEnum deviceAuthority : VehicleColorEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(VehicleColorEnum alarmType : VehicleColorEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (VehicleColorEnum t : VehicleColorEnum.values()) {
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
