package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:12:53
 */
@Data
public class SecurityAuthRelationReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("主键")
	private Long id;

	@ApiModelProperty("保密区id")
    private Long securityId;

	@ApiModelProperty("权限id")
    private Integer authId;

	@ApiModelProperty("权限名")
	private String authName;

}
