package com.tce.smart.common.security.annotation;

import com.tce.smart.common.security.component.SmartResourceServerAutoConfiguration;
import com.tce.smart.common.security.component.SmartSecurityBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import java.lang.annotation.*;

/**
 * 资源服务注解
 */
@Documented
@Inherited
@EnableResourceServer
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({SmartResourceServerAutoConfiguration.class, SmartSecurityBeanDefinitionRegistrar.class})
public @interface EnableSmartResourceServer {

}
