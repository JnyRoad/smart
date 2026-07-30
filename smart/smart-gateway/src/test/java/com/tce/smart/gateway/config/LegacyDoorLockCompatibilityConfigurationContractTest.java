package com.tce.smart.gateway.config;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Gateway Nacos 模板必须默认关闭遗留兼容能力，且不能把签名密钥写入仓库。 */
public class LegacyDoorLockCompatibilityConfigurationContractTest {

	@Test
	public void gatewayTemplateUsesEnvironmentBackedKeyAndAnEmptyCallerList() throws IOException {
		String config = new String(Files.readAllBytes(locateConfig()), StandardCharsets.UTF_8);

		assertTrue(config.contains("legacy-door-lock:"));
		assertTrue(config.contains("enabled: false"));
		assertTrue(config.contains("key-id: \"${SMART_LEGACY_DOORLOCK_KEY_ID:}\""));
		assertTrue(config.contains("signature-key: \"${SMART_LEGACY_DOORLOCK_SIGNATURE_KEY:}\""));
		assertTrue(config.contains("signature-ttl-seconds: 30"));
		assertFalse(config.contains("trusted-proxy-cidrs"));
		assertTrue(config.contains("clients: []"));
	}

	private Path locateConfig() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			Path candidate = current.resolve("docker/nacos/config/dev/smart-gateway.yml");
			if (Files.isRegularFile(candidate)) {
				return candidate;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位 Gateway Nacos 配置模板");
	}
}
