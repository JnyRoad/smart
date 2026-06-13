package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.isc.EditIscParkConfigReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscParkConfigRespDTO;
import com.tce.smart.platform.core.service.SmtIscParkConfigService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
@RequestMapping("/isc/park/config")
public class SmtIscParkConfigController extends BaseController {

	private final SmtIscParkConfigService smtIscParkConfigService;

	@GetMapping("/page")
	public Result getPage(Page page, @RequestParam(value = "parkId", required = false) Integer parkId) {
		return success(smtIscParkConfigService.getPage(page, parkId), IscParkConfigRespDTO.class);
	}

	@GetMapping("/by/parkId")
	public Result getByParkId(@RequestParam("parkId") Integer parkId) {
		return success(smtIscParkConfigService.getConfigByPark(parkId), IscParkConfigRespDTO.class);
	}

	@GetMapping("/by/id")
	public Result getById(@RequestParam("id") Long id) {
		return success(smtIscParkConfigService.getActiveConfigById(id), IscParkConfigRespDTO.class);
	}

	@SysLog("编辑ISC平台绑定配置")
	@PostMapping("/edit")
	public Result edit(@Valid @RequestBody EditIscParkConfigReqDTO reqDTO) {
		return success(smtIscParkConfigService.editConfig(reqDTO));
	}

	@SysLog("删除ISC平台绑定配置")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Long id) {
		return success(smtIscParkConfigService.removeConfigById(id));
	}
}
