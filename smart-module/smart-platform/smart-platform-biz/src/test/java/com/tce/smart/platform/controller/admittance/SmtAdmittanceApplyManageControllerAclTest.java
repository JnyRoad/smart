package com.tce.smart.platform.controller.admittance;

import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** 管理端作废接口不得落在访客 H5 放行的 /admittance/** 路径下。 */
public class SmtAdmittanceApplyManageControllerAclTest {

	@Test
	public void revokeUsesProtectedManagementRouteAndDedicatedPermission() throws Exception {
		Class<?> controllerType;
		try {
			controllerType = Class.forName("com.tce.smart.platform.controller.admittance.SmtAdmittanceApplyManageController");
		} catch (ClassNotFoundException exception) {
			controllerType = null;
		}
		assertNotNull("必须提供独立的管理端作废控制器", controllerType);

		RequestMapping requestMapping = controllerType.getAnnotation(RequestMapping.class);
		assertNotNull(requestMapping);
		assertTrue("管理端接口不能位于 /admittance/** 放行路径", requestMapping.value()[0].startsWith("/manage/"));

		Method revokeApply = controllerType.getDeclaredMethod("revokeApply", String.class);
		PostMapping postMapping = revokeApply.getAnnotation(PostMapping.class);
		PreAuthorize preAuthorize = revokeApply.getAnnotation(PreAuthorize.class);
		assertNotNull(postMapping);
		assertNotNull(preAuthorize);
		assertTrue(preAuthorize.value().contains("platform_visitor_incoming_revoke"));
	}
}
