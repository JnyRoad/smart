package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLdxRegLeaveAllRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:36
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwLdxRegLeaveAllService {
	@GetMapping("/evwLdxRegLeaveAll/list")
	Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/evwLdxRegLeaveAll/list/byDay")
	Result<List<EvwLdxRegLeaveAllRespDTO>> listByDay(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/evwLdxRegLeaveAll/detail")
	Result getByBadge(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime, @RequestHeader(SecurityConstants.FROM) String from);
}
