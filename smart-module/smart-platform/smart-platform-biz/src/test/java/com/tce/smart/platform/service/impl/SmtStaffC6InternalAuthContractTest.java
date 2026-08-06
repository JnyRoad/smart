package com.tce.smart.platform.service.impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 许昌 C6 人员同步必须显式以服务身份调用。
 *
 * 仅 Feign 契约声明认证头不足以保证运行时带上标记；此测试固定三条员工同步路径和一条照片读取路径，
 * 防止后续重构把服务令牌降级为用户令牌透传或无认证调用。
 */
public class SmtStaffC6InternalAuthContractTest {

	private static final String STAFF_SOURCE = "src/main/java/com/tce/smart/platform/service/impl/SmtStaffServiceImpl.java";
	private static final String PHOTO_TOOL_SOURCE = "src/test/java/com/tce/smart/platform/biz/ModiftyDormitorData.java";
	private static final String SERVICE_AUTH = "SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED";

	@Test
	public void c6EmployeeSynchronizationUsesServiceTokenForEveryMutation() throws IOException {
		String source = normalizedSource(STAFF_SOURCE);
		assertCallUsesServiceToken(source, "remoteXCRsEmpService.leaveEmp(");
		assertCallUsesServiceToken(source, "remoteXCRsEmpService.intoEmp(");
		assertCallUsesServiceToken(source, "remoteXCRsEmpService.saveEmp(");
	}

	@Test
	public void c6EmployeePhotoToolUsesServiceToken() throws IOException {
		assertCallUsesServiceToken(normalizedSource(PHOTO_TOOL_SOURCE), "remoteXCRsEmpService.getEmpPhoto(");
	}

	private void assertCallUsesServiceToken(String source, String callPrefix) {
		int callStart = source.indexOf(callPrefix);
		Assert.assertTrue("缺少 C6 内部 Feign 调用 " + callPrefix, callStart >= 0);
		int callEnd = source.indexOf(");", callStart);
		Assert.assertTrue("调用未正常结束 " + callPrefix, callEnd > callStart);
		String invocation = source.substring(callStart, callEnd);
		Assert.assertTrue(callPrefix + " 必须传入 FROM_IN 与服务令牌标记", invocation.contains(SERVICE_AUTH));
	}

	private String normalizedSource(String sourcePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(sourcePath)), StandardCharsets.UTF_8)
				.replaceAll("\\s+", " ");
	}
}
