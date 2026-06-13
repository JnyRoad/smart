package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 *
 *OA同步区域与权限关联表
 * @author fushiping
 * @date 2021-07-29 11:13:44
 */
@Data
public class OaAreaRelationEditReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("oa区域")
    private String oaAreaName;

	@ApiModelProperty("oa区域id")
    private Integer oaAreaId;

	@ApiModelProperty("园区id")
    private Integer parkId;

	@ApiModelProperty("关联权限")
	private List<RelationAuth> authIds;

	@Data
	public static class RelationAuth {

		@ApiModelProperty("权限id")
		private Integer authId;

		@ApiModelProperty("权限名")
		private String authName;

		@ApiModelProperty("权限类型")
		private Integer authType;
	}
}
