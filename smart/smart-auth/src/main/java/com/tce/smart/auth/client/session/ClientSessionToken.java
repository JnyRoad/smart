package com.tce.smart.auth.client.session;

/** 认证层向 HTTP 适配层返回的最小会话投影。 */
public final class ClientSessionToken {
	private final String value;
	private final long expiresAt;
	ClientSessionToken(String value, long expiresAt) { this.value = value; this.expiresAt = expiresAt; }
	public String getValue() { return value; }
	public long getExpiresAt() { return expiresAt; }
}
