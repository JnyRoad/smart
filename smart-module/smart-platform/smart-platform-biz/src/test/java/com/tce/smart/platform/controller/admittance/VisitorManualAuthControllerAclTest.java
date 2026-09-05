package com.tce.smart.platform.controller.admittance;

import com.tce.smart.platform.api.dto.req.admittance.VisitorManualAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorManualAuthOptionsRespDTO;
import com.tce.smart.common.core.wrapper.ControllerWrapper;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.VisitorManualAuthService;
import com.tce.smart.common.security.service.SmartUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 管理端手动授权路由必须使用独立的权限码，防止落入访客 H5 历史放行路径。
 */
public class VisitorManualAuthControllerAclTest {

	@Test
	public void manualAuthEndpointsUseProtectedManagementRoutes() throws Exception {
		Class<SmtAdmittanceApplyManageController> controllerType = SmtAdmittanceApplyManageController.class;
		RequestMapping requestMapping = controllerType.getAnnotation(RequestMapping.class);
		Assert.assertNotNull(requestMapping);
		Assert.assertTrue(requestMapping.value()[0].startsWith("/manage/"));

		Method options = controllerType.getDeclaredMethod("getManualAuthOptions", String.class);
		GetMapping getMapping = options.getAnnotation(GetMapping.class);
		PreAuthorize optionsAcl = options.getAnnotation(PreAuthorize.class);
		Assert.assertNotNull(getMapping);
		Assert.assertEquals("/device/auth/options", getMapping.value()[0]);
		Assert.assertNotNull(optionsAcl);
		Assert.assertTrue(optionsAcl.value().contains("platform_visitor_incoming_auth"));

		Method submit = controllerType.getDeclaredMethod("manualAuth", VisitorManualAuthReqDTO.class);
		PostMapping postMapping = submit.getAnnotation(PostMapping.class);
		PreAuthorize submitAcl = submit.getAnnotation(PreAuthorize.class);
		Assert.assertNotNull(postMapping);
		Assert.assertEquals("/device/auth", postMapping.value()[0]);
		Assert.assertNotNull(submitAcl);
		Assert.assertTrue(submitAcl.value().contains("platform_visitor_incoming_auth"));
	}

	@Test
	public void manualAuthDelegatesAndReturnsBatchId() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorManualAuthService manualAuthService = Mockito.mock(VisitorManualAuthService.class);
		SmtAdmittanceApplyManageController controller =
				new SmtAdmittanceApplyManageController(applyService, manualAuthService);
		VisitorManualAuthReqDTO request = new VisitorManualAuthReqDTO();
		request.setApplyId(101L);
		Mockito.when(manualAuthService.submit(request)).thenReturn("9001");

		Assert.assertEquals("9001", controller.manualAuth(request).getData());
		Mockito.verify(manualAuthService).submit(request);
	}

	@Test
	public void optionsParsesPositiveApplyIdBeforeDelegating() {
		SmtAdmittanceApplyService applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		VisitorManualAuthService manualAuthService = Mockito.mock(VisitorManualAuthService.class);
		SmtAdmittanceApplyManageController controller =
				new SmtAdmittanceApplyManageController(applyService, manualAuthService);
		VisitorManualAuthOptionsRespDTO response = new VisitorManualAuthOptionsRespDTO();
		Mockito.when(manualAuthService.getOptions(101L)).thenReturn(response);

		Assert.assertSame(response, controller.getManualAuthOptions("101").getData());
		Mockito.verify(manualAuthService).getOptions(101L);
	}

	@Test
	public void methodSecurityBlocksUnauthorizedCallerBeforeBusinessService() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MethodSecurityTestConfig.class);
		try {
			SmtAdmittanceApplyManageController controller = context.getBean(SmtAdmittanceApplyManageController.class);
			VisitorManualAuthService manualAuthService = context.getBean(VisitorManualAuthService.class);
			PermissionChecker permissionChecker = context.getBean(PermissionChecker.class);
			SmartUser user = new SmartUser(1, 1, "tester", Collections.singletonList(7),
					"password", true, true, true, true, Collections.emptyList());
			SecurityContextHolder.clearContext();
			try {
				controller.getManualAuthOptions("101");
				Assert.fail("匿名调用手动授权选项必须被 Spring Method Security 拒绝");
			} catch (AuthenticationCredentialsNotFoundException expected) {
				// 预期拒绝，确认匿名请求未进入业务服务。
			}
			Mockito.verifyZeroInteractions(manualAuthService);

			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(user, "password", Collections.emptyList()));

			permissionChecker.allowed = false;
			try {
				controller.getManualAuthOptions("101");
				Assert.fail("缺少手动授权权限的 GET 必须被 Spring Method Security 拒绝");
			} catch (AccessDeniedException expected) {
				// 预期拒绝，确认未进入业务服务。
			}
			Mockito.verifyZeroInteractions(manualAuthService);

			try {
				controller.manualAuth(new VisitorManualAuthReqDTO());
				Assert.fail("缺少手动授权权限必须被 Spring Method Security 拒绝");
			} catch (AccessDeniedException expected) {
				// 预期拒绝，确认未进入业务服务。
			}
			Mockito.verifyZeroInteractions(manualAuthService);

			permissionChecker.allowed = true;
			Mockito.when(manualAuthService.submit(Mockito.any())).thenReturn("9001");
			Assert.assertEquals("9001", controller.manualAuth(new VisitorManualAuthReqDTO()).getData());
			Mockito.verify(manualAuthService).submit(Mockito.any(VisitorManualAuthReqDTO.class));
		} finally {
			SecurityContextHolder.clearContext();
			context.close();
		}
	}

	/**
	 * 仅提供本测试所需的 pms 权限表达式 bean，不启动 HTTP、数据库或真实权限服务。
	 */
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	public static class MethodSecurityTestConfig {
		@Bean
		public ControllerWrapper controllerWrapper() {
			return new ControllerWrapper();
		}

		@Bean
		public PermissionChecker pms() {
			return new PermissionChecker();
		}

		@Bean
		public SmtAdmittanceApplyService smtAdmittanceApplyService() {
			return Mockito.mock(SmtAdmittanceApplyService.class);
		}

		@Bean
		public VisitorManualAuthService visitorManualAuthService() {
			return Mockito.mock(VisitorManualAuthService.class);
		}

		@Bean
		public SmtAdmittanceApplyManageController smtAdmittanceApplyManageController(
				SmtAdmittanceApplyService applyService, VisitorManualAuthService manualAuthService) {
			return new SmtAdmittanceApplyManageController(applyService, manualAuthService);
		}
	}

	/**
	 * 模拟项目 pms bean 的最小权限判断，权限值由测试切换。
	 */
	public static class PermissionChecker {
		private boolean allowed;

		public boolean hasPermission(String permission) {
			return allowed && "platform_visitor_incoming_auth".equals(permission);
		}
	}
}
