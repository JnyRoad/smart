package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 车辆所属类型
 * 辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆 ;4:非员工车辆
 * @author wuling
 *
 */
public enum VehicleBelongTypeEnum {


	PARK_VEHICLE(0, "公司车辆"),

	STAFF_VEHICLE(1, "员工车辆"),

	VISITOR_VEHICLE(2, "访客车辆"),

	LOGISTICS_VEHICLE(3, "物流车辆"),

	NON_STAFF_VEHICLE(4, "非员工车辆"),

	IN_VEHICLE(5, "园区车辆");

    private final Integer code;

    private final String desc;

    VehicleBelongTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VehicleBelongTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VehicleBelongTypeEnum alarmType : VehicleBelongTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VehicleBelongTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VehicleBelongTypeEnum deviceAuthority : VehicleBelongTypeEnum.values()){
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
            for(VehicleBelongTypeEnum alarmType : VehicleBelongTypeEnum.values()){
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
