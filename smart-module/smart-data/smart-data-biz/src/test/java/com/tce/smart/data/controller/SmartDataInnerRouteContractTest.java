package com.tce.smart.data.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.feign.xcc6.RemoteXCRsEmpService;
import com.tce.smart.data.api.feign.xcvehicle.RemoteXCVehicleService;
import com.tce.smart.data.controller.xcc6.RsXCEmpController;
import com.tce.smart.data.controller.xcvehicle.TParkCardController;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * smart-data 纯内部路由的安全契约。
 *
 * 许昌 C6 人员、车辆同步直接写入外部业务数据源，不能依赖 Nacos 白名单或可伪造的 URL 约定。
 * 该测试要求入站端点声明内部调用与服务 scope，并要求 Feign 契约显式触发服务令牌。
 */
public class SmartDataInnerRouteContractTest {

	@Test
	public void xcC6EmployeeRoutesRequireInternalServerScope() {
		assertInternalServerRoute(RsXCEmpController.class, "saveEmp");
		assertInternalServerRoute(RsXCEmpController.class, "leaveEmp");
		assertInternalServerRoute(RsXCEmpController.class, "intoEmp");
		assertInternalServerRoute(RsXCEmpController.class, "getEmpPhoto");
	}

	@Test
	public void xcVehicleRoutesRequireInternalServerScope() {
		assertInternalServerRoute(TParkCardController.class, "saveVehicle");
		assertInternalServerRoute(TParkCardController.class, "deleteVehicle");
	}

	@Test
	public void xcC6EmployeeFeignRoutesRequireServiceTokenMarker() {
		assertServiceTokenHeader(RemoteXCRsEmpService.class, "saveEmp");
		assertServiceTokenHeader(RemoteXCRsEmpService.class, "leaveEmp");
		assertServiceTokenHeader(RemoteXCRsEmpService.class, "intoEmp");
		assertServiceTokenHeader(RemoteXCRsEmpService.class, "getEmpPhoto");
	}

	@Test
	public void xcVehicleFeignRoutesRequireServiceTokenMarker() {
		assertServiceTokenHeader(RemoteXCVehicleService.class, "saveVehicle");
		assertServiceTokenHeader(RemoteXCVehicleService.class, "deleteVehicle");
	}

	private void assertInternalServerRoute(Class<?> controllerType, String methodName) {
		Method method = findMethod(controllerType, methodName);
		Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
		Assert.assertEquals(methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
	}

	private void assertServiceTokenHeader(Class<?> feignType, String methodName) {
		Method method = findMethod(feignType, methodName);
		boolean hasServiceTokenMarker = Arrays.stream(method.getParameters())
				.map(Parameter::getAnnotations)
				.flatMap(Arrays::stream)
				.filter(RequestHeader.class::isInstance)
				.map(RequestHeader.class::cast)
				.anyMatch(header -> SecurityConstants.INTERNAL_SERVICE_AUTH.equals(header.value()));
		Assert.assertTrue(methodName + " 必须显式声明内部服务令牌标记", hasServiceTokenMarker);
	}

	private Method findMethod(Class<?> type, String methodName) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> methodName.equals(method.getName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少方法 " + methodName));
	}
}
