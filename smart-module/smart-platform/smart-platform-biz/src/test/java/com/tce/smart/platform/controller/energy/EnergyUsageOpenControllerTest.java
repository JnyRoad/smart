package com.tce.smart.platform.controller.energy;

import com.tce.smart.common.core.config.GlobalExceptionHandlerResolver;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.resp.energy.ParkUtilityUsageMonthToDateRespDTO;
import com.tce.smart.platform.api.dto.resp.energy.UtilityUsageItemRespDTO;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 使用真实控制器、开放接口拦截器和认证适配器验证应用月累计读取的完整授权边界。 */
public class EnergyUsageOpenControllerTest {

	private EnergyProjectionService energyProjectionService;
	private MockMvc mockMvc;

	/** 装配新旧真实能耗控制器及公共鉴权链，查询服务使用无外部副作用的替身。 */
	@Before
	public void setUp() {
		energyProjectionService = mock(EnergyProjectionService.class);
		ParkUtilityUsageMonthToDateRespDTO response = new ParkUtilityUsageMonthToDateRespDTO();
		response.setParkId(7L);
		UtilityUsageItemRespDTO water = new UtilityUsageItemRespDTO();
		water.setUnit("m3");
		response.setWater(water);
		UtilityUsageItemRespDTO electricity = new UtilityUsageItemRespDTO();
		electricity.setUnit("kWh");
		response.setElectricity(electricity);
		when(energyProjectionService.getCurrentMonthToDate(7L)).thenReturn(response);

		OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
		mockMvc = MockMvcBuilders.standaloneSetup(
				new EnergyUsageOpenController(energyProjectionService, adapter),
				new EnergyProjectionController(energyProjectionService))
				.addInterceptors(new OpenApiInterceptor(adapter))
				.setControllerAdvice(new GlobalExceptionHandlerResolver())
				.build();
	}

	/** 清理线程认证上下文，防止授权状态在用例之间泄漏。 */
	@After
	public void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	/** server 应用在 token 授权园区内读取时返回既有月累计 DTO。 */
	@Test
	public void serverClientReadsAuthorizedPark() throws Exception {
		authenticateClient("server", Collections.singletonList(7));

		mockMvc.perform(get("/open/energy/month/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.parkId").value(7))
				.andExpect(jsonPath("$.data.water.unit").value("m3"))
				.andExpect(jsonPath("$.data.electricity.unit").value("kWh"));

		verify(energyProjectionService).getCurrentMonthToDate(7L);
	}

	/** 用户 token 不得冒充应用访问开放入口，但原后台用户入口仍保持可读。 */
	@Test
	public void userTokenCannotUseOpenEndpointButKeepsOrdinaryEndpoint() throws Exception {
		authenticateUser(Collections.singletonList(7));

		mockMvc.perform(get("/open/energy/month/7")).andExpect(status().isForbidden());
		mockMvc.perform(get("/sd/statistics/month/7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.parkId").value(7));

		verify(energyProjectionService).getCurrentMonthToDate(7L);
	}

	/** 匿名请求不得进入开放入口的查询服务。 */
	@Test
	public void anonymousCannotUseOpenEndpoint() throws Exception {
		mockMvc.perform(get("/open/energy/month/7")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** 非 server scope 的应用令牌不得进入开放入口的查询服务。 */
	@Test
	public void clientWithWrongScopeCannotUseOpenEndpoint() throws Exception {
		authenticateClient("internal:energy:projection:run", Collections.singletonList(7));

		mockMvc.perform(get("/open/energy/month/7")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** server 应用的园区授权集合为空时拒绝，不能降级为全园区。 */
	@Test
	public void clientWithoutAuthorizedParksCannotRead() throws Exception {
		authenticateClient("server", Collections.emptyList());

		mockMvc.perform(get("/open/energy/month/7")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** server 应用请求 token 未绑定的园区时拒绝，且不调用汇总查询。 */
	@Test
	public void clientCannotReadParkOutsideClaim() throws Exception {
		authenticateClient("server", Collections.singletonList(8));

		mockMvc.perform(get("/open/energy/month/7")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** 超过 Integer 上限的正数园区不得因 long 转 int 截断而命中其他园区。 */
	@Test
	public void clientCannotReadParkBeyondIntegerRange() throws Exception {
		authenticateClient("server", Collections.singletonList(Integer.MIN_VALUE));

		mockMvc.perform(get("/open/energy/month/2147483648")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** 非正数园区标识不属于合法授权范围，拒绝时不调用汇总查询。 */
	@Test
	public void clientCannotReadNonPositivePark() throws Exception {
		authenticateClient("server", Collections.singletonList(0));

		mockMvc.perform(get("/open/energy/month/0")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** server 应用只可访问显式开放的新入口，原后台用户入口继续由既有权限体系保护。 */
	@Test
	public void serverClientCannotUseOrdinaryUserEndpoint() throws Exception {
		authenticateClient("server", Collections.singletonList(7));

		mockMvc.perform(get("/sd/statistics/month/7")).andExpect(status().isForbidden());

		verifyZeroInteractions(energyProjectionService);
	}

	/** 构造纯客户端 OAuth 认证，并按用例写入 server scope 与 app_park_ids claim。 */
	private void authenticateClient(String scope, List<Integer> parkIds) {
		Map<String, Serializable> extensions = new HashMap<>();
		extensions.put("app_park_ids", new ArrayList<>(parkIds));
		OAuth2Request request = new OAuth2Request(Collections.emptyMap(), "energy-reader", Collections.emptyList(),
				true, Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), extensions);
		SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
	}

	/** 构造携带既有 SmartUser 园区范围的后台用户认证。 */
	private void authenticateUser(List<Integer> parkIds) {
		SmartUser user = new SmartUser(1, 1, "energy-user", parkIds, "unused",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "unused", Collections.emptyList()));
	}
}
