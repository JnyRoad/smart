package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: SmtSecurityAreaSupplierVO
 * @date: 2020-07-23 17:05
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtStaffAppealQueryVO extends SmtStaffAppealListVO{
	/**
	 * BU
	 */
	@ApiModelProperty("BU")
	private String compName;

	/**
	 * 部门名称
	 */
	@ApiModelProperty("部门名称")
	private String depName;

	/**
	 * 反馈人电话
	 */
	@ApiModelProperty("反馈人电话")
	private String staffPhone;

	/**
	 * 回复人名称
	 */
	@ApiModelProperty("回复人名称")
	private String replyName;

	/**
	 * 回复内容
	 */
	@ApiModelProperty("回复内容")
	private String replyDesc;

	/**
	 * 回复时间
	 */
	@ApiModelProperty("回复时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date replyTime;
}
