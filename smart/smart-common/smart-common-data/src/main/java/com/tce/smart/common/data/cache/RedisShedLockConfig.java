package com.tce.smart.common.data.cache;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * @ClassName: ShedLockConfig
 * @Package com.tce.smart.yunan.lock.config
 * @Description:
 * @Author wuxinjian
 * @Date 2019-08-08 16:46
 * @Version V1.0
 */
@Configuration
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnMissingBean(LockProvider.class)
public class RedisShedLockConfig {
    @Bean
    public LockProvider lockProvider(RedisTemplate redisTemplate){
        return new RedisLockProvider(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
    }
}
