package com.tce.smart.tool.enums;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ISCDeviceTaskErrorClassifier {

	private static final List<String> NESTED_ERROR_PRIORITY = Arrays.asList(
			"person", "cards", "card", "fingerprints", "fingerprint", "faces", "face");

	private ISCDeviceTaskErrorClassifier() {
	}

	public static String describeForUser(String errorCode) {
		return ISCDeviceTaskErrorEnum.descriptionForErrorCode(errorCode);
	}

	public static boolean isNoAvailableDownloadData(String errorCode) {
		return ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.matchesErrorCode(errorCode);
	}

	public static List<String> describeNestedErrors(Object value) {
		List<String> descriptions = new ArrayList<>();
		collectNestedErrors(value, descriptions);
		return descriptions;
	}

	private static void collectNestedErrors(Object value, List<String> descriptions) {
		if (value == null) {
			return;
		}
		if (value instanceof JSONObject) {
			JSONObject object = (JSONObject) value;
			String errorCode = object.getStr("errorCode");
			if (isFailure(errorCode)) {
				descriptions.add(describeForUser(errorCode));
			}
			Set<String> visitedKeys = new HashSet<>();
			for (String key : NESTED_ERROR_PRIORITY) {
				if (object.containsKey(key)) {
					visitedKeys.add(key);
					collectNestedErrors(object.get(key), descriptions);
				}
			}
			for (String key : object.keySet()) {
				if (!"errorCode".equals(key) && !visitedKeys.contains(key)) {
					collectNestedErrors(object.get(key), descriptions);
				}
			}
			return;
		}
		if (value instanceof JSONArray) {
			JSONArray array = (JSONArray) value;
			for (int i = 0; i < array.size(); i++) {
				collectNestedErrors(array.get(i), descriptions);
			}
		}
	}

	private static boolean isFailure(String errorCode) {
		return errorCode != null && !"".equals(errorCode.trim())
				&& !ISCDeviceTaskErrorEnum.SUCCESS.matchesErrorCode(errorCode);
	}
}
