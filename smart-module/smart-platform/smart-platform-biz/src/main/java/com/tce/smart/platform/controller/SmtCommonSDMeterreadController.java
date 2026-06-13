package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.sddto.SaveCommonSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SearchCommonSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.CommonSDMeterreadRespDTO;
import com.tce.smart.platform.service.settlement.SmtCommonSDMeterreadService;
import com.tce.smart.platform.service.settlement.SmtCommonSDMeterreadService;
import com.tce.smart.platform.service.settlement.SmtCommonSDService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 公摊水电管理
 * @date: 2020/10/9 15:41
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "公摊水电抄表管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/commonsd/meterread")
public class SmtCommonSDMeterreadController {

	private final SmtCommonSDMeterreadService smtCommonSDMeterreadService;

	private final SmtCommonSDService smtCommonSDService;

	@ApiOperation("按收费项目查询所有公摊水电表抄表记录")
	@GetMapping("/catePage")
	public Result<IPage<CommonSDMeterreadRespDTO>> getCommonSDMeterreadHisByCate(SearchCommonSDMeterreadReqDTO searchCommonSDMeterreadReqDTO) {
		return new Result<>(smtCommonSDMeterreadService.getCommonSDMeterreadHisByCate(searchCommonSDMeterreadReqDTO,smtCommonSDService));
	}

	@ApiOperation("按公摊水电表标识查询所有抄表记录")
	@GetMapping("/page")
	public Result<IPage<CommonSDMeterreadRespDTO>> getCommonSDMeterreadHis(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam("current") final long current,
																		   @ApiParam(name = "size",value = "大小",required = true) @RequestParam("size") final long size,
																		  @ApiParam(name = "commId",value = "记录ID",required = true) @RequestParam("commId") Long commId) {
		return new Result<>(smtCommonSDMeterreadService.getCommonSDMeterreadHis(new Page(current,size),commId));
	}

	@ApiOperation("按公摊水电表标识和抄表月份查询抄表记录")
	@GetMapping("/query")
	public Result<CommonSDMeterreadRespDTO> getCommonSDMeterread(SearchCommonSDMeterreadReqDTO searchCommonSDMeterreadReqDTO) {
		return new Result<>(smtCommonSDMeterreadService.getCommonSDMeterread(searchCommonSDMeterreadReqDTO));
	}

	@ApiOperation("保存公摊水电抄表数据")
	@PostMapping("/save")
	public Result<Boolean> saveCommonSDMeterread(@RequestBody SaveCommonSDMeterreadReqDTO saveCommonSDMeterreadReqDTO) {
		return new Result<>(smtCommonSDMeterreadService.saveCommonSDMeterread(saveCommonSDMeterreadReqDTO));
	}

	@ApiOperation("删除公摊水电抄表数据")
	@PostMapping("/del/{id}")
	public Result<Boolean> delCommonSDMeterread(@ApiParam(name = "id",value = "记录ID",required = true) @PathVariable Long id) {
		return new Result<>(smtCommonSDMeterreadService.delCommonSDMeterread(id));
	}
}
