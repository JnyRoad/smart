package com.tce.smart.common.core.config;

import com.tce.smart.common.core.constant.CommonConstants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandlerResolver 单测。
 *
 * 背景：处理器曾错误 import java.nio.file.AccessDeniedException，
 * 导致 Spring Security 的拒绝异常（@PreAuthorize 拒绝、@Inner 切面 ENFORCE 拒绝）
 * 永远匹配不到 403 处理器，落入兜底 Exception 处理器返回 HTTP 200 + code=FAIL。
 */
public class GlobalExceptionHandlerResolverTest {

	/** 模拟业务 Controller：抛出 Spring Security 的拒绝授权异常 */
	@RestController
	static class AccessDeniedThrowingController {

		@GetMapping("/test/denied")
		public void denied() {
			throw new AccessDeniedException("Access is denied");
		}
	}

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		// standalone 模式挂上全局异常处理器，验证真实的 @ExceptionHandler 匹配行为
		this.mockMvc = MockMvcBuilders.standaloneSetup(new AccessDeniedThrowingController())
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
	}

	/**
	 * Spring Security 的 AccessDeniedException 必须返回 HTTP 403，
	 * 同时保留 Result JSON body（code=FAIL），前端拦截器按 body 提示、不触发登出。
	 */
	@Test
	public void springSecurityAccessDeniedShouldReturn403WithFailBody() throws Exception {
		mockMvc.perform(get("/test/denied"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(CommonConstants.FAIL));
	}
}
