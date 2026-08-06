package com.tce.smart.platform.service.energy;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/** 园区日汇总须通过同一持久锚点串行重算，不能依赖 scheduler 的进程锁。 */
public class EnergyProjectionConcurrencyContractTest {
	@Test
	public void mapperUsesExactUniqueAnchorAndRowLock() throws Exception {
		InputStream input = getClass().getResourceAsStream("/mapper/SmtEnergyParkDayLockMapper.xml");
		ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] bytes = new byte[1024]; int length;
		while ((length = input.read(bytes)) >= 0) output.write(bytes, 0, length);
		String xml = new String(output.toByteArray(), "UTF-8");
		assertTrue(xml.contains("IGNORE_ROW_ON_DUPKEY_INDEX(SMT_ENERGY_PARK_DAY_LOCK (PARK_ID, STAT_DATE, RESOURCE_TYPE, UNIT))"));
		assertTrue(xml.contains("FOR UPDATE"));
		assertTrue(xml.indexOf("ensureAnchor") < xml.indexOf("lockForUpdate"));
	}

	@Test
	public void meterDayFenceUsesSameKeyBeforeQueueWrites() throws Exception {
		InputStream input = getClass().getResourceAsStream("/mapper/SmtEnergyMeterDayLockMapper.xml");
		ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] bytes = new byte[1024]; int length;
		while ((length = input.read(bytes)) >= 0) output.write(bytes, 0, length);
		String xml = new String(output.toByteArray(), "UTF-8");
		assertTrue(xml.contains("IGNORE_ROW_ON_DUPKEY_INDEX(SMT_ENERGY_METER_DAY_LOCK (METER_SOURCE, METER_ID, STAT_DATE))"));
		assertTrue(xml.contains("FOR UPDATE"));
	}

	@Test
	public void factParkSnapshotIsNotUpdatedOnMerge() throws Exception {
		InputStream input = getClass().getResourceAsStream("/mapper/SmtEnergyMeterDayFactMapper.xml");
		ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] bytes = new byte[1024]; int length;
		while ((length = input.read(bytes)) >= 0) output.write(bytes, 0, length);
		String xml = new String(output.toByteArray(), "UTF-8");
		String matched = xml.substring(xml.indexOf("WHEN MATCHED THEN UPDATE SET"), xml.indexOf("WHEN NOT MATCHED"));
		assertTrue(!matched.contains("PARK_ID="));
	}

	@Test
	public void parkSummaryCountsProjectedMeters() throws Exception {
		InputStream input = getClass().getResourceAsStream("/mapper/SmtEnergyParkDayMapper.xml");
		ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] bytes = new byte[1024]; int length;
		while ((length = input.read(bytes)) >= 0) output.write(bytes, 0, length);
		assertTrue(new String(output.toByteArray(), "UTF-8").contains("COUNT(DISTINCT i.METER_ID) PROJECTED_COUNT"));
	}
}
