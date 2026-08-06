package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 能耗汇总投影的离线数据库契约测试，不依赖真实 Oracle 实例。
 */
public class SmtEnergyProjectionSchemaTest {

	@Test
	public void projectionSqlDefinesReplaySafeTablesKeysAndIndexes() throws Exception {
		String sql = readForwardSql();

		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_INGESTION_LEDGER");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_PARK_DAY_LOCK");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_METER_DAY_LOCK");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_PROJECTION_QUEUE");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_METER_SCOPE_RULE");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_METER_DAY_FACT");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_PARK_DAY_ITEM");
		assertForwardCreatesAndProtectsTable(sql, "SMT_ENERGY_PARK_DAY");

		assertContains(sql, "UK_SMT_ENERGY_LEDGER_EVENT UNIQUE (SOURCE_EVENT_ID)");
		assertContains(sql, "UK_SMT_ENERGY_QUEUE_METER_DAY UNIQUE (METER_SOURCE, METER_ID, STAT_DATE)");
		assertContains(sql, "UK_SMT_ENERGY_FACT_METER_DAY UNIQUE (METER_SOURCE, METER_ID, STAT_DATE)");
		assertContains(sql, "UK_SMT_ENERGY_PARK_DAY UNIQUE (PARK_ID, STAT_DATE, RESOURCE_TYPE, UNIT)");
		assertContains(sql, "PK_SMT_ENERGY_PD_LOCK PRIMARY KEY (PARK_ID, STAT_DATE, RESOURCE_TYPE, UNIT)");
		assertContains(sql, "PK_SMT_ENERGY_MD_LOCK PRIMARY KEY (METER_SOURCE, METER_ID, STAT_DATE)");
		assertContains(sql, "RETRY_COUNT NUMBER(10) DEFAULT 0 NOT NULL");
		assertContains(sql, "NEXT_RETRY_AT TIMESTAMP(6)");
		assertContains(sql, "CREATE INDEX IDX_SMT_ENERGY_LEDGER_TIME");
		assertContains(sql, "CREATE INDEX IDX_SMT_ENERGY_QUEUE_STATUS");
		assertContains(sql, "CREATE INDEX IDX_SMT_ENERGY_RULE_LOOKUP");
		assertContains(sql, "CREATE INDEX IDX_SMT_ENERGY_FACT_PARK_DAY");
		assertContains(sql, "CREATE INDEX IDX_SMT_ENERGY_ITEM_PARK_DAY");
	}

	@Test
	public void releaseMarkerMakesRollbackSafeAndOracleObjectNamesPortable() throws Exception {
		String forwardSql = readForwardSql();
		String rollbackSql = new String(Files.readAllBytes(Paths.get("..", "..", "database", "manual",
				"20260805_rollback_energy_projection.sql")), StandardCharsets.UTF_8);

		assertContains(forwardSql, "CREATE TABLE SMT_ENERGY_RELEASE_MARKER");
		assertContains(forwardSql, "RELEASE_KEY VARCHAR2(64) NOT NULL");
		assertContains(forwardSql, "'20260805_ENERGY_PROJECTION'");
		assertContains(forwardSql, "MERGE INTO SMT_ENERGY_RELEASE_MARKER");
		assertContains(rollbackSql, "FROM SMT_ENERGY_RELEASE_MARKER");
		assertContains(rollbackSql, "ROLLED_BACK_AT IS NULL");
		assertContains(rollbackSql, "v_marker_count > 0");
		Assert.assertFalse("回滚默认不能使用 PURGE，避免绕开可恢复窗口", rollbackSql.contains("PURGE"));

		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_METER_DAY_LOCK CASCADE", "DROP TABLE SMT_ENERGY_PARK_DAY_LOCK CASCADE");
		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_PARK_DAY_LOCK CASCADE", "DROP TABLE SMT_ENERGY_PARK_DAY CASCADE");
		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_PARK_DAY", "DROP TABLE SMT_ENERGY_PARK_DAY_ITEM");
		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_PARK_DAY_ITEM", "DROP TABLE SMT_ENERGY_METER_DAY_FACT");
		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_METER_DAY_FACT", "DROP TABLE SMT_ENERGY_METER_SCOPE_RULE");
		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_METER_SCOPE_RULE", "DROP TABLE SMT_ENERGY_PROJECTION_QUEUE");
		assertAppearsBefore(rollbackSql, "DROP TABLE SMT_ENERGY_PROJECTION_QUEUE", "DROP TABLE SMT_ENERGY_INGESTION_LEDGER");
		Assert.assertFalse("发行标识表是审计记录，回滚不得删除它", rollbackSql.contains("DROP TABLE SMT_ENERGY_RELEASE_MARKER"));
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_METER_DAY_LOCK");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_PARK_DAY_LOCK");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_PARK_DAY");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_PARK_DAY_ITEM");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_METER_DAY_FACT");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_METER_SCOPE_RULE");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_PROJECTION_QUEUE");
		assertRollbackBranchUsesActiveMarkerBeforeDrop(rollbackSql, "SMT_ENERGY_INGESTION_LEDGER");

		Matcher matcher = Pattern.compile("(?m)\\b(?:CONSTRAINT|INDEX|TABLE)\\s+([A-Z][A-Z0-9_$#]*)").matcher(forwardSql);
		while (matcher.find()) {
			Assert.assertTrue("Oracle 对象名不能超过 30 个字符：" + matcher.group(1), matcher.group(1).length() <= 30);
		}
	}

	@Test
	public void meterDayFactAndRuleSqlRetainAuditAndEffectivePeriodFields() throws Exception {
		String sql = readForwardSql();

		assertContains(sql, "DAY_START_HISTORY_ID");
		assertContains(sql, "DAY_START_TIME");
		assertContains(sql, "DAY_START_READING");
		assertContains(sql, "DAY_END_HISTORY_ID");
		assertContains(sql, "DAY_END_TIME");
		assertContains(sql, "DAY_END_READING");
		assertContains(sql, "MULTIPLIER_SNAPSHOT");
		assertContains(sql, "RAW_DELTA");
		assertContains(sql, "USAGE_VALUE");
		assertContains(sql, "QUALITY_CODE");
		assertContains(sql, "QUALITY_DETAIL");
		assertContains(sql, "SOURCE_HASH");
		assertContains(sql, "CALCULATED_AT");
		assertContains(sql, "EFFECTIVE_START_DATE");
		assertContains(sql, "EFFECTIVE_END_DATE");
		assertContains(sql, "INCLUDE_FLAG");
		assertContains(sql, "METER_GROUP_ID");
		assertContains(sql, "PARENT_METER_ID");
		assertContains(sql, "RULE_VERSION");
		assertContains(sql, "RULE_DECISION");
		assertContains(sql, "RULE_REASON");
		assertContains(sql, "LEASE_TOKEN VARCHAR2(64)");
		assertContains(sql, "USER_TAB_COLUMNS");

		String rollbackSql = new String(Files.readAllBytes(Paths.get("..", "..", "database", "manual",
				"20260805_rollback_energy_projection.sql")), StandardCharsets.UTF_8);
		assertContains(rollbackSql, "DROP TABLE SMT_ENERGY_PARK_DAY");
		assertContains(rollbackSql, "DROP TABLE SMT_ENERGY_INGESTION_LEDGER");
	}

	@Test
	public void energyProjectionModelsAndMappersMatchTheTableContracts() throws Exception {
		assertModel("SmtEnergyIngestionLedger", "SMT_ENERGY_INGESTION_LEDGER", "sourceEventId");
		assertModel("SmtEnergyProjectionQueue", "SMT_ENERGY_PROJECTION_QUEUE", "meterSource", "meterId", "statDate", "leaseToken");
		assertModel("SmtEnergyMeterScopeRule", "SMT_ENERGY_METER_SCOPE_RULE", "effectiveStartDate", "effectiveEndDate", "includeFlag", "ruleVersion");
		assertModel("SmtEnergyMeterDayFact", "SMT_ENERGY_METER_DAY_FACT", "meterSource", "meterId", "statDate",
				"dayStartHistoryId", "dayEndHistoryId", "multiplierSnapshot", "rawDelta", "usageValue", "calculatedAt");
		assertModel("SmtEnergyParkDayItem", "SMT_ENERGY_PARK_DAY_ITEM", "ruleId", "ruleVersion", "ruleDecision", "ruleReason");
		assertModel("SmtEnergyParkDay", "SMT_ENERGY_PARK_DAY", "parkId", "statDate", "resourceType", "unit");

		assertMapper("SmtEnergyIngestionLedgerMapper");
		assertLockMapper();
		Assert.assertNotNull("表计日锁 Mapper 必须存在", Class.forName("com.tce.smart.platform.core.mapper.energy.SmtEnergyMeterDayLockMapper"));
		assertMapper("SmtEnergyProjectionQueueMapper");
		assertMapper("SmtEnergyMeterScopeRuleMapper");
		assertMapper("SmtEnergyMeterDayFactMapper");
		assertMapper("SmtEnergyParkDayItemMapper");
		assertMapper("SmtEnergyParkDayMapper");
	}

	private String readForwardSql() throws Exception {
		return new String(Files.readAllBytes(Paths.get("..", "..", "database", "manual",
				"20260805_add_energy_projection.sql")), StandardCharsets.UTF_8);
	}

	private void assertModel(String simpleClassName, String expectedTableName, String... requiredFields) throws Exception {
		Class<?> modelClass = Class.forName("com.tce.smart.platform.core.entity.energy." + simpleClassName);
		TableName tableName = modelClass.getAnnotation(TableName.class);
		Assert.assertNotNull("实体必须声明表名：" + simpleClassName, tableName);
		Assert.assertEquals(expectedTableName, tableName.value());
		for (String fieldName : requiredFields) {
			Field field = modelClass.getDeclaredField(fieldName);
			Assert.assertNotNull("实体字段不应为空：" + fieldName, field);
		}
	}

	private void assertMapper(String simpleClassName) throws Exception {
		Class<?> mapperClass = Class.forName("com.tce.smart.platform.core.mapper.energy." + simpleClassName);
		Assert.assertTrue("Mapper 必须继承 MyBatis-Plus BaseMapper：" + simpleClassName,
				BaseMapper.class.isAssignableFrom(mapperClass));
	}

	private void assertLockMapper() throws Exception {
		Class<?> mapperClass = Class.forName("com.tce.smart.platform.core.mapper.energy.SmtEnergyParkDayLockMapper");
		Assert.assertNotNull("园区日锁 Mapper 必须存在", mapperClass);
	}

	private void assertContains(String content, String expected) {
		Assert.assertTrue("缺少数据库契约：" + expected, content.contains(expected));
	}

	private void assertForwardCreatesAndProtectsTable(String sql, String tableName) {
		assertContains(sql, "CREATE TABLE " + tableName);
		assertContains(sql, "FROM USER_TABLES WHERE TABLE_NAME = '" + tableName + "'");
		int tableGuardIndex = sql.indexOf("FROM USER_TABLES WHERE TABLE_NAME = '" + tableName + "'");
		int createBranchIndex = sql.indexOf("IF v_count = 0 THEN", tableGuardIndex);
		int createTableIndex = sql.indexOf("CREATE TABLE " + tableName, tableGuardIndex);
		int markerMergeIndex = sql.indexOf("MERGE INTO SMT_ENERGY_RELEASE_MARKER", createTableIndex);
		int blockEndIndex = sql.indexOf("\nEND;\n/", tableGuardIndex);
		Assert.assertTrue("建表分支必须存在：" + tableName, createBranchIndex >= 0 && blockEndIndex > createBranchIndex);
		Assert.assertTrue("marker 只能在该表实际建表后写入：" + tableName,
				createBranchIndex < createTableIndex && createTableIndex < markerMergeIndex && markerMergeIndex < blockEndIndex);
	}

	private void assertAppearsBefore(String content, String earlier, String later) {
		int earlierIndex = content.indexOf(earlier);
		int laterIndex = content.indexOf(later);
		Assert.assertTrue("回滚顺序缺少对象：" + earlier, earlierIndex >= 0);
		Assert.assertTrue("回滚顺序缺少对象：" + later, laterIndex >= 0);
		Assert.assertTrue("回滚应先处理 " + earlier + " 再处理 " + later, earlierIndex < laterIndex);
	}

	private void assertRollbackBranchUsesActiveMarkerBeforeDrop(String rollbackSql, String tableName) {
		String markerBinding = "'20260805_ENERGY_PROJECTION', '" + tableName + "', 'TABLE'";
		int markerBindingIndex = rollbackSql.indexOf(markerBinding);
		int branchStartIndex = rollbackSql.lastIndexOf("DECLARE", markerBindingIndex);
		int branchEndIndex = rollbackSql.indexOf("\nEND;\n/", markerBindingIndex);
		Assert.assertTrue("回滚分支必须绑定对应 marker：" + tableName,
				markerBindingIndex >= 0 && branchStartIndex >= 0 && branchEndIndex > markerBindingIndex);

		String branch = rollbackSql.substring(branchStartIndex, branchEndIndex);
		int activeMarkerQueryIndex = branch.indexOf("FROM SMT_ENERGY_RELEASE_MARKER");
		int activeMarkerConditionIndex = branch.indexOf("ROLLED_BACK_AT IS NULL");
		int markerGateIndex = branch.indexOf("IF v_marker_count > 0 THEN");
		int dropIndex = branch.indexOf("DROP TABLE " + tableName);
		Assert.assertTrue("回滚必须查询活动 marker：" + tableName,
				activeMarkerQueryIndex >= 0 && activeMarkerConditionIndex > activeMarkerQueryIndex);
		Assert.assertTrue("回滚必须在活动 marker gate 之后才删除表：" + tableName,
				markerGateIndex > activeMarkerConditionIndex && dropIndex > markerGateIndex);
	}
}
