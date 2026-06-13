package com.tce.smart.platform.core.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SmtStaffMapperXmlTest {

	@Test
	public void iscCardImportStaffUsesParkBuCompIdForParkScope() throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("mapper/SmtStaffMapper.xml");
		Assert.assertNotNull(stream);
		String mapperXml = readString(stream);
		String importStaffSql = iscCardImportStaffSql(mapperXml);

		Assert.assertTrue(importStaffSql.contains("LEFT JOIN SMT_PARK_BU EBU ON EBU.COMP_ID = STAFF.COMP_ID"));
		Assert.assertFalse(importStaffSql.contains("LEFT JOIN SMT_PARK_BU EBU ON EBU.ID = STAFF.COMP_ID"));
	}

	@Test
	public void iscCardImportStaffDoesNotUnconditionallyExcludeResignedStaff() throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("mapper/SmtStaffMapper.xml");
		Assert.assertNotNull(stream);
		String mapperXml = readString(stream);
		String importStaffSql = iscCardImportStaffSql(mapperXml);

		Assert.assertFalse(importStaffSql.contains("WHERE STAFF.COMP_ID IS NOT NULL\n\t\t  AND STAFF.STATUS != 0"));
	}

	@Test
	public void iscCardImportStaffSupportsStaffScopeFilters() throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("mapper/SmtStaffMapper.xml");
		Assert.assertNotNull(stream);
		String mapperXml = readString(stream);
		String importStaffSql = iscCardImportStaffSql(mapperXml);

		Assert.assertTrue(importStaffSql.contains("staffScope"));
		Assert.assertTrue(importStaffSql.contains("STAFF.STATUS != 0"));
		Assert.assertTrue(importStaffSql.contains("STAFF.STATUS = 0"));
	}

	private String iscCardImportStaffSql(String mapperXml) {
		int start = mapperXml.indexOf("<select id=\"listIscCardImportStaff\"");
		int end = mapperXml.indexOf("</select>", start);
		Assert.assertTrue(start > 0);
		Assert.assertTrue(end > start);
		return mapperXml.substring(start, end);
	}

	private String readString(InputStream stream) throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int length;
		while ((length = stream.read(buffer)) >= 0) {
			output.write(buffer, 0, length);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}
}
