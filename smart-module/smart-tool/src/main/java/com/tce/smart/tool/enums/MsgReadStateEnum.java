package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * App消息是否已读
 *
 * @author mkwu
 * @date 2019-07-03
 */
public enum MsgReadStateEnum {

	UNREAD(0, "消息未读"),
	READ(1, "消息已读");

	private final Integer code;

	private final String desc;

	MsgReadStateEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static MsgReadStateEnum getDeviceEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (MsgReadStateEnum enmuTemp : MsgReadStateEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		MsgReadStateEnum enmuTemp = getDeviceEnmu(code);
		return enmuTemp == null ? null : getDeviceEnmu(code).desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (MsgReadStateEnum enmuTemp : MsgReadStateEnum.values()) {
				if (enmuTemp.desc.equals(desc)) {
					return enmuTemp.code;
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
