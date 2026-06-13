package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/25 20:03
 */
@Getter
@AllArgsConstructor
public enum ISCActionTypeEnum {

	DOWN(0, 1, "下发"),
	UPDATE(1, 3, "修改"),
	DEL(2, 2, "删除"),
	DELAY_DOWN(0, 11, "延迟下发"),
	DELAY_UPDATE(1, 13, "延迟修改"),
	DELAY_DEL(2, 12, "延迟删除");

	/**
	 * ISC平台操作类型，0新增；1修改；2删除
	 */
	private Integer type;
	/**
	 * 任务类型
	 */
	private Integer action;

	private String desc;

	public static Integer getType(Integer action) {
		for (ISCActionTypeEnum item : ISCActionTypeEnum.values()) {
			if (item.getAction().equals(action)) {
				return item.getType();
			}
		}
		return null;
	}
}
