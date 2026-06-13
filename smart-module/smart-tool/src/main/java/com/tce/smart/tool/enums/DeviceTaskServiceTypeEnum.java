package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

/**
 *  设备下发业务类型枚举
 * @author wuling
 *
 */
public enum DeviceTaskServiceTypeEnum {

	CARD_STAFF_IMPORT(1, "员工通关权限"),
	CARD_APP_PERFECT(2, "APP信息完善"),
	CARD_VISITOR(3, "访客预约"),
	CARD_STAFF_SCAN(4, "员工扫码登记"),
	CARD_RECRUIT(5, "招聘入职"),
	CARD_ADMITTANCE(6, "入厂申请"),

	CAR_STAFF(1, "员工车辆"),
	CAR_COMPANY(2, "公司车辆"),
	CAR_NOT_STAFF(3, "非员工车辆"),
	CAR_VISITOR(4, "访客预约"),
	CAR_GUARD(5, "物流车预约"),
	CAR_ADMITTANCE(6, "入厂申请");

    private final Integer code;

    private final String desc;

    DeviceTaskServiceTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static DeviceTaskServiceTypeEnum deviceAuthority(Integer code){
		for(DeviceTaskServiceTypeEnum alarmType : DeviceTaskServiceTypeEnum.values()){
			if(alarmType.code.equals(code)){
				return alarmType;
			}
		}
        return null;
    }

    public static String desc(Integer code){
	DeviceTaskServiceTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? "未知异常" : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceTaskServiceTypeEnum deviceAuthority : DeviceTaskServiceTypeEnum.values()){
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
