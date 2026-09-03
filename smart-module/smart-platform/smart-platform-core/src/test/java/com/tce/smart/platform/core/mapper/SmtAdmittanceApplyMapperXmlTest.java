package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.dto.SearchSmtVisitorDTO;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SmtAdmittanceApplyMapperXmlTest {

	/**
	 * 验证随行人员姓名只作为主申请单的筛选条件，不能通过一对多连接扩展列表结果行。
	 */
	@Test
	public void getSmtVisitorPageFindsApplyByFellowNameWithoutJoinMultiplication() throws Exception {
		SearchSmtVisitorDTO query = new SearchSmtVisitorDTO();
		query.setVisitorName("随行人员甲");

		BoundSql boundSql = visitorPageBoundSql(query);
		String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue("姓名筛选应保留主申请人姓名匹配",
				sql.contains("V.VISITOR_NAME LIKE CONCAT(CONCAT('%',?),'%')"));
		Assert.assertTrue("姓名筛选应关联匹配任一随行人员姓名",
				sql.contains("OR EXISTS ( SELECT 1 FROM SMT_ADMITTANCE_FELLOW SF"));
		Assert.assertTrue("随行人员姓名子查询应关联当前申请单",
				sql.contains("SF.VISITOR_ID = V.ID AND SF.FELLOW_NAME LIKE CONCAT(CONCAT('%',?),'%')"));
		Assert.assertFalse("随行人员不能再以主查询外连接参与列表，以免一对多放大结果行",
				sql.contains("JOIN SMT_ADMITTANCE_FELLOW"));
		Assert.assertEquals(Arrays.asList("query.visitorName", "query.visitorName"),
				boundSql.getParameterMappings().stream()
						.map(parameterMapping -> parameterMapping.getProperty())
						.collect(Collectors.toList()));
	}

	/**
	 * 验证证件号同时匹配主访客和随行人员，并且随行人员分支不依赖已移除的外连接别名。
	 */
	@Test
	public void getSmtVisitorPageFindsApplyByMainOrFellowCertNoWithoutJoinMultiplication() throws Exception {
		SearchSmtVisitorDTO query = new SearchSmtVisitorDTO();
		query.setCertNo("440100200001010001");

		BoundSql boundSql = visitorPageBoundSql(query);
		String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue("证件号筛选应保留主访客证件号匹配",
				sql.contains("V.CERT_NO LIKE CONCAT(CONCAT('%',?),'%')"));
		Assert.assertTrue("证件号筛选应关联匹配任一随行人员证件号",
				sql.contains("EXISTS ( SELECT 1 FROM SMT_ADMITTANCE_FELLOW SF"));
		Assert.assertTrue("随行人员证件号子查询应关联当前申请单",
				sql.contains("SF.VISITOR_ID = V.ID AND SF.CERT_NO LIKE CONCAT(CONCAT('%',?),'%')"));
		Assert.assertFalse("证件号筛选不能依赖已删除的随行人员外连接别名", sql.contains("SAF.CERT_NO"));
		Assert.assertFalse("证件号筛选不能让随行表直接参与主查询",
				sql.contains("JOIN SMT_ADMITTANCE_FELLOW"));
		Assert.assertEquals(Arrays.asList("query.certNo", "query.certNo"),
				boundSql.getParameterMappings().stream()
						.map(parameterMapping -> parameterMapping.getProperty())
						.collect(Collectors.toList()));
	}

	/**
	 * 验证同证件号的任一随行人员均参与判重，区域以逗号边界精确匹配，且时间使用开区间。
	 */
	@Test
	public void countActiveFellowOverlapByCertNoUsesExactAreaAndOpenInterval() throws Exception {
		Configuration configuration = new Configuration();
		String resource = "mapper/SmtAdmittanceApplyMapper.xml";
		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
					resource, configuration.getSqlFragments());
			mapperParser.parse();
		}
		MappedStatement statement = configuration.getMappedStatement(
				SmtAdmittanceApplyMapper.class.getName() + ".countActiveFellowOverlapByCertNo");
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 8, 18, 28, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 8, 15, 20, 0);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("certNo", "411281199606254513");
		parameters.put("startTime", startTime);
		parameters.put("endTime", endTime);
		parameters.put("areaTypes", Arrays.asList(1, 11));

		BoundSql boundSql = statement.getBoundSql(parameters);
		String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue(sql.contains("INNER JOIN SMT_ADMITTANCE_FELLOW F ON F.VISITOR_ID = A.ID"));
		Assert.assertTrue(sql.contains("F.CERT_NO = ?"));
		Assert.assertFalse("随行人员身份不应影响判重范围", sql.contains("F.IS_MAIN = 1"));
		Assert.assertTrue(sql.contains("A.STATUS NOT IN (1, 7)"));
		Assert.assertTrue(sql.contains("A.START_TIME < ?"));
		Assert.assertFalse(sql.contains("A.START_TIME <= ?"));
		Assert.assertTrue(sql.contains("A.END_TIME > ?"));
		Assert.assertFalse(sql.contains("A.END_TIME >= ?"));
		Assert.assertTrue("区域 1 与 11 必须按完整逗号分隔值匹配",
				sql.contains("INSTR(',' || A.AREA_TYPE || ',', ',' || TO_CHAR(?) || ',') > 0"));
		Assert.assertEquals("两个申请区域应分别绑定，避免把 1 误匹配为 11", 5,
				boundSql.getParameterMappings().size());
		Assert.assertEquals(startTime, parameters.get("startTime"));
		Assert.assertEquals(endTime, parameters.get("endTime"));
	}

	/**
	 * 验证历史申请未保存区域或区域格式异常时，以全部区域冲突的保守策略参与判重。
	 */
	@Test
	public void countActiveFellowOverlapByCertNoTreatsUnknownHistoricalAreaAsConflict() throws Exception {
		Configuration configuration = new Configuration();
		String resource = "mapper/SmtAdmittanceApplyMapper.xml";
		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
					resource, configuration.getSqlFragments());
			mapperParser.parse();
		}
		MappedStatement statement = configuration.getMappedStatement(
				SmtAdmittanceApplyMapper.class.getName() + ".countActiveFellowOverlapByCertNo");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("certNo", "411281199606254513");
		parameters.put("startTime", LocalDateTime.of(2026, 6, 8, 18, 28, 0));
		parameters.put("endTime", LocalDateTime.of(2026, 10, 8, 15, 20, 0));
		parameters.put("areaTypes", Collections.singletonList(1));

		String sql = statement.getBoundSql(parameters).getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue("未保存区域的历史申请必须参与判重", sql.contains("A.AREA_TYPE IS NULL"));
		Assert.assertTrue("区域格式异常的历史申请必须参与判重",
				sql.contains("NOT REGEXP_LIKE(A.AREA_TYPE, '^[0-9]+(,[0-9]+)*$')"));
	}

	/**
	 * 验证证件哈希锁按唯一键加行锁，确保同一证件的并发保存只能串行通过判重区。
	 */
	@Test
	public void admissionCertificateLockUsesHashAndOracleRowLock() throws Exception {
		Configuration configuration = new Configuration();
		String resource = "mapper/SmtAdmittanceApplyMapper.xml";
		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
					resource, configuration.getSqlFragments());
			mapperParser.parse();
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("certNoHash", "1a5e48a3a319f4e7b6ad0d1524436d34dcf4d50c8f86999d67644c0570a5dbaa");

		MappedStatement lockStatement = configuration.getMappedStatement(
				SmtAdmittanceApplyMapper.class.getName() + ".lockAdmittanceCertByHash");
		MappedStatement insertStatement = configuration.getMappedStatement(
				SmtAdmittanceApplyMapper.class.getName() + ".insertAdmittanceCertLock");
		String lockSql = lockStatement.getBoundSql(parameters).getSql().replaceAll("\\s+", " ").trim();
		String insertSql = insertStatement.getBoundSql(parameters).getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue(lockSql.contains("FROM SMT_ADMITTANCE_CERT_LOCK"));
		Assert.assertTrue(lockSql.contains("WHERE CERT_NO_HASH = ?"));
		Assert.assertTrue(lockSql.endsWith("FOR UPDATE WAIT 10"));
		Assert.assertTrue(insertSql.contains("INSERT INTO SMT_ADMITTANCE_CERT_LOCK"));
		Assert.assertTrue(insertSql.contains("CERT_NO_HASH, CREATE_TIME"));
		Assert.assertTrue(insertSql.contains("VALUES (?, SYSTIMESTAMP)"));
	}

	/**
	 * 使用 MyBatis 实际解析后的绑定 SQL，避免测试仅依赖 XML 文本格式。
	 */
	private BoundSql visitorPageBoundSql(SearchSmtVisitorDTO query) throws Exception {
		Configuration configuration = new Configuration();
		String resource = "mapper/SmtAdmittanceApplyMapper.xml";
		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
					resource, configuration.getSqlFragments());
			mapperParser.parse();
		}
		MappedStatement statement = configuration.getMappedStatement(
				SmtAdmittanceApplyMapper.class.getName() + ".getSmtVisitorPage");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("query", query);
		parameters.put("park", Collections.emptyList());
		return statement.getBoundSql(parameters);
	}
}
