package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @description: 房间自动分配标识枚举
 * @date: 2020-07-21 13:57
 * @author: wuling
 * @version: 1.0
 */
public enum IsDormitoryRoomEnum {

	NO(1, "非宿舍"),

	YES(0,"宿舍");

	private final Integer code;

	private final String desc;

	IsDormitoryRoomEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static IsDormitoryRoomEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (IsDormitoryRoomEnum enmuTemp : IsDormitoryRoomEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		IsDormitoryRoomEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (IsDormitoryRoomEnum enmuTemp : IsDormitoryRoomEnum.values()) {
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
