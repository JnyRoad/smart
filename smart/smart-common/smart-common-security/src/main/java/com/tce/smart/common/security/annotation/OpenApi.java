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

	/**
	 * 迁移期临时兼容的旧 scope。新接口必须只声明 {@link #value()}；仅在已制定回收计划的
	 * 存量调用迁移期间填写本属性，迁移完成后应删除，不能将其当作长期多权限授权机制。
	 *
	 * <p>新增带默认值的注解属性可保持既有已编译 Controller 的二进制兼容，避免滚动发布时
	 * 将 {@code value()} 从单值改为数组造成注解元数据不兼容。</p>
	 */
	String[] compatibilityScopes() default {};
}
