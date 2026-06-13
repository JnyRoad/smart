package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AddDormitoryAdministratorReqDTO;
import com.tce.smart.platform.api.dto.req.DormitoryAdministratorReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryAdministratorRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryAdministrator;
import com.tce.smart.platform.service.SmtDormitoryAdministratorService;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @Title: SmtDormitoryAdministratorController
 * @Descripition: 宿舍管理员设置
 * @Auther: guohongtai
 * @Date: 2020-10-14 15:26
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/administrator")
public class SmtDormitoryAdministratorController extends BaseController {
	private final SmtDormitoryAdministratorService smtDormitoryAdministratorService;

	@PostMapping("/page")
	public Result getPage(Page page, @RequestBody DormitoryAdministratorReqDTO reqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return success(smtDormitoryAdministratorService.page(page, Wrappers.<SmtDormitoryAdministrator>query().lambda()
				.eq(Objects.nonNull(reqDTO.getParkId()), SmtDormitoryAdministrator::getParkId, reqDTO.getParkId())
				.in(CollectionUtils.isNotEmpty(parkIds), SmtDormitoryAdministrator::getParkId, parkIds)), SmtDormitoryAdministratorRespDTO.class);
	}

	@PostMapping("/save")
	public Result save(@RequestBody AddDormitoryAdministratorReqDTO reqDTO){
		return success(smtDormitoryAdministratorService.saveDormitoryAdministrator(reqDTO));
	}

	@GetMapping("/query/{id}")
	public Result getById(@PathVariable("id") Integer Id){
		return success(smtDormitoryAdministratorService.getByParkId(Id));
	}
}