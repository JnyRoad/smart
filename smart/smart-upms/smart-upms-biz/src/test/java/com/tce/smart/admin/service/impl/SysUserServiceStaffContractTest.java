package com.tce.smart.admin.service.impl;

import com.tce.smart.admin.api.feign.RemoteStaffService;
import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * UPMS 员工账号校验只能消费最小内部绑定契约。
 */
public class SysUserServiceStaffContractTest {

	@Test
	public void staffProvisioningFeignRequiresInternalServiceToken() throws Exception {
		Method method = RemoteStaffService.class.getMethod("getProvisioningStaff", String.class, String.class, String.class);
		RequestHeader fromHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
		RequestHeader serviceAuthHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);
		assertNotNull(fromHeader);
		assertNotNull(serviceAuthHeader);
		assertEquals(SecurityConstants.FROM, fromHeader.value());
		assertEquals(SecurityConstants.INTERNAL_SERVICE_AUTH, serviceAuthHeader.value());
	}

	@Test
	public void sysUserServiceDoesNotUseOrLogTheLegacyStaffEntityResponse() throws IOException {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/tce/smart/admin/service/impl/SysUserServiceImpl.java")), StandardCharsets.UTF_8);
		assertFalse(source.contains("getSimpleSttaffByBadge("));
		assertFalse(source.contains("remoteStaffService.getSimpleSttaffByBadge.rs="));
	}
}
