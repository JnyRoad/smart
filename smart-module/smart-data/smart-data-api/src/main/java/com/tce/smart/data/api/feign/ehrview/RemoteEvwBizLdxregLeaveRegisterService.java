package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:36
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwBizLdxregLeaveRegisterService {
	@GetMapping("/evwBizLdxregLeaveRegister/list")
	Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/evwBizLdxregLeaveRegister/detail")
	Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime, @RequestHeader(SecurityConstants.FROM) String from);
}
