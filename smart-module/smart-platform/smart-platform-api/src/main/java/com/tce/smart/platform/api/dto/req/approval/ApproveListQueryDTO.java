package com.tce.smart.platform.api.dto.req.approval;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author sunfujian
 * @since 2021/9/27 17:37
 */
@Data
public class ApproveListQueryDTO extends BaseDTO {
	@NotNull(message = "审批类型不能为空")
	private Integer recordType;
	@NotNull(message = "审批状态不能为空")
	private Integer recordState;
	@ApiModelProperty(value = "携带人名称")
	private String name;
	@ApiModelProperty(value = "携带人工号")
	private String badge;
	@ApiModelProperty(value = "开始时间")
	private String startTime;
	@ApiModelProperty(value = "结束时间")
	private String endTime;
}
