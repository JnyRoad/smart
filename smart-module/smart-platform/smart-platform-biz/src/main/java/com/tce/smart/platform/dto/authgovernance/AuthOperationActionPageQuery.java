package com.tce.smart.platform.dto.authgovernance;

import lombok.Data;

/** 目标治理历史分页。 */
@Data
public class AuthOperationActionPageQuery {
	private Integer current = 1;
	private Integer size = 20;
}
