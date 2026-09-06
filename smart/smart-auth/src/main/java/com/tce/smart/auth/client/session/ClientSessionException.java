package com.tce.smart.auth.client.session;

/** 固定状态的 App 登录失败，避免回显认证中心、UPMS 或凭据细节。 */
public final class ClientSessionException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int status;
	ClientSessionException(int status) { super("App 会话请求未完成"); this.status = status; }
	int getStatus() { return status; }
}
