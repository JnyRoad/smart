package com.tce.smart.app.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.service.fore.VisitorService;
import com.tce.smart.app.vo.wechat.PhotoVisitorVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 已登录员工图片上传入口。
 *
 * 宿舍等登录态 H5 不再复用匿名访客上传路径；复用既有存图业务前先明确校验员工主体，
 * 本路径不得出现在 Nacos ignore-urls 中。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/employee/photo")
public class EmployeePhotoUploadController extends BaseController {
	private final VisitorService visitorService;

	@PostMapping("/upload")
	public Result<PhotoVisitorVo> upload(@RequestBody CheckFaceAo request) {
		requireAuthenticatedEmployee();
		return success(visitorService.checkFace(request, null, null));
	}

	private void requireAuthenticatedEmployee() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("未认证用户不可上传员工图片");
		}
		SmartUser user = SecurityUtils.getUser(authentication);
		if (user == null || StrUtil.isBlank(user.getUsername())) {
			throw new AccessDeniedException("未认证用户不可上传员工图片");
		}
	}
}
