package com.tce.smart.schedule.config;

import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 能耗投影定时任务获取内部服务访问令牌的运行配置。
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
	/**
	 * 通用服务取令牌方法使用的授权域，内部调用默认统一使用 server。
	 */
	private String scope = OpenApiScopeCatalog.SERVER;
	/** 能耗投影默认使用 server；保留原配置键以兼容已配置细分权限的调用方。 */
	private String energyProjectionRunScope = OpenApiScopeCatalog.SERVER;
	/** 在访问令牌过期前提前刷新的秒数。 */
	private long refreshBeforeExpirySeconds = 60L;
}
