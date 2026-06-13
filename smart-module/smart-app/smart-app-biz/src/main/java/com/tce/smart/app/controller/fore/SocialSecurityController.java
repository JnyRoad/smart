package com.tce.smart.app.controller.fore;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.service.fore.SocialSecurityService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;

/**
 *app 社保公积金
 * @author 齐佩
 *
 */
@RestController
@RequestMapping("/social/security")
public class SocialSecurityController  extends BaseController{

	@Autowired
	private SocialSecurityService socialSecurityService;

	/**
	 *  查询列表
	 * @param page
	 * @return
	 */
	@GetMapping("/list")
	public Result<List<SearchSocialSecurityRespDTO>> getSmtSocialSecurityList() {
		return socialSecurityService.getSmtSocialSecurityList();
	}
}
