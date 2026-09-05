package com.tce.smart.platform.controller.admittance;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.impl.AdmittancePhotoOpenServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 真实下载入口、鉴权和照片服务的组合回归；仅数据库服务用 Mock，不连接外部资源。 */
public class AdmittancePhotoDownloadAuthorizationTest {
    private static final String PHOTO_ID = "eed9a5c2-2b38-4ff5-96d2-e56c237337e1";
    private static final byte[] PHOTO_BYTES = {1, 2, 3};
    private SmtAdmittanceApplyService applyService;
    private SmtAdmittanceFellowService fellowService;
    private SmtImageService imageService;
    private MockMvc mvc;

    /** 装配真实服务和鉴权，图片始终存在，用于暴露跳过业务授权直接读图的回归。 */
    @Before
    public void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtAdmittanceApply.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtAdmittanceFellow.class);
        applyService = mock(SmtAdmittanceApplyService.class);
        fellowService = mock(SmtAdmittanceFellowService.class);
        imageService = mock(SmtImageService.class);
        when(imageService.getImageBinaryByCode(PHOTO_ID)).thenReturn(PHOTO_BYTES);
        AdmittancePhotoOpenServiceImpl service = new AdmittancePhotoOpenServiceImpl(applyService, fellowService, imageService);
        OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();
        mvc = MockMvcBuilders.standaloneSetup(new AdmittancePhotoOpenController(service, adapter))
                .addInterceptors(new OpenApiInterceptor(adapter, true)).build();
    }

    /** 清除认证上下文，避免令牌在用例间串用。 */
    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /** server 没有园区授权时返回 404，请求参数不能扩大 token 范围，且不读取业务数据。 */
    @Test
    public void serverWithoutParksCannotDownloadEvenWithForgedParkParameter() throws Exception {
        authenticate("server", null);
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID).param("parkId", "7")
                .param("app_park_ids", "7")).andExpect(status().isNotFound());
        verifyZeroInteractions(applyService, fellowService, imageService);
    }

    /** claim 元素类型错误须按空授权拒绝，不能把字符串园区当成有效授权。 */
    @Test
    public void malformedParkClaimCannotDownload() throws Exception {
        authenticate("server", new ArrayList<>(Collections.singletonList("7")));
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID)).andExpect(status().isNotFound());
        verifyZeroInteractions(applyService, fellowService, imageService);
    }

    /** 历史照片 scope 也必须经过同样的园区校验。 */
    @Test
    public void historicalPhotoScopeWithoutParksCannotDownload() throws Exception {
        authenticate("open:admittance:photo:read", new ArrayList<Integer>());
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID)).andExpect(status().isNotFound());
        verifyZeroInteractions(applyService, fellowService, imageService);
    }

    /** 图片存在但无随行人员关联时不可下载，拒绝前不读图。 */
    @Test
    public void unlinkedImageCannotDownload() throws Exception {
        authenticate("server", new ArrayList<>(Collections.singletonList(7)));
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID)).andExpect(status().isNotFound());
        verifyZeroInteractions(applyService, imageService);
    }

    /** 有照片关联，但获授权园区内不存在有效申请时，保持与缺图一致的 404。 */
    @Test
    public void photoOutsideAuthorizedApplicationsCannotDownload() throws Exception {
        authenticate("server", new ArrayList<>(Collections.singletonList(7)));
        linkedPhoto();
        when(applyService.list(any())).thenReturn(Collections.emptyList());
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID)).andExpect(status().isNotFound());
        verifyZeroInteractions(imageService);
    }

    /** 获授权园区内存在有效申请时，继续返回原始 PNG 字节。 */
    @Test
    public void authorizedApplicationCanDownload() throws Exception {
        authenticate("server", new ArrayList<>(Collections.singletonList(7)));
        linkedPhoto();
        SmtAdmittanceApply apply = new SmtAdmittanceApply();
        apply.setId(100L);
        when(applyService.list(any())).thenReturn(Collections.singletonList(apply));
        mvc.perform(get("/open/admittance/photo/download/" + PHOTO_ID)).andExpect(status().isOk())
                .andExpect(content().contentType("image/png")).andExpect(content().bytes(PHOTO_BYTES));
    }

    /** 查询替身返回目标照片的随行人员关联，不在测试中自行实现授权筛选。 */
    private void linkedPhoto() {
        SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
        fellow.setVisitorId(100L);
        when(fellowService.list(any())).thenReturn(Collections.singletonList(fellow));
    }

    /** 构造应用凭证令牌，园区 claim 按用例提供，缺失时不写入。 */
    private void authenticate(String scope, Serializable parks) {
        Map<String, Serializable> extensions = new HashMap<>();
        if (parks != null) {
            extensions.put("app_park_ids", parks);
        }
        OAuth2Request request = new OAuth2Request(Collections.emptyMap(), "photo-test", Collections.emptyList(),
                true, Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), extensions);
        SecurityContextHolder.getContext().setAuthentication(new OAuth2Authentication(request, null));
    }
}
