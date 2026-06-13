package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.isc.EditIscStaffCardReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscStaffCardRespDTO;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
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
@RequestMapping("/isc/staff/card")
public class SmtIscStaffCardController extends BaseController {

	private final SmtIscStaffCardService smtIscStaffCardService;

	@GetMapping("/list")
	public Result list(@RequestParam("staffId") Long staffId) {
		return success(smtIscStaffCardService.listStaffCards(staffId), IscStaffCardRespDTO.class);
	}

	@SysLog("编辑员工ISC实体卡")
	@PostMapping("/edit")
	public Result edit(@Valid @RequestBody EditIscStaffCardReqDTO reqDTO) {
		return success(smtIscStaffCardService.saveStaffCard(reqDTO));
	}

	@SysLog("删除员工ISC实体卡")
	@PostMapping("/{id}")
	public Result remove(@PathVariable Long id) {
		return success(smtIscStaffCardService.removeStaffCard(id));
	}
}
