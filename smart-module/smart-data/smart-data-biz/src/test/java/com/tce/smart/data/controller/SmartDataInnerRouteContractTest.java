package com.tce.smart.data.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.feign.xcc6.RemoteXCRsEmpService;
import com.tce.smart.data.api.feign.xcvehicle.RemoteXCVehicleService;
import com.tce.smart.data.api.feign.dhrview.RemoteYutoDhrYsService;
import com.tce.smart.data.controller.dhrview.YutoDhrPsndoController;
import com.tce.smart.data.controller.xcc6.RsXCEmpController;
import com.tce.smart.data.controller.xcvehicle.TParkCardController;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
		assertInternalServerRoute(RsXCEmpController.class, "saveEmp", "/xc-rsemp/inner/saveEmp", PostMapping.class);
		assertInternalServerRoute(RsXCEmpController.class, "leaveEmp", "/xc-rsemp/inner/leaveEmp", PostMapping.class);
		assertInternalServerRoute(RsXCEmpController.class, "intoEmp", "/xc-rsemp/inner/intoEmp", PostMapping.class);
		assertInternalServerRoute(RsXCEmpController.class, "getEmpPhoto", "/xc-rsemp/inner/get-empPhoto/{empNo}", GetMapping.class);
	}

	@Test
	public void xcVehicleRoutesRequireInternalServerScope() {
		assertInternalServerRoute(TParkCardController.class, "saveVehicle", "/xc-vehicle/inner/saveVehicle", PostMapping.class);
		assertInternalServerRoute(TParkCardController.class, "deleteVehicle", "/xc-vehicle/inner/deleteVehicle/{cardNo}", PostMapping.class);
	}

	@Test
	public void xcC6EmployeeFeignRoutesRequireServiceTokenMarker() {
		assertFeignContract(RemoteXCRsEmpService.class, "saveEmp", "/xc-rsemp/inner/saveEmp", PostMapping.class);
		assertFeignContract(RemoteXCRsEmpService.class, "leaveEmp", "/xc-rsemp/inner/leaveEmp", PostMapping.class);
		assertFeignContract(RemoteXCRsEmpService.class, "intoEmp", "/xc-rsemp/inner/intoEmp", PostMapping.class);
		assertFeignContract(RemoteXCRsEmpService.class, "getEmpPhoto", "/xc-rsemp/inner/get-empPhoto/{empNo}", GetMapping.class);
	}

	@Test
	public void xcVehicleFeignRoutesRequireServiceTokenMarker() {
		assertFeignContract(RemoteXCVehicleService.class, "saveVehicle", "/xc-vehicle/inner/saveVehicle", PostMapping.class);
		assertFeignContract(RemoteXCVehicleService.class, "deleteVehicle", "/xc-vehicle/inner/deleteVehicle/{cardNo}", PostMapping.class);
	}

	@Test
	public void dhrEmployeePageRouteRequiresInternalServerScope() {
		Method method = findMethod(YutoDhrPsndoController.class, "page");
		Assert.assertEquals("DHR 员工分页基础路径必须保持精确",
				"/empdhr/ys", YutoDhrPsndoController.class.getAnnotation(RequestMapping.class).value()[0]);
		assertMappingPath(method, "/internal/page", GetMapping.class);
		Assert.assertNotNull("DHR 员工分页必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull("DHR 员工分页必须声明 @OpenApi", openApi);
		Assert.assertEquals("DHR 员工分页必须只接受 server 服务令牌", "server", openApi.value());
	}

	@Test
	public void dhrEmployeePageFeignRouteRequiresServiceTokenMarker() {
		assertFeignContract(RemoteYutoDhrYsService.class, "page", "/empdhr/ys/internal/page", GetMapping.class);
	}

	private void assertInternalServerRoute(Class<?> controllerType, String methodName, String expectedPath,
			Class<?> mappingType) {
		Method method = findMethod(controllerType, methodName);
		Assert.assertEquals(controllerType.getName() + " 基础路径不符合内部 C6 契约",
				expectedBasePath(expectedPath), controllerType.getAnnotation(RequestMapping.class).value()[0]);
		assertMappingPath(method, expectedPath.substring(expectedBasePath(expectedPath).length()), mappingType);
		Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
		Assert.assertEquals(methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
	}

	private void assertFeignContract(Class<?> feignType, String methodName, String expectedPath, Class<?> mappingType) {
		Method method = findMethod(feignType, methodName);
		assertMappingPath(method, expectedPath, mappingType);
		assertRequestHeader(method, SecurityConstants.FROM);
		assertRequestHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH);
	}

	private void assertMappingPath(Method method, String expectedPath, Class<?> mappingType) {
		String methodPath;
		if (PostMapping.class.equals(mappingType)) {
			methodPath = method.getAnnotation(PostMapping.class).value()[0];
		} else {
			methodPath = method.getAnnotation(GetMapping.class).value()[0];
		}
		Assert.assertEquals(method.getName() + " 路径必须与内部契约精确一致",
			expectedPath, methodPath);
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

	private String expectedBasePath(String expectedPath) {
		return expectedPath.substring(0, expectedPath.indexOf("/inner"));
	}

	private Method findMethod(Class<?> type, String methodName) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> methodName.equals(method.getName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少方法 " + methodName));
	}
}
