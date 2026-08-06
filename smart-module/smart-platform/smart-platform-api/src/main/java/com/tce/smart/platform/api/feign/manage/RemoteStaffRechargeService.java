package com.tce.smart.platform.api.feign.manage;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 员工充值名单
 * @author fushiping
 * @date 2020/7/10 15:34
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteStaffRechargeService {

	/**
	 * 新员工充值名单定时任务
	 * @param from
	 * @return
	 */
	@GetMapping("/recharge/new/recharge")
	Result<Boolean> syncNewStaff(@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 老员工充值名单定时任务
	 * @param from
	 * @return
	 */
	@GetMapping("/recharge/senior/recharge")
	Result<Boolean> syncSeniorStaff(@RequestHeader(SecurityConstants.FROM) String from);

}
