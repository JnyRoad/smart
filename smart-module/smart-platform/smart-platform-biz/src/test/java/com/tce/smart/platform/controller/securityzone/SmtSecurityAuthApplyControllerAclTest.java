package com.tce.smart.platform.controller.securityzone;

import com.tce.smart.common.security.annotation.Inner;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * 端点访问控制注解断言（PR #118/#119 评审安全后续项）：
 * /msg 仅供 smart-schedule 内部调用须 @Inner；
 * /down/{id} 是管理端手动下发须 @pms 权限码。
 */
public class SmtSecurityAuthApplyControllerAclTest {

	/** /msg 必须标 @Inner：唯一调用方是定时任务 Feign（FROM_IN），不面向前端 */
	@Test
	public void sendMessage_hasInnerAnnotation() throws Exception {
		Method method = SmtSecurityAuthApplyController.class.getMethod("sendMessage");
		assertNotNull("/msg 缺少 @Inner 注解", method.getAnnotation(Inner.class));
	}

	/** /down/{id} 必须标 @PreAuthorize 且权限码为 platform_security_auth_down */
	@Test
	public void downDevice_hasPreAuthorizeWithPermissionCode() throws Exception {
		Method method = SmtSecurityAuthApplyController.class.getMethod("downDevice", String.class);
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
		assertNotNull("/down/{id} 缺少 @PreAuthorize 注解", preAuthorize);
		assertTrue("权限码必须是 platform_security_auth_down",
				preAuthorize.value().contains("platform_security_auth_down"));
	}
}
