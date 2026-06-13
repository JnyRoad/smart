package com.tce.smart.platform.api.feign.manage;

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
 * 考勤签单
 *
 * @author fushiping
 * @date 2020-07-27 17:19:50
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteAttendanceSignService {


	/**
	 * 每月一号定时同步员工任务
	 * @param from
	 * @return
	 */
	@GetMapping("/attendance/sign/sync/task")
	Result syncTask(@RequestHeader(SecurityConstants.FROM) String from);
}
