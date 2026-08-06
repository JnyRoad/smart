package com.tce.smart.platform.service.energy;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

/** 队列 SQL 必须使用请求版本和处理中状态保护重投竞态。 */
public class EnergyQueueMapperContractTest {
	@Test
	public void claimAndFinishAreConditional() throws Exception {
		InputStream input = getClass().getResourceAsStream("/mapper/SmtEnergyProjectionQueueMapper.xml");
		ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] bytes = new byte[1024]; int length;
		while ((length = input.read(bytes)) >= 0) output.write(bytes, 0, length);
		String xml = new String(output.toByteArray(), "UTF-8");
		assertTrue(xml.contains("REQUEST_COUNT=#{expectedRequestCount}"));
		assertTrue(xml.contains("QUEUE_STATUS='PROCESSING'"));
		assertTrue(xml.contains("PROCESSED_AT &lt;= #{now}"));
		assertTrue(xml.contains("LEASE_TOKEN=#{leaseToken}"));
		assertTrue(xml.contains("LEASE_TOKEN=NULL"));
		assertTrue(xml.contains("IGNORE_ROW_ON_DUPKEY_INDEX(SMT_ENERGY_PROJECTION_QUEUE (METER_SOURCE, METER_ID, STAT_DATE))"));
		assertTrue(xml.contains("RETRY_COUNT=RETRY_COUNT+1"));
		assertTrue(xml.contains("NEXT_RETRY_AT"));
	}
}
