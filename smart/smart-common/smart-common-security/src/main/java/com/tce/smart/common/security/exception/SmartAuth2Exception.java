package com.tce.smart.common.security.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tce.smart.common.security.component.SmartAuth2ExceptionSerializer;
import lombok.Getter;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * 自定义OAuth2Exception
 */
@JsonSerialize(using = SmartAuth2ExceptionSerializer.class)
public class SmartAuth2Exception extends OAuth2Exception {
	@Getter
	private String errorCode;

	public SmartAuth2Exception(String msg) {
		super(msg);
	}

	public SmartAuth2Exception(String msg, String errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}
}
