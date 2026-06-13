package com.tce.smart.platform.api.dto.resp.outsrcapply;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 11:36
 */
@Data
@Builder
public class SmtOutSrcApplyDetailRespDTO extends BaseDTO {
	/**
	 * 申请单号
	 */
	@ApiModelProperty("申请单号")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long applyId;

	/**
	 * 申请时间
	 */
	@ApiModelProperty("申请时间")
	private String applyTime;

	/**
	 * 申请状态
	 */
	@ApiModelProperty("申请状态")
	private String statusDesc;

	/**
	 * 拒绝原因
	 */
	@ApiModelProperty("拒绝原因")
	private String reason;

	/**
	 * 审批人姓名
	 */
	@ApiModelProperty("审批人姓名")
	private String approver;

	/**
	 * 审批时间
	 */
	@ApiModelProperty("审批时间")
	private String approverTime;
}
