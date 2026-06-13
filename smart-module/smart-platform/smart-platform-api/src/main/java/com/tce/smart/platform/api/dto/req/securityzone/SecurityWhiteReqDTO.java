package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *权限删除白名单
 * @author fushiping
 * @date 2021-07-29 11:13:07
 */
@Data
public class SecurityWhiteReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("员工id")
    private Long staffId;

	@ApiModelProperty("员工工号")
    private String staffBadge;

	@ApiModelProperty("员工姓名")
    private String staffName;

	@ApiModelProperty("园区id")
	private Integer parkId;

}
