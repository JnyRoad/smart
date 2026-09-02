package com.tce.smart.schedule.config;

import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 能耗投影定时任务获取最小 capability scope 访问令牌的运行配置。
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
	 * 旧版无参取令牌方法使用的历史 scope，仅服务于未迁移调用方的二进制兼容；
	 * 新的能耗投影任务不得使用本字段。
	 */
	private String scope = OpenApiScopeCatalog.LEGACY_SERVER;
	/** 能耗投影任务实际申请的最小能力 scope。 */
	private String energyProjectionRunScope = OpenApiScopeCatalog.ENERGY_PROJECTION_RUN;
	/** 在访问令牌过期前提前刷新的秒数。 */
	private long refreshBeforeExpirySeconds = 60L;
}
