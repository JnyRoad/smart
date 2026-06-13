package com.tce.smart.common.security.wxpublic;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.admin.api.feign.RemoteAppSmsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.SpringContextHolder;
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

/**
 * 微信公众号验证过滤器
 */
@Slf4j
public class WxPublicAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
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

	public WxPublicAuthenticationFilter() {
		super(new AntPathRequestMatcher(SecurityConstants.WX_PUBLIC_TOKEN_URL, "POST"));
	}

	@Override
	@SneakyThrows
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		Authentication authResult = null;
		try {
			String requestBodyStr = getRequestPostStr(request);
			if (StrUtil.isBlank(requestBodyStr)) {
				throw new TCEException("微信公众号登录参数为空");
			}
			JSONObject resultJson = JSONUtil.parseObj(requestBodyStr.replace("\"", "'"));
			String wxCode = resultJson.getStr("code");
			if (StrUtil.isBlank(wxCode)) {
				throw new TCEException("微信CODE码为空");
			}
//			String type = resultJson.getStr("type");
//			if (StrUtil.isBlank(type)) {
//				throw new TCEException("微信登录类型为空");
//			}
			// 通过微信code获取员工号
			String userName = null;
			RemoteAppSmsService remoteAppSmsService = SpringContextHolder.getBean(RemoteAppSmsService.class);
			Result<String> result = remoteAppSmsService.getBadgeByCode(wxCode);
			log.info("result remoteAppSmsService.getBadge======={}", result);
			if (!result.isSuccess()) {
				throw new TCEException("获取工号异常");
			}
			userName = result.getData();

			if (StrUtil.isBlank(userName)) {
				log.info("账号未绑定工号，请先绑定");
				throw new TCEException("账号未绑定工号，请先绑定");
			}

			WxPublicAuthenticationToken wxAuthenticationToken = new WxPublicAuthenticationToken(userName);

			setDetails(request, wxAuthenticationToken);

			authResult = this.getAuthenticationManager().authenticate(wxAuthenticationToken);

			logger.debug("WxPublic Authentication success: " + authResult);
			SecurityContextHolder.getContext().setAuthentication(authResult);
		} catch (Exception failed) {
			SecurityContextHolder.clearContext();
			logger.debug("WxPublic Authentication request failed: " + failed);

			eventPublisher.publishAuthenticationFailure(new BadCredentialsException(failed.getMessage(), failed),
					new PreAuthenticatedAuthenticationToken("access-token", "N/A"));

			try {
				authenticationEntryPoint.commence(request, response,
						new UsernameNotFoundException(failed.getMessage(), failed));
			} catch (Exception e) {
				logger.error("WxPublic authenticationEntryPoint handle error:{}", failed);
			}
		}

		return authResult;
	}

	private void setDetails(HttpServletRequest request, WxPublicAuthenticationToken authRequest) {
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
