package com.tce.smart.platform.api.dto.resp.outsrcapply;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 11:20
 */
@Data
@Builder
public class SmtOutSrcApplyRespDTO extends BaseDTO {
	/**
	 * 申请单号
	 */
	@ApiModelProperty("申请单号")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long applyId;

	/**
	 * 单位名称
	 */
	@ApiModelProperty("单位名称")
	private String compName;

	/**
	 * 申请人数
	 */
	@ApiModelProperty("申请人数")
	private Integer applyNum;

	/**
	 * 申请时间
	 */
	@ApiModelProperty("申请时间")
	private String applyTime;

	/**
	 * 状态
	 */
	@ApiModelProperty("状态")
	private String statusDesc;
}
