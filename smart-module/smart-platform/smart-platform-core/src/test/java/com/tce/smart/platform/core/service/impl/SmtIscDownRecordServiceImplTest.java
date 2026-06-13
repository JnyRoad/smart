package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class SmtIscDownRecordServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
	}

	@Test
	public void buildDownRecordQueryUsesPersonIdForTemporaryAccessRecords() {
		SmtIscDownRecordServiceImpl service = newService();
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("9990000001");
		task.setPersonId("person-1");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		task.setParkId(5000021);

		LambdaQueryWrapper<SmtIscDownRecord> query = service.buildDownRecordQuery(task);

		String sqlSegment = query.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("DEVICE_CODE"));
		Assert.assertTrue(sqlSegment.contains("DEVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("PERSON_ID"));
		Assert.assertTrue(sqlSegment.contains("SERVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("PARK_ID"));
		Assert.assertFalse(sqlSegment.contains("CARD_NO"));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD_VISITOR));
	}

	@Test
	public void buildDownRecordQueryKeepsCardNoForStaffRecords() {
		SmtIscDownRecordServiceImpl service = newService();
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("1001");
		task.setPersonId("staff-person-1");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		task.setParkId(5000021);

		LambdaQueryWrapper<SmtIscDownRecord> query = service.buildDownRecordQuery(task);

		String sqlSegment = query.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("DEVICE_CODE"));
		Assert.assertTrue(sqlSegment.contains("DEVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("SERVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertFalse(sqlSegment.contains("PERSON_ID"));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD_STAFF_IMPORT));
	}

	@Test
	public void buildDownRecordQueryMatchesLegacyAndNormalizedUpdateFaceRecords() {
		SmtIscDownRecordServiceImpl service = newService();
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("1001");
		task.setPersonId("staff-person-1");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		task.setParkId(5000021);

		LambdaQueryWrapper<SmtIscDownRecord> query = service.buildDownRecordQuery(task);

		String sqlSegment = query.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("DEVICE_CODE"));
		Assert.assertTrue(sqlSegment.contains("DEVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("SERVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertFalse(sqlSegment.contains("PERSON_ID"));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD_STAFF_IMPORT));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.UPDATE_FACE));
	}

	@Test
	public void buildDownRecordQueryKeepsCardNoWhenSameServiceCodeIsNotCardAccess() {
		SmtIscDownRecordServiceImpl service = newService();
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("vehicle-local-id");
		task.setPersonId("person-1");
		task.setDeviceType(DeviceTaskConstants.CAR);
		task.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		task.setParkId(5000021);

		LambdaQueryWrapper<SmtIscDownRecord> query = service.buildDownRecordQuery(task);

		String sqlSegment = query.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("DEVICE_CODE"));
		Assert.assertTrue(sqlSegment.contains("DEVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("SERVICE_TYPE"));
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertFalse(sqlSegment.contains("PERSON_ID"));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CAR));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD_ADMITTANCE));
	}

	@Test
	public void handleTaskDownRecordRemovesAllLegacyAndNormalizedUpdateFaceRecords() {
		SmtIscDownRecordServiceImpl service = Mockito.spy(newService());
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("1001");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		task.setAction(DeviceTaskActionEnum.DEL.getCode());

		SmtIscDownRecord normalizedRecord = new SmtIscDownRecord();
		normalizedRecord.setId(1L);
		normalizedRecord.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		SmtIscDownRecord legacyRecord = new SmtIscDownRecord();
		legacyRecord.setId(2L);
		legacyRecord.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		Mockito.doReturn(Arrays.asList(normalizedRecord, legacyRecord)).when(service).list(Mockito.any());
		Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());

		service.handleTaskDownRecord(task);

		ArgumentCaptor<Collection> idsCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(service).removeByIds(idsCaptor.capture());
		Assert.assertEquals(Arrays.asList(1L, 2L), idsCaptor.getValue());
	}

	@Test
	public void handleTaskDownRecordRemovesLegacyFaceRecordForStaffPermissionDeleteTask() {
		SmtIscDownRecordServiceImpl service = Mockito.spy(newService());
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("1001");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		task.setAction(DeviceTaskActionEnum.DEL.getCode());

		SmtIscDownRecord normalizedRecord = new SmtIscDownRecord();
		normalizedRecord.setId(1L);
		normalizedRecord.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		SmtIscDownRecord legacyRecord = new SmtIscDownRecord();
		legacyRecord.setId(2L);
		legacyRecord.setServiceType(DeviceTaskConstants.UPDATE_FACE);
		Mockito.doReturn(Arrays.asList(normalizedRecord, legacyRecord)).when(service).list(Mockito.any());
		Mockito.doReturn(true).when(service).removeByIds(Mockito.anyCollection());

		service.handleTaskDownRecord(task);

		ArgumentCaptor<Collection> idsCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(service).removeByIds(idsCaptor.capture());
		Assert.assertEquals(Arrays.asList(1L, 2L), idsCaptor.getValue());
		LambdaQueryWrapper<SmtIscDownRecord> query = service.buildDownRecordQuery(task);
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.CARD_STAFF_IMPORT));
		Assert.assertTrue(queryHasParam(query, DeviceTaskConstants.UPDATE_FACE));
	}

	@Test
	public void handleTaskDownRecordSyncsStaffAuthWhenDeleteHasNoLocalDownRecord() {
		StaffDeviceAuthSyncService syncService = Mockito.mock(StaffDeviceAuthSyncService.class);
		SmtIscDownRecordServiceImpl service = Mockito.spy(new SmtIscDownRecordServiceImpl(
				Mockito.mock(SmtDeviceMapper.class), syncService));
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setDeviceCode("device-1");
		task.setCardNo("1001");
		task.setGeneral("8031249-李世勋");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		task.setAction(DeviceTaskActionEnum.DEL.getCode());
		Mockito.doReturn(Collections.emptyList()).when(service).list(Mockito.any());

		service.handleTaskDownRecord(task);

		Mockito.verify(syncService).syncAfterDelete("device-1", "1001", "8031249-李世勋",
				DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT);
	}

	private boolean queryHasParam(LambdaQueryWrapper<SmtIscDownRecord> query, Object expected) {
		query.getSqlSegment();
		return query.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value)));
	}

	private SmtIscDownRecordServiceImpl newService() {
		return new SmtIscDownRecordServiceImpl(Mockito.mock(SmtDeviceMapper.class),
				Mockito.mock(StaffDeviceAuthSyncService.class));
	}
}
