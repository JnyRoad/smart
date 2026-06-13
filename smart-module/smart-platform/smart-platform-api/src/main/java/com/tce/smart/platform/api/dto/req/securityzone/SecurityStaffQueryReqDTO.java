package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
public class SecurityStaffQueryReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("人员工号")
    private List<String> staffBadges;

	@ApiModelProperty("人员id")
	private List<Long> staffIds;


}
