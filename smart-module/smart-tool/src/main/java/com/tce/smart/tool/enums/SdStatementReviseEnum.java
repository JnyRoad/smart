package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 水电抄表是否手动调整上月止度枚举
 * @date: 2020-09-05 12:11
 * @author: wuling
 * @version: 1.0
 */
public enum SdStatementReviseEnum {
	NON_REVISE(0, "未修正"),
	REVISE(1, "已修正");


	private final Integer code;

	private final String desc;

	SdStatementReviseEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SdStatementReviseEnum deviceAuthority(Integer code){
		if(Objects.nonNull(code)){
			for(SdStatementReviseEnum alarmType : SdStatementReviseEnum.values()){
				if(alarmType.code.equals(code)){
					return alarmType;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code){
		SdStatementReviseEnum alarmType = deviceAuthority(code);
		return alarmType == null ? null : alarmType.desc;
	}

	public static Integer code(String desc){
		if(StringUtils.isNotEmpty(desc)){
			for(SdStatementReviseEnum deviceAuthority : SdStatementReviseEnum.values()){
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
			for(SdStatementReviseEnum alarmType : SdStatementReviseEnum.values()){
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
