package com.tce.smart.platform.service.watermeter;

import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterDataUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterHistory;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyIngestionLedgerMapper;
import com.tce.smart.platform.core.mapper.watermeter.SmtEleMeterHistoryMapper;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import com.tce.smart.platform.service.energy.EnergyReadingIngestionService;
import com.tce.smart.platform.service.watermeter.impl.SmtEleMeterHistoryServiceImpl;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 设备采集时间顺序和投影脏日期的回归测试。 */
public class SmtEleMeterHistoryIngestionTest {

	@Test
	public void lateDecreasingReadingKeepsMasterCurrentAndQueuesAdjacentDates() {
		SmtEleMeterHistoryMapper historyMapper = mock(SmtEleMeterHistoryMapper.class);
		SmtEleMeterService meterService = mock(SmtEleMeterService.class);
		EnergyProjectionService projectionService = mock(EnergyProjectionService.class);
		SmtEnergyIngestionLedgerMapper ledgerMapper = mock(SmtEnergyIngestionLedgerMapper.class);
		when(ledgerMapper.insertIgnoreDuplicate(any())).thenReturn(1);
		EnergyReadingIngestionService ingestionService = new EnergyReadingIngestionService(ledgerMapper);
		SmtEleMeterHistoryServiceImpl service = spy(new SmtEleMeterHistoryServiceImpl());
		ReflectionTestUtils.setField(service, "baseMapper", historyMapper);
		ReflectionTestUtils.setField(service, "eleMeterService", meterService);
		ReflectionTestUtils.setField(service, "energyReadingIngestionService", ingestionService);
		ReflectionTestUtils.setField(service, "energyProjectionService", projectionService);
		ReflectionTestUtils.setField(service, "life", 100);

		SmtEleMeter meter = SmtEleMeter.builder().id(8L).parkId(1).currentReading("120").build();
		when(meterService.getByConcentratorIdAndSeq(1001L, 2)).thenReturn(meter);
		when(historyMapper.lockMeterForUpdate(8L)).thenReturn(8L);
		when(meterService.getById(8L)).thenReturn(meter);
		when(historyMapper.selectPreviousByCollectTime(anyLong(), any(), anyLong()))
				.thenReturn(SmtEleMeterHistory.builder().id(7L).collectTime(LocalDateTime.of(2026, 8, 5, 9, 0)).currentReading("100").build());
		when(historyMapper.selectLatestByCollectTime(8L))
				.thenReturn(SmtEleMeterHistory.builder().id(Long.MAX_VALUE).collectTime(LocalDateTime.of(2026, 8, 5, 12, 0)).currentReading("120").build());
		doAnswer(invocation -> Boolean.TRUE).when(service).save(any(SmtEleMeterHistory.class));

		EleMeterDataUpdateDTO dto = new EleMeterDataUpdateDTO();
		dto.setDeviceCode("1001");
		dto.setEleMeterSeq(2);
		dto.setEleMeterCurrVal("90");
		dto.setCollectTime("2026-08-05 10:00:00");
		dto.setSourceEventId("late-event");

		service.saveCurrentReading(dto);

		ArgumentCaptor<SmtEleMeterHistory> history = ArgumentCaptor.forClass(SmtEleMeterHistory.class);
		verify(service).save(history.capture());
		assertNotNull(history.getValue().getId());
		assertEquals(LocalDateTime.of(2026, 8, 5, 10, 0), history.getValue().getCollectTime());
		assertEquals(Integer.valueOf(1), history.getValue().getIsError());
		verify(meterService, never()).updateById(any(SmtEleMeter.class));
		verify(historyMapper).lockMeterForUpdate(8L);
		verify(projectionService).requestProjection("ELE", 8L, LocalDate.of(2026, 8, 4));
		verify(projectionService).requestProjection("ELE", 8L, LocalDate.of(2026, 8, 5));
		verify(projectionService).requestProjection("ELE", 8L, LocalDate.of(2026, 8, 6));
		verify(projectionService, times(3)).requestProjection(any(), anyLong(), any());
	}

	@Test
	public void duplicateLedgerEventDoesNotSaveHistoryOrQueueProjection() {
		SmtEleMeterHistoryMapper historyMapper = mock(SmtEleMeterHistoryMapper.class);
		SmtEleMeterService meterService = mock(SmtEleMeterService.class);
		EnergyProjectionService projectionService = mock(EnergyProjectionService.class);
		SmtEnergyIngestionLedgerMapper ledgerMapper = mock(SmtEnergyIngestionLedgerMapper.class);
		when(ledgerMapper.insertIgnoreDuplicate(any())).thenReturn(0);
		EnergyReadingIngestionService ingestionService = new EnergyReadingIngestionService(ledgerMapper);
		SmtEleMeterHistoryServiceImpl service = spy(new SmtEleMeterHistoryServiceImpl());
		ReflectionTestUtils.setField(service, "baseMapper", historyMapper);
		ReflectionTestUtils.setField(service, "eleMeterService", meterService);
		ReflectionTestUtils.setField(service, "energyReadingIngestionService", ingestionService);
		ReflectionTestUtils.setField(service, "energyProjectionService", projectionService);

		SmtEleMeter meter = SmtEleMeter.builder().id(8L).parkId(1).currentReading("100").build();
		when(meterService.getByConcentratorIdAndSeq(1001L, 2)).thenReturn(meter);
		when(historyMapper.lockMeterForUpdate(8L)).thenReturn(8L);
		when(meterService.getById(8L)).thenReturn(meter);
		EleMeterDataUpdateDTO dto = new EleMeterDataUpdateDTO();
		dto.setDeviceCode("1001");
		dto.setEleMeterSeq(2);
		dto.setEleMeterCurrVal("100");
		dto.setCollectTime("2026-08-05 10:00:00");
		dto.setSourceEventId("duplicate-event");
		when(ledgerMapper.selectPayloadHash("duplicate-event")).thenReturn(ingestionService.hashPayload(JSONUtil.toJsonStr(dto)));

		service.saveCurrentReading(dto);

		verify(service, never()).save(any(SmtEleMeterHistory.class));
		verify(meterService, never()).updateById(any(SmtEleMeter.class));
		verify(projectionService, never()).requestProjection(any(), anyLong(), any());
	}

	@Test
	public void invalidReadingFailsClosedBeforeAnyDatabaseAccess() {
		SmtEleMeterHistoryServiceImpl service = new SmtEleMeterHistoryServiceImpl();
		EleMeterDataUpdateDTO dto = new EleMeterDataUpdateDTO();
		dto.setDeviceCode("1001");
		dto.setEleMeterSeq(2);
		dto.setEleMeterCurrVal("-1");
		dto.setCollectTime("2026-08-05 10:00:00");

		try {
			service.saveCurrentReading(dto);
			throw new AssertionError("负数读数必须拒绝");
		} catch (SmartException expected) {
			assertEquals("电表读数、集中器标识或采集时间格式不合法", expected.getMessage());
		}
	}
}
