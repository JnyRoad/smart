package com.tce.smart.common.security.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;

@Slf4j
public class NotStrongPasswordException extends AuthenticationException {

	public NotStrongPasswordException(String msg){
		super(msg);
	}

	public NotStrongPasswordException(String msg,Throwable t){
		super(msg,t);
	}
}
