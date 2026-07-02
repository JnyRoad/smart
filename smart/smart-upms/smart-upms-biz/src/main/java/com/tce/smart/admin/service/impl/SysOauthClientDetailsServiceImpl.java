package com.tce.smart.admin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysOauthClientDetails;
import com.tce.smart.admin.api.feign.RemoteTokenService;
import com.tce.smart.admin.mapper.SysOauthClientDetailsMapper;
import com.tce.smart.admin.service.SysOauthClientDetailsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
	 * 根据客户端信息
	 *
	 * @param clientDetails
	 * @return
	 */
	@Override
	@CacheEvict(value = SecurityConstants.CLIENT_DETAILS_KEY, key = "#clientDetails.clientId")
	public Boolean updateClientDetailsById(SysOauthClientDetails clientDetails) {
		return this.updateById(clientDetails);
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

		String plainSecret = RandomUtil.randomString(RESET_SECRET_LENGTH);
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
}
