package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 治理幂等载荷冲突必须返回HTTP 409，不能被通用异常处理器降成普通失败。 */
@RestControllerAdvice(assignableTypes = AuthOperationGovernanceController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthOperationGovernanceControllerAdvice {

	@ExceptionHandler(AuthOperationGovernanceConflictException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Result conflict(AuthOperationGovernanceConflictException conflict) {
		return Result.fail(HttpStatus.CONFLICT.value(), conflict.getMessage());
	}
}
