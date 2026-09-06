package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * 分片内的删除请求种子，保存来源身份快照后再关联目标。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationRequestCommand {
	Long id;
	Integer parkId;
	String subjectType;
	String sourceType;
	String sourceRowId;
	String sourceIdentityKey;
	String identitySnapshot;
	Long generation;
	LocalDateTime deadlineAt;
}
