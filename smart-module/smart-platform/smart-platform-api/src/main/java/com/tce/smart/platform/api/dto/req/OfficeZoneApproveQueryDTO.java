package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 办公区物品放行审批列表查询对象
 * @author sunfujian
 * @since 2021/9/9 11:40
 */
@Data
public class OfficeZoneApproveQueryDTO extends BaseDTO {
	@ApiModelProperty(value = "OA单号")
	private String processId;
	@ApiModelProperty(value = "申请人名称")
	private String name;
	@ApiModelProperty(value = "开始时间")
	private String startTime;
	@ApiModelProperty(value = "结束时间")
	private String endTime;
	@ApiModelProperty(value = "车牌号")
	private String licensePlate;
	@ApiModelProperty(value = "放行事项")
	private Integer releaseItem;
	@ApiModelProperty(value = "工号")
	private String badge;
	@ApiModelProperty(value = "审批状态,0-待审批,1-已审批")
	@NotNull(message = "审批状态字段不能为空")
	private Integer approvalStatus;
}
