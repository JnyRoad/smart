package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtDormitoryRepairsDTO
 * @date: 2020-07-20 14:19
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtDormitoryRepairsDTO {
	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 员工工号
	 */
	private String staffBadge;

	/**
	 * 员工姓名
	 */
	private String staffName;

	/**
	 * 开始时间
	 */
	private Date beginTime;

	/**
	 * 结束时间
	 */
	private Date endTime;

	/**
	 * 维修类型
	 */
	private Integer repairType;

	private List<Integer> range;

	/**
	 * 状态
	 */
	private Integer status;
}
