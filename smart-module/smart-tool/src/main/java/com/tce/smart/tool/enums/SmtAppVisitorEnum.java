package com.tce.smart.tool.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 访客类型
 */
@Getter
@AllArgsConstructor
public enum SmtAppVisitorEnum {

	//访客的信息标识

	/**
	 * 我发起的预约
	 */
	VISITOR_LIST_TYPE1(1,"我发起的预约"),

	/**
	 * 待我审核
	 */
	VISITOR_LIST_TYPE2(2, "待我审核"),

	/**
	 * 访客未处理
	 */
	UNTREATED_STATUS(2, "访客未处理");


	/**
	 * 类型
	 */
	private Integer type;
	/**
	 * 描述
	 */
	private String description;
}
