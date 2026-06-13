package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupExecuteReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupPageReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupRecordRespDTO;
import com.tce.smart.platform.service.isc.SmtIscAccessCleanupService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/isc/access/cleanup")
public class SmtIscAccessCleanupController extends BaseController {

	private final SmtIscAccessCleanupService cleanupService;

	@GetMapping("/page")
	public Result getPage(Page page, IscAccessCleanupPageReqDTO query) {
		return success(cleanupService.getPage(page, query, allowedParkIds()), IscAccessCleanupRecordRespDTO.class);
	}

	@GetMapping("/summary")
	public Result getSummary(IscAccessCleanupPageReqDTO query) {
		return success(cleanupService.getSummary(query, allowedParkIds()));
	}

	@SysLog("生成ISC权限残留删除任务")
	@PostMapping("/execute")
	public Result execute(@RequestBody IscAccessCleanupExecuteReqDTO reqDTO) {
		return success(cleanupService.execute(reqDTO, allowedParkIds()));
	}

	private List<Integer> allowedParkIds() {
		return SecurityUtils.getUser() == null ? Collections.emptyList() : SecurityUtils.getUser().getParkIdList();
	}
}
