package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 离职类型
 * @author Lenovo
 *
 */
public enum LeaveHandoverEnum {

    NORMAL(0, "待确认"),

    ABNORMAL(1, "已确认");

    private final Integer code;

    private final String desc;

    LeaveHandoverEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LeaveHandoverEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(LeaveHandoverEnum alarmType : LeaveHandoverEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	LeaveHandoverEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(LeaveHandoverEnum deviceAuthority : LeaveHandoverEnum.values()){
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
            for(LeaveHandoverEnum alarmType : LeaveHandoverEnum.values()){
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
