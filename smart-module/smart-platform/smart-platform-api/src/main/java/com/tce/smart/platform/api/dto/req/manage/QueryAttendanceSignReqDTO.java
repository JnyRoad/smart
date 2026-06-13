package com.tce.smart.platform.api.dto.req.manage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 考勤汇总确认签单
 *
 * @author fushiping
 * @date 2019-04-13 18:19:30
 */
@Data
public class QueryAttendanceSignReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * buID
	 */
	@ApiModelProperty("buID")
	private String compId;

	/**
	 * 部门ID
	 */
	@ApiModelProperty("部门ID")
	private String depId;

	/**
	 * 员工号
	 */
	@ApiModelProperty("员工号")
	private String badge;

	/**
	 * 名称
	 */
	@ApiModelProperty("名称")
	private String name;

	/**
	 * 考勤月份
	 */
	@ApiModelProperty("考勤月份")
	private String checkDate;

	/**
	 * 园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer parkId;

	/**
	 * 园区ID
	 */
	@ApiModelProperty("工资月份")
	private List<Integer> parkIds;

	/**
	 * 签收状态
	 */
	@ApiModelProperty("签收状态")
	private Integer signStatus;

}
