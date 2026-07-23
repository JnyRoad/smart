package com.tce.smart.platform.controller.admittance;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.admittance.VisitorFaceCropCapabilityReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorFaceCropReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorFaceCropCapabilityRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorFaceCropRespDTO;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 微信访客人脸裁剪入口。
 *
 * 网关可精确放行本路径，但控制器必须校验微信授权后签发的草稿会话与一次性能力；
 * 绝不接受工号、人员 ID 或图片 URL 作为身份或图片来源。
 */
@RestController
@AllArgsConstructor
@RequestMapping("/admittance/visitor-face")
@Api(tags = "platform-访客人脸图片处理")
public class VisitorFaceCropController extends BaseController {
	private static final int MAX_IMAGE_DATA_LENGTH = 8 * 1024 * 1024;
	private static final String DRAFT_TOKEN_HEADER = "X-Visitor-Draft-Token";
	private static final String CAPABILITY_HEADER = "X-Visitor-Face-Capability";

	private final VisitorFaceCropCapabilityService capabilityService;
	private final RemoteAlgorithmService remoteAlgorithmService;

	@ApiOperation("换取一次性访客人脸裁剪能力")
	@PostMapping("/capability")
	public Result<VisitorFaceCropCapabilityRespDTO> issueCapability(
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestBody VisitorFaceCropCapabilityReqDTO request) {
		if (request == null || StrUtil.isBlank(request.getDraftId())) {
			throw new TCEException("访客草稿无效");
		}
		VisitorFaceCropCapabilityRespDTO response = new VisitorFaceCropCapabilityRespDTO();
		response.setCapability(capabilityService.issueCropCapability(draftToken, request.getDraftId()));
		return success(response);
	}

	@ApiOperation("裁剪访客人脸图片")
	@PostMapping("/crop")
	public Result<VisitorFaceCropRespDTO> crop(
			@RequestHeader(value = CAPABILITY_HEADER, required = false) String capability,
			@RequestBody VisitorFaceCropReqDTO request) {
		if (request == null || StrUtil.isBlank(request.getDraftId()) || StrUtil.isBlank(request.getImageData())) {
			throw new TCEException("访客人脸图片不能为空");
		}
		if (request.getImageData().length() > MAX_IMAGE_DATA_LENGTH) {
			throw new TCEException("人脸图片过大");
		}
		capabilityService.consumeCropCapability(capability, request.getDraftId());
		FaceImgCutReq internalRequest = new FaceImgCutReq();
		internalRequest.setSerialNo(IdUtil.fastSimpleUUID());
		internalRequest.setImageData(request.getImageData());
		String croppedImage = remoteAlgorithmService.cutFace(internalRequest, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED).data();
		if (StrUtil.isBlank(croppedImage)) {
			throw new TCEException("访客人脸处理失败，请重新拍摄");
		}
		VisitorFaceCropRespDTO response = new VisitorFaceCropRespDTO();
		response.setImageData(croppedImage);
		// 只签发与算法真实返回图片摘要绑定的上传票据，不能拿去上传另一张任意图片。
		response.setUploadCapability(capabilityService.issueActionCapabilityForVerifiedDraft(request.getDraftId(),
				VisitorActionCapabilityAction.FACE_UPLOAD, sha256(croppedImage)));
		return success(response);
	}

	private String sha256(String imageData) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(imageData.getBytes(StandardCharsets.UTF_8));
			StringBuilder value = new StringBuilder(digest.length * 2);
			for (byte current : digest) {
				value.append(String.format("%02x", current));
			}
			return value.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new TCEException("访客人脸处理失败，请重新拍摄");
		}
	}
}
