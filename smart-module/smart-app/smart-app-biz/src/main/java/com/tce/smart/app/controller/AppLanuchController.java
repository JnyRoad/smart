package com.tce.smart.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.app.service.AppContentPictureService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;

import lombok.AllArgsConstructor;

/**
 * App启动引导控制器
 *
 * @author mingkai.wu
 * @date 2019-05-12 17:57:31
 */
@RestController
@AllArgsConstructor
@RequestMapping("/applanuch")
public class AppLanuchController extends BaseController {

	private final AppContentPictureService appContentPictureService;

	/**
	 * 新增引导页
	 *
	 * @param appPictureDto 引导页信息
	 * @return Result
	 */
	@SysLog("新增引导页`")
	@PostMapping("/uplodBoot")
	public Result<?> saveBootPage(@RequestBody AppPictureDto appPictureDto) {
		return new Result<>(appContentPictureService.addBootPage(appPictureDto));
	}

}
