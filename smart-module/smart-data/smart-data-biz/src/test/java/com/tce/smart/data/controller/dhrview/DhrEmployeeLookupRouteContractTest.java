package com.tce.smart.data.controller.dhrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.feign.dhrview.RemoteYutoDhrYsService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * DHR 员工标识查询的内部调用安全契约。
 *
 * 工号与员工性质只能由受控服务查询，不能作为外网已认证用户的枚举入口。
 */
public class DhrEmployeeLookupRouteContractTest {

	@Test
	public void lookupRoutesRequireInternalServerScope() {
		assertInternalRoute("getProperties", "/internal/properties");
		assertInternalRoute("getBadgeByUserId", "/internal/badge/{userId}");
	}

	@Test
	public void lookupFeignRoutesRequireServiceTokenMarker() {
		assertFeignRoute("getProperties", "/empdhr/ys/internal/properties");
		assertFeignRoute("getBadgeByUserId", "/empdhr/ys/internal/badge/{userId}");
	}

	private void assertInternalRoute(String methodName, String expectedPath) {
		Method method = findMethod(YutoDhrPsndoController.class, methodName);
		String[] mappings = method.getAnnotation(GetMapping.class).value();
		Assert.assertArrayEquals("内部查询不得保留旧公开路径别名", new String[]{expectedPath}, mappings);
		Assert.assertNotNull(method.getAnnotation(Inner.class));
		Assert.assertEquals("server", method.getAnnotation(OpenApi.class).value());
	}

	private void assertFeignRoute(String methodName, String expectedPath) {
		Method method = findMethod(RemoteYutoDhrYsService.class, methodName);
		Assert.assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
		assertHeader(method, SecurityConstants.FROM);
		assertHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH);
	}

	private void assertHeader(Method method, String expectedHeader) {
		boolean found = Arrays.stream(method.getParameters())
				.map(Parameter::getAnnotations)
				.flatMap(Arrays::stream)
				.filter(RequestHeader.class::isInstance)
				.map(RequestHeader.class::cast)
				.map(RequestHeader::value)
				.anyMatch(expectedHeader::equals);
		Assert.assertTrue(method.getName() + " 必须传递请求头 " + expectedHeader, found);
	}

	private Method findMethod(Class<?> type, String methodName) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> methodName.equals(method.getName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少方法 " + methodName));
	}
}
