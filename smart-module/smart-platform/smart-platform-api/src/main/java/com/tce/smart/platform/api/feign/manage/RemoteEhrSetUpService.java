package com.tce.smart.platform.api.feign.manage;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 考勤汇总提醒设置与工资签收提醒设置
 * @author fushiping
 * @date 2020/7/10 15:34
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteEhrSetUpService {

	/**
	 * 考勤汇总提醒设置与工资签收提醒设置
	 * @param from
	 * @return
	 */
	@GetMapping("/ehr/setup/smg")
	Result<Boolean> sendMsg(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 考勤汇总提醒设置与工资签收提醒设置
	 * @param from
	 * @return
	 */
	@GetMapping("/ehr/setup/auto/confirm")
	Result<Boolean> autoConfirm(@RequestHeader(SecurityConstants.FROM) String from);

}
