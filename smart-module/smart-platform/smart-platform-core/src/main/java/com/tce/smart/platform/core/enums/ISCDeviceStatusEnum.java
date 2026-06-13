package com.tce.smart.platform.core.enums;

import com.tce.smart.tool.constant.DeviceConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/27 11:21
 */
@Getter
@AllArgsConstructor
public enum ISCDeviceStatusEnum {
	ONLINE(1, DeviceConstants.ON_LINE, "在线"),
	OFFLINE(0, DeviceConstants.OFF_LINE, "离线"),
	UNKNOWN(-1, DeviceConstants.UNCONNECTED, "未连接");

	/**
	 * ISC平台设备在线状态
	 */
	private Integer code;
	/**
	 * 智慧园区设备在线状态
	 */
	private Integer status;
	private String desc;

	public static Integer getStatusByCode(Integer code) {
		if (code == null) return UNKNOWN.getStatus();
		for (ISCDeviceStatusEnum statusEnum : ISCDeviceStatusEnum.values()) {
			if (statusEnum.getCode().equals(code)) {
				return statusEnum.getStatus();
			}
		}
		return UNKNOWN.getStatus();
	}
}
