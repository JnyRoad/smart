package com.tce.smart.auth.client.session;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.service.SmartUserDetailsService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/** App 会话服务不触发旧 Servlet 参数登录，失败时也不把认证异常暴露给调用方。 */
public class ClientSessionServiceTest {
	@Test
	public void explicitCredentialsIssueMinimalTokenResponse() {
		SmartUserDetailsService users = Mockito.mock(SmartUserDetailsService.class);
		ClientSessionTokenIssuer issuer = Mockito.mock(ClientSessionTokenIssuer.class);
		SmartUser subject = user();
		Mockito.when(users.authenticate("E100", "pass-1")).thenReturn(subject);
		Mockito.when(issuer.issue(subject)).thenReturn(new ClientSessionToken("access-token", 1770000000000L));

		Map<String, Object> response = new ClientSessionService(users, issuer).login(" E100 ", "pass-1");
		Assert.assertEquals("access-token", response.get("token"));
		Assert.assertEquals(1770000000000L, response.get("expiresAt"));
		Assert.assertEquals(2, response.size());
		Mockito.verify(users).authenticate("E100", "pass-1");
	}

	@Test
	public void invalidOrRejectedCredentialsNeverReachIssuer() {
		SmartUserDetailsService users = Mockito.mock(SmartUserDetailsService.class);
		ClientSessionTokenIssuer issuer = Mockito.mock(ClientSessionTokenIssuer.class);
		Mockito.when(users.authenticate(Mockito.eq("E100"), Mockito.anyString()))
				.thenThrow(new BadCredentialsException("invalid credentials"));
		ClientSessionService service = new ClientSessionService(users, issuer);
		expectStatus(400, () -> service.login("\nE100", "pass-1"));
		expectStatus(401, () -> service.login("E100", "wrong"));
		Mockito.verifyZeroInteractions(issuer);
	}

	@Test
	public void unavailableIdentityDependencyReturnsServiceUnavailable() {
		SmartUserDetailsService users = Mockito.mock(SmartUserDetailsService.class);
		ClientSessionTokenIssuer issuer = Mockito.mock(ClientSessionTokenIssuer.class);
		Mockito.when(users.authenticate("E100", "pass-1"))
				.thenThrow(new AuthenticationServiceException("upstream unavailable"));

		expectStatus(503, () -> new ClientSessionService(users, issuer).login("E100", "pass-1"));
		Mockito.verifyZeroInteractions(issuer);
	}

	private static SmartUser user() {
		return new SmartUser(10, 1, "E100", Collections.singletonList(1), "ignored", true, true, true,
				true, Arrays.asList(new SimpleGrantedAuthority("release:apply")));
	}

	private static void expectStatus(int expected, Action action) {
		try { action.run(); Assert.fail("应被拒绝"); }
		catch (ClientSessionException failure) { Assert.assertEquals(expected, failure.getStatus()); }
	}
	private interface Action { void run(); }
}
