package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorUpdateDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterConcentratorRespDTO;
import com.tce.smart.platform.service.watermeter.SmtWaterValveConcentratorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Li.JiaJun
 * @since 2022/5/10 17:37
 */
@RestController
@RequestMapping("/water/meter/valve/concentrator")
@Api(tags = "智能水表外置阀门集中器")
public class WaterValveConcentratorController extends BaseController {

	@Autowired
	private SmtWaterValveConcentratorService valveConcentratorService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询外置阀门集中器")
	public Result<IPage<WaterMeterConcentratorRespDTO>> getValveConcentratorPage(Page page, WaterValveConcentratorQueryDTO dto) {
		return success(valveConcentratorService.getPage(page, dto), WaterMeterConcentratorRespDTO.class);
	}

	@PostMapping("/save")
	@ApiOperation(value = "新增外置阀门集中器")
	public Result<Boolean> saveValveConcentrator(@RequestBody @Valid WaterValveConcentratorAddDTO dto) {
		return success(valveConcentratorService.addConcentrator(dto));
	}

	@PostMapping("/update")
	@ApiOperation(value = "修改外置阀门集中器")
	public Result<Boolean> updateValveConcentrator(@RequestBody @Valid WaterValveConcentratorUpdateDTO dto) {
		return success(valveConcentratorService.updateConcentrator(dto));
	}

	@PostMapping("/del/{id}")
	@ApiOperation(value = "删除外置阀门集中器")
	public Result<Boolean> delValveConcentrator(@PathVariable("id") Long id) {
		return success(valveConcentratorService.delConcentrator(id));
	}
}
