package com.tce.smart.common.security.yht;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.admin.api.feign.RemoteAppSmsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
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
import java.util.Objects;

/**
 * 友互通验证过滤器
 */
@Slf4j
public class YhtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
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

	public YhtAuthenticationFilter() {
		super(new AntPathRequestMatcher(SecurityConstants.YHT_TOKEN_URL, "POST"));
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
				throw new TCEException("友互通登陆参数为空");
			}
			JSONObject resultJson = JSONUtil.parseObj(requestBodyStr.replace("\"", "'"));
			String authCode = Objects.isNull(resultJson.get("code")) ? null : String.valueOf(resultJson.get("code"));
			if (StrUtil.isBlank(authCode)) {
				throw new TCEException("友互通CODE码为空");
			}
			// 通过友互通code获取员工号
			String userName = null;
			RemoteAppSmsService remoteAppSmsService = SpringContextHolder.getBean(RemoteAppSmsService.class);
			Result<String> result = remoteAppSmsService.getUserBadge(authCode);
			log.info("result remoteAppSmsService.getUserBadge======={}", result);
			if (!result.isSuccess()) {
				throw new TCEException("获取工号异常");
			}
			userName = result.getData();

			if (userName == null) {
				throw new SmartException("暂未绑定用友APP，请先绑定");
			}

			YhtAuthenticationToken authenticationToken = new YhtAuthenticationToken(userName);

			setDetails(request, authenticationToken);

			authResult = this.getAuthenticationManager().authenticate(authenticationToken);

			logger.debug("YHT Authentication success: " + authResult);
			SecurityContextHolder.getContext().setAuthentication(authResult);
		} catch (Exception failed) {
			SecurityContextHolder.clearContext();
			logger.debug("YHT Authentication request failed: " + failed);

			eventPublisher.publishAuthenticationFailure(new BadCredentialsException(failed.getMessage(), failed),
					new PreAuthenticatedAuthenticationToken("access-token", "N/A"));

			try {
				authenticationEntryPoint.commence(request, response,
						new UsernameNotFoundException(failed.getMessage(), failed));
			} catch (Exception e) {
				logger.error("YHT authenticationEntryPoint handle error:{}", failed);
			}
		}

		return authResult;
	}

	private void setDetails(HttpServletRequest request, YhtAuthenticationToken authRequest) {
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
