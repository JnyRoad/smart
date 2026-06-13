package com.tce.smart.platform.controller;

import cn.hutool.core.lang.Assert;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.ResetSdDetailReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadDetailService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @description: SmtSdMeterreadDetailController
 * @date: 2020-07-13 15:54
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "房间水电抄表明细管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/meterreaddetail")
public class SmtSdMeterreadDetailController {

	private final SmtSdMeterreadDetailService smtSdMeterreadDetailService;

	private final SmtSdMeterreadService smtSdMeterreadService;

	/**
	 * 添加房间水电抄表详细
	 * @param sdMeterreadDetailReqDTO 抄表详细数据
	 * @return Result
	 */
	@ApiOperation("添加房间水电抄表详细")
	@PostMapping("/add")
	public Result<Boolean> save(@RequestBody SdMeterreadDetailReqDTO sdMeterreadDetailReqDTO) {
		return new Result<>(smtSdMeterreadDetailService.saveMeterReadDetail(sdMeterreadDetailReqDTO,smtSdMeterreadService));
	}

	/**
	 * 查询房间水电抄表详细
	 * @param mrId 抄表记录Id
	 * @return Result
	 */
	@ApiOperation("查询房间水电抄表详细")
	@GetMapping("/query/{mrId}")
	public Result getMeterreadDetail(@PathVariable("mrId") Long mrId){
		Assert.notNull(mrId,"记录ID不能为NULL");
		return new Result<>(smtSdMeterreadDetailService.getMeterReadDetail(mrId));
	}

	/**
	 * 查询房间上月止度
	 * @param roomId 抄表记录Id
	 * @return Result
	 */
	@ApiOperation("查询房间上月止度")
	@GetMapping("/premonth/query")
	public Result getPreMonthDetail(@RequestParam Integer roomId,@RequestParam @DateTimeFormat(pattern = "yyyy-MM") Date meterMonth){
		Assert.notNull(roomId,"房间标识不能为NULL");
		Assert.notNull(meterMonth,"抄表月份不能为NULL");
		return new Result<>(smtSdMeterreadDetailService.getPreMonthDetail(roomId,meterMonth));
	}

	/**
	 * 重新抄表
	 * @param resetSdDetailReqDTO
	 * @return Result
	 */
	@ApiOperation("重新抄表")
	@PostMapping("/reset-sd-detail")
	public Result<Boolean> resetSdMeterDetail(@RequestBody ResetSdDetailReqDTO resetSdDetailReqDTO){
		return new Result<>(smtSdMeterreadDetailService.resetSdMeterDetail(resetSdDetailReqDTO,smtSdMeterreadService));
	}
}
