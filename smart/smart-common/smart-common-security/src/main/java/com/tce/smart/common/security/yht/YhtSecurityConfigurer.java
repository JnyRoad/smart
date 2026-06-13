package com.tce.smart.common.security.yht;

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
 * 友互通登录配置入口
 */
@Getter
@Setter
@Component
public class YhtSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthenticationEventPublisher defaultAuthenticationEventPublisher;
	private AuthenticationSuccessHandler yhtLoginSuccessHandler;
	private SmartUserDetailsService userDetailsService;

	@Override
	public void configure(HttpSecurity http) {
		YhtAuthenticationFilter authenticationFilter = new YhtAuthenticationFilter();
		authenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		authenticationFilter.setAuthenticationSuccessHandler(yhtLoginSuccessHandler);
		authenticationFilter.setEventPublisher(defaultAuthenticationEventPublisher);
		authenticationFilter.setAuthenticationEntryPoint(new ResourceAuthExceptionEntryPoint(objectMapper));

		YhtAuthenticationProvider authenticationProvider = new YhtAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService);
		http.authenticationProvider(authenticationProvider).addFilterAfter(authenticationFilter,
				UsernamePasswordAuthenticationFilter.class);
	}
}
