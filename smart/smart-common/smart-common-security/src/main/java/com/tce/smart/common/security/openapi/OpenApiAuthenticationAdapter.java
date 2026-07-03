package com.tce.smart.common.security.openapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 开放 API 认证信息适配类。
 * <p>
 * 迁移缝（spec §6）：{@link OAuth2Authentication} 等 Spring Security OAuth 私有类型
 * 只允许出现在本类，拦截器 / 注解 / 自动配置一律不得直接 import，
 * 后续升级或替换 OAuth 实现时只需改动本类。
 */
@Slf4j
public class OpenApiAuthenticationAdapter {

	private static final String APP_PARK_IDS_CLAIM = "app_park_ids";

	/**
	 * 是否为纯客户端凭证（client_credentials）认证，即请求方是应用本身而非某个用户。
	 */
	public boolean isClientOnly(Authentication authentication) {
		return authentication instanceof OAuth2Authentication && ((OAuth2Authentication) authentication).isClientOnly();
	}

	/**
	 * 取客户端 ID（client_id）。仅在 {@link #isClientOnly(Authentication)} 为 true 时调用有意义。
	 */
	public String clientId(Authentication authentication) {
		return ((OAuth2Authentication) authentication).getOAuth2Request().getClientId();
	}

	/**
	 * 取 token 的 scope 集合，用于和 {@link com.tce.smart.common.security.annotation.OpenApi#value()} 比对。
	 */
	public Set<String> scopes(Authentication authentication) {
		return ((OAuth2Authentication) authentication).getOAuth2Request().getScope();
	}

	/**
	 * 取 token 携带的园区绑定 claim（app_park_ids），供业务侧做数据范围过滤。
	 * <p>
	 * 防御性解析（Task 2 遗留问题）：token 增强环节（{@code AuthorizationServerConfig#buildTokenEnhancer}）
	 * 把 client 的 {@code allowedParkIds} 原样透传进 claim，未做类型校验；
	 * 资源服务侧经 {@code DefaultAccessTokenConverter} 反序列化后可能拿到非 List 或元素非 Integer 的脏数据。
	 * 这里一律做防御性解析：非法数据按空列表处理并 WARN 日志，不抛类型转换异常——
	 * 空列表语义等价于"拒绝一切园区数据"，是最安全的降级方向。
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> appParkIds(Authentication authentication) {
		Object value = ((OAuth2Authentication) authentication).getOAuth2Request().getExtensions().get(APP_PARK_IDS_CLAIM);
		if (value == null) {
			return Collections.emptyList();
		}
		if (!(value instanceof List)) {
			log.warn("open-api-audit app_park_ids claim 类型异常，期望 List，实际={}，按空列表处理", value.getClass());
			return Collections.emptyList();
		}
		List<?> rawList = (List<?>) value;
		for (Object element : rawList) {
			if (!(element instanceof Integer)) {
				log.warn("open-api-audit app_park_ids claim 元素类型异常，期望 Integer，实际元素={}，按空列表处理", element);
				return Collections.emptyList();
			}
		}
		return (List<Integer>) rawList;
	}
}
