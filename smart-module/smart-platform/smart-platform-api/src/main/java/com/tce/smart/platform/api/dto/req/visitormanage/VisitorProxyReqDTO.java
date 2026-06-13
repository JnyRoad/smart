package com.tce.smart.platform.api.dto.req.visitormanage;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 访客审批代理添加DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorProxyReqDTO implements Serializable {
	private static final long serialVersionUID = 2999125011325852721L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "被访人员工工号")
	private String interVieweeBadge;

	@ApiModelProperty(value = "代理人员工工号")
	private String proxyBadge;
}
