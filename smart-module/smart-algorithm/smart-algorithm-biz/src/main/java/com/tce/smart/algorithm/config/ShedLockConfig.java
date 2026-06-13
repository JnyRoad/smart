package com.tce.smart.algorithm.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: ShedLockConfig
 * @Package com.tce.smart.yunan.lock.config
 * @Description:
 * @Author wuxinjian
 * @Date 2019-08-08 16:46
 * @Version V1.0
 */
@Configuration
//@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class ShedLockConfig {
    @Bean
    public LockProvider lockProvider(RedisTemplate redisTemplate){
        return new RedisLockProvider(redisTemplate.getConnectionFactory());
    }
}
