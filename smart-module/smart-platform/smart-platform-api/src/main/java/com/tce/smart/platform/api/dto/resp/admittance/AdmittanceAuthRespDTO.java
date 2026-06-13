package com.tce.smart.platform.api.dto.resp.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdmittanceAuthRespDTO implements Serializable {
private static final long serialVersionUID = 7302343024987546495L;

	@ApiModelProperty("oa区域类型id")
	private Integer areaTypeId;

	@ApiModelProperty("oa区域类型名")
	private String typeName;

	@ApiModelProperty("关联权限策略")
	private List<AuthList> authLists;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	private Integer factoryType;

	@Data
	public static class AuthList{

		@ApiModelProperty("权限策略名")
		private String authName;

		@ApiModelProperty("权限策略id")
		private Integer authId;

		@ApiModelProperty("权限类型")
		private Integer authType;
	}

}
