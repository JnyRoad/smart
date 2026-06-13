package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 水电结算状态枚举
 * @date: 2020-09-05 12:11
 * @author: wuling
 * @version: 1.0
 */
public enum SdStatementStatusEnum {
	NON_STATEMENT(0, "未结算"),
	STATEMENT(1, "已结算");


	private final Integer code;

	private final String desc;

	SdStatementStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SdStatementStatusEnum deviceAuthority(Integer code){
		if(Objects.nonNull(code)){
			for(SdStatementStatusEnum alarmType : SdStatementStatusEnum.values()){
				if(alarmType.code.equals(code)){
					return alarmType;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code){
		SdStatementStatusEnum alarmType = deviceAuthority(code);
		return alarmType == null ? null : alarmType.desc;
	}

	public static Integer code(String desc){
		if(StringUtils.isNotEmpty(desc)){
			for(SdStatementStatusEnum deviceAuthority : SdStatementStatusEnum.values()){
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
			for(SdStatementStatusEnum alarmType : SdStatementStatusEnum.values()){
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
