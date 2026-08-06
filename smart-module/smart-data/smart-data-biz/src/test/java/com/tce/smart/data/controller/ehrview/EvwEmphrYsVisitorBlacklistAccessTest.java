package com.tce.smart.data.controller.ehrview;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import com.tce.smart.ehrview.core.service.IEvwEmphrYsService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 访客黑名单在 SmartData 侧也必须精确约束 Platform client 与调用用途。 */
public class EvwEmphrYsVisitorBlacklistAccessTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void onlyConfiguredPlatformClientCanReceiveBooleanBlacklistStatus() throws Exception {
		EvwEmphrYsController controller = new EvwEmphrYsController();
		IEvwEmphrYsService blacklistService = Mockito.mock(IEvwEmphrYsService.class);
		ReflectionTestUtils.setField(controller, "iEvwEmphrYsService", blacklistService);
		ReflectionTestUtils.setField(controller, "openApiAuthenticationAdapter", new OpenApiAuthenticationAdapter());
		ReflectionTestUtils.setField(controller, "platformServiceClientId", "smart-platform");
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();

		SecurityContextHolder.getContext().setAuthentication(serverClientAuthentication("smart-platform"));
		mockMvc.perform(get("/emphr/ys/internal/visitor-blacklist-status").param("cerNo", "110101199001010011")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "other-purpose"))
				.andExpect(status().isForbidden());
		Mockito.when(blacklistService.list(Mockito.any())).thenReturn(Collections.singletonList(new EvwEmphrYs()));
		mockMvc.perform(get("/emphr/ys/internal/visitor-blacklist-status").param("cerNo", "110101199001010011")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));

		Mockito.when(blacklistService.list(Mockito.any())).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/emphr/ys/internal/visitor-blacklist-status").param("cerNo", "110101199001010011")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data").value(false));

		ReflectionTestUtils.setField(controller, "platformServiceClientId", "");
		mockMvc.perform(get("/emphr/ys/internal/visitor-blacklist-status").param("cerNo", "110101199001010011")
				.header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
				.header("X-Smart-Internal-Purpose", "visitor-blacklist"))
				.andExpect(status().isForbidden());
	}

	private OAuth2Authentication serverClientAuthentication(String clientId) {
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
				Collections.singleton("server"), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(request, null);
	}
}
