package com.tce.smart.app.controller;

import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.app.service.AppContentPictureService;
import com.tce.smart.app.vo.AppPictureVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/appguide")
public class AppGuideController  extends BaseController {
	@Autowired
	private AppContentPictureService appContentPictureService;

	/**
	 * 新增引导页
	 * @param appPictureDto 引导页信息
	 * @return Result
	 */
	@SysLog("新增主题信息")
	@PostMapping("/uplodBoot")
	public Result saveBootPage(@RequestBody AppPictureDto appPictureDto){
		Integer id = appContentPictureService.addBootPage(appPictureDto);
		return success(id);
	}

	/**
	 * 新增启动页
	 * @param appPictureDto 启动页信息
	 * @return Result
	 */
	@SysLog("新增主题信息")
	@PostMapping("/uplodStart")
	public Result saveStartPage(@RequestBody AppPictureDto appPictureDto){
		Integer id = appContentPictureService.addStartPage(appPictureDto);
		return success(id);
	}

	/**
	 * 新增引导页
	 * @param
	 * @return Result
	 */
	@SysLog("显示引导页")
	@GetMapping("/boot")
	public Result bootPage(){
		AppPictureVo list  = appContentPictureService.bootPage();
		return success(list);
	}

	/**
	 * 新增引导页
	 * @param
	 * @return Result
	 */
	@SysLog("显示启动页")
	@GetMapping("/start")
	public Result startPage(){
		AppPictureDto appPictureDto = appContentPictureService.startPage();
		return success(appPictureDto);
	}

	/**
	 * 更新引导启动页图片
	 * @param appPictureDto 启动页信息
	 * @return Result
	 */
	@SysLog("新增主题信息")
	@PostMapping("/update")
	public Result updatePage(@RequestBody AppPictureDto appPictureDto){
		appContentPictureService.updatePage(appPictureDto);
		return success();
	}
}
