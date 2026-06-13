package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.MeterreadConfigReqDTO;
import com.tce.smart.platform.api.dto.resp.MeterreadConfigRespDTO;
import com.tce.smart.platform.service.settlement.SmtMeterreadCnfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 水电结算日配置
 * @author QIPEI
 *
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-水电结算日配置")
@RequestMapping("/config/meterread")
public class SmtMeterreadConfigController extends BaseController {


	@Autowired
	private SmtMeterreadCnfigService smtMeterreadCnfigService;

	@ApiOperation("根据园区id获得配置详情")
	@GetMapping("/details/{parkId}")
	public Result<MeterreadConfigRespDTO> getByParkId(@PathVariable Integer parkId) {
		return success(smtMeterreadCnfigService.getByParkId(parkId), MeterreadConfigRespDTO.class);
	}

	@ApiOperation("编辑配置")
	@PostMapping("/edit")
	public Result<Boolean> editConfig(@RequestBody MeterreadConfigReqDTO reqDTO){
		return success(smtMeterreadCnfigService.editConfig(reqDTO));
	}

}
