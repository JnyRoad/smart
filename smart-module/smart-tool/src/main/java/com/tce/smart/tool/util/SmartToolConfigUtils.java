package com.tce.smart.tool.util;

import com.tce.smart.common.core.util.SpringContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

final class SmartToolConfigUtils {

	private SmartToolConfigUtils() {
	}

	static String get(String propertyName, String envName) {
		return get(propertyName, envName, "");
	}

	static String get(String propertyName, String envName, String defaultValue) {
		String value = getSpringProperty(propertyName);
		if (hasText(value)) {
			return value;
		}
		value = System.getProperty(propertyName);
		if (hasText(value)) {
			return value;
		}
		value = System.getenv(envName);
		if (hasText(value)) {
			return value;
		}
		return defaultValue;
	}

	static String getRequired(String propertyName, String envName) {
		String value = get(propertyName, envName);
		if (hasText(value)) {
			return value;
		}
		throw new IllegalStateException(propertyName + " or " + envName + " is not configured");
	}

	private static String getSpringProperty(String propertyName) {
		try {
			ApplicationContext context = SpringContextHolder.getApplicationContext();
			if (context == null) {
				return null;
			}
			Environment environment = context.getEnvironment();
			return environment == null ? null : environment.getProperty(propertyName);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}
}
