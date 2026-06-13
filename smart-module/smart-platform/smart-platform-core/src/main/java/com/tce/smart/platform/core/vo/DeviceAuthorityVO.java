package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;

@Data
public class DeviceAuthorityVO {


	private Integer id;
	/**
	 * 权限名称
	 */
	private String authorityName;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 类型 2：人员； 3：车辆
	 */
	private Integer type;
	/**
	 * 权限性质0-公共区域 1-保密区域
	 */
	private Integer areaType;

	/**
	 * 所属园区
	 */
	private Integer parkId;

	private String parkName;
}
