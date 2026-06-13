package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 保密区供应商协议到期是否通知枚举
 * @author wuling
 *
 */
public enum SupplierNotifyEnum {

	NON_NOTIFY(0, "未通知"),
	NOTIFY(1, "已通知");


    private final Integer code;

    private final String desc;

    SupplierNotifyEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SupplierNotifyEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(SupplierNotifyEnum alarmType : SupplierNotifyEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	SupplierNotifyEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(SupplierNotifyEnum deviceAuthority : SupplierNotifyEnum.values()){
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
            for(SupplierNotifyEnum alarmType : SupplierNotifyEnum.values()){
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
