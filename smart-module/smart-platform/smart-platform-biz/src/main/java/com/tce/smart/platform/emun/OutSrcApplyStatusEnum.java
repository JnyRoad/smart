package com.tce.smart.platform.emun;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/4 10:29
 */
public enum OutSrcApplyStatusEnum {

	/**
	 * 待审批
	 */
	PENDING(0, "待审批"),

	/**
	 * 已通过
	 */
	AGREE(1, "已通过"),

	/**
	 * 已拒绝
	 */
	REFUSE(2, "已拒绝");

	private final Integer code;

	private final String desc;

	OutSrcApplyStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}



	public static OutSrcApplyStatusEnum deviceAuthority(Integer code){
		if(Objects.nonNull(code)){
			for(OutSrcApplyStatusEnum alarmType : OutSrcApplyStatusEnum.values()){
				if(alarmType.code.equals(code)){
					return alarmType;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code){
		OutSrcApplyStatusEnum alarmType = deviceAuthority(code);
		return alarmType == null ? null : deviceAuthority(code).desc;
	}

	public static Integer code(String desc){
		if(StringUtils.isNotEmpty(desc)){
			for(OutSrcApplyStatusEnum deviceAuthority : OutSrcApplyStatusEnum.values()){
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
			for(OutSrcApplyStatusEnum alarmType : OutSrcApplyStatusEnum.values()){
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
