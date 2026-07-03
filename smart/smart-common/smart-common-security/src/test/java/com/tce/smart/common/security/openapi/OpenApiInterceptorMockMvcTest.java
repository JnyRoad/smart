package com.tce.smart.common.security.openapi;

import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OpenApiInterceptor 端到端测试：通过真实的 Spring MVC 分发链路（standaloneSetup + addInterceptors）
 * 验证 403 会真正体现在 HTTP 响应状态码上，而不是仅在单元测试里断言方法返回值。
 * <p>
 * 这是对 Task 3 评审发现问题的回归验证：本仓库 {@code ExceptionTranslationFilter}
 * 够不着 MVC 拦截器阶段，早期实现用抛异常的方式最终会被 {@code GlobalExceptionHandlerResolver}
 * 的兜底 handler 吞成 HTTP 200；现在拦截器直接调用 {@code response.sendError(403, ...)}，
 * 此处用 MockMvc 走完整分发链路证明响应状态码确实是 403。
 */
public class OpenApiInterceptorMockMvcTest {

	/** 测试专用 Controller：一个标注 @OpenApi 的开放接口，一个未标注的普通接口。 */
	@RestController
	static class SampleController {

		@OpenApi("open:test:read")
		@GetMapping("/open/test")
		public String openEndpoint() {
			return "open-ok";
		}

		@GetMapping("/plain/test")
		public String plainEndpoint() {
			return "plain-ok";
		}
	}

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
		mockMvc = MockMvcBuilders.standaloneSetup(new SampleController())
				.addInterceptors(new OpenApiInterceptor(adapter))
				.build();
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	/** 构造 client_credentials 模式的 OAuth2Authentication，isClientOnly()=true，携带给定 scope。 */
	private OAuth2Authentication clientOnlyAuthentication(String clientId, Set<String> scopes) {
		OAuth2Request oAuth2Request = new OAuth2Request(
				Collections.emptyMap(), clientId, Collections.emptyList(),
				true, scopes, Collections.emptySet(),
				null, Collections.emptySet(), Collections.emptyMap());
		// 第二个参数（userAuthentication）为 null 时 OAuth2Authentication.isClientOnly() 返回 true
		return new OAuth2Authentication(oAuth2Request, null);
	}

	/** 构造用户 token 场景的 Authentication（非 OAuth2Authentication，模拟资源服务解析出的用户认证）。 */
	private Authentication userAuthentication() {
		return new UsernamePasswordAuthenticationToken("normal-user", "N/A", Collections.emptyList());
	}

	@Test
	public void clientToken_withScope_onOpenApi_returns200() throws Exception {
		Set<String> scopes = new HashSet<>();
		scopes.add("open:test:read");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("open-app", scopes));

		mockMvc.perform(get("/open/test")).andExpect(status().isOk());
	}

	@Test
	public void clientToken_missingScope_onOpenApi_returns403() throws Exception {
		Set<String> scopes = new HashSet<>();
		scopes.add("other:scope");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("open-app", scopes));

		mockMvc.perform(get("/open/test")).andExpect(status().isForbidden());
	}

	@Test
	public void userToken_onOpenApi_returns403() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(userAuthentication());

		mockMvc.perform(get("/open/test")).andExpect(status().isForbidden());
	}

	@Test
	public void clientToken_onPlainEndpoint_returns403() throws Exception {
		// deny-by-default：client_credentials token 不允许访问未标注 @OpenApi 的普通接口
		Set<String> scopes = new HashSet<>();
		scopes.add("open:test:read");
		SecurityContextHolder.getContext().setAuthentication(clientOnlyAuthentication("open-app", scopes));

		mockMvc.perform(get("/plain/test")).andExpect(status().isForbidden());
	}
}
