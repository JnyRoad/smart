package com.tce.smart.schedule.support;

import cn.hutool.json.JSONUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISC 调用日志载荷格式化器。
 *
 * 保留排障所需请求与响应内容，同时防止手机号、证件号和人脸图片内容直接写入生产日志。
 */
public final class IscLogPayloadFormatter {

	private static final int MAX_PAYLOAD_LENGTH = 16 * 1024;

	private static final int MAX_FACE_VALUE_LENGTH = 128;

	private static final Pattern PHONE_FIELD_PATTERN = Pattern.compile(
			"(\\\"(?:phone|mobile|tel|telephone|手机号|电话)[^\\\"]*\\\"\\s*:\\s*\\\")([^\\\"]*)(\\\")",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern CERTIFICATE_FIELD_PATTERN = Pattern.compile(
			"(\\\"(?:certNo|certificateNo|idCard|identityCard|身份证|证件号)[^\\\"]*\\\"\\s*:\\s*\\\")([^\\\"]*)(\\\")",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern FACE_FIELD_PATTERN = Pattern.compile(
			"(\\\"[^\\\"]*(?:face|photo|image|base64)[^\\\"]*\\\"\\s*:\\s*\\\")([^\\\"]*)(\\\")",
			Pattern.CASE_INSENSITIVE);

	private IscLogPayloadFormatter() {
	}

	/**
	 * 将载荷转换为可安全记录的字符串。
	 *
	 * @param payload ISC 请求或响应对象
	 * @return 脱敏且受长度限制的载荷字符串
	 */
	public static String format(Object payload) {
		if (payload == null) {
			return "null";
		}
		String serializedPayload = payload instanceof CharSequence
				? payload.toString() : JSONUtil.toJsonStr(payload);
		// ISC 常将 JSON 再作为 data 字符串返回，先展开一层引号转义后才能识别其中的敏感字段。
		String normalizedPayload = serializedPayload.replace("\\\"", "\"");
		String maskedPayload = replaceFieldValue(normalizedPayload, PHONE_FIELD_PATTERN,
				IscLogPayloadFormatter::maskPhone);
		maskedPayload = replaceFieldValue(maskedPayload, CERTIFICATE_FIELD_PATTERN,
				IscLogPayloadFormatter::maskCertificate);
		maskedPayload = replaceFieldValue(maskedPayload, FACE_FIELD_PATTERN,
				IscLogPayloadFormatter::truncateFaceValue);
		return truncatePayload(maskedPayload);
	}

	private static String replaceFieldValue(String payload, Pattern pattern, ValueFormatter formatter) {
		Matcher matcher = pattern.matcher(payload);
		StringBuffer output = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(output, Matcher.quoteReplacement(
					matcher.group(1) + formatter.format(matcher.group(2)) + matcher.group(3)));
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private static String maskPhone(String value) {
		return maskValue(value, 3, 4);
	}

	/**
	 * 脱敏证件号，供任务上下文日志复用。
	 */
	public static String maskCertificate(String value) {
		return maskValue(value, 4, 4);
	}

	private static String maskValue(String value, int prefixLength, int suffixLength) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		if (value.length() <= prefixLength + suffixLength) {
			return "***";
		}
		return value.substring(0, prefixLength) + "***" + value.substring(value.length() - suffixLength);
	}

	private static String truncateFaceValue(String value) {
		if (value == null || value.length() <= MAX_FACE_VALUE_LENGTH) {
			return value;
		}
		return value.substring(0, MAX_FACE_VALUE_LENGTH)
				+ "...[truncated=true,original_length=" + value.length() + "]";
	}

	private static String truncatePayload(String payload) {
		if (payload.length() <= MAX_PAYLOAD_LENGTH) {
			return payload;
		}
		return payload.substring(0, MAX_PAYLOAD_LENGTH)
				+ "...[truncated=true,original_length=" + payload.length() + "]";
	}

	@FunctionalInterface
	private interface ValueFormatter {
		String format(String value);
	}
}
