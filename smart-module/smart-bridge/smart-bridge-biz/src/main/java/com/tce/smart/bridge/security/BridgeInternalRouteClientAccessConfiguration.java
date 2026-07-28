package com.tce.smart.bridge.security;

import com.tce.smart.common.security.internal.InternalServerRouteClientAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 将共享精确调用方守卫注册到普通 Bridge 的全部 MVC 路由。 */
@Configuration
public class BridgeInternalRouteClientAccessConfiguration implements WebMvcConfigurer {

	private final InternalServerRouteClientAccessInterceptor interceptor;

	public BridgeInternalRouteClientAccessConfiguration(InternalServerRouteClientAccessInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor).addPathPatterns("/**");
	}
}
