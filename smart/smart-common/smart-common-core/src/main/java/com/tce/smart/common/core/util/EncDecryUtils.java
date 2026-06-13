package com.tce.smart.common.core.util;

import java.nio.charset.StandardCharsets;

import org.jasypt.util.text.BasicTextEncryptor;

import cn.hutool.core.codec.Base64;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EncDecryUtils {

	private static final String ENCODE_KEY_PROPERTY = "security.auth-code.encode-key";
	private static final String ENCODE_KEY_ENV = "SECURITY_AUTH_CODE_ENCODE_KEY";

	/**
	 * 使用 jasypt 方式加密字符串
	 *
	 * @param data
	 * @return
	 */
	@SneakyThrows
	public static String encryptByJasypt(String data) {
		return encryptByJasypt(data, getDefaultEncodeKey());
	}

	/**
	 * 使用 jasypt 方式加密字符串
	 *
	 * @param data
	 * @return
	 */
	@SneakyThrows
	public static String encryptByJasypt(String data, String pass) {
		if (isBlank(pass)) {
			throw new IllegalStateException("auth code encode key is not configured");
		}
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword(pass);

		return basicTextEncryptor.encrypt(Base64.encode(data.getBytes(StandardCharsets.UTF_8)));
	}

	@SneakyThrows
	public static String decryptAES(String data) {
		return decryptAES(data, getDefaultEncodeKey());
	}

	@SneakyThrows
	public static String decryptAES(String data, String pass) {
		if (isBlank(pass)) {
			throw new IllegalStateException("auth code encode key is not configured");
		}

		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword(pass);
		return new String(Base64.decode(basicTextEncryptor.decrypt(data), StandardCharsets.UTF_8));
	}

	private static String getDefaultEncodeKey() {
		String value = System.getProperty(ENCODE_KEY_PROPERTY);
		if (!isBlank(value)) {
			return value;
		}
		value = System.getenv(ENCODE_KEY_ENV);
		if (!isBlank(value)) {
			return value;
		}
		throw new IllegalStateException("auth code encode key is not configured");
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
