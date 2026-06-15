package com.tce.smart.common.data.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 限流服务自动配置。
 *
 * <p>仅当容器中存在 {@link StringRedisTemplate}（即应用已启用 Redis）时才装配限流服务，
 * 否则整体不生效——避免在无 Redis 的服务里因找不到依赖而启动失败。</p>
 *
 * <p>限流服务只依赖 Redis 与配置，不依赖 micrometer/actuator，观测仅通过日志，
 * 避免下游缺少监控依赖时启动报 {@code NoClassDefFoundError}。</p>
 */
@Configuration
@ConditionalOnBean(StringRedisTemplate.class)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RateLimitService rateLimitService(StringRedisTemplate stringRedisTemplate,
											 RateLimitProperties rateLimitProperties) {
		return new RateLimitService(stringRedisTemplate, rateLimitProperties);
	}
}
