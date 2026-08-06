package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 申请审批
 * @author 梁圆
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteVisitorTaskService {

	/**
	 * 访客超时，更改访客状态
	 * @return
	 */
	@GetMapping("/visitorTask/overTime")
    Result visitorOverTime(@RequestParam("parkId") Integer parkId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 访客超时未离开，给被访人发短息
	 * @param from
	 * @return
	 */
	@GetMapping("/visitorTask/overTimeNoLeave")
    Result overTimeNoLeave(@RequestParam("parkId") Integer parkId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 访客已经到达
	 * @return
	 */
/*	@GetMapping("/visitorTask/comeOnTime")
    Result visitorComeOnTime(@RequestHeader(SecurityConstants.FROM) String from);*/
	/**
	 * 访客提醒
	 * @return
	 */
	@GetMapping("/visitorTask/remind")
    Result visitorRemind(@RequestParam("parkId") Integer parkId, @RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 推送访客信息给email
	 * @param
	 * @return
	 */
	@GetMapping("/visitorTask/toEmail")
	Result toEmail(@RequestHeader(SecurityConstants.FROM) String from);

}
