package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorUpdateDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterConcentratorRespDTO;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterConcentratorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Li.JiaJun
 * @since 2022/5/10 17:30
 */
@RestController
@RequestMapping("/water/meter/concentrator")
@Api(tags = "智能水表集中器")
public class WaterConcentratorController extends BaseController {

	@Autowired
	private SmtWaterMeterConcentratorService concentratorService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询水表集中器")
	public Result<IPage<WaterMeterConcentratorRespDTO>> getConcentratorPage(Page page, WaterMeterConcentratorQueryDTO dto) {
		return success(concentratorService.getPage(page, dto), WaterMeterConcentratorRespDTO.class);
	}

	@PostMapping("/save")
	@ApiOperation(value = "新增水表集中器")
	public Result<Boolean> saveConcentrator(@RequestBody @Valid WaterMeterConcentratorAddDTO dto) {
		return success(concentratorService.addConcentrator(dto));
	}

	@PostMapping("/update")
	@ApiOperation(value = "修改水表集中器")
	public Result<Boolean> updateConcentrator(@RequestBody @Valid WaterMeterConcentratorUpdateDTO dto) {
		return success(concentratorService.updateConcentrator(dto));
	}

	@PostMapping("/del/{id}")
	@ApiOperation(value = "删除水表集中器")
	public Result<Boolean> delConcentrator(@PathVariable("id") Long id) {
		return success(concentratorService.delConcentrator(id));
	}

	@PostMapping("/query/file/{id}")
	@ApiOperation(value = "水表集中器查询档案")
	public Result<Boolean> queryFile(@PathVariable("id") Long id) {
		return success(concentratorService.queryFile(id));
	}
}
