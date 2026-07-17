package com.tce.smart.platform.core.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/** 保密区 worker 候选必须在稳定限额前排除旧批次，避免旧 WAIT 占满前一百行。 */
public class SmtSecurityTaskDetailsMapperXmlTest {

	@Test
	public void pendingWorkerFiltersCurrentBatchBeforeOrderingAndHundredRowLimit() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get(
				"src/main/resources/mapper/SmtSecurityTaskDetailsMapper.xml")), StandardCharsets.UTF_8);
		int selectStart = mapper.indexOf("<select id=\"listPendingCurrentDispatchDetails\"");
		Assert.assertTrue("必须由专用 SQL 直接筛申请单当前批次", selectStart >= 0);
		int selectEnd = mapper.indexOf("</select>", selectStart);
		String sql = mapper.substring(selectStart, selectEnd);
		Assert.assertTrue(sql.contains("SMT_SECURITY_AUTH_APPLY"));
		Assert.assertTrue(sql.contains("CURRENT_DISPATCH_BATCH_ID = STD.DISPATCH_BATCH_ID"));
		Assert.assertTrue(sql.contains("STD.STATUS = #{waitStatus}"));
		int orderIndex = sql.indexOf("ORDER BY STD.ID ASC");
		int limitIndex = sql.indexOf("ROWNUM <![CDATA[<=]]> #{limit}");
		Assert.assertTrue("必须先按明细 ID 稳定排序，再从当前批次候选中限额", orderIndex >= 0 && limitIndex > orderIndex);
	}
}
