package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录员工的入住资料摘要。
 *
 * 身份证号仅允许以脱敏形式返回，客户端不得据此构造或覆盖员工身份资料。
 */
@Data
@ApiModel("本人入住资料响应")
public class StaffSelfCheckInProfileRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("资料是否完整")
	private Boolean profileComplete;

	@ApiModelProperty("脱敏身份证号")
	private String maskedCertNo;
}
