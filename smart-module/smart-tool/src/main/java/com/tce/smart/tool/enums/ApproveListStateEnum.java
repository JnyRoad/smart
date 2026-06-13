package com.tce.smart.tool.enums;


import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 待审批状态
 * @author Lenovo
 *
 */
public enum ApproveListStateEnum {

    PENDING(0, "待审批"),

    AGREE(1, "通过"),

    REFUSE(2, "拒绝"),

	CLOSE(3, "关闭"),

    WAITING(4, "等待");

    private final Integer code;

    private final String desc;

    ApproveListStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }



    public static ApproveListStateEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(ApproveListStateEnum alarmType : ApproveListStateEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	ApproveListStateEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(ApproveListStateEnum deviceAuthority : ApproveListStateEnum.values()){
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
            for(ApproveListStateEnum alarmType : ApproveListStateEnum.values()){
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
