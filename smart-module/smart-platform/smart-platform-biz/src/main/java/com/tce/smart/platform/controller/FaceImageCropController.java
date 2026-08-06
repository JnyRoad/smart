package com.tce.smart.platform.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台人脸图片裁剪代理。
 *
 * 浏览器只允许通过已认证的平台服务提交人脸图片，算法服务仅接受服务令牌调用，
 * 防止人脸原图直接发送至外部匿名算法域。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/face")
@Api(tags = "platform-人脸图片处理")
public class FaceImageCropController extends BaseController {

	/** 浏览器提交的 Base64 图片上限，避免公共管理端点被超大请求耗尽内存。 */
	private static final int MAX_IMAGE_DATA_LENGTH = 8 * 1024 * 1024;

	private final RemoteAlgorithmService remoteAlgorithmService;

	/**
	 * 将后台管理人员已选择的人脸图片交由内部算法服务裁剪。
	 * 服务端重新生成追踪号，调用方不能伪造内部处理请求的关联信息。
	 */
	@ApiOperation("裁剪人脸图片")
	@PostMapping("/crop")
	@PreAuthorize("@pms.hasPermission('platform_staff_manage')")
	public Result<String> crop(@RequestBody FaceImgCutReq request) {
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
}
