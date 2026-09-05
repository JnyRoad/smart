package com.tce.smart.admin.service.impl;

import com.tce.smart.admin.api.entity.SysOauthClientDetails;
import com.tce.smart.admin.api.feign.RemoteTokenService;
import com.tce.smart.admin.entity.OauthClientTokenRevocationTask;
import com.tce.smart.admin.mapper.OauthClientTokenRevocationTaskMapper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
	private OauthClientTokenRevocationTaskMapper tokenRevocationTaskMapper;
	private Map<String, OauthClientTokenRevocationTask> pendingRevocations;
	private PlatformTransactionManager transactionManager;
	private Map<String, OauthClientTokenRevocationTask> transactionSnapshot;
	private SysOauthClientDetailsServiceImpl service;

	@Before
	public void setUp() {
		remoteTokenService = mock(RemoteTokenService.class);
		cacheManager = mock(CacheManager.class);
		cache = mock(Cache.class);
		when(cacheManager.getCache(SecurityConstants.CLIENT_DETAILS_KEY)).thenReturn(cache);
		tokenRevocationTaskMapper = mock(OauthClientTokenRevocationTaskMapper.class);
		pendingRevocations = new LinkedHashMap<>();
		transactionManager = mock(PlatformTransactionManager.class);
		when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenAnswer(invocation -> {
			transactionSnapshot = new LinkedHashMap<>(pendingRevocations);
			return new SimpleTransactionStatus();
		});
		doAnswer(invocation -> {
			pendingRevocations.clear();
			pendingRevocations.putAll(transactionSnapshot);
			return null;
		}).when(transactionManager).rollback(any(TransactionStatus.class));
		doAnswer(invocation -> {
			OauthClientTokenRevocationTask task = invocation.getArgument(0);
			pendingRevocations.put(task.getTaskId(), task);
			return 1;
		}).when(tokenRevocationTaskMapper).insert(any(OauthClientTokenRevocationTask.class));
		when(tokenRevocationTaskMapper.selectOldestByClientId(anyString())).thenAnswer(invocation -> {
			String clientId = invocation.getArgument(0);
			return pendingRevocations.values().stream()
					.filter(task -> clientId.equals(task.getClientId()))
					.sorted(Comparator.comparing(OauthClientTokenRevocationTask::getCreateTime)
							.thenComparing(OauthClientTokenRevocationTask::getTaskId))
					.findFirst().orElse(null);
		});
		when(tokenRevocationTaskMapper.selectPendingBatch(any(LocalDateTime.class), any(Integer.class)))
				.thenAnswer(invocation -> {
			LocalDateTime now = invocation.getArgument(0);
			int limit = invocation.getArgument(1);
			return pendingRevocations.values().stream()
					.filter(task -> !task.getNextRetryAt().isAfter(now))
					.sorted(Comparator.comparing(OauthClientTokenRevocationTask::getNextRetryAt)
							.thenComparing(OauthClientTokenRevocationTask::getCreateTime)
							.thenComparing(OauthClientTokenRevocationTask::getTaskId))
					.limit(limit).collect(Collectors.toList());
		});
		when(tokenRevocationTaskMapper.postponeFailure(anyString(), any(LocalDateTime.class)))
				.thenAnswer(invocation -> {
			String taskId = invocation.getArgument(0);
			LocalDateTime nextRetryAt = invocation.getArgument(1);
			OauthClientTokenRevocationTask task = pendingRevocations.get(taskId);
			if (task == null || !task.getNextRetryAt().isBefore(nextRetryAt)) {
				return 0;
			}
			task.setNextRetryAt(nextRetryAt);
			return 1;
		});
		when(tokenRevocationTaskMapper.deleteById(anyString())).thenAnswer(invocation -> {
			String taskId = invocation.getArgument(0);
			return pendingRevocations.remove(taskId) == null ? 0 : 1;
		});

		// 用 spy 隔离 MyBatis-Plus ServiceImpl 对真实数据库的依赖（getById/updateById/removeById 由父类 IService 提供）。
		service = spy(new SysOauthClientDetailsServiceImpl(remoteTokenService, cacheManager,
				tokenRevocationTaskMapper, transactionManager));
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
		assertThat(pendingRevocations).isEmpty();
		verify(transactionManager).rollback(any(TransactionStatus.class));
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
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenReturn(new Result<>(0));
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

	/** scope 更新后的吊销异常不能阻止清缓存，且再次提交相同 scope 必须恢复上次待办。 */
	@Test
	public void updateClientDetailsById_evictsCacheAndRetriesPendingRevocation_whenSameScopeIsResubmitted() {
		String clientId = "retry-scope-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenThrow(new RuntimeException("auth unavailable"))
				.thenReturn(new Result<>(0));
		SysOauthClientDetails firstUpdate = new SysOauthClientDetails();
		firstUpdate.setClientId(clientId);
		firstUpdate.setScope("server");

		try {
			service.updateClientDetailsById(firstUpdate);
			org.junit.Assert.fail("首次吊销异常必须向调用方暴露");
		} catch (RuntimeException expected) {
			assertThat(expected.getMessage()).contains("auth unavailable");
		}

		assertThat(hasPendingForClient(clientId)).isTrue();
		verify(cache).evict(clientId);

		existing.setScope("server");
		SysOauthClientDetails retry = new SysOauthClientDetails();
		retry.setClientId(clientId);
		retry.setScope("server");
		assertThat(service.updateClientDetailsById(retry)).isTrue();

		verify(remoteTokenService, times(2)).removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
		verify(cache, times(2)).evict(clientId);
		assertThat(hasPendingForClient(clientId)).isFalse();
	}

	/** auth 返回失败 Result 时不能伪装成撤销成功，待办必须保留以便恢复。 */
	@Test
	public void updateClientDetailsById_rejectsRemoteBusinessFailure_andKeepsPending() {
		String clientId = "business-failure-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenReturn(Result.fail("auth refused"));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("server");

		try {
			service.updateClientDetailsById(update);
			org.junit.Assert.fail("远程业务失败不能视为已撤销");
		} catch (TCEException expected) {
			assertThat(expected.getMessage()).contains("吊销");
		}

		assertThat(hasPendingForClient(clientId)).isTrue();
	}

	/** 相同 scope 的历史待办查询异常不能阻止已成功授权写入后的缓存失效。 */
	@Test
	public void updateClientDetailsById_evictsCache_beforePendingLookupFailure() {
		String clientId = "pending-read-failure-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope("server");
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		doThrow(new RuntimeException("outbox read unavailable"))
				.when(tokenRevocationTaskMapper).selectOldestByClientId(clientId);
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("server");

		try {
			service.updateClientDetailsById(update);
			org.junit.Assert.fail("待办读取异常必须向调用方暴露");
		} catch (RuntimeException expected) {
			assertThat(expected.getMessage()).contains("outbox read unavailable");
		}

		verify(cache).evict(clientId);
		verify(remoteTokenService, never()).removeTokensByClientId(anyString(), anyString());
	}

	/** 数据库 outbox 待办无法写入时必须在授权变更前失败关闭。 */
	@Test
	public void updateClientDetailsById_doesNotPersistAuthorization_whenPendingRecordCannotBeStored() {
		String clientId = "outbox-failure-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		doThrow(new RuntimeException("outbox unavailable"))
				.when(tokenRevocationTaskMapper).insert(any(OauthClientTokenRevocationTask.class));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("server");

		try {
			service.updateClientDetailsById(update);
			org.junit.Assert.fail("待办写入失败时不能继续修改授权");
		} catch (TCEException expected) {
			assertThat(expected.getMessage()).contains("待办");
		}

		verify(service, never()).updateById(any(SysOauthClientDetails.class));
		verify(remoteTokenService, never()).removeTokensByClientId(anyString(), anyString());
	}

	/** 写退避时间也失败时不能用存储异常替换原始 auth 失败。 */
	@Test
	public void updateClientDetailsById_preservesOriginalFailure_whenPostponeAlsoFails() {
		String clientId = "postpone-failure-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doReturn(true).when(service).updateById(any(SysOauthClientDetails.class));
		RuntimeException authFailure = new RuntimeException("original auth failure");
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenThrow(authFailure);
		doThrow(new RuntimeException("postpone unavailable")).when(tokenRevocationTaskMapper)
				.postponeFailure(anyString(), any(LocalDateTime.class));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("server");

		try {
			service.updateClientDetailsById(update);
			org.junit.Assert.fail("原始 auth 异常必须继续向调用方暴露");
		} catch (RuntimeException actual) {
			assertThat(actual).isSameAs(authFailure);
			assertThat(actual.getSuppressed()).hasSize(1);
			assertThat(actual.getSuppressed()[0].getMessage()).contains("postpone unavailable");
		}

		assertThat(hasPendingForClient(clientId)).isTrue();
	}

	/** 删除成功后的吊销异常也必须先清缓存并保留不含凭据的恢复待办。 */
	@Test
	public void removeClientDetailsById_evictsCacheAndKeepsPending_whenRevocationFails() {
		String clientId = "deleted-app";
		doReturn(true).when(service).removeById(clientId);
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenThrow(new RuntimeException("auth unavailable"));

		try {
			service.removeClientDetailsById(clientId);
			org.junit.Assert.fail("删除后的吊销异常必须向调用方暴露");
		} catch (RuntimeException expected) {
			assertThat(expected.getMessage()).contains("auth unavailable");
		}

		verify(cache).evict(clientId);
		assertThat(pendingRevocations).hasSize(1);
		OauthClientTokenRevocationTask task = pendingRevocations.values().iterator().next();
		assertThat(task.getClientId()).isEqualTo(clientId);
		assertThat(task.toString()).doesNotContain("secret", "accessToken", "refreshToken");
	}

	/** 模拟进程在事务提交后、同步恢复前退出：后台必须恢复并清除已提交待办。 */
	@Test
	public void recoverPendingTokenRevocations_clearsPendingAfterSuccess() {
		String clientId = "recover-app";
		addPending(clientId, "version-1");
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenReturn(new Result<>(0));

		service.recoverPendingTokenRevocations();

		verify(cache).evict(clientId);
		verify(remoteTokenService).removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
		assertThat(hasPendingForClient(clientId)).isFalse();
	}

	/** 后台补偿遇到远程业务失败时必须保留待办，供后续周期继续重试。 */
	@Test
	public void recoverPendingTokenRevocations_keepsPendingAfterRemoteBusinessFailure() {
		String clientId = "recover-failure-app";
		addPending(clientId, "version-1");
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenReturn(Result.fail("auth refused"));

		service.recoverPendingTokenRevocations();

		assertThat(pendingRevocations).containsKey("version-1");
	}

	/** 缓存清理异常发生在待办提交之后，必须保留待办且不能提前调用 auth。 */
	@Test
	public void recoverPendingTokenRevocations_keepsPending_whenCacheEvictionFails() {
		String clientId = "cache-failure-app";
		addPending(clientId, "version-cache");
		doThrow(new RuntimeException("cache unavailable")).when(cache).evict(clientId);

		service.recoverPendingTokenRevocations();

		assertThat(pendingRevocations).containsKey("version-cache");
		verify(remoteTokenService, never()).removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
	}

	/** 单个客户端恢复失败不能阻断同一批次中后续客户端。 */
	@Test
	public void recoverPendingTokenRevocations_continuesAfterOneClientFails() {
		addPending("first-failure-app", "version-first");
		addPending("later-success-app", "version-later");
		when(remoteTokenService.removeTokensByClientId("first-failure-app", SecurityConstants.FROM_IN))
				.thenReturn(Result.fail("auth refused"));
		when(remoteTokenService.removeTokensByClientId("later-success-app", SecurityConstants.FROM_IN))
				.thenReturn(new Result<>(0));

		service.recoverPendingTokenRevocations();

		assertThat(pendingRevocations).containsOnlyKeys("version-first");
		verify(remoteTokenService).removeTokensByClientId("later-success-app", SecurityConstants.FROM_IN);
	}

	/** 旧补偿执行期间出现新待办时，旧版本的成功结果不能删除新版本。 */
	@Test
	public void recoverPendingTokenRevocations_doesNotClearNewerPendingVersion() {
		String clientId = "concurrent-app";
		addPending(clientId, "version-1");
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenAnswer(invocation -> {
					addPending(clientId, "version-2");
					return new Result<>(0);
				});

		service.recoverPendingTokenRevocations();

		assertThat(pendingRevocations).containsOnlyKeys("version-2");
	}

	/** 授权与 outbox 提交完成前不得清缓存或调用 auth，避免外部副作用早于数据库事实。 */
	@Test
	public void updateClientDetailsById_commitsAuthorizationAndOutbox_beforeCacheEvictionAndRevocation() {
		String clientId = "commit-order-app";
		List<String> events = new ArrayList<>();
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doAnswer(invocation -> {
			events.add("update");
			return true;
		}).when(service).updateById(any(SysOauthClientDetails.class));
		doAnswer(invocation -> {
			events.add("outbox");
			OauthClientTokenRevocationTask task = invocation.getArgument(0);
			pendingRevocations.put(task.getTaskId(), task);
			return 1;
		}).when(tokenRevocationTaskMapper).insert(any(OauthClientTokenRevocationTask.class));
		doAnswer(invocation -> {
			events.add("commit");
			return null;
		}).when(transactionManager).commit(any(TransactionStatus.class));
		doAnswer(invocation -> {
			events.add("cache");
			return null;
		}).when(cache).evict(clientId);
		when(remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN))
				.thenAnswer(invocation -> {
					events.add("revoke");
					return new Result<>(0);
				});
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("server");

		assertThat(service.updateClientDetailsById(update)).isTrue();

		assertThat(events).containsExactly("outbox", "update", "commit", "cache", "revoke");
		org.mockito.ArgumentCaptor<TransactionDefinition> definitionCaptor =
				org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(transactionManager).getTransaction(definitionCaptor.capture());
		assertThat(definitionCaptor.getValue().getPropagationBehavior())
				.isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		org.mockito.ArgumentCaptor<OauthClientTokenRevocationTask> taskCaptor =
				org.mockito.ArgumentCaptor.forClass(OauthClientTokenRevocationTask.class);
		verify(tokenRevocationTaskMapper).insert(taskCaptor.capture());
		assertThat(taskCaptor.getValue().getNextRetryAt()).isEqualTo(taskCaptor.getValue().getCreateTime());
	}

	/** 数据库未更新时 outbox 必须回滚，且不能清缓存或吊销仍有效客户端。 */
	@Test
	public void updateClientDetailsById_rollsBackOutbox_whenDatabaseUpdateReturnsFalse() {
		String clientId = "rollback-app";
		SysOauthClientDetails existing = new SysOauthClientDetails();
		existing.setClientId(clientId);
		existing.setScope(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN);
		doReturn(existing).when(service).getById(clientId);
		doReturn(false).when(service).updateById(any(SysOauthClientDetails.class));
		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setScope("server");

		assertThat(service.updateClientDetailsById(update)).isFalse();

		verify(transactionManager).rollback(any(TransactionStatus.class));
		assertThat(pendingRevocations).isEmpty();
		verify(cache, never()).evict(clientId);
		verify(remoteTokenService, never()).removeTokensByClientId(anyString(), anyString());
	}

	/** 单次后台恢复必须受批大小限制，避免积压任务长期占用调度线程。 */
	@Test
	public void recoverPendingTokenRevocations_limitsEachScheduledBatch() throws Exception {
		java.lang.reflect.Field batchSize = SysOauthClientDetailsServiceImpl.class
				.getDeclaredField("recoveryBatchSize");
		batchSize.setAccessible(true);
		batchSize.set(service, 2);
		addPending("batch-app-1", "version-1");
		addPending("batch-app-2", "version-2");
		addPending("batch-app-3", "version-3");
		when(remoteTokenService.removeTokensByClientId(anyString(), eq(SecurityConstants.FROM_IN)))
				.thenReturn(new Result<>(0));

		service.recoverPendingTokenRevocations();

		verify(remoteTokenService, times(2)).removeTokensByClientId(anyString(), eq(SecurityConstants.FROM_IN));
		assertThat(pendingRevocations).hasSize(1);
	}

	/** 首批任务持续失败时，退避后的下一周期必须让后续可成功客户端获得处理机会。 */
	@Test
	public void recoverPendingTokenRevocations_advancesPastFailedFirstBatchOnNextCycle() throws Exception {
		java.lang.reflect.Field batchSize = SysOauthClientDetailsServiceImpl.class
				.getDeclaredField("recoveryBatchSize");
		batchSize.setAccessible(true);
		batchSize.set(service, 2);
		addPending("failed-batch-app-1", "version-failed-1");
		addPending("failed-batch-app-2", "version-failed-2");
		addPending("later-success-app", "version-success");
		when(remoteTokenService.removeTokensByClientId("failed-batch-app-1", SecurityConstants.FROM_IN))
				.thenReturn(Result.fail("auth refused"));
		when(remoteTokenService.removeTokensByClientId("failed-batch-app-2", SecurityConstants.FROM_IN))
				.thenReturn(Result.fail("auth refused"));
		when(remoteTokenService.removeTokensByClientId("later-success-app", SecurityConstants.FROM_IN))
				.thenReturn(new Result<>(0));

		service.recoverPendingTokenRevocations();
		service.recoverPendingTokenRevocations();

		verify(remoteTokenService).removeTokensByClientId("later-success-app", SecurityConstants.FROM_IN);
		assertThat(pendingRevocations).doesNotContainKey("version-success");
	}

	/** 向内存 outbox 写入一个已提交待办，用于模拟调度恢复。 */
	private OauthClientTokenRevocationTask addPending(String clientId, String taskId) {
		OauthClientTokenRevocationTask task = new OauthClientTokenRevocationTask();
		task.setTaskId(taskId);
		task.setClientId(clientId);
		task.setCreateTime(LocalDateTime.of(2026, 9, 4, 0, 0).plusSeconds(pendingRevocations.size()));
		task.setNextRetryAt(task.getCreateTime());
		pendingRevocations.put(taskId, task);
		return task;
	}

	/** 判断内存 outbox 中是否仍存在指定客户端的待办。 */
	private boolean hasPendingForClient(String clientId) {
		return pendingRevocations.values().stream().anyMatch(task -> clientId.equals(task.getClientId()));
	}
}
