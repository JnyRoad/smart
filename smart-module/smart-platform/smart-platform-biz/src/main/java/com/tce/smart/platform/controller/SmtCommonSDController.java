package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.sddto.AddCommonSDReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SearchCommonSDRecordRespDTO;
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
@Api(tags = "公摊水电管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/commonsd")
public class SmtCommonSDController {

	private final SmtCommonSDService smtCommonSDService;

	@ApiOperation("公摊水电表分页记录")
	@GetMapping("/page")
	public Result<IPage<SearchCommonSDRecordRespDTO>> getCommonSDRecord(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
																		@ApiParam(name = "size",value = "大小",required = true) @RequestParam long size) {
		return new Result<>(smtCommonSDService.getCommonSDCategoryRecord(new Page(current,size),null));
	}

	@ApiOperation("公摊水电表按类型分页记录")
	@GetMapping("/category/page")
	public Result<IPage<SearchCommonSDRecordRespDTO>> getCommonSDCategoryRecord(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
																		@ApiParam(name = "size",value = "大小",required = true) @RequestParam long size,
																				@ApiParam(name = "categoryId",value = "收费类型",required = true) @RequestParam int categoryId) {
		return new Result<>(smtCommonSDService.getCommonSDCategoryRecord(new Page(current,size),categoryId));
	}

	@ApiOperation("保存公摊水电表")
	@PostMapping("/save")
	public Result<Boolean> saveCommonSDRecord(@RequestBody AddCommonSDReqDTO addCommonSDReqDTO) {
		return new Result<>(smtCommonSDService.saveCommonSDRecord(addCommonSDReqDTO));
	}

	@ApiOperation("删除公摊水电表")
	@PostMapping("/del/{id}")
	public Result<Boolean> delCommonSDRecord(@ApiParam(name = "id",value = "记录ID",required = true) @PathVariable Long id) {
		return new Result<>(smtCommonSDService.delCommonSDRecord(id));
	}
}
