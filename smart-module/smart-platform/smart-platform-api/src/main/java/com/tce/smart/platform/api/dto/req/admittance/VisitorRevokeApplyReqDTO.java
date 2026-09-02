package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 访客自助作废入厂申请请求。
 */
@Data
public class VisitorRevokeApplyReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "入厂申请单 ID", required = true)
	private String applyId;
}
