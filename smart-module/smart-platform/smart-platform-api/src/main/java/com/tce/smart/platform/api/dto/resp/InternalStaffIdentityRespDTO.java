package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * OCR 完善资料和工商银行实名请求专用的员工身份资料。
 *
 * 完整证件号只允许受控服务端流程消费，绝不允许透传至 App 客户端或写入日志。
 */
@Data
@ApiModel("内部员工身份响应")
public class InternalStaffIdentityRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("完整证件号，仅限服务端受控流程")
	private String certno;
}
