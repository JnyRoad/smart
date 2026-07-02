package com.tce.smart.admin.service.impl;

import com.tce.smart.admin.api.entity.SysOauthClientDetails;
import com.tce.smart.admin.api.feign.RemoteTokenService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysOauthClientDetailsServiceImpl} 单元测试。
 *
 * <p>覆盖两条新增能力：
 * 1. 重置 client secret：返回 32 位明文，库中落地值必须以 {@link SecurityConstants#BCRYPT} 开头，
 *    且重置成功后必须调用 Feign 吊销该 clientId 下的旧 token；
 * 2. 删除应用：删除成功后必须调用 Feign 吊销该 clientId 下的旧 token，
 *    确保验收标准“删除后旧 token 立即 401/403”。
 * 两处均通过 mock {@link RemoteTokenService} 验证调用发生，不依赖真实 auth 服务。</p>
 */
public class SysOauthClientDetailsServiceImplTest {

	private RemoteTokenService remoteTokenService;
	private CacheManager cacheManager;
	private Cache cache;
	private SysOauthClientDetailsServiceImpl service;

	@Before
	public void setUp() {
		remoteTokenService = mock(RemoteTokenService.class);
		cacheManager = mock(CacheManager.class);
		cache = mock(Cache.class);
		when(cacheManager.getCache(SecurityConstants.CLIENT_DETAILS_KEY)).thenReturn(cache);

		// 用 spy 隔离 MyBatis-Plus ServiceImpl 对真实数据库的依赖（getById/updateById/removeById 由父类 IService 提供）。
		service = spy(new SysOauthClientDetailsServiceImpl(remoteTokenService, cacheManager));
	}

	/**
	 * 重置 secret：返回明文长度 32，且落库值必须带 {bcrypt} 前缀（Task 1 约定的编码前缀规范）。
	 */
	@Test
	public void resetSecret_returnsThirtyTwoCharPlainSecret_andPersistsBcryptPrefixed() {
		String clientId = "smart-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setClientSecret("{bcrypt}old-hash");
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		when(remoteTokenService.removeTokensByClientId(eq(clientId), eq(SecurityConstants.FROM_IN)))
				.thenReturn(new Result<>(1));

		String plainSecret = service.resetSecret(clientId);

		assertThat(plainSecret).hasSize(32);

		// 校验落库的 client_secret 带 {bcrypt} 前缀，且解码后能匹配返回的明文。
		org.mockito.ArgumentCaptor<SysOauthClientDetails> captor =
				org.mockito.ArgumentCaptor.forClass(SysOauthClientDetails.class);
		verify(service).updateById(captor.capture());
		String persisted = captor.getValue().getClientSecret();
		assertThat(persisted).startsWith(SecurityConstants.BCRYPT);
		org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
				new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
		assertThat(encoder.matches(plainSecret, persisted.substring(SecurityConstants.BCRYPT.length()))).isTrue();
	}

	/**
	 * 重置 secret 成功后必须：1) 清 CLIENT_DETAILS_KEY 缓存 2) 调用 Feign 吊销旧 token。
	 * 快速失败要求：任一步不能被静默吞掉，这里通过 verify 强制断言调用确实发生。
	 */
	@Test
	public void resetSecret_evictsCache_andRevokesOldTokensViaFeign() {
		String clientId = "smart-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setClientSecret("{bcrypt}old-hash");
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		when(remoteTokenService.removeTokensByClientId(eq(clientId), eq(SecurityConstants.FROM_IN)))
				.thenReturn(new Result<>(2));

		service.resetSecret(clientId);

		verify(cache, times(1)).evict(clientId);
		verify(remoteTokenService, times(1)).removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
	}

	/**
	 * 删除应用成功后必须调用 Feign 吊销该 clientId 下的旧 token，
	 * 对应简报验收标准“删除后旧 token 立即 401/403”。
	 */
	@Test
	public void removeClientDetailsById_revokesTokensViaFeign_whenDeleteSucceeds() {
		String clientId = "smart-app";
		doReturn(true).when(service).removeById(clientId);
		when(remoteTokenService.removeTokensByClientId(eq(clientId), eq(SecurityConstants.FROM_IN)))
				.thenReturn(new Result<>(3));

		Boolean result = service.removeClientDetailsById(clientId);

		assertThat(result).isTrue();
		verify(remoteTokenService, times(1)).removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
	}

	/**
	 * 删除失败（removeById 返回 false）时不应该去吊销 token——快速失败原则下，
	 * 没有真正删除成功就不能制造“旧 token 已失效”的假象。
	 */
	@Test
	public void removeClientDetailsById_doesNotRevokeTokens_whenDeleteFails() {
		String clientId = "smart-app";
		doReturn(false).when(service).removeById(clientId);

		Boolean result = service.removeClientDetailsById(clientId);

		assertThat(result).isFalse();
		verify(remoteTokenService, never()).removeTokensByClientId(anyString(), anyString());
	}
}
