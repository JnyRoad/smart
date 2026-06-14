package com.tce.smart.common.data.ratelimit;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
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
 * <p>{@link MeterRegistry} 通过 {@link ObjectProvider} 可选注入：有 actuator/micrometer 则上报指标，
 * 没有则静默降级，不强制下游引入监控依赖。</p>
 */
@Configuration
@ConditionalOnBean(StringRedisTemplate.class)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RateLimitService rateLimitService(StringRedisTemplate stringRedisTemplate,
											 RateLimitProperties rateLimitProperties,
											 ObjectProvider<MeterRegistry> meterRegistry) {
		return new RateLimitService(stringRedisTemplate, rateLimitProperties, meterRegistry);
	}
}
