package com.tce.smart.platform.api.feign.admittance;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 入厂申请定时任务
 * @author fushiping
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteAdmittanceTaskService {

	/**
	 * 访客超时，更改访客状态
	 * @return
	 */
	@GetMapping("/admittance/apply/overTime")
    Result visitorOverTime(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 访客超时未离开
	 * @return
	 */
	@GetMapping("/admittance/apply/overTimeNoLeave")
    Result overTimeNoLeave(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 访客已经到达
	 * @return
	 */
	@GetMapping("/admittance/apply/comeOnTime")
    Result visitorComeOnTime(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 访客提醒
	 * @return
	 */
	@GetMapping("/admittance/apply/remind")
	Result visitorRemind(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 访客提醒
	 * @return
	 */
	@GetMapping("/admittance/apply/update/oa")
	Result updateOaStatusTask(@RequestHeader(SecurityConstants.FROM) String from);


}
