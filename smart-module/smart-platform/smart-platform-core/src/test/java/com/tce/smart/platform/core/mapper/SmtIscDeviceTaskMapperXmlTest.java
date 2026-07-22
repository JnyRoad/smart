package com.tce.smart.platform.core.mapper;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SmtIscDeviceTaskMapperXmlTest {

	@Test
	public void getCardDownWaitsUntilTaskStartTime() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int getCardDownStart = mapper.indexOf("<select id=\"getCardDown\"");
		int getCardDownEnd = mapper.indexOf("</select>", getCardDownStart);
		String getCardDownSql = mapper.substring(getCardDownStart, getCardDownEnd);

		Assert.assertTrue(getCardDownSql.contains("SDA.START_TIME"));
		Assert.assertTrue(getCardDownSql.contains("SDA.START_TIME IS NULL OR SDA.START_TIME <![CDATA[<]]> #{overTime}"));
		Assert.assertTrue(getCardDownSql.contains("CASE WHEN SDA.START_TIME IS NULL THEN 0 ELSE 1 END ASC"));
		Assert.assertTrue(getCardDownSql.contains("SDA.START_TIME ASC, SDA.TIMES ASC"));
	}

	@Test
	public void getCardDownSkipsTasksAfterMaxRetryTimes() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int getCardDownStart = mapper.indexOf("<select id=\"getCardDown\"");
		int getCardDownEnd = mapper.indexOf("</select>", getCardDownStart);
		String getCardDownSql = mapper.substring(getCardDownStart, getCardDownEnd);

		Assert.assertTrue(getCardDownSql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
	}

	@Test
	public void pendingDownloadQueriesSkipTasksAfterMaxRetryTimes() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		String[] selectIds = {
				"getDeleteTasks",
				"getValidDownloadTasks",
				"getPersonDown",
				"getDelayDown",
				"getReTryCardDown"
		};

		for (String selectId : selectIds) {
			int queryStart = mapper.indexOf("<select id=\"" + selectId + "\"");
			Assert.assertTrue("Missing select: " + selectId, queryStart >= 0);
			int queryEnd = mapper.indexOf("</select>", queryStart);
			String querySql = mapper.substring(queryStart, queryEnd);
			Assert.assertTrue(selectId, querySql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
		}
	}

	@Test
	public void deleteQueriesSkipTasksAfterMaxRetryTimes() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int getDelStart = mapper.indexOf("<select id=\"getDel\"");
		int getDelEnd = mapper.indexOf("</select>", getDelStart);
		String getDelSql = mapper.substring(getDelStart, getDelEnd);
		int getDelayDelStart = mapper.indexOf("<select id=\"getDelayDel\"");
		int getDelayDelEnd = mapper.indexOf("</select>", getDelayDelStart);
		String getDelayDelSql = mapper.substring(getDelayDelStart, getDelayDelEnd);

		Assert.assertTrue(getDelSql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
		Assert.assertTrue(getDelayDelSql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
	}

	@Test
	public void recentOnlineDeviceTasksSkipTasksAfterMaxRetryTimes() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int queryStart = mapper.indexOf("<select id=\"getRecentOnlineDeviceTasks\"");
		int queryEnd = mapper.indexOf("</select>", queryStart);
		String querySql = mapper.substring(queryStart, queryEnd);

		Assert.assertTrue(querySql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
	}

	@Test
	public void staleOfflineQueriesSkipTasksAfterMaxRetryTimes() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int getStaleStart = mapper.indexOf("<select id=\"getStaleOfflineDownloadTasks\"");
		int getStaleEnd = mapper.indexOf("</select>", getStaleStart);
		String getStaleSql = mapper.substring(getStaleStart, getStaleEnd);
		int cancelStaleStart = mapper.indexOf("<update id=\"cancelStaleOfflineDownloadTask\"");
		int cancelStaleEnd = mapper.indexOf("</update>", cancelStaleStart);
		String cancelStaleSql = mapper.substring(cancelStaleStart, cancelStaleEnd);

		Assert.assertTrue(getStaleSql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
		Assert.assertTrue(cancelStaleSql.contains("SDA.TIMES IS NULL OR SDA.TIMES <![CDATA[<]]> 10"));
	}

	@Test
	public void dispatchQueriesIncludeOfflineMarkedTasks() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		String[] selectIds = {"getCardDown", "getDelayDown", "getDel", "getDelayDel"};
		for (String selectId : selectIds) {
			int queryStart = mapper.indexOf("<select id=\"" + selectId + "\"");
			Assert.assertTrue("Missing select: " + selectId, queryStart >= 0);
			int queryEnd = mapper.indexOf("</select>", queryStart);
			String querySql = mapper.substring(queryStart, queryEnd);
			Assert.assertTrue(selectId + " should pick offline-marked tasks so they resume when device is back online",
					querySql.contains("SDA.STATUS IN (0, 6)"));
		}
	}

	@Test
	public void markOfflineDeviceTasksMarksPendingTasksOnOfflineDevices() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		// WHERE谓词已抽为共享片段：先断言片段本身覆盖离线标记条件
		int conditionStart = mapper.indexOf("<sql id=\"mark_offline_device_tasks_condition\"");
		Assert.assertTrue(conditionStart >= 0);
		int conditionEnd = mapper.indexOf("</sql>", conditionStart);
		String conditionSql = mapper.substring(conditionStart, conditionEnd);
		Assert.assertTrue(conditionSql.contains("SDA.STATUS = 0 OR SDA.STATUS IS NULL"));
		Assert.assertTrue(conditionSql.contains("CONNECT_STATUS != 2"));
		// UPDATE语句必须引用共享片段（保证与SELECT APPLY_ID同条件，防止两处谓词漂移）
		int updateStart = mapper.indexOf("<update id=\"markOfflineDeviceTasks\"");
		Assert.assertTrue(updateStart >= 0);
		int updateEnd = mapper.indexOf("</update>", updateStart);
		String updateSql = mapper.substring(updateStart, updateEnd);
		Assert.assertTrue(updateSql.contains("SDA.STATUS = 6"));
		Assert.assertTrue(updateSql.contains("<include refid=\"mark_offline_device_tasks_condition\""));
	}

	@Test
	public void offlineDeviceTaskApplyIdsQueryReusesMarkOfflineConditionAndFiltersNullApplyId() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int selectStart = mapper.indexOf("<select id=\"getOfflineDeviceTaskApplyIds\"");
		Assert.assertTrue(selectStart >= 0);
		int selectEnd = mapper.indexOf("</select>", selectStart);
		String selectSql = mapper.substring(selectStart, selectEnd);
		// SELECT必须与UPDATE复用同一共享片段（同条件），并追加APPLY_ID非空过滤+去重
		Assert.assertTrue(selectSql.contains("<include refid=\"mark_offline_device_tasks_condition\""));
		Assert.assertTrue(selectSql.contains("SDA.APPLY_ID IS NOT NULL"));
		Assert.assertTrue(selectSql.contains("SELECT DISTINCT SDA.APPLY_ID"));
	}

	@Test
	public void getCardDownPicksRequeuedTasksWithNullCode() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int queryStart = mapper.indexOf("<select id=\"getCardDown\"");
		int queryEnd = mapper.indexOf("</select>", queryStart);
		String querySql = mapper.substring(queryStart, queryEnd);
		// 孤儿回收会把任务重置为 INIT + code=null，getCardDown必须能选中；502/506交由退避重试查询处理。
		Assert.assertTrue(querySql.contains("SDA.CODE NOT IN (200, 502, 506) OR SDA.CODE IS NULL"));
	}

	@Test
	public void retryCardDownUsesRetryableCodesAndExponentialBackoff() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int queryStart = mapper.indexOf("<select id=\"getReTryCardDown\"");
		int queryEnd = mapper.indexOf("</select>", queryStart);
		String querySql = mapper.substring(queryStart, queryEnd);

		Assert.assertTrue(querySql.contains("SDA.STATUS IN (0, 6)"));
		Assert.assertTrue(querySql.contains("SDA.CODE in (502,506)"));
		Assert.assertTrue(querySql.contains("SDA.UPDATE_TIME"));
		Assert.assertTrue(querySql.contains("NUMTODSINTERVAL"));
		Assert.assertTrue(querySql.contains("POWER(2, NVL(SDA.TIMES, 1) - 1) * 5"));
	}

	@Test
	public void staleOfflineCancellationCoversOfflineMarkedStatus() throws Exception {
		String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mapper/SmtIscDeviceTaskMapper.xml")), StandardCharsets.UTF_8);
		int selectStart = mapper.indexOf("<select id=\"getStaleOfflineDownloadTasks\"");
		int selectEnd = mapper.indexOf("</select>", selectStart);
		Assert.assertTrue(mapper.substring(selectStart, selectEnd).contains("SDA.STATUS IN (0, 3, 6)"));
		int updateStart = mapper.indexOf("<update id=\"cancelStaleOfflineDownloadTask\"");
		int updateEnd = mapper.indexOf("</update>", updateStart);
		Assert.assertTrue(mapper.substring(updateStart, updateEnd).contains("SDA.STATUS IN (0, 3, 6)"));
	}
}
