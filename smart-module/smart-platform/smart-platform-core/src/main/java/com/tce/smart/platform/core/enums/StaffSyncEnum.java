package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/24 20:34
 */
@Getter
@AllArgsConstructor
public enum StaffSyncEnum {
	YES(1, "是"),
	NO(0, "否");

	private Integer code;
	private String desc;
}
