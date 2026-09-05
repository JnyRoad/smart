package com.tce.smart.common.security.openapi;

import com.tce.smart.common.security.annotation.OpenApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
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
 * <p>
 * 拒绝路径为什么用 {@link HttpServletResponse#sendError(int, String)} 而不是抛
 * {@code org.springframework.security.access.AccessDeniedException}：
 * 本仓库的 {@code ExceptionTranslationFilter}（Spring Security 过滤器链阶段）够不着 MVC
 * 拦截器阶段抛出的异常——拦截器抛出的异常会沿 {@code DispatcherServlet} 走到
 * {@code GlobalExceptionHandlerResolver}，而该类里绑定 {@code @ExceptionHandler(AccessDeniedException.class)}
 * 的其实是 {@code java.nio.file.AccessDeniedException}（包名不同，永远不会匹配 Spring Security 的
 * {@code AccessDeniedException}），最终异常会落到兜底的 {@code Exception.class} handler，
 * 而该 handler 没有 {@code @ResponseStatus}，会返回 HTTP 200 而不是 403。
 * 因此这里不依赖任何异常翻译链，直接调用 {@code response.sendError(403, ...)} 是唯一可靠的路径。
 */
@Slf4j
public class OpenApiInterceptor implements HandlerInterceptor {

	private final OpenApiAuthenticationAdapter adapter;
	private final boolean allowDeprecatedCompatibilityScopes;

	/**
	 * 保持既有构造方式的二进制兼容；默认启用已废弃 scope 的迁移兼容，
	 * 具体服务可通过双参构造在完成迁移后显式关闭。
	 */
	public OpenApiInterceptor(OpenApiAuthenticationAdapter adapter) {
		this(adapter, true);
	}

	public OpenApiInterceptor(OpenApiAuthenticationAdapter adapter, boolean allowDeprecatedCompatibilityScopes) {
		this.adapter = adapter;
		this.allowDeprecatedCompatibilityScopes = allowDeprecatedCompatibilityScopes;
	}

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

		boolean allow;
		if (openApi != null) {
			// 规则1：标注了 @OpenApi 的接口，必须是纯客户端 token 且主 scope 或迁移兼容 scope 命中。
			// server 等主 scope 不受兼容开关影响；compatibilityScopes 只服务于有限迁移期。
			allow = clientOnly && hasRequiredScope(adapter.scopes(authentication), openApi);
		} else {
			// 规则2：deny-by-default——纯客户端 token 不允许访问未显式标注 @OpenApi 的接口
			// 规则3：其余场景（用户 token 调普通接口）放行，交由既有权限体系继续校验
			allow = !clientOnly;
		}

		long costMs = System.currentTimeMillis() - startTime;
		log.info("open-api-audit clientId={} uri={} result={} costMs={} ip={}",
				clientId, request.getRequestURI(), allow ? "ALLOW" : "DENY", costMs, request.getRemoteAddr());

		if (!allow) {
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "open api access denied");
			} catch (IOException e) {
				// sendError 本身写响应失败属于容器层异常，转成非受检异常向上抛，不吞掉
				throw new UncheckedIOException(e);
			}
			return false;
		}
		return true;
	}

	/**
	 * 主 scope 始终是长期授权边界；兼容 scope 必须先被目录标记为已废弃，才会作为迁移期
	 * 的精确额外候选项。不支持通配符、前缀或层级匹配，避免把历史细分授权泛化为通配权限。
	 */
	private boolean hasRequiredScope(Set<String> tokenScopes, OpenApi openApi) {
		if (tokenScopes == null || tokenScopes.isEmpty()) {
			return false;
		}
		if (tokenScopes.contains(openApi.value())) {
			return true;
		}
		for (String compatibilityScope : openApi.compatibilityScopes()) {
			if (allowDeprecatedCompatibilityScopes && OpenApiScopeCatalog.isDeprecated(compatibilityScope)
					&& tokenScopes.contains(compatibilityScope)) {
				return true;
			}
		}
		return false;
	}
}
