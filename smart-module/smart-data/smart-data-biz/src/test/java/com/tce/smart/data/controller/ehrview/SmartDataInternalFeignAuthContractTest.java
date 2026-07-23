package com.tce.smart.data.controller.ehrview;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * EHR 内部路由对应的 Feign 契约必须同时声明来源头和服务凭据头。
 */
public class SmartDataInternalFeignAuthContractTest {

	private static final int EXPECTED_INTERNAL_ROUTE_COUNT = 46;
	private static final Pattern CLASS_MAPPING = Pattern.compile("@RequestMapping\\(\\\"([^\\\"]+)\\\"\\)");
	private static final Pattern INNER_MAPPING = Pattern.compile(
			"@Inner\\s+@OpenApi\\(\\\"server\\\"\\)\\s+@(GetMapping|PostMapping)\\(\\\"([^\\\"]+)\\\"\\)");
	private static final List<ControllerFeignPair> CONTROLLER_FEIGN_PAIRS = Arrays.asList(
			new ControllerFeignPair("LvwAdjustbasicController", "RemoteLvwAdjustbasicService"),
			new ControllerFeignPair("LvwAttendYcxxController", "RemoteLvwAttendYcxxService"),
			new ControllerFeignPair("AvaGetskyPayYSHRController", "RemoteAvaGetskyPayService"),
			new ControllerFeignPair("CvwCcdAllowRuleController", "RemoteCvwCcdAllowRuleService"),
			new ControllerFeignPair("CvwCcdAllowanceController", "RemoteCvwCcdAllowanceService"),
			new ControllerFeignPair("CInterFaceBenSupplyController", "RemoteCInterFaceBenSupplyService"),
			new ControllerFeignPair("EvwEmphrYsController", "RemoteEvwEmphrYsService"),
			new ControllerFeignPair("EvwAcardlostAllController", "RemoteEvwAcardlostAllService"),
			new ControllerFeignPair("EvwAshiftRunNoController", "RemoteEvwAshiftRunNoService"),
			new ControllerFeignPair("EvwBizAregotRegisterController", "RemoteEvwBizAregotRegisterService"),
			new ControllerFeignPair("EvwBizCallowanceController", "RemoteEvwBizCallowanceService"),
			new ControllerFeignPair("EvwBizCallowanceFoodController", "RemoteEvwBizCallowanceFoodService"),
			new ControllerFeignPair("EvwBizCallowanceFoodCancelController", "RemoteEvwBizCallowanceFoodCancelService"),
			new ControllerFeignPair("EvwBizLcardlostController", "RemoteEvwBizLcardlostService"),
			new ControllerFeignPair("EvwBizLdxregLeaveRegisterController", "RemoteEvwBizLdxregLeaveRegisterService"),
			new ControllerFeignPair("EvwBizLregleaveController", "RemoteEvwBizLregleaveService"),
			new ControllerFeignPair("EvwBizLregleaveRegisterController", "RemoteEvwBizLregleaveRegisterService"),
			new ControllerFeignPair("EvwCallowanceAlltController", "RemoteEvwCallowanceAlltService"),
			new ControllerFeignPair("EvwCallowanceCancelAlltController", "RemoteEvwCallowanceCancelAlltService"),
			new ControllerFeignPair("EvwCotherAllowanceAllController", "RemoteEvwCotherAllowanceAllService"),
			new ControllerFeignPair("EvwHortationsAllController", "RemoteEvwHortationsAllService"),
			new ControllerFeignPair("EvwLdxRegLeaveAllController", "RemoteEvwLdxRegLeaveAllService"));
	private static final List<CallerExpectation> INTERNAL_CALLERS = Arrays.asList(
			new CallerExpectation(
					"smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/RestApplicationServiceImpl.java",
					"remoteLvwAdjustbasicService.getByBadge("),
			new CallerExpectation(
					"smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/AttendanceServiceImpl.java",
					"remoteAvaGetskyPayService.info("),
			new CallerExpectation(
					"smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/EmployeeServiceImpl.java",
					"remoteEvwEmphrYsService.info("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtBlackVisitorServiceImpl.java",
					"remoteEvwEmphrYsService.getBlack("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/OAWorkflowServiceImpl.java",
					"remoteEvwEmphrYsService.leave("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthDeleteServiceImpl.java",
					"remoteEvwLdxRegLeaveAllService.listByDay("),
			new CallerExpectation(
					"smart/smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/service/impl/SysUserServiceImpl.java",
					"remoteEvwEmphrYsService.info("));

	@Test
	public void everyEhrInternalRouteFeignMethodDeclaresSourceAndServiceAuthHeaders() throws IOException {
		Path repositoryRoot = locateRepositoryRoot();
		Path controllerDirectory = repositoryRoot.resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller/ehrview");
		Path feignDirectory = repositoryRoot.resolve(
				"smart-module/smart-data/smart-data-api/src/main/java/com/tce/smart/data/api/feign/ehrview");
		int internalRouteCount = 0;

		for (ControllerFeignPair pair : CONTROLLER_FEIGN_PAIRS) {
			String controllerSource = readSource(controllerDirectory.resolve(pair.controller + ".java"));
			String feignSource = readSource(feignDirectory.resolve(pair.feign + ".java"));
			String controllerPrefix = findClassMapping(controllerSource, pair.controller);
			Matcher innerMappings = INNER_MAPPING.matcher(controllerSource);

			while (innerMappings.find()) {
				internalRouteCount++;
				String route = joinPath(controllerPrefix, innerMappings.group(2));
				String declaration = findFeignDeclaration(feignSource, route, pair.feign);
				assertTrue(pair.feign + " 的 " + route + " 必须显式声明来源头",
						declaration.contains("@RequestHeader(SecurityConstants.FROM)"));
				assertTrue(pair.feign + " 的 " + route + " 必须显式声明服务凭据头，调用方固定传入 "
								+ "SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED",
						declaration.contains("@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH)"));
			}
		}

		assertEquals("A 组应覆盖且只覆盖 46 个 EHR 内部路由", EXPECTED_INTERNAL_ROUTE_COUNT, internalRouteCount);
	}

	@Test
	public void selectedInternalFeignCallersPassSourceAndServiceCredentials() throws IOException {
		Path repositoryRoot = locateRepositoryRoot();
		for (CallerExpectation expectation : INTERNAL_CALLERS) {
			String source = readSource(repositoryRoot.resolve(expectation.sourceFile));
			String invocation = findInvocation(source, expectation.invocation, expectation.sourceFile);
			assertTrue(expectation.invocation + " 必须显式传入内部来源标记",
					invocation.contains("SecurityConstants.FROM_IN"));
			assertTrue(expectation.invocation + " 必须显式传入服务凭据标记",
					invocation.contains("SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED"));
		}
	}

	private String findClassMapping(String source, String controller) {
		Matcher mapping = CLASS_MAPPING.matcher(source);
		assertTrue(controller + " 必须声明类级路由", mapping.find());
		return mapping.group(1);
	}

	private String findFeignDeclaration(String source, String route, String feign) {
		Pattern endpoint = Pattern.compile("@(GetMapping|PostMapping)\\(\\\"" + Pattern.quote(route)
				+ "\\\"\\)([\\s\\S]*?);");
		Matcher declaration = endpoint.matcher(source);
		assertTrue(feign + " 必须声明与内部路由一致的 " + route + " 契约", declaration.find());
		return declaration.group(2);
	}

	private String findInvocation(String source, String invocationPrefix, String sourceFile) {
		Pattern invocationPattern = Pattern.compile(Pattern.quote(invocationPrefix) + "([\\s\\S]*?)\\);");
		Matcher invocation = invocationPattern.matcher(source);
		assertTrue(sourceFile + " 必须调用 " + invocationPrefix, invocation.find());
		return invocation.group();
	}

	private String joinPath(String prefix, String path) {
		return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) + path : prefix + path;
	}

	private String readSource(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).replace("\r\n", "\n");
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			if (Files.isRegularFile(current.resolve("docker/nacos/config/dev/smart-data.yml"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位仓库根目录");
	}

	private static final class ControllerFeignPair {
		private final String controller;
		private final String feign;

		private ControllerFeignPair(String controller, String feign) {
			this.controller = controller;
			this.feign = feign;
		}
	}

	private static final class CallerExpectation {
		private final String sourceFile;
		private final String invocation;

		private CallerExpectation(String sourceFile, String invocation) {
			this.sourceFile = sourceFile;
			this.invocation = invocation;
		}
	}
}
