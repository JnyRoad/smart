package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 访客流程审批表
 * @author QIPEI
 *
 */
public enum VisitorProcessEnum {


	PASS_0(0,"已通过"),
	REFUSE_1(1,"已拒绝"),
	WATING_2(2,"待审批"),
	WATING_3(3,"超时未审批");

    private final Integer code;

    private final String desc;

    VisitorProcessEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static VisitorProcessEnum visitorAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VisitorProcessEnum alarmType : VisitorProcessEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VisitorProcessEnum alarmType = visitorAuthority(code);
        return alarmType == null ? null : visitorAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VisitorProcessEnum deviceAuthority : VisitorProcessEnum.values()){
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
            for(VisitorProcessEnum alarmType : VisitorProcessEnum.values()){
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
