package com.tce.smart.dispatcher.service.impl;

import com.tce.smart.bridge.api.feign.RemoteBridgeService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.security.DispatcherBridgeTargetProperties;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import feign.RequestInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

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
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)).thenReturn(successfulTargets(Collections.emptyList()));
		RemoteFeignServiceImpl service = newService(parkInternalService, "");

		Method init = RemoteFeignServiceImpl.class.getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(service);

		Mockito.verify(parkInternalService).getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}

	@Test
	public void removesCachedBridgeWhenSuccessfulSyncNoLongerReturnsThePark() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(successfulTargets(Collections.emptyList()));
		RemoteFeignServiceImpl service = newService(parkInternalService,
				"https://bridge-a.example.com,https://bridge-b.example.com");

		invokeInit(service);
		Assert.assertNotNull("首次成功同步必须缓存合法园区目标", service.getBridge(1));

		invokeInit(service);
		Assert.assertNull("成功同步未返回园区时，旧客户端不得继续携带服务令牌", service.getBridge(1));
	}

	@Test
	public void removesCachedBridgeWhenSuccessfulSyncReturnsDisallowedUrl() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://untrusted.example.com"))));
		RemoteFeignServiceImpl service = newService(parkInternalService, "https://bridge-a.example.com");

		invokeInit(service);
		Assert.assertNotNull("首次成功同步必须缓存合法园区目标", service.getBridge(1));

		invokeInit(service);
		Assert.assertNull("成功同步返回不合法地址时，旧客户端不得继续携带服务令牌", service.getBridge(1));
	}

	@Test
	public void removesCachedBridgeWhenSuccessfulSyncClearsTheUrl() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, ""))));
		RemoteFeignServiceImpl service = newService(parkInternalService, "https://bridge-a.example.com");

		invokeInit(service);
		Assert.assertNotNull("首次成功同步必须缓存合法园区目标", service.getBridge(1));

		invokeInit(service);
		Assert.assertNull("成功同步清空地址时，旧客户端不得继续携带服务令牌", service.getBridge(1));
	}

	@Test
	public void replacesCachedBridgeWhenSuccessfulSyncChangesAllowedUrl() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-b.example.com"))));
		RemoteFeignServiceImpl service = newService(parkInternalService,
				"https://bridge-a.example.com,https://bridge-b.example.com");

		invokeInit(service);
		RemoteBridgeService firstBridge = service.getBridge(1);
		invokeInit(service);

		Assert.assertNotNull("成功同步更新后的合法地址必须可用", service.getBridge(1));
		Assert.assertNotSame("地址变化时必须替换旧客户端", firstBridge, service.getBridge(1));
	}

	@Test
	public void keepsCachedBridgeWhenTargetFetchFails() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(failedTargets());
		RemoteFeignServiceImpl service = newService(parkInternalService, "https://bridge-a.example.com");

		invokeInit(service);
		RemoteBridgeService cachedBridge = service.getBridge(1);
		invokeInit(service);

		Assert.assertSame("上游失败不是撤销信号，必须保留上次成功同步的客户端", cachedBridge, service.getBridge(1));
	}

	@Test
	public void revokesCachedBridgeWhenRefreshedAllowlistRemovesTheTarget() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))));
		DispatcherBridgeTargetProperties properties = new DispatcherBridgeTargetProperties();
		properties.setBridgeTargetAllowlist("https://bridge-a.example.com");
		RemoteFeignServiceImpl service = newService(parkInternalService, properties);

		invokeInit(service);
		Assert.assertNotNull("刷新前的合法白名单必须创建客户端", service.getBridge(1));

		properties.setBridgeTargetAllowlist("");
		invokeInit(service);

		Assert.assertNull("白名单刷新移除目标后，下一次成功同步必须撤销旧客户端", service.getBridge(1));
		Assert.assertTrue("白名单属性必须在 Nacos 刷新时重建",
				DispatcherBridgeTargetProperties.class.isAnnotationPresent(RefreshScope.class));
	}

	@Test
	public void revokesCachedBridgeWhenTargetFetchFailsAfterAllowlistRefresh() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(failedTargets());
		DispatcherBridgeTargetProperties properties = new DispatcherBridgeTargetProperties();
		properties.setBridgeTargetAllowlist("https://bridge-a.example.com");
		RemoteFeignServiceImpl service = newService(parkInternalService, properties);

		invokeInit(service);
		properties.setBridgeTargetAllowlist("");
		invokeInit(service);

		Assert.assertNull("白名单刷新移除目标后，即使上游同步失败也不得返回旧客户端", service.getBridge(1));
	}

	@Test
	public void keepsCachedBridgeWhenSuccessfulTargetResponseHasNullData() throws Exception {
		RemoteParkInternalService parkInternalService = Mockito.mock(RemoteParkInternalService.class);
		Mockito.when(parkInternalService.getBridgeTargets(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED))
				.thenReturn(successfulTargets(Collections.singletonList(bridgeTarget(1, "https://bridge-a.example.com"))))
				.thenReturn(successfulTargets(null));
		RemoteFeignServiceImpl service = newService(parkInternalService, "https://bridge-a.example.com");

		invokeInit(service);
		RemoteBridgeService cachedBridge = service.getBridge(1);
		invokeInit(service);

		Assert.assertSame("成功但没有目标数据是无效响应，不能清空最近一次成功快照", cachedBridge, service.getBridge(1));
	}

	private RemoteFeignServiceImpl newService(RemoteParkInternalService parkInternalService, String allowlist) throws Exception {
		DispatcherBridgeTargetProperties properties = new DispatcherBridgeTargetProperties();
		properties.setBridgeTargetAllowlist(allowlist);
		return newService(parkInternalService, properties);
	}

	private RemoteFeignServiceImpl newService(RemoteParkInternalService parkInternalService,
			DispatcherBridgeTargetProperties properties) throws Exception {
		RemoteFeignServiceImpl service = new RemoteFeignServiceImpl();
		setField(service, "remoteParkInternalService", parkInternalService);
		setField(service, "internalServiceTokenInterceptor", Mockito.mock(RequestInterceptor.class));
		setField(service, "bridgeTargetProperties", properties);
		return service;
	}

	private InternalParkBridgeTargetRespDTO bridgeTarget(Integer id, String bridgeUrl) {
		InternalParkBridgeTargetRespDTO target = new InternalParkBridgeTargetRespDTO();
		target.setId(id);
		target.setBridgeUrl(bridgeUrl);
		return target;
	}

	private Result<List<InternalParkBridgeTargetRespDTO>> successfulTargets(List<InternalParkBridgeTargetRespDTO> targets) {
		return new Result<>(targets);
	}

	private Result<List<InternalParkBridgeTargetRespDTO>> failedTargets() {
		return new Result<>(new IllegalStateException("上游不可用"));
	}

	private void invokeInit(RemoteFeignServiceImpl service) throws Exception {
		Method init = RemoteFeignServiceImpl.class.getDeclaredMethod("init");
		init.setAccessible(true);
		init.invoke(service);
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
