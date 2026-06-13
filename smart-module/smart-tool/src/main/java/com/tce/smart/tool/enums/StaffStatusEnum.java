package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import org.omg.CORBA.UNKNOWN;

import java.util.Objects;

/**
 * 员工状态枚举 0-已离职 1-在职
 * @author 齐佩
 *
 */
public enum StaffStatusEnum {

	STAFF_STATUS_QUIT(0,"已离职"),
	STAFF_STATUS_IN(1,"在职"),
	STAFF_STATUS_TTRY(2,"试用"),
	STAFF_STATUS_PRACTICE(3,"实习"),
	STAFF_STATUS_TEMPORARY(4,"临时人员"),
	UNKNOWN(-1,"未知");

	StaffStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	private final Integer code;
	private final String desc;

	public Integer getCode() {
		return this.code;
	}
	public String getDesc() {
		return this.desc;
	}

    public static StaffStatusEnum staffAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(StaffStatusEnum alarmType : StaffStatusEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return UNKNOWN;
    }
    public static Integer changeStaffStatus(Integer status){
		if(status.equals(1)){
			return StaffStatusEnum.STAFF_STATUS_IN.getCode();
		}
		if(status.equals(4)){
			return StaffStatusEnum.STAFF_STATUS_QUIT.getCode();
		}
		if(status.equals(2)){
			return StaffStatusEnum.STAFF_STATUS_TTRY.getCode();
		}
		if(status.equals(3)){
			return StaffStatusEnum.STAFF_STATUS_PRACTICE.getCode();
		}
		return UNKNOWN.getCode();
	}
    public static String desc(Integer code){
	StaffStatusEnum alarmType = staffAuthority(code);
        return alarmType == null ? null : staffAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(StaffStatusEnum deviceAuthority : StaffStatusEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return UNKNOWN.getCode();
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(StaffStatusEnum alarmType : StaffStatusEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }
}
