package com.tce.smart.auth.client.session;

import com.tce.smart.common.security.service.SmartUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 使用既有 TokenStore 和 TokenEnhancer 签发专属 App 会话，不走 URL 参数密码入口。 */
final class OAuthClientSessionTokenIssuer implements ClientSessionTokenIssuer {
	private final ClientSessionProperties properties;
	private final ClientDetailsService clientDetailsService;
	private final AuthorizationServerTokenServices tokenServices;

	OAuthClientSessionTokenIssuer(ClientSessionProperties properties, ClientDetailsService clientDetailsService,
			AuthorizationServerTokenServices tokenServices) {
		this.properties = properties;
		this.clientDetailsService = clientDetailsService;
		this.tokenServices = tokenServices;
	}

	@Override
	public ClientSessionToken issue(UserDetails subject) {
		if (!(subject instanceof SmartUser)) throw new ClientSessionException(401);
		properties.validate();
		try {
			ClientDetails client = clientDetailsService.loadClientByClientId(properties.getClientId());
			if (client == null || client.getAuthorizedGrantTypes() == null
					|| !client.getAuthorizedGrantTypes().contains("password")) throw new ClientSessionException(503);
			Map<String, String> parameters = new LinkedHashMap<>();
			parameters.put("grant_type", "password");
			OAuth2Request request = new OAuth2Request(parameters, client.getClientId(), client.getAuthorities(),
					true, client.getScope() == null ? Collections.<String>emptySet() : client.getScope(), Collections.<String>emptySet(), null,
					Collections.<String>emptySet(), Collections.<String, Serializable>emptyMap());
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					subject, null, subject.getAuthorities());
			OAuth2AccessToken token = tokenServices.createAccessToken(new OAuth2Authentication(request, authentication));
			if (token == null || token.getValue() == null || token.getValue().trim().isEmpty()
					|| token.getExpiration() == null) throw new ClientSessionException(503);
			return new ClientSessionToken(token.getValue(), token.getExpiration().getTime());
		} catch (ClientSessionException failure) {
			throw failure;
		} catch (Exception failure) {
			throw new ClientSessionException(503);
		}
	}
}
