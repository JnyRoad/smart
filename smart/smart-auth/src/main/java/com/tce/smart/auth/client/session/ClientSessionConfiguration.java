package com.tce.smart.auth.client.session;

import com.tce.smart.common.security.service.SmartUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

/** 仅在部署显式启用并配置独立 OAuth 客户端时创建 App 会话签发链。 */
@Configuration
@ConditionalOnProperty(prefix = "smart.client.session", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ClientSessionProperties.class)
public class ClientSessionConfiguration {
	@Bean public ClientSessionTokenIssuer clientSessionTokenIssuer(ClientSessionProperties properties,
			@Qualifier("smartClientDetailsService") ClientDetailsService clients, AuthorizationServerTokenServices tokenServices) {
		return new OAuthClientSessionTokenIssuer(properties, clients, tokenServices);
	}

	@Bean public ClientSessionService clientSessionService(SmartUserDetailsService users,
			ClientSessionTokenIssuer issuer) {
		return new ClientSessionService(users, issuer);
	}
}
