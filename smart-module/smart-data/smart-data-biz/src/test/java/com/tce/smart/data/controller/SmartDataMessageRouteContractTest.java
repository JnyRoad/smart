package com.tce.smart.data.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.data.api.feign.msg.RemoteEmailManagerService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.data.controller.msg.EmailManagerController;
import com.tce.smart.data.controller.msg.SmsManageController;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 消息发送端点只能由持有服务令牌的内部调用方访问，避免外部请求直接指定收件人或短信正文。
 */
public class SmartDataMessageRouteContractTest {

	@Test
	public void emailRoutesRequireInternalServerScope() {
		assertControllerRoute(EmailManagerController.class, "sendEmail", "/internal/send/email", PostMapping.class);
		assertControllerRoute(EmailManagerController.class, "sendEmails", "/internal/send/emails", PostMapping.class);
		assertControllerRoute(EmailManagerController.class, "sendEmailsWithContent", "/internal/send/emailwithcontent", PostMapping.class);
		assertControllerRoute(EmailManagerController.class, "getTemplateKey", "/internal/template/{tempCode}", PostMapping.class);
	}

	@Test
	public void smsRoutesRequireInternalServerScope() {
		assertControllerRoute(SmsManageController.class, "sendAppointmentSms", "/internal/send/appointment", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendVisitorProxySms", "/internal/send/visitor/proxy", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendRecruitSms", "/internal/send/recruit", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendDimissionSms", "/internal/send/dimission", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendGuardSms", "/internal/send/guard", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendSmsCode", "/internal/send/smsCode", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendSmsError", "/internal/send/smsError", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendBadgeAgree", "/internal/send/badge/agree", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendBadgeRefuse", "/internal/send/badge/refuse", PostMapping.class);
		assertControllerRoute(SmsManageController.class, "sendAttendanceSign", "/internal/send/attendance/sign", GetMapping.class);
		assertControllerRoute(SmsManageController.class, "sendWageSign", "/internal/send/wage/sign", GetMapping.class);
		assertControllerRoute(SmsManageController.class, "sendArticlesRelease", "/internal/send/articlesrelease/smscode", GetMapping.class);
		assertControllerRoute(SmsManageController.class, "sendMessage", "/internal/send/msg", GetMapping.class);
	}

	@Test
	public void emailFeignRoutesAlwaysUseServiceToken() {
		assertFeignRoute(RemoteEmailManagerService.class, "sendEmail", "/emailmanager/internal/send/email", PostMapping.class);
		assertFeignRoute(RemoteEmailManagerService.class, "sendEmails", "/emailmanager/internal/send/emails", PostMapping.class);
		assertFeignRoute(RemoteEmailManagerService.class, "sendEmailsWithContent", "/emailmanager/internal/send/emailwithcontent", PostMapping.class);
		assertFeignRoute(RemoteEmailManagerService.class, "getTemplateKey", "/emailmanager/internal/template/{tempCode}", PostMapping.class);
	}

	@Test
	public void smsFeignRoutesAlwaysUseServiceToken() {
		assertFeignRoute(RemoteSmsManageService.class, "sendAppointmentSms", "/smsmanage/internal/send/appointment", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendVisitorProxySms", "/smsmanage/internal/send/visitor/proxy", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendRecruitSms", "/smsmanage/internal/send/recruit", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendDimissionSms", "/smsmanage/internal/send/dimission", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendGuardSms", "/smsmanage/internal/send/guard", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendSmsCode", "/smsmanage/internal/send/smsCode", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendSmsError", "/smsmanage/internal/send/smsError", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendBadgeAgree", "/smsmanage/internal/send/badge/agree", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendBadgeRefuse", "/smsmanage/internal/send/badge/refuse", PostMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendAttendanceSign", "/smsmanage/internal/send/attendance/sign", GetMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendWageSign", "/smsmanage/internal/send/wage/sign", GetMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendArticlesRelease", "/smsmanage/internal/send/articlesrelease/smscode", GetMapping.class);
		assertFeignRoute(RemoteSmsManageService.class, "sendMessage", "/smsmanage/internal/send/msg", GetMapping.class);
	}

	private void assertControllerRoute(Class<?> controllerType, String methodName, String expectedPath,
			Class<?> mappingType) {
		Method method = findMethod(controllerType, methodName, 1);
		assertMappingPath(method, expectedPath, mappingType);
		Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
		Assert.assertEquals(methodName + " 必须只接受 server 服务令牌", "server", openApi.value());
	}

	private void assertFeignRoute(Class<?> feignType, String methodName, String expectedPath, Class<?> mappingType) {
		Method internalMethod = findMethod(feignType, methodName, 3);
		assertMappingPath(internalMethod, expectedPath, mappingType);
		assertRequestHeader(internalMethod, SecurityConstants.FROM);
		assertRequestHeader(internalMethod, SecurityConstants.INTERNAL_SERVICE_AUTH);

		Method facadeMethod = findMethod(feignType, methodName, 1);
		Assert.assertTrue(methodName + " 必须提供默认内部调用门面", facadeMethod.isDefault());
		Assert.assertNull(methodName + " 默认门面不得声明可被网关路由的映射", facadeMethod.getAnnotation(PostMapping.class));
		Assert.assertNull(methodName + " 默认门面不得声明可被网关路由的映射", facadeMethod.getAnnotation(GetMapping.class));
	}

	private void assertMappingPath(Method method, String expectedPath, Class<?> mappingType) {
		String[] paths = PostMapping.class.equals(mappingType)
				? method.getAnnotation(PostMapping.class).value()
				: method.getAnnotation(GetMapping.class).value();
		Assert.assertEquals(method.getName() + " 路径必须与内部契约精确一致", expectedPath, paths[0]);
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

	private Method findMethod(Class<?> type, String methodName, int parameterCount) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> methodName.equals(method.getName()) && method.getParameterCount() == parameterCount)
				.findFirst()
				.orElseThrow(() -> new AssertionError(type.getName() + " 缺少 " + methodName + " 的 " + parameterCount + " 参数契约"));
	}
}
