package com.tce.smart.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.api.vo.AppModuleSimpleInfoVo;
import com.tce.smart.app.dto.AppModuleDateDto;
import com.tce.smart.app.dto.AppServerDto;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.service.AppServeService;
import com.tce.smart.app.vo.AppServeVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;

import lombok.AllArgsConstructor;

/**
 * App服务管理
 *
 * @author ke.zhou
 * @date 2019-05-22 11:31:36
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appserve")
public class AppServeController extends BaseController {
	@Autowired
	private AppServeService appServeService;
	/**
	 *
	 * @return
	 */
	@GetMapping("/page")
	public Result getAll(){
		List<AppServeVo> list = appServeService.getAllSreve();
		return success(list);
	}

	/**
	 *
	 * @return
	 */
	@GetMapping("/pageServe")
	public Result getServe(){
		AppServeVo list = appServeService.getFix();
		return success(list);
	}

	/**
	 * 获取业务模块
	 * @return
	 */
	@GetMapping("/module/business/simple")
	public Result<List<AppModuleSimpleInfoVo>> getSimpleBusModule(){
		List<AppModuleInfo> moduleList = appServeService.getBusSimpleModule();
		return success(moduleList,AppModuleSimpleInfoVo.class);
	}

	/**
	 * 增加一级模块信息
	 * @param appModuleDateDto
	 * @return
	 */
	@SysLog("新增一级模块")
	@PostMapping("/add")
	public Result add(@RequestBody AppModuleDateDto appModuleDateDto){
		appServeService.addParent(appModuleDateDto.getModuleName());
		return success();
	}

	/**
	 * 根据ID删除协议信息
	 * @param ids
	 * @return
	 */
	@PostMapping("/delete")
	public Result deleteAgree(@RequestBody int[] ids){
		appServeService.deleteById(ids);
		return success();
	}

	/**
	 * 修改功能模块信息
	 * @param appServerDto
	 * @return
	 */
	@SysLog("修改功能模块信息")
	@PostMapping("/update")
	public Result update(@RequestBody AppServerDto appServerDto){
		appServeService.update(appServerDto.getUpdateData());
		return success();
	}

	/**
	 * 修改一级模块名
	 * @param appModuleDateDto
	 * @return
	 */
	@SysLog("修改功能模块信息")
	@PostMapping("/updatename")
	public Result updateName(@RequestBody AppModuleDateDto appModuleDateDto){
		appServeService.updateName(appModuleDateDto);
		return success();
	}

	/**
	 * 增加非业务二级模块信息
	 * @param appModuleDateDto
	 * @return
	 */
	@SysLog("添加模块")
	@PostMapping("/addmodule")
	public Result addModule(@RequestBody AppModuleDateDto appModuleDateDto){
		appServeService.addmodule(appModuleDateDto);
		return success();
	}

	/**
	 * 增加业务二级模块信息
	 * @param appModuleDateDto
	 * @return
	 */
	@SysLog("添加模块")
	@PostMapping("/addserver")
	public Result addserver(@RequestBody AppModuleDateDto appModuleDateDto){
		appServeService.addServeModule(appModuleDateDto);
		return success();
	}
}
