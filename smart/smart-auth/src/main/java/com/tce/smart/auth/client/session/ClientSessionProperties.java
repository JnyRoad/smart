package com.tce.smart.auth.client.session;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** App 会话签发只在显式配置独立 OAuth 客户端后开启，客户端永不持有该客户端密钥。 */
@ConfigurationProperties(prefix = "smart.client.session")
public class ClientSessionProperties {
	private boolean enabled;
	private String clientId;

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public String getClientId() { return clientId; }
	public void setClientId(String clientId) { this.clientId = clientId; }

	void validate() {
		if (!enabled || clientId == null || !clientId.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,95}")) {
			throw new ClientSessionException(503);
		}
	}
}
