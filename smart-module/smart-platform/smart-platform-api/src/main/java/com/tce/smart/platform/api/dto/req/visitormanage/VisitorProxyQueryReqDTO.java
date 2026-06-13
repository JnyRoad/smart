package com.tce.smart.platform.api.dto.req.visitormanage;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 访客审批代理查询DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorProxyQueryReqDTO implements Serializable {
	private static final long serialVersionUID = -5895170256885011919L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "被访人员工工号")
	private String interVieweeBadge;

	@ApiModelProperty(value = "被访人姓名")
	private String interVieweeName;

	@ApiModelProperty(value = "代理人员工工号")
	private String proxyBadge;

	@ApiModelProperty(value = "代理人姓名")
	private String proxyName;

	@ApiModelProperty(value = "当前页")
	private Long current;

	@ApiModelProperty(value = "页大小")
	private Long size;
}
