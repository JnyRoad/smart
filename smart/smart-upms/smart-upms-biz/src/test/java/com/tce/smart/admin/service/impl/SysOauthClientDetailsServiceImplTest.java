package com.tce.smart.admin.service.impl;

import com.tce.smart.admin.api.entity.SysOauthClientDetails;
import com.tce.smart.admin.api.feign.RemoteTokenService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
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
 * <p>覆盖四条能力：
 * 1. 重置 client secret：返回 32 位明文，库中落地值必须以 {@link SecurityConstants#BCRYPT} 开头，
 *    且重置成功后必须调用 Feign 吊销该 clientId 下的旧 token；
 * 2. 删除应用：删除成功后必须调用 Feign 吊销该 clientId 下的旧 token，
 *    确保验收标准“删除后旧 token 立即 401/403”。
 * 3. 新建/编辑应用时对明文 client_secret 做 BCrypt 编码（终审 F-1 修复）：
 *    明文落库前必须补齐 {bcrypt} 前缀，已带前缀的值原样保留，避免二次编码。
 * 4. 重置 secret 使用密码学安全随机源（终审 F-2 修复）：长度、字符集与原实现保持一致。
 * 均通过 mock {@link RemoteTokenService} 验证调用发生，不依赖真实 auth 服务。</p>
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
	 * F-2 修复：resetSecret 改用密码学安全随机源（{@code RandomUtil.getSecureRandom()}）后，
	 * 长度与字符集必须与迁移前保持一致（32 位、仅小写字母 + 数字），否则会破坏管理页展示、
	 * 复制粘贴等下游行为。这里连续生成多次，断言字符集始终落在约定范围内，
	 * 顺带覆盖“不同调用产生不同明文”这一基本随机性期望。
	 */
	@Test
	public void resetSecret_usesExpectedCharsetAndLength_acrossMultipleInvocations() {
		String clientId = "smart-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setClientSecret("{bcrypt}old-hash");
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		when(remoteTokenService.removeTokensByClientId(eq(clientId), eq(SecurityConstants.FROM_IN)))
				.thenReturn(new Result<>(1));

		java.util.Set<String> generated = new java.util.HashSet<>();
		for (int i = 0; i < 20; i++) {
			String plainSecret = service.resetSecret(clientId);
			assertThat(plainSecret).hasSize(32);
			assertThat(plainSecret).matches("^[a-z0-9]{32}$");
			generated.add(plainSecret);
		}
		// 20 次独立生成理论上极大概率互不相同，若出现大量重复说明随机源退化。
		assertThat(generated.size() > 1).isTrue();
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

	// ---------------------------------------------------------------------
	// F-1 修复：新建/编辑应用时明文 client_secret 必须编码后再落库，
	// 否则 DelegatingPasswordEncoder 找不到匹配算法前缀，换 token 必然失败。
	// ---------------------------------------------------------------------

	private static final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder BCRYPT_ENCODER_FOR_TEST =
			new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

	/**
	 * save() 最终落库直接读取父类 {@code ServiceImpl} 的 protected 字段 {@code baseMapper} 后调用
	 * {@code baseMapper.insert(entity)}（MyBatis-Plus 内部实现字节码级确认，不经过 {@code getBaseMapper()}
	 * 这类可 spy 的方法），因此这里 mock {@link com.tce.smart.admin.mapper.SysOauthClientDetailsMapper}
	 * 并通过反射直接替换该字段，只验证「传给 mapper.insert 的实体上 clientSecret 是否已被正确编码」，
	 * 不依赖真实数据库。
	 */
	private void mockBaseMapperForSave() throws Exception {
		com.tce.smart.admin.mapper.SysOauthClientDetailsMapper mapper =
				mock(com.tce.smart.admin.mapper.SysOauthClientDetailsMapper.class);
		when(mapper.insert(any(SysOauthClientDetails.class))).thenReturn(1);
		java.lang.reflect.Field baseMapperField =
				com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(service, mapper);
	}

	/**
	 * 新建应用（save）传入明文 secret：落库前必须补齐 {bcrypt} 前缀，
	 * 且编码结果能被 BCryptPasswordEncoder.matches 验证通过。
	 */
	@Test
	public void save_encodesPlainSecret_withBcryptPrefix() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("new-app");
		entity.setClientSecret("plain-secret-123");
		entity.setScope("server");

		boolean result = service.save(entity);

		assertThat(result).isTrue();
		String persisted = entity.getClientSecret();
		assertThat(persisted).startsWith(SecurityConstants.BCRYPT);
		assertThat(BCRYPT_ENCODER_FOR_TEST.matches("plain-secret-123",
				persisted.substring(SecurityConstants.BCRYPT.length()))).isTrue();
	}

	/**
	 * 新建应用传入已带 {noop} 前缀的值：视为调用方已知晓自己在做什么，原样落库，不做二次编码。
	 */
	@Test
	public void save_keepsAlreadyPrefixedNoopSecret_unchanged() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("legacy-app");
		entity.setClientSecret("{noop}plain-secret");
		entity.setScope("server");

		service.save(entity);

		assertThat(entity.getClientSecret()).isEqualTo("{noop}plain-secret");
	}

	/**
	 * 新建应用传入已带 {bcrypt} 前缀的值（例如从别处迁移过来的密文）：原样落库，避免二次编码。
	 */
	@Test
	public void save_keepsAlreadyPrefixedBcryptSecret_unchanged() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("migrated-app");
		String alreadyEncoded = SecurityConstants.BCRYPT + BCRYPT_ENCODER_FOR_TEST.encode("some-secret");
		entity.setClientSecret(alreadyEncoded);
		entity.setScope("server");

		service.save(entity);

		assertThat(entity.getClientSecret()).isEqualTo(alreadyEncoded);
	}

	/**
	 * 编辑应用（update）传入新明文 secret：落库前必须补齐 {bcrypt} 前缀。
	 */
	@Test
	public void updateClientDetailsById_encodesNewPlainSecret_withBcryptPrefix() {
		String clientId = "existing-app";
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setClientSecret("new-plain-secret");
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));

		Boolean result = service.updateClientDetailsById(update);

		assertThat(result).isTrue();
		String persisted = update.getClientSecret();
		assertThat(persisted).startsWith(SecurityConstants.BCRYPT);
		assertThat(BCRYPT_ENCODER_FOR_TEST.matches("new-plain-secret",
				persisted.substring(SecurityConstants.BCRYPT.length()))).isTrue();
	}

	/**
	 * 编辑应用未修改 secret（前端传 null，表示“保持原值不变”）：不能把 null 当明文误编码，
	 * 必须原样跳过，交给 MyBatis-Plus 的 updateById 按非空策略不覆盖该字段。
	 */
	@Test
	public void updateClientDetailsById_doesNotTouchSecret_whenNull() {
		String clientId = "existing-app";
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setClientSecret(null);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));

		service.updateClientDetailsById(update);

		assertThat(update.getClientSecret()).isNull();
	}

	/**
	 * 编辑应用未修改 secret（前端传空字符串）：同上，视为“不修改”，不做编码。
	 */
	@Test
	public void updateClientDetailsById_doesNotTouchSecret_whenBlank() {
		String clientId = "existing-app";
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setClientSecret("");
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));

		service.updateClientDetailsById(update);

		assertThat(update.getClientSecret()).isEmpty();
	}

	/**
	 * 编辑应用传入已带前缀（{bcrypt}/{noop}）的值：原样落库，避免把密文再编码一层导致校验永远失败。
	 */
	@Test
	public void updateClientDetailsById_keepsAlreadyPrefixedSecret_unchanged() {
		String clientId = "existing-app";
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setClientSecret("{bcrypt}already-encoded-hash");
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));

		service.updateClientDetailsById(update);

		assertThat(update.getClientSecret()).isEqualTo("{bcrypt}already-encoded-hash");
	}

	/**
	 * 编辑时只要提交了 scope 字段，就必须拒绝空字符串；否则 MyBatis-Plus 会把存量授权域清空，
	 * 而已签发 token 又不会因为 scope 被视为“未提交”而及时吊销。
	 */
	@Test
	public void updateClientDetailsById_rejectsEmptySubmittedScope_beforePersistingOrRevokingTokens() {
		String clientId = "existing-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope("open:admittance:photo:read");
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("");

		try {
			service.updateClientDetailsById(update);
			org.junit.Assert.fail("提交空 scope 必须被拒绝");
		} catch (TCEException expected) {
			assertThat(expected.getMessage()).contains("不能为空");
		}

		verify(service, never()).updateById(any(SysOauthClientDetails.class));
		verify(remoteTokenService, never()).removeTokensByClientId(anyString(), anyString());
	}

	/**
	 * 空白字符与空字符串同样属于无效授权域，不能绕过编辑接口的 scope 完整性校验。
	 */
	@Test
	public void updateClientDetailsById_rejectsWhitespaceSubmittedScope_beforePersistingOrRevokingTokens() {
		String clientId = "existing-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope("open:admittance:photo:read");
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("  ");

		try {
			service.updateClientDetailsById(update);
			org.junit.Assert.fail("提交空白 scope 必须被拒绝");
		} catch (TCEException expected) {
			assertThat(expected.getMessage()).contains("不能为空");
		}

		verify(service, never()).updateById(any(SysOauthClientDetails.class));
		verify(remoteTokenService, never()).removeTokensByClientId(anyString(), anyString());
	}

	/** 新建客户端不能绕过管理端下拉直接写入未知 scope。 */
	@Test
	public void save_rejectsUnknownCapabilityScope() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("unknown-scope-app");
		entity.setClientSecret("plain-secret");
		entity.setScope("internal:unknown:write");

		try {
			service.save(entity);
			org.junit.Assert.fail("未知 capability scope 必须被后端拒绝");
		} catch (TCEException expected) {
			assertThat(expected.getMessage()).contains("未知");
		}
	}

	/** 新客户端可直接授予 server，作为内部开放接口的统一授权边界。 */
	@Test
	public void save_acceptsServerScope() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("new-server-scope-app");
		entity.setClientSecret("plain-secret");
		entity.setScope("server");

		assertThat(service.save(entity)).isTrue();
		assertThat(entity.getScope()).isEqualTo("server");
	}

	/** 存量细分 scope 客户端可补充 server，变更成功后仍必须吊销旧 token。 */
	@Test
	public void update_addsServerToExistingHistoricalScope_andRevokesTokens() {
		String clientId = "legacy-schedule";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope(" server , internal:energy:projection:run ");

		Boolean result = service.updateClientDetailsById(update);

		assertThat(result).isTrue();
		assertThat(update.getScope()).isEqualTo("server,internal:energy:projection:run");
		verify(remoteTokenService).removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
	}

	/** server 写库前统一去空格并稳定为单项逗号分隔格式。 */
	@Test
	public void save_normalizesServerScope() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("projection-app");
		entity.setClientSecret("plain-secret");
		entity.setScope(" server ");

		service.save(entity);

		assertThat(entity.getScope()).isEqualTo("server");
	}

	/** 新客户端不得重新授予已降级为历史兼容的细分 scope。 */
	@Test
	public void save_rejectsDeprecatedHistoricalScope() throws Exception {
		mockBaseMapperForSave();
		SysOauthClientDetails entity = new SysOauthClientDetails();
		entity.setClientId("new-historical-scope-app");
		entity.setClientSecret("plain-secret");
		entity.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);

		try {
			service.save(entity);
			org.junit.Assert.fail("历史细分 scope 必须拒绝新增授予");
		} catch (TCEException expected) {
			assertThat(expected.getMessage()).contains("历史");
		}
	}
}
