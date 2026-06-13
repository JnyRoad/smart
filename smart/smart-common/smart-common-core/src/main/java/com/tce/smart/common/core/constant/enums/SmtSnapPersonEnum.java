package com.tce.smart.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 抓拍人员的类型
 */
@Getter
@AllArgsConstructor
public enum SmtSnapPersonEnum {

	/**
	 * 员工类型
	 */
	SNAP_PERSON_TYPE1(1, "员工类型"),
	/**
	 * 访客类型
	 */
	SNAP_PERSON_TYPE2(2, "访客类型");


	/**
	 * 类型
	 */
	private Integer type;
	/**
	 * 描述
	 */
	private String description;
}
