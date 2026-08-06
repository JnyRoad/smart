package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.feign.RemoteUserInternalService;
import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 防止后续新增 Feign 方法时遗漏内部服务令牌或用途约束。
 */
public class RemoteUserInternalServiceContractTest {

    @Test
    public void everyRemoteMethodRequiresServiceTokenAndPurposeHeader() {
        for (Method method : RemoteUserInternalService.class.getMethods()) {
            if (method.isDefault() || method.getDeclaringClass() == Object.class) {
                continue;
            }
            assertTrue("内部用户 Feign 方法必须声明 from 头：" + method,
                    hasHeader(method, SecurityConstants.FROM));
            assertTrue("内部用户 Feign 方法必须显式触发服务令牌：" + method,
                    hasHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH));
            assertTrue("内部用户 Feign 方法必须绑定用途：" + method,
                    hasHeader(method, "X-Smart-Internal-Purpose"));
            assertFalse("内部用户 Feign 契约不得返回完整 UserInfo：" + method,
                    method.getGenericReturnType().getTypeName().contains("UserInfo"));
        }
    }

    private boolean hasHeader(Method method, String expectedHeader) {
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestHeader
                        && expectedHeader.equals(((RequestHeader) annotation).value())) {
                    return true;
                }
            }
        }
        return false;
    }
}
