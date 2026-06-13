package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.isc.IscCardTaskPageReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscCardTaskRespDTO;
import com.tce.smart.platform.core.service.SmtIscCardTaskService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/isc/card/task")
public class SmtIscCardTaskController extends BaseController {

	private final SmtIscCardTaskService smtIscCardTaskService;

	@GetMapping("/page")
	public Result getPage(Page page, IscCardTaskPageReqDTO query) {
		IscCardTaskPageReqDTO pageQuery = query == null ? new IscCardTaskPageReqDTO() : query;
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		pageQuery.setParkIds(parkIds);
		return success(smtIscCardTaskService.getPage(page, pageQuery), IscCardTaskRespDTO.class);
	}
}
