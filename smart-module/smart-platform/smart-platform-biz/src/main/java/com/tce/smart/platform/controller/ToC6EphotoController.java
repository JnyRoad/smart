package com.tce.smart.platform.controller;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.SearchToC6DTO;
import com.tce.smart.platform.core.entity.SmtBlackVisitor;
import com.tce.smart.platform.core.entity.ToC6Ephoto;
import com.tce.smart.platform.service.ToC6EphotoService;

/**
 * 供c6同步员工头像
 * @author QIPEI
 *  2019/11/08
 */
@RestController
@RequestMapping("/toC6")
public class ToC6EphotoController extends BaseController {


	@Autowired
	private ToC6EphotoService  toC6EphotoService;


	@PostMapping("/add")
	public Result save(@RequestBody ToC6Ephoto toC6Ephoto) {
		return toC6EphotoService.saveToC6(toC6Ephoto);
	}

	/**
	 * 查询c6同步员工头像
	 * @param page
	 * @param toC6Ephoto
	 * @return
	 */
	@GetMapping("/page")
	public Result page(Page page,SearchToC6DTO searchToC6DTO) {
		return new Result<>(toC6EphotoService.searchPage(page,searchToC6DTO));
	}
}
