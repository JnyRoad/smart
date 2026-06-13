package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteReqDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorWhiteQueryRespDTO;
import com.tce.smart.platform.service.SmtVisitorApprovalWhiteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 访客审批白名单管理
 * @date: 2020/12/29 15:41
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "访客审批白名单管理")
@RestController
@AllArgsConstructor
@RequestMapping("/visitor/white")
public class SmtVisitorApprovalWhiteController {

	private final SmtVisitorApprovalWhiteService smtVisitorApprovalWhiteService;

	@ApiOperation("分页查询白名单")
	@GetMapping("/page")
	public Result<IPage<VisitorWhiteQueryRespDTO>> pageQuery(VisitorWhiteQueryReqDTO visitorWhiteReqDTO) {
		return new Result<>(smtVisitorApprovalWhiteService.pageQuery(visitorWhiteReqDTO));
	}

	@ApiOperation("添加名单")
	@PostMapping("/save")
	public Result<Boolean> saveItem(@RequestBody VisitorWhiteReqDTO visitorWhiteReqDTO) {
		return new Result<>(smtVisitorApprovalWhiteService.saveItem(visitorWhiteReqDTO));
	}

	@ApiOperation("批量删除")
	@PostMapping("/batch/del")
	public Result<Boolean> batchDel(@RequestBody List<String> ids) {
		List<Long> longIds = ids.stream().map(Long::parseLong).collect(Collectors.toList());
		return new Result<>(smtVisitorApprovalWhiteService.batchDel(longIds));
	}
}
