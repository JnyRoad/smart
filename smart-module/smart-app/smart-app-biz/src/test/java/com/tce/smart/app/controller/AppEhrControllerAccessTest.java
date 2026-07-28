package com.tce.smart.app.controller;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.data.api.feign.attendance.RemoteDepartmentService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwAcardlostAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizAregotRegisterService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizCallowanceFoodCancelService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizCallowanceFoodService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizCallowanceService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizLcardlostService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizLdxregLeaveRegisterService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizLregleaveRegisterService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwBizLregleaveService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCallowanceAlltService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCallowanceCancelAlltService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCotherAllowanceAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwHortationsAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLdxRegLeaveAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLergotAllService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.fail;

/** App EHR 路由只能查询当前认证员工，禁止客户端指定其他工号。 */
public class AppEhrControllerAccessTest {

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void lergotAllRejectsCrossBadgeBeforeFeignCall() {
        RemoteEvwLergotAllService lergotService = Mockito.mock(RemoteEvwLergotAllService.class);
        AppEhrController controller = controller(lergotService);
        authenticate("self-badge");

        try {
            controller.lergotAllList("other-badge", "2026-07");
            fail("跨员工工号请求必须在调用内部 Feign 前拒绝");
        } catch (AccessDeniedException expected) {
            Mockito.verifyZeroInteractions(lergotService);
        }
    }

    private AppEhrController controller(RemoteEvwLergotAllService lergotService) {
        return new AppEhrController(
                Mockito.mock(RemoteEvwHortationsAllService.class),
                Mockito.mock(RemoteEvwAcardlostAllService.class),
                Mockito.mock(RemoteEvwBizAregotRegisterService.class),
                Mockito.mock(RemoteEvwBizCallowanceFoodCancelService.class),
                Mockito.mock(RemoteEvwBizCallowanceFoodService.class),
                Mockito.mock(RemoteEvwBizCallowanceService.class),
                Mockito.mock(RemoteEvwBizLcardlostService.class),
                Mockito.mock(RemoteEvwBizLdxregLeaveRegisterService.class),
                Mockito.mock(RemoteEvwBizLregleaveRegisterService.class),
                Mockito.mock(RemoteEvwBizLregleaveService.class),
                Mockito.mock(RemoteEvwCallowanceAlltService.class),
                Mockito.mock(RemoteEvwCallowanceCancelAlltService.class),
                Mockito.mock(RemoteEvwCotherAllowanceAllService.class),
                Mockito.mock(RemoteEvwLdxRegLeaveAllService.class),
                lergotService,
                Mockito.mock(RemoteDepartmentService.class));
    }

    private void authenticate(String badge) {
        SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(1), "N/A",
                true, true, true, true, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
    }
}
