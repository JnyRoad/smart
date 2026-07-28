package com.tce.smart.data.controller.ehrview;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 第一批 EHR 数据接口仅允许受管服务调用，所有 {@code @Inner} 路由均须声明服务调用方契约。
 */
public class SmartDataInternalRouteOpenApiContractTest {
	private static final Pattern EHR_MAPPING = Pattern.compile(
			"(?m)^\\s*@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\\(");
	private static final Pattern EHR_INNER_SERVER_MAPPING = Pattern.compile(
			"(?m)^\\s*@Inner\\s+@OpenApi\\(\\\"server\\\"\\)\\s+@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\\(");

	@Test
	public void everyRealDataInnerRouteDeclaresServerOpenApiContract() throws IOException {
		Path sourceDirectory = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller");
		int innerRouteCount = 0;
		try (Stream<Path> paths = Files.walk(sourceDirectory)) {
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = new String(Files.readAllBytes(controller), StandardCharsets.UTF_8).replace("\r\n", "\n");
				int innerCount = count(source, "(?m)^\\s*@Inner\\s*$");
				int protectedCount = count(source,
						"(?m)^\\s*@Inner\\s*\\n\\s*@OpenApi\\(\\\"server\\\"\\)");
				assertEquals(controller.getFileName() + " 的每个活动内部路由必须显式限定 server OpenApi",
						innerCount, protectedCount);
				innerRouteCount += innerCount;
			}
		}
		assertTrue("测试必须从真实 Controller 源码枚举到内部路由", innerRouteCount > 0);
	}

	@Test
	public void everyActiveEhrRouteDeclaresTheInternalServerContract() throws IOException {
		Path sourceDirectory = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller/ehrview");
		int mappingCount = 0;
		int protectedMappingCount = 0;
		try (Stream<Path> paths = Files.walk(sourceDirectory)) {
			for (Path controller : paths.filter(path -> path.toString().endsWith("Controller.java"))
					.collect(Collectors.toList())) {
				String source = new String(Files.readAllBytes(controller), StandardCharsets.UTF_8).replace("\r\n", "\n");
				mappingCount += countMatches(source, EHR_MAPPING);
				protectedMappingCount += countMatches(source, EHR_INNER_SERVER_MAPPING);
			}
		}
		assertTrue("测试必须从真实 EHR Controller 源码枚举到活动路由", mappingCount > 0);
		assertEquals("每个活动 EHR 路由都必须由 @Inner + @OpenApi(server) 收口",
				mappingCount, protectedMappingCount);
	}

	private int count(String source, String target) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(target).matcher(source);
		int occurrences = 0;
		while (matcher.find()) {
			occurrences++;
		}
		return occurrences;
	}

	private int countMatches(String source, Pattern pattern) {
		Matcher matcher = pattern.matcher(source);
		int occurrences = 0;
		while (matcher.find()) {
			occurrences++;
		}
		return occurrences;
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
}
