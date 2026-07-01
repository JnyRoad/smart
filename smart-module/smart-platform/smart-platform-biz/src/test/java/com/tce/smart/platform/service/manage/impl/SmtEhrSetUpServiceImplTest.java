package com.tce.smart.platform.service.manage.impl;

import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import com.tce.smart.platform.core.vo.WageSignVO;
import com.tce.smart.platform.service.SmtWageSignService;
import com.tce.smart.platform.service.manage.SmtAttendanceSignService;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

public class SmtEhrSetUpServiceImplTest {

	/**
	 * 回归用例：工资签单短信发送成功(CommonConstants.SUCCESS=0)后应标记为已通知。
	 * 修复前误用 SuccessEnum.SUCCESS.getCode()(=1) 判断成功，导致成功场景反而被跳过。
	 */
	@Test
	public void sendWageSignMarksNoticeSentWhenSmsSendSucceeds() throws Exception {
		SmtWageSignService smtWageSignService = Mockito.mock(SmtWageSignService.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		SmtEhrSetUpServiceImpl service = new SmtEhrSetUpServiceImpl();
		setField(service, "smtWageSignService", smtWageSignService);
		setField(service, "remoteSmsManageService", remoteSmsManageService);

		WageSignVO vo = new WageSignVO();
		vo.setId(1);
		vo.setPhone("13800000000");
		vo.setName("张三");
		Mockito.when(smtWageSignService.getMegInfoList(Mockito.any(WageSignDTO.class))).thenReturn(Collections.singletonList(vo));
		Mockito.when(remoteSmsManageService.sendWageSign(Mockito.anyList())).thenReturn(Result.success(null));

		invokePrivate(service, "sendWageSign", "2026-06", 1);

		Mockito.verify(smtWageSignService).updateNotice(Collections.singletonList(1));
	}

	/**
	 * 回归用例：工资签单短信发送失败(CommonConstants.FAIL=1)时不应标记为已通知。
	 */
	@Test
	public void sendWageSignSkipsNoticeWhenSmsSendFails() throws Exception {
		SmtWageSignService smtWageSignService = Mockito.mock(SmtWageSignService.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		SmtEhrSetUpServiceImpl service = new SmtEhrSetUpServiceImpl();
		setField(service, "smtWageSignService", smtWageSignService);
		setField(service, "remoteSmsManageService", remoteSmsManageService);

		WageSignVO vo = new WageSignVO();
		vo.setId(1);
		vo.setPhone("13800000000");
		vo.setName("张三");
		Mockito.when(smtWageSignService.getMegInfoList(Mockito.any(WageSignDTO.class))).thenReturn(Collections.singletonList(vo));
		Mockito.when(remoteSmsManageService.sendWageSign(Mockito.anyList())).thenReturn(Result.fail(CommonConstants.FAIL, "网关异常"));

		invokePrivate(service, "sendWageSign", "2026-06", 1);

		Mockito.verify(smtWageSignService, Mockito.never()).updateNotice(Mockito.anyList());
	}

	/**
	 * 回归用例：考勤汇总确认短信发送成功后应标记为已通知，原因同上。
	 */
	@Test
	public void sendAttendanceSignMarksNoticeSentWhenSmsSendSucceeds() throws Exception {
		SmtAttendanceSignService smtAttendanceSignService = Mockito.mock(SmtAttendanceSignService.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		SmtEhrSetUpServiceImpl service = new SmtEhrSetUpServiceImpl();
		setField(service, "smtAttendanceSignService", smtAttendanceSignService);
		setField(service, "remoteSmsManageService", remoteSmsManageService);

		AttendanceSignVO vo = new AttendanceSignVO();
		vo.setId(1L);
		vo.setPhone("13800000000");
		vo.setName("李四");
		Mockito.when(smtAttendanceSignService.getMegInfoList(Mockito.any(QueryAttendanceSignDTO.class))).thenReturn(Collections.singletonList(vo));
		Mockito.when(remoteSmsManageService.sendAttendanceSign(Mockito.anyList())).thenReturn(Result.success(null));

		invokePrivate(service, "sendAttendanceSign", "2026-06", 1);

		Mockito.verify(smtAttendanceSignService).updateNotice(Collections.singletonList(1L));
	}

	/**
	 * 回归用例：考勤汇总确认短信发送失败时不应标记为已通知。
	 */
	@Test
	public void sendAttendanceSignSkipsNoticeWhenSmsSendFails() throws Exception {
		SmtAttendanceSignService smtAttendanceSignService = Mockito.mock(SmtAttendanceSignService.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		SmtEhrSetUpServiceImpl service = new SmtEhrSetUpServiceImpl();
		setField(service, "smtAttendanceSignService", smtAttendanceSignService);
		setField(service, "remoteSmsManageService", remoteSmsManageService);

		AttendanceSignVO vo = new AttendanceSignVO();
		vo.setId(1L);
		vo.setPhone("13800000000");
		vo.setName("李四");
		Mockito.when(smtAttendanceSignService.getMegInfoList(Mockito.any(QueryAttendanceSignDTO.class))).thenReturn(Collections.singletonList(vo));
		Mockito.when(remoteSmsManageService.sendAttendanceSign(Mockito.anyList())).thenReturn(Result.fail(CommonConstants.FAIL, "网关异常"));

		invokePrivate(service, "sendAttendanceSign", "2026-06", 1);

		Mockito.verify(smtAttendanceSignService, Mockito.never()).updateNotice(Mockito.anyList());
	}

	private void invokePrivate(Object target, String name, String noticeDate, Integer parkId) throws Exception {
		Method method = target.getClass().getDeclaredMethod(name, String.class, Integer.class);
		method.setAccessible(true);
		method.invoke(target, noticeDate, parkId);
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}
