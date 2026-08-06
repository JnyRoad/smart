package com.tce.smart.data.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/** SmartData 库存 B 组纯内部 EHR 读取路由的服务令牌契约。 */
public class SmartDataBlockedRouteBContractTest {

	@Test
	public void blockedInternalRoutesRequireServerScope() throws Exception {
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.EvwLergotAllController", "list", "getByBadge");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.EvwCcdFlstandardController", "getById");
		assertOpenApiServer("com.tce.smart.data.controller.businesstrip.FormtableHrController", "infoPerson");
		assertOpenApiServer("com.tce.smart.data.controller.businesstrip.FormtableMainController", "info", "infoTravel", "infoDay", "infoReport");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.EvwJjitemController", "info");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.LvwAcardlostController", "getByBadge");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.LvwLeavetypeController", "getById");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.LvwLcdLeavetypeController", "info");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.EvwLregLeaveAllController", "info");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.OvwYsCallOwanceCancelController", "getInfo");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.OvwYsCallOwanceDetailsController", "getInfo", "getInfoByTime", "getInfoByTimeList", "getInfoByBadge");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.LvwAyearholidayController", "info");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.YotoDhrOrgsController", "getByCompId", "getList");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.OvwYsConComanyController", "getByTitle", "getByCompId");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.YutoDhrDeptController", "getByCompId", "getByDepId", "getParentDep");
		assertOpenApiServer("com.tce.smart.data.controller.ehrview.OvwYsjobController", "getByDeptId", "getByCompId", "getListByCompId", "getJChenList", "getByDeptName");
	}

	@Test
	public void blockedInternalFeignRoutesRequireExplicitServiceTokenMarker() throws Exception {
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteEvwLergotAllService", "list", "getByBadge");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteEvwCcdFlstandardService", "getById");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.businesstrip.RemoteFormTableMainService", "info", "infoTravel", "infoDay", "infoReport", "infoPerson");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteEvwJjitemService", "info");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteLvwAcardlostService", "getByBadge");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteLvwLeavetypeService", "getById");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteLvwLcdLeavetypeService", "info");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteEvwLregLeaveAllService", "info");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteOvwYsCallOwanceCancelService", "getInfo");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteOvwYsCallOwanceDetailsService", "getInfo", "getInfoByTime", "getInfoByTimeList", "getInfoByBadge");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteLvwAyearholidayService", "info");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService", "getByCompId", "getList");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteOvwYsConComanyService", "getByTitle", "getByCompId");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteOvwYsdepService", "getByCompId", "getByDepId", "getParentDep");
		assertFeignServiceTokenHeaders("com.tce.smart.data.api.feign.ehrview.RemoteOvwYsjobService", "getByDeptId", "getByCompId", "getListByCompId", "getJChenList", "getByDeptName");
	}

	private void assertOpenApiServer(String controllerClassName, String... methodNames) throws Exception {
		Class<?> controllerType = Class.forName(controllerClassName);
		for (String methodName : methodNames) {
			Method method = findMethod(controllerType, methodName);
			Assert.assertNotNull(controllerClassName + "#" + methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
			OpenApi openApi = method.getAnnotation(OpenApi.class);
			Assert.assertNotNull(controllerClassName + "#" + methodName + " 必须声明 @OpenApi", openApi);
			Assert.assertEquals(controllerClassName + "#" + methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
		}
	}

	private void assertFeignServiceTokenHeaders(String feignClassName, String... methodNames) throws Exception {
		Class<?> feignType = Class.forName(feignClassName);
		for (String methodName : methodNames) {
			Method method = findMethod(feignType, methodName);
			Assert.assertTrue(feignClassName + "#" + methodName + " 必须声明 FROM 头", hasHeader(method, SecurityConstants.FROM));
			Assert.assertTrue(feignClassName + "#" + methodName + " 必须声明服务令牌标记头",
					hasHeader(method, SecurityConstants.INTERNAL_SERVICE_AUTH));
		}
	}

	private boolean hasHeader(Method method, String expectedHeader) {
		return Arrays.stream(method.getParameters())
				.map(Parameter::getAnnotations)
				.flatMap(Arrays::stream)
				.filter(RequestHeader.class::isInstance)
				.map(RequestHeader.class::cast)
				.anyMatch(header -> expectedHeader.equals(header.value()));
	}

	private Method findMethod(Class<?> type, String methodName) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> methodName.equals(method.getName()))
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少方法 " + methodName));
	}
}
