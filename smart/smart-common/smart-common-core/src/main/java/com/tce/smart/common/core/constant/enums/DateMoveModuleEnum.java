package com.tce.smart.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据转移表模块字段枚举
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Getter
@AllArgsConstructor
public enum DateMoveModuleEnum {
	SMART(1, "smart模块平台"),

	APP(2, "app模块平台"),

	PLATFORM(3, "platform模块平台");

	/**
	 * 类型
	 */
	private final Integer code;
	/**
	 * 描述
	 */
	private final String description;
}
