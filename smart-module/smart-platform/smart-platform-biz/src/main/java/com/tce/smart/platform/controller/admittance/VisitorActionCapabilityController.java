package com.tce.smart.platform.controller.admittance;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.api.dto.req.admittance.VisitorActionCapabilityConsumeReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorActionCapabilityReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorActionCapabilityRespDTO;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访客匿名草稿派生的受限动作 capability。
 *
 * 浏览器只允许用草稿会话签发 upload/blacklist 两类一次性能力；真正消费必须由
 * Smart App 持服务客户端令牌发起，避免把 Platform 内部消费端点暴露给外网。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admittance/visitor-action")
public class VisitorActionCapabilityController extends BaseController {
	private static final String DRAFT_TOKEN_HEADER = "X-Visitor-Draft-Token";

	private final VisitorFaceCropCapabilityService capabilityService;
	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/** 只有受管 Smart App service client 可消费 capability；空配置必须拒绝。 */
	@Value("${security.inner.visitor-action.app-client-id:}")
	private String appServiceClientId;

	/** 公开入口只签发与草稿绑定的受限动作，裁剪结果上传票据只能由 crop 服务端派生。 */
	@PostMapping("/capability")
	public Result<VisitorActionCapabilityRespDTO> issue(
			@RequestHeader(value = DRAFT_TOKEN_HEADER, required = false) String draftToken,
			@RequestBody VisitorActionCapabilityReqDTO request) {
		if (StrUtil.isBlank(draftToken) || request == null || StrUtil.isBlank(request.getDraftId())
				|| !isBrowserIssuableAction(request.getAction())) {
			throw expired();
		}
		VisitorActionCapabilityRespDTO response = new VisitorActionCapabilityRespDTO();
		try {
			response.setCapability(capabilityService.issueActionCapability(draftToken, request.getDraftId(), request.getAction(),
					request.getPayloadHash()));
		} catch (SmartException exception) {
			throw expired();
		}
		log.info("访客动作 capability 已签发 action={}", request.getAction());
		return success(response);
	}

	/** 仅 App 服务端可原子消费；外部浏览器不得直连本接口。 */
	@Inner
	@OpenApi("server")
	@PostMapping("/internal/consume")
	public Result<Boolean> consume(@RequestBody VisitorActionCapabilityConsumeReqDTO request,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from) {
		assertAppServiceCaller(from);
		if (request == null || StrUtil.isBlank(request.getCapability()) || StrUtil.isBlank(request.getDraftId())
				|| !isConsumableAction(request.getAction())) {
			throw expired();
		}
		try {
			capabilityService.consumeActionCapability(request.getCapability(), request.getDraftId(), request.getAction(),
					request.getPayloadHash());
		} catch (SmartException exception) {
			throw expired();
		}
		log.info("访客动作 capability 已消费 action={} callerService={}", request.getAction(), callerService());
		return success(Boolean.TRUE);
	}

	/** 浏览器不可自行签发 FACE_UPLOAD，只有裁剪成功的服务端才可派生该票据。 */
	private boolean isBrowserIssuableAction(VisitorActionCapabilityAction action) {
		return action == VisitorActionCapabilityAction.DOCUMENT_UPLOAD
				|| action == VisitorActionCapabilityAction.BLACKLIST_CHECK
				|| action == VisitorActionCapabilityAction.RECEPTIONIST_SEARCH
				|| action == VisitorActionCapabilityAction.APPLY_PRECHECK
				|| action == VisitorActionCapabilityAction.APPLY_SUBMIT;
	}

	/** App 受管服务消费时还必须允许服务端裁剪流程派生的人脸上传票据。 */
	private boolean isConsumableAction(VisitorActionCapabilityAction action) {
		return action == VisitorActionCapabilityAction.FACE_UPLOAD
				|| action == VisitorActionCapabilityAction.DOCUMENT_UPLOAD
				|| action == VisitorActionCapabilityAction.BLACKLIST_CHECK;
	}

	private void assertAppServiceCaller(String from) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || StrUtil.isBlank(appServiceClientId) || authentication == null
				|| !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !appServiceClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("访客动作内部调用未获授权");
		}
	}

	private String callerService() {
		Authentication authentication = SecurityUtils.getAuthentication();
		return authentication == null ? "unknown" : openApiAuthenticationAdapter.clientId(authentication);
	}

	private AccessDeniedException expired() {
		return new AccessDeniedException("访客操作授权已失效，请重新进入申请流程");
	}
}
