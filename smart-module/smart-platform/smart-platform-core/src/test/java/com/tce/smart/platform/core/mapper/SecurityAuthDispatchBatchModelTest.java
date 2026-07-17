package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 保密区权限最新下发批次的数据模型回归测试。
 *
 * 仅验证 Task 1 的字段、迁移与 Mapper 支撑；批次领取和任务接管行为由后续任务覆盖。
 */
public class SecurityAuthDispatchBatchModelTest {

	@Test
	public void batchModelKeepsSecurityAuthAndAdmittanceTaskSourcesIsolated() throws Exception {
		Assert.assertNotNull(SmtSecurityAuthApply.class.getDeclaredField("currentDispatchBatchId"));
		Assert.assertNotNull(SmtSecurityTaskDetails.class.getDeclaredField("dispatchBatchId"));

		Assert.assertNotNull(SmtIscDeviceTask.class.getDeclaredField("batchId"));
		Assert.assertNotNull(SmtIscDeviceTask.class.getDeclaredField("sourceType"));
		Assert.assertNotNull(SmtIscDeviceTask.class.getDeclaredField("sourceId"));
		Assert.assertNotNull(SmtIscDeviceTask.class.getDeclaredField("sourceDetailId"));
		Assert.assertNotNull(SmtIscDeviceTask.class.getDeclaredField("intentKey"));

		Assert.assertNotNull(DeviceTaskVO.class.getDeclaredField("batchId"));
		Assert.assertNotNull(DeviceTaskVO.class.getDeclaredField("sourceType"));
		Assert.assertNotNull(DeviceTaskVO.class.getDeclaredField("sourceId"));
		Assert.assertNotNull(DeviceTaskVO.class.getDeclaredField("sourceDetailId"));
		Assert.assertNotNull(DeviceTaskVO.class.getDeclaredField("intentKey"));

		String taskSource = new String(Files.readAllBytes(Paths.get("src/main/java/com/tce/smart/platform/core/entity/SmtIscDeviceTask.java")), StandardCharsets.UTF_8);
		Assert.assertTrue("保密区来源关联不得复用入厂申请 APPLY_ID", taskSource.contains("private Long applyId;")
				&& taskSource.contains("private Long sourceId;"));
	}

	@Test
	public void migrationIsIdempotentAndAddsSecurityDispatchIndexes() throws Exception {
		String migration = new String(Files.readAllBytes(Paths.get("../../database/manual/2026-07-16-security-auth-dispatch-batch.sql")), StandardCharsets.UTF_8)
				.toUpperCase(java.util.Locale.ROOT);

		Assert.assertTrue(migration.contains("COLUMN_NAME = 'BATCH_ID'"));
		Assert.assertTrue(migration.contains("CURRENT_DISPATCH_BATCH_ID"));
		Assert.assertTrue(migration.contains("DISPATCH_BATCH_ID"));
		Assert.assertTrue(migration.contains("SOURCE_TYPE"));
		Assert.assertTrue(migration.contains("SOURCE_ID"));
		Assert.assertTrue(migration.contains("SOURCE_DETAIL_ID"));
		Assert.assertTrue(migration.contains("INTENT_KEY"));
		Assert.assertTrue(migration.contains("IDX_SEC_AUTH_DETAIL_BATCH"));
		Assert.assertTrue(migration.contains("IDX_ISC_TASK_SECURITY_BATCH"));
		Assert.assertTrue(migration.contains("IDX_ISC_TASK_SECURITY_INTENT"));
		Assert.assertTrue("迁移必须使用 Oracle 匿名 PL/SQL 块保证重复执行安全", migration.contains("DECLARE") && migration.contains("USER_TAB_COLUMNS"));
	}

	@Test
	public void rollbackKeepsPreExistingBatchIdColumn() throws Exception {
		String rollback = new String(Files.readAllBytes(Paths.get("../../database/manual/2026-07-16-security-auth-dispatch-batch-rollback.sql")), StandardCharsets.UTF_8)
				.toUpperCase(java.util.Locale.ROOT);

		Assert.assertTrue(rollback.contains("IDX_SEC_AUTH_DETAIL_BATCH"));
		Assert.assertTrue(rollback.contains("CURRENT_DISPATCH_BATCH_ID"));
		Assert.assertTrue(rollback.contains("DISPATCH_BATCH_ID"));
		Assert.assertTrue(rollback.contains("SOURCE_TYPE"));
		Assert.assertTrue(rollback.contains("SOURCE_ID"));
		Assert.assertTrue(rollback.contains("SOURCE_DETAIL_ID"));
		Assert.assertTrue(rollback.contains("INTENT_KEY"));
		Assert.assertFalse("BATCH_ID 是既有迁移字段，Task 1 回滚不得删除", rollback.contains("DROP COLUMN BATCH_ID"));
	}

	@Test
	public void iscTaskMapperExposesSecurityAuthSourceFieldsAndIntentLookup() throws Exception {
		Assert.assertNotNull(SmtIscDeviceTaskMapper.class.getMethod("listSecurityAuthTasksByIntent", Long.class, String.class));

		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		Assert.assertTrue(mapper.contains("SDA.BATCH_ID"));
		Assert.assertTrue(mapper.contains("SDA.SOURCE_TYPE"));
		Assert.assertTrue(mapper.contains("SDA.SOURCE_ID"));
		Assert.assertTrue(mapper.contains("SDA.SOURCE_DETAIL_ID"));
		Assert.assertTrue(mapper.contains("SDA.INTENT_KEY"));
		Assert.assertTrue(mapper.contains("<select id=\"listSecurityAuthTasksByIntent\""));
		Assert.assertTrue(mapper.contains("SDA.SOURCE_TYPE = 'SECURITY_AUTH'"));
	}
}
