package com.tce.smart.platform.core.dto.authoperation;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * 权限目标分片续接命令，列表由构造器复制为不可变集合。
 */
@Value
@Builder(toBuilder = true)
public class AuthOperationAppendCommand {
	Long batchId;
	Long previousCursor;
	Long nextCursor;
	@Singular("request")
	List<AuthOperationRequestCommand> requests;
	@Singular("target")
	List<AuthOperationTargetCommand> targets;
}
