package com.tce.smart.common.security.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

@Slf4j
public class SmartPreAuthenticationChecks implements UserDetailsChecker {
	private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	@Override
	public void check(UserDetails user) {
		if (!user.isAccountNonLocked()) {
			log.error("User account is locked");
			throw new LockedException(
					messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked")
			);
		}

		if (!user.isEnabled()) {
			log.error("User account is disabled");
			throw new DisabledException(
					messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
		}

		if (!user.isAccountNonExpired()) {
			log.error("User account is expired");
			throw new AccountExpiredException(
					messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
		}
	}
}
