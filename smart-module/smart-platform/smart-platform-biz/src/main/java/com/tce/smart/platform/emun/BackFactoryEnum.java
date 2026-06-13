package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工返厂
 * @author sunfujian
 * @date 2021/8/11 11:02
 */
@Getter
@AllArgsConstructor
public enum BackFactoryEnum {
	YES("0", "是"),
	NO("1", "否"),
	UNKNOWN("-1", "未知");

	private String code;
	private String desc;

	public static String getByCode(String code) {
		if (code == null) {
			return UNKNOWN.getDesc();
		}
		for (BackFactoryEnum item : BackFactoryEnum.values()) {
			if (item.getCode().equals(code)) {
				return item.getDesc();
			}
		}
		return UNKNOWN.getDesc();
	}
}
