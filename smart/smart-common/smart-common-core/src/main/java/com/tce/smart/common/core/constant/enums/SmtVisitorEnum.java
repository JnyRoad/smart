package com.tce.smart.common.core.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 访客类型
 */
@Getter
@AllArgsConstructor
public enum SmtVisitorEnum {

	//访客的信息标识

	/**
	 * 访客已经到达的状态
	 */
	COME_STATUS(3, "访客已经到达的状态"),

	/**
	 * 访客通过的状态
	 */
	PASS_STATUS(0, "访客通过的状态"),
	/**
	 * 访客被驳回的状态
	 */
	NOTPASS_STATUS(1, "访客被驳回的状态"),
	/**
	 * 无车辆信息的状态
	 */
	NOT_VEHICLE(0, "访客没有车辆信息"),
	/**
	 * 车辆信息的状态
	 */
	IS_VEHICLE(1, "访客有车辆信息"),
	/**
	 * 发送短信的状态
	 */
	IS_SEND(0, "已经给访客发送短信"),
	/**
	 * 发送短信的状态
	 */
	NOT_IS_SEND(1, "没有给访客发送短信"),


	/**
	 * 卡片类型
	 */
	CARD_TYPE_1(1, "普通卡"),
	CARD_TYPE_2(2, "残疾卡"),
	CARD_TYPE_3(3, "黑名单卡"),
	CARD_TYPE_4(4, "巡逻卡"),
	CARD_TYPE_5(5, "胁迫卡"),
	CARD_TYPE_6(6, "超级卡"),
	CARD_TYPE_7(7, "来宾卡"),

	/**
	 * 车的卡片类型
	 */
	CAR_CARD_TYPE_0(0, "临时卡"),
	CAR_CARD_TYPE_1(1, "普通卡"),


	/**
	 * 访客类型
	 */
	VISITOR_TYPE(2, "访客"),
	/**
	 * 访客车辆类型
	 */
	VEHICLE_TYPE(3, "访客车辆");


	/**
	 * 类型
	 */
	private Integer type;
	/**
	 * 描述
	 */
	private String description;
}
