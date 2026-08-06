package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.service.SysSocialDetailsService;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 真实 MVC 拦截器链必须拒绝匿名、用户 token 和错误 scope，只允许受管 server client。 */
public class InternalUserOpenApiScopeContractTest {

    private MockMvc mockMvc;
    private SysUserService userService;

    @Before
    public void setUp() {
        userService = Mockito.mock(SysUserService.class);
        InternalUserController controller = new InternalUserController(userService,
                Mockito.mock(SysSocialDetailsService.class), new OpenApiAuthenticationAdapter());
        ReflectionTestUtils.setField(controller, "authServiceClientId", "auth-service");
        ReflectionTestUtils.setField(controller, "platformServiceClientId", "platform-service");
        ReflectionTestUtils.setField(controller, "appServiceClientId", "app-service");
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AccessDeniedAdvice())
                .addInterceptors(new OpenApiInterceptor(new OpenApiAuthenticationAdapter()))
                .build();
    }

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void anonymousUserAndWrongScopeAreForbidden() throws Exception {
        performLogin().andExpect(status().isForbidden());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("normal-user", "N/A", Collections.emptyList()));
        performLogin().andExpect(status().isForbidden());

        SecurityContextHolder.getContext().setAuthentication(clientToken("auth-service", "wrong-scope"));
        performLogin().andExpect(status().isForbidden());
    }

    @Test
    public void managedServerClientWithExactPurposeIsAllowed() throws Exception {
        SysUser user = new SysUser();
        user.setUserId(1);
        user.setUsername("employee");
        user.setLockFlag("0");
        UserInfo userInfo = new UserInfo();
        userInfo.setSysUser(user);
        userInfo.setRoles(new Integer[0]);
        userInfo.setPermissions(new String[0]);
        Mockito.when(userService.getOne(Mockito.any())).thenReturn(user);
        Mockito.when(userService.findUserInfo(user)).thenReturn(userInfo);
        Mockito.when(userService.listUserPark(1)).thenReturn(Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(clientToken("auth-service", "server"));

        performLogin().andExpect(status().isOk());
    }

    @Test
    public void passwordResetRejectsAnonymousUserWrongScopeWrongClientAndWrongPurpose() throws Exception {
        performPasswordReset("app-password-reset").andExpect(status().isForbidden());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("normal-user", "N/A", Collections.emptyList()));
        performPasswordReset("app-password-reset").andExpect(status().isForbidden());

        SecurityContextHolder.getContext().setAuthentication(clientToken("app-service", "wrong-scope"));
        performPasswordReset("app-password-reset").andExpect(status().isForbidden());

        SecurityContextHolder.getContext().setAuthentication(clientToken("unexpected-client", "server"));
        performPasswordReset("app-password-reset").andExpect(status().isForbidden());

        SecurityContextHolder.getContext().setAuthentication(clientToken("app-service", "server"));
        performPasswordReset("wrong-purpose").andExpect(status().isForbidden());
    }

    @Test
    public void passwordResetAllowsOnlyManagedAppServerClientWithExactPurpose() throws Exception {
        Mockito.when(userService.updatePwd("employee", "NewPassword1!", "one-time-code")).thenReturn(Boolean.TRUE);
        SecurityContextHolder.getContext().setAuthentication(clientToken("app-service", "server"));

        performPasswordReset("app-password-reset").andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions performLogin() throws Exception {
        return mockMvc.perform(get("/internal/user/login/username/employee")
                .header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
                .header("X-Smart-Internal-Purpose", "user-authentication"));
    }

    private OAuth2Authentication clientToken(String clientId, String scope) {
        OAuth2Request request = new OAuth2Request(Collections.emptyMap(), clientId, Collections.emptyList(), true,
                Collections.singleton(scope), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyMap());
        return new OAuth2Authentication(request, null);
    }

    private org.springframework.test.web.servlet.ResultActions performPasswordReset(String purpose) throws Exception {
        return mockMvc.perform(post("/internal/user/password/app-reset")
                .contentType(APPLICATION_JSON)
                .content("{\"username\":\"employee\",\"password\":\"NewPassword1!\",\"updateAuthCode\":\"one-time-code\"}")
                .header(SecurityConstants.FROM, SecurityConstants.FROM_IN)
                .header("X-Smart-Internal-Purpose", purpose));
    }

    @RestControllerAdvice
    private static class AccessDeniedAdvice {

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        void accessDenied() {
            // 测试 MVC 链路将服务端拒绝明确映射为 403，与生产全局异常处理保持一致。
        }
    }
}
