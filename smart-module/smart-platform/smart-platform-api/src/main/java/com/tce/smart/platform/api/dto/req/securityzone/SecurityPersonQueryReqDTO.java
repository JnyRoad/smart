package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
public class SecurityPersonQueryReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("关联人员ID")
	private List<String> relationId;

	@ApiModelProperty("保密区id")
	private Long securityId;

	@ApiModelProperty("园区id")
	private Integer parkId;

	@ApiModelProperty("人员工号")
	private String staffBadge;

	@ApiModelProperty("人员工号")
	private List<String> staffBadges;

	@ApiModelProperty("人员姓名")
	private String staffName;

	@ApiModelProperty("人员BU")
	private String buId;

	@ApiModelProperty("人员部门")
	private String depId;

	@ApiModelProperty("bu列表")
	private List<String> buIds;

	@ApiModelProperty("部门列表")
	private List<String> depIds;

	@ApiModelProperty("签署状态")
	private Integer signStatus;

	@ApiModelProperty("入职时间")
	private String startDate;

	@ApiModelProperty("入职时间")
	private String endDate;

}
