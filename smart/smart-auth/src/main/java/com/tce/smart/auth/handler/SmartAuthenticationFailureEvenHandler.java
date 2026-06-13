package com.tce.smart.auth.handler;

import com.tce.smart.common.core.constant.AuthConstants;
import com.tce.smart.common.security.handler.AbstractAuthenticationFailureEvenHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SmartAuthenticationFailureEvenHandler extends AbstractAuthenticationFailureEvenHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 处理登录失败方法
     * <p>
     *
     * @param authenticationException 登录的authentication 对象
     * @param authentication          登录的authenticationException 对象
     */
    @Override
    public void handle(AuthenticationException authenticationException, Authentication authentication) {
        // key值(锁前缀+用户名)
        String userName = authentication.getName();
        String redisLockKey = AuthConstants.REDIS_KEY_PREFIX + userName;

        // 递增错误登录次数
        Long count = redisTemplate.opsForValue().increment(redisLockKey);
        log.info("登录失败用户: {} 失败次数: {}", userName, count);

        if (count != null && count == 1) {
            // 设置过期时间
            redisTemplate.expire(redisLockKey, AuthConstants.MAX_LOCK_TIME, TimeUnit.MINUTES);
        }
        log.error("用户：{} 登录失败，异常：{}", authentication.getPrincipal(), authenticationException.getLocalizedMessage());
    }
}
