package com.tce.smart.app.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 已登录员工的人脸裁剪入口。
 *
 * 该接口不接收工号、人员 ID 或图片 URL；只处理当前认证主体提交的图片，并通过服务令牌
 * 调用内部算法服务，因此不能借此读取或处理其他员工的档案图片。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/employee/face")
@Api(tags = "app-员工人脸图片处理")
public class EmployeeFaceCropController extends BaseController {
	private static final int MAX_IMAGE_DATA_LENGTH = 8 * 1024 * 1024;

	private final RemoteAlgorithmService remoteAlgorithmService;

	@ApiOperation("裁剪当前登录员工提交的人脸图片")
	@PostMapping("/crop")
	public Result<String> crop(@RequestBody FaceImgCutReq request) {
		requireAuthenticatedSubject();
		if (request == null || StrUtil.isBlank(request.getImageData())) {
			throw new TCEException("人脸图片不能为空");
		}
		if (request.getImageData().length() > MAX_IMAGE_DATA_LENGTH) {
			throw new TCEException("人脸图片过大");
		}
		FaceImgCutReq internalRequest = new FaceImgCutReq();
		internalRequest.setSerialNo(IdUtil.fastSimpleUUID());
		internalRequest.setImageData(request.getImageData());
		return success(remoteAlgorithmService.cutFace(internalRequest, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).data());
	}

	/**
	 * 除网关资源服务器外，控制器也明确检查认证主体，防止误将该路由加入 ignore-urls 后静默降级。
	 */
	private void requireAuthenticatedSubject() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("未认证用户不可裁剪员工人脸图片");
		}
		SmartUser user = SecurityUtils.getUser(authentication);
		if (user == null || StrUtil.isBlank(user.getUsername())) {
			throw new AccessDeniedException("未认证用户不可裁剪员工人脸图片");
		}
	}
}
