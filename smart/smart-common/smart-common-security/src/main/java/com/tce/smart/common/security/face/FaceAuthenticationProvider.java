package com.tce.smart.common.security.face;

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
 * 人脸登录校验逻辑
 */
@Slf4j
public class FaceAuthenticationProvider implements AuthenticationProvider {
	private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	private UserDetailsChecker detailsChecker = new SmartPreAuthenticationChecks();

	@Getter
	@Setter
	private SmartUserDetailsService userDetailsService;

	@Override
	@SneakyThrows
	public Authentication authenticate(Authentication authentication) {
		FaceAuthenticationToken faceAuthenticationToken = (FaceAuthenticationToken) authentication;

		String principal = faceAuthenticationToken.getPrincipal().toString();
		UserDetails userDetails = userDetailsService.loadUserByFace(principal);
		if (userDetails == null) {
			log.debug("Authentication failed: no credentials provided");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.noopBindAccount",
					"Noop Bind Account"));
		}

		// 检查账号状态
		detailsChecker.check(userDetails);

		FaceAuthenticationToken authenticationToken = new FaceAuthenticationToken(userDetails, userDetails.getAuthorities());
		authenticationToken.setDetails(faceAuthenticationToken.getDetails());
		return authenticationToken;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return FaceAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
