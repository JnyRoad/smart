package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/24 19:03
 */
@Getter
@AllArgsConstructor
public enum DeviceSyncEnum {
	YES(1, "是"),
	NO(0, "否");

	private Integer code;
	private String desc;
}
