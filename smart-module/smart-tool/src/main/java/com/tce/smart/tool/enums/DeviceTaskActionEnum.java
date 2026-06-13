package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

/**
 *  设备操作任务
 * @author wuling
 *
 */
public enum DeviceTaskActionEnum {

	DOWN(1, "下发"),
	DEL(2, "删除"),
	UPDATE(3, "修改"),
	DELAY_DOWN(11, "延迟下发"),
	DELAY_DEL(12, "延迟删除"),
	DELAY_UPDATE(13, "延迟修改");

    private final Integer code;

    private final String desc;

    DeviceTaskActionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static DeviceTaskActionEnum deviceAuthority(Integer code){
		for(DeviceTaskActionEnum alarmType : DeviceTaskActionEnum.values()){
			if(alarmType.code.equals(code)){
				return alarmType;
			}
		}
        return null;
    }

    public static String desc(Integer code){
	DeviceTaskActionEnum alarmType = deviceAuthority(code);
        return alarmType == null ? "未知异常" : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceTaskActionEnum deviceAuthority : DeviceTaskActionEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
