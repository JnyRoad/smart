package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.service.SmtVisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 匿名访客预约的黑名单最小内部接口。
 *
 * App 先在外网入口消费一次性 capability，再用专属 client_credentials 调用本接口；
 * 这里不返回黑名单实体、身份证、姓名或备注，避免跨服务扩大个人信息暴露面。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/visitor-blacklist")
public class InternalVisitorBlacklistController extends BaseController {
	private static final String PURPOSE = "visitor-blacklist";

	private final SmtVisitorService smtVisitorService;
	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/** App 服务客户端必须由受管配置精确指定，未注入配置时拒绝访问。 */
	@Value("${security.inner.visitor-blacklist.app-client-id:}")
	private String appServiceClientId;

	@Inner
	@OpenApi("server")
	@PostMapping("/visitor")
	public Result<Boolean> checkVisitor(@RequestBody SmtVisitorDTO request,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		return success(smtVisitorService.checkBlackVisitor(toVisitor(request)));
	}

	@Inner
	@OpenApi("server")
	@PostMapping("/vehicle")
	public Result<Boolean> checkVehicle(@RequestBody SmtVisitorDTO request,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		return success(smtVisitorService.checkBlackVehicle(toVisitor(request)));
	}

	/** 不信任跨服务 DTO 的其他字段，仅复制两种黑名单校验的必要输入。 */
	private SmtVisitor toVisitor(SmtVisitorDTO request) {
		if (request == null) {
			throw new AccessDeniedException("访客黑名单校验参数不完整");
		}
		SmtVisitor visitor = new SmtVisitor();
		visitor.setCertNo(normalizeCertNo(request.getCertNo()));
		visitor.setVehiclePlate(request.getVehiclePlate());
		visitor.setParkId(request.getParkId());
		return visitor;
	}

	/** 防御性重复规范化，避免任一内部调用方绕过 App 的 capability 输入规范。 */
	private String normalizeCertNo(String certNo) {
		return certNo == null ? null : certNo.replaceAll("\\s+", "").toUpperCase(java.util.Locale.ROOT);
	}

	/** server scope 不能替代调用方白名单和用途约束。 */
	private void assertAppCaller(String from, String purpose) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || !PURPOSE.equals(purpose) || StrUtil.isBlank(appServiceClientId)
				|| authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !appServiceClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("访客黑名单内部调用未获授权");
		}
	}
}
