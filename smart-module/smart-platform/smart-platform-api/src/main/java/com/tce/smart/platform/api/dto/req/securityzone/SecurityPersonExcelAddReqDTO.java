package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
public class SecurityPersonExcelAddReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("保密区ID")
	private Long securityId;

	@ApiModelProperty("人员工号")
	private String staffBadge;

	@ApiModelProperty("人员姓名")
	private String staffName;

	@ApiModelProperty("园区id")
	private Integer parkId;

}
