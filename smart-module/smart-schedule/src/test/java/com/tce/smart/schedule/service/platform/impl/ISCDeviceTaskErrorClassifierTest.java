package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.JSONUtil;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.enums.ISCApiErrorCodeClassifier;
import com.tce.smart.tool.enums.ISCDeviceTaskErrorClassifier;
import com.tce.smart.tool.enums.ISCDeviceTaskErrorEnum;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ISCDeviceTaskErrorClassifierTest {

	@Test
	public void officialApiCodesUsedByIscInterfacesAreMappedByEventContext() {
		Assert.assertTrue(ISCApiErrorCodeClassifier.officialDefinitionCount() >= 1600);
		Assert.assertEquals("参数错误：必填参数为空",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_PERSON_ADD, "0x00072001"));
		Assert.assertEquals("The required parameter $$ is blank.",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_EVENT_SUBSCRIBE, "0x00072001"));
		Assert.assertEquals("请求参数含有非法字符",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_FACE_IMAGE_GET, "0x02f19094"));
		Assert.assertEquals("操作成功",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_FACE_IMAGE_GET, "0x00000000"));
		Assert.assertEquals("没有资源权限",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_TEMPERATURE_GET, "0x8BF19091"));
		Assert.assertEquals("参数解析失败",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_ACCESS_DEVICE_STATUS_GET, " 0x03511018 "));
		Assert.assertEquals("下载错误：无可用数据下载",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_TASK_PROCESS_GET, "0x15403007"));
		Assert.assertEquals("回调错误：人脸图片不符合要求",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_TASK_PROCESS_GET, "0x15403519"));
		Assert.assertEquals("参数错误：参数格式不正确",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_CARD_ADD, "0x072003"));
		Assert.assertEquals("卡号不存在",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_CARD_DELETE, "0x04a12701"));
		Assert.assertEquals("卡号不存在",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_CARD_DELETE, "0x04a12023"));
	}

	@Test
	public void unknownOfficialApiCodeIsSanitizedForUserMessage() {
		Assert.assertEquals("ISC返回未知错误",
				ISCApiErrorCodeClassifier.describeForUser(EventEnum.ISC_PERSON_ADD, "0x15409999"));
	}

	@Test
	public void officialAccessControlPermissionCodesAreMapped() {
		Map<String, ISCDeviceTaskErrorEnum> officialCodes = new LinkedHashMap<>();
		officialCodes.put("0", ISCDeviceTaskErrorEnum.SUCCESS);
		officialCodes.put("0x15400001", ISCDeviceTaskErrorEnum.SERVICE_EXCEPTION);
		officialCodes.put("0x15400002", ISCDeviceTaskErrorEnum.REQUIRED_FIELD_EMPTY);
		officialCodes.put("0x15400003", ISCDeviceTaskErrorEnum.FIELD_VALIDATION_FAILED);
		officialCodes.put("0x15404001", ISCDeviceTaskErrorEnum.RESOURCE_NOT_FOUND);
		officialCodes.put("0x15401001", ISCDeviceTaskErrorEnum.SCHEDULE_TEMPLATE_ID_NOT_FOUND);
		officialCodes.put("0x15401002", ISCDeviceTaskErrorEnum.HOLIDAY_GROUP_ID_NOT_FOUND);
		officialCodes.put("0x15401003", ISCDeviceTaskErrorEnum.SCHEDULE_TEMPLATE_NAME_DUPLICATED);
		officialCodes.put("0x15401004", ISCDeviceTaskErrorEnum.HOLIDAY_GROUP_NAME_DUPLICATED);
		officialCodes.put("0x15405001", ISCDeviceTaskErrorEnum.TASK_NOT_FOUND);
		officialCodes.put("0x15405002", ISCDeviceTaskErrorEnum.TASK_ALREADY_DOWNLOADING);
		officialCodes.put("0x15405003", ISCDeviceTaskErrorEnum.TASK_DELETE_FAILED);
		officialCodes.put("0x15405004", ISCDeviceTaskErrorEnum.TASK_STOP_FAILED);
		officialCodes.put("0x15405005", ISCDeviceTaskErrorEnum.TASK_PAUSE_FAILED);
		officialCodes.put("0x15405006", ISCDeviceTaskErrorEnum.TASK_ADD_DOWNLOAD_DATA_FAILED);
		officialCodes.put("0x15403007", ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA);
		officialCodes.put("0x15403008", ISCDeviceTaskErrorEnum.DOWNLOAD_PERMISSION_PACKET_SEND_FAILED);
		officialCodes.put("0x15403009", ISCDeviceTaskErrorEnum.DOWNLOAD_ACCESS_SERVICE_OFFLINE);
		officialCodes.put("0x1540300a", ISCDeviceTaskErrorEnum.DOWNLOAD_DEVICE_OFFLINE);
		officialCodes.put("0x1540300b", ISCDeviceTaskErrorEnum.DOWNLOAD_DEVICE_BUSY);
		officialCodes.put("0x1540300c", ISCDeviceTaskErrorEnum.DOWNLOAD_DRIVER_ADDRESS_QUERY_FAILED);
		officialCodes.put("0x1540300d", ISCDeviceTaskErrorEnum.DOWNLOAD_CARD_PERMISSION_UNSUPPORTED);
		officialCodes.put("0x1540300e", ISCDeviceTaskErrorEnum.DOWNLOAD_FINGERPRINT_PERMISSION_UNSUPPORTED);
		officialCodes.put("0x1540300f", ISCDeviceTaskErrorEnum.DOWNLOAD_FACE_PERMISSION_UNSUPPORTED);
		officialCodes.put("0x15403010", ISCDeviceTaskErrorEnum.DOWNLOAD_DEVICE_CAPACITY_FULL);
		officialCodes.put("0x15403011", ISCDeviceTaskErrorEnum.DOWNLOAD_PERSON_CARD_NOT_OPENED);
		officialCodes.put("0x15403012", ISCDeviceTaskErrorEnum.DOWNLOAD_PERSON_NO_FINGERPRINT);
		officialCodes.put("0x15403013", ISCDeviceTaskErrorEnum.DOWNLOAD_PERSON_NO_FACE_IMAGE);
		officialCodes.put("0x15403014", ISCDeviceTaskErrorEnum.DOWNLOAD_FACE_IMAGE_ADDRESS_QUERY_FAILED);
		officialCodes.put("0x15403501", ISCDeviceTaskErrorEnum.CALLBACK_DRIVER_RESULT_TIMEOUT);
		officialCodes.put("0x15403502", ISCDeviceTaskErrorEnum.CALLBACK_DOWNLOAD_RECORD_PROCESS_FAILED);
		officialCodes.put("0x15403503", ISCDeviceTaskErrorEnum.CALLBACK_SCHEDULE_TEMPLATE_DOWNLOAD_FAILED);
		officialCodes.put("0x15403504", ISCDeviceTaskErrorEnum.CALLBACK_CLEAR_PERMISSION_FAILED);
		officialCodes.put("0x15403505", ISCDeviceTaskErrorEnum.CALLBACK_DEVICE_DOWNLOAD_TIMEOUT);
		officialCodes.put("0x15403506", ISCDeviceTaskErrorEnum.CALLBACK_PERMISSION_DATA_INVALID);
		officialCodes.put("0x15403507", ISCDeviceTaskErrorEnum.CALLBACK_CARD_NUMBER_INVALID);
		officialCodes.put("0x15403508", ISCDeviceTaskErrorEnum.CALLBACK_BIOMETRIC_ALREADY_EXISTS);
		officialCodes.put("0x15403509", ISCDeviceTaskErrorEnum.CALLBACK_BIOMETRIC_QUALITY_POOR);
		officialCodes.put("0x1540350a", ISCDeviceTaskErrorEnum.CALLBACK_IMAGE_SERVER_NOT_BOUND);
		officialCodes.put("0x1540350b", ISCDeviceTaskErrorEnum.CALLBACK_FACE_IMAGE_DOWNLOAD_FAILED);
		officialCodes.put("0x1540350c", ISCDeviceTaskErrorEnum.CALLBACK_FACE_MODELING_FAILED);
		officialCodes.put("0x1540350d", ISCDeviceTaskErrorEnum.CALLBACK_FACE_EYE_DISTANCE_TOO_SMALL);
		officialCodes.put("0x1540350e", ISCDeviceTaskErrorEnum.CALLBACK_CARD_PERMISSION_NOT_ISSUED);
		officialCodes.put("0x1540350f", ISCDeviceTaskErrorEnum.CALLBACK_UNKNOWN_REASON);
		officialCodes.put("0x15403519", ISCDeviceTaskErrorEnum.CALLBACK_FACE_IMAGE_INVALID);

		for (Map.Entry<String, ISCDeviceTaskErrorEnum> entry : officialCodes.entrySet()) {
			Assert.assertSame(entry.getKey(), entry.getValue(),
					ISCDeviceTaskErrorEnum.fromErrorCode(entry.getKey()).orElse(null));
			Assert.assertEquals(entry.getKey(), entry.getValue().getDesc(),
					ISCDeviceTaskErrorClassifier.describeForUser(entry.getKey()));
		}
	}

	@Test
	public void unknownCodeIsSanitizedForUserMessage() {
		Assert.assertFalse(ISCDeviceTaskErrorEnum.fromErrorCode("0x15409999").isPresent());
		Assert.assertEquals("ISC返回未知错误", ISCDeviceTaskErrorClassifier.describeForUser("0x15409999"));
	}

	@Test
	public void internalCodeLookupUsesEnumConstants() {
		Assert.assertSame(ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA,
				ISCDeviceTaskErrorEnum.fromInternalCode(
						ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getCode()).orElse(null));
		Assert.assertSame(ISCDeviceTaskErrorEnum.CALLBACK_DEVICE_DOWNLOAD_TIMEOUT,
				ISCDeviceTaskErrorEnum.fromInternalCode(
						ISCDeviceTaskErrorEnum.CALLBACK_DEVICE_DOWNLOAD_TIMEOUT.getCode()).orElse(null));
		Assert.assertSame(ISCDeviceTaskErrorEnum.IMAGE_INFO_DECODE_FAILED,
				ISCDeviceTaskErrorEnum.fromInternalCode(
						ISCDeviceTaskErrorEnum.IMAGE_INFO_DECODE_FAILED.getCode()).orElse(null));
	}

	@Test
	public void enumConstantsDoNotUseNumberedErrorNames() {
		for (ISCDeviceTaskErrorEnum error : ISCDeviceTaskErrorEnum.values()) {
			Assert.assertFalse(error.name(), error.name().matches("ERROR_\\d+"));
		}
	}

	@Test
	public void classifierDetectsNoAvailableDownloadDataCaseInsensitively() {
		String mixedCaseNoAvailableDataCode = " "
				+ ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.getErrorCode().toUpperCase() + " ";
		Assert.assertTrue(ISCDeviceTaskErrorEnum.DOWNLOAD_NO_AVAILABLE_DATA.matchesErrorCode(mixedCaseNoAvailableDataCode));
		Assert.assertTrue(ISCDeviceTaskErrorClassifier.isNoAvailableDownloadData(mixedCaseNoAvailableDataCode));
		Assert.assertFalse(ISCDeviceTaskErrorClassifier.isNoAvailableDownloadData(
				ISCDeviceTaskErrorEnum.DOWNLOAD_PERMISSION_PACKET_SEND_FAILED.getErrorCode()));
	}

	@Test
	public void extractsNestedNonSuccessErrorCodesInOrder() {
		List<String> descriptions = ISCDeviceTaskErrorClassifier.describeNestedErrors(JSONUtil.parseObj("{"
				+ "\"person\":{\"errorCode\":\"0\"},"
				+ "\"cards\":[{\"errorCode\":\""
				+ ISCDeviceTaskErrorEnum.CALLBACK_CARD_NUMBER_INVALID.getErrorCode() + "\"}],"
				+ "\"fingerprints\":[{\"errorCode\":\""
				+ ISCDeviceTaskErrorEnum.DOWNLOAD_PERSON_NO_FINGERPRINT.getErrorCode() + "\"}],"
				+ "\"faces\":[{\"errorCode\":\"0\"},{\"errorCode\":\""
				+ ISCDeviceTaskErrorEnum.CALLBACK_FACE_MODELING_FAILED.getErrorCode() + "\"}]"
				+ "}"));

		Assert.assertEquals(3, descriptions.size());
		Assert.assertEquals("回调错误：卡号错误", descriptions.get(0));
		Assert.assertEquals("下载错误：人员没有指纹", descriptions.get(1));
		Assert.assertEquals("回调错误：人脸建模失败", descriptions.get(2));
	}

	@Test
	public void treatsNullOrSuccessOnlyNestedErrorsAsEmpty() {
		Assert.assertTrue(ISCDeviceTaskErrorClassifier.describeNestedErrors(null).isEmpty());
		Assert.assertTrue(ISCDeviceTaskErrorClassifier.describeNestedErrors(JSONUtil.parseObj("{"
				+ "\"person\":{\"errorCode\":\"0\"},"
				+ "\"faces\":[{\"errorCode\":\"0\"}]"
				+ "}")).isEmpty());
	}
}
