package com.tce.smart.common.security.util;

import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SecurityUtilsTest {

	private static final String USERNAME = "test-user";
	private static final String COMPLIANT_PASSWORD = "Credential8X";
	private static final String NON_COMPLIANT_PASSWORD = "short";

	@After
	public void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void explicitCredentialsDoNotNeedServletContext() {
		RequestContextHolder.resetRequestAttributes();

		assertTrue(SecurityUtils.isStrongPwd(USERNAME, COMPLIANT_PASSWORD));
		assertFalse(SecurityUtils.isStrongPwd(USERNAME, NON_COMPLIANT_PASSWORD));
	}

	@Test
	public void legacyNonOauthRequestKeepsBypassSemantics() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/mobile/token/sms");
		request.setQueryString(SecurityConstants.PASSWORD + "=" + NON_COMPLIANT_PASSWORD);

		assertTrue(SecurityUtils.isStrongPwd(USERNAME, request));
	}

	@Test
	public void legacyOauthRequestStillChecksQueryCredential() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(SecurityConstants.OAUTH_TOKEN_URL);
		request.setQueryString(SecurityConstants.PASSWORD + "=" + NON_COMPLIANT_PASSWORD);

		assertFalse(SecurityUtils.isStrongPwd(USERNAME, request));
	}
}
