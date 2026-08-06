package com.tce.smart.platform.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 公开简历登记流程的人脸裁剪 capability。
 *
 * 不把图片内容写入 Redis：初始 capability 仅绑定应聘记录，裁剪后的保存 capability
 * 仅绑定应聘记录和图片摘要。两个 capability 都是短时、单用途的 HttpOnly Cookie。
 */
@Service
public class ResumeFaceCropCapabilityService {

	private static final String INITIAL_KEY_PREFIX = "smart:resume:face-crop:init:";
	private static final String SAVE_KEY_PREFIX = "smart:resume:face-crop:save:";
	private static final String INITIAL_COOKIE = "resume_face_crop";
	private static final String SAVE_COOKIE = "resume_face_save";
	private static final long CAPABILITY_TTL_SECONDS = 10 * 60;
	private static final int TOKEN_BYTES = 32;
	private static final String TAKE_AND_DELETE_LUA = "local value = redis.call('GET', KEYS[1]); "
			+ "if value then redis.call('DEL', KEYS[1]); end; return value;";

	private final StringRedisTemplate redisTemplate;
	private final SecureRandom secureRandom = new SecureRandom();
	private final DefaultRedisScript<String> takeAndDeleteScript = new DefaultRedisScript<>(TAKE_AND_DELETE_LUA, String.class);

	public ResumeFaceCropCapabilityService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/** 在身份证资料成功保存后签发仅能用于一次裁剪的 capability。 */
	public void issueCropCapability(HttpServletResponse response, Long applicationId) {
		if (applicationId == null) {
			throw new IllegalArgumentException("应聘记录不能为空");
		}
		String token = createToken();
		redisTemplate.opsForValue().set(INITIAL_KEY_PREFIX + token, applicationId.toString(),
				CAPABILITY_TTL_SECONDS, TimeUnit.SECONDS);
		addCapabilityCookie(response, INITIAL_COOKIE, token, "/platform/regist/face/crop");
	}

	/** 原子消费裁剪 capability，避免同一公开上下文并发处理多张人脸。 */
	public Long consumeCropCapability(String token) {
		String applicationId = take(INITIAL_KEY_PREFIX, token);
		try {
			return Long.valueOf(applicationId);
		} catch (NumberFormatException e) {
			throw denied();
		}
	}

	/** 裁剪成功后签发保存 capability，只允许相同应聘记录保存同一张裁剪图。 */
	public void issueSaveCapability(HttpServletResponse response, Long applicationId, String croppedImage) {
		if (applicationId == null || croppedImage == null) {
			throw denied();
		}
		String token = createToken();
		String binding = binding(applicationId, croppedImage);
		redisTemplate.opsForValue().set(SAVE_KEY_PREFIX + token, binding,
				CAPABILITY_TTL_SECONDS, TimeUnit.SECONDS);
		addCapabilityCookie(response, SAVE_COOKIE, token, "/platform/regist/face/add");
	}

	/** 原子消费保存 capability，拒绝跨应聘记录或替换图片的请求。 */
	public void consumeSaveCapability(String token, Long applicationId, String croppedImage) {
		String actual = take(SAVE_KEY_PREFIX, token);
		String expected = applicationId == null || croppedImage == null ? null : binding(applicationId, croppedImage);
		if (expected == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8))) {
			throw denied();
		}
	}

	private String take(String prefix, String token) {
		if (token == null || token.isEmpty()) {
			throw denied();
		}
		String value = redisTemplate.execute(takeAndDeleteScript, Collections.singletonList(prefix + token));
		if (value == null || value.isEmpty()) {
			throw denied();
		}
		return value;
	}

	private String binding(Long applicationId, String image) {
		return applicationId + ":" + sha256(image);
	}

	private String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 不可用", e);
		}
	}

	private String createToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private void addCapabilityCookie(HttpServletResponse response, String name, String value, String path) {
		response.addHeader("Set-Cookie", name + "=" + value + "; Max-Age=" + CAPABILITY_TTL_SECONDS
				+ "; Path=" + path + "; HttpOnly; Secure; SameSite=Strict");
	}

	private AccessDeniedException denied() {
		return new AccessDeniedException("简历人脸处理上下文无效或已过期");
	}
}
