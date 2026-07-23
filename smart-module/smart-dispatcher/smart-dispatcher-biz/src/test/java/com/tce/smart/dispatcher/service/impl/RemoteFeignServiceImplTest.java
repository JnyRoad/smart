package com.tce.smart.dispatcher.service.impl;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Collections;

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

	@Test
	public void dynamicBridgeSyncRequiresAServiceTokenWhenReadingTargets() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)).thenReturn(Result.success(Collections.emptyList()));
		RemoteFeignServiceImpl service = new RemoteFeignServiceImpl();
		setField(service, "remoteParkInternalService", parkInternalService);

		Method init = RemoteFeignServiceImpl.class.getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(service);

		Mockito.verify(parkInternalService).getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
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

	private void setField(Object target, String fieldName, Object value) throws Exception {
		java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
}
}
