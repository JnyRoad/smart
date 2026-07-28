package com.tce.smart.common.security.face;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.SpringContextHolder;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.InternalStaffFaceLoginReqDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffFaceLoginRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
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
 * 人脸登录验证filter
 */
@Slf4j
public class FaceAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
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

	public FaceAuthenticationFilter() {
		super(new AntPathRequestMatcher(SecurityConstants.OCR_TOKEN_URL, "POST"));
	}

	@Override
	@SneakyThrows
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		Authentication authResult = null;
		try {
			String faceImage = null;
			String deviceNo = null;//设备编号
			try {
				String requestBodyStr = getRequestPostStr(request);
				if (StringUtils.isNotBlank(requestBodyStr)) {
					JSONObject resultJson = JSONUtil.parseObj(requestBodyStr.replace("\"", "'"));
					faceImage = Objects.isNull(resultJson.get("facePhoto")) ? null
							: String.valueOf(resultJson.get("facePhoto"));
					if (StringUtils.isEmpty(faceImage) || StringUtils.isBlank(faceImage)) {
						throw new TCEException("未包含人脸信息");
					}

					faceImage = faceImage.replaceAll("[\\t\\n\\r]", "");//替换换行符号
					deviceNo = Objects.isNull(resultJson.get("deviceNo")) ? null
							: String.valueOf(resultJson.get("deviceNo"));
					if (StringUtils.isEmpty(deviceNo) || StringUtils.isBlank(deviceNo)) {
						throw new TCEException("设备信息为空");
					}
				}
			} catch (IOException e) {
				log.error("人脸登陆信息", e);
			}

			// 人脸搜索员工信息
			String userName = null;
			RemoteStaffInternalService remoteStaffService = SpringContextHolder.getBean(RemoteStaffInternalService.class);
			InternalStaffFaceLoginReqDTO staffPerfectDTO = new InternalStaffFaceLoginReqDTO();
			staffPerfectDTO.setFacePic(faceImage);
			staffPerfectDTO.setDeviceNo(deviceNo);

			Result<InternalStaffFaceLoginRespDTO> result = null;
			try {
				result = remoteStaffService.faceLogin(staffPerfectDTO, SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "face-login");
			}catch(Exception e) {
				log.error("人脸识别验证未通过",e);
				throw new TCEException("人脸识别验证未通过");
			}

			log.info("人脸登录内部查询完成 success={}", result != null && result.isSuccess());
			if (!result.isSuccess() || Objects.isNull(result.getData())) {
				throw new TCEException("人脸识别验证未通过");
			} else {
				userName = result.getData().getBadge();
			}

			FaceAuthenticationToken mobileAuthenticationToken = new FaceAuthenticationToken(userName);

			setDetails(request, mobileAuthenticationToken);

			authResult = this.getAuthenticationManager().authenticate(mobileAuthenticationToken);

			logger.debug("Authentication success: " + authResult);
			SecurityContextHolder.getContext().setAuthentication(authResult);

		} catch (Exception failed) {
			SecurityContextHolder.clearContext();
			logger.debug("Authentication request failed: " + failed);

			eventPublisher.publishAuthenticationFailure(new BadCredentialsException(failed.getMessage(), failed),
					new PreAuthenticatedAuthenticationToken("access-token", "N/A"));

			try {
				authenticationEntryPoint.commence(request, response,
						new UsernameNotFoundException(failed.getMessage(), failed));
			} catch (Exception e) {
				logger.error("authenticationEntryPoint handle error:{}", failed);
			}
		}

		return authResult;
	}

	private void setDetails(HttpServletRequest request, FaceAuthenticationToken authRequest) {
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
