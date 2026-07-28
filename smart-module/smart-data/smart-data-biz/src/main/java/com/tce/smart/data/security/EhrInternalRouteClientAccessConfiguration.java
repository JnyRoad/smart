package com.tce.smart.data.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 将精确服务调用方守卫注册到 SmartData 的全部 MVC 路由。 */
@Configuration
public class EhrInternalRouteClientAccessConfiguration implements WebMvcConfigurer {

	private final EhrInternalRouteClientAccessInterceptor interceptor;

	public EhrInternalRouteClientAccessConfiguration(EhrInternalRouteClientAccessInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor).addPathPatterns("/**");
	}
}
