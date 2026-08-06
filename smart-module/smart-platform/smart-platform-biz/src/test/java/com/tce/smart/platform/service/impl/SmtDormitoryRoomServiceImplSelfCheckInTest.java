package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.api.dto.req.SelfCheckInReqDTO;
import com.tce.smart.platform.service.SmtDormitoryBedService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.core.entity.SmtStaff;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 本人入住接口的最小资料契约测试。
 *
 * 浏览器只能提交床位选择，员工身份必须由已认证工号在服务端回填。
 */
public class SmtDormitoryRoomServiceImplSelfCheckInTest {

	@Test
	public void selfCheckInRequestContainsOnlyRoomSelectionFields() {
		Set<String> fields = Arrays.stream(SelfCheckInReqDTO.class.getDeclaredFields())
				.filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
				.map(Field::getName)
				.collect(Collectors.toSet());

		assertEquals(new HashSet<>(Arrays.asList(
				"parkId", "dormitoryId", "floorId", "roomId", "bedId", "roomType")), fields);
	}

	@Test
	public void selfCheckInUsesAuthenticatedBadgeInsteadOfBrowserIdentity() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtDormitoryRoomServiceImpl service = Mockito.spy(new SmtDormitoryRoomServiceImpl());
		SmtDormitoryBedService bedService = Mockito.mock(SmtDormitoryBedService.class);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("real-badge");
		staff.setCertno("server-certno");
		staff.setName("服务端员工");
		staff.setSex(1);
		ReflectionTestUtils.setField(service, "smtStaffService", staffService);
		Mockito.when(staffService.getActiveStaffByBadge("real-badge")).thenReturn(staff);
		Mockito.doReturn(Collections.emptyList()).when(service).autoAllot(Mockito.any(), Mockito.eq(bedService));
		SelfCheckInReqDTO request = new SelfCheckInReqDTO();
		request.setParkId(1);
		request.setDormitoryId(2);
		request.setRoomType(3);

		service.autoAllotForAuthenticatedStaff("real-badge", request, bedService);

		ArgumentCaptor<com.tce.smart.platform.api.dto.req.AutoAllotRoomReqDTO> requestCaptor =
				ArgumentCaptor.forClass(com.tce.smart.platform.api.dto.req.AutoAllotRoomReqDTO.class);
		Mockito.verify(staffService).getActiveStaffByBadge("real-badge");
		Mockito.verify(service).autoAllot(requestCaptor.capture(), Mockito.eq(bedService));
		assertEquals("real-badge", requestCaptor.getValue().getBadge());
		assertEquals("server-certno", requestCaptor.getValue().getCertno());
		assertEquals("服务端员工", requestCaptor.getValue().getName());
	}

	@Test
	public void selfCheckInRejectsAnActiveStaffRecordWithDifferentBadge() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtDormitoryRoomServiceImpl service = Mockito.spy(new SmtDormitoryRoomServiceImpl());
		SmtDormitoryBedService bedService = Mockito.mock(SmtDormitoryBedService.class);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("other-badge");
		staff.setCertno("server-certno");
		ReflectionTestUtils.setField(service, "smtStaffService", staffService);
		Mockito.when(staffService.getActiveStaffByBadge("real-badge")).thenReturn(staff);
		SelfCheckInReqDTO request = new SelfCheckInReqDTO();
		request.setParkId(1);
		request.setDormitoryId(2);
		request.setRoomType(3);

		try {
			service.autoAllotForAuthenticatedStaff("real-badge", request, bedService);
			fail("认证工号与服务端档案不一致时不能创建入住记录");
		} catch (org.springframework.security.access.AccessDeniedException expected) {
			Mockito.verify(service, Mockito.never()).autoAllot(Mockito.any(), Mockito.any());
		}
	}
}
