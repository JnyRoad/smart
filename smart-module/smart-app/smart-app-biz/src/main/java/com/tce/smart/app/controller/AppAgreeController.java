package com.tce.smart.app.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppAgreeDto;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppAgreeService;
import com.tce.smart.app.service.fore.ParkService;
import com.tce.smart.app.vo.AppAgreeVo;
import com.tce.smart.app.vo.AppCheckVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.feign.RemoteParkService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/appagree")
public class AppAgreeController extends BaseController{
	@Autowired
	private ParkService parkService;
	@Autowired
	private AppAgreeService appAgreeService;
	@Autowired
	private RemoteParkService remoteParkService;

	/**
	 * 添加协议信息
	 * @param appAgreeDto
	 * @return
	 */
	@PostMapping("/add")
    public Result addAgree(@RequestBody AppAgreeDto appAgreeDto){
		Integer id = appAgreeService.addAppAgree(appAgreeDto);
	return success(id);
	}

	/**
	 * 根据ID删除协议信息
	 * @param id
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public Result deleteAgree(@PathVariable("id")int id){
		appAgreeService.deleteAgree(id);
		return success();
	}

	/**
	 * 分页查询协议信息
	 * @return
	 */
	@GetMapping("/page")
	public Result agreePage(Page page, AppSubject appSubject){
		IPage<AppSubject> iPage = appAgreeService.getAppQuestionPage(page,appSubject);
	return success(iPage, AppAgreeVo.class);
	}

	/**
	 * 修改App协议信息
	 *
	 * @param appAgreeDto App协议信息
	 * @return Result
	 */
	@SysLog("修改App协议信息")
	@PostMapping("/update")
	public Result updateById(@RequestBody AppAgreeDto appAgreeDto) {
		appAgreeService.updateAppAgree(appAgreeDto);
		return success();
	}

	/**
	 *
	 * @return
	 */
	@GetMapping("/test")
	public  Result  test(){
		AppCheckVo appCheckVo = appAgreeService.getInitDate();
		return success(appCheckVo);
	}

	/**
	 * 根据ID查询协议信息
	 * @param id
	 * @return
	 */
	@GetMapping("/detail/{id}")
	public Result deAgree(@PathVariable("id")int id){
		AppSubject appSubject = appAgreeService.getById(id);
		return success(appSubject,AppAgreeVo.class);
	}

}
