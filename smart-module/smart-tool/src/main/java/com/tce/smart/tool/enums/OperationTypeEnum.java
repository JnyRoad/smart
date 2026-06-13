package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 操作类型
 * @author ly
 *
 */
public enum OperationTypeEnum {


	OPERATION_TYPE_1(1,"下发"),
	OPERATION_TYPE_2(2,"删除");


    private final Integer code;

    private final String desc;

    OperationTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OperationTypeEnum operationTypeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(OperationTypeEnum alarmType : OperationTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	OperationTypeEnum alarmType = operationTypeAuthority(code);
        return alarmType == null ? null : operationTypeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(OperationTypeEnum deviceAuthority : OperationTypeEnum.values()){
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
            for(OperationTypeEnum alarmType : OperationTypeEnum.values()){
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
