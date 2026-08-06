package com.tce.smart.common.security.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * 仅为显式内部服务调用签发 client_credentials 令牌的 Feign 拦截器。
 */
public class SmartInternalServiceTokenInterceptor extends OAuth2FeignRequestInterceptor implements RequestInterceptor {
	private static final String SERVER_SCOPE = "server";
	private final ClientCredentialsResourceDetails resource;

	public SmartInternalServiceTokenInterceptor(OAuth2ClientContext context,
			ClientCredentialsResourceDetails resource) {
		super(context, resource);
		this.resource = resource;
	}

	/**
	 * 令牌获取失败直接抛出异常；Feign 因此不会进入下游 HTTP 调用。
	 */
	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization");
		validateResource();
		OAuth2AccessToken accessToken = getToken();
		if (accessToken == null || !StringUtils.hasText(accessToken.getValue())) {
			throw new IllegalStateException("内部服务令牌为空，拒绝调用下游服务");
		}
		template.header("Authorization", "Bearer " + accessToken.getValue());
	}

	/**
	 * 配置不完整或资源属性被意外修改时拒绝调用，避免退化到全局用户资源或匿名请求。
	 */
	private void validateResource() {
		if (!"client_credentials".equals(resource.getGrantType())
				|| !Collections.singletonList(SERVER_SCOPE).equals(resource.getScope())
				|| !StringUtils.hasText(resource.getClientId())
				|| !StringUtils.hasText(resource.getClientSecret())
				|| !StringUtils.hasText(resource.getAccessTokenUri())) {
			throw new IllegalStateException("内部服务 client_credentials 配置不完整，拒绝调用下游服务");
		}
	}
}
