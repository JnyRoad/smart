package com.tce.smart.platform.core.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SmtIscStaffCardSchemaTest {

	@Test
	public void staffCardSqlMaintainsSyncColumnsAndCardNoConstraint() throws Exception {
		String sql = new String(Files.readAllBytes(
				Paths.get("..", "..", "database", "manual", "20260602_add_smt_isc_staff_card.sql")),
				StandardCharsets.UTF_8);

		Assert.assertTrue(sql.contains("SYNC_STATUS NUMBER(3) DEFAULT 0"));
		Assert.assertTrue(sql.contains("ALTER TABLE SMT_ISC_STAFF_CARD ADD SYNC_STATUS NUMBER(3)"));
		Assert.assertFalse(sql.contains("ALTER TABLE SMT_ISC_STAFF_CARD ADD SYNC_STATUS NUMBER(3) DEFAULT 0"));
		Assert.assertTrue(sql.contains("ALTER TABLE SMT_ISC_STAFF_CARD MODIFY SYNC_STATUS DEFAULT 0"));
		Assert.assertTrue(sql.contains("LAST_TASK_ID NUMBER(20)"));
		Assert.assertTrue(sql.contains("LAST_SYNC_CODE NUMBER(10)"));
		Assert.assertTrue(sql.contains("LAST_SYNC_REMARK VARCHAR2(512)"));
		Assert.assertTrue(sql.contains("LAST_SYNC_TIME DATE"));
		Assert.assertTrue(sql.contains("CK_SMT_ISC_STAFF_CARD_SYNC"));
		Assert.assertTrue(sql.contains("CK_SMT_ISC_STAFF_CARD_NO"));
		Assert.assertTrue(sql.contains("REGEXP_LIKE(CARD_NO, '^[0-9A-Z]{8,20}$')"));
		Assert.assertTrue(sql.contains("CARD_NO NOT LIKE '999%'"));
		Assert.assertTrue(sql.contains("RAISE_APPLICATION_ERROR(-20004"));
		Assert.assertTrue(sql.contains("20260610_cleanup_invalid_isc_staff_cards.sql"));
	}

	@Test
	public void invalidCardCleanupSoftDeletesOnlyActiveInvalidCards() throws Exception {
		String sql = new String(Files.readAllBytes(
				Paths.get("..", "..", "database", "manual", "20260610_cleanup_invalid_isc_staff_cards.sql")),
				StandardCharsets.UTF_8);

		Assert.assertTrue(sql.contains("UPDATE SMT_ISC_STAFF_CARD"));
		Assert.assertTrue(sql.contains("DEL_FLAG = 1"));
		Assert.assertTrue(sql.contains("ACTIVE_KEY = NULL"));
		Assert.assertTrue(sql.contains("SYNC_STATUS = 2"));
		Assert.assertTrue(sql.contains("LAST_SYNC_CODE = 506"));
		Assert.assertTrue(sql.contains("999开头为ISC虚拟卡号，不允许维护"));
		Assert.assertTrue(sql.contains("WHERE DEL_FLAG = 0"));
		Assert.assertTrue(sql.contains("NOT REGEXP_LIKE(CARD_NO, '^[0-9A-Z]{8,20}$')"));
		Assert.assertTrue(sql.contains("CARD_NO LIKE '999%'"));
	}
}
