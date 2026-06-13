package com.tce.smart.platform.api.dto.req.outsrcapply;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 11:13
 */
@Data
public class SmtOutSrcApplyReqDTO extends BaseDTO {

	/**
	 * 申请开始时间
	 */
	@ApiModelProperty("申请开始时间")
	private String applyStartTime;

	/**
	 * 申请结束时间
	 */
	@ApiModelProperty("申请结束时间")
	private String applyEndTime;

	/**
	 * 状态
	 */
	@ApiModelProperty("状态：0、待审批；1、已通过；2、已拒绝")
	private Integer status;

	/**
	 * 单位名称
	 */
	@ApiModelProperty("单位名称")
	private String compName;

	/**
	 * 是否审批菜单
	 */
	@ApiModelProperty("是否审批菜单")
	private Boolean isApprove;
}
