package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sunfujian
 * @date 2021/8/12 14:54
 */
@Data
public class ArticlesBackFactoryReqDTO extends BaseDTO {
	@ApiModelProperty(value = "OA单号")
	private String processId;
	@ApiModelProperty(value = "申请人名称")
	private String name;
	@ApiModelProperty(value = "物品名称")
	private String goodsName;
	@ApiModelProperty(value = "开始时间")
	private String startTime;
	@ApiModelProperty(value = "结束时间")
	private String endTime;
	@ApiModelProperty(value = "工号")
	private String badge;
	@ApiModelProperty(value = "审批状态,0-待审批,1-已审批")
	private Integer approvalStatus;
}
