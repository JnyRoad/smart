package com.tce.smart.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据转移表生效字段枚举
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Getter
@AllArgsConstructor
public enum DateMoveEeffectEnum {
	DISABLED(0, "失效"),

	ENABLED(1, "生效");

	/**
	 * 类型
	 */
	private final Integer status;

	/**
	 * 描述
	 */
	private final String description;
}
