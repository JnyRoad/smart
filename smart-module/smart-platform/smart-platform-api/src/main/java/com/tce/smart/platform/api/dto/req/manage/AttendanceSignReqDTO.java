package com.tce.smart.platform.api.dto.req.manage;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 考勤签单
 *
 * @date 2019-05-09 15:17:02
 */
@Data
public class AttendanceSignReqDTO {

	/**
	 * 工资月份
	 */
	@ApiModelProperty("工资月份")
	private String attendanceDate;

	/**
	 * 签名照
	 */
	@ApiModelProperty("签名照")
	private String signImg;

	@ApiModelProperty("是否有异议")
	private Integer isObjection;

	@ApiModelProperty("异议")
	private String objection;

}
