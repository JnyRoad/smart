package com.tce.smart.bridge.isc.controller;

import com.tce.smart.bridge.isc.api.feign.RemoteBridgeService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * ISC Bridge 路由的服务令牌契约。
 *
 * 海康订阅回调当前没有可验证的签名、时间窗或 nonce 证据，因此 /handle 也必须默认拒绝匿名请求。
 */
public class BridgeIscInternalRouteContractTest {

	@Test
	public void iscBridgeRoutesRequireInternalServerScope() {
		assertInternalServerRoute("dispatch", "/dispatch");
		assertInternalServerRoute("handle", "/handle");
		assertInternalServerRoute("getImage", "/image");
		assertInternalServerRoute("getThumbnail", "/thumbnail");
	}

	@Test
	public void remoteIscBridgeContractRequestsServiceToken() {
		assertFeignContract("dispatch", "/bridge/dispatch");
		assertFeignContract("getImage", "/bridge/image");
		assertFeignContract("getThumbnail", "/bridge/thumbnail");
	}

	private void assertInternalServerRoute(String methodName, String expectedPath) {
		Method method = findMethod(BridgeISCController.class, methodName);
		Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
		Assert.assertEquals(methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
		Assert.assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
	}

	private void assertFeignContract(String methodName, String expectedPath) {
		Method method = findMethod(RemoteBridgeService.class, methodName);
		Assert.assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
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
