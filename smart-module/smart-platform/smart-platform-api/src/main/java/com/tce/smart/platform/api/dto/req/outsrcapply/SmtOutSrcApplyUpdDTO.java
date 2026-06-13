package com.tce.smart.platform.api.dto.req.outsrcapply;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 14:20
 */
@Data
public class SmtOutSrcApplyUpdDTO extends BaseDTO {
	/**
	 * 申请单ID
	 */
	@ApiModelProperty("申请单ID")
	private Long applyId;

	/**
	 * 状态：1、通过；2、拒绝
	 */
	@ApiModelProperty("状态：1、通过；2、拒绝")
	private Integer status;

	/**
	 * 拒绝原因
	 */
	@ApiModelProperty("拒绝原因")
	private String reason;
}
