package com.tce.smart.admin.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * 通过 MyBatis 实际解析并展开撤销 outbox SQL，验证 Oracle 到期调度与并发退避契约。
 */
public class OauthClientTokenRevocationTaskMapperContractTest {

	/** 到期任务先按下次重试时间排序，之后才按创建时间和任务 ID 稳定限批。 */
	@Test
	public void pendingBatchFiltersDueTasksBeforeStableOracleLimit() throws Exception {
		Configuration configuration = configuration();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("now", LocalDateTime.of(2026, 9, 5, 12, 0));
		parameters.put("limit", 50);

		BoundSql boundSql = configuration.getMappedStatement(
				"com.tce.smart.admin.mapper.OauthClientTokenRevocationTaskMapper.selectPendingBatch")
				.getBoundSql(parameters);
		String sql = normalize(boundSql.getSql());

		assertThat(sql).contains("WHERE NEXT_RETRY_AT <= ?");
		assertThat(sql).contains("ORDER BY NEXT_RETRY_AT, CREATE_TIME, TASK_ID");
		assertThat(sql).contains("WHERE ROWNUM <= ?");
		assertThat(parameterProperties(boundSql)).containsExactly("now", "limit");
	}

	/** 失败退避只更新精确 taskId，且较旧尝试不能把更晚的退避时间提前。 */
	@Test
	public void postponeFailureOnlyMovesRetryTimeForwardForExactTask() throws Exception {
		Configuration configuration = configuration();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("taskId", "task-version-1");
		parameters.put("nextRetryAt", LocalDateTime.of(2026, 9, 5, 12, 1));

		BoundSql boundSql = configuration.getMappedStatement(
				"com.tce.smart.admin.mapper.OauthClientTokenRevocationTaskMapper.postponeFailure")
				.getBoundSql(parameters);
		String sql = normalize(boundSql.getSql());

		assertThat(sql).contains("SET NEXT_RETRY_AT = ?");
		assertThat(sql).contains("WHERE TASK_ID = ?");
		assertThat(sql).contains("AND NEXT_RETRY_AT < ?");
		assertThat(parameterProperties(boundSql))
				.containsExactly("nextRetryAt", "taskId", "nextRetryAt");
	}

	/** 解析真实 Mapper 文件，XML、MappedStatement 或参数映射错误都会让测试失败。 */
	private Configuration configuration() throws Exception {
		Configuration configuration = new Configuration();
		String resource = "mapper/OauthClientTokenRevocationTaskMapper.xml";
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
			assertNotNull(input);
			new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
		}
		return configuration;
	}

	/** 压缩空白，避免格式化差异干扰 SQL 语义断言。 */
	private String normalize(String sql) {
		return sql.replaceAll("\\s+", " ").trim();
	}

	/** 返回 MyBatis 实际绑定的参数属性顺序。 */
	private List<String> parameterProperties(BoundSql boundSql) {
		return boundSql.getParameterMappings().stream()
				.map(mapping -> mapping.getProperty()).collect(Collectors.toList());
	}
}
