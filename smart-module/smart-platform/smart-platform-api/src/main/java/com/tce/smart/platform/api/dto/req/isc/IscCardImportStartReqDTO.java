package com.tce.smart.platform.api.dto.req.isc;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class IscCardImportStartReqDTO {

	@NotNull(message = "园区不能为空")
	private Integer parkId;

	private String staffScope;

	private String badge;
}
