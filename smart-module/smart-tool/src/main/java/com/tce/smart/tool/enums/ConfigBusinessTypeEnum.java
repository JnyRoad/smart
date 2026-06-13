package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 公共配置事务类型
 * @author
 *
 */
public enum ConfigBusinessTypeEnum {



	VISITOR(1,"访客邀约"),

	NOTICE(2,"访客提示"),

	DEVICE_ADMIN(3,"设备管理员"),

	HEALTH_CODE(4, "是否开启健康码"),

	LEAVE_SETTLEMENT(5, "是否计算最后一天水电费"),

	DELETE_DAYS(6, "日志保留天数"),

	ADMITTANCE_AREA_DISPLAY(7, "入厂申请区域展示配置");


	private final Integer code;
    private final String desc;

    ConfigBusinessTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConfigBusinessTypeEnum visitorAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(ConfigBusinessTypeEnum alarmType : ConfigBusinessTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	ConfigBusinessTypeEnum alarmType = visitorAuthority(code);
        return alarmType == null ? null : visitorAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(ConfigBusinessTypeEnum deviceAuthority : ConfigBusinessTypeEnum.values()){
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
            for(ConfigBusinessTypeEnum alarmType : ConfigBusinessTypeEnum.values()){
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
