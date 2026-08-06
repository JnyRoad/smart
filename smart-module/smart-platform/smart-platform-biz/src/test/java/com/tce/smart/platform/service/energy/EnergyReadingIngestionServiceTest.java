package com.tce.smart.platform.service.energy;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyIngestionLedgerMapper;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 能耗读数账本幂等行为测试。 */
public class EnergyReadingIngestionServiceTest {

	@Test
	public void legacyPayloadUsesStableFallbackEventId() {
		SmtEnergyIngestionLedgerMapper mapper = mock(SmtEnergyIngestionLedgerMapper.class);
		EnergyReadingIngestionService service = new EnergyReadingIngestionService(mapper);
		EnergyReadingIngestionService.RegisterCommand command = command(null, "1001", 2, "123.45", null);

		String first = service.resolveSourceEventId(command);
		String second = service.resolveSourceEventId(command);

		assertEquals(first, second);
		assertEquals(64, first.length());
	}

	@Test
	public void duplicateWithSamePayloadDoesNotRequestHistoryWorkAgain() {
		SmtEnergyIngestionLedgerMapper mapper = mock(SmtEnergyIngestionLedgerMapper.class);
		when(mapper.insertIgnoreDuplicate(any())).thenReturn(0);
		when(mapper.selectPayloadHash(anyString())).thenReturn("payload-hash");
		EnergyReadingIngestionService service = new EnergyReadingIngestionService(mapper);

		assertFalse(service.register(command("event-1", "1001", 2, "123.45", null), "payload-hash", "{}"));
	}

	@Test
	public void duplicateWithDifferentPayloadFailsClosed() {
		SmtEnergyIngestionLedgerMapper mapper = mock(SmtEnergyIngestionLedgerMapper.class);
		when(mapper.insertIgnoreDuplicate(any())).thenReturn(0);
		when(mapper.selectPayloadHash(anyString())).thenReturn("original-hash");
		EnergyReadingIngestionService service = new EnergyReadingIngestionService(mapper);

		try {
			service.register(command("event-1", "1001", 2, "123.45", null), "changed-hash", "{}");
			fail("相同事件标识但内容变化必须拒绝");
		} catch (SmartException expected) {
			assertTrue(expected.getMessage().contains("冲突"));
		}
	}

	private EnergyReadingIngestionService.RegisterCommand command(String sourceEventId, String deviceCode, Integer sequence, String reading, Integer valveState) {
		return new EnergyReadingIngestionService.RegisterCommand(sourceEventId, "ELE_READ", "ELE", 1L, 11L,
				deviceCode, sequence, reading, valveState, LocalDateTime.of(2026, 8, 5, 10, 0));
	}
}
