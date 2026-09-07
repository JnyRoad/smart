package com.tce.smart.platform.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 权限治理写动作默认关闭；尝试上限基于目标持久 attemptNo，不按管理请求重置。 */
@Data
@Component
@ConfigurationProperties(prefix = "smart.auth-governance")
public class AuthOperationGovernanceProperties {

	private boolean actionsEnabled = false;
	private int maxRetryAttempts = 3;
	private int maxObservationAgeDays = 30;

	public int checkedMaxRetryAttempts() {
		if (maxRetryAttempts < 1 || maxRetryAttempts > 10) {
			throw new IllegalStateException("治理最大尝试次数必须为1至10");
		}
		return maxRetryAttempts;
	}

	public int checkedMaxObservationAgeDays() {
		if (maxObservationAgeDays < 1 || maxObservationAgeDays > 365) {
			throw new IllegalStateException("人工观察有效天数必须为1至365");
		}
		return maxObservationAgeDays;
	}
}
