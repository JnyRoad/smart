package com.tce.smart.platform.dto.authgovernance;

import lombok.Data;

/** 问题队列分页；普通入口必须提供单个明确园区。 */
@Data
public class AuthOperationReviewPageQuery {
	private Integer parkId;
	private Integer current = 1;
	private Integer size = 20;
}
