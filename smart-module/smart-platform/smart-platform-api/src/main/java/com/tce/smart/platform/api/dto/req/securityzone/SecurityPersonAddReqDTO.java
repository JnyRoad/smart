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
public class SecurityPersonAddReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("保密区id列表")
	private List<Long> securityId;

	@ApiModelProperty("园区id")
	private Integer parkId;

	@ApiModelProperty("关联人员id")
	private Long staffId;

	@ApiModelProperty("人员工号")
	private String staffBadge;

	@ApiModelProperty("人员姓名")
	private String staffName;

	@ApiModelProperty("BU")
	private String buName;

	@ApiModelProperty("部门")
	private String deptName;

	@ApiModelProperty("职位")
	private String jobName;

	@ApiModelProperty("是否已下发权限 (非必传)")
	private Integer isAuth;

}
