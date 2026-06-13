package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.ao.ParkOrgSetSaveAO;
import com.tce.smart.platform.core.vo.ParkOrgSetEditVo;
import com.tce.smart.platform.service.SmtParkOrgSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.tce.smart.common.core.model.Result.success;

/***
 * description: 园区组织关系设置控制器 <br>
 * date: 2019/11/22 8:21 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/orgset")
@Api(tags = "园区组织关系设置")
public class SmtParkOrgSetController {

	private final SmtParkOrgSetService smtParkOrgSetService;


	/**
	 * 通过id查询园区组织关系
	 *
	 * @param parkId 园区ID
	 * @return Result
	 */
	@GetMapping("/view/{parkId}")
	@ApiOperation("通过id查询园区组织关系")
	public Result<ParkOrgSetEditVo> viewParkOrg(@PathVariable("parkId") Integer parkId) {
		return success(smtParkOrgSetService.viewParkOrg(parkId));
	}

	/**
	 * 新增、修改园区组织关系
	 *
	 * @param parkOrgSetSaveAO 园区组织信息保存Ao
	 * @return Result
	 */
	@SysLog("新增、修改园区组织关系")
	@PostMapping
	@ApiOperation("新增、修改园区组织关系")
	public Result<Boolean> save(@RequestBody ParkOrgSetSaveAO parkOrgSetSaveAO) {
		return success(smtParkOrgSetService.saveParkOrg(parkOrgSetSaveAO));
	}

}
