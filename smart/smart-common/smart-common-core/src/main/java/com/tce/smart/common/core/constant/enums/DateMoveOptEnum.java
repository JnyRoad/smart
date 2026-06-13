package com.tce.smart.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据转移表操作类型字段枚举
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Getter
@AllArgsConstructor
public enum DateMoveOptEnum {
	MOVE(1, "转移"),

	DELETE(2, "删除");

	/**
	 * 类型
	 */
	private final Integer code;

	/**
	 * 描述
	 */
	private final String description;
}
