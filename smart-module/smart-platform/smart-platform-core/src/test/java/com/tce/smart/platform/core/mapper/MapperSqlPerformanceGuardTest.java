package com.tce.smart.platform.core.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MapperSqlPerformanceGuardTest {

	@Test
	public void msgRecordReadStateMustUseRecordAlias() throws Exception {
		String mapper = mapper("SmtMsgRecordMapper.xml");

		Assert.assertFalse("read_state belongs to smt_msg_record alias mr, not smt_msg_template alias mt",
				mapper.contains("AND mt.read_state=#{readState}"));
		Assert.assertTrue("listRecordByPage should filter read_state on smt_msg_record alias mr",
				mapper.contains("AND mr.read_state=#{readState}"));
	}

	@Test
	public void iscDeviceTaskOrderByMustSeparateColumns() throws Exception {
		String mapper = mapper("SmtIscDeviceTaskMapper.xml");

		Assert.assertFalse("ORDER BY columns must be separated by comma",
				mapper.contains("ORDER BY SDT.CREATE_TIME DESC SDT.UPDATE_TIME DESC"));
		Assert.assertTrue("getPerson should keep deterministic ordering by create_time and update_time",
				mapper.contains("ORDER BY SDT.CREATE_TIME DESC, SDT.UPDATE_TIME DESC"));
	}

	@Test
	public void largeTableDateFiltersMustNotWrapIndexedColumns() throws Exception {
		assertNoWrappedDateColumn("SmtSnapVehicleMapper.xml");
		assertNoWrappedDateColumn("SmtSnapPersonMapper.xml");
		assertNoWrappedDateColumn("SmtWageSignMapper.xml");
		assertNoWrappedDateColumn("SmtIscDeviceTaskMapper.xml");
		assertNoWrappedDateColumn("SmtDeviceTaskMapper.xml");
		assertNoWrappedDateColumn("SmtTaskDownRecordMapper.xml");
		assertNoWrappedDateColumn("SmtIscDownRecordMapper.xml");
		assertNoWrappedDateColumn("SmtIscCardTaskMapper.xml");
	}

	@Test
	public void activeStaffDateFiltersMustNotWrapCreateTime() throws Exception {
		String activeSql = removeXmlComments(mapper("SmtStaffMapper.xml")).toUpperCase(Locale.ROOT);

		Assert.assertFalse("SmtStaffMapper.xml active SQL should not wrap create_time with to_char/to_date",
				activeSql.contains("TO_DATE(TO_CHAR("));
		Assert.assertFalse("SmtStaffMapper.xml active SQL should compare STAFF.CREATE_TIME directly",
				activeSql.contains("TO_CHAR(STAFF.CREATE_TIME"));
		Assert.assertFalse("SmtStaffMapper.xml active SQL should compare CREATE_TIME directly",
				activeSql.contains("TO_CHAR(CREATE_TIME"));
	}

	@Test
	public void iscAccessCleanupSummaryMustUseAggregateSql() throws Exception {
		String mapper = removeXmlComments(mapper("SmtIscAccessCleanupMapper.xml")).toUpperCase(Locale.ROOT);
		String summarySql = selectSql(mapper, "GETSUMMARY");

		Assert.assertTrue("summary must aggregate counts in the database instead of loading detail rows",
				summarySql.contains("SUM(CASE"));
		Assert.assertFalse("summary must not sort detail rows before counting",
				summarySql.contains("ORDER BY"));
		Assert.assertFalse("summary must not use top-n detail fetching for counts",
				summarySql.contains("ROWNUM"));
	}

	@Test
	public void iscAccessCleanupStaffJoinMustKeepStaffIdIndexUsable() throws Exception {
		String mapper = removeXmlComments(mapper("SmtIscAccessCleanupMapper.xml")).toUpperCase(Locale.ROOT);

		Assert.assertFalse("staff cleanup join must not wrap STAFF.ID with TO_CHAR because it disables the ID index",
				mapper.contains("TO_CHAR(STAFF.ID) = DR.CARD_NO"));
	}

	private void assertNoWrappedDateColumn(String fileName) throws IOException {
		String normalizedSql = removeXmlComments(mapper(fileName)).toUpperCase(Locale.ROOT);

		Assert.assertFalse(fileName + " should compare date columns directly so normal indexes remain usable",
				normalizedSql.contains("TO_DATE(TO_CHAR("));
	}

	private String removeXmlComments(String mapper) {
		return mapper.replaceAll("(?s)<!--.*?-->", "");
	}

	private String selectSql(String mapper, String selectId) {
		String openTag = "<SELECT ID=\"" + selectId + "\"";
		int start = mapper.indexOf(openTag);
		Assert.assertTrue("Missing mapper select: " + selectId, start >= 0);
		int end = mapper.indexOf("</SELECT>", start);
		Assert.assertTrue("Missing mapper select end: " + selectId, end > start);
		return mapper.substring(start, end);
	}

	private String mapper(String fileName) throws IOException {
		String resourceName = "mapper/" + fileName;
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
		Assert.assertNotNull("Missing mapper resource: " + resourceName, input);
		try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[4096];
			int read;
			while ((read = stream.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return output.toString(StandardCharsets.UTF_8.name());
		}
	}
}
