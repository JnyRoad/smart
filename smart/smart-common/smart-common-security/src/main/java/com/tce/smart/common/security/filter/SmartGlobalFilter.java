package com.tce.smart.common.security.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class SmartGlobalFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		log.info("处理请求：Path={}, Method={}", ((HttpServletRequest) servletRequest).getRequestURI(), ((HttpServletRequest) servletRequest).getMethod());
		filterChain.doFilter(new XSSHttpServletRequestWrapper((HttpServletRequest) servletRequest), servletResponse);
	}

	@Override
	public void destroy() {

	}
}
