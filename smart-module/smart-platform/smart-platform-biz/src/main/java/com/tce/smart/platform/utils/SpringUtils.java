package com.tce.smart.platform.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author sunfujian
 * @date 2021/7/20 17:42
 */
@Component
public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.appContext = applicationContext;
    }

    public static <T> T getBeanByType(Class<T> findClass) {
        return appContext.getBean(findClass);
    }
}
