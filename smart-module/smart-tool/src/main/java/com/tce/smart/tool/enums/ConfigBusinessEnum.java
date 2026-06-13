package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 公共配置事务类型
 * @author
 *
 */
public enum ConfigBusinessEnum {


	VISITOR(1,"访客预约"),

	ADMITTANCE(2,"入厂申请"),

	DEVICE_ADMIN(3,"设备管理员"),

	LEAVE_SETTLEMENT(4,"离职结算");

    private final Integer code;

    private final String desc;

    ConfigBusinessEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConfigBusinessEnum visitorAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(ConfigBusinessEnum alarmType : ConfigBusinessEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	ConfigBusinessEnum alarmType = visitorAuthority(code);
        return alarmType == null ? null : visitorAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(ConfigBusinessEnum deviceAuthority : ConfigBusinessEnum.values()){
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
            for(ConfigBusinessEnum alarmType : ConfigBusinessEnum.values()){
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
