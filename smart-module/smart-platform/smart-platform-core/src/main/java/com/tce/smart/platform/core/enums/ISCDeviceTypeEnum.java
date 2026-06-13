package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/24 16:23
 */
@Getter
@AllArgsConstructor
public enum ISCDeviceTypeEnum {

	TYPE_1("acsDevice", 1, "门禁控制器"),
	TYPE_2("barrierGate", 2, "停车场-道闸"),
	TYPE_3("barrierGate", 3, "停车场-道闸");
//	TYPE_2("reader", 2, "门禁读卡器");

	private String type;
	private Integer code;
	private String desc;

	public static String getByCode(Integer code) {
		if (code == null) return null;
		for (ISCDeviceTypeEnum item : ISCDeviceTypeEnum.values()) {
			if (item.getCode().equals(code)) {
				return item.getType();
			}
		}
		return null;
	}
}
