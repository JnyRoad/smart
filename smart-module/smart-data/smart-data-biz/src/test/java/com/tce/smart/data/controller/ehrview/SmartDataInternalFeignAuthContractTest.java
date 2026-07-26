package com.tce.smart.data.controller.ehrview;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * 所有实际存在的 SmartData 内部 Feign 契约都必须携带来源标记和独立服务凭据。
 */
public class SmartDataInternalFeignAuthContractTest {

	private static final Pattern CLASS_MAPPING = Pattern.compile("@RequestMapping\\(\\\"([^\\\"]+)\\\"\\)");
	private static final Pattern INNER_MAPPING = Pattern.compile(
			"(?m)^\\s*@Inner\\s+@OpenApi\\(\\\"server\\\"\\)\\s+@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\\(\\\"([^\\\"]+)\\\"\\)");
	private static final Pattern FEIGN_MAPPING = Pattern.compile(
			"@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\\(\\\"([^\\\"]+)\\\"\\)([\\s\\S]*?);");
	private static final Pattern EHR_MAPPING = Pattern.compile(
			"(?m)^\\s*@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\\(\\\"([^\\\"]+)\\\"\\)");
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
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtVisitorServiceImpl.java",
					"remoteEvwEmphrYsService.getVisitorBlacklistStatus("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/OAWorkflowServiceImpl.java",
					"remoteEvwEmphrYsService.leave("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthDeleteServiceImpl.java",
					"remoteEvwLdxRegLeaveAllService.listByDay("),
			new CallerExpectation(
					"smart/smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/service/impl/SysUserServiceImpl.java",
					"remoteEvwEmphrYsService.info("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtReplaceApplicationServiceImpl.java",
					"remoteEvwBizLcardlostService.info("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtAskLeaveApplicationServiceImpl.java",
					"remoteEvwBizLregleaveRegisterService.info("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtOvertimeApplicationServiceImpl.java",
					"remoteEvwBizAregotRegisterService.info("),
			new CallerExpectation(
					"smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtOvertimeApplicationServiceImpl.java",
					"remoteEvwLergotAllService.info("));

	@Test
	public void everyRealInternalFeignContractDeclaresSourceAndServiceAuthHeaders() throws IOException {
		Path repositoryRoot = locateRepositoryRoot();
		Set<String> internalRoutes = collectInternalRoutes(repositoryRoot.resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller"));
		internalRoutes.addAll(collectActiveEhrRoutes(repositoryRoot.resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller/ehrview")));
		int matchedFeignRouteCount = verifyFeignContracts(repositoryRoot.resolve(
				"smart-module/smart-data/smart-data-api/src/main/java/com/tce/smart/data/api/feign"), internalRoutes);

		assertTrue("测试必须从真实 Controller 源码枚举到内部服务路由", !internalRoutes.isEmpty());
		assertTrue("测试必须从真实 Feign 源码枚举到内部服务契约", matchedFeignRouteCount > 0);
	}

	private Set<String> collectActiveEhrRoutes(Path controllerDirectory) throws IOException {
		Set<String> routes = new HashSet<>();
		try (Stream<Path> paths = Files.walk(controllerDirectory)) {
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = readSource(controller);
				Matcher classMapping = CLASS_MAPPING.matcher(source);
				if (!classMapping.find()) {
					continue;
				}
				Matcher mappings = EHR_MAPPING.matcher(source);
				while (mappings.find()) {
					routes.add(joinPath(classMapping.group(1), mappings.group(2)));
				}
			}
		}
		return routes;
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

	private Set<String> collectInternalRoutes(Path controllerDirectory) throws IOException {
		Set<String> routes = new HashSet<>();
		try (Stream<Path> paths = Files.walk(controllerDirectory)) {
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = readSource(controller);
				Matcher classMapping = CLASS_MAPPING.matcher(source);
				if (!classMapping.find()) {
					continue;
				}
				Matcher innerMappings = INNER_MAPPING.matcher(source);
				while (innerMappings.find()) {
					routes.add(joinPath(classMapping.group(1), innerMappings.group(2)));
				}
			}
		}
		return routes;
	}

	private int verifyFeignContracts(Path feignDirectory, Set<String> internalRoutes) throws IOException {
		int matchedRouteCount = 0;
		try (Stream<Path> paths = Files.walk(feignDirectory)) {
			for (Path feign : paths.filter(path -> path.toString().endsWith(".java"))
					.collect(Collectors.toList())) {
				Matcher mappings = FEIGN_MAPPING.matcher(readSource(feign));
				while (mappings.find()) {
					String route = mappings.group(2);
					if (!internalRoutes.contains(route)) {
						continue;
					}
					matchedRouteCount++;
					String declaration = mappings.group(3);
					assertTrue(feign.getFileName() + " 的 " + route + " 必须显式声明来源头",
							declaration.contains("@RequestHeader(SecurityConstants.FROM)"));
					assertTrue(feign.getFileName() + " 的 " + route + " 必须显式声明服务凭据头，调用方固定传入 "
									+ "SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED",
							declaration.contains("@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH)"));
				}
			}
		}
		return matchedRouteCount;
	}

	private String findInvocation(String source, String invocationPrefix, String sourceFile) {
		String executableSource = source.replaceAll("(?m)^\\s*//.*$", "").replaceAll("/\\*[\\s\\S]*?\\*/", "");
		Pattern invocationPattern = Pattern.compile(Pattern.quote(invocationPrefix) + "([\\s\\S]*?)\\);");
		Matcher invocation = invocationPattern.matcher(executableSource);
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

	private static final class CallerExpectation {
		private final String sourceFile;
		private final String invocation;

		private CallerExpectation(String sourceFile, String invocation) {
			this.sourceFile = sourceFile;
			this.invocation = invocation;
		}
	}
}
