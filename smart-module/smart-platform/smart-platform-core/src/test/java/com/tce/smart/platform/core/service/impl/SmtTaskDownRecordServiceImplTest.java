package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 直连下发记录写失败必须向完成流程传播，不能继续删除业务来源。
 */
public class SmtTaskDownRecordServiceImplTest {

	@BeforeClass
	public static void initLambdaMetadata() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				SmtTaskDownRecord.class);
	}

	@Test
	public void failedInsertIsReportedToCaller() {
		Fixture fixture = new Fixture();
		Mockito.doReturn(null).when(fixture.service).getOne(Mockito.any());
		Mockito.doReturn(false).when(fixture.service).save(Mockito.any(SmtTaskDownRecord.class));

		assertWriteFailed(() -> fixture.service.handleTaskDownRecord(task(DeviceTaskActionEnum.DOWN)));
	}

	@Test
	public void failedDeleteDoesNotRemoveSource() {
		Fixture fixture = new Fixture();
		Mockito.doReturn(fixture.record).when(fixture.service).getOne(Mockito.any());
		Mockito.doReturn(false).when(fixture.service).removeById(fixture.record.getId());

		assertWriteFailed(() -> fixture.service.handleTaskDownRecord(task(DeviceTaskActionEnum.DEL)));
		Mockito.verifyZeroInteractions(fixture.sourceSync);
	}

	@Test
	public void failedReplacementDeleteDoesNotInsertNewRecord() {
		Fixture fixture = new Fixture();
		Mockito.doReturn(fixture.record).when(fixture.service).getOne(Mockito.any());
		Mockito.doReturn(false).when(fixture.service).removeById(fixture.record.getId());
		Mockito.doReturn(true).when(fixture.service).save(Mockito.any(SmtTaskDownRecord.class));

		assertWriteFailed(() -> fixture.service.handleTaskDownRecord(task(DeviceTaskActionEnum.UPDATE)));
		Mockito.verify(fixture.service, Mockito.never()).save(Mockito.any(SmtTaskDownRecord.class));
	}

	@Test
	public void failedReplacementInsertIsReportedToCaller() {
		Fixture fixture = new Fixture();
		Mockito.doReturn(fixture.record).when(fixture.service).getOne(Mockito.any());
		Mockito.doReturn(true).when(fixture.service).removeById(fixture.record.getId());
		Mockito.doReturn(false).when(fixture.service).save(Mockito.any(SmtTaskDownRecord.class));

		assertWriteFailed(() -> fixture.service.handleTaskDownRecord(task(DeviceTaskActionEnum.UPDATE)));
	}

	@Test
	public void successfulDeleteStillSynchronizesSource() {
		Fixture fixture = new Fixture();
		Mockito.doReturn(fixture.record).when(fixture.service).getOne(Mockito.any());
		Mockito.doReturn(true).when(fixture.service).removeById(fixture.record.getId());

		fixture.service.handleTaskDownRecord(task(DeviceTaskActionEnum.DEL));

		Mockito.verify(fixture.sourceSync).syncAfterDelete(fixture.record);
	}

 @Test public void currentVersionDeleteOnlyMaintainsRecordAndNeverOldSource() {
  Fixture f=new Fixture();Mockito.doReturn(f.record).when(f.service).getOne(Mockito.any());Mockito.doReturn(true).when(f.service).removeById(f.record.getId());
  com.tce.smart.platform.core.entity.SmtAuthTransportPhase p=new com.tce.smart.platform.core.entity.SmtAuthTransportPhase();p.setAccessType("DIRECT");p.setTaskId("123");p.setParkId(9002);p.setDeviceId("direct-test-device");p.setCardNo("1001");p.setServiceType("1");p.setStartTime(1L);p.setOverTime(2L);p.setAction("ADD");
  p.setAction("DELETE");try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)){f.service.handleTaskDownRecord(task(DeviceTaskActionEnum.DEL));}
  Mockito.verifyZeroInteractions(f.sourceSync);
 }
 @Test public void currentVersionAddReplacesExistingWindow() {
  Fixture f=new Fixture();Mockito.doReturn(f.record).when(f.service).getOne(Mockito.any());Mockito.doReturn(true).when(f.service).removeById(f.record.getId());Mockito.doReturn(true).when(f.service).save(Mockito.any(SmtTaskDownRecord.class));
  com.tce.smart.platform.core.entity.SmtAuthTransportPhase p=new com.tce.smart.platform.core.entity.SmtAuthTransportPhase();p.setAccessType("DIRECT");p.setTaskId("123");p.setParkId(9002);p.setDeviceId("direct-test-device");p.setCardNo("1001");p.setServiceType("1");p.setStartTime(1L);p.setOverTime(2L);p.setAction("ADD");
  Mockito.when(f.deviceMapper.selectById("direct-test-device")).thenReturn(null);
  try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)){f.service.handleTaskDownRecord(task(DeviceTaskActionEnum.DOWN));}
  Mockito.verify(f.service).save(Mockito.argThat((SmtTaskDownRecord r)->r.getId()==null&&r.getOverTime().getTime()==2000&&r.getParkId()==9002));
 }
	@Test public void currentVersionAddRejectsMissingFrozenWindowBeforeRecordWrite() {
		Fixture f=new Fixture();com.tce.smart.platform.core.entity.SmtAuthTransportPhase p=new com.tce.smart.platform.core.entity.SmtAuthTransportPhase();p.setAccessType("DIRECT");p.setTaskId("123");p.setParkId(9002);p.setDeviceId("direct-test-device");p.setCardNo("1001");p.setServiceType("1");p.setAction("ADD");
		try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)){try {f.service.handleTaskDownRecord(task(DeviceTaskActionEnum.DOWN));Assert.fail("缺失冻结有效期必须拒绝");} catch (IllegalArgumentException expected) {Assert.assertTrue(expected.getMessage().contains("有效期"));}}
		Mockito.verify(f.service,Mockito.never()).save(Mockito.any(SmtTaskDownRecord.class));
	}
	private static void assertWriteFailed(Runnable operation) {
		try {
			operation.run();
			Assert.fail("数据库写入返回失败时必须中断收敛");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("记录"));
		}
	}

	private static SmtDeviceTask task(DeviceTaskActionEnum action) {
		SmtDeviceTask task = new SmtDeviceTask();
		task.setId(123);
		task.setDeviceCode("direct-test-device");
		task.setCardNo("1001");
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		task.setAction(action.getCode());
		task.setStartTime(1L);
		task.setOverTime(2L);
		return task;
	}

	private static class Fixture {
		final StaffDeviceAuthSyncService sourceSync = Mockito.mock(StaffDeviceAuthSyncService.class);
		final SmtDeviceMapper deviceMapper = Mockito.mock(SmtDeviceMapper.class);
		final SmtTaskDownRecordServiceImpl service = Mockito.spy(
				new SmtTaskDownRecordServiceImpl(deviceMapper, sourceSync));
		final SmtTaskDownRecord record = new SmtTaskDownRecord();

		Fixture() {
			record.setId(456);
			record.setDeviceCode("direct-test-device");
			record.setCardNo("1001");
			SmtDevice device = new SmtDevice();
			device.setId("direct-test-device");
			device.setParkId(9002);
			Mockito.when(deviceMapper.selectById(device.getId())).thenReturn(device);
		}
	}
}
