package com.tce.smart.common.security.component;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源服务端 access token 反序列化扩展：在 {@link DefaultAccessTokenConverter} 默认转换结果基础上，
 * 把 /oauth/check_token 返回 map 中的 {@code app_park_ids} claim 原样搬进 {@link OAuth2Request#getExtensions()}，
 * 供 {@code OpenApiAuthenticationAdapter#appParkIds(org.springframework.security.core.Authentication)} 读取。
 * <p>
 * 本类只负责"搬运数据"这一件事，不做任何业务裁决（scope 比对、client-only 判断等裁决逻辑
 * 统一收敛在 {@code OpenApiAuthenticationAdapter}，避免散落）。
 */
public class OpenApiAccessTokenConverter extends DefaultAccessTokenConverter {

	private static final String APP_PARK_IDS_CLAIM = "app_park_ids";

	@Override
	public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
		OAuth2Authentication authentication = super.extractAuthentication(map);
		if (!map.containsKey(APP_PARK_IDS_CLAIM)) {
			return authentication;
		}

		OAuth2Request original = authentication.getOAuth2Request();
		Map<String, Serializable> extensions = new HashMap<>(original.getExtensions());
		// map 中的值来自 /oauth/check_token 的 JSON 反序列化结果，类型未必是 Serializable 子类
		// （理论上应为 ArrayList<Integer>，防御性解析交给 OpenApiAuthenticationAdapter 统一处理，这里只负责原样透传）
		Object appParkIds = map.get(APP_PARK_IDS_CLAIM);
		if (appParkIds instanceof Serializable) {
			extensions.put(APP_PARK_IDS_CLAIM, (Serializable) appParkIds);
		}

		OAuth2Request enrichedRequest = new OAuth2Request(
				original.getRequestParameters(), original.getClientId(), original.getAuthorities(),
				original.isApproved(), original.getScope(), original.getResourceIds(),
				original.getRedirectUri(), original.getResponseTypes(), extensions);
		return new OAuth2Authentication(enrichedRequest, authentication.getUserAuthentication());
	}
}
