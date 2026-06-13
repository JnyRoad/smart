package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:37
 */
@Data
public class SecurityApplyPersonReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("id")
	private Long id;

	@ApiModelProperty("员工工号")
    private String badge;

	@ApiModelProperty("员工姓名")
    private String staffName;

	@ApiModelProperty("员工部门")
	private String staffDepId;

	@ApiModelProperty("员工职务")
	private String staffJobId;

	@ApiModelProperty("申请区域名")
	private String areaName;

	@ApiModelProperty("申请权限")
	private List<ApplyAuth> applyAuths;

	@Data
	public static class ApplyAuth{

		@ApiModelProperty("权限名")
		private String authName;

		@ApiModelProperty("权限ID")
		private Integer authId;
	}

}
