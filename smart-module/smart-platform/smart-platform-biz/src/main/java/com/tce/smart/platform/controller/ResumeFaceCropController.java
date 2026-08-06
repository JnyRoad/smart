package com.tce.smart.platform.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.service.ResumeFaceCropCapabilityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 公开简历登记的人脸裁剪入口。
 *
 * 该入口不接受管理员权限替代 capability；必须先完成身份证资料保存，
 * 并携带该步骤签发的一次性 HttpOnly Cookie，才能将图片转交内部算法服务。
 */
@RestController
@RequestMapping("/regist/face")
@Api(tags = "platform-简历人脸处理")
public class ResumeFaceCropController extends BaseController {

	private static final int MAX_IMAGE_DATA_LENGTH = 8 * 1024 * 1024;

	private final RemoteAlgorithmService remoteAlgorithmService;
	private final ResumeFaceCropCapabilityService capabilityService;

	public ResumeFaceCropController(RemoteAlgorithmService remoteAlgorithmService,
			ResumeFaceCropCapabilityService capabilityService) {
		this.remoteAlgorithmService = remoteAlgorithmService;
		this.capabilityService = capabilityService;
	}

	/** 裁剪简历人脸并签发只能保存当前裁剪结果的下一步 capability。 */
	@ApiOperation("裁剪简历人脸")
	@PostMapping("/crop")
	public Result<String> crop(@CookieValue(value = "resume_face_crop", required = false) String capability,
			@RequestBody FaceImgCutReq request, HttpServletResponse response) {
		if (request == null || StrUtil.isBlank(request.getImageData())) {
			throw new TCEException("人脸图片不能为空");
		}
		if (request.getImageData().length() > MAX_IMAGE_DATA_LENGTH) {
			throw new TCEException("人脸图片过大");
		}
		Long applicationId = capabilityService.consumeCropCapability(capability);
		FaceImgCutReq internalRequest = new FaceImgCutReq();
		internalRequest.setSerialNo(IdUtil.fastSimpleUUID());
		internalRequest.setImageData(request.getImageData());
		String croppedImage = remoteAlgorithmService.cutFace(internalRequest, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).data();
		capabilityService.issueSaveCapability(response, applicationId, croppedImage);
		return success(croppedImage);
	}
}
