package com.tce.smart.platform.api.dto.resp.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
@Builder
public class SecurityPersonAddRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;


	@ApiModelProperty("关联人员id")
	private Long staffId;

	@ApiModelProperty("人员工号")
	private String staffBadge;

	@ApiModelProperty("人员姓名")
	private String staffName;

	@ApiModelProperty("人员信息")
	private String staffInfo;
}
