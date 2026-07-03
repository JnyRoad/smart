package com.tce.smart.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开放 API 标注：仅允许持有对应 scope 的应用（client_credentials）token 访问。
 * 业务服务只需在 Controller 方法上加此注解即可接入开放 API 鉴权，无需额外配置
 * （拦截器由 {@code SmartResourceServerAutoConfiguration} 统一注册，全局生效）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenApi {

	/**
	 * 访问该接口所需的 scope，必填；client token 的 scope 集合必须包含此值才允许放行。
	 */
	String value();
}
