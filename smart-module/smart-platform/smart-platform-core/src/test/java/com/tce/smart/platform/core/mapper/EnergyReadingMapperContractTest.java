package com.tce.smart.platform.core.mapper;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/** 能耗读数 Mapper 的数据库幂等和时间顺序契约测试。 */
public class EnergyReadingMapperContractTest {

	@Test
	public void ledgerUsesDatabaseUniqueConstraintAndHistoryUsesCollectTimeThenId() throws IOException {
		String ledger = resource("mapper/SmtEnergyIngestionLedgerMapper.xml");
		String eleHistory = resource("mapper/SmtEleMeterHistoryMapper.xml");
		String waterHistory = resource("mapper/SmtWaterMeterHistoryMapper.xml");

		assertTrue(ledger.contains("IGNORE_ROW_ON_DUPKEY_INDEX(SMT_ENERGY_INGESTION_LEDGER (SOURCE_EVENT_ID))"));
		assertTrue(eleHistory.contains("ORDER BY COLLECT_TIME DESC, ID DESC"));
		assertTrue(eleHistory.contains("COLLECT_TIME &lt; #{collectTime}"));
		assertTrue(eleHistory.contains("IS_ERROR = 0"));
		assertTrue(eleHistory.contains("FOR UPDATE"));
		assertTrue(waterHistory.contains("ORDER BY COLLECT_TIME DESC, ID DESC"));
		assertTrue(waterHistory.contains("COLLECT_TIME &lt; #{collectTime}"));
		assertTrue(waterHistory.contains("IS_ERROR = 0"));
		assertTrue(waterHistory.contains("FOR UPDATE"));
	}

	private String resource(String path) throws IOException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(path); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] bytes = new byte[1024];
			int read;
			while ((read = input.read(bytes)) >= 0) output.write(bytes, 0, read);
			return new String(output.toByteArray(), "UTF-8");
		}
	}
}
