package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 0 1 导入状态枚举
 * @author ly
 *
 */
public enum ExportStatusEnum {


	YES(1,"导入成功"),
	FAIL(0,"导入失败");

    private final Integer code;

    private final String desc;

    ExportStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ExportStatusEnum overTimeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(ExportStatusEnum alarmType : ExportStatusEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	ExportStatusEnum alarmType = overTimeAuthority(code);
        return alarmType == null ? null : overTimeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(ExportStatusEnum deviceAuthority : ExportStatusEnum.values()){
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
            for(ExportStatusEnum alarmType : ExportStatusEnum.values()){
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
