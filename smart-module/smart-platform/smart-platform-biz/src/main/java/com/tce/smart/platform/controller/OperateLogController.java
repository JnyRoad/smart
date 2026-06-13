package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.OperateLogQueryDTO;
import com.tce.smart.platform.api.dto.resp.OperateLogRespDTO;
import com.tce.smart.platform.service.SmtOperateLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:33
 */
@RestController
@RequestMapping("/operate/log")
@Api(tags = "操作日志")
public class OperateLogController extends BaseController {

	@Autowired
	private SmtOperateLogService operateLogService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询操作日志")
	public Result<IPage<OperateLogRespDTO>> getHistoryPage(Page page, @Valid OperateLogQueryDTO dto) {
		return success(operateLogService.getPage(page, dto), OperateLogRespDTO.class);
	}

	@GetMapping("/list")
	@ApiOperation(value = "查询操作日志集合")
	public Result<List<OperateLogRespDTO>> getValvePage(@Valid OperateLogQueryDTO dto) {
		return success(operateLogService.getList(dto), OperateLogRespDTO.class);
	}
}
