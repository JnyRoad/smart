package com.tce.smart.common.security.face;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.security.component.ResourceAuthExceptionEntryPoint;
import com.tce.smart.common.security.service.SmartUserDetailsService;

import lombok.Getter;
import lombok.Setter;

/**
 * 人脸登录配置入口
 */
@Getter
@Setter
@Component
public class FaceSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private AuthenticationEventPublisher defaultAuthenticationEventPublisher;
	private AuthenticationSuccessHandler faceLoginSuccessHandler;
	private SmartUserDetailsService userDetailsService;

	@Override
	public void configure(HttpSecurity http) {
		FaceAuthenticationFilter faceAuthenticationFilter = new FaceAuthenticationFilter();
		faceAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		faceAuthenticationFilter.setAuthenticationSuccessHandler(faceLoginSuccessHandler);
		faceAuthenticationFilter.setEventPublisher(defaultAuthenticationEventPublisher);
		faceAuthenticationFilter.setAuthenticationEntryPoint(new ResourceAuthExceptionEntryPoint(objectMapper));

		FaceAuthenticationProvider faceAuthenticationProvider = new FaceAuthenticationProvider();
		faceAuthenticationProvider.setUserDetailsService(userDetailsService);
		http.authenticationProvider(faceAuthenticationProvider).addFilterAfter(faceAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class);
	}
}
