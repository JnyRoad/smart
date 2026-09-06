package com.tce.smart.common.security.service;

import com.tce.smart.admin.api.dto.UserCredentialDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.admin.api.feign.RemoteUserService;
import com.tce.smart.common.core.constant.AuthConstants;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.exception.NotStrongPasswordException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class SmartUserDetailsServiceImplTest {

	private static final String USERNAME = "test-user";
	private static final String PASSWORD = "Credential8X";
	private static final String OTHER_PASSWORD = "Different8X";

	private SmartUserDetailsServiceImpl service;
	private RemoteUserService remoteUserService;
	private RemoteDictService remoteDictService;
	private CacheManager cacheManager;
	private Cache cache;
	private ValueOperations<String, String> valueOperations;

	@Before
	public void setUp() {
		service = new SmartUserDetailsServiceImpl();
		remoteUserService = mock(RemoteUserService.class);
		remoteDictService = mock(RemoteDictService.class);
		cacheManager = mock(CacheManager.class);
		cache = mock(Cache.class);
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		valueOperations = mock(ValueOperations.class);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(null);
		when(remoteDictService.findByType(SecurityConstants.SYS_DEFAULT_USER, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(Collections.emptyList()));
		when(remoteUserService.info(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(userInfo(CommonConstants.STATUS_NORMAL,
						new BCryptPasswordEncoder().encode(PASSWORD))));
		when(remoteUserService.listUserPark(anyInt(), eq(SecurityConstants.FROM_IN)))
				.thenReturn(Result.success(Collections.emptyList()));

		ReflectionTestUtils.setField(service, "remoteUserService", remoteUserService);
		ReflectionTestUtils.setField(service, "remoteDictService", remoteDictService);
		ReflectionTestUtils.setField(service, "cacheManager", cacheManager);
		ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
		RequestContextHolder.resetRequestAttributes();
	}

	@After
	public void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void explicitAuthenticationRejectsBlankCredentialsBeforeRemoteCalls() {
		expectException(BadCredentialsException.class, () -> service.authenticate(" ", PASSWORD));
		expectException(BadCredentialsException.class, () -> service.authenticate(USERNAME, " "));

		verify(remoteDictService, never()).findByType(anyString(), anyString());
	}

	@Test
	public void explicitAuthenticationRejectsRedisLockedAccount() {
		when(valueOperations.get(AuthConstants.REDIS_KEY_PREFIX + USERNAME))
				.thenReturn(String.valueOf(AuthConstants.MAX_LOGIN_ATTEMPTS));

		expectException(TCEException.class, () -> service.authenticate(USERNAME, PASSWORD));

		verify(remoteDictService, never()).findByType(anyString(), anyString());
	}

	@Test
	public void platformAccountAuthenticatesWithoutServletContext() {
		markAsPlatformAccount();

		UserDetails details = service.authenticate(USERNAME, PASSWORD);

		assertEquals(USERNAME, details.getUsername());
		verify(remoteUserService, never()).authenticateAppSession(any(UserCredentialDTO.class), anyString());
		verify(cacheManager, never()).getCache(anyString());
	}

	@Test
	public void platformAccountRejectsWrongPasswordDespiteLegacyCache() {
		markAsPlatformAccount();
		when(cacheManager.getCache("user_details")).thenReturn(cache);
		when(cache.get(USERNAME)).thenReturn(() -> activeSmartUser());

		expectException(BadCredentialsException.class, () -> service.authenticate(USERNAME, OTHER_PASSWORD));

		verify(cacheManager, never()).getCache(anyString());
	}

	@Test
	public void employeeAccountRequiresSuccessfulTruePostResult() {
		when(remoteUserService.authenticateAppSession(any(UserCredentialDTO.class), eq(SecurityConstants.FROM_IN)))
				.thenAnswer(invocation -> {
					UserCredentialDTO credential = invocation.getArgument(0);
					boolean matches = USERNAME.equals(credential.getUsername())
							&& PASSWORD.equals(credential.getPassword());
					return Result.success(matches);
				});

		UserDetails details = service.authenticate(USERNAME, PASSWORD);

		assertEquals(USERNAME, details.getUsername());
		verify(remoteUserService, never()).simpleLogin(anyString(), anyString(), anyString());
		verify(cacheManager, never()).getCache(anyString());
	}

	@Test
	public void employeeAccountDoesNotRecheckStoredHashAfterDhrAdapterPassesCredential() {
		mockEmployeeSuccess(PASSWORD);
		when(remoteUserService.info(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(userInfo(CommonConstants.STATUS_NORMAL,
						new BCryptPasswordEncoder().encode(OTHER_PASSWORD))));

		UserDetails details = service.authenticate(USERNAME, PASSWORD);
		assertEquals(USERNAME, details.getUsername());

		verify(cacheManager, never()).getCache(anyString());
	}

	@Test
	public void employeeAccountRejectsSuccessfulFalseResult() {
		when(remoteUserService.authenticateAppSession(any(UserCredentialDTO.class), eq(SecurityConstants.FROM_IN)))
				.thenReturn(Result.success(Boolean.FALSE));

		expectException(BadCredentialsException.class, () -> service.authenticate(USERNAME, PASSWORD));
	}

	@Test
	public void employeeAccountRejectsSuccessfulNullResult() {
		when(remoteUserService.authenticateAppSession(any(UserCredentialDTO.class), eq(SecurityConstants.FROM_IN)))
				.thenReturn(Result.success(null));

		expectException(BadCredentialsException.class, () -> service.authenticate(USERNAME, PASSWORD));
	}

	@Test
	public void employeeAccountRejectsFailedRemoteResultWithGenericMessage() {
		Result<Boolean> failed = new Result<>();
		failed.setCode(CommonConstants.FAIL);
		failed.setMsg("upstream-" + PASSWORD);
		when(remoteUserService.authenticateAppSession(any(UserCredentialDTO.class), eq(SecurityConstants.FROM_IN)))
				.thenReturn(failed);

		BadCredentialsException failure = expectException(BadCredentialsException.class,
				() -> service.authenticate(USERNAME, PASSWORD));

		assertFalse(failure.getMessage().contains(PASSWORD));
		assertFalse(failure.getMessage().contains("upstream"));
	}

	@Test
	public void employeeAccountRejectsRemoteExceptionWithGenericMessage() {
		when(remoteUserService.authenticateAppSession(any(UserCredentialDTO.class), eq(SecurityConstants.FROM_IN)))
				.thenThrow(new IllegalStateException("upstream-" + PASSWORD));

		BadCredentialsException failure = expectException(BadCredentialsException.class,
				() -> service.authenticate(USERNAME, PASSWORD));

		assertFalse(failure.getMessage().contains(PASSWORD));
		assertFalse(failure.getMessage().contains("upstream"));
	}

	@Test
	public void explicitAuthenticationRejectsDisabledAccount() {
		mockEmployeeSuccess(PASSWORD);
		when(remoteUserService.info(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(userInfo(CommonConstants.STATUS_DEL,
						new BCryptPasswordEncoder().encode(PASSWORD))));

		expectException(DisabledException.class, () -> service.authenticate(USERNAME, PASSWORD));
	}

	@Test
	public void explicitAuthenticationRejectsLockedAccountStatus() {
		mockEmployeeSuccess(PASSWORD);
		when(remoteUserService.info(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(userInfo(CommonConstants.STATUS_LOCK,
						new BCryptPasswordEncoder().encode(PASSWORD))));

		expectException(LockedException.class, () -> service.authenticate(USERNAME, PASSWORD));
	}

	@Test
	public void explicitAuthenticationRejectsNonCompliantPassword() {
		mockEmployeeSuccess("short");
		when(remoteUserService.info(USERNAME, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(userInfo(CommonConstants.STATUS_NORMAL,
						new BCryptPasswordEncoder().encode("short"))));

		expectException(NotStrongPasswordException.class, () -> service.authenticate(USERNAME, "short"));
	}

	@Test
	public void legacyOauthStillUsesGetFeignMethodAndWritesCache() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(SecurityConstants.OAUTH_TOKEN_URL);
		request.setQueryString(SecurityConstants.PASSWORD + "=" + PASSWORD);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		when(cacheManager.getCache("user_details")).thenReturn(cache);
		when(cache.get(USERNAME)).thenReturn(null);
		when(remoteUserService.simpleLogin(USERNAME, PASSWORD, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(Boolean.TRUE));

		UserDetails details = service.loadUserByUsername(USERNAME);

		assertEquals(USERNAME, details.getUsername());
		verify(cache).put(eq(USERNAME), any(UserDetails.class));
		verify(remoteUserService, never()).authenticateAppSession(any(UserCredentialDTO.class), anyString());
	}

	@Test
	public void legacyCachedDetailsStillReturnWithoutServletContext() {
		SmartUser cached = activeSmartUser();
		when(cacheManager.getCache("user_details")).thenReturn(cache);
		when(cache.get(USERNAME)).thenReturn(() -> cached);

		assertSame(cached, service.loadUserByUsername(USERNAME));
	}

	private void mockEmployeeSuccess(String password) {
		when(remoteUserService.authenticateAppSession(any(UserCredentialDTO.class), eq(SecurityConstants.FROM_IN)))
				.thenAnswer(invocation -> {
					UserCredentialDTO credential = invocation.getArgument(0);
					return Result.success(USERNAME.equals(credential.getUsername())
							&& password.equals(credential.getPassword()));
				});
	}

	private void markAsPlatformAccount() {
		SysDict dict = new SysDict();
		dict.setValue(USERNAME);
		when(remoteDictService.findByType(SecurityConstants.SYS_DEFAULT_USER, SecurityConstants.FROM_IN))
				.thenReturn(Result.success(Collections.singletonList(dict)));
	}

	private UserInfo userInfo(String lockFlag, String encodedPassword) {
		SysUser user = new SysUser();
		user.setUserId(1);
		user.setDeptId(2);
		user.setUsername(USERNAME);
		user.setPassword(encodedPassword);
		user.setLockFlag(lockFlag);
		UserInfo info = new UserInfo();
		info.setSysUser(user);
		info.setRoles(new Integer[0]);
		info.setPermissions(new String[0]);
		return info;
	}

	private SmartUser activeSmartUser() {
		return new SmartUser(1, 2, USERNAME, Collections.emptyList(), "{noop}ignored",
				true, true, true, true, Collections.emptyList());
	}

	private <T extends Throwable> T expectException(Class<T> type, ThrowingRunnable action) {
		try {
			action.run();
			fail("应抛出 " + type.getSimpleName());
			return null;
		} catch (Throwable failure) {
			assertTrue(type.isInstance(failure));
			return type.cast(failure);
		}
	}

	@FunctionalInterface
	private interface ThrowingRunnable {
		void run() throws Exception;
	}
}
