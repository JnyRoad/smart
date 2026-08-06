package com.tce.smart.common.security.mobile;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.admin.api.feign.RemoteAppSmsService;
import com.tce.smart.admin.api.feign.RemoteUserService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.SpringContextHolder;
import com.tce.smart.common.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 手机号登录验证filter
 */
@Slf4j
public class MobileAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	@Getter
	@Setter
	private boolean postOnly = true;
	@Getter
	@Setter
	private AuthenticationEventPublisher eventPublisher;
	@Getter
	@Setter
	private AuthenticationEntryPoint authenticationEntryPoint;

	private Charset charSet;

	public MobileAuthenticationFilter() {
		super(new AntPathRequestMatcher(SecurityConstants.MOBILE_TOKEN_URL, "POST"));
	}

	@Override
	@SneakyThrows
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		Authentication authResult = null;
		try {
			String mobile = null;
			String requestBodyStr = getRequestPostStr(request);
			if (StringUtils.isNotBlank(requestBodyStr)) {
				JSONObject resultJson = JSONUtil.parseObj(requestBodyStr.replace("\"", "'"));
				mobile = Objects.isNull(resultJson.get("mobile")) ? null
						: String.valueOf(resultJson.get("mobile"));
				if (StringUtils.isEmpty(mobile)) {
					throw new TCEException("手机号码为空");
				}
				String smsCode = Objects.isNull(resultJson.get("code")) ? null
						: String.valueOf(resultJson.get("code"));
				if (StringUtils.isBlank(smsCode)) {
					throw new TCEException("验证码为空");
				}

				RemoteAppSmsService remoteAppSmsService = SpringContextHolder.getBean(RemoteAppSmsService.class);
				Result<Boolean> result = null;
				try {
					result = remoteAppSmsService.verifySmsCode(mobile.trim(), smsCode.trim());
				}catch(Exception e) {
					log.error("验证码未通过",e);
					throw new TCEException("短信码已失效");
				}

				if (!result.isSuccess()){
					throw new TCEException("短信码已失效");
				}

				RemoteUserService remoteUserService = SpringContextHolder.getBean(RemoteUserService.class);
				try {
					result = remoteUserService.verifyMobile(mobile.trim(),SecurityConstants.FROM_IN);
				}catch(Exception e) {
					log.error("验证码登录失败",e);
					throw new TCEException(e.getMessage());
				}

				if (!result.isSuccess()){
					throw new TCEException(result.getMsg());
				}

			}

			MobileAuthenticationToken mobileAuthenticationToken = new MobileAuthenticationToken(mobile);

			setDetails(request, mobileAuthenticationToken);

			authResult = this.getAuthenticationManager().authenticate(mobileAuthenticationToken);

			log.debug("Authentication success: " + authResult);
			SecurityContextHolder.getContext().setAuthentication(authResult);

		} catch (Exception failed) {
			SecurityContextHolder.clearContext();
			log.debug("Authentication request failed: " + failed);

			eventPublisher.publishAuthenticationFailure(new BadCredentialsException(failed.getMessage(), failed),
					new PreAuthenticatedAuthenticationToken("access-token", "N/A"));

			try {
				authenticationEntryPoint.commence(request, response,
						new UsernameNotFoundException(failed.getMessage(), failed));
			} catch (Exception e) {
				log.error("authenticationEntryPoint handle error:{}", failed);
			}
		}

		return authResult;
	}

	private void setDetails(HttpServletRequest request,
							MobileAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}

	private String getRequestPostStr(HttpServletRequest request) throws IOException {
		String charSetStr = request.getCharacterEncoding();
		if (charSetStr == null) {
			charSetStr = "UTF-8";
		}
		charSet = Charset.forName(charSetStr);

		return StreamUtils.copyToString(request.getInputStream(), charSet);
	}
}
