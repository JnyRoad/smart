package com.tce.smart.app.service;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Smart App 调用 DHR 内部查询的服务令牌契约。
 *
 * 两个调用点都是用户业务链路的一部分，不能因遗漏服务令牌标记而退回用户令牌或失去访问能力。
 */
public class SmartAppInternalDhrFeignContractTest {

	@Test
	public void employeeProfileLookupUsesServiceToken() throws IOException {
		assertSourceContains(
				"src/main/java/com/tce/smart/app/service/fore/impl/EmployeeServiceImpl.java",
				"remoteYutoDhrYsService.getProperties(employeeVo.getEmployeeBadge(),SecurityConstants.FROM_IN,SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)");
	}

	@Test
	public void yhtBadgeLookupUsesServiceToken() throws IOException {
		assertSourceContains(
				"src/main/java/com/tce/smart/app/service/yht/impl/YhtAuthServiceImpl.java",
				"remoteYutoDhrYsService.getBadgeByUserId(userId,SecurityConstants.FROM_IN,SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)");
	}

	private void assertSourceContains(String sourcePath, String expectedCall) throws IOException {
		String source = new String(Files.readAllBytes(Paths.get(sourcePath)), StandardCharsets.UTF_8)
				.replaceAll("\\s+", "");
		Assert.assertTrue(sourcePath + " 必须以服务令牌调用 DHR 内部查询", source.contains(expectedCall));
	}
}
