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
import static org.junit.Assert.assertTrue;

/**
 * UPMS 员工账号校验只能消费最小内部绑定契约。
 */
public class SysUserServiceStaffContractTest {

	@Test
	public void staffProvisioningFeignRequiresInternalServiceToken() throws Exception {
		Method method = RemoteStaffService.class.getMethod("getProvisioningStaff", String.class, String.class, String.class,
				String.class);
		RequestHeader fromHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
		RequestHeader serviceAuthHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);
		RequestHeader purposeHeader = method.getParameters()[3].getAnnotation(RequestHeader.class);
		assertNotNull(fromHeader);
		assertNotNull(serviceAuthHeader);
		assertEquals(SecurityConstants.FROM, fromHeader.value());
		assertEquals(SecurityConstants.INTERNAL_SERVICE_AUTH, serviceAuthHeader.value());
		assertNotNull(purposeHeader);
		assertEquals("X-Smart-Internal-Purpose", purposeHeader.value());
	}

	@Test
	public void sysUserServiceDoesNotUseOrLogTheLegacyStaffEntityResponse() throws IOException {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/tce/smart/admin/service/impl/SysUserServiceImpl.java")), StandardCharsets.UTF_8);
		assertFalse(source.contains("getSimpleSttaffByBadge("));
		assertFalse(source.contains("remoteStaffService.getSimpleSttaffByBadge.rs="));
		assertFalse("手机号登录不得调用公开员工实体查询", source.contains("remoteStaffService.queryMobile("));
		assertFalse("手机号登录不得消费 SmtStaffDTO", source.contains("SmtStaffDTO"));
		assertTrue("改密授权必须使用原子 compare-and-delete", source.contains("COMPARE_AND_DELETE")
				&& source.contains("stringRedisTemplate.execute"));
		assertTrue("改密授权必须拒绝缺少 challenge 来源的记录", source.contains("StringUtils.isBlank(authCodeJson.getStr(\"verifiedChallengeId\"))"));
	}

	@Test
	public void upmsFeignDoesNotExposeLegacyMobileOrFaceEntityHandlers() throws Exception {
		for (Method method : RemoteStaffService.class.getMethods()) {
			assertFalse("UPMS Feign 不得保留公开手机号员工实体查询", "queryMobile".equals(method.getName()));
			assertFalse("UPMS Feign 不得保留公开人脸员工实体查询", "faceSearchForLogin".equals(method.getName()));
		}
	}
}
