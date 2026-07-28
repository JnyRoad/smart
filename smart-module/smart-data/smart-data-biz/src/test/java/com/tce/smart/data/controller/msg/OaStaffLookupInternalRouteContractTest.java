package com.tce.smart.data.controller.msg;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.dto.msg.resp.OaStaffLookupRespDTO;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.vo.msg.QueryOaStaffRespVo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OA 员工查询只允许内部服务使用，并且不得把 OA 原始员工档案透传给调用方。
 */
public class OaStaffLookupInternalRouteContractTest {

	@Test
	public void staffLookupRequiresInternalServiceTokenAndMinimalResponse() {
		Method controllerMethod = findMethod(OaDataManageController.class, "getOAInfoByBadge", 1);
		GetMapping controllerMapping = controllerMethod.getAnnotation(GetMapping.class);
		Assert.assertNotNull("OA 员工查询必须保留 GET 映射", controllerMapping);
		Assert.assertArrayEquals("OA 员工查询不得保留外部路由",
				new String[] {"/internal/staff/info/{badge}"}, controllerMapping.value());
		Assert.assertNotNull("OA 员工查询必须声明 @Inner", controllerMethod.getAnnotation(Inner.class));
		OpenApi controllerOpenApi = controllerMethod.getAnnotation(OpenApi.class);
		Assert.assertNotNull("OA 员工查询必须声明 @OpenApi", controllerOpenApi);
		Assert.assertEquals("OA 员工查询必须只接受 server 服务令牌", "server", controllerOpenApi.value());
		assertMinimalLookupResponse(controllerMethod);

		Method feignMethod = findMethod(RemoteOaWorkFlowService.class, "getOAInfoByBadge", 3);
		GetMapping feignMapping = feignMethod.getAnnotation(GetMapping.class);
		Assert.assertNotNull("OA 员工查询 Feign 必须保留 GET 映射", feignMapping);
		Assert.assertArrayEquals("OA 员工查询 Feign 路径必须与内部入口一致",
				new String[] {"/oarmanage/internal/staff/info/{badge}"}, feignMapping.value());
		assertRequestHeader(feignMethod, SecurityConstants.FROM);
		assertRequestHeader(feignMethod, SecurityConstants.INTERNAL_SERVICE_AUTH);
		assertMinimalLookupResponse(feignMethod);
	}

	@Test
	public void upstreamOaStaffModelKeepsOnlyWorkflowRequiredFields() {
		Set<String> expectedFields = new HashSet<>(Arrays.asList(
				"ID", "LASTNAME", "DEPARTMENTID", "SUBCOMPANYID1", "JOBTITLE"));
		Set<String> actualFields = Arrays.stream(QueryOaStaffRespVo.class.getDeclaredFields())
				.map(Field::getName)
				.filter(fieldName -> !"serialVersionUID".equals(fieldName))
				.collect(Collectors.toSet());
		Assert.assertEquals("OA 原始员工模型不得保留密码或无业务用途的员工字段", expectedFields, actualFields);
	}

	@Test
	public void lookupResponseContainsOnlyIdAndName() {
		Set<String> fields = Arrays.stream(OaStaffLookupRespDTO.class.getDeclaredFields())
				.map(Field::getName)
				.filter(fieldName -> !"serialVersionUID".equals(fieldName))
				.collect(Collectors.toSet());
		Assert.assertEquals("OA 员工查询响应只能包含物品放行需要的 id 与 name",
				new HashSet<>(Arrays.asList("id", "name")), fields);
	}

	private void assertMinimalLookupResponse(Method method) {
		Type returnType = method.getGenericReturnType();
		Assert.assertTrue(method.getName() + " 必须返回参数化 Result", returnType instanceof ParameterizedType);
		Type responseType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
		Assert.assertEquals(method.getName() + " 只能返回最小 OA 员工查询 DTO",
				"com.tce.smart.data.api.dto.msg.resp.OaStaffLookupRespDTO", responseType.getTypeName());
	}

	private void assertRequestHeader(Method method, String expectedHeader) {
		boolean present = Arrays.stream(method.getParameters())
				.map(Parameter::getAnnotations)
				.flatMap(Arrays::stream)
				.filter(RequestHeader.class::isInstance)
				.map(RequestHeader.class::cast)
				.anyMatch(header -> expectedHeader.equals(header.value()));
		Assert.assertTrue(method.getName() + " 必须声明请求头 " + expectedHeader, present);
	}

	private Method findMethod(Class<?> type, String methodName, int parameterCount) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> methodName.equals(method.getName()) && parameterCount == method.getParameterCount())
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少目标方法 " + methodName));
	}
}
