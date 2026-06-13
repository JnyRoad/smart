package com.tce.smart.bridge.isc.service.impl;

import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BridgeISCServiceImplTest {

	@Test
	public void rawPostResponseUsesEventSpecificErrorDescription() {
		BridgeISCServiceImpl service = new BridgeISCServiceImpl();

		ISCResponse response = service.convertToResp(EventEnum.ISC_EVENT_SUBSCRIBE,
				"{\"code\":\"0x00072001\",\"msg\":\"raw message\",\"data\":null}");

		Assert.assertEquals("0x00072001", response.getCode());
		Assert.assertTrue(response.getMsg().contains("The required parameter $$ is blank."));
		Assert.assertTrue(response.getMsg().contains("0x00072001"));
		Assert.assertTrue(response.getMsg().contains("raw message"));
	}

	@Test
	public void imageFailureResponseUsesAccessControlManagementDescription() {
		BridgeISCServiceImpl service = new BridgeISCServiceImpl();

		ISCResponse response = service.convertToResp(EventEnum.ISC_FACE_IMAGE_GET,
				"{\"code\":\"0x02f19094\",\"msg\":\"raw message\",\"data\":null}");

		Assert.assertEquals("0x02f19094", response.getCode());
		Assert.assertTrue(response.getMsg().contains("请求参数含有非法字符"));
		Assert.assertTrue(response.getMsg().contains("0x02f19094"));
		Assert.assertTrue(response.getMsg().contains("raw message"));
	}

	@Test
	public void rawPostResponseMapsRuntimeCardDeleteNotExistsCodeAndKeepsRawMessage() {
		BridgeISCServiceImpl service = new BridgeISCServiceImpl();

		ISCResponse response = service.convertToResp(EventEnum.ISC_CARD_DELETE,
				"{\"code\":\"0x04a12023\",\"msg\":\"cardNo 12345678 is not exists\",\"data\":null}");

		Assert.assertEquals("0x04a12023", response.getCode());
		Assert.assertTrue(response.getMsg().contains("卡号不存在"));
		Assert.assertTrue(response.getMsg().contains("0x04a12023"));
		Assert.assertTrue(response.getMsg().contains("cardNo 12345678 is not exists"));
	}

	@Test
	public void imageSuccessHttpResponseWithJsonBusinessErrorIsNotReturnedAsImage() {
		BridgeISCServiceImpl service = new BridgeISCServiceImpl();
		byte[] responseBytes = "{\"code\":\"0x02f19094\",\"msg\":\"raw message\",\"data\":null}"
				.getBytes(StandardCharsets.UTF_8);

		byte[] imageBytes = service.resolveImageResponseBytes(EventEnum.ISC_FACE_IMAGE_GET, responseBytes,
				"application/json;charset=UTF-8");

		Assert.assertNull(imageBytes);
	}

	@Test
	public void imageSuccessHttpResponseWithBinaryPayloadReturnsImageBytes() {
		BridgeISCServiceImpl service = new BridgeISCServiceImpl();
		byte[] responseBytes = new byte[] {1, 2, 3, 4};

		byte[] imageBytes = service.resolveImageResponseBytes(EventEnum.ISC_FACE_IMAGE_GET, responseBytes,
				"image/jpeg");

		Assert.assertTrue(Arrays.equals(responseBytes, imageBytes));
	}

	@Test
	public void failedDispatchResponseWithLargeOfficialCodeUsesDescriptionWithoutParsingFailure() {
		BridgeISCServiceImpl service = new BridgeISCServiceImpl();

		try {
			service.convertResp(EventEnum.ISC_TEMPERATURE_GET,
					"{\"code\":\"0x8bf19091\",\"msg\":\"raw message\",\"data\":null}");
			Assert.fail("expected TCEException");
			} catch (TCEException e) {
				Assert.assertTrue(e.getMessage().contains("没有资源权限"));
				Assert.assertTrue(e.getMessage().contains("0x8bf19091"));
				Assert.assertTrue(e.getMessage().contains("raw message"));
			}
	}
}
