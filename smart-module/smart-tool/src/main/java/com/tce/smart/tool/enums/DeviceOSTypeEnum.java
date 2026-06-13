package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 设备操作系统枚举
 *
 * @author mkwu
 * @date 2019-07-03
 */
public enum DeviceOSTypeEnum {

	ANDROID(1, "安卓系统"),
	IOS(2, "IOS系统");

	private final Integer code;

	private final String desc;

	DeviceOSTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DeviceOSTypeEnum getDeviceEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (DeviceOSTypeEnum enmuTemp : DeviceOSTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		DeviceOSTypeEnum enmuTemp = getDeviceEnmu(code);
		return enmuTemp == null ? null : getDeviceEnmu(code).desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DeviceOSTypeEnum enmuTemp : DeviceOSTypeEnum.values()) {
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
