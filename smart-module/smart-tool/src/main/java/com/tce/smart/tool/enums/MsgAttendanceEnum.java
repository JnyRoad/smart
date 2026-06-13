package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

public enum MsgAttendanceEnum {
	MSG_1(1,"补卡"),
	MSG_2(2,"考勤");

	private final Integer code;

	private final String desc;

	MsgAttendanceEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static MsgAttendanceEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (MsgAttendanceEnum enmuType : MsgAttendanceEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (MsgAttendanceEnum typeEnmu : MsgAttendanceEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
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
