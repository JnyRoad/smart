package com.tce.smart.admin.handler;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** OAuth 回包可能含有 access token，日志只允许记录固定事件，不得记录原始响应。 */
public class WeChatLoginHandlerSecurityContractTest {

	@Test
	public void authorizationCodeResponseIsNotWrittenToLogs() throws IOException {
		String handler = new String(Files.readAllBytes(Paths.get("src/main/java/com/tce/smart/admin/handler/WeChatLoginHandler.java")),
				StandardCharsets.UTF_8);

		assertTrue("授权码交换仍保留现有协议，未经官方证据不得擅自改变请求方法", handler.contains("HttpUtil.get(url)"));
		assertFalse("OAuth 原始响应可能包含 access token，不得写入日志", handler.contains("微信响应报文:{}"));
	}
}
