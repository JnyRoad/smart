package com.tce.smart.platform.service.watermeter;

import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterDataUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterHistory;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyIngestionLedgerMapper;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterHistoryMapper;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import com.tce.smart.platform.service.energy.EnergyReadingIngestionService;
import com.tce.smart.platform.service.watermeter.impl.SmtWaterMeterHistoryServiceImpl;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 水表异常读数不能污染主表当前读数和阀门状态。 */
public class SmtWaterMeterHistoryIngestionTest {

	@Test
	public void abnormalLatestWaterReadingDoesNotUpdateCurrentOrValveButQueuesDates() {
		SmtWaterMeterHistoryMapper historyMapper = mock(SmtWaterMeterHistoryMapper.class);
		SmtWaterMeterService meterService = mock(SmtWaterMeterService.class);
		EnergyProjectionService projectionService = mock(EnergyProjectionService.class);
		SmtEnergyIngestionLedgerMapper ledgerMapper = mock(SmtEnergyIngestionLedgerMapper.class);
		when(ledgerMapper.insertIgnoreDuplicate(any())).thenReturn(1);
		SmtWaterMeterHistoryServiceImpl service = spy(new SmtWaterMeterHistoryServiceImpl());
		ReflectionTestUtils.setField(service, "baseMapper", historyMapper);
		ReflectionTestUtils.setField(service, "waterMeterService", meterService);
		ReflectionTestUtils.setField(service, "energyReadingIngestionService", new EnergyReadingIngestionService(ledgerMapper));
		ReflectionTestUtils.setField(service, "energyProjectionService", projectionService);
		ReflectionTestUtils.setField(service, "life", 100);

		SmtWaterMeter meter = SmtWaterMeter.builder().id(9L).parkId(1).currentReading("100").isOpen(1).build();
		when(meterService.getByConcentratorIdAndSeq(1001L, 2)).thenReturn(meter);
		when(historyMapper.lockMeterForUpdate(9L)).thenReturn(9L);
		when(meterService.getById(9L)).thenReturn(meter);
		when(historyMapper.selectPreviousByCollectTime(anyLong(), any(), anyLong()))
				.thenReturn(SmtWaterMeterHistory.builder().id(8L).currentReading("100").collectTime(LocalDateTime.of(2026, 8, 5, 9, 0)).build());
		AtomicReference<SmtWaterMeterHistory> savedHistory = new AtomicReference<>();
		when(historyMapper.selectLatestByCollectTime(9L)).thenAnswer(invocation -> savedHistory.get());
		doAnswer(invocation -> {
			savedHistory.set(invocation.getArgument(0));
			return Boolean.TRUE;
		}).when(service).save(any(SmtWaterMeterHistory.class));

		WaterMeterDataUpdateDTO dto = new WaterMeterDataUpdateDTO();
		dto.setDeviceCode("1001");
		dto.setWaterMeterSeq(2);
		dto.setWaterMeterCurrVal("90");
		dto.setValveState(0);
		dto.setCollectTime("2026-08-05 10:00:00");
		dto.setSourceEventId("water-abnormal");

		service.saveCurrentReading(dto);

		assertEquals(Integer.valueOf(1), savedHistory.get().getIsError());
		verify(meterService, never()).updateById(any(SmtWaterMeter.class));
		verify(projectionService).requestProjection("WATER", 9L, LocalDate.of(2026, 8, 4));
		verify(projectionService).requestProjection("WATER", 9L, LocalDate.of(2026, 8, 5));
		verify(projectionService).requestProjection("WATER", 9L, LocalDate.of(2026, 8, 6));
	}
}
