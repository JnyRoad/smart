package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 外宿申请状态
 * @author qipei
 *
 */
public enum OutDormitoryStatusEnum {

	NOT_APPROVAL(0,"审批中"), //未审批，未删除
	IS_APPROVAL(1,"已审批"), //已审批，已删除
	IS_REFUSEL(2,"已回退"), //已拒绝，已删除
	IS_REVOKE(3,"已撤销");

	private final Integer code;
    private final String desc;

    OutDormitoryStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OutDormitoryStatusEnum overTimeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(OutDormitoryStatusEnum alarmType : OutDormitoryStatusEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	OutDormitoryStatusEnum alarmType = overTimeAuthority(code);
        return alarmType == null ? null : overTimeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(OutDormitoryStatusEnum deviceAuthority : OutDormitoryStatusEnum.values()){
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
            for(OutDormitoryStatusEnum alarmType : OutDormitoryStatusEnum.values()){
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
