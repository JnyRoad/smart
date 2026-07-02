package com.tce.smart.common.security.component;

import com.tce.smart.common.security.filter.SmartGlobalFilter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.web.client.RestTemplate;

/**
 * 1. 支持remoteTokenServices 负载均衡
 * 2. 支持 获取用户全部信息
 */
@Slf4j
public class SmartResourceServerConfigurerAdapter extends ResourceServerConfigurerAdapter {
	@Autowired
	protected ResourceAuthExceptionEntryPoint resourceAuthExceptionEntryPoint;
	@Autowired
	protected RemoteTokenServices remoteTokenServices;
	@Autowired
	private PermitAllUrlProperties permitAllUrlProperties;
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * 默认的配置，对外暴露
	 *
	 * @param httpSecurity
	 */
	@Override
	@SneakyThrows
	public void configure(HttpSecurity httpSecurity) {
		httpSecurity.addFilterAfter(new SmartGlobalFilter(), ChannelProcessingFilter.class);
		//允许使用iframe 嵌套，避免swagger-ui 不被加载的问题
		httpSecurity
				.headers()
				.frameOptions()
				.disable();
		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity.authorizeRequests();
		permitAllUrlProperties.getIgnoreUrls().forEach(url -> registry.antMatchers(url).permitAll());
		registry
				.anyRequest()
				.authenticated()
				.and()
				.csrf()
				.disable();
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		// 用 OpenApiAccessTokenConverter 替换默认实现：在标准转换结果基础上，
		// 额外把 app_park_ids claim 搬进 OAuth2Request 扩展，供开放 API 鉴权读取园区绑定信息；
		// userTokenConverter 装配行为保持不变。
		DefaultAccessTokenConverter accessTokenConverter = new OpenApiAccessTokenConverter();
		UserAuthenticationConverter userTokenConverter = new SmartUserAuthenticationConverter();
		accessTokenConverter.setUserTokenConverter(userTokenConverter);

		remoteTokenServices.setRestTemplate(restTemplate);
		remoteTokenServices.setAccessTokenConverter(accessTokenConverter);
		resources.authenticationEntryPoint(resourceAuthExceptionEntryPoint).tokenServices(remoteTokenServices);
	}
}
