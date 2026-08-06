package com.tce.smart.algorithm.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;

/**
 * 算法配置和演示入口的访问控制契约。
 *
 * 算法配置详情可包含第三方算法服务的连接参数或密钥；演示入口可处理证件照和人脸照，
 * 两类端点都不能仅凭普通员工登录态访问。
 */
public class AlgorithmConfigurationAccessContractTest {

	private static final String CONFIG_PERMISSION = "@pms.hasPermission('algorithm_config_manage')";

	@Test
	public void algorithmConfigurationRoutesRequireDedicatedPermission() throws Exception {
		assertConfigPermission("algorithms", String.class);
		assertConfigPermission("getAlgorithmConfigPage", com.baomidou.mybatisplus.extension.plugins.pagination.Page.class, String.class);
		assertConfigPermission("getById", String.class);
		assertConfigPermission("updateById", com.tce.smart.algorithm.api.dto.req.UpdateAlgorithmConfigDTO.class);
	}

	@Test
	public void algorithmDemoRoutesAreInternalServerOnly() throws Exception {
		assertInternalServerRoute("index");
		assertInternalServerRoute("algorithms", String.class);
		assertInternalServerRoute("faceDetectType");
		assertInternalServerRoute("faceDetect", String.class, String.class, Integer.class, String.class);
		assertInternalServerRoute("ocr", String.class, String.class, String.class, String.class);
		assertInternalServerRoute("compare", String.class, String.class, com.tce.smart.algorithm.api.dto.req.CompareDTO.class);
	}

	@Test
	public void legacyDemoAlgorithmListAlsoRequiresConfigurationPermission() throws Exception {
		Method method = TestController.class.getDeclaredMethod("algorithms", String.class);
		PreAuthorize permission = method.getAnnotation(PreAuthorize.class);
		Assert.assertNotNull("遗留演示算法列表不得绕过配置管理权限", permission);
		Assert.assertEquals(CONFIG_PERMISSION, permission.value());
	}

	/**
	 * 算法服务不得通过 Nacos 白名单把配置、演示或图片接口重新暴露给匿名流量。
	 * 内部来源校验先以 AUDIT 灰度，生产观察完成后由环境变量热切至 ENFORCE。
	 */
	@Test
	public void nacosConfigKeepsPublicRoutesClosedAndInternalValidationGradual() throws Exception {
		Path config = locateRepositoryRoot().resolve("docker/nacos/config/dev/smart-algorithm.yml");
		String content = new String(Files.readAllBytes(config), StandardCharsets.UTF_8);
		Assert.assertTrue("算法服务不得配置 OAuth 忽略路由", content.contains("ignore-urls: []"));
		Assert.assertTrue("内部来源校验必须保留可灰度切换的精确开关",
				content.contains("mode: \"${SMART_ALGORITHM_INNER_MODE:AUDIT}\""));
	}

	private void assertConfigPermission(String methodName, Class<?>... parameterTypes) throws Exception {
		Method method = AlgorithmConfigController.class.getDeclaredMethod(methodName, parameterTypes);
		PreAuthorize permission = method.getAnnotation(PreAuthorize.class);
		Assert.assertNotNull(methodName + " 必须声明算法配置专用权限", permission);
		Assert.assertEquals(CONFIG_PERMISSION, permission.value());
	}

	private void assertInternalServerRoute(String methodName, Class<?>... parameterTypes) throws Exception {
		Method method = TestController.class.getDeclaredMethod(methodName, parameterTypes);
		Assert.assertNotNull(methodName + " 必须声明 @Inner", method.getAnnotation(Inner.class));
		OpenApi openApi = method.getAnnotation(OpenApi.class);
		Assert.assertNotNull(methodName + " 必须声明 @OpenApi", openApi);
		Assert.assertEquals(methodName + " 只能接收 server 服务令牌", "server", openApi.value());
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get("").toAbsolutePath();
		while (current != null) {
			if (Files.isRegularFile(current.resolve("docker/nacos/config/dev/smart-algorithm.yml"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("未找到仓库根目录");
	}
}
