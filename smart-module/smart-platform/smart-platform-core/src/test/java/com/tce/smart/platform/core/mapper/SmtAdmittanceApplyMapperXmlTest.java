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
				sql.contains("LEFT JOIN SMT_ADMITTANCE_FELLOW"));
		Assert.assertEquals(Arrays.asList("query.visitorName", "query.visitorName"),
				boundSql.getParameterMappings().stream()
						.map(parameterMapping -> parameterMapping.getProperty())
						.collect(Collectors.toList()));
	}

	/**
	 * 验证随行人员证件号通过相关子查询筛选主申请单，不依赖已移除的随行人员外连接别名。
	 */
	@Test
	public void getSmtVisitorPageFindsApplyByFellowCertNoWithoutJoinMultiplication() throws Exception {
		SearchSmtVisitorDTO query = new SearchSmtVisitorDTO();
		query.setCertNo("440100200001010001");

		BoundSql boundSql = visitorPageBoundSql(query);
		String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue("证件号筛选应关联匹配任一随行人员证件号",
				sql.contains("EXISTS ( SELECT 1 FROM SMT_ADMITTANCE_FELLOW SF"));
		Assert.assertTrue("随行人员证件号子查询应关联当前申请单",
				sql.contains("SF.VISITOR_ID = V.ID AND SF.CERT_NO LIKE CONCAT(CONCAT('%',?),'%')"));
		Assert.assertFalse("证件号筛选不能依赖已删除的随行人员外连接别名", sql.contains("SAF.CERT_NO"));
		Assert.assertEquals(Collections.singletonList("query.certNo"),
				boundSql.getParameterMappings().stream()
						.map(parameterMapping -> parameterMapping.getProperty())
						.collect(Collectors.toList()));
	}

	/**
	 * 验证主申请人与主随行人员的证件号重叠校验使用证件号和开区间时间条件。
	 */
	@Test
	public void countActiveMainFellowOverlapByCertNoUsesCertNoAndOpenInterval() throws Exception {
		Configuration configuration = new Configuration();
		String resource = "mapper/SmtAdmittanceApplyMapper.xml";
		try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
			XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
					resource, configuration.getSqlFragments());
			mapperParser.parse();
		}
		MappedStatement statement = configuration.getMappedStatement(
				SmtAdmittanceApplyMapper.class.getName() + ".countActiveMainFellowOverlapByCertNo");
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 8, 18, 28, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 8, 15, 20, 0);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("certNo", "411281199606254513");
		parameters.put("startTime", startTime);
		parameters.put("endTime", endTime);

		BoundSql boundSql = statement.getBoundSql(parameters);
		String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

		Assert.assertTrue(sql.contains("INNER JOIN SMT_ADMITTANCE_FELLOW F ON F.VISITOR_ID = A.ID"));
		Assert.assertTrue(sql.contains("F.CERT_NO = ?"));
		Assert.assertTrue(sql.contains("F.IS_MAIN = 1"));
		Assert.assertTrue(sql.contains("A.STATUS NOT IN (1, 7)"));
		Assert.assertTrue(sql.contains("A.START_TIME < ?"));
		Assert.assertFalse(sql.contains("A.START_TIME <= ?"));
		Assert.assertTrue(sql.contains("A.END_TIME > ?"));
		Assert.assertFalse(sql.contains("A.END_TIME >= ?"));
		Assert.assertEquals(Arrays.asList("certNo", "endTime", "startTime"),
				boundSql.getParameterMappings().stream()
						.map(parameterMapping -> parameterMapping.getProperty())
						.collect(Collectors.toList()));
		Assert.assertEquals(startTime, parameters.get("startTime"));
		Assert.assertEquals(endTime, parameters.get("endTime"));
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
