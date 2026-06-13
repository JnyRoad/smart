package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class SmtIscCardImportSchemaTest {

	@Test
	public void batchSqlUsesNonReservedImportModeColumnAndFailsFastForUnknownLegacyShape() throws Exception {
		String sql = new String(Files.readAllBytes(
				Paths.get("..", "..", "database", "manual", "20260608_add_smt_isc_card_import.sql")),
				StandardCharsets.UTF_8);

		Assert.assertTrue(sql.contains("IMPORT_MODE VARCHAR2(32) NOT NULL"));
		Assert.assertTrue(sql.contains("RENAME COLUMN \"MODE\" TO IMPORT_MODE"));
		Assert.assertTrue(sql.contains("RAISE_APPLICATION_ERROR(-20003"));
		Assert.assertFalse("Do not create a nullable fallback column that cannot converge to the target schema",
				Pattern.compile("(?is)\\bADD\\s+IMPORT_MODE\\s+VARCHAR2\\s*\\(\\s*32\\s*\\)(?!\\s+NOT\\s+NULL)")
						.matcher(sql).find());
		Assert.assertFalse("SMT_ISC_CARD_IMPORT_BATCH should not define MODE as a physical column",
				Pattern.compile("(?im)^\\s*MODE\\s+").matcher(sql).find());
		Assert.assertFalse("SMT_ISC_CARD_IMPORT_BATCH should not keep MODE as a runtime column",
				Pattern.compile("SMT_ISC_CARD_IMPORT_BATCH\\.MODE\\b").matcher(sql).find());
	}

	@Test
	public void mapperAndEntityMapModePropertyToImportModeColumn() throws Exception {
		String mapper = new String(Files.readAllBytes(
				Paths.get("src", "main", "resources", "mapper", "SmtIscCardImportBatchMapper.xml")),
				StandardCharsets.UTF_8);

		Assert.assertTrue(mapper.contains("<result property=\"mode\" column=\"IMPORT_MODE\"/>"));
		Assert.assertTrue(mapper.contains("AND IMPORT_MODE = #{query.mode}"));
		Assert.assertFalse("Mapper query should not reference Oracle reserved identifier MODE directly",
				Pattern.compile("(?is)\\bCOLUMN\\s*=\\s*\"MODE\"").matcher(mapper.toUpperCase()).find());
		String mapperSql = mapper.replaceAll("(?is)<[^>]+>", " ")
				.replaceAll("#\\{[^}]+}", "?")
				.toUpperCase();
		Assert.assertFalse("Mapper SQL should not reference Oracle reserved identifier MODE directly",
				Pattern.compile("\\b(?:[A-Z]+\\.)?MODE\\b").matcher(mapperSql).find());

		Field modeField = SmtIscCardImportBatch.class.getDeclaredField("mode");
		TableField tableField = modeField.getAnnotation(TableField.class);
		Assert.assertNotNull(tableField);
		Assert.assertEquals("IMPORT_MODE", tableField.value());
	}

	@Test
	public void explicitBatchLookupDoesNotUseOracleReservedModeAlias() throws Exception {
		String mapper = new String(Files.readAllBytes(
				Paths.get("src", "main", "resources", "mapper", "SmtIscCardImportBatchMapper.xml")),
				StandardCharsets.UTF_8);
		int selectStart = mapper.indexOf("<select id=\"getById\"");
		int selectEnd = mapper.indexOf("</select>", selectStart);
		Assert.assertTrue("getById select should be declared in mapper XML", selectStart >= 0 && selectEnd > selectStart);
		String lookupSql = mapper.substring(selectStart, selectEnd);

		Assert.assertTrue(lookupSql.contains("IMPORT_MODE"));
		Assert.assertFalse("Oracle rejects generated aliases named MODE",
				Pattern.compile("(?i)\\bAS\\s+mode\\b").matcher(lookupSql).find());
	}
}
