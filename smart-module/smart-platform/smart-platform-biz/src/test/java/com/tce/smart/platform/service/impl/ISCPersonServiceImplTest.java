package com.tce.smart.platform.service.impl;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.service.SmtStaffOtherService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ISCPersonServiceImplTest {

	@Test
	public void syncISCPersonCardAddsCardToPersonInTargetPark() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		ISCPersonServiceImpl service = service(dispatcherService);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenAnswer(invocation -> {
					DispatcherDTO<Map> dto = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(dto.getEventType())) {
						return Result.success("{\"list\":[{\"personId\":\"isc-person-1\",\"status\":1}]}");
					}
					return Result.success("{}");
				});

		Boolean synced = service.syncISCPersonCard("JA26086", 5000021, "12345678");

		Assert.assertTrue(synced);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN));
		DispatcherDTO<Map> cardRequest = captor.getAllValues().get(1);
		Assert.assertEquals(EventEnum.ISC_CARD_ADD.getCode(), cardRequest.getEventType());
		Assert.assertEquals(Integer.valueOf(5000021), cardRequest.getParkId());
		List cardList = (List) cardRequest.getData().get("cardList");
		Assert.assertEquals(1, cardList.size());
		Map cardItem = (Map) cardList.get(0);
		Assert.assertEquals("isc-person-1", cardItem.get("personId"));
		Assert.assertEquals("12345678", cardItem.get("cardNo"));
		Assert.assertEquals(1, cardItem.get("cardType"));
	}

	@Test
	public void syncISCPersonCardAcceptsHikvisionUppercaseLetterCardNo() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		ISCPersonServiceImpl service = service(dispatcherService);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenAnswer(invocation -> {
					DispatcherDTO<Map> dto = invocation.getArgument(0);
					if (EventEnum.ISC_PERSON_GET.getCode().equals(dto.getEventType())) {
						return Result.success("{\"list\":[{\"personId\":\"isc-person-1\",\"status\":1}]}");
					}
					return Result.success("{}");
				});

		Boolean synced = service.syncISCPersonCard("JA26086", 5000021, "AB123456");

		Assert.assertTrue(synced);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN));
		DispatcherDTO<Map> cardRequest = captor.getAllValues().get(1);
		List cardList = (List) cardRequest.getData().get("cardList");
		Map cardItem = (Map) cardList.get(0);
		Assert.assertEquals("AB123456", cardItem.get("cardNo"));
	}

	@Test
	public void syncISCPersonCardSkipsVirtualCards() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		ISCPersonServiceImpl service = service(dispatcherService);

		Boolean synced = service.syncISCPersonCard("JA26086", 5000021, "9990000001");

		Assert.assertTrue(synced);
		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(), Mockito.anyString());
	}

	@Test
	public void deleteISCPersonCardRequiresResolvedPersonId() throws Exception {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		ISCPersonServiceImpl service = service(dispatcherService);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN)))
				.thenReturn(Result.success("{\"list\":[]}"));

		Boolean deleted = service.deleteISCPersonCard("JA26086", 5000021, "AB123456");

		Assert.assertFalse(deleted);
		ArgumentCaptor<DispatcherDTO> captor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService).dispatch(captor.capture(), Mockito.eq(SecurityConstants.FROM_IN));
		Assert.assertEquals(EventEnum.ISC_PERSON_GET.getCode(), captor.getValue().getEventType());
	}

	private ISCPersonServiceImpl service(RemoteDispatcherService dispatcherService) throws Exception {
		ISCPersonServiceImpl service = new ISCPersonServiceImpl();
		SmtStaffOtherService staffOtherService = Mockito.mock(SmtStaffOtherService.class);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("JA26086");
		staff.setName("张珂");
		Mockito.when(staffOtherService.getOne(Mockito.any())).thenReturn(staff);
		setField(service, "remoteDispatcherService", dispatcherService);
		setField(service, "smtStaffOtherService", staffOtherService);
		setField(service, "xcHpoOrgIndexCode", "org-xc");
		setField(service, "hfOrgIndexCode", "org-hf");
		setField(service, "xcParkId", 5000021);
		return service;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
