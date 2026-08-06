package com.tce.smart.data.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.feign.temporary.RemoteEPhotoService;
import com.tce.smart.data.api.feign.temporary.RemoteEleaveJjitemService;
import com.tce.smart.data.api.feign.temporary.RemoteEstaffRegisterService;
import com.tce.smart.data.controller.temporary.EPhotoController;
import com.tce.smart.data.controller.temporary.EleaveJjitemController;
import com.tce.smart.data.controller.temporary.EstaffRegisterController;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 临时人事写入接口的内部调用安全契约。
 *
 * 这些接口接收人脸、入职和离职交接数据，只允许持有服务令牌的服务间调用，
 * 不能继续作为任意已认证用户可访问的外部写入端点。
 */
public class SmartDataTemporaryInnerRouteContractTest {

	@Test
	public void temporaryWriteRoutesRequireInternalServerScope() {
		assertInternalServerRoute(EPhotoController.class, "/ephoto", "/internal/save");
		assertInternalServerRoute(EstaffRegisterController.class, "/estaffRegister", "/internal/save");
		assertInternalServerRoute(EleaveJjitemController.class, "/eleaveJjitem", "/internal/save");
		assertInternalServerRoute(EleaveJjitemController.class, "/eleaveJjitem", "/internal/save/batch");
	}

	@Test
	public void temporaryWriteFeignContractsRequireServiceTokenMarker() {
		assertFeignContract(RemoteEPhotoService.class, "/ephoto/internal/save");
		assertFeignContract(RemoteEstaffRegisterService.class, "/estaffRegister/internal/save");
		assertFeignContract(RemoteEleaveJjitemService.class, "/eleaveJjitem/internal/save");
		assertFeignContract(RemoteEleaveJjitemService.class, "/eleaveJjitem/internal/save/batch");
	}

	private void assertInternalServerRoute(Class<?> controllerType, String expectedBasePath, String expectedMethodPath) {
		Assert.assertEquals("控制器基础路径必须保持精确", expectedBasePath,
				controllerType.getAnnotation(RequestMapping.class).value()[0]);
		Method method = findPostMappingMethod(controllerType, expectedMethodPath);
		Assert.assertNotNull(expectedMethodPath + " 必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull(expectedMethodPath + " 必须声明 @OpenApi", openApi);
		Assert.assertEquals(expectedMethodPath + " 必须只接受 server 服务令牌", "server", openApi.value());
	}

	private void assertFeignContract(Class<?> feignType, String expectedPath) {
		Method method = findPostMappingMethod(feignType, expectedPath);
		assertRequestHeader(method, SecurityConstants.FROM);
		assertRequestHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH);
	}

	private Method findPostMappingMethod(Class<?> type, String expectedPath) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(PostMapping.class))
				.filter(method -> expectedPath.equals(method.getAnnotation(PostMapping.class).value()[0]))
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少 POST 路由 " + expectedPath));
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
}
