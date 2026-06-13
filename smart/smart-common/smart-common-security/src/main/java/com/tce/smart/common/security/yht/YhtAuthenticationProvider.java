package com.tce.smart.common.security.yht;

import com.tce.smart.common.security.component.SmartPreAuthenticationChecks;
import com.tce.smart.common.security.service.SmartUserDetailsService;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * 友互通登陆校验逻辑
 */
@Slf4j
public class YhtAuthenticationProvider implements AuthenticationProvider {
	private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	private final UserDetailsChecker detailsChecker = new SmartPreAuthenticationChecks();

	@Getter
	@Setter
	private SmartUserDetailsService userDetailsService;

	@Override
	@SneakyThrows
	public Authentication authenticate(Authentication authentication) {
		YhtAuthenticationToken authenticationToken0 = (YhtAuthenticationToken) authentication;

		String principal = authenticationToken0.getPrincipal().toString();
		UserDetails userDetails = userDetailsService.loadUserByBadge(principal);
		if (userDetails == null) {
			log.debug("Authentication failed: no credentials provided");
			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.noopBindAccount",
					"Noop Bind Account"));
		}

		// 检查账号状态
		detailsChecker.check(userDetails);

		YhtAuthenticationToken authenticationToken = new YhtAuthenticationToken(userDetails, userDetails.getAuthorities());
		authenticationToken.setDetails(authenticationToken.getDetails());
		return authenticationToken;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return YhtAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
