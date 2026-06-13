package com.tce.smart.auth.config;

import cn.hutool.json.JSONObject;
import com.tce.smart.common.security.exception.NotStrongPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: CustomAuthenticationFailureHandler
 * @date: 2020/10/30 0030 23:30
 * @author: wuling
 * @version: 1.0
 */
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		if(exception instanceof NotStrongPasswordException){
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			JSONObject object = new JSONObject();
			String key = "isStrongPwd";
			object.put(key,false);
			response.getOutputStream()
					.println(object.toString());
		}
	}
}
