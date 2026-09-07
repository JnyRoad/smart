package com.tce.smart.platform.dto.authgovernance;

import lombok.Builder;
import lombok.Value;

/** 园区或全局问题队列投影。 */
@Value
@Builder
public class AuthOperationReviewView {
	String reviewId;
	Integer parkId;
	String accessType;
	String deviceId;
	String taskKey;
	String reason;
	String state;
	String createdAt;
}
