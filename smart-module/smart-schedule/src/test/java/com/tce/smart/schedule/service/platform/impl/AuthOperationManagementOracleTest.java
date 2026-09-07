package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementAttemptRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementBatchFilter;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementBatchRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementTargetFilter;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementTargetRow;
import com.tce.smart.platform.core.mapper.AuthOperationManagementMapper;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 在本任务隔离 Oracle 上验证管理查询使用生产分页插件生成 count 和页边界 SQL。
 */
public class AuthOperationManagementOracleTest {

	private static final int PRIMARY_TARGET_COUNT = 240;
	private static HikariDataSource sharedDataSource;

	private JdbcTemplate jdbc;
	private AuthOperationManagementMapper mapper;
	private SqlEvidenceInterceptor sqlEvidence;
	private final List<Long> ownedBatchIds = new ArrayList<>();
	private int ownParkId;
	private int foreignParkId;
	private String sourceMarker;
	private long idBase;

	@Before
	public void setUp() throws Exception {
		String url = System.getenv("SMART_AUTH_ORACLE_URL");
		Assume.assumeTrue("显式启用本机合成 Oracle 才执行", url != null && !url.isEmpty());
		Assert.assertTrue("本用例只允许本机 Oracle", url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));
		Assert.assertEquals("必须使用本任务合成 schema", "SMART_AUTH_TEST",
				System.getenv("SMART_AUTH_ORACLE_USER"));

		synchronized (AuthOperationManagementOracleTest.class) {
			if (sharedDataSource == null) {
				sharedDataSource = new HikariDataSource();
				sharedDataSource.setJdbcUrl(url);
				sharedDataSource.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
				sharedDataSource.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
				sharedDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
				sharedDataSource.setMaximumPoolSize(4);
				sharedDataSource.setMinimumIdle(0);
				sharedDataSource.setPoolName("auth-management-query-test");
			}
		}
		jdbc = new JdbcTemplate(sharedDataSource);
		assertRequiredFixture();
		printOracleVersion();

		sqlEvidence = new SqlEvidenceInterceptor();
		PaginationInterceptor pagination = new EvidencePaginationInterceptor(sqlEvidence);
		pagination.setLimit(-1);
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		configuration.addMapper(AuthOperationManagementMapper.class);
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
		factory.setDataSource(sharedDataSource);
		factory.setConfiguration(configuration);
		factory.setMapperLocations(new ClassPathResource[]{
				new ClassPathResource("mapper/AuthOperationManagementMapper.xml")
		});
		// 分页插件必须位于外层，先改写 SQL，再由证据插件记录最终送往 JDBC 的页 SQL。
		factory.setPlugins(new Interceptor[]{sqlEvidence, pagination});
		SqlSessionFactory sessionFactory = factory.getObject();
		mapper = new SqlSessionTemplate(sessionFactory).getMapper(AuthOperationManagementMapper.class);

		long random = positiveId() % 8000000000000000L;
		idBase = 100000000000000000L + random;
		ownParkId = 100000 + (int) (positiveId() % 700000000L);
		foreignParkId = ownParkId == 700100000 ? ownParkId - 1 : ownParkId + 1;
		Assert.assertNotEquals("不得使用其他测试保留园区", 9001, ownParkId);
		Assert.assertNotEquals("不得使用其他测试保留园区", 9001, foreignParkId);
		sourceMarker = "mgmt-oracle-" + Long.toUnsignedString(positiveId(), 36);
	}

	@AfterClass
	public static void closePool() {
		if (sharedDataSource != null) {
			sharedDataSource.close();
			sharedDataSource = null;
		}
	}

	/**
	 * 批次分页同时验证授权园区过滤、真实 count 和第二页稳定边界。
	 */
	@Test
	public void batchPageUsesOraclePaginationAndAuthorizedParkBoundary() {
		Timestamp fixedTime = Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC()).minusHours(1));
		for (int i = 0; i < 5; i++) {
			insertBatch(idBase + i, ownParkId, 0, "QUEUED", fixedTime);
		}
		insertBatch(idBase + 10, foreignParkId, 0, "QUEUED", fixedTime);

		sqlEvidence.clear();
		IPage<AuthOperationManagementBatchRow> result = mapper.selectBatchPage(
				new Page<>(2, 2), AuthOperationManagementBatchFilter.builder()
						.allowedParkIds(Collections.singletonList(ownParkId))
						.action("DELETE").status("QUEUED")
						.sourceType("MGMT_ORACLE").sourceId(sourceMarker).build());

		Assert.assertEquals("count 必须只统计授权园区", 5L, result.getTotal());
		Assert.assertEquals("第二页必须保留两条", 2, result.getRecords().size());
		Assert.assertEquals(Long.valueOf(idBase + 2), result.getRecords().get(0).getBatchId());
		Assert.assertEquals(Long.valueOf(idBase + 1), result.getRecords().get(1).getBatchId());
		Assert.assertTrue(result.getRecords().stream().allMatch(row -> row.getParkId() == ownParkId));
		assertOracleCountAndPageSql("SMT_AUTH_OPERATION_BATCH");
	}

	/**
	 * 目标分页验证批次隔离、状态组合、空页，以及只读取当前页目标的最新尝试。
	 */
	@Test
	public void targetPageUsesOraclePaginationAndLatestAttemptForCurrentPageOnly() {
		long selectedBatchId = idBase + 100;
		long otherBatchId = idBase + 101;
		long foreignBatchId = idBase + 102;
		Timestamp fixedTime = Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC()).minusMinutes(30));
		insertBatch(selectedBatchId, ownParkId, PRIMARY_TARGET_COUNT, "RUNNING", fixedTime);
		insertBatch(otherBatchId, ownParkId, 40, "RUNNING", fixedTime);
		insertBatch(foreignBatchId, foreignParkId, 20, "RUNNING", fixedTime);

		long selectedTargetBase = idBase + 10000;
		insertTargets(selectedBatchId, ownParkId, selectedTargetBase, PRIMARY_TARGET_COUNT, fixedTime);
		insertTargets(otherBatchId, ownParkId, idBase + 20000, 40, fixedTime);
		insertTargets(foreignBatchId, foreignParkId, idBase + 30000, 20, fixedTime);
		insertAttempts(selectedTargetBase, PRIMARY_TARGET_COUNT);
		insertAttempts(idBase + 20000, 40);

		AuthOperationManagementTargetFilter combinedStates = AuthOperationManagementTargetFilter.builder()
				.batchId(selectedBatchId).parkId(ownParkId)
				.states(Arrays.asList("FAILED", "VERIFYING")).build();
		sqlEvidence.clear();
		IPage<AuthOperationManagementTargetRow> page = mapper.selectTargetPage(new Page<>(2, 37), combinedStates);

		Assert.assertEquals("两种状态应各占四分之一", 120L, page.getTotal());
		Assert.assertEquals(37, page.getRecords().size());
		Assert.assertTrue(page.getRecords().stream().allMatch(row -> row.getBatchId() == selectedBatchId));
		Assert.assertTrue(page.getRecords().stream().allMatch(row -> row.getParkId() == ownParkId));
		Assert.assertTrue(page.getRecords().stream()
				.allMatch(row -> "FAILED".equals(row.getState()) || "VERIFYING".equals(row.getState())));
		assertOracleCountAndPageSql("SMT_AUTH_OPERATION_TARGET");

		List<Long> pageTargetIds = page.getRecords().stream()
				.map(AuthOperationManagementTargetRow::getTargetId).collect(Collectors.toList());
		List<AuthOperationManagementAttemptRow> latest = mapper.selectLatestAttempts(pageTargetIds);
		Assert.assertEquals("每个当前页目标只返回一个最新尝试", pageTargetIds.size(), latest.size());
		Assert.assertTrue(latest.stream().allMatch(attempt -> pageTargetIds.contains(attempt.getTargetId())));
		Assert.assertTrue(latest.stream().allMatch(attempt -> attempt.getAttemptNo() == 2));
		Assert.assertTrue(latest.stream().allMatch(attempt -> "LATEST".equals(attempt.getStatus())));
		Assert.assertTrue(latest.stream().allMatch(attempt ->
				attempt.getExternalBatchId().equals(sourceMarker + "-latest")));

		IPage<AuthOperationManagementTargetRow> emptyPage = mapper.selectTargetPage(new Page<>(100, 50), combinedStates);
		Assert.assertEquals("越界页仍返回准确 count", 120L, emptyPage.getTotal());
		Assert.assertTrue("越界页记录必须为空", emptyPage.getRecords().isEmpty());
		Assert.assertEquals("未加状态过滤时只能统计指定批次", PRIMARY_TARGET_COUNT,
				mapper.selectTargetPage(new Page<>(1, 25), AuthOperationManagementTargetFilter.builder()
						.batchId(selectedBatchId).parkId(ownParkId).build()).getTotal());
	}

	@After
	public void cleanOnlyOwnedBatches() {
		if (jdbc == null) {
			return;
		}
		for (Long batchId : ownedBatchIds) {
			jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN "
					+ "(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?)", batchId);
			jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN "
					+ "(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?)", batchId);
			jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?", batchId);
			jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE BATCH_ID = ?", batchId);
			jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ? AND SOURCE_ID = ?", batchId, sourceMarker);
		}
	}

	private void assertRequiredFixture() {
		Integer tableCount = jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN "
				+ "('SMT_AUTH_OPERATION_BATCH','SMT_AUTH_DELETE_REQUEST','SMT_AUTH_OPERATION_TARGET',"
				+ "'SMT_AUTH_OPERATION_ATTEMPT','SMT_AUTH_RESULT_EVENT')", Integer.class);
		Assert.assertEquals("必须复用已存在的五表 fixture，测试不得创建表", Integer.valueOf(5), tableCount);
	}

	private void printOracleVersion() {
		String version = jdbc.queryForObject("SELECT BANNER FROM V$VERSION WHERE ROWNUM = 1", String.class);
		Assert.assertNotNull(version);
		System.out.println("ORACLE_VERSION_EVIDENCE=" + version.replace('\n', ' '));
	}

	private void insertBatch(long batchId, int parkId, int expectedCount, String status, Timestamp createTime) {
		ownedBatchIds.add(batchId);
		jdbc.update("INSERT INTO SMT_AUTH_OPERATION_BATCH "
				+ "(ID, PARK_ID, IDEMPOTENCY_KEY, ACTION, SOURCE_TYPE, SOURCE_ID, SELECTION_SNAPSHOT, "
				+ "PAYLOAD_FINGERPRINT, EXPECTED_COUNT, EXPANDED_COUNT, EXPANSION_CURSOR, STATUS, "
				+ "ACCEPTED_AT, CREATE_TIME, UPDATE_TIME) "
				+ "VALUES (?, ?, ?, 'DELETE', 'MGMT_ORACLE', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				batchId, parkId, sourceMarker + "-key-" + batchId, sourceMarker,
				"{\"owner\":\"" + sourceMarker + "\"}", sourceMarker + "-fingerprint-" + batchId,
				expectedCount, expectedCount, expectedCount, status, createTime, createTime, createTime);
	}

	private void insertTargets(long batchId, int parkId, long targetBase, int count, Timestamp createTime) {
		String sql = "INSERT INTO SMT_AUTH_OPERATION_TARGET "
				+ "(ID, BATCH_ID, PARK_ID, TARGET_KEY, SUBJECT_TYPE, SUBJECT_ID, SUBJECT_SNAPSHOT, "
				+ "RESOURCE_TYPE, DEVICE_ID, RESOURCE_ID, ACCESS_TYPE, OPERATION_QUEUE, ACTION, "
				+ "OPERATION_VERSION, STATE, FAILURE_REASON, CREATE_TIME, UPDATE_TIME) "
				+ "VALUES (?, ?, ?, ?, 'STAFF', ?, ?, 'PERMISSION', ?, ?, 'ISC', 'DELETE', 'DELETE', "
				+ "1, ?, ?, ?, ?)";
		String[] states = {"FAILED", "VERIFYING", "CONVERGED", "QUEUED"};
		jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement statement, int index) throws SQLException {
				long targetId = targetBase + index;
				statement.setLong(1, targetId);
				statement.setLong(2, batchId);
				statement.setInt(3, parkId);
				statement.setString(4, sourceMarker + "-target-" + targetId);
				statement.setString(5, sourceMarker + "-staff-" + index);
				statement.setString(6, "{\"owner\":\"" + sourceMarker + "\"}");
				statement.setString(7, sourceMarker + "-device-" + index);
				statement.setString(8, sourceMarker + "-resource-" + index);
				statement.setString(9, states[index % states.length]);
				statement.setString(10, index % states.length == 0 ? "synthetic-failure" : null);
				statement.setTimestamp(11, createTime);
				statement.setTimestamp(12, createTime);
			}

			@Override
			public int getBatchSize() {
				return count;
			}
		});
	}

	private void insertAttempts(long targetBase, int targetCount) {
		String sql = "INSERT INTO SMT_AUTH_OPERATION_ATTEMPT "
				+ "(ID, TARGET_ID, ATTEMPT_NO, ACCESS_TYPE, STATUS, EXTERNAL_BATCH_ID, "
				+ "EXTERNAL_COMMAND_ID, CREATE_TIME, UPDATE_TIME) VALUES (?, ?, ?, 'ISC', ?, ?, ?, ?, ?)";
		int total = targetCount * 2;
		Timestamp now = Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC()));
		jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement statement, int index) throws SQLException {
				int targetOffset = index / 2;
				int attemptNo = index % 2 + 1;
				long targetId = targetBase + targetOffset;
				statement.setLong(1, idBase + 1000000L + (targetId - idBase) * 2L + attemptNo);
				statement.setLong(2, targetId);
				statement.setInt(3, attemptNo);
				statement.setString(4, attemptNo == 2 ? "LATEST" : "OLDER");
				statement.setString(5, sourceMarker + (attemptNo == 2 ? "-latest" : "-older"));
				statement.setString(6, sourceMarker + "-command-" + targetId + "-" + attemptNo);
				statement.setTimestamp(7, now);
				statement.setTimestamp(8, now);
			}

			@Override
			public int getBatchSize() {
				return total;
			}
		});
	}

	private void assertOracleCountAndPageSql(String tableName) {
		String countSql = sqlEvidence.find(tableName, "COUNT");
		String pageSql = sqlEvidence.find(tableName, "ROWNUM");
		Assert.assertNotNull("分页插件必须生成 count SQL: " + sqlEvidence.sql(), countSql);
		Assert.assertNotNull("分页插件必须生成 Oracle ROWNUM 页 SQL: " + sqlEvidence.sql(), pageSql);
		System.out.println("ORACLE_COUNT_SQL_EVIDENCE=" + countSql);
		System.out.println("ORACLE_PAGE_SQL_EVIDENCE=" + pageSql);
	}

	private long positiveId() {
		long value = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
		return value == 0L ? 1L : value;
	}

	@Intercepts(@Signature(type = StatementHandler.class, method = "prepare",
			args = {Connection.class, Integer.class}))
	public static class SqlEvidenceInterceptor implements Interceptor {
		private final List<String> sql = new CopyOnWriteArrayList<>();

		@Override
		public Object intercept(Invocation invocation) throws Throwable {
			StatementHandler handler = (StatementHandler) invocation.getTarget();
			sql.add(handler.getBoundSql().getSql().replaceAll("\\s+", " ").trim());
			return invocation.proceed();
		}

		void clear() {
			sql.clear();
		}

		void add(String statement) {
			sql.add(statement.replaceAll("\\s+", " ").trim());
		}

		String find(String tableName, String token) {
			String upperTable = tableName.toUpperCase(Locale.ROOT);
			String upperToken = token.toUpperCase(Locale.ROOT);
			return sql.stream().filter(statement -> {
				String normalized = statement.toUpperCase(Locale.ROOT);
				return normalized.contains(upperTable) && normalized.contains(upperToken);
			}).findFirst().orElse(null);
		}

		List<String> sql() {
			return new ArrayList<>(sql);
		}
	}

	/** 复用生产分页实现，仅旁路记录插件实际交给 JDBC 的 count SQL。 */
	@Intercepts(@Signature(type = StatementHandler.class, method = "prepare",
			args = {Connection.class, Integer.class}))
	private static final class EvidencePaginationInterceptor extends PaginationInterceptor {
		private final SqlEvidenceInterceptor evidence;

		private EvidencePaginationInterceptor(SqlEvidenceInterceptor evidence) {
			this.evidence = evidence;
		}

		@Override
		protected void queryTotal(boolean overflowCurrent, String sql, MappedStatement mappedStatement, BoundSql boundSql,
				com.baomidou.mybatisplus.core.metadata.IPage<?> page, Connection connection) {
			evidence.add(sql);
			super.queryTotal(overflowCurrent, sql, mappedStatement, boundSql, page, connection);
		}
	}
}
