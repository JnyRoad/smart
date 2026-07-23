package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.core.dto.AddEhrToStaffSettingDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaffSetting;
import com.tce.smart.platform.service.SmtEhrToStaffSettingService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/ehr/to/staff/set")
public class SmtEhrToStaffSettingController extends BaseController {

	@Autowired
	private SmtEhrToStaffSettingService service;

	@GetMapping("/list")
	public Result<List<SmtEhrToStaffSetting>> getList() {
		return new Result<>(service.list());
	}


	@PostMapping("/addList")
	public Result addList(@RequestBody AddEhrToStaffSettingDTO dto ) {
		return new Result<>(service.addList(dto));
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/list-ehr")
	public Result<List<SmtEhrToStaffSetting>> getListEHR() {
		return new Result<>(service.getListEHR());
	}


	@PostMapping("/addList-ehr")
	public Result addListEHR(@RequestBody AddEhrToStaffSettingDTO dto ) {
		return new Result<>(service.addListEHR(dto));
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/list-dhr")
	public Result<List<SmtEhrToStaffSetting>> getListDHR() {
		return new Result<>(service.getListDHR());
	}


	@PostMapping("/addList-dhr")
	public Result addListDHR(@RequestBody AddEhrToStaffSettingDTO dto ) {
		return new Result<>(service.addListDHR(dto));
	}

}
