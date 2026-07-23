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

	private List<String> smsIgnoreUrls(Path configPath) throws IOException {
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
			if (matcher.matches() && matcher.group(1).startsWith("/sms/")) {
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
