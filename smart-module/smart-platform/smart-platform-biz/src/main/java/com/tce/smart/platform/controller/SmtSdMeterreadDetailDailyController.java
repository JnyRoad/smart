package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.dailySd.DailyMeterQueryDTO;
import com.tce.smart.platform.api.dto.resp.dailySd.DailyMeterRespDTO;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadDetailDailyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: SmtSdMeterreadDetailController
 * @date: 2020-07-13 15:54
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "platform-房间水电日结算")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/meterread/daily")
public class SmtSdMeterreadDetailDailyController {

	private final SmtSdMeterreadDetailDailyService smtSdMeterreadDetailDailyService;

	@ApiOperation("日结算查询")
	@GetMapping("/byFloor/new")
	public Result<List<DailyMeterRespDTO>> getDailyCount(DailyMeterQueryDTO queryDTO){
		return new Result<>(smtSdMeterreadDetailDailyService.getFloorSdMeterReadNew(queryDTO));
	}

	@Inner
	@ApiOperation("每日水电结算")
	@PostMapping("/gen")
	public Result genDailyRecord() {
		smtSdMeterreadDetailDailyService.genDailyRecord();
		return Result.success(Boolean.TRUE);
	}

}
