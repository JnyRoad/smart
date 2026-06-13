package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 下发数据的类型
 * @author ly
 *
 */
public enum DownRecordTypeEnum {


	TYPE_1(1,"访客"),
	TYPE_2(2,"员工车辆"),
	TYPE_3(3,"物流车辆 "),
	TYPE_4(4,"面试人员"),
	TYPE_5(5,"复试人员"),
	TYPE_6(6,"待入职人员"),
	TYPE_7(7,"拒绝人员"),
	TYPE_8(8,"入库人员");
    private final Integer code;

    private final String desc;

    DownRecordTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DownRecordTypeEnum downRecordTypeAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(DownRecordTypeEnum alarmType : DownRecordTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	DownRecordTypeEnum alarmType = downRecordTypeAuthority(code);
        return alarmType == null ? null : downRecordTypeAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DownRecordTypeEnum deviceAuthority : DownRecordTypeEnum.values()){
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
            for(DownRecordTypeEnum alarmType : DownRecordTypeEnum.values()){
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
