package com.tce.smart.platform.api.dto.req.visitormanage;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 访客审批白名单添加DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorWhiteReqDTO implements Serializable {
	private static final long serialVersionUID = -8687825383685235719L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge;
}
