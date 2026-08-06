package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.dto.InternalUserLoginRespDTO;
import com.tce.smart.admin.api.dto.InternalPasswordResetReqDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.admin.service.SysSocialDetailsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * UPMS 内部用户资料接口必须同时绑定服务令牌客户端和端点用途，不能因删除匿名白名单后退化为任意用户可读。
 */
public class InternalUserControllerAccessTest {

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void loginProjectionRejectsAnonymousUserTokenWrongClientAndWrongPurpose() {
        SysUserService userService = Mockito.mock(SysUserService.class);
        OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
        InternalUserController controller = configuredController(userService, adapter);

        assertDenied(() -> controller.getLoginUserByUsername("employee", SecurityConstants.FROM_IN, "user-authentication"));

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Mockito.when(adapter.isClientOnly(authentication)).thenReturn(false);
        assertDenied(() -> controller.getLoginUserByUsername("employee", SecurityConstants.FROM_IN, "user-authentication"));

        Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
        Mockito.when(adapter.clientId(authentication)).thenReturn("unexpected-client");
        assertDenied(() -> controller.getLoginUserByUsername("employee", SecurityConstants.FROM_IN, "user-authentication"));

        Mockito.when(adapter.clientId(authentication)).thenReturn("auth-service");
        assertDenied(() -> controller.getLoginUserByUsername("employee", SecurityConstants.FROM_IN, "platform-user-management"));
    }

    @Test
    public void managedAuthClientGetsOnlyLoginProjection() {
        SysUserService userService = Mockito.mock(SysUserService.class);
        OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
        Mockito.when(adapter.clientId(authentication)).thenReturn("auth-service");

        SysUser user = new SysUser();
        user.setUserId(7);
        user.setDeptId(9);
        user.setUsername("employee");
        user.setPassword("password-hash");
        user.setLockFlag("0");
        UserInfo userInfo = new UserInfo();
        userInfo.setSysUser(user);
        userInfo.setRoles(new Integer[] {3});
        userInfo.setPermissions(new String[] {"profile:read"});
        userInfo.setSalaryTypeName("monthly");
        Mockito.when(userService.getOne(Mockito.any())).thenReturn(user);
        Mockito.when(userService.findUserInfo(user)).thenReturn(userInfo);
        Mockito.when(userService.listUserPark(7)).thenReturn(Collections.singletonList(1));

        InternalUserController controller = configuredController(userService, adapter);
        InternalUserLoginRespDTO response = controller.getLoginUserByUsername("employee", SecurityConstants.FROM_IN,
                "user-authentication").getData();

        assertEquals(Integer.valueOf(7), response.getUserId());
        assertEquals("employee", response.getUsername());
        assertEquals(Collections.singletonList(1), response.getParkIds());
        assertExactFields(InternalUserLoginRespDTO.class, "userId", "deptId", "username", "passwordHash", "lockFlag",
                "roleIds", "permissions", "salaryTypeName", "parkIds");
    }

    @Test
    public void internalLoginEndpointDeclaresInnerAndServerScope() throws Exception {
        Method method = InternalUserController.class.getMethod("getLoginUserByUsername", String.class, String.class,
                String.class);
        GetMapping mapping = method.getAnnotation(GetMapping.class);
        assertNotNull(method.getAnnotation(Inner.class));
        assertNotNull(method.getAnnotation(OpenApi.class));
        assertEquals("server", method.getAnnotation(OpenApi.class).value());
        assertEquals("/login/username/{username}", mapping.value()[0]);
    }

    @Test
    public void appPasswordResetRequiresOnlyManagedAppClientAndMinimalCommand() throws Exception {
        SysUserService userService = Mockito.mock(SysUserService.class);
        OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
        Mockito.when(adapter.clientId(authentication)).thenReturn("platform-service");
        InternalUserController controller = configuredController(userService, adapter);
        InternalPasswordResetReqDTO request = new InternalPasswordResetReqDTO();
        request.setUsername("employee");
        request.setPassword("new-password");
        request.setUpdateAuthCode("one-time-code");

        assertDenied(() -> controller.resetAppPassword(request, SecurityConstants.FROM_IN, "app-password-reset"));

        Mockito.when(adapter.clientId(authentication)).thenReturn("app-service");
        Mockito.when(userService.updatePwd("employee", "new-password", "one-time-code")).thenReturn(Boolean.TRUE);
        assertEquals(Boolean.TRUE, controller.resetAppPassword(request, SecurityConstants.FROM_IN, "app-password-reset").getData());
        assertExactFields(InternalPasswordResetReqDTO.class, "username", "password", "updateAuthCode");

        Method method = InternalUserController.class.getMethod("resetAppPassword", InternalPasswordResetReqDTO.class,
                String.class, String.class);
        assertNotNull(method.getAnnotation(Inner.class));
        assertEquals("server", method.getAnnotation(OpenApi.class).value());
        assertEquals("/password/app-reset", method.getAnnotation(PostMapping.class).value()[0]);
    }

    private InternalUserController configuredController(SysUserService userService, OpenApiAuthenticationAdapter adapter) {
        InternalUserController controller = new InternalUserController(userService, Mockito.mock(SysSocialDetailsService.class), adapter);
        ReflectionTestUtils.setField(controller, "authServiceClientId", "auth-service");
        ReflectionTestUtils.setField(controller, "platformServiceClientId", "platform-service");
        ReflectionTestUtils.setField(controller, "appServiceClientId", "app-service");
        return controller;
    }

    private void assertDenied(ThrowingRunnable action) {
        try {
            action.run();
            fail("未受管的内部调用必须拒绝");
        } catch (AccessDeniedException expected) {
            // 预期：服务 client、用途或认证主体不满足要求。
        }
    }

    private void assertExactFields(Class<?> type, String... expectedNames) {
        java.util.Set<String> actualNames = new java.util.TreeSet<>();
        for (java.lang.reflect.Field field : type.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                actualNames.add(field.getName());
            }
        }
        java.util.Set<String> expected = new java.util.TreeSet<>(java.util.Arrays.asList(expectedNames));
        assertEquals(expected, actualNames);
        assertFalse("内部登录投影不得包含手机号", actualNames.contains("phone"));
    }

    private interface ThrowingRunnable {
        void run();
    }
}
