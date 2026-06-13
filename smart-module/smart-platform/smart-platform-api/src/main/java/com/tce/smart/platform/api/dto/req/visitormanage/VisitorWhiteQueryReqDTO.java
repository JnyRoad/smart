package com.tce.smart.platform.api.dto.req.visitormanage;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 访客审批白名单查询DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorWhiteQueryReqDTO implements Serializable {
	private static final long serialVersionUID = -2060285219812512284L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge;

	@ApiModelProperty(value = "员工姓名")
	private String staffName;

	@ApiModelProperty(value = "BUID")
	private Integer compId;

	@ApiModelProperty(value = "部门ID")
	private Integer depId;

	@ApiModelProperty(value = "岗位ID")
	private String jobId;

	@ApiModelProperty(value = "当前页")
	private Long current;

	@ApiModelProperty(value = "页大小")
	private Long size;
}
