package com.tce.smart.common.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface SmartUserDetailsService extends UserDetailsService {

	/**
	 * 使用显式工号密码认证，不依赖 Servlet 请求参数。
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @return UserDetails
	 */
	UserDetails authenticate(String username, String password);

	/**
	 * 根据社交登录code 登录
	 *
	 * @param code TYPE@CODE
	 * @return UserDetails
	 * @throws UsernameNotFoundException
	 */
	UserDetails loadUserBySocial(String code);

	/**
	 * 根据人脸登录
	 *
	 * @param userName 用户名
	 * @return UserDetails
	 */
	UserDetails loadUserByFace(String userName);

	/**
	 * 根据手机号码登录
	 *
	 * @param mobile 手机号码
	 * @return UserDetails
	 */
	UserDetails loadUserByMobile(String mobile);

	/**
	 * 根据微信公众号登陆
	 * @param badge
	 * @return
	 */
	UserDetails loadUserByBadge(String badge);
}
