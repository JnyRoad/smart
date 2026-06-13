package com.tce.smart.platform.controller.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterConcentratorUpdateDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterConcentratorRespDTO;
import com.tce.smart.platform.service.watermeter.SmtEleMeterConcentratorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Li.JiaJun
 * @since 2022/5/10 17:26
 */
@RestController
@RequestMapping("/ele/meter/concentrator")
@Api(tags = "智能电表集中器")
public class EleConcentratorController extends BaseController {

	@Autowired
	private SmtEleMeterConcentratorService concentratorService;

	@GetMapping("/page")
	@ApiOperation(value = "分页查询电表集中器")
	public Result<IPage<EleMeterConcentratorRespDTO>> getConcentratorPage(Page page, EleMeterConcentratorQueryDTO dto) {
		return success(concentratorService.getPage(page, dto), EleMeterConcentratorRespDTO.class);
	}

	@PostMapping("/save")
	@ApiOperation(value = "新增电表集中器")
	public Result<Boolean> addConcentrator(@RequestBody @Valid EleMeterConcentratorAddDTO dto) {
		return success(concentratorService.addConcentrator(dto));
	}

	@PostMapping("/update")
	@ApiOperation(value = "修改电表集中器")
	public Result<Boolean> updateConcentrator(@RequestBody @Valid EleMeterConcentratorUpdateDTO dto) {
		return success(concentratorService.updateConcentrator(dto));
	}

	@PostMapping("/del/{id}")
	@ApiOperation(value = "删除电表集中器")
	public Result<Boolean> delConcentrator(@PathVariable("id") Long id) {
		return success(concentratorService.delConcentrator(id));
	}

	@PostMapping("/query/file/{id}")
	@ApiOperation(value = "电表集中器查询档案")
	public Result<Boolean> queryFile(@PathVariable("id") Long id) {
		return success(concentratorService.queryFile(id));
	}
}
