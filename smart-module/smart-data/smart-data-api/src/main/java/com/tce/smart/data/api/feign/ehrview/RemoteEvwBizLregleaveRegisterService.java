package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
/**
 * 审批中的请假
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwBizLregleaveRegisterService {
	@GetMapping("/evwBizLregleaveRegister/info")
	Result info(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime,@RequestParam("endTime") String endTime);

	@GetMapping("/evwBizLregleaveRegister/list")
	Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/evwBizLregleaveRegister/detail")
	Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
