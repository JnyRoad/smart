package com.tce.smart.platform.service.impl;

import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.mapper.SmtWageSignMapper;
import com.tce.smart.platform.core.vo.WageSignVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

public class SmtWageSignServiceImplTest {

	/**
	 * 回归用例：短信网关返回成功(CommonConstants.SUCCESS=0)时，应标记为已通知。
	 * 修复前误用 SuccessEnum.SUCCESS.getCode()(=1) 判断成功，导致该场景反而被当作失败处理。
	 */
	@Test
	public void sendMessageMarksNoticeSentWhenSmsSendSucceeds() throws Exception {
		SmtWageSignMapper mapper = Mockito.mock(SmtWageSignMapper.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		SmtWageSignServiceImpl service = new SmtWageSignServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "remoteSmsManageService", remoteSmsManageService);

		WageSignDTO query = new WageSignDTO();
		WageSignVO vo = new WageSignVO();
		vo.setId(1);
		vo.setPhone("13800000000");
		vo.setName("张三");
		Mockito.when(mapper.getCount(query)).thenReturn(Collections.singletonList(vo));
		Mockito.when(remoteSmsManageService.sendWageSign(Mockito.anyList())).thenReturn(Result.success(null));

		Boolean sent = service.sendMessage(query);

		Assert.assertTrue("短信发送成功后应标记为已通知", sent);
		Mockito.verify(mapper).updateNotice(Collections.singletonList(1));
	}

	/**
	 * 回归用例：短信网关返回失败(CommonConstants.FAIL=1)时，不应标记为已通知。
	 */
	@Test
	public void sendMessageSkipsNoticeWhenSmsSendFails() throws Exception {
		SmtWageSignMapper mapper = Mockito.mock(SmtWageSignMapper.class);
		RemoteSmsManageService remoteSmsManageService = Mockito.mock(RemoteSmsManageService.class);
		SmtWageSignServiceImpl service = new SmtWageSignServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "remoteSmsManageService", remoteSmsManageService);

		WageSignDTO query = new WageSignDTO();
		WageSignVO vo = new WageSignVO();
		vo.setId(1);
		vo.setPhone("13800000000");
		vo.setName("张三");
		Mockito.when(mapper.getCount(query)).thenReturn(Collections.singletonList(vo));
		Mockito.when(remoteSmsManageService.sendWageSign(Mockito.anyList())).thenReturn(Result.fail(CommonConstants.FAIL, "网关异常"));

		Boolean sent = service.sendMessage(query);

		Assert.assertFalse("短信发送失败时不应标记为已通知", sent);
		Mockito.verify(mapper, Mockito.never()).updateNotice(Mockito.anyList());
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
