package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 放行人级别
 * @author sunfujian
 * @date 2021/8/12 20:56
 */
@Getter
@AllArgsConstructor
public enum ApplyPersonLevelEnum {
	LEVEL_0("0", "周边职员级以下"),
	LEVEL_1("1", "课长级"),
	LEVEL_2("2", "经理级"),
	LEVEL_3("3", "生产楼层长(含)以上"),
	LEVEL_4("4", "生产楼层长以下"),
	UNKNOWN("-1", "未知");

	private String code;
	private String desc;

	public static String getByCode(String code) {
		if (code == null) {
			return UNKNOWN.getDesc();
		}
		for (ApplyPersonLevelEnum item : ApplyPersonLevelEnum.values()) {
			if (item.getCode().equals(code)) {
				return item.getDesc();
			}
		}
		return UNKNOWN.getDesc();
	}
}
