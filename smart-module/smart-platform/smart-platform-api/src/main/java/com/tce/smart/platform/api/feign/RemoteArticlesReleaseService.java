package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddArticlesReleaseReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-08-09 22:25
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteArticlesReleaseService {
	@GetMapping("/articlesrelease/detail/approveId/{id}")
	Result getByApproveId(@PathVariable("id") String id, @RequestHeader(SecurityConstants.FROM) String from);

	@PostMapping("/articlesrelease/save")
	Result save(@RequestBody AddArticlesReleaseReqDTO reqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/articlesrelease/list")
	Result getRecord(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestParam("badge") String badge, @RequestParam("status") Integer status, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/articlesrelease/app/status/update")
	Result status(@RequestParam("id") Long id, @RequestParam("approveBadge") String approveBadge, @RequestParam("status") Integer status, @RequestParam(value = "remark", required = false) String remark, @RequestHeader(SecurityConstants.FROM) String from);
}
