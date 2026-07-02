package com.tce.smart.common.security.openapi;

import com.tce.smart.common.security.annotation.OpenApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 开放 API 统一裁决拦截器：业务服务零配置生效（由 {@code SmartResourceServerAutoConfiguration} 全局注册）。
 * <p>
 * 裁决规则（单一真相，与 spec §3.2 一致）：
 * <ol>
 *     <li>handler 带 {@link OpenApi} → 必须是纯客户端 token 且 scope 含注解值，否则 403；</li>
 *     <li>handler 不带 {@link OpenApi} 但请求方是纯客户端 token → 403（deny-by-default，客户端 token 不得访问未显式开放的接口）；</li>
 *     <li>其余（用户 token 调普通接口）→ 放行，走既有权限体系；</li>
 *     <li>每次裁决都输出结构化审计日志。</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class OpenApiInterceptor implements HandlerInterceptor {

	private final OpenApiAuthenticationAdapter adapter;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// 非 Controller 方法（如静态资源）直接放行，不参与本裁决
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}

		long startTime = System.currentTimeMillis();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		OpenApi openApi = handlerMethod.getMethodAnnotation(OpenApi.class);
		boolean clientOnly = authentication != null && adapter.isClientOnly(authentication);
		// clientId 仅在纯客户端 token 场景下才有意义，其余场景（用户 token / 匿名）不强行取值，避免类型转换异常
		String clientId = clientOnly ? adapter.clientId(authentication) : null;

		String result = "DENY";
		try {
			if (openApi != null) {
				// 规则1：标注了 @OpenApi 的接口，必须是纯客户端 token 且 scope 命中
				if (!clientOnly || !adapter.scopes(authentication).contains(openApi.value())) {
					throw new AccessDeniedException("open api access denied: missing client token or required scope [" + openApi.value() + "]");
				}
				result = "ALLOW";
				return true;
			}

			// 规则2：deny-by-default——纯客户端 token 不允许访问未显式标注 @OpenApi 的接口
			if (clientOnly) {
				throw new AccessDeniedException("client token denied on non-open-api endpoint");
			}

			// 规则3：其余场景（用户 token 调普通接口）放行，交由既有权限体系继续校验
			result = "ALLOW";
			return true;
		} finally {
			long costMs = System.currentTimeMillis() - startTime;
			log.info("open-api-audit clientId={} uri={} result={} costMs={} ip={}",
					clientId, request.getRequestURI(), result, costMs, request.getRemoteAddr());
		}
	}
}
