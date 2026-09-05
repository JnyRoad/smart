package com.tce.smart.platform.controller;

import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.controller.admittance.AdmittancePhotoOpenController;
import com.tce.smart.platform.controller.energy.EnergyProjectionController;
import com.tce.smart.platform.service.admittance.AdmittancePhotoOpenService;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 使用真实控制器、OAuth 适配器和 MVC 拦截器验证通用服务授权；业务服务使用 Mock，不连接外部系统。 */
public class OpenApiServerScopeMockMvcTest {

    private static final String PHOTO_ID = "eed9a5c2-2b38-4ff5-96d2-e56c237337e1";
    private AdmittancePhotoOpenService photoService;
    private EnergyProjectionService energyService;

    /** 提供无外部副作用的照片及能耗服务响应，避免鉴权测试依赖数据库。 */
    @Before
    public void setUp() {
        photoService = mock(AdmittancePhotoOpenService.class);
        energyService = mock(EnergyProjectionService.class);
        when(photoService.loadPhoto(PHOTO_ID, Arrays.asList(7, 8))).thenReturn(new byte[] {1});
    }

    /** 清除每个用例写入的认证上下文，防止授权状态串用。 */
    @After
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /** 默认兼容开启时，server 可访问六个入口并继续向照片查询传递令牌内园区范围。 */
    @Test
    public void serverAccessesAllOpenEndpoints() throws Exception {
        authenticateClient("server");
        MockMvc mvc = mvc(true);
        assertPhotos(mvc, 200);
        assertEnergy(mvc, 200);
        verify(photoService).listPendingPhotoIds(Arrays.asList(7, 8));
        verify(photoService).loadPhoto(PHOTO_ID, Arrays.asList(7, 8));
    }

    /** server 是长期主授权，关闭历史兼容后仍能访问全部入口。 */
    @Test
    public void serverDoesNotDependOnCompatibilitySwitch() throws Exception {
        authenticateClient("server");
        MockMvc mvc = mvc(false);
        assertPhotos(mvc, 200);
        assertEnergy(mvc, 200);
    }

    /** 存量照片权限只兼容照片入口，不获得能耗执行权。 */
    @Test
    public void historicalPhotoScopeRemainsLimitedToPhotos() throws Exception {
        authenticateClient("open:admittance:photo:read");
        MockMvc mvc = mvc(true);
        assertPhotos(mvc, 200);
        assertEnergy(mvc, 403);
    }

    /** 存量能耗权限只兼容能耗入口，不获得照片访问权。 */
    @Test
    public void historicalEnergyScopeRemainsLimitedToEnergy() throws Exception {
        authenticateClient("internal:energy:projection:run");
        MockMvc mvc = mvc(true);
        assertEnergy(mvc, 200);
        assertPhotos(mvc, 403);
    }

    /** 关闭历史兼容只拒绝细分旧权限，不将它们提升为 server。 */
    @Test
    public void compatibilitySwitchCanRejectHistoricalScopes() throws Exception {
        MockMvc mvc = mvc(false);
        authenticateClient("open:admittance:photo:read");
        assertPhotos(mvc, 403);
        authenticateClient("internal:energy:projection:run");
        assertEnergy(mvc, 403);
    }

    /** 空权限和无关权限都不能通过通用服务授权。 */
    @Test
    public void emptyAndUnrelatedScopesRemainForbidden() throws Exception {
        MockMvc mvc = mvc(true);
        authenticateClient();
        assertPhotos(mvc, 403);
        assertEnergy(mvc, 403);
        authenticateClient("unrelated");
        assertPhotos(mvc, 403);
        assertEnergy(mvc, 403);
    }

    /** 匿名和普通用户身份仍不能使用应用开放接口。 */
    @Test
    public void anonymousAndUserAuthenticationRemainForbidden() throws Exception {
        MockMvc mvc = mvc(true);
        SecurityContextHolder.clearContext();
        assertPhotos(mvc, 403);
        assertEnergy(mvc, 403);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "unused", Collections.emptyList()));
        assertPhotos(mvc, 403);
        assertEnergy(mvc, 403);
    }

    /** server 只放行显式开放的入口，普通能耗查询仍拒绝客户端令牌。 */
    @Test
    public void serverCannotAccessOrdinaryUserEndpoint() throws Exception {
        authenticateClient("server");
        mvc(true).perform(get("/sd/statistics/month/7")).andExpect(status().isForbidden());
    }

    /** 按指定开关装配实际入口及公共鉴权拦截器，仅在本进程处理请求。 */
    private MockMvc mvc(boolean compatibilityEnabled) {
        OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
        return MockMvcBuilders.standaloneSetup(
                new AdmittancePhotoOpenController(photoService, adapter),
                new EnergyProjectionController(energyService))
                .addInterceptors(new OpenApiInterceptor(adapter, compatibilityEnabled)).build();
    }

    /** 为当前用例建立应用令牌认证，scope 来自入参，园区 claim 固定为 7、8。 */
    private void authenticateClient(String... scopes) {
        Map<String, Serializable> extensions = new HashMap<>();
        extensions.put("app_park_ids", new ArrayList<>(Arrays.asList(7, 8)));
        OAuth2Request request = new OAuth2Request(Collections.emptyMap(), "test-client", Collections.emptyList(),
                true, new java.util.HashSet<>(Arrays.asList(scopes)), Collections.emptySet(), null,
                Collections.emptySet(), extensions);
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
    }

    /** 对两个真实照片路由断言统一的 HTTP 授权结果，失败时由 MockMvc 抛出断言错误。 */
    private void assertPhotos(MockMvc mvc, int expectedStatus) throws Exception {
        mvc.perform(get("/open/admittance/photo/pending")).andExpect(status().is(expectedStatus));
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID)).andExpect(status().is(expectedStatus));
    }

    /** 对四个真实能耗路由断言 HTTP 授权结果；合法日期确保失败来源仅为授权。 */
    private void assertEnergy(MockMvc mvc, int expectedStatus) throws Exception {
        MockHttpServletRequestBuilder[] requests = {
                post("/inner/energy/projection/process-pending"),
                post("/inner/energy/projection/reconcile/2026-09-04"),
                post("/inner/energy/projection/backfill-month-to-date"),
                post("/inner/energy/projection/daily/2026-09-04").param("reconcile", "true").param("backfill", "true")
        };
        for (MockHttpServletRequestBuilder request : requests) {
            mvc.perform(request).andExpect(status().is(expectedStatus));
        }
    }
}
