package com.tce.smart.push.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.push.service.PushService;
import com.tce.smart.push.feign.RemotePushService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 推送服务内部接口的安全契约。
 *
 * 推送入口会向设备发出实际通知，不能依赖 Nacos URL 白名单或可伪造的单一请求头。
 * 该测试要求入站端点同时声明内部调用和 server scope，并要求 Feign 契约触发服务令牌。
 */
public class PushInternalRouteContractTest {

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    public void pushRoutesRequireInternalServerScope() {
        assertInternalServerRoute("notice", "/notice");
        assertInternalServerRoute("transmission", "/transmission");
        assertInternalServerRoute("pushAll", "/pushAll");
    }

    @Test
    public void pushFeignRoutesRequireServiceTokenMarker() {
        assertFeignContract("notice", "/push/notice");
        assertFeignContract("transmission", "/push/transmission");
        assertFeignContract("pushAll", "/push/pushAll");
    }

    @Test
    public void singlePushRoutesRejectUnmanagedCallersAndAllowOnlyPlatformClient() {
        PushService pushService = Mockito.mock(PushService.class);
        OpenApiAuthenticationAdapter adapter = Mockito.mock(OpenApiAuthenticationAdapter.class);
        PushController controller = configuredController(pushService, adapter);

        assertDenied(() -> invokePush(controller, "notice", SecurityConstants.FROM_IN));

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Mockito.when(adapter.isClientOnly(authentication)).thenReturn(false);
        assertDenied(() -> invokePush(controller, "notice", SecurityConstants.FROM_IN));

        Mockito.when(adapter.isClientOnly(authentication)).thenReturn(true);
        Mockito.when(adapter.clientId(authentication)).thenReturn("unexpected-service");
        assertDenied(() -> invokePush(controller, "notice", SecurityConstants.FROM_IN));
        assertDenied(() -> invokePush(controller, "transmission", SecurityConstants.FROM_IN));

        Mockito.when(adapter.clientId(authentication)).thenReturn("platform-service");
        assertDenied(() -> invokePush(controller, "notice", "N"));

        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(),
                invokePush(controller, "notice", SecurityConstants.FROM_IN).getCode().intValue());
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(),
                invokePush(controller, "transmission", SecurityConstants.FROM_IN).getCode().intValue());
    }

    @Test
    public void pushAllIsExplicitlyDisabledUntilAnAuthorizedCallerIsRegistered() {
        PushController controller = configuredController(Mockito.mock(PushService.class),
                Mockito.mock(OpenApiAuthenticationAdapter.class));

        assertDenied(() -> invokePush(controller, "pushAll", SecurityConstants.FROM_IN));
    }

    @Test
    public void nacosTemplateEnforcesInnerCallsAndUsesDedicatedPlatformClientId() throws Exception {
        Path template = findPushNacosTemplate();
        String yaml = new String(Files.readAllBytes(template), StandardCharsets.UTF_8);

        Assert.assertTrue("Push 内部调用必须启用 ENFORCE", yaml.contains("mode: ENFORCE"));
        Assert.assertTrue("Push 必须使用 Platform 专属 client_id，且默认空值拒绝", yaml.contains(
                "platform-client-id: \"${SMART_PUSH_PLATFORM_CLIENT_ID:}\""));
        Assert.assertFalse("Push 不得回退到通用 OAuth client 配置", yaml.contains("SMART_OAUTH_CLIENT_ID"));
    }

    private void assertInternalServerRoute(String methodName, String expectedPath) {
        Method method = findMethod(PushController.class, methodName);
        Assert.assertEquals("推送基础路径必须固定", "/push",
                PushController.class.getAnnotation(RequestMapping.class).value()[0]);
        Assert.assertEquals(methodName + " 路径必须与内部契约一致", expectedPath,
                method.getAnnotation(PostMapping.class).value()[0]);
        Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
        OpenApi openApi = method.getAnnotation(OpenApi.class);
        Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
        Assert.assertEquals(methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
    }

    private void assertFeignContract(String methodName, String expectedPath) {
        Method method = findMethod(RemotePushService.class, methodName);
        Assert.assertEquals(methodName + " Feign 路径必须与入站契约一致", expectedPath,
                method.getAnnotation(PostMapping.class).value()[0]);
        assertRequestHeader(method, SecurityConstants.FROM);
        assertRequestHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH);
    }

    private void assertRequestHeader(Method method, String expectedHeader) {
        boolean present = Arrays.stream(method.getParameters())
                .map(Parameter::getAnnotations)
                .flatMap(Arrays::stream)
                .filter(RequestHeader.class::isInstance)
                .map(RequestHeader.class::cast)
                .anyMatch(header -> expectedHeader.equals(header.value()));
        Assert.assertTrue(method.getName() + " 必须显式声明请求头 " + expectedHeader, present);
    }

    private Method findMethod(Class<?> type, String methodName) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(type.getName() + " 缺少方法 " + methodName));
    }

    private PushController configuredController(PushService pushService, OpenApiAuthenticationAdapter adapter) {
        PushController controller = new PushController();
        ReflectionTestUtils.setField(controller, "pushService", pushService);
        Field adapterField = ReflectionUtils.findField(PushController.class, "openApiAuthenticationAdapter");
        Assert.assertNotNull("Push 必须校验纯服务令牌的 client_id", adapterField);
        Field clientIdField = ReflectionUtils.findField(PushController.class, "platformServiceClientId");
        Assert.assertNotNull("Push 必须配置 Platform 专属 client_id", clientIdField);
        ReflectionTestUtils.setField(controller, "openApiAuthenticationAdapter", adapter);
        ReflectionTestUtils.setField(controller, "platformServiceClientId", "platform-service");
        return controller;
    }

    private Path findPushNacosTemplate() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path template = current.resolve(Paths.get("docker", "nacos", "config", "dev", "smart-push.yml"));
            if (Files.exists(template)) {
                return template;
            }
            current = current.getParent();
        }
        throw new AssertionError("未找到 smart-push Nacos 模板");
    }

    private void assertDenied(ThrowingRunnable action) {
        try {
            action.run();
            Assert.fail("未受管的推送调用必须拒绝");
        } catch (AccessDeniedException expected) {
            // 预期：调用方不是受管 Platform 服务，或群发接口仍处于关闭状态。
        }
    }

    private Result<?> invokePush(PushController controller, String methodName, String from) {
        Method method = findMethod(PushController.class, methodName);
        Object[] arguments = method.getParameterCount() == 2 ? new Object[] {null, from} : new Object[] {null};
        try {
            return (Result<?>) method.invoke(controller, arguments);
        } catch (IllegalAccessException exception) {
            throw new AssertionError("无法调用推送入口", exception);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException) {
                throw (RuntimeException) exception.getCause();
            }
            throw new AssertionError("推送入口抛出非运行时异常", exception.getCause());
        }
    }

    private interface ThrowingRunnable {
        void run();
    }
}
