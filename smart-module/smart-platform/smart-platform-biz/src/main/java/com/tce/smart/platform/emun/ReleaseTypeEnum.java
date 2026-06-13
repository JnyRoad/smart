package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/13 8:49
 */
@Getter
@AllArgsConstructor
public enum ReleaseTypeEnum {
	TYPE_0("0", "领/退/转料"),
	TYPE_2("2", "异动/转卖"),
	TYPE_3("3", "其它"),
	UNKNOWN("-1", "未知");

	private String code;
	private String desc;

	public static String getByCode(String code) {
		if (code == null) {
			return UNKNOWN.getDesc();
		}
		for (ReleaseTypeEnum item : ReleaseTypeEnum.values()) {
			if (item.getCode().equals(code)) {
				return item.getDesc();
			}
		}
		return UNKNOWN.getDesc();
	}
}
