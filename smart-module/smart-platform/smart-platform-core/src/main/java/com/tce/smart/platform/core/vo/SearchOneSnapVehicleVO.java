package com.tce.smart.platform.core.vo;
import java.util.Date;

import lombok.Data;

/**
 * 查询出入车辆抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchOneSnapVehicleVO{

	/**
	 * 主键
	 */
	private Integer id;
	/**
	 * 车牌号
	 */
	private String vehiclePlate;
	/**
	 * 车辆品牌
	 */
	private String vehicleBrand;
	/**
	 * 车辆颜色
	 */
	private Integer vehicleColor;
	/**
	 * 车辆颜色
	 */
	private String vehicleColorDesc;

	/**
	 *
	 */
	private String areaName;
	/**
	 *
	 */
	private Integer driverType;
	private Integer eventType;
	private String eventTypeDesc;
	/**
	 * 通过时间
	 */
	private Date snapTime;
	/**
	 * 车辆图片ID
	 */
	private String snapPhotoId;
	private String snapPhoto;

	/**
	 *
	 */
	private String driverName;
	/**
	 *
	 */
	private String driverPhone;

	private String staffBadge;
	/**
	 * compName BU名称
	 */
	private String compName;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * 职层名称
	 */
	private String jcheName;

	/**
	 *
	 */
	private String company;

	private Integer parkId;

	private String parkName;

}
