package com.tce.smart.common.security.internal;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bridge 系列的内部设备路由只允许 Nacos 明确列出的 OAuth client_credentials 调用。
 * <p>
 * {@code @OpenApi("server")} 仅校验令牌 scope，不能区分任意内部服务与 Dispatcher。
 * 本守卫叠加 {@code from=Y}、纯服务令牌和精确 client_id 白名单；空白名单按拒绝处理。
 */
@Component
public class InternalServerRouteClientAccessInterceptor implements HandlerInterceptor {

	private static final String SERVER_SCOPE = "server";

	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;
	private final InternalServerRouteClientAccessProperties properties;

	public InternalServerRouteClientAccessInterceptor(OpenApiAuthenticationAdapter openApiAuthenticationAdapter,
			InternalServerRouteClientAccessProperties properties) {
		this.openApiAuthenticationAdapter = openApiAuthenticationAdapter;
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!(handler instanceof HandlerMethod)
				|| !requiresExactClientGuard(((HandlerMethod) handler).getMethod())) {
			return true;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Set<String> allowedClientIds = parseClientIds(properties.getAllowedClientIds());
		boolean allowed = SecurityConstants.FROM_IN.equals(request.getHeader(SecurityConstants.FROM))
				&& !allowedClientIds.isEmpty()
				&& authentication != null
				&& openApiAuthenticationAdapter.isClientOnly(authentication)
				&& allowedClientIds.contains(openApiAuthenticationAdapter.clientId(authentication));
		if (allowed) {
			return true;
		}

		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "internal server client access denied");
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		return false;
	}

	/** 仅为同时标注内部与 server scope 的路由追加精确调用方限制。 */
	boolean requiresExactClientGuard(Method method) {
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		return method.isAnnotationPresent(Inner.class) && openApi != null && SERVER_SCOPE.equals(openApi.value());
	}

	private Set<String> parseClientIds(String configuredClientIds) {
		if (configuredClientIds == null || configuredClientIds.trim().isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> clientIds = new HashSet<>();
		for (String clientId : Arrays.asList(configuredClientIds.split(","))) {
			if (clientId != null && !clientId.trim().isEmpty()) {
				clientIds.add(clientId.trim());
			}
		}
		return Collections.unmodifiableSet(clientIds);
	}
}
