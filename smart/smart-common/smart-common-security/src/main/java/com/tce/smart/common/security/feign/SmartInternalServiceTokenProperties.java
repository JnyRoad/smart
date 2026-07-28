package com.tce.smart.common.security.feign;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 显式标记内部调用使用的独立 OAuth 客户端配置，不能复用应用的用户 OAuth 资源。
 */
@Data
@ConfigurationProperties(prefix = "security.inner.service-token")
public class SmartInternalServiceTokenProperties {
	/** 客户端标识。 */
	private String clientId;
	/** 客户端密钥。 */
	private String clientSecret;
	/** 授权服务器的 client_credentials 令牌地址。 */
	private String accessTokenUri;
}
