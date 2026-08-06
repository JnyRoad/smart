package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtWageSignDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 工资签单
 *
 * @author mingkai.wu
 * @date 2019-05-09 17:19:50
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteWageSignService {

	/**
	 * 保存工资签单
	 *
	 * @param smtWageSignDTO smtWageSignDTO
	 * @return Result<?>
	 */
	@PostMapping("/wage/sign/save")
	Result updateToSign(@RequestBody SmtWageSignDTO smtWageSignDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 每月一号定时同步员工任务
	 * @param from
	 * @return
	 */
	@GetMapping("/wage/sign/sync/task")
	Result syncTask(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
