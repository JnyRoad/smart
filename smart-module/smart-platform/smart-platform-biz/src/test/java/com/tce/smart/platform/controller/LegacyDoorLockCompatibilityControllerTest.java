package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.LegacyDoorLockStaffRespDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.security.LegacyDoorLockCallerContext;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/** 兼容控制器必须把经过签名认证的调用方园区范围传给每条旧路径。 */
public class LegacyDoorLockCompatibilityControllerTest {

	@Test
	public void parkTreeWithoutParkIdUsesOnlyCallerScope() {
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtDormitoryStaffService dormitoryStaffService = Mockito.mock(SmtDormitoryStaffService.class);
		MockHttpServletRequest request = callerRequest(21, 22);

		controller(parkService, dormitoryStaffService, Mockito.mock(SmtStaffService.class))
				.dormitoryTree(null, request);

		Mockito.verify(parkService).dormitoryAllListToLock(dormitoryStaffService, Arrays.asList(21, 22));
	}

	@Test
	public void staffLookupRejectsDormitoryOutsideCallerScope() {
		SmtDormitoryStaffService dormitoryStaffService = Mockito.mock(SmtDormitoryStaffService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		DormitoryRoomDetailRespDTO room = new DormitoryRoomDetailRespDTO();
		room.setParkId(99);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("A001");
		Mockito.when(dormitoryStaffService.getStaffRoomInfoList("A001")).thenReturn(Collections.singletonList(room));
		Mockito.when(staffService.getSimpleSttaffByBadge("A001")).thenReturn(staff);

		try {
			controller(Mockito.mock(SmtParkService.class), dormitoryStaffService, staffService)
					.staffByBadge("A001", callerRequest(21));
			fail("园区外员工不得被门锁调用方查询");
		} catch (AccessDeniedException expected) {
			Mockito.verify(dormitoryStaffService).getStaffRoomInfoList("A001");
		}
	}

	@Test
	public void staffLookupRejectsWhenAnyDormitoryRecordIsOutsideCallerScope() {
		SmtDormitoryStaffService dormitoryStaffService = Mockito.mock(SmtDormitoryStaffService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		DormitoryRoomDetailRespDTO allowed = new DormitoryRoomDetailRespDTO();
		allowed.setParkId(21);
		DormitoryRoomDetailRespDTO forbidden = new DormitoryRoomDetailRespDTO();
		forbidden.setParkId(99);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("A001");
		Mockito.when(dormitoryStaffService.getStaffRoomInfoList("A001")).thenReturn(Arrays.asList(allowed, forbidden));
		Mockito.when(staffService.getSimpleSttaffByBadge("A001")).thenReturn(staff);

		try {
			controller(Mockito.mock(SmtParkService.class), dormitoryStaffService, staffService)
					.staffByBadge("A001", callerRequest(21));
			fail("多个入住记录中任一园区越权时必须拒绝");
		} catch (AccessDeniedException expected) {
			Mockito.verify(dormitoryStaffService).getStaffRoomInfoList("A001");
		}
	}

	@Test
	public void staffLookupReturnsLegacySuccessNullWhenStaffDoesNotExist() {
		SmtDormitoryStaffService dormitoryStaffService = Mockito.mock(SmtDormitoryStaffService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		Mockito.when(staffService.getSimpleSttaffByBadge("UNKNOWN")).thenReturn(null);

		Result<LegacyDoorLockStaffRespDTO> result = controller(Mockito.mock(SmtParkService.class),
				dormitoryStaffService, staffService).staffByBadge("UNKNOWN", callerRequest(21));

		assertEquals(Integer.valueOf(0), result.getCode());
		assertEquals("success", result.getMessage());
		assertNull(result.getData());
		Mockito.verify(staffService).getSimpleSttaffByBadge("UNKNOWN");
		Mockito.verifyZeroInteractions(dormitoryStaffService);
	}

	@Test
	public void staffLookupRejectsExistingStaffWithoutDormitoryScope() {
		SmtDormitoryStaffService dormitoryStaffService = Mockito.mock(SmtDormitoryStaffService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("A001");
		Mockito.when(staffService.getSimpleSttaffByBadge("A001")).thenReturn(staff);
		Mockito.when(dormitoryStaffService.getStaffRoomInfoList("A001")).thenReturn(Collections.emptyList());

		try {
			controller(Mockito.mock(SmtParkService.class), dormitoryStaffService, staffService)
					.staffByBadge("A001", callerRequest(21));
			fail("无法证明园区归属的真实员工不得返回资料");
		} catch (AccessDeniedException expected) {
			Mockito.verify(staffService).getSimpleSttaffByBadge("A001");
			Mockito.verify(dormitoryStaffService).getStaffRoomInfoList("A001");
		}
	}

	@Test
	public void staffLookupProjectsOnlyApprovedMinimumFields() {
		SmtDormitoryStaffService dormitoryStaffService = Mockito.mock(SmtDormitoryStaffService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		DormitoryRoomDetailRespDTO room = new DormitoryRoomDetailRespDTO();
		room.setParkId(21);
		SmtStaff staff = new SmtStaff();
		staff.setBadge("A001");
		staff.setName("测试员工");
		staff.setPhone("13800000000");
		staff.setCertno("不得返回");
		Mockito.when(dormitoryStaffService.getStaffRoomInfoList("A001")).thenReturn(Collections.singletonList(room));
		Mockito.when(staffService.getSimpleSttaffByBadge("A001")).thenReturn(staff);

		assertEquals("A001", controller(Mockito.mock(SmtParkService.class), dormitoryStaffService, staffService)
				.staffByBadge("A001", callerRequest(21)).getData().getBadge());
		assertEquals("测试员工", controller(Mockito.mock(SmtParkService.class), dormitoryStaffService, staffService)
				.staffByBadge("A001", callerRequest(21)).getData().getName());
		assertEquals("13800000000", controller(Mockito.mock(SmtParkService.class), dormitoryStaffService, staffService)
				.staffByBadge("A001", callerRequest(21)).getData().getPhone());
	}

	private LegacyDoorLockCompatibilityController controller(SmtParkService parkService,
			SmtDormitoryStaffService dormitoryStaffService, SmtStaffService staffService) {
		return new LegacyDoorLockCompatibilityController(parkService, dormitoryStaffService, staffService);
	}

	private MockHttpServletRequest callerRequest(Integer... parkIds) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute(LegacyDoorLockCallerContext.REQUEST_ATTRIBUTE,
				new LegacyDoorLockCallerContext("lock-test", Arrays.asList(parkIds)));
		return request;
	}
}
