package com.tce.smart.common.swagger.annotation;

import com.tce.smart.common.swagger.config.SwaggerAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 swagger
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SwaggerAutoConfiguration.class})
public @interface EnableSmartSwagger2 {
}
