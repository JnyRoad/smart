package com.tce.smart.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysOauthClientDetails;
import com.tce.smart.admin.api.feign.RemoteTokenService;
import com.tce.smart.admin.mapper.SysOauthClientDetailsMapper;
import com.tce.smart.admin.service.SysOauthClientDetailsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOauthClientDetailsServiceImpl extends ServiceImpl<SysOauthClientDetailsMapper, SysOauthClientDetails> implements SysOauthClientDetailsService {

	/**
	 * 重置 secret 时生成的随机明文长度，与简报约定一致。
	 */
	private static final int RESET_SECRET_LENGTH = 32;

	/**
	 * 随机明文 secret 使用的字符集：与 {@code cn.hutool.core.util.RandomUtil#randomString(int)}
	 * 默认字符集保持一致（小写字母 + 数字），仅将随机源从线程本地伪随机数换成密码学安全随机数，
	 * 避免破坏既有明文长度/字符集约定，减少下游（管理页展示、复制）意外行为。
	 */
	private static final String SECRET_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

	private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

	private final RemoteTokenService remoteTokenService;

	private final CacheManager cacheManager;

	/**
	 * 通过ID删除客户端
	 *
	 * <p>删除成功后必须吊销该 clientId 下的旧 token，确保验收标准“删除后旧 token 立即 401/403”；
	 * 吊销走 Feign 调用 auth 服务，失败时直接抛出（快速失败），不允许静默吞掉让删除“看起来成功”。</p>
	 *
	 * @param id
	 * @return
	 */
	@Override
	@CacheEvict(value = SecurityConstants.CLIENT_DETAILS_KEY, key = "#id")
	public Boolean removeClientDetailsById(String id) {
		Boolean removed = this.removeById(id);
		if (Boolean.TRUE.equals(removed)) {
			revokeTokens(id);
		}
		return removed;
	}

	/**
	 * 新增客户端（管理页“新建应用”走此入口，继承自 {@link com.baomidou.mybatisplus.extension.service.IService#save}）。
	 *
	 * <p>DelegatingPasswordEncoder 要求库中 client_secret 必须带编码前缀（{noop}/{bcrypt}）才能正确匹配算法，
	 * 否则换 token 时 Basic 认证必然失败（详见 spec §3.1）。管理页新建应用时前端只提交明文，
	 * 这里必须在落库前补齐 {bcrypt} 前缀，与 {@link #resetSecret} 使用同一编码方式，保持行为一致。</p>
	 *
	 * @param entity 待新增的客户端信息
	 * @return 是否新增成功
	 */
	@Override
	public boolean save(SysOauthClientDetails entity) {
		validateAndNormalizeScopes(entity, Collections.<String>emptySet());
		encodePlainSecretIfNeeded(entity);
		return super.save(entity);
	}

	/**
	 * 根据客户端信息
	 *
	 * <p>与 {@link #save} 同理：管理页“编辑应用”若填写了新的明文 secret，落库前必须补齐 {bcrypt} 前缀；
	 * 若未改 secret（前端传 null/空，表示保持原值不变），不得误编码空值，直接跳过即可。
	 * 已带前缀（{noop}/{bcrypt}）的值视为“调用方已知晓自己在做什么”，原样保留，避免二次编码导致密文错乱。
	 * scope 实际变化且落库成功后会吊销该客户端已有令牌，防止已签发的大权限 token 在过期前继续生效。</p>
	 *
	 * @param clientDetails
	 * @return
	 */
	@Override
	@CacheEvict(value = SecurityConstants.CLIENT_DETAILS_KEY, key = "#clientDetails.clientId")
	public Boolean updateClientDetailsById(SysOauthClientDetails clientDetails) {
		SysOauthClientDetails existing = null;
		// null 表示调用方没有修改 scope；空字符串或空白表示提交了无效值，必须走校验并拒绝落库。
		boolean scopeSubmitted = clientDetails.getScope() != null;
		boolean scopeChanged = false;
		if (scopeSubmitted) {
			existing = this.getById(clientDetails.getClientId());
			Set<String> currentScopes = existingScopes(existing);
			validateAndNormalizeScopes(clientDetails, currentScopes);
			scopeChanged = !currentScopes.equals(scopesFromRaw(clientDetails.getScope()));
		}
		encodePlainSecretIfNeeded(clientDetails);
		Boolean updated = this.updateById(clientDetails);
		if (Boolean.TRUE.equals(updated) && scopeSubmitted && scopeChanged) {
			revokeTokens(clientDetails.getClientId());
		}
		return updated;
	}

	/**
	 * 校验并规范化逗号分隔 scope。前端下拉只是体验层；所有写库入口都必须在这里拦截未知、空白
	 * 和重复 capability，避免绕过管理页直接扩大 OAuth 客户端权限。
	 *
	 * <p>已经存在的废弃或历史未知 scope 仅可在原客户端编辑时原样保留，不能被没有该存量授权的
	 * 客户端新增授予；{@code server} 是内部开放接口唯一正常可授予的 scope。</p>
	 */
	private void validateAndNormalizeScopes(SysOauthClientDetails clientDetails, Set<String> existingScopes) {
		String rawScopes = clientDetails.getScope();
		if (!StringUtils.hasText(rawScopes)) {
			throw new TCEException("客户端 scope 不能为空");
		}
		Set<String> normalizedScopes = new LinkedHashSet<>();
		for (String rawScope : rawScopes.split(",", -1)) {
			String scope = rawScope == null ? null : rawScope.trim();
			if (!StringUtils.hasText(scope)) {
				throw new TCEException("客户端 scope 不能包含空值");
			}
			if (!normalizedScopes.add(scope)) {
				throw new TCEException("客户端 scope 不能重复：" + scope);
			}
			boolean existing = existingScopes.contains(scope);
			if (!OpenApiScopeCatalog.contains(scope) && !existing) {
				throw new TCEException("未知 capability scope：" + scope);
			}
			if (OpenApiScopeCatalog.isDeprecated(scope) && !existing) {
				throw new TCEException("历史 capability scope 不允许新增授予：" + scope);
			}
		}
		clientDetails.setScope(String.join(",", normalizedScopes));
	}

	/**
	 * 读取当前记录的 scope，用于允许历史客户端在不扩权前提下保留已存在的兼容 scope。
	 */
	private Set<String> existingScopes(SysOauthClientDetails existing) {
		if (existing == null) {
			return Collections.emptySet();
		}
		return scopesFromRaw(existing.getScope());
	}

	/**
	 * 将已存或待提交的逗号分隔 scope 规范为集合，仅用于比较和存量兼容判断；
	 * 写入前的空值、重复与未知值仍由 {@link #validateAndNormalizeScopes} 严格拒绝。
	 */
	private Set<String> scopesFromRaw(String rawScopes) {
		if (!StringUtils.hasText(rawScopes)) {
			return Collections.emptySet();
		}
		Set<String> scopes = new LinkedHashSet<>();
		for (String rawScope : rawScopes.split(",", -1)) {
			String scope = rawScope == null ? null : rawScope.trim();
			if (StringUtils.hasText(scope)) {
				scopes.add(scope);
			}
		}
		return scopes;
	}

	/**
	 * 若 clientSecret 非空且不带编码前缀（不以 '{' 开头），视为明文，用与 {@link #resetSecret} 相同的
	 * BCryptPasswordEncoder 编码后覆盖为 {bcrypt} 前缀值；已带前缀（{noop}/{bcrypt} 等）或为空
	 * （update 场景下表示“本次不修改 secret”）时原样跳过，避免二次编码把密文再套一层导致校验永远失败。
	 *
	 * @param clientDetails 待落库的客户端信息，方法内直接修改其 clientSecret 字段
	 */
	private void encodePlainSecretIfNeeded(SysOauthClientDetails clientDetails) {
		String clientSecret = clientDetails.getClientSecret();
		if (!StringUtils.hasText(clientSecret) || clientSecret.startsWith("{")) {
			return;
		}
		clientDetails.setClientSecret(SecurityConstants.BCRYPT + BCRYPT_ENCODER.encode(clientSecret));
	}

	/**
	 * 重置指定客户端的 secret。
	 *
	 * <p>流程：1) 校验客户端存在 2) 生成 32 位随机明文 3) BCryptPasswordEncoder 编码后拼 {bcrypt} 前缀落库
	 * 4) 清 CLIENT_DETAILS_KEY 缓存 5) Feign 吊销该 clientId 下的旧 token。
	 * 明文只在方法返回值里出现一次，调用方（controller）负责禁止把它打进日志。</p>
	 *
	 * @param clientId 客户端ID
	 * @return 新生成的明文 secret
	 */
	@Override
	public String resetSecret(String clientId) {
		SysOauthClientDetails existing = this.getById(clientId);
		if (existing == null) {
			throw new TCEException("客户端不存在：" + clientId);
		}

		String plainSecret = randomSecret(RESET_SECRET_LENGTH);
		String encodedSecret = SecurityConstants.BCRYPT + BCRYPT_ENCODER.encode(plainSecret);

		SysOauthClientDetails update = new SysOauthClientDetails();
		update.setClientId(clientId);
		update.setClientSecret(encodedSecret);
		boolean updated = this.updateById(update);
		if (!updated) {
			// 落库失败必须显式报错，禁止返回明文给调用方却没有真正生效。
			throw new TCEException("重置 client secret 失败：" + clientId);
		}

		evictClientDetailsCache(clientId);
		revokeTokens(clientId);
		return plainSecret;
	}

	/**
	 * 通过 Feign 调用 auth 服务，吊销指定 clientId 下的所有旧 token。
	 * 调用失败（Feign 抛异常）时不捕获，向上抛出让调用方感知——
	 * 否则会出现“secret 已重置/应用已删除，但旧 token 仍然有效”的安全假象。
	 *
	 * @param clientId 客户端ID
	 */
	private void revokeTokens(String clientId) {
		remoteTokenService.removeTokensByClientId(clientId, SecurityConstants.FROM_IN);
	}

	/**
	 * 显式清除 CLIENT_DETAILS_KEY 缓存中该 clientId 对应的条目。
	 * 与 removeClientDetailsById / updateClientDetailsById 上的 {@link CacheEvict} 注解是同一个缓存名，
	 * 这里用编程式调用是因为 resetSecret 需要在方法内部（而非整方法返回后）就完成清缓存，
	 * 便于在吊销 token 之前先让新读取拿到最新的 client 详情。
	 *
	 * @param clientId 客户端ID
	 */
	private void evictClientDetailsCache(String clientId) {
		Cache cache = cacheManager.getCache(SecurityConstants.CLIENT_DETAILS_KEY);
		if (cache == null) {
			// 缓存未配置/名字对不上时必须显式报错，不能悄悄跳过——否则旧 secret 可能继续被缓存命中。
			throw new TCEException("客户端详情缓存不存在：" + SecurityConstants.CLIENT_DETAILS_KEY);
		}
		cache.evict(clientId);
	}

	/**
	 * 生成指定长度的随机明文 secret，字符集与长度保持 {@code RandomUtil.randomString(int)} 原有约定不变，
	 * 但随机源改用 {@link RandomUtil#getSecureRandom()}（底层为 {@link SecureRandom}）。
	 *
	 * <p>原因：resetSecret 生成的是对外长期有效的应用凭证（App Secret），不是一次性验证码或界面装饰，
	 * 若使用 {@code RandomUtil.randomString} 底层依赖的 {@code ThreadLocalRandom}（非密码学安全的伪随机数），
	 * 攻击者在已知部分输出或线程调度信息的情况下有理论上可预测后续输出的风险，
	 * 长期凭证必须使用 CSPRNG（Cryptographically Secure Pseudo-Random Number Generator）。</p>
	 *
	 * @param length 期望生成的明文长度
	 * @return 由小写字母与数字组成的随机明文
	 */
	private static String randomSecret(int length) {
		SecureRandom secureRandom = RandomUtil.getSecureRandom();
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(SECRET_CHARS.charAt(secureRandom.nextInt(SECRET_CHARS.length())));
		}
		return builder.toString();
	}
}
