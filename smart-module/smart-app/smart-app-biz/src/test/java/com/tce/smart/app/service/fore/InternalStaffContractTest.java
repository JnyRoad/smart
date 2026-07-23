package com.tce.smart.app.service.fore;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffFaceLoginRespDTO;
import com.tce.smart.platform.api.dto.req.InternalStaffFaceLoginReqDTO;
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
import static org.junit.Assert.assertTrue;

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
		assertPurposeHeaders("getBindingStaff");
		assertPurposeHeaders("getModuleStaff");
		assertPurposeHeaders("getPasswordStaff");
		assertPurposeHeaders("getPasswordPhone");
		assertPurposeHeaders("getSelfProfile");
		assertPurposeHeaders("getMyDormitory");
		assertIdentityHeaders();
		assertFaceLoginHeaders();
	}

	@Test
	public void internalStaffDtosExposeOnlyTheirDeclaredPurposeFields() {
		assertFields(InternalStaffBindingRespDTO.class, "staffId", "badge", "name", "status", "certNoLast6");
		assertFields(InternalStaffModuleRespDTO.class, "badge", "compId");
		assertFields(InternalStaffPasswordRespDTO.class, "staffId", "badge", "facePicId");
		assertFields(InternalStaffIdentityRespDTO.class, "staffId", "badge", "name", "certno");
		assertFields(InternalStaffFaceLoginRespDTO.class, "badge");
		assertFields(InternalStaffFaceLoginReqDTO.class, "facePic", "deviceNo");
	}

	@Test
	public void appBusinessServicesDoNotUseLegacyStaffEntityLookupOrLogIt() throws IOException {
		String[] relativePaths = {
				"fore/impl/PasswordServiceImpl.java",
				"fore/impl/EmployeeServiceImpl.java",
				"fore/impl/SettingServiceImpl.java",
				"fore/impl/ForeModuleServiceImpl.java",
				"fore/impl/BadgeLossServiceImpl.java",
				"fore/impl/PerfectInfoServiceImpl.java",
				"fore/impl/IcbcCommonServiceImpl.java",
				"impl/AppWechatBindingServiceImpl.java",
				"wechat/impl/JobServiceImpl.java"
		};

		for (String relativePath : relativePaths) {
			String source = new String(Files.readAllBytes(Paths.get(APP_SOURCE_ROOT, relativePath)), StandardCharsets.UTF_8);
			assertFalse("业务服务不得调用旧员工实体接口：" + relativePath,
					source.contains("getSimpleSttaffByBadge("));
			assertFalse("业务服务不得记录完整员工查询结果：" + relativePath,
					source.contains("staffResult"));
			assertFalse("业务服务不得消费旧 StaffInfoRespDTO：" + relativePath,
					source.contains("StaffInfoRespDTO"));
			if (relativePath.contains("EmployeeServiceImpl") || relativePath.contains("SettingServiceImpl")) {
				assertFalse("员工设置服务不得消费 SmtStaffDTO：" + relativePath,
						source.contains("SmtStaffDTO"));
			}
		}
	}

	@Test
	public void passwordAndIcbcEndpointsDoNotReturnRawPhoneOrIdentityHtml() throws IOException {
		String controllerRoot = "src/main/java/com/tce/smart/app/controller/fore/";
		String passwordController = read(controllerRoot + "PasswordController.java");
		String icbcController = read(controllerRoot + "IcbcCommonController.java");
		String icbcService = read(APP_SOURCE_ROOT + "/fore/impl/IcbcCommonServiceImpl.java");

		assertFalse("找回密码接口不得接收客户端回传的完整手机号", passwordController.contains("@RequestParam(value = \"mobile\""));
		assertFalse("银行实名接口不得回传 HTML 表单", icbcController.contains("ResponseEntity<String>"));
		assertFalse("银行实名接口不得向客户端构造包含身份证的表单", icbcService.contains("buildPostForm("));
		assertFalse("已停用的银行实名模式不得读取身份证投影", icbcService.contains("getIdentityStaff("));
	}

	@Test
	public void passwordResetUsesOpaqueChallengeInsteadOfReusableBadgeAuthorization() throws IOException {
		String controllerRoot = "src/main/java/com/tce/smart/app/controller/fore/";
		String passwordController = read(controllerRoot + "PasswordController.java");
		String passwordService = read(APP_SOURCE_ROOT + "/fore/impl/PasswordServiceImpl.java");

		assertTrue("短信发送必须只接收一次性 challenge，不得再次接收工号", passwordController.contains("challengeId"));
		assertFalse("短信发送不得将任意工号直接传入服务", passwordController.contains("sendSmsCode(badge)"));
		assertFalse("短信校验不得将任意工号直接传入服务", passwordController.contains("verifySmsCode(badge, smsCode)"));
		assertTrue("challenge 必须绑定用途、过期时间和有限重试次数", passwordService.contains("PASSWORD_RESET_PURPOSE")
				&& passwordService.contains("CHALLENGE_TTL_SECONDS") && passwordService.contains("MAX_VERIFY_ATTEMPTS"));
		assertTrue("密码更新授权必须标明已验证 challenge 来源", passwordService.contains("verifiedChallengeId"));
		assertTrue("短信发送必须使用 POST 最小请求体", passwordController.contains("@PostMapping(\"/sms/send\")")
				&& passwordController.contains("PasswordSmsSendReqDTO"));
		assertTrue("短信校验必须使用 POST 最小请求体", passwordController.contains("@PostMapping(\"/verify\")")
				&& passwordController.contains("PasswordSmsVerifyReqDTO"));
		assertFalse("找回密码短信端点不得继续通过 GET 查询串传参", passwordController.contains("@GetMapping(\"/sms/send\")")
				|| passwordController.contains("@GetMapping(\"/verify\")"));
		assertTrue("短信次数预占必须使用 Redis Lua 原子脚本", passwordService.contains("RESERVE_SMS_SEND_ATTEMPT")
				&& passwordService.contains("stringRedisTemplate.execute(RESERVE_SMS_SEND_ATTEMPT"));
		assertTrue("短信发送必须有 challenge 级预约状态，禁止多个赢家同时下发", passwordService.contains("SMS_SEND_STATE_SENDING")
				&& passwordService.contains("SMS_SEND_STATE_SENT") && passwordService.contains("sendReservationId"));
		assertTrue("短信 provider 完成后必须原子提交或释放预约", passwordService.contains("completeSmsSendAttempt")
				&& passwordService.contains("COMPLETE_SMS_SEND_ATTEMPT"));
	}

	@Test
	public void appDoesNotPutPasswordAuthorizationOrEmployeeIdentityInUrlsOrLogs() throws IOException {
		String userController = read("../../../smart/smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/controller/UserController.java");
		String passwordApi = read("../../../smart-app-uniapp/api/api-password.js");
		String employeeService = read(APP_SOURCE_ROOT + "/fore/impl/EmployeeServiceImpl.java");
		String jobService = read(APP_SOURCE_ROOT + "/wechat/impl/JobServiceImpl.java");

		assertTrue("改密端点必须接收最小 JSON 请求体", userController.contains("@RequestBody"));
		assertTrue("改密端点必须使用 PUT", userController.contains("@PutMapping(\"/password/update\")"));
		assertFalse("前端不得把密码或授权码拼到 URL", passwordApi.contains("?username=${obj.username}"));
		assertFalse("前端不得把 challenge 或短信验证码拼到 URL", passwordApi.contains("?challengeId=${challengeId}")
				|| passwordApi.contains("?smsCode=${obj.smsCode}"));
		assertFalse("员工服务日志不得记录工号", employeeService.contains("Badge={}") || employeeService.contains("StaffBadge={}"));
		assertFalse("岗位服务日志不得记录姓名或对象字段", jobService.contains("EmergencyName={}")
				|| jobService.contains("Company={}") || jobService.contains("Email={}"));
		assertFalse("岗位服务日志不得直接输出业务响应对象", jobService.contains("applicationId, deleteFamily)"));
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

	private void assertPurposeHeaders(String methodName) throws Exception {
		Method method = RemoteStaffInternalService.class.getMethod(methodName,
				String.class, String.class, String.class, String.class);
		RequestHeader fromHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
		RequestHeader serviceAuthHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);
		RequestHeader purposeHeader = method.getParameters()[3].getAnnotation(RequestHeader.class);
		assertNotNull("内部来源头必须显式声明", fromHeader);
		assertNotNull("服务令牌标记必须显式声明", serviceAuthHeader);
		assertNotNull("高敏感资料调用必须显式声明用途", purposeHeader);
		assertEquals("X-Smart-Internal-Purpose", purposeHeader.value());
	}

	private void assertIdentityHeaders() throws Exception {
		Method method = RemoteStaffInternalService.class.getMethod("getIdentityStaff",
				String.class, String.class, String.class, String.class);
		RequestHeader purposeHeader = method.getParameters()[3].getAnnotation(RequestHeader.class);
		assertNotNull("身份资料调用必须显式声明用途", purposeHeader);
		assertEquals("X-Smart-Internal-Purpose", purposeHeader.value());
	}

	private void assertFaceLoginHeaders() throws Exception {
		Method method = RemoteStaffInternalService.class.getMethod("faceLogin",
				InternalStaffFaceLoginReqDTO.class, String.class, String.class, String.class);
		RequestHeader fromHeader = method.getParameters()[1].getAnnotation(RequestHeader.class);
		RequestHeader serviceAuthHeader = method.getParameters()[2].getAnnotation(RequestHeader.class);
		RequestHeader purposeHeader = method.getParameters()[3].getAnnotation(RequestHeader.class);
		assertNotNull("人脸登录必须声明内部来源", fromHeader);
		assertNotNull("人脸登录必须声明服务令牌标记", serviceAuthHeader);
		assertNotNull("人脸登录必须声明调用用途", purposeHeader);
		assertEquals(SecurityConstants.FROM, fromHeader.value());
		assertEquals(SecurityConstants.INTERNAL_SERVICE_AUTH, serviceAuthHeader.value());
		assertEquals("X-Smart-Internal-Purpose", purposeHeader.value());
	}

	private String read(String relativePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(relativePath)), StandardCharsets.UTF_8);
	}

	private void assertFields(Class<?> type, String... expectedFields) {
		Set<String> actual = Arrays.stream(type.getDeclaredFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.map(Field::getName)
				.collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList(expectedFields)), actual);
	}
}
