package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @description: 离职交接项确认提交DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaveHandoverSubmitReqDTO implements Serializable {
	private static final long serialVersionUID = -7697559926009574845L;

	@ApiModelProperty(value = "离职申请Id")
	private Integer applicationId;

	@ApiModelProperty(value = "交接项")
	private List<HandoverItem> items;

	@Data
	public static class HandoverItem{

		@ApiModelProperty(value = "交接项Id")
		private Integer itemId;

		@ApiModelProperty(value = "金额")
		private BigDecimal amount;

		@ApiModelProperty(value = "说明")
		private String remark;
	}
}
