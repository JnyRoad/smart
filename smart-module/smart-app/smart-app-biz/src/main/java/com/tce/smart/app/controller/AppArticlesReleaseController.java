package com.tce.smart.app.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.AddArticlesReleaseReqDTO;
import com.tce.smart.platform.api.feign.RemoteArticlesReleaseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-08-09 22:29
 */
@RestController
@AllArgsConstructor
@RequestMapping("/apparticles")
public class AppArticlesReleaseController extends BaseController {
	private final RemoteArticlesReleaseService remoteArticlesReleaseService;

	@GetMapping("/detail/{id}")
	public Result getByApproveId(@PathVariable("id") String approveId){
		return success(remoteArticlesReleaseService.getByApproveId(approveId, SecurityConstants.FROM_IN));
	}

	@PostMapping("/save")
	public Result save(@RequestBody AddArticlesReleaseReqDTO reqDTO){
		return success(remoteArticlesReleaseService.save(reqDTO, SecurityConstants.FROM_IN));
	}

	@GetMapping("/list")
	public Result list(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestParam("badge") String badge, @RequestParam("status") Integer status) {
		return success(remoteArticlesReleaseService.getRecord(current, size, badge, status, SecurityConstants.FROM_IN));
	}

	@Inner
	@GetMapping("/status/update")
	public Result status(@RequestParam("id") Long id, @RequestParam("approveBadge") String approveBadge, @RequestParam("status") Integer status, @RequestParam(value = "remark", required = false) String remark){
		return success(remoteArticlesReleaseService.status(id, approveBadge, status, remark, SecurityConstants.FROM_IN));
	}
}
