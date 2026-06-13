package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorProxyQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorProxyReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteReqDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorProxyQueryRespDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorWhiteQueryRespDTO;
import com.tce.smart.platform.service.SmtVisitorApprovalProxyService;
import com.tce.smart.platform.service.SmtVisitorApprovalWhiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 访客审批代理管理
 * @date: 2020/12/29 15:41
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "访客审批代理管理")
@RestController
@AllArgsConstructor
@RequestMapping("/visitor/proxy")
public class SmtVisitorApprovalProxyController {

	private final SmtVisitorApprovalProxyService smtVisitorApprovalProxyService;

	@ApiOperation("分页查询")
	@GetMapping("/page")
	public Result<IPage<VisitorProxyQueryRespDTO>> pageQuery(VisitorProxyQueryReqDTO visitorProxyQueryReqDTO) {
		return new Result<>(smtVisitorApprovalProxyService.pageQuery(visitorProxyQueryReqDTO));
	}

	@ApiOperation("添加代理")
	@PostMapping("/save")
	public Result<Boolean> saveProxy(@RequestBody VisitorProxyReqDTO visitorProxyReqDTO) {
		return new Result<>(smtVisitorApprovalProxyService.saveProxy(visitorProxyReqDTO));
	}

	@ApiOperation("批量删除")
	@PostMapping("/batch/del")
	public Result<Boolean> batchDel(@RequestBody List<String> ids) {
		List<Long> longIds = ids.stream().map(Long::parseLong).collect(Collectors.toList());
		return new Result<>(smtVisitorApprovalProxyService.batchDel(longIds));
	}
}
