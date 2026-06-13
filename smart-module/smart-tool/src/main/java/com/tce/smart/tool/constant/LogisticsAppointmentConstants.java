package com.tce.smart.tool.constant;

/**
 * 物流车预约状态
 * @author Lenovo
 *
 */
public interface LogisticsAppointmentConstants {

	/**
	 * 已预约
	 */
	Integer BOOKED = 1;

	/**
	 * 已到达
	 */
	Integer ARRIVED = 2;

	/**
	 * 已离开
	 */
	Integer ALREADY_LEFT = 3;

	/**
	 * 已超时
	 */
	Integer TIMEOUT = 4;

	/**
	 * 已取消
	 */
	Integer CANCEL = 5;





}
