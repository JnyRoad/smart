package com.tce.smart.platform.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtVisitorServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
	}

	@Test
	public void addCardKeepsVisitorExactEndTime() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		Date startTime = DateUtil.parseDateTime("2026-06-02 08:30:00");
		Date endTime = DateUtil.parseDateTime("2026-06-02 18:00:00");
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(1001L);
		visitor.setVisitorName("visitor");
		visitor.setVisitorPhotoId("image-1");
		visitor.setCertNo("cert-1");
		visitor.setStartTime(startTime);
		visitor.setEndTime(endTime);

		Method addCard = SmtVisitorServiceImpl.class.getDeclaredMethod("addCard", SmtVisitor.class, String.class);
		addCard.setAccessible(true);
		addCard.invoke(service, visitor, "device-1");

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskConstants.CARD_VISITOR, task.getServiceType());
		Assert.assertEquals(SmtVisitorEnum.CARD_TYPE_7.getType(), task.getCardType());
		Assert.assertEquals(Long.valueOf(endTime.getTime() / 1000), task.getOverTime());
		Assert.assertEquals("cert-1", task.getApplyBadge());
	}

	@Test
	public void addCarCardKeepsVisitorExactEndTimeAndCertNo() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		Date startTime = DateUtil.parseDateTime("2026-06-02 08:30:00");
		Date endTime = DateUtil.parseDateTime("2026-06-02 18:00:00");
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(1001L);
		visitor.setVehiclePlate("粤B12345");
		visitor.setCertNo("cert-1");
		visitor.setStartTime(startTime);
		visitor.setEndTime(endTime);

		Method addCarCard = SmtVisitorServiceImpl.class.getDeclaredMethod("addCarCard", SmtVisitor.class, String.class);
		addCarCard.setAccessible(true);
		addCarCard.invoke(service, visitor, "car-device-1");

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskConstants.CAR, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CAR_VISITOR, task.getServiceType());
		Assert.assertEquals(Long.valueOf(endTime.getTime() / 1000), task.getOverTime());
		Assert.assertEquals("cert-1", task.getApplyBadge());
	}

	@Test
	public void delCarCardTaskReusesCarVisitorDeleteTaskByDeviceType() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(1001L);
		visitor.setVehiclePlate("粤B12345");
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("car-device-1");
		SmtDeviceTask existingDeleteTask = new SmtDeviceTask();
		Mockito.when(taskService.getOne(Mockito.any())).thenReturn(existingDeleteTask);
		Method delCarCardTask = SmtVisitorServiceImpl.class.getDeclaredMethod("delCarCardTask",
				SmtVisitor.class, java.util.List.class);
		delCarCardTask.setAccessible(true);

		delCarCardTask.invoke(service, visitor, Collections.singletonList(relation));

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).getOne(queryCaptor.capture());
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CAR);
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CAR_VISITOR);
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskStatusEnum.INIT.getCode());
		Mockito.verify(taskService).updateById(existingDeleteTask);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void delPersonCardTaskCreatesCardVisitorDeleteTaskWithDeviceType() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(1002L);
		visitor.setVisitorName("visitor");
		visitor.setVisitorPhotoId("image-1");
		visitor.setCertNo("cert-1");
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("device-1");
		Mockito.when(taskService.getOne(Mockito.any())).thenReturn(null);
		Method delPersonCardTask = SmtVisitorServiceImpl.class.getDeclaredMethod("delPersonCardTask",
				SmtVisitor.class, java.util.List.class);
		delPersonCardTask.setAccessible(true);

		delPersonCardTask.invoke(service, visitor, Collections.singletonList(relation));

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).getOne(queryCaptor.capture());
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CARD);
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CARD_VISITOR);
		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(taskCaptor.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, taskCaptor.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_VISITOR, taskCaptor.getValue().getServiceType());
		Assert.assertEquals("1002", taskCaptor.getValue().getCardNo());
		Assert.assertEquals("cert-1", taskCaptor.getValue().getApplyBadge());
	}

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value))));
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
