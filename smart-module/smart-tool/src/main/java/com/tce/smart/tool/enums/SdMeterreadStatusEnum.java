package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: SdMeterreadStatusEnum
 * @date: 2020-09-05 12:11
 * @author: wuling
 * @version: 1.0
 */
public enum SdMeterreadStatusEnum {
	NON_METER_READ(0, "未抄表"),
	HALF_METER_READ(1, "抄表一部分"),
	ALL_METER_READ(2, "抄表完成");


	private final Integer code;

	private final String desc;

	SdMeterreadStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SdMeterreadStatusEnum deviceAuthority(Integer code){
		if(Objects.nonNull(code)){
			for(SdMeterreadStatusEnum alarmType : SdMeterreadStatusEnum.values()){
				if(alarmType.code.equals(code)){
					return alarmType;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code){
		SdMeterreadStatusEnum alarmType = deviceAuthority(code);
		return alarmType == null ? null : alarmType.desc;
	}

	public static Integer code(String desc){
		if(StringUtils.isNotEmpty(desc)){
			for(SdMeterreadStatusEnum deviceAuthority : SdMeterreadStatusEnum.values()){
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
			for(SdMeterreadStatusEnum alarmType : SdMeterreadStatusEnum.values()){
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
