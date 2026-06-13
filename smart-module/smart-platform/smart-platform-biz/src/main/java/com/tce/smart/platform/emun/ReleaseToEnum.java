package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/12 20:41
 */
@Getter
@AllArgsConstructor
public enum ReleaseToEnum {
	INNER("0", "厂内"),
	OUT("1", "厂外"),
	UNKNOWN("-1", "未知");

	private String code;
	private String desc;

	public static String getByCode(String code) {
		if (code == null) {
			return UNKNOWN.getDesc();
		}
		for (ReleaseToEnum release : ReleaseToEnum.values()) {
			if (release.getCode().equals(code)) {
				return release.getDesc();
			}
		}
		return UNKNOWN.getDesc();
	}
}
