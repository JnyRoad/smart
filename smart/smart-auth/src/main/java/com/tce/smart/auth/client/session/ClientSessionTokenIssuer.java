package com.tce.smart.auth.client.session;

import org.springframework.security.core.userdetails.UserDetails;

/** 令牌签发可替换，使 HTTP 登录不需要知道 OAuth 存储实现。 */
public interface ClientSessionTokenIssuer {
	ClientSessionToken issue(UserDetails subject);
}
