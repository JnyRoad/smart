package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 车辆出入记录查询
 *
 * @author 梁园
 * @date 2019-04-21 18:19:30
 */
@Data
public class SnapVehicleAccessDTO {
	private static final long serialVersionUID = 1L;
	/**
	 * 车主名称
	 */
	private String driverName;

	/**
	 * buId
	 */
	private String compId;

	/**
	 * 部门id
	 */
	private String depId;
	/**
	 * 车牌号
	 */
	private String vehiclePlate;
	/**
	 * 抓拍查询开始时间
	 */
	private String startTime;

	/**
	 * 抓拍查询结束时间
	 */
	private String endTime;
	/**
	 * 区域ID
	 */
	private Integer areaId;

	private String areaName;
	/**
	 * 手机号
	 */
	private String driverPhone;
	/**
	 * 车辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆 ;4:非员工车辆
	 */
	private Integer vehicleAscription;
	/**
	 * 事件类型：1-进；2-出；
	 */
	private Integer eventType;
	/**
	 * 1:员工；2：访客；3：物流车车主
	 */
	private Integer driverType;

	private Integer parkId;
}
