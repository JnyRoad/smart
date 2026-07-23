package com.tce.smart.platform.service.admittance.impl;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import com.tce.smart.platform.service.admittance.VisitorFaceDraftCredential;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 访客人脸裁剪能力的 Redis 实现。
 *
 * 草稿会话只能由成功校验的微信 code 签发，Redis 只保存 openId 摘要；裁剪能力
 * 通过 Lua 比对草稿后删除，避免并发重放同一能力。
 */
@Service
public class VisitorFaceCropCapabilityServiceImpl implements VisitorFaceCropCapabilityService {
	private static final String DRAFT_KEY_PREFIX = "smart:admittance:visitor-face:draft:";
	private static final String CROP_KEY_PREFIX = "smart:admittance:visitor-face:crop:";
	private static final long DRAFT_TTL_SECONDS = 30L * 60L;
	private static final long CROP_TTL_SECONDS = 2L * 60L;
	private static final DefaultRedisScript<Long> CONSUME_CROP_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
			Long.class);

	private final StringRedisTemplate redisTemplate;
	private final Supplier<String> tokenSupplier;

	public VisitorFaceCropCapabilityServiceImpl(StringRedisTemplate redisTemplate) {
		this(redisTemplate, () -> UUID.randomUUID().toString().replace("-", ""));
	}

	VisitorFaceCropCapabilityServiceImpl(StringRedisTemplate redisTemplate, Supplier<String> tokenSupplier) {
		this.redisTemplate = redisTemplate;
		this.tokenSupplier = tokenSupplier;
	}

	@Override
	public VisitorFaceDraftCredential issueDraft(String openId) {
		if (!StringUtils.hasText(openId)) {
			throw forbidden();
		}
		String draftToken = nextToken();
		String draftId = nextToken();
		redisTemplate.opsForValue().set(draftKey(draftToken), hashOpenId(openId) + "|" + draftId,
				DRAFT_TTL_SECONDS, TimeUnit.SECONDS);
		return new VisitorFaceDraftCredential(draftToken, draftId);
	}

	@Override
	public String issueCropCapability(String draftToken, String draftId) {
		String expectedDraftId = draftIdFromSession(draftToken);
		if (!StringUtils.hasText(draftId) || !draftId.equals(expectedDraftId)) {
			throw forbidden();
		}
		String capability = nextToken();
		redisTemplate.opsForValue().set(cropKey(capability), draftId, CROP_TTL_SECONDS, TimeUnit.SECONDS);
		return capability;
	}

	@Override
	public void consumeCropCapability(String capability, String draftId) {
		if (!StringUtils.hasText(capability) || !StringUtils.hasText(draftId)) {
			throw forbidden();
		}
		Long consumed = redisTemplate.execute(CONSUME_CROP_SCRIPT, Collections.singletonList(cropKey(capability)), draftId);
		if (!Long.valueOf(1L).equals(consumed)) {
			throw forbidden();
		}
	}

	private String draftIdFromSession(String draftToken) {
		if (!StringUtils.hasText(draftToken)) {
			throw forbidden();
		}
		String record = redisTemplate.opsForValue().get(draftKey(draftToken));
		if (!StringUtils.hasText(record)) {
			throw forbidden();
		}
		int separator = record.lastIndexOf('|');
		if (separator <= 0 || separator == record.length() - 1) {
			throw forbidden();
		}
		return record.substring(separator + 1);
	}

	private String nextToken() {
		String token = tokenSupplier.get();
		if (!StringUtils.hasText(token)) {
			throw new SmartException("访客身份服务不可用");
		}
		return token;
	}

	private String hashOpenId(String openId) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(openId.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte current : digest) {
				result.append(String.format("%02x", current));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new SmartException("访客身份服务不可用");
		}
	}

	private String draftKey(String token) {
		return DRAFT_KEY_PREFIX + token;
	}

	private String cropKey(String capability) {
		return CROP_KEY_PREFIX + capability;
	}

	private SmartException forbidden() {
		return new SmartException("访客人脸授权已失效，请重新进入申请流程");
	}
}
