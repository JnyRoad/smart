package com.tce.smart.app.service.fore.impl;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.SmtOutDormitoryStaffDTO;
import com.tce.smart.platform.api.feign.RemoteCallowanceCancelRecordService;
import com.tce.smart.platform.api.feign.RemoteOutDormitoryStaffService;
import com.tce.smart.platform.core.vo.CallowanceDetailVO;
import com.tce.smart.platform.core.vo.OutDormitoryDetailVO;
import com.tce.smart.tool.exception.TCEException;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 员工外宿流程必须以当前认证工号作为唯一业务主体。
 */
public class EmployeeServiceAuthorizationTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void outDormitoryApplicationAlwaysUsesAuthenticatedEmployeeInsteadOfCallerBadge() {
		RemoteOutDormitoryStaffService outDormitoryService = Mockito.mock(RemoteOutDormitoryStaffService.class);
		Mockito.when(outDormitoryService.addOutDormitory(Mockito.any(SmtOutDormitoryStaffDTO.class), Mockito.anyString()))
				.thenReturn(Result.success(Boolean.TRUE));
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "outDormitoryStaffService", outDormitoryService);
		setAuthenticatedEmployee("employee-self");
		SmtOutDormitoryStaffDTO application = new SmtOutDormitoryStaffDTO();
		application.setStaffBadge("employee-other");

		service.outRoomApply(application);

		ArgumentCaptor<SmtOutDormitoryStaffDTO> request = ArgumentCaptor.forClass(SmtOutDormitoryStaffDTO.class);
		Mockito.verify(outDormitoryService).addOutDormitory(request.capture(), Mockito.anyString());
		assertEquals("employee-self", request.getValue().getStaffBadge());
	}

	@Test
	public void outDormitoryDetailRejectsARecordOwnedByAnotherEmployee() {
		RemoteOutDormitoryStaffService outDormitoryService = Mockito.mock(RemoteOutDormitoryStaffService.class);
		OutDormitoryDetailVO foreignDetail = new OutDormitoryDetailVO();
		foreignDetail.setStaffBadge("employee-other");
		Mockito.when(outDormitoryService.outRoomApplyDetail(Mockito.eq(123), Mockito.anyString()))
				.thenReturn(Result.success(foreignDetail));
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "outDormitoryStaffService", outDormitoryService);
		setAuthenticatedEmployee("employee-self");

		try {
			service.outRoomApplyDetail(123);
			fail("其他员工的外宿详情不得返回给当前登录员工");
		} catch (TCEException expected) {
			assertEquals("无权查看其他员工外宿记录", expected.getMessage());
		}
	}

	@Test
	public void outDormitoryDetailKeepsTheCurrentEmployeesLegitimateRecord() {
		RemoteOutDormitoryStaffService outDormitoryService = Mockito.mock(RemoteOutDormitoryStaffService.class);
		OutDormitoryDetailVO ownDetail = new OutDormitoryDetailVO();
		ownDetail.setStaffBadge("employee-self");
		Result expected = Result.success(ownDetail);
		Mockito.when(outDormitoryService.outRoomApplyDetail(Mockito.eq(123), Mockito.anyString())).thenReturn(expected);
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "outDormitoryStaffService", outDormitoryService);
		setAuthenticatedEmployee("employee-self");

		assertEquals(expected, service.outRoomApplyDetail(123));
	}

	@Test
	public void allowanceCancellationDetailRejectsARecordOwnedByAnotherEmployee() {
		RemoteCallowanceCancelRecordService allowanceService = Mockito.mock(RemoteCallowanceCancelRecordService.class);
		CallowanceDetailVO foreignDetail = new CallowanceDetailVO();
		foreignDetail.setBadge("employee-other");
		Mockito.when(allowanceService.detail(Mockito.eq(456), Mockito.anyString())).thenReturn(Result.success(foreignDetail));
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "remoteCallowanceCancelRecordService", allowanceService);
		setAuthenticatedEmployee("employee-self");

		try {
			service.callowanceCancelDetail(456);
			fail("其他员工的外宿补贴撤销记录不得返回给当前登录员工");
		} catch (TCEException expected) {
			assertEquals("无权查看其他员工外宿补贴记录", expected.getMessage());
		}
	}

	@Test
	public void allowanceCancellationDetailKeepsTheCurrentEmployeesLegitimateRecord() {
		RemoteCallowanceCancelRecordService allowanceService = Mockito.mock(RemoteCallowanceCancelRecordService.class);
		CallowanceDetailVO ownDetail = new CallowanceDetailVO();
		ownDetail.setBadge("employee-self");
		Result expected = Result.success(ownDetail);
		Mockito.when(allowanceService.detail(Mockito.eq(456), Mockito.anyString())).thenReturn(expected);
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "remoteCallowanceCancelRecordService", allowanceService);
		setAuthenticatedEmployee("employee-self");

		assertEquals(expected, service.callowanceCancelDetail(456));
	}

	@Test
	public void outDormitoryDetailFailsClosedWhenTheOwnerFieldIsMissing() {
		RemoteOutDormitoryStaffService outDormitoryService = Mockito.mock(RemoteOutDormitoryStaffService.class);
		Mockito.when(outDormitoryService.outRoomApplyDetail(Mockito.eq(123), Mockito.anyString()))
				.thenReturn(Result.success(new OutDormitoryDetailVO()));
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "outDormitoryStaffService", outDormitoryService);
		setAuthenticatedEmployee("employee-self");

		try {
			service.outRoomApplyDetail(123);
			fail("缺少归属工号的详情不得返回给当前登录员工");
		} catch (TCEException expected) {
			assertEquals("无权查看其他员工外宿记录", expected.getMessage());
		}
	}

	@Test
	public void outDormitoryDetailFailsClosedWhenTheRemoteServiceReturnsNull() {
		RemoteOutDormitoryStaffService outDormitoryService = Mockito.mock(RemoteOutDormitoryStaffService.class);
		Mockito.when(outDormitoryService.outRoomApplyDetail(Mockito.eq(123), Mockito.anyString())).thenReturn(null);
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "outDormitoryStaffService", outDormitoryService);
		setAuthenticatedEmployee("employee-self");

		try {
			service.outRoomApplyDetail(123);
			fail("远程详情为空时必须按拒绝处理");
		} catch (TCEException expected) {
			assertEquals("查询员工记录失败", expected.getMessage());
		}
	}

	@Test
	public void outDormitoryDetailAcceptsTheFeignMapPayloadForTheCurrentEmployee() {
		RemoteOutDormitoryStaffService outDormitoryService = Mockito.mock(RemoteOutDormitoryStaffService.class);
		Map<String, Object> ownDetail = new HashMap<>();
		ownDetail.put("staffBadge", "employee-self");
		Result expected = Result.success(ownDetail);
		Mockito.when(outDormitoryService.outRoomApplyDetail(Mockito.eq(123), Mockito.anyString())).thenReturn(expected);
		EmployeeServiceImpl service = new EmployeeServiceImpl();
		ReflectionTestUtils.setField(service, "outDormitoryStaffService", outDormitoryService);
		setAuthenticatedEmployee("employee-self");

		assertEquals(expected, service.outRoomApplyDetail(123));
	}

	private void setAuthenticatedEmployee(String badge) {
		SmartUser user = new SmartUser(1, 1, badge, Collections.singletonList(5000021), "N/A", true, true,
				true, true, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities()));
	}
}
