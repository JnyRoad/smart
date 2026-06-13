package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 住宿类型枚举
 * @author QIPEI
 *
 */
public enum DormitoryHisotryTypeEnum {

	IN_DORMITORY(0,"入住"),

	CHANGE_DORMITORY(1,"换宿"),

	QUTI_DORMITORY(2,"外宿"),

	OUT_DORMITORY(3,"离职"),

	CHECK_OUT_DORMITORY(4,"退房"),

	OUT_SELF(5,"自离");

    private final Integer code;

    private final String desc;

    DormitoryHisotryTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DormitoryHisotryTypeEnum eventTypeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(DormitoryHisotryTypeEnum alarmType : DormitoryHisotryTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	DormitoryHisotryTypeEnum alarmType = eventTypeAuthority(code);
        return alarmType == null ? null : eventTypeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DormitoryHisotryTypeEnum deviceAuthority : DormitoryHisotryTypeEnum.values()){
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
            for(DormitoryHisotryTypeEnum alarmType : DormitoryHisotryTypeEnum.values()){
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
