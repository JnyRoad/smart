package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 设备下发状态枚举
 *
 * @author
 * @date
 */
public enum DeviceDownStatusEnum {
	WAIT(0, "待下发"),
	SUCCESS(1, "下发成功"),
	FAIL(2, "下发失败"),
	IN_WORK(3, "下发中"),
	ALRAEDY(4, "已下发");

	private final Integer code;

	private final String desc;

	DeviceDownStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (DeviceDownStatusEnum enmuType : DeviceDownStatusEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DeviceDownStatusEnum typeEnmu : DeviceDownStatusEnum.values()) {
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
