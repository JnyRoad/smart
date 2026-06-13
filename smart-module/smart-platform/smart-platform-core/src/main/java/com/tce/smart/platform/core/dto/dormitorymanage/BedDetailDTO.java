package com.tce.smart.platform.core.dto.dormitorymanage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class BedDetailDTO {

	/**
	 * 床位ID
	 */
	private Integer bedId;

	/**
	 * 员工入住表ID
	 */
	private Integer dorStaffId;

	/**
	 * 床位编号
	 */
	private Integer bedNumber;

	/**
	 * 员工工号
	 */
	private String staffBadge;

	/**
	 * 员工姓名
	 */
	private String staffName;

	/**
	 * 员工性别
	 */
	private Integer staffSex;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 职层名称
	 */
	private String jobName;

	/**
	 * 员工当前状态
	 */
	private Integer staffStatus;

	/**
	 * 入住时间
	 */
	private Date inTime;
}
