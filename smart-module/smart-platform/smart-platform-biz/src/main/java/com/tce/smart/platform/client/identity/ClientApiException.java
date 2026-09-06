package com.tce.smart.platform.client.identity;

/** App 专用 API 的固定状态错误，不携带人员资料、权限规则或下游异常内容。 */
public final class ClientApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int status;
	public ClientApiException(int status) { super("App 客户端请求未完成"); this.status = status; }
	public int getStatus() { return status; }
}
