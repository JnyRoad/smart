package com.tce.smart.dispatcher.service.impl;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * 动态园区 Bridge 客户端的目标地址约束。
 */
public class RemoteFeignServiceImplTest {

    @Test
    public void allowsOnlyExactConfiguredHttpOrigin() {
        Assert.assertTrue(isAllowedBridgeTarget(
                "https://bridge.example.com:9443/api", "https://bridge.example.com:9443"));
        Assert.assertFalse(isAllowedBridgeTarget(
                "https://bridge.example.com/api", "https://bridge.example.com:9443"));
    }

    @Test
    public void rejectsEmptyAllowlistAndUnsafeUrls() {
        Assert.assertFalse(isAllowedBridgeTarget(
                "https://bridge.example.com:9443", ""));
        Assert.assertFalse(isAllowedBridgeTarget(
                "ftp://bridge.example.com:9443", "https://bridge.example.com:9443"));
        Assert.assertFalse(isAllowedBridgeTarget(
                "https://token@bridge.example.com:9443", "https://bridge.example.com:9443"));
        Assert.assertFalse(isAllowedBridgeTarget(
                "https://bridge.example.com:9443?redirect=https://evil.example", "https://bridge.example.com:9443"));
    }

    private boolean isAllowedBridgeTarget(String serviceUrl, String allowlist) {
        try {
            Method method = RemoteFeignServiceImpl.class.getDeclaredMethod(
                    "isAllowedBridgeTarget", String.class, String.class);
            return (Boolean) method.invoke(null, serviceUrl, allowlist);
        } catch (ReflectiveOperationException ex) {
            Assert.fail("动态 Bridge 客户端必须提供可测试的目标地址白名单校验");
            return false;
        }
    }
}
