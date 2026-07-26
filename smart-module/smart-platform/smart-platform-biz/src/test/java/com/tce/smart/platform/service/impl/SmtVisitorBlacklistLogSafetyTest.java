package com.tce.smart.platform.service.impl;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;

/** 黑名单判定日志不得序列化包含姓名、证件号和原因的实体。 */
public class SmtVisitorBlacklistLogSafetyTest {

	@Test
	public void blacklistCheckDoesNotLogBlacklistEntity() throws Exception {
		Path source = locateRepositoryRoot().resolve("smart-module/smart-platform/smart-platform-biz/src/main/java/"
				+ "com/tce/smart/platform/service/impl/SmtVisitorServiceImpl.java");
		String content = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
		assertFalse("黑名单校验不得把实体写入日志", content.contains("smtBlackVisitor:\" + smtBlackVisitor"));
		assertFalse("黑名单校验不得把实体作为日志参数", content.contains("log.info(\"smtBlackVisitor"));
	}

	private Path locateRepositoryRoot() {
		Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null) {
			if (Files.isRegularFile(current.resolve("docker/nacos/config/dev/smart-platform.yml"))) {
				return current;
			}
			current = current.getParent();
		}
		throw new IllegalStateException("无法定位仓库根目录");
	}
}
