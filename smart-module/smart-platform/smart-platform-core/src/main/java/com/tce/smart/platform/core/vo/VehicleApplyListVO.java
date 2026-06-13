package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;

/**
 * 我的车辆的入园列表
 * @author dell
 *
 */
@Data
public class VehicleApplyListVO {


	/**
	 * 申请ID
	 */
	private Integer id;

	/**
	 * 部门名称
	 */
	private String parkName;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 申请状态
	 */
	private Integer applyStatus;

	/**
	 * 车主姓名
	 */
	private String name;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 申请时间
	 */
	private Date createTime;

	/**
	 * 车牌号
	 */
	private String vehiclePlate;

	private String welfareLevel;

}
