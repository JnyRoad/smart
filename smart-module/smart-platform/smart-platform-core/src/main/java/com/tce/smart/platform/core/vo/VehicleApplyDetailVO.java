package com.tce.smart.platform.core.vo;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * 我的车辆的入园列表
 * @author dell
 *
 */
@Data
public class VehicleApplyDetailVO {

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 申请状态
	 */
	private Integer status;

	/**
	 * 申请时间
	 */
	private LocalDateTime createTime;

	/**
	 * 描述信息
	 */
	private String reason;

	/**
	 * 审批人
	 */
	private String approver;

	/**
	 * 审批时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 申请信息
	 */
	private VehicleVO vehicle;

}
