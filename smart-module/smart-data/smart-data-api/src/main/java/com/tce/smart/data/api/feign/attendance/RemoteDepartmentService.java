package com.tce.smart.data.api.feign.attendance;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Auther: guohongtai
 * @Date: 2020-08-09 16:33
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteDepartmentService {
	@GetMapping("/attendance/department/info")
	Result info(@RequestParam("badge") String badge, @RequestParam("queryDate") String queryDate, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/attendance/department/list")
	Result list(@RequestParam("badge") String badge, @RequestParam("queryDate") String queryDate, @RequestParam("type") Integer type, @RequestHeader(SecurityConstants.FROM) String from);
}
