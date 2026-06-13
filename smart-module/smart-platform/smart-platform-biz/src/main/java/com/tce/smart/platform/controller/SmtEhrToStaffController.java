package com.tce.smart.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.core.dto.SearchEhrToStaffDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaff;
import com.tce.smart.platform.service.SmtEhrToStaffService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/ehr/to/staff")
public class SmtEhrToStaffController extends BaseController {

	@Autowired
	private SmtEhrToStaffService smtEhrToStaffService;

	@GetMapping("/page")
	public Result getParkPage(Page page, SearchEhrToStaffDTO searchEhrToStaffDTO) {
		return new Result<>(smtEhrToStaffService.page(page,searchEhrToStaffDTO));
	}


}
