package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出发地点
 * @author sunfujian
 * @date 2021/8/12 20:50
 */
@Getter
@AllArgsConstructor
public enum FromToPlaceEnum {
	PLACE_0("0", "A栋"),
	PLACE_1("1", "B栋"),
	PLACE_2("2", "C栋"),
	PLACE_3("3", "D栋"),
	PLACE_4("4", "E栋"),
	PLACE_5("5", "F栋"),
	PLACE_6("6", "福侨4号仓库"),
	PLACE_7("7", "厂区加工中心"),
	PLACE_8("8", "纸托"),
	PLACE_9("9", "万顺"),
	PLACE_10("10", "其它"),
	PLACE_11("11", "市场开发部/生活区"),
	UNKNOWN("-1", "未知");

	private String code;
	private String desc;

	public static String getByCode(String code) {
		if (code == null) {
			return UNKNOWN.getDesc();
		}
		for (FromToPlaceEnum item : FromToPlaceEnum.values()) {
			if (item.getCode().equals(code)) {
				return item.getDesc();
			}
		}
		return UNKNOWN.getDesc();
	}
}
