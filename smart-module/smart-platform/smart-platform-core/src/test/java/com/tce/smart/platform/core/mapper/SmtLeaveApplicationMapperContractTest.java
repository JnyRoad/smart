package com.tce.smart.platform.core.mapper;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

/** 离职 App 自助授权依赖园区字段，mapper 不能在投影或分页查询中遗漏它。 */
public class SmtLeaveApplicationMapperContractTest {
	@Test
	public void processLookupProjectsParkAndActorRecordQueryFiltersParks() throws Exception {
		String xml = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtLeaveApplicationMapper.xml")),
				StandardCharsets.UTF_8);

		int byProcessId = xml.indexOf("<select id=\"getLeaveApplicationByProcessId\"");
		int byProcessIdEnd = xml.indexOf("</select>", byProcessId);
		String processLookup = xml.substring(byProcessId, byProcessIdEnd);
		assertTrue(processLookup.contains("SLA.PARK_ID"));

		int recordList = xml.indexOf("<select id=\"getLeaveRecordList\"");
		int recordListEnd = xml.indexOf("</select>", recordList);
		String actorRecordList = xml.substring(recordList, recordListEnd);
		assertTrue(actorRecordList.contains("SLA.PARK_ID IN"));
		assertTrue(actorRecordList.contains("collection=\"parkIds\""));
	}
}
