package com.tce.smart.common.security.wxpublic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.security.component.ResourceAuthExceptionEntryPoint;
import com.tce.smart.common.security.service.SmartUserDetailsService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * 微信公众号登录配置入口
 */
@Getter
@Setter
@Component
public class WxPublicSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthenticationEventPublisher defaultAuthenticationEventPublisher;
	private AuthenticationSuccessHandler wxPublicLoginSuccessHandler;
	private SmartUserDetailsService userDetailsService;

	@Override
	public void configure(HttpSecurity http) {
		WxPublicAuthenticationFilter wxAuthenticationFilter = new WxPublicAuthenticationFilter();
		wxAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		wxAuthenticationFilter.setAuthenticationSuccessHandler(wxPublicLoginSuccessHandler);
		wxAuthenticationFilter.setEventPublisher(defaultAuthenticationEventPublisher);
		wxAuthenticationFilter.setAuthenticationEntryPoint(new ResourceAuthExceptionEntryPoint(objectMapper));

		WxPublicAuthenticationProvider wxAuthenticationProvider = new WxPublicAuthenticationProvider();
		wxAuthenticationProvider.setUserDetailsService(userDetailsService);
		http.authenticationProvider(wxAuthenticationProvider).addFilterAfter(wxAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class);
	}
}
