package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 放行事项
 * @author sunfujian
 * @date 2021/8/13 8:45
 */
@Getter
@AllArgsConstructor
public enum ReleaseItemEnum {
	ITEM_0("0", "人员放行"),
	ITEM_1("1", "非保密物品放行"),
	ITEM_3("3", "空车放行"),
	ITEM_4("4", "保密物品放行"),
	ITEM_5("5", "固定资产放行(不包含电脑)"),
	ITEM_6("6", "电脑放行"),
	ITEM_7("7", "人员放行(仅限出差使用)"),
	ITEM_8("8", "自动化物品放行"),
	ITEM_10("10", "废品出售"),
	UNKNOWN("-1", "未知");

	private String code;
	private String desc;

	public static String getByCode(String code) {
		if (code == null) {
			return UNKNOWN.getDesc();
		}
		for (ReleaseItemEnum item : ReleaseItemEnum.values()) {
			if (item.getCode().equals(code)) {
				return item.getDesc();
			}
		}
		return UNKNOWN.getDesc();
	}
}
