package com.tce.smart.common.security.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.Collections;

/**
 * 创建内部服务专用 OAuth 资源，避免客户端凭据流程继承全局用户 OAuth 配置。
 */
public class SmartInternalServiceTokenResourceFactory {
	private static final String RESOURCE_ID = "internal-service";
	private static final String SERVER_SCOPE = "server";

	/**
	 * 固定授权类型和最小 scope，配置只提供客户端身份和令牌端点。
	 */
	public ClientCredentialsResourceDetails create(SmartInternalServiceTokenProperties properties) {
		ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
		resource.setId(RESOURCE_ID);
		resource.setClientId(properties.getClientId());
		resource.setClientSecret(properties.getClientSecret());
		resource.setAccessTokenUri(properties.getAccessTokenUri());
		resource.setGrantType(SecurityConstants.CLIENT_CREDENTIALS);
		resource.setScope(Collections.singletonList(SERVER_SCOPE));
		return resource;
	}
}
