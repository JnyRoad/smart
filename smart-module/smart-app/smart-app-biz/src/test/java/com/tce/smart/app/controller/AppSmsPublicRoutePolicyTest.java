package com.tce.smart.app.controller;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * 短信白名单契约：只能开放精确的匿名业务场景，内部验证码校验不得回退为 permit-all。
 */
public class AppSmsPublicRoutePolicyTest {

	private static final Pattern LIST_ITEM = Pattern.compile("^\\s*-\\s+([^#\\s]+).*$" );
	private static final List<String> EXPECTED_SMS_ROUTES = Arrays.asList(
			"/sms/visitor/send", "/sms/visitor/verify", "/sms/login/send");

	@Test
	public void nacosAllowsOnlyExactPublicSmsScenes() throws IOException {
		List<String> routes = smsIgnoreUrls(locateRepositoryRoot().resolve("docker/nacos/config/dev/smart-app.yml"));
		assertEquals(EXPECTED_SMS_ROUTES, routes);
		assertFalse(routes.contains("/sms/**"));
		assertFalse(routes.contains("/sms/send/**"));
		assertFalse(routes.contains("/sms/verify/**"));
		assertFalse(routes.contains("/sms/internal/verify"));
	}

	@Test
	public void testProfileCannotReintroduceWildcardSmsAccess() throws IOException {
		Path testConfig = locateRepositoryRoot().resolve(
				"smart-module/smart-app/smart-app-biz/src/main/resources/test.yml");
		assertEquals(EXPECTED_SMS_ROUTES, smsIgnoreUrls(testConfig));
	}

	/**
	 * App 的匿名入口必须逐项登记。媒体、设备、园区详情和微信业务不能再通过
	 * 通配符获得匿名权限，否则新增 Controller 会在未经过安全评审的情况下自动暴露。
	 */
	@Test
	public void nacosDoesNotAllowBusinessWildcardRoutes() throws IOException {
		List<String> routes = allIgnoreUrls(locateRepositoryRoot().resolve("docker/nacos/config/dev/smart-app.yml"));
		assertFalse("匿名白名单不得保留业务通配符", routes.stream()
				.anyMatch(route -> route.contains("*")));
		assertFalse("无 Controller 映射的历史人脸登录路径不得继续占用匿名白名单", routes.contains("/login/face"));
	}

	private List<String> smsIgnoreUrls(Path configPath) throws IOException {
		List<String> routes = allIgnoreUrls(configPath);
		List<String> smsRoutes = new ArrayList<>();
		for (String route : routes) {
			if (route.startsWith("/sms/")) {
				smsRoutes.add(route);
			}
		}
		return smsRoutes;
	}

	private List<String> allIgnoreUrls(Path configPath) throws IOException {
		List<String> routes = new ArrayList<>();
		boolean inIgnoreUrls = false;
		for (String line : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
			if (line.startsWith("spring:")) {
				break;
			}
			if (line.trim().equals("ignore-urls:")) {
				inIgnoreUrls = true;
				continue;
			}
			if (!inIgnoreUrls) {
				continue;
			}
			Matcher matcher = LIST_ITEM.matcher(line);
			if (matcher.matches()) {
				routes.add(matcher.group(1).replace("\"", ""));
			}
		}
		return routes;
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			if (Files.isRegularFile(current.resolve("docker/nacos/config/dev/smart-app.yml"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位仓库根目录");
	}
}
