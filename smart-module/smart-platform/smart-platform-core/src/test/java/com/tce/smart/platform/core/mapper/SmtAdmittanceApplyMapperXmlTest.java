package com.tce.smart.platform.core.mapper;

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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SmtAdmittanceApplyMapperXmlTest {

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
		Assert.assertTrue(sql.contains("A.STATUS != 1"));
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
}
