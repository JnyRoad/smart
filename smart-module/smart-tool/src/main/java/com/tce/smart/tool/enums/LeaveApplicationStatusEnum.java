package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 离职类型
 * @author Lenovo
 *
 */
public enum LeaveApplicationStatusEnum {

    PENDING(0, "待审批"),

    APPROVED(1, "已同意"),

    REJECTED(2, "已退回"),

	START(3, "开始交接"),

	END(4, "交接完成"),

	COMMIT(5, "交接已提交");
    private final Integer code;

    private final String desc;

    LeaveApplicationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LeaveApplicationStatusEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(LeaveApplicationStatusEnum alarmType : LeaveApplicationStatusEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	LeaveApplicationStatusEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(LeaveApplicationStatusEnum deviceAuthority : LeaveApplicationStatusEnum.values()){
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
            for(LeaveApplicationStatusEnum alarmType : LeaveApplicationStatusEnum.values()){
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
