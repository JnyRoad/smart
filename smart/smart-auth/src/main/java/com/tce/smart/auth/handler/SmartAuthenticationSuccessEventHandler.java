package com.tce.smart.auth.handler;

import com.tce.smart.common.core.constant.AuthConstants;
import com.tce.smart.common.security.handler.AbstractAuthenticationSuccessEventHandler;
import com.tce.smart.common.security.service.SmartUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmartAuthenticationSuccessEventHandler extends AbstractAuthenticationSuccessEventHandler {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 处理登录成功方法
     * <p>
     * 获取到登录的authentication 对象
     *
     * @param authentication 登录对象
     */
    @Override
    public void handle(Authentication authentication) {
        // 清除该账号登录限制次数
        String userName = authentication.getName();
        String redisLockKey = AuthConstants.REDIS_KEY_PREFIX + userName;
        Boolean delete = redisTemplate.delete(redisLockKey);
        log.info("用户: {} 清除账号登录限制次数: {}", userName, delete);
        log.info("用户: {}, 登录成功", ((SmartUser) authentication.getPrincipal()).getUsername());
    }
}
