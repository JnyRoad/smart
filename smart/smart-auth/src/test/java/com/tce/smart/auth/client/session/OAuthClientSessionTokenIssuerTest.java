package com.tce.smart.auth.client.session;

import com.tce.smart.common.security.service.SmartUser;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 签发端仅使用服务端登记的password客户端，App请求里没有客户端密钥或grant参数。 */
public class OAuthClientSessionTokenIssuerTest {
	@Test public void serverRegisteredClientSignsMinimalSessionToken() {
		ClientSessionProperties properties = properties();
		ClientDetailsService clients = mock(ClientDetailsService.class);
		ClientDetails client = mock(ClientDetails.class);
		when(client.getClientId()).thenReturn("smart-app-app");
		when(client.getAuthorizedGrantTypes()).thenReturn(Collections.singleton("password"));
		when(client.getAuthorities()).thenReturn(Collections.emptyList());
		when(client.getScope()).thenReturn(null);
		when(clients.loadClientByClientId("smart-app-app")).thenReturn(client);
		AuthorizationServerTokenServices tokens = mock(AuthorizationServerTokenServices.class);
		DefaultOAuth2AccessToken access = new DefaultOAuth2AccessToken("server-token");
		access.setExpiration(new Date(1770000000000L));
		when(tokens.createAccessToken(any(OAuth2Authentication.class))).thenReturn(access);

		ClientSessionToken issued = new OAuthClientSessionTokenIssuer(properties, clients, tokens).issue(user());
		Assert.assertEquals("server-token", issued.getValue());
		Assert.assertEquals(1770000000000L, issued.getExpiresAt());
		ArgumentCaptor<OAuth2Authentication> authentication = ArgumentCaptor.forClass(OAuth2Authentication.class);
		verify(tokens).createAccessToken(authentication.capture());
		Assert.assertEquals("password", authentication.getValue().getOAuth2Request().getRequestParameters().get("grant_type"));
		Assert.assertEquals("smart-app-app", authentication.getValue().getOAuth2Request().getClientId());
	}

	@Test public void nonPasswordClientFailsClosedBeforeTokenIssuance() {
		ClientDetailsService clients = mock(ClientDetailsService.class);
		ClientDetails client = mock(ClientDetails.class);
		when(client.getAuthorizedGrantTypes()).thenReturn(Collections.singleton("client_credentials"));
		when(clients.loadClientByClientId("smart-app-app")).thenReturn(client);
		AuthorizationServerTokenServices tokens = mock(AuthorizationServerTokenServices.class);
		try { new OAuthClientSessionTokenIssuer(properties(), clients, tokens).issue(user()); Assert.fail("应拒绝不支持password的客户端"); }
		catch (ClientSessionException failure) { Assert.assertEquals(503, failure.getStatus()); }
		verifyZeroInteractions(tokens);
	}

	private static ClientSessionProperties properties() {
		ClientSessionProperties value = new ClientSessionProperties(); value.setEnabled(true); value.setClientId("smart-app-app"); return value;
	}
	private static SmartUser user() {
		return new SmartUser(10, 1, "E100", Collections.singletonList(1), "ignored", true, true, true,
				true, Arrays.asList(new SimpleGrantedAuthority("release:apply")));
	}
}
