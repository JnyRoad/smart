package com.tce.smart.transfer.service;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 历史数据搬运的人脸图片只能交给受服务令牌保护的算法 Feign 契约。
 */
public class FaceCropInternalCallContractTest {
	private static final String EXTERNAL_FACE_CUT_PATH = String.join("/", "algorithm", "out", "face", "cut");

	@Test
	public void transferServicesDoNotSendFaceImagesToExternalAnonymousAlgorithmUrl() throws Exception {
		assertUsesInternalFaceCropClient("src/main/java/com/tce/smart/transfer/service/StaffImageTransferService.java");
		assertUsesInternalFaceCropClient("src/main/java/com/tce/smart/transfer/service/DBTableCompareService.java");
		assertServiceTokenMarkerIsDeclared();
	}

	@Test
	public void transferLoadsDedicatedServiceTokenConfigurationWithoutEmbeddingSecrets() throws Exception {
		String bootstrap = new String(Files.readAllBytes(Paths.get("src/main/resources/bootstrap.yml")), StandardCharsets.UTF_8);
		String dedicatedConfig = new String(Files.readAllBytes(Paths.get("../../docker/nacos/config/dev/smart-transfer.yml")),
				StandardCharsets.UTF_8);
		assertTrue("迁移程序必须加载专属配置", bootstrap.contains("data-id: smart-transfer.yml"));
		assertTrue("专属配置必须由受管环境变量注入客户端标识", dedicatedConfig.contains("${SMART_TRANSFER_OAUTH_CLIENT_ID:}"));
		assertTrue("专属配置不得嵌入客户端密钥", dedicatedConfig.contains("${SMART_TRANSFER_OAUTH_CLIENT_SECRET:}"));
		assertTrue("专属配置必须明确令牌端点", dedicatedConfig.contains("${SMART_TRANSFER_OAUTH_TOKEN_URI:}"));
	}

	private void assertUsesInternalFaceCropClient(String sourcePath) throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(sourcePath)), StandardCharsets.UTF_8);
		assertFalse(sourcePath + " 不得直连匿名算法域", source.contains(EXTERNAL_FACE_CUT_PATH));
		assertTrue(sourcePath + " 必须经过受服务令牌保护的裁剪客户端", source.contains("faceCropInternalClient.crop("));
	}

	private void assertServiceTokenMarkerIsDeclared() throws Exception {
		String source = new String(Files.readAllBytes(Paths.get(
				"src/main/java/com/tce/smart/transfer/service/FaceCropInternalClient.java")), StandardCharsets.UTF_8);
		assertTrue("内部裁剪客户端必须使用算法 Feign 契约", source.contains("remoteAlgorithmService.cutFace("));
		assertTrue("内部裁剪客户端必须标记服务令牌", source.contains("SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED"));
	}
}
