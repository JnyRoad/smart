package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * UPMS 创建或更新本地账号时使用的最小员工资料。
 *
 * 此资料仅允许 UPMS 服务端账号开通流程消费：证件号仅保留末六位，手机号不得返回客户端或写日志。
 */
@Data
@ApiModel("内部员工账号开通响应")
public class InternalStaffProvisioningRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工状态")
	private Integer status;

	@ApiModelProperty("证件号末六位")
	private String certNoLast6;

	@ApiModelProperty("员工预留手机号，仅限 UPMS 服务端账号开通")
	private String phone;
}
