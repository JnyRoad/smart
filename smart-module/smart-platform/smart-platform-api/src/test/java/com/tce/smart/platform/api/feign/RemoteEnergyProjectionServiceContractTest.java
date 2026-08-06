package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 能耗投影内部 Feign 调用的 HTTP 契约测试。
 */
public class RemoteEnergyProjectionServiceContractTest {

	@Test
	public void energyProjectionEndpointsRequireInternalSourceAndBearerAuthorization() throws Exception {
		assertContract("processPending", new Class<?>[] { String.class, String.class },
				"/inner/energy/projection/process-pending", 0, 1);
		assertContract("reconcile", new Class<?>[] { String.class, String.class, String.class },
				"/inner/energy/projection/reconcile/{businessDate}", 1, 2);
		assertContract("backfillMonthToDate", new Class<?>[] { String.class, String.class },
				"/inner/energy/projection/backfill-month-to-date", 0, 1);
		assertContract("daily", new Class<?>[] { String.class, boolean.class, boolean.class, String.class, String.class },
				"/inner/energy/projection/daily/{businessDate}", 3, 4);
	}

	/**
	 * 逐个验证路径和两个安全请求头，避免调度端在无 Web 请求上下文时丢失 Bearer 凭证。
	 */
	private void assertContract(String methodName, Class<?>[] parameterTypes, String expectedPath, int fromIndex,
			int authorizationIndex) throws Exception {
		Method method = RemoteEnergyProjectionService.class.getMethod(methodName, parameterTypes);
		PostMapping mapping = method.getAnnotation(PostMapping.class);
		Assert.assertNotNull(mapping);
		Assert.assertArrayEquals(new String[] { expectedPath }, mapping.value());
		assertRequiredHeader(method, fromIndex, SecurityConstants.FROM);
		assertRequiredHeader(method, authorizationIndex, HttpHeaders.AUTHORIZATION);
	}

	/**
	 * 请求头参数必须是必填字符串，防止 Feign 将安全头静默省略。
	 */
	private void assertRequiredHeader(Method method, int parameterIndex, String expectedHeaderName) {
		Assert.assertEquals(String.class, method.getParameterTypes()[parameterIndex]);
		RequestHeader requestHeader = findRequestHeader(method.getParameterAnnotations()[parameterIndex]);
		Assert.assertNotNull(requestHeader);
		Assert.assertEquals(expectedHeaderName, requestHeader.value());
		Assert.assertTrue(requestHeader.required());
	}

	private RequestHeader findRequestHeader(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof RequestHeader) {
				return (RequestHeader) annotation;
			}
		}
		return null;
	}
}
