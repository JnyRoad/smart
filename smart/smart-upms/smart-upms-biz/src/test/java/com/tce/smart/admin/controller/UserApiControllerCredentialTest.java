package com.tce.smart.admin.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.ListAppender;
import com.tce.smart.admin.api.dto.SmtStaffDTO;
import com.tce.smart.admin.api.dto.PersonnelAuthSourceDTO;
import com.tce.smart.admin.api.dto.UserCredentialDTO;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.api.feign.RemoteStaffService;
import com.tce.smart.admin.mapper.SysUserMapper;
import com.tce.smart.admin.service.client.ClientEmployeeCredentialAdapter;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.admin.service.impl.SysUserServiceImpl;
import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserApiControllerCredentialTest {

	private static final String USERNAME = "test-user";
	private static final String INVALID_PASSWORD = "Credential8X";
	private static final String VALID_PASSWORD = "ValidCredential8X";
	private static final String MALFORMED_CREDENTIAL_MARKER = "SYNTHETICCREDENTIALMARKER";
	private static final String MALFORMED_JSON_MESSAGE = "请求参数格式错误";

	@Test
	public void jsonPostBindsCredentialBody() throws Exception {
		SysUserService userService = mock(SysUserService.class);
		when(userService.authenticate(USERNAME, VALID_PASSWORD)).thenReturn(Boolean.TRUE);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userService)).build();

		mockMvc.perform(post("/api/user/simple")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + USERNAME + "\",\"password\":\"" + VALID_PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	public void appSessionUsesSeparateInternalRouteWithoutChangingLegacySimple() throws Exception {
		SysUserService userService = mock(SysUserService.class);
		when(userService.authenticateAppSession(USERNAME, VALID_PASSWORD)).thenReturn(Boolean.TRUE);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userService)).build();

		mockMvc.perform(post("/api/user/session")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + USERNAME + "\",\"password\":\"" + VALID_PASSWORD + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value(true));
		verify(userService).authenticateAppSession(USERNAME, VALID_PASSWORD);
		verify(userService, never()).authenticate(USERNAME, VALID_PASSWORD);
	}

	@Test
	public void jsonPostRejectsMissingPassword() throws Exception {
		SysUserService userService = mock(SysUserService.class);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userService)).build();

		mockMvc.perform(post("/api/user/simple")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + USERNAME + "\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void jsonPostRejectsFormEncodedCredentials() throws Exception {
		SysUserService userService = mock(SysUserService.class);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userService)).build();

		mockMvc.perform(post("/api/user/simple")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", USERNAME)
				.param("password", VALID_PASSWORD))
				.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	public void malformedCredentialJsonUsesLocalGenericBadRequestWithoutLoggingBody() throws Exception {
		SysUserService userService = mock(SysUserService.class);
		Logger globalAdviceLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandlerResolver.class);
		Logger controllerLogger = (Logger) LoggerFactory.getLogger(UserApiController.class);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		globalAdviceLogger.addAppender(appender);
		controllerLogger.addAppender(appender);
		try {
			MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userService))
					.setControllerAdvice(new GlobalExceptionHandlerResolver())
					.build();

			MvcResult result = mockMvc.perform(post("/api/user/simple")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"username\":\"" + USERNAME + "\",\"password\":"
							+ MALFORMED_CREDENTIAL_MARKER + "}"))
					.andReturn();

			assertEquals(400, result.getResponse().getStatus());
			assertFalse(result.getResponse().getContentAsString().contains(MALFORMED_CREDENTIAL_MARKER));
			assertTrue(result.getResponse().getContentAsString().contains(MALFORMED_JSON_MESSAGE));
			assertFalse(appender.list.stream().anyMatch(this::containsMalformedCredentialMarker));
		} finally {
			globalAdviceLogger.detachAppender(appender);
			controllerLogger.detachAppender(appender);
			appender.stop();
		}
	}

	@Test
	public void legacyGetStillUsesQueryParameters() throws Exception {
		SysUserService userService = mock(SysUserService.class);
		when(userService.simpleLogin(USERNAME, VALID_PASSWORD)).thenReturn(Boolean.TRUE);
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserApiController(userService)).build();

		mockMvc.perform(get("/api/user/simple")
				.param("username", USERNAME)
				.param("password", VALID_PASSWORD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value(true));
	}

	@Test
	public void jsonPostKeepsInnerProtection() throws Exception {
		Method method = UserApiController.class.getMethod("authenticate", UserCredentialDTO.class);

		assertNotNull(method.getAnnotation(Inner.class));
	}

	@Test
	public void jsonAuthenticationRejectsInvalidCredentialDespiteLegacyCacheHit() throws Exception {
		Fixture fixture = fixtureWithLegacyCacheHit();
		UserApiController controller = new UserApiController(fixture.userService);
		UserCredentialDTO credential = credential();
		Method method = Arrays.stream(UserApiController.class.getMethods())
				.filter(candidate -> candidate.getName().equals("authenticate"))
				.filter(candidate -> Arrays.equals(candidate.getParameterTypes(),
						new Class<?>[]{UserCredentialDTO.class}))
				.findFirst()
				.orElse(null);
		assertNotNull(method);

		try {
			method.invoke(controller, credential);
			fail("不匹配的显式凭据必须被拒绝");
		} catch (InvocationTargetException expected) {
			assertTrue(expected.getCause() instanceof TCEException);
		}
		verify(fixture.cacheManager, never()).getCache("user_details");
	}

	@Test
	public void legacySimpleLoginKeepsCacheHitCompatibility() {
		Fixture fixture = fixtureWithLegacyCacheHit();

		assertTrue(fixture.userService.simpleLogin(USERNAME, INVALID_PASSWORD));
		verify(fixture.cacheManager).getCache("user_details");
	}

	@Test
	public void explicitAuthenticationAcceptsMatchingStoredHashForExistingTempUser() {
		Fixture fixture = fixtureWithExistingUser(4, false, VALID_PASSWORD);

		assertTrue(fixture.userService.authenticate(USERNAME, VALID_PASSWORD));
		verify(fixture.remoteStaffService).inintLoginAuth(USERNAME);
	}

	@Test
	public void explicitAuthenticationRejectsWrongStoredHashForExistingTempUserBeforeSideEffects() {
		Fixture fixture = fixtureWithExistingUser(4, false, VALID_PASSWORD);

		expectTceException(() -> fixture.userService.authenticate(USERNAME, INVALID_PASSWORD));

		verify(fixture.remoteStaffService, never()).inintLoginAuth(anyString());
		verify(fixture.userMapper, never()).insert(any(SysUser.class));
	}

	@Test
	public void explicitAuthenticationRejectsTestModeSubstituteWithoutStoredMatch() {
		Fixture fixture = fixtureWithExistingUser(1, true, VALID_PASSWORD);

		expectTceException(() -> fixture.userService.authenticate(USERNAME, INVALID_PASSWORD));

		verify(fixture.remoteStaffService, never()).inintLoginAuth(anyString());
	}

	@Test
	public void legacySimpleLoginKeepsTestModeProviderCompatibility() {
		Fixture fixture = fixtureWithExistingUser(1, true, VALID_PASSWORD);

		assertTrue(fixture.userService.simpleLogin(USERNAME, INVALID_PASSWORD));
		verify(fixture.remoteStaffService).inintLoginAuth(USERNAME);
	}

	@Test
	public void appSessionRoutesExternalAndDispatchedWorkersToSystemCredentialOnly() {
		Fixture fixture = fixtureWithExistingUser(1, false, VALID_PASSWORD);
		when(fixture.remoteStaffService.getAppAuthSource(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(new PersonnelAuthSourceDTO("system")));

		assertTrue(fixture.userService.authenticateAppSession(USERNAME, VALID_PASSWORD));
		assertFalse(fixture.userService.authenticateAppSession(USERNAME, INVALID_PASSWORD));
		verify(fixture.employeeAdapter, never()).verify(anyString(), anyString());
	}

	@Test
	public void appSessionRoutesFormalEmployeesToDhrAdapterAndFailsClosedForUnknownSource() {
		Fixture fixture = fixtureWithExistingUser(1, false, VALID_PASSWORD);
		when(fixture.remoteStaffService.getAppAuthSource(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(new PersonnelAuthSourceDTO("dhr")));
		when(fixture.employeeAdapter.verify(USERNAME, VALID_PASSWORD)).thenReturn(true);

		assertTrue(fixture.userService.authenticateAppSession(USERNAME, VALID_PASSWORD));
		verify(fixture.employeeAdapter).verify(USERNAME, VALID_PASSWORD);
		when(fixture.remoteStaffService.getAppAuthSource(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(new PersonnelAuthSourceDTO("unknown")));
		assertFalse(fixture.userService.authenticateAppSession(USERNAME, VALID_PASSWORD));
	}

	@SuppressWarnings("unchecked")
	private Fixture fixtureWithLegacyCacheHit() {
		return fixture(null, 4, true, true);
	}

	private Fixture fixtureWithExistingUser(int staffStatus, boolean isTest, String storedPassword) {
		SysUser user = new SysUser();
		user.setUserId(1);
		user.setUsername(USERNAME);
		user.setPassword(new BCryptPasswordEncoder().encode(storedPassword));
		user.setDelFlag("0");
		user.setLockFlag("0");
		return fixture(user, staffStatus, isTest, false);
	}

	@SuppressWarnings("unchecked")
	private Fixture fixture(SysUser existingUser, int staffStatus, boolean isTest, boolean cacheHit) {
		SysUserServiceImpl userService = new SysUserServiceImpl();
		CacheManager cacheManager = mock(CacheManager.class);
		Cache cache = mock(Cache.class);
		when(cacheManager.getCache("user_details")).thenReturn(cache);
		when(cache.get(USERNAME)).thenReturn(cacheHit ? new SimpleValueWrapper(new Object()) : null);

		SmtStaffDTO staff = new SmtStaffDTO();
		staff.setStatus(staffStatus);
		staff.setCertno("000000999999");
		staff.setPhone("00000000000");
		RemoteStaffService remoteStaffService = mock(RemoteStaffService.class);
		when(remoteStaffService.getSimpleSttaffByBadge(USERNAME)).thenReturn(Result.success(staff));
		when(remoteStaffService.inintLoginAuth(USERNAME)).thenReturn(Result.success(Boolean.TRUE));

		SysUserMapper userMapper = mock(SysUserMapper.class);
		when(userMapper.selectOne(any())).thenReturn(existingUser);
		ClientEmployeeCredentialAdapter employeeAdapter = mock(ClientEmployeeCredentialAdapter.class);

		ReflectionTestUtils.setField(userService, "cacheManager", cacheManager);
		ReflectionTestUtils.setField(userService, "remoteStaffService", remoteStaffService);
		ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
		ReflectionTestUtils.setField(userService, "clientEmployeeCredentialAdapter", employeeAdapter);
		ReflectionTestUtils.setField(userService, "loginUrl", "http://localhost/internal-login");
		ReflectionTestUtils.setField(userService, "loginToken", "test-token");
		ReflectionTestUtils.setField(userService, "isTest", isTest);
		return new Fixture(userService, cacheManager, remoteStaffService, userMapper, employeeAdapter);
	}

	private boolean containsMalformedCredentialMarker(ILoggingEvent event) {
		if (event.getFormattedMessage().contains(MALFORMED_CREDENTIAL_MARKER)) {
			return true;
		}
		return event.getThrowableProxy() != null
				&& ThrowableProxyUtil.asString(event.getThrowableProxy())
				.contains(MALFORMED_CREDENTIAL_MARKER);
	}

	private void expectTceException(ThrowingRunnable action) {
		try {
			action.run();
			fail("应拒绝未验证的显式凭据");
		} catch (TCEException expected) {
			// 预期拒绝。
		} catch (Exception unexpected) {
			throw new AssertionError(unexpected);
		}
	}

	private UserCredentialDTO credential() {
		UserCredentialDTO credential = new UserCredentialDTO();
		credential.setUsername(USERNAME);
		credential.setPassword(INVALID_PASSWORD);
		return credential;
	}

	private static class Fixture {
		private final SysUserServiceImpl userService;
		private final CacheManager cacheManager;
		private final RemoteStaffService remoteStaffService;
		private final SysUserMapper userMapper;
		private final ClientEmployeeCredentialAdapter employeeAdapter;

		private Fixture(SysUserServiceImpl userService, CacheManager cacheManager,
				RemoteStaffService remoteStaffService, SysUserMapper userMapper, ClientEmployeeCredentialAdapter employeeAdapter) {
			this.userService = userService;
			this.cacheManager = cacheManager;
			this.remoteStaffService = remoteStaffService;
			this.userMapper = userMapper;
			this.employeeAdapter = employeeAdapter;
		}
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
