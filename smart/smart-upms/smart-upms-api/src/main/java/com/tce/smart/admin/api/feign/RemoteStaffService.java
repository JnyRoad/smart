package com.tce.smart.admin.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.InternalStaffProvisioningRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffLoginRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 17:48
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteStaffService {

	/**
	 * 仅供 UPMS 本地账号开通流程使用的员工最小资料。
	 */
	@GetMapping("/internal/staff/provisioning/{badge}")
	Result<InternalStaffProvisioningRespDTO> getProvisioningStaff(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	/**
	 * 登陆初始化员工权限
	 * @return
	 */
	@PostMapping("/staff/auth/login/init")
	Result<Boolean> inintLoginAuth(@RequestParam("badge") String badge);

	/**
	 * 手机号登录补建账号时使用的最小内部资料。
	 */
	@GetMapping("/internal/staff/login/mobile/{mobile}")
	Result<List<InternalStaffLoginRespDTO>> getLoginStaffByMobile(@PathVariable("mobile") String mobile,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);
}
