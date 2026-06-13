package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterHisQueryDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterHisRespDTO;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/5/10 17:29
 */
@RestController
@RequestMapping("/water/meter/history")
@Api(tags = "智能水表历史读数")
public class WaterHistoryController extends BaseController {

	@Autowired
	private SmtWaterMeterHistoryService historyService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询历史读数")
	public Result<IPage<WaterMeterHisRespDTO>> getHistoryPage(Page page, @Valid WaterMeterHisQueryDTO dto) {
		return success(historyService.getPage(page, dto), WaterMeterHisRespDTO.class);
	}

	@GetMapping("/list")
	@ApiOperation(value = "查询历史读数集合")
	public Result<List<WaterMeterHisRespDTO>> getHistoryList(@Valid WaterMeterHisQueryDTO dto) {
		return success(historyService.getList(dto), WaterMeterHisRespDTO.class);
	}

	@GetMapping("/export/{id}")
	@ApiOperation(value = "导出历史读数Excel")
	public ResponseEntity<byte[]> exportHistoryList(@PathVariable("id") Long id) {
		return historyService.exportHistory(id);
	}
}
