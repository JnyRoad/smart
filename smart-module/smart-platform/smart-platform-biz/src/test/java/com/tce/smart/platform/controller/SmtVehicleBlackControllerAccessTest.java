package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.entity.SmtVehicleBlack;
import com.tce.smart.platform.service.SmtVehicleBlackService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/** 车辆黑名单园区对象级授权契约。 */
public class SmtVehicleBlackControllerAccessTest {

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void anonymousBlackListPageIsRejectedBeforeQueryingRecords() {
		SmtVehicleBlackService service = Mockito.mock(SmtVehicleBlackService.class);

		try {
			new SmtVehicleBlackController(service).getSmtAlarmPage(new Page<>(), new SmtVehicleBlack());
			fail("匿名请求不能查询车辆黑名单");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(service);
		}
	}

	@Test
	public void crossParkBlackRecordIsRejected() {
		SmtVehicleBlackService service = Mockito.mock(SmtVehicleBlackService.class);
		SmtVehicleBlack record = new SmtVehicleBlack();
		record.setId(101);
		record.setParkId("20");
		Mockito.when(service.getById(101)).thenReturn(record);
		loginForPark(10);

		try {
			new SmtVehicleBlackController(service).getById(101);
			fail("管理员不能读取未授权园区的黑名单记录");
		} catch (AccessDeniedException expected) {
			Mockito.verify(service).getById(101);
		}
	}

	@Test
	public void authorizedParkBlackRecordCanBeRead() {
		SmtVehicleBlackService service = Mockito.mock(SmtVehicleBlackService.class);
		SmtVehicleBlack record = new SmtVehicleBlack();
		record.setId(102);
		record.setParkId("10");
		Mockito.when(service.getById(102)).thenReturn(record);
		loginForPark(10);

		assertSame(record, new SmtVehicleBlackController(service).getById(102).getData());
	}

	private void loginForPark(Integer parkId) {
		SmartUser user = new SmartUser(1, 1, "vehicle-admin", Collections.singletonList(parkId), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}
}
