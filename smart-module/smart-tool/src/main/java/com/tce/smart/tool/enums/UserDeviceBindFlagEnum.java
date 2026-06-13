package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 默认设备枚举 <br>
 * 0-其他设备，1-默认设备
 *
 * @author mkwu
 * @date 2019-07-12
 */
public enum UserDeviceBindFlagEnum {
	UN_BIND(0, "未绑定"),
	BIND(1, "已绑定");

	private final Integer code;

	private final String desc;

	UserDeviceBindFlagEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static UserDeviceBindFlagEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (UserDeviceBindFlagEnum enmuType : UserDeviceBindFlagEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (UserDeviceBindFlagEnum typeEnmu : UserDeviceBindFlagEnum.values()) {
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
