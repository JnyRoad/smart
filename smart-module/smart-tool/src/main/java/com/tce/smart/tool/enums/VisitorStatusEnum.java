package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 访客的状态
 * @author ly
 *
 */
public enum VisitorStatusEnum {


	Status_0(0,"已通过"),
	Status_1(1,"已拒绝"),
	Status_2(2,"待审核"),
	Status_3(3,"已到达"),
	CAUSE_4(4,"超时未到"),
	CAUSE_5(5,"已离开"),
	CAUSE_6(6,"预约超时");
    private final Integer code;

    private final String desc;

    VisitorStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VisitorStatusEnum visitorAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VisitorStatusEnum alarmType : VisitorStatusEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VisitorStatusEnum alarmType = visitorAuthority(code);
        return alarmType == null ? null : visitorAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VisitorStatusEnum deviceAuthority : VisitorStatusEnum.values()){
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
            for(VisitorStatusEnum alarmType : VisitorStatusEnum.values()){
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
