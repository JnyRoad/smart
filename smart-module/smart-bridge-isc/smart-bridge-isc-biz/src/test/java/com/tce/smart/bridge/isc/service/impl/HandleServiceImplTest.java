package com.tce.smart.bridge.isc.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.isc.service.HBaseFileService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class HandleServiceImplTest {

	@Test
	public void eventHandleSendsPersonIdAndOmitsBlankCardNoWhenIscJobNoMissing() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		BridgeISCServiceImpl bridgeISCService = Mockito.mock(BridgeISCServiceImpl.class);
		HBaseFileService hBaseFileService = Mockito.mock(HBaseFileService.class);
		HandleServiceImpl handleService = new HandleServiceImpl();
		setField(handleService, "remoteDispatcherService", dispatcherService);
		setField(handleService, "bridgeISCService", bridgeISCService);
		setField(handleService, "hBaseFileService", hBaseFileService);
		setField(handleService, "parkId", 5000021);
		ISCResponse personResponse = new ISCResponse();
		personResponse.setCode("0");
		personResponse.setData("{\"list\":[{\"personId\":\"isc-person-1\",\"jobNo\":\"\"}]}");
		Mockito.when(bridgeISCService.post(Mockito.eq(EventEnum.ISC_PERSON_GET), Mockito.anyString()))
				.thenReturn(personResponse);
		Mockito.when(bridgeISCService.downISCImage(Mockito.eq(EventEnum.ISC_FACE_IMAGE_GET),
				Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		Mockito.when(dispatcherService.handle(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success(true));
		String eventData = "{\"params\":{\"events\":[{\"eventType\":196893,\"srcParentIndex\":\"device-1\","
				+ "\"happenTime\":\"2026-06-01T10:00:00+08:00\",\"data\":{\"ExtEventPersonNo\":\"isc-person-1\","
				+ "\"ExtEventInOut\":1,\"svrIndexCode\":\"svr-1\",\"ExtEventPictureURL\":\"pic-1\"}}]}}";

		handleService.eventHandle(eventData);

		ArgumentCaptor<BridgeDTO> captor = ArgumentCaptor.forClass(BridgeDTO.class);
		Mockito.verify(dispatcherService).handle(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN),
				Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		JSONObject payload = JSONUtil.parseObj(captor.getValue().getData());
		Assert.assertEquals("isc-person-1", payload.getStr("personId"));
		Assert.assertEquals("device-1", payload.getStr("deviceCode"));
		Assert.assertFalse(payload.containsKey("cardNo"));
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
