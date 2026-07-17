package com.tce.smart.platform.api.feign.securityzone;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 权限自动删除
 *
 * @author fushiping
 * @date
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSecurityAuthService {


	/**
	 * 权限自动删除任务
	 * @param from
	 * @return
	 */
	@GetMapping("/security/auth/delete/task")
	Result syncTask(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 下发提示信息推送
	 * @param from
	 * @return
	 */
	@GetMapping("/security/auth/apply/msg")
	Result sendMessage(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 保密门禁申请 OA 审批状态对账任务（spec §3.1.3/§3.1.4，PR2 定时补偿入口）
	 * @param from
	 * @return
	 */
	@GetMapping("/security/auth/apply/oa/status/task")
	Result updateOaStatusTask(@RequestHeader(SecurityConstants.FROM) String from);

	/** 消费保密区权限下发持久化命令。 */
	@PostMapping("/security/auth/apply/dispatch/process")
	Result<Integer> processDispatch(@RequestHeader(SecurityConstants.FROM) String from);

	/** ISC 任务终态触发当前批次状态聚合。 */
	@PostMapping("/security/auth/apply/{id}/dispatch/sync")
	Result syncDispatchStatus(@PathVariable("id") Long applyId,
			@RequestHeader(SecurityConstants.FROM) String from);
}
