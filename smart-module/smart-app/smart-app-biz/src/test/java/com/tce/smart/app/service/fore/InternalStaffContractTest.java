package com.tce.smart.app.service.fore;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * App 到 Platform 的员工内部契约测试。
 *
 * 员工实体不得跨服务传输；四种用途只能使用对应的最小 DTO，且每个调用都必须显式
 * 声明内部来源和服务客户端令牌标记。
 */
public class InternalStaffContractTest {

	private static final String APP_SOURCE_ROOT = "src/main/java/com/tce/smart/app/service";

	@Test
	public void internalStaffMethodsRequireFromAndServiceAuthenticationHeaders() throws Exception {
		assertInternalHeaders("getBindingStaff");
		assertInternalHeaders("getModuleStaff");
		assertInternalHeaders("getPasswordStaff");
		assertInternalHeaders("getIdentityStaff");
	}

	@Test
	public void internalStaffDtosExposeOnlyTheirDeclaredPurposeFields() {
		assertFields(InternalStaffBindingRespDTO.class, "staffId", "badge", "name", "status", "certNoLast6");
		assertFields(InternalStaffModuleRespDTO.class, "badge", "compId");
		assertFields(InternalStaffPasswordRespDTO.class, "staffId", "badge", "facePicId");
		assertFields(InternalStaffIdentityRespDTO.class, "staffId", "badge", "name", "certno");
	}

	@Test
	public void appBusinessServicesDoNotUseLegacyStaffEntityLookupOrLogIt() throws IOException {
		String[] relativePaths = {
				"fore/impl/PasswordServiceImpl.java",
				"fore/impl/ForeModuleServiceImpl.java",
				"fore/impl/BadgeLossServiceImpl.java",
				"fore/impl/PerfectInfoServiceImpl.java",
				"fore/impl/IcbcCommonServiceImpl.java",
				"impl/AppWechatBindingServiceImpl.java"
		};

		for (String relativePath : relativePaths) {
			String source = new String(Files.readAllBytes(Paths.get(APP_SOURCE_ROOT, relativePath)), StandardCharsets.UTF_8);
			assertFalse("业务服务不得调用旧员工实体接口：" + relativePath,
					source.contains("getSimpleSttaffByBadge("));
			assertFalse("业务服务不得记录完整员工查询结果：" + relativePath,
					source.contains("staffResult"));
		}
	}

	private void assertInternalHeaders(String methodName) throws Exception {
		Method method = RemoteStaffInternalService.class.getMethod(methodName,
				String.class, String.class, String.class);
		RequestHeader fromHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
		RequestHeader serviceAuthHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);

		assertNotNull("内部来源头必须显式声明", fromHeader);
		assertNotNull("服务令牌标记必须显式声明", serviceAuthHeader);
		assertEquals(SecurityConstants.FROM, fromHeader.value());
		assertEquals(SecurityConstants.INTERNAL_SERVICE_AUTH, serviceAuthHeader.value());
	}

	private void assertFields(Class<?> type, String... expectedFields) {
		Set<String> actual = Arrays.stream(type.getDeclaredFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.map(Field::getName)
				.collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList(expectedFields)), actual);
	}
}
