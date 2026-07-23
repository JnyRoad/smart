package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信绑定等内部流程使用的员工最小资料。
 *
 * 证件号仅保留末六位，用于服务端比对，禁止返回完整证件号。
 */
@Data
@ApiModel("内部员工绑定响应")
public class InternalStaffBindingRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("员工状态")
	private Integer status;

	@ApiModelProperty("证件号末六位")
	private String certNoLast6;
}
