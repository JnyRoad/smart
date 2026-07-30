package com.tce.smart.common.core.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Gateway 与业务服务之间的遗留调用证明协议。
 * <p>
 * 该协议不是外部客户端认证协议：外部请求先由 Gateway 按网络来源裁决，
 * Gateway 再使用仅部署在受控服务上的密钥生成证明。签名同时绑定方法、
 * 服务本地路径、原始查询字符串、调用方、来源 IP、时间窗口和 nonce，避免证明被
 * 转移到其他敏感接口或参数上使用。
 */
public final class LegacyCompatibilityProof {

	public static final String VERSION = "v1";
	public static final String HEADER_PREFIX = "X-Smart-Legacy-";
	public static final String HEADER_KEY_ID = HEADER_PREFIX + "Key-Id";
	public static final String HEADER_CALLER_ID = HEADER_PREFIX + "Caller";
	public static final String HEADER_SOURCE_IP = HEADER_PREFIX + "Source-Ip";
	public static final String HEADER_ISSUED_AT = HEADER_PREFIX + "Issued-At";
	public static final String HEADER_EXPIRES_AT = HEADER_PREFIX + "Expires-At";
	public static final String HEADER_NONCE = HEADER_PREFIX + "Nonce";
	public static final String HEADER_SIGNATURE = HEADER_PREFIX + "Signature";

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final long DEFAULT_MAX_CLOCK_SKEW_SECONDS = 30L;
	private static final long DEFAULT_MAX_TTL_SECONDS = 60L;

	private LegacyCompatibilityProof() {
	}

	/**
	 * 为证明声明生成 Base64URL 编码的 HMAC-SHA-256 签名。
	 *
	 * @param claims 待签名的请求声明
	 * @param signatureKey Gateway 与业务服务共享的环境密钥
	 * @return 不带填充字符的 Base64URL 签名
	 */
	public static String sign(Claims claims, String signatureKey) {
		try {
			return Base64.getUrlEncoder().withoutPadding().encodeToString(mac(claims, signatureKey));
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("遗留兼容证明签名失败", exception);
		}
	}

	/**
	 * 以常量时间比较方式验证签名和默认时间窗口。任何非法输入都返回 false，调用方
	 * 必须按拒绝处理；调用方仍须通过可靠的原子存储消费 nonce，完成重放防护。
	 */
	public static boolean verify(Claims claims, String signature, String signatureKey) {
		return verify(claims, signature, signatureKey, Instant.now().getEpochSecond(),
				DEFAULT_MAX_CLOCK_SKEW_SECONDS, DEFAULT_MAX_TTL_SECONDS);
	}

	/**
	 * 验证签名及请求时间窗口。显式传入当前时钟和策略值，保证业务服务可以与其
	 * Nacos 配置保持一致；时间窗口不可信时必须失败关闭。
	 *
	 * @param nowEpochSeconds 当前 UTC epoch 秒
	 * @param maxClockSkewSeconds 允许 Gateway 与业务服务的最大时钟偏差
	 * @param maxTtlSeconds 证明允许的最长存活时间
	 * @return 签名和时间窗口均有效时返回 true
	 */
	public static boolean verify(Claims claims, String signature, String signatureKey, long nowEpochSeconds,
			long maxClockSkewSeconds, long maxTtlSeconds) {
		if (!isWithinTimeWindow(claims, nowEpochSeconds, maxClockSkewSeconds, maxTtlSeconds)) {
			return false;
		}
		if (isBlank(signature)) {
			return false;
		}
		try {
			byte[] actual = Base64.getUrlDecoder().decode(signature);
			byte[] expected = mac(claims, signatureKey);
			return MessageDigest.isEqual(expected, actual);
		} catch (IllegalArgumentException | GeneralSecurityException exception) {
			return false;
		}
	}

	/**
	 * 判断签发时间、失效时间和配置的时钟偏差是否构成可接受的短期证明。
	 * 该方法不消费 nonce；nonce 必须由 Platform 使用原子 SET NX + TTL 单独消费。
	 */
	public static boolean isWithinTimeWindow(Claims claims, long nowEpochSeconds, long maxClockSkewSeconds,
			long maxTtlSeconds) {
		if (claims == null || nowEpochSeconds < 0 || maxClockSkewSeconds < 0 || maxTtlSeconds <= 0) {
			return false;
		}
		long issuedAt = claims.getIssuedAtEpochSeconds();
		long expiresAt = claims.getExpiresAtEpochSeconds();
		if (issuedAt < 0 || expiresAt < issuedAt || expiresAt - issuedAt > maxTtlSeconds) {
			return false;
		}
		if (issuedAt > nowEpochSeconds && issuedAt - nowEpochSeconds > maxClockSkewSeconds) {
			return false;
		}
		return expiresAt >= nowEpochSeconds || nowEpochSeconds - expiresAt <= maxClockSkewSeconds;
	}

	private static byte[] mac(Claims claims, String signatureKey) throws GeneralSecurityException {
		if (claims == null || isBlank(signatureKey)) {
			throw new IllegalArgumentException("遗留兼容证明参数不完整");
		}
		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		mac.init(new SecretKeySpec(signatureKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
		return mac.doFinal(claims.canonicalValue().getBytes(StandardCharsets.UTF_8));
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	/** Gateway 写入 Platform 的、已签名的请求声明。 */
	public static final class Claims {
		private final String version;
		private final String keyId;
		private final String callerId;
		private final String sourceIp;
		private final String method;
		private final String servicePath;
		private final String rawQuery;
		private final long issuedAtEpochSeconds;
		private final long expiresAtEpochSeconds;
		private final String nonce;

		private Claims(String version, String keyId, String callerId, String sourceIp, String method, String servicePath,
				String rawQuery, long issuedAtEpochSeconds, long expiresAtEpochSeconds, String nonce) {
			this.version = required(version, "版本");
			this.keyId = required(keyId, "密钥标识");
			this.callerId = required(callerId, "调用方标识");
			this.sourceIp = required(sourceIp, "来源 IP");
			this.method = required(method, "请求方法");
			this.servicePath = required(servicePath, "服务路径");
			this.rawQuery = optionalWithoutLineBreak(rawQuery, "查询参数");
			this.issuedAtEpochSeconds = issuedAtEpochSeconds;
			this.expiresAtEpochSeconds = expiresAtEpochSeconds;
			this.nonce = required(nonce, "随机数");
			if (!servicePath.startsWith("/") || servicePath.indexOf('?') >= 0 || issuedAtEpochSeconds < 0
					|| expiresAtEpochSeconds < issuedAtEpochSeconds) {
				throw new IllegalArgumentException("遗留兼容证明路径或时间窗口非法");
			}
		}

		public static Claims of(String version, String keyId, String callerId, String sourceIp, String method,
				String servicePath, String rawQuery, long issuedAtEpochSeconds, long expiresAtEpochSeconds, String nonce) {
			return new Claims(version, keyId, callerId, sourceIp, method, servicePath, rawQuery,
					issuedAtEpochSeconds, expiresAtEpochSeconds, nonce);
		}

		public String getVersion() {
			return version;
		}

		public String getKeyId() {
			return keyId;
		}

		public String getCallerId() {
			return callerId;
		}

		public String getSourceIp() {
			return sourceIp;
		}

		public String getMethod() {
			return method;
		}

		public String getServicePath() {
			return servicePath;
		}

		public String getRawQuery() {
			return rawQuery;
		}

		public long getIssuedAtEpochSeconds() {
			return issuedAtEpochSeconds;
		}

		public long getExpiresAtEpochSeconds() {
			return expiresAtEpochSeconds;
		}

		public String getNonce() {
			return nonce;
		}

		private String canonicalValue() {
			return version + '\n' + keyId + '\n' + callerId + '\n' + sourceIp + '\n'
					+ issuedAtEpochSeconds + '\n' + expiresAtEpochSeconds + '\n' + nonce + '\n'
					+ method + '\n' + servicePath + '\n' + rawQuery;
		}

		private static String required(String value, String fieldName) {
			if (isBlank(value) || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
				throw new IllegalArgumentException("遗留兼容证明" + fieldName + "非法");
			}
			return value;
		}

		private static String optionalWithoutLineBreak(String value, String fieldName) {
			String normalized = value == null ? "" : value;
			if (normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0) {
				throw new IllegalArgumentException("遗留兼容证明" + fieldName + "非法");
			}
			return normalized;
		}
	}
}
