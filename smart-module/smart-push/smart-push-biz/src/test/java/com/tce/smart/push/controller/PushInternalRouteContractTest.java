package com.tce.smart.push.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.push.feign.RemotePushService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 推送服务内部接口的安全契约。
 *
 * 推送入口会向设备发出实际通知，不能依赖 Nacos URL 白名单或可伪造的单一请求头。
 * 该测试要求入站端点同时声明内部调用和 server scope，并要求 Feign 契约触发服务令牌。
 */
public class PushInternalRouteContractTest {

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
}
