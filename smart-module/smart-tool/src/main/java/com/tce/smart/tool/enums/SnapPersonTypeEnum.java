package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 设备
 * @author Lenovo
 *
 */
public enum SnapPersonTypeEnum {

	/**
	 * 访客类型
	 */
	VISITOR_TYPE(2, "访客"),
	/**
	 * 员工类型
	 */
	PERSON_TYPE(1, "员工"),
	/**
	 * 物流城车主
	 */
	PERSON_DERVER_TYPE(3, "物流车车主");


    private final Integer code;

    private final String desc;

    SnapPersonTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SnapPersonTypeEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(SnapPersonTypeEnum alarmType : SnapPersonTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	SnapPersonTypeEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(SnapPersonTypeEnum deviceAuthority : SnapPersonTypeEnum.values()){
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
            for(SnapPersonTypeEnum alarmType : SnapPersonTypeEnum.values()){
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
