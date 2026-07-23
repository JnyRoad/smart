package com.tce.smart.data.controller.ehrview;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * 第一批 EHR 数据接口仅允许受管服务调用，所有 {@code @Inner} 路由均须声明服务调用方契约。
 */
public class SmartDataInternalRouteOpenApiContractTest {

	private static final List<String> CONTROLLERS = Arrays.asList(
			"LvwAdjustbasicController", "LvwAttendYcxxController", "AvaGetskyPayYSHRController",
			"CvwCcdAllowRuleController", "CvwCcdAllowanceController", "CInterFaceBenSupplyController",
			"EvwEmphrYsController", "EvwAcardlostAllController", "EvwAshiftRunNoController",
			"EvwBizAregotRegisterController", "EvwBizCallowanceController", "EvwBizCallowanceFoodController",
			"EvwBizCallowanceFoodCancelController", "EvwBizLcardlostController",
			"EvwBizLdxregLeaveRegisterController", "EvwBizLregleaveController",
			"EvwBizLregleaveRegisterController", "EvwCallowanceAlltController",
			"EvwCallowanceCancelAlltController", "EvwCotherAllowanceAllController",
			"EvwHortationsAllController", "EvwLdxRegLeaveAllController");

	@Test
	public void selectedInnerRoutesDeclareServerOpenApiContract() throws IOException {
		Path sourceDirectory = locateRepositoryRoot().resolve(
				"smart-module/smart-data/smart-data-biz/src/main/java/com/tce/smart/data/controller/ehrview");
		for (String controller : CONTROLLERS) {
			String source = new String(Files.readAllBytes(sourceDirectory.resolve(controller + ".java")), StandardCharsets.UTF_8);
			String normalized = source.replace("\r\n", "\n");
			int innerCount = count(normalized, "@Inner");
			int protectedCount = count(normalized, "@Inner\n\t@OpenApi(\"server\")")
					+ count(normalized, "@Inner\n    @OpenApi(\"server\")")
					+ count(normalized, "@Inner\n\t\t@OpenApi(\"server\")");
			assertTrue(controller + " 的每个内部路由必须显式限定 server OpenApi", innerCount == protectedCount);
		}
	}

	private int count(String source, String target) {
		int count = 0;
		int index = source.indexOf(target);
		while (index >= 0) {
			count++;
			index = source.indexOf(target, index + target.length());
		}
		return count;
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
