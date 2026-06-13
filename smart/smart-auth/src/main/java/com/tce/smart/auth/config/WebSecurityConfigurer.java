package com.tce.smart.auth.config;

import com.tce.smart.common.security.handler.WxPublicLoginSuccessHandler;
import com.tce.smart.common.security.handler.YhtLoginSuccessHandler;
import com.tce.smart.common.security.wxpublic.WxPublicSecurityConfigurer;
import com.tce.smart.common.security.yht.YhtSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.security.face.FaceSecurityConfigurer;
import com.tce.smart.common.security.handler.FaceLoginSuccessHandler;
import com.tce.smart.common.security.handler.MobileLoginSuccessHandler;
import com.tce.smart.common.security.mobile.MobileSecurityConfigurer;
import com.tce.smart.common.security.service.SmartUserDetailsService;

import lombok.SneakyThrows;

/**
 * 认证相关配置
 */
@Primary
@Order(90)
@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ClientDetailsService clientDetailsService;
	@Autowired
	private SmartUserDetailsService userDetailsService;
	@Lazy
	@Autowired
	private AuthorizationServerTokenServices defaultAuthorizationServerTokenServices;

	@Override
	@SneakyThrows
	protected void configure(HttpSecurity http) {
		http.formLogin()
			//	.failureHandler(customAuthenticationFailureHandler())
			.loginPage("/token/login")
			.loginProcessingUrl("/token/form")
			.and()
			.authorizeRequests()
			.antMatchers(
				"/token/**",
				"/actuator/**",
				"/mobile/token/**",
				"/ocr/token/face/**").permitAll()
			.anyRequest().authenticated()
			.and().csrf().disable()
				.apply(mobileSecurityConfigurer())
				.and().apply(faceSecurityConfigurer())
				.and().apply(wxPublicSecurityConfigurer())
				.and().apply(yhtSecurityConfigurer());
	}

	@Bean
	public AuthenticationFailureHandler customAuthenticationFailureHandler() {
		return new CustomAuthenticationFailureHandler();
	}

	/**
	 * 不拦截静态资源
	 *
	 * @param web
	 */
	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers("/css/**");
	}

	@Bean
	@Override
	@SneakyThrows
	public AuthenticationManager authenticationManagerBean() {
		return super.authenticationManagerBean();
	}

	@Bean
	public AuthenticationSuccessHandler mobileLoginSuccessHandler() {
		return MobileLoginSuccessHandler.builder()
			.objectMapper(objectMapper)
			.clientDetailsService(clientDetailsService)
			.passwordEncoder(passwordEncoder())
			.defaultAuthorizationServerTokenServices(defaultAuthorizationServerTokenServices).build();
	}

	@Bean
	public MobileSecurityConfigurer mobileSecurityConfigurer() {
		MobileSecurityConfigurer mobileSecurityConfigurer = new MobileSecurityConfigurer();
		mobileSecurityConfigurer.setMobileLoginSuccessHandler(mobileLoginSuccessHandler());
		mobileSecurityConfigurer.setUserDetailsService(userDetailsService);
		return mobileSecurityConfigurer;
	}

	@Bean
	public AuthenticationSuccessHandler faceLoginSuccessHandler() {
		return FaceLoginSuccessHandler.builder()
			.objectMapper(objectMapper)
			.clientDetailsService(clientDetailsService)
			.passwordEncoder(passwordEncoder())
			.defaultAuthorizationServerTokenServices(defaultAuthorizationServerTokenServices).build();
	}

	@Bean
	public FaceSecurityConfigurer faceSecurityConfigurer() {
		FaceSecurityConfigurer faceSecurityConfigurer = new FaceSecurityConfigurer();
		faceSecurityConfigurer.setFaceLoginSuccessHandler(faceLoginSuccessHandler());
		faceSecurityConfigurer.setUserDetailsService(userDetailsService);
		return faceSecurityConfigurer;
	}

	@Bean
	public AuthenticationSuccessHandler wxPublicLoginSuccessHandler() {
		return WxPublicLoginSuccessHandler.builder()
				.objectMapper(objectMapper)
				.clientDetailsService(clientDetailsService)
				.passwordEncoder(passwordEncoder())
				.defaultAuthorizationServerTokenServices(defaultAuthorizationServerTokenServices).build();
	}

	@Bean
	public WxPublicSecurityConfigurer wxPublicSecurityConfigurer() {
		WxPublicSecurityConfigurer wxSecurityConfigurer = new WxPublicSecurityConfigurer();
		wxSecurityConfigurer.setWxPublicLoginSuccessHandler(wxPublicLoginSuccessHandler());
		wxSecurityConfigurer.setUserDetailsService(userDetailsService);
		return wxSecurityConfigurer;
	}

	@Bean
	public AuthenticationSuccessHandler yhtLoginSuccessHandler() {
		return YhtLoginSuccessHandler.builder()
				.objectMapper(objectMapper)
				.clientDetailsService(clientDetailsService)
				.passwordEncoder(passwordEncoder())
				.defaultAuthorizationServerTokenServices(defaultAuthorizationServerTokenServices).build();
	}

	@Bean
	public YhtSecurityConfigurer yhtSecurityConfigurer() {
		YhtSecurityConfigurer securityConfigurer = new YhtSecurityConfigurer();
		securityConfigurer.setYhtLoginSuccessHandler(yhtLoginSuccessHandler());
		securityConfigurer.setUserDetailsService(userDetailsService);
		return securityConfigurer;
	}


	/**
	 * https://spring.io/blog/2017/11/01/spring-security-5-0-0-rc1-released#password-storage-updated
	 * Encoded password does not look like BCrypt
	 *
	 * @return PasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
