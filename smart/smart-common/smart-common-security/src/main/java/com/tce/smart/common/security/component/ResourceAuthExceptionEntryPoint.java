package com.tce.smart.common.security.component;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.model.Result;

import cn.hutool.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端异常处理
 * 1. 可以根据 AuthenticationException 不同细化异常处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class ResourceAuthExceptionEntryPoint implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;

	@Override
	@SneakyThrows
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
		response.setCharacterEncoding(CommonConstants.UTF8);
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		Result<String> result = new Result<>();
		result.setCode(CommonConstants.FAIL);
		if (authException != null) {
			String respMessage;
			if (authException.getCause() instanceof InvalidTokenException) {
				InvalidTokenException invalidTokenException = (InvalidTokenException) authException.getCause();
				result.setCode(invalidTokenException.getHttpErrorCode());
				respMessage = invalidTokenException.getOAuth2ErrorCode();
			} else {
				respMessage = authException.getMessage();
			}
			result.setMsg(respMessage);
			result.setData(respMessage);
		}
		log.error("Token无效: {}", result.getMsg());
		response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
		response.getWriter().append(objectMapper.writeValueAsString(result));
	}
}
