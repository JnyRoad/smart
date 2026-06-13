package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class SecurityAuthRelationRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty("主键")
	private Long id;

	@ApiModelProperty("保密区id")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long securityId;

	@ApiModelProperty("权限id")
    private Integer authId;

	@ApiModelProperty("权限名")
	private String authName;

}
