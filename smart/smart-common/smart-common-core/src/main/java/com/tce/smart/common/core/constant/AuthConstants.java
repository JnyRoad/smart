package com.tce.smart.common.core.constant;

/**
 * @author zhangzi
 * @create 2023-07-13 11:34
 */
public class AuthConstants {

    /**
     * redis锁定前缀
     */
    public static final String REDIS_KEY_PREFIX = "lock_user_";

    /**
     * 最大锁定限制
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 限制锁定时间
     */
    public static final int MAX_LOCK_TIME = 10;
}
