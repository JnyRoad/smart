package com.tce.smart.schedule.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * ISC 日志载荷格式化测试。
 */
public class IscLogPayloadFormatterTest {

	@Test
	public void masksPhoneAndCertificateButKeepsTaskFields() {
		String payload = "{\"taskId\":\"task-1\",\"phone\":\"13800138000\",\"certificateNo\":\"410123199001011234\"}";

		String formatted = IscLogPayloadFormatter.format(payload);

		Assert.assertTrue(formatted.contains("task-1"));
		Assert.assertTrue(formatted.contains("138***8000"));
		Assert.assertTrue(formatted.contains("4101***1234"));
		Assert.assertFalse(formatted.contains("13800138000"));
		Assert.assertFalse(formatted.contains("410123199001011234"));
	}

	@Test
	public void truncatesLongFacePayload() {
		StringBuilder base64 = new StringBuilder();
		for (int index = 0; index < 200; index++) {
			base64.append('a');
		}
		String payload = "{\"faceBase64\":\"" + base64 + "\"}";

		String formatted = IscLogPayloadFormatter.format(payload);

		Assert.assertTrue(formatted.contains("truncated=true"));
		Assert.assertTrue(formatted.contains("original_length=200"));
		Assert.assertFalse(formatted.contains(base64.toString()));
	}

	@Test
	public void masksSensitiveFieldsInsideEscapedResponseData() {
		String payload = "{\"data\":\"{\\\"phone\\\":\\\"13800138000\\\",\\\"certNo\\\":\\\"410123199001011234\\\"}\"}";

		String formatted = IscLogPayloadFormatter.format(payload);

		Assert.assertFalse(formatted.contains("13800138000"));
		Assert.assertFalse(formatted.contains("410123199001011234"));
		Assert.assertTrue(formatted.contains("138***8000"));
		Assert.assertTrue(formatted.contains("4101***1234"));
	}
}
