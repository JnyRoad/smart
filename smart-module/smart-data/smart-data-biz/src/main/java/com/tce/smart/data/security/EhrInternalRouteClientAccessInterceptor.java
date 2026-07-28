package com.tce.smart.data.security;

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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SmartData 的内部服务路由只允许 Nacos 明确列出的 OAuth client_credentials 调用。
 * <p>
 * {@code @OpenApi("server")} 只负责确认 token scope，不能表达“哪个服务可以读取哪类 EHR 数据”。
 * 该守卫叠加 {@code from=Y}、纯服务 token 与按 HandlerMethod 配置的精确 client_id 白名单；
 * 缺失路由映射或空白名单均按拒绝处理。
 */
@Component
public class EhrInternalRouteClientAccessInterceptor implements HandlerInterceptor {

	private static final String SERVER_SCOPE = "server";

	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;
	private final EhrInternalRouteClientAccessProperties properties;

	public EhrInternalRouteClientAccessInterceptor(OpenApiAuthenticationAdapter openApiAuthenticationAdapter,
			EhrInternalRouteClientAccessProperties properties) {
		this.openApiAuthenticationAdapter = openApiAuthenticationAdapter;
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!(handler instanceof HandlerMethod)
				|| !requiresExactClientGuard(((HandlerMethod) handler).getMethod())) {
			return true;
		}

		Method routeMethod = ((HandlerMethod) handler).getMethod();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Set<String> allowedClientIds = allowedClientIds(routeMethod);
		boolean allowed = SecurityConstants.FROM_IN.equals(request.getHeader(SecurityConstants.FROM))
				&& !allowedClientIds.isEmpty()
				&& authentication != null
				&& openApiAuthenticationAdapter.isClientOnly(authentication)
				&& allowedClientIds.contains(openApiAuthenticationAdapter.clientId(authentication));
		if (allowed) {
			return true;
		}

		try {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "ehr internal client access denied");
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		return false;
	}

	/** 返回当前 HandlerMethod 的白名单；未知路由不能复用其他路由的调用方权限。 */
	private Set<String> allowedClientIds(Method routeMethod) {
		Map<String, String> routeClientIds = properties.getRouteClientIds();
		if (routeClientIds == null) {
			return Collections.emptySet();
		}
		return parseClientIds(routeClientIds.get(routeKey(routeMethod)));
	}

	/**
	 * 使用 Controller 全限定类名和方法名作为稳定的 Nacos 映射键，避免 URI 前缀变化扩大授权。
	 * 同一 Controller 中存在多个受守卫同名方法时，补充擦除后的参数类型，避免重载路由复用一份授权。
	 */
	String routeKey(Method routeMethod) {
		String routeKey = routeMethod.getDeclaringClass().getName() + "#" + routeMethod.getName();
		if (!hasExactClientGuardedOverload(routeMethod)) {
			return routeKey;
		}
		return routeKey + Arrays.stream(routeMethod.getParameterTypes())
				.map(Class::getName)
				.collect(Collectors.joining(",", "(", ")"));
	}

	/** 仅在多个受守卫重载会竞争同一配置键时增加方法签名，兼容既有的非重载 Nacos 键。 */
	private boolean hasExactClientGuardedOverload(Method routeMethod) {
		for (Method candidate : routeMethod.getDeclaringClass().getDeclaredMethods()) {
			if (!routeMethod.equals(candidate) && routeMethod.getName().equals(candidate.getName())
					&& requiresExactClientGuard(candidate)) {
				return true;
			}
		}
		return false;
	}

	/** 只有真正标记为内部 server API 的 Controller 方法才走本守卫，普通用户接口不受影响。 */
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
