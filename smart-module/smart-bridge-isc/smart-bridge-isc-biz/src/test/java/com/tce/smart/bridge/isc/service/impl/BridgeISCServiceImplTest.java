package com.tce.smart.bridge.isc.service.impl;

import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BridgeISCServiceImplTest {

	/**
	 * 验证本地订阅标志失真且长期无成功回调时，巡检会重新向ISC发起订阅。
	 */
	@Test
	public void subscribeTaskResubscribesWhenLocalStatusIsStale() throws Exception {
		RecordingBridgeISCService service = new RecordingBridgeISCService();
		setField(service, "eventSubscribeEnabled", true);
		setField(service, "subscribeCallbackUrl", "http://callback.example/isc");
		setField(service, "lastSuccessfulEventCallbackTime",
				System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10));
		boolean originalSubscribeStatus = BridgeISCServiceImpl.SUBSCRIBE_STATUS;
		try {
			BridgeISCServiceImpl.SUBSCRIBE_STATUS = true;

			service.subscribeTask();

			Assert.assertEquals(1, service.getSubscribeRequestCount());
			Assert.assertTrue(BridgeISCServiceImpl.SUBSCRIBE_STATUS);
		} finally {
			BridgeISCServiceImpl.SUBSCRIBE_STATUS = originalSubscribeStatus;
		}
	}

	/**
	 * 验证成功回调仍在十分钟健康窗口内时，巡检不会重复向ISC订阅。
	 */
	@Test
	public void subscribeTaskDoesNotResubscribeWhenCallbackIsHealthy() throws Exception {
		RecordingBridgeISCService service = new RecordingBridgeISCService();
		setField(service, "eventSubscribeEnabled", true);
		setField(service, "subscribeCallbackUrl", "http://callback.example/isc");
		setField(service, "lastSuccessfulEventCallbackTime",
				System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10) + 1000);
		boolean originalSubscribeStatus = BridgeISCServiceImpl.SUBSCRIBE_STATUS;
		try {
			BridgeISCServiceImpl.SUBSCRIBE_STATUS = true;

			service.subscribeTask();

			Assert.assertEquals(0, service.getSubscribeRequestCount());
			Assert.assertTrue(BridgeISCServiceImpl.SUBSCRIBE_STATUS);
		} finally {
			BridgeISCServiceImpl.SUBSCRIBE_STATUS = originalSubscribeStatus;
		}
	}

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

	/**
	 * 通过反射设置服务私有配置，避免测试依赖Spring容器启动。
	 */
	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getSuperclass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	/**
	 * 记录订阅请求次数，隔离真实ISC网络调用。
	 */
	private static class RecordingBridgeISCService extends BridgeISCServiceImpl {
		private int subscribeRequestCount;

		@Override
		public ISCResponse post(EventEnum eventEnum, String data) {
			if (EventEnum.ISC_EVENT_SUBSCRIBE.equals(eventEnum)) {
				subscribeRequestCount++;
			}
			ISCResponse response = new ISCResponse();
			response.setCode("0");
			return response;
		}

		/**
		 * 返回测试期间记录的订阅请求次数。
		 */
		private int getSubscribeRequestCount() {
			return subscribeRequestCount;
		}
	}
}
