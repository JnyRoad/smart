package com.tce.smart.platform.security;

import com.tce.smart.common.data.security.RedisNonceReplayGuard;
import com.tce.smart.platform.conf.LegacyDoorLockCompatibilityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/** 将遗留兼容过滤器放在 OAuth 白名单处理之前，确保 ignore-urls 不会跳过验签。 */
@Configuration
public class LegacyDoorLockCompatibilityFilterConfiguration {

	@Bean
	public FilterRegistrationBean<LegacyDoorLockCompatibilityFilter> legacyDoorLockCompatibilityFilter(
			LegacyDoorLockCompatibilityProperties properties, RedisNonceReplayGuard replayGuard) {
		FilterRegistrationBean<LegacyDoorLockCompatibilityFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new LegacyDoorLockCompatibilityFilter(properties, replayGuard));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
