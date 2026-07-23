package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Smart Schedule ISC 查询与卡片下发专用的最小员工资料。
 *
 * 证件号仅用于 ISC 受控服务端按人员复查，严禁写入日志或透传客户端。
 */
@Data
@ApiModel("Schedule员工身份内部响应")
public class InternalScheduleStaffIdentityRespDTO {

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("完整证件号，仅 ISC 受控服务端流程使用")
	private String certno;

	@ApiModelProperty("员工状态")
	private Integer status;
}
