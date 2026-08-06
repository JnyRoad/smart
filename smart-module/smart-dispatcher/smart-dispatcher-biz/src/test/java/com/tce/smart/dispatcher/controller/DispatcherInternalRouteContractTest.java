package com.tce.smart.dispatcher.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 分发服务内部接口的安全契约。
 *
 * 该服务可将集团请求转发至园区设备，必须同时持有受控服务令牌和内部调用标识。
 */
public class DispatcherInternalRouteContractTest {

    @Test
    public void dispatcherRoutesRequireInternalServerScope() {
        assertInternalServerRoute("dispatch", "/dispatch");
        assertInternalServerRoute("handle", "/handle");
        assertInternalServerRoute("getImage", "/image");
        assertInternalServerRoute("getThumbnail", "/thumbnail");
    }

    @Test
    public void dispatcherFeignRoutesRequireServiceTokenMarker() {
        assertFeignContract("dispatch", "/dispatcher/dispatch");
        assertFeignContract("handle", "/dispatcher/handle");
        assertFeignContract("getImage", "/dispatcher/image");
        assertFeignContract("getThumbnail", "/dispatcher/thumbnail");
    }

    private void assertInternalServerRoute(String methodName, String expectedPath) {
        Method method = findMethod(DispatcherController.class, methodName);
        Assert.assertEquals("分发基础路径必须固定", "/dispatcher",
                DispatcherController.class.getAnnotation(RequestMapping.class).value()[0]);
        Assert.assertEquals(methodName + " 路径必须与内部契约一致", expectedPath,
                method.getAnnotation(PostMapping.class).value()[0]);
        Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
        OpenApi openApi = method.getAnnotation(OpenApi.class);
        Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
        Assert.assertEquals(methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
    }

    private void assertFeignContract(String methodName, String expectedPath) {
        Method method = findMethod(RemoteDispatcherService.class, methodName);
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
