package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

/**
 *  设备操作状态
 * @author wuling
 *
 */
public enum DeviceTaskStatusEnum {

	INIT(0, "初始化"),
	SUCCESS(1, "成功"),
	FAIL(2,"失败"),
	DOING(3,"处理中"),
	CANCEL(4,"已取消"),
	EXPIRED(5, "权限已过期"),
	DEVICE_OFFLINE(6, "设备离线");

    private final Integer code;

    private final String desc;

    DeviceTaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static DeviceTaskStatusEnum deviceAuthority(Integer code){
		for(DeviceTaskStatusEnum alarmType : DeviceTaskStatusEnum.values()){
			if(alarmType.code.equals(code)){
				return alarmType;
			}
		}
        return null;
    }

    public static String desc(Integer code){
	DeviceTaskStatusEnum alarmType = deviceAuthority(code);
        return alarmType == null ? "未知异常" : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceTaskStatusEnum deviceAuthority : DeviceTaskStatusEnum.values()){
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
