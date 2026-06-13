package com.tce.smart.platform.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.AddSocialSecurityReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;
import com.tce.smart.platform.core.dto.SearchStaffDTO;
import com.tce.smart.platform.core.entity.SmtSocialSecurity;
import com.tce.smart.platform.service.SmtSocialSecurityService;

import lombok.AllArgsConstructor;

/**
 * 社保控制台
 * @author 齐佩
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/social/security")
public class SmtSocialSecurityController extends BaseController {

	private final SmtSocialSecurityService smtSocialSecurityService;

	/**
	 * 分页查询设备列表
	 * @param page
	 * @param smtSocialSecurity
	 * @return
	 */
	@GetMapping("/page")
	public Result<IPage<SmtSocialSecurity>> getSmtSocialSecurityPage(Page page, SmtSocialSecurity smtSocialSecurity) {
		return new Result<>(smtSocialSecurityService.getSmtSocialSecurityPage(page,smtSocialSecurity));
	}





	/**
	 * 查看社保详情
	 * @return
	 */
	@GetMapping("/detail/{id}")
	public Result<SearchSocialSecurityRespDTO> detailById(@PathVariable("id") String id) {
		return new Result<>(smtSocialSecurityService.detailById(id));
	}

	/**
	 * 删除社保
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public Result<Boolean> deleteById(@PathVariable("id") String id) {
		return new Result<>(smtSocialSecurityService.removeById(id));
	}

	/**
	 * 添加社保
	 * @param addSocialSecurityReqDTO
	 * @return
	 */
	@PostMapping("/add")
	public Result<Boolean> save(@RequestBody AddSocialSecurityReqDTO addSocialSecurityReqDTO) {
		return new Result<>(smtSocialSecurityService.save(addSocialSecurityReqDTO));
	}

	/*
	 * 修改社保
	 */
	@PostMapping("/update")
	public Result<Boolean> update(@RequestBody AddSocialSecurityReqDTO addSocialSecurityReqDTO) {
		return new Result<>(smtSocialSecurityService.update(addSocialSecurityReqDTO));
	}


	/**
	 * app接口  查询列表
	 * @param page
	 * @param smtSocialSecurity
	 * @return
	 */
	@GetMapping("/list")
	public Result<List<SearchSocialSecurityRespDTO>> getSmtSocialSecurityList() {
		return new Result<>(smtSocialSecurityService.getSmtSocialSecurityList());
	}
}
