package com.tce.smart.data.api.feign.ehrview;

import java.util.List;

import com.tce.smart.common.core.constant.SecurityConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizAregotRegisterRespDTO;

/**
 * 加班记录
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwBizAregotRegisterService {

	 @GetMapping("/evwBizAregotRegister/info")
	 Result<List<EvwBizAregotRegisterRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("otTerm") String otTerm,
			 @RequestHeader(SecurityConstants.FROM) String from,
			 @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/evwBizAregotRegister/list")
	Result list(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/evwBizAregotRegister/detail")
	Result getByBadge(@RequestParam("badge") String badge, @RequestParam("otterm") String otterm, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
