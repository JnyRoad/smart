package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
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
 * 算法图片处理端点的内部服务安全契约。
 *
 * 人脸检测、OCR、比对、特征提取和裁剪均可能处理员工证件或人脸图像，
 * 不能因本地 Nacos 白名单误配而退化为外网匿名图片处理 API。
 */
public class AlgorithmInternalImageRouteContractTest {

	@Test
	public void imageProcessingPostRoutesRequireInternalServerScope() {
		for (Class<?> controllerType : imageProcessingControllers()) {
			Method[] handlers = Arrays.stream(controllerType.getDeclaredMethods())
					.filter(method -> method.getAnnotation(PostMapping.class) != null)
					.toArray(Method[]::new);
			Assert.assertTrue(controllerType.getSimpleName() + " 必须至少包含一个图片处理 POST 路由", handlers.length > 0);
			for (Method handler : handlers) {
				Assert.assertNotNull(controllerType.getSimpleName() + "." + handler.getName()
						+ " 必须声明 @Inner", handler.getAnnotation(Inner.class));
				OpenApi openApi = handler.getAnnotation(OpenApi.class);
				Assert.assertNotNull(controllerType.getSimpleName() + "." + handler.getName()
						+ " 必须声明 @OpenApi", openApi);
				Assert.assertEquals(controllerType.getSimpleName() + "." + handler.getName()
						+ " 只能接收 server 服务令牌", "server", openApi.value());
			}
		}
	}

	@Test
	public void internalAlgorithmFeignRoutesRequireOriginAndServiceTokenMarkers() {
		for (Method method : RemoteAlgorithmService.class.getDeclaredMethods()) {
			Assert.assertNotNull(method.getName() + " 必须使用内部映射", method.getAnnotation(PostMapping.class));
			assertRequestHeader(method, SecurityConstants.FROM);
			assertRequestHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH);
		}
	}

	private Class<?>[] imageProcessingControllers() {
		return new Class<?>[]{
				CompareApiController.class,
				CompareController.class,
				FaceDetectApiController.class,
				FaceDetectController.class,
				OcrApiController.class,
				OcrController.class,
				FaceController.class,
				FaceImgCutController.class,
				TestController.class
		};
	}

	private void assertRequestHeader(Method method, String expectedHeader) {
		boolean present = Arrays.stream(method.getParameters())
				.map(Parameter::getAnnotations)
				.flatMap(Arrays::stream)
				.filter(RequestHeader.class::isInstance)
				.map(RequestHeader.class::cast)
				.map(RequestHeader::value)
				.anyMatch(expectedHeader::equals);
		Assert.assertTrue(method.getName() + " 必须显式声明请求头 " + expectedHeader, present);
	}
}
