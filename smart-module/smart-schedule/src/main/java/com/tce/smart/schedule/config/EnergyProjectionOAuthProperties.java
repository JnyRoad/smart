package com.tce.smart.schedule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 能耗投影定时任务获取 server scope 访问令牌的运行配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart.energy.projection.oauth")
public class EnergyProjectionOAuthProperties {

	/** OAuth 服务的 client_credentials 授权端点。 */
	private String accessTokenUri;
	/** Scheduler 专用客户端标识。 */
	private String clientId;
	/** Scheduler 专用客户端密钥，只能通过环境变量或受控配置注入。 */
	private String clientSecret;
	/** 内部开放接口需要的 OAuth scope。 */
	private String scope = "server";
	/** 在访问令牌过期前提前刷新的秒数。 */
	private long refreshBeforeExpirySeconds = 60L;
}
