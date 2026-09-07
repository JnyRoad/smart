package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.config.AuthOperationGovernanceProperties;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceConflictException;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.AuthOperationGovernanceService;
import com.tce.smart.platform.core.service.impl.AuthOperationVersionService;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** 真实 Oracle 上验证治理动作不可变审计、精确 CAS、回滚和生产分页插件。 */
public class AuthOperationGovernanceOracleTest {

	private static HikariDataSource sharedDataSource;
	private JdbcTemplate jdbc;
	private DataSourceTransactionManager transactionManager;
	private AuthOperationGovernanceMapper mapper;
	private AuthOperationVersionService versions;
	private AuthOperationGovernanceService service;
	private SqlEvidenceInterceptor sqlEvidence;
	private Actor actor;
	private int parkId;
	private String marker;
	private final List<Long> targetIds = new ArrayList<>();
	private final List<Long> batchIds = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		String url = System.getenv("SMART_AUTH_ORACLE_URL");
		Assume.assumeTrue("显式启用本机合成 Oracle 才执行", url != null && !url.isEmpty());
		Assert.assertTrue("本用例只允许本机 Oracle", url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));
		Assert.assertEquals("必须使用本任务合成 schema", "SMART_AUTH_TEST", System.getenv("SMART_AUTH_ORACLE_USER"));

		synchronized (AuthOperationGovernanceOracleTest.class) {
			if (sharedDataSource == null) {
				sharedDataSource = new HikariDataSource();
				sharedDataSource.setJdbcUrl(url);
				sharedDataSource.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
				sharedDataSource.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
				sharedDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
				sharedDataSource.setMaximumPoolSize(4);
				sharedDataSource.setMinimumIdle(0);
				sharedDataSource.setPoolName("auth-governance-test");
			}
		}
		jdbc = new JdbcTemplate(sharedDataSource);
		jdbc.setQueryTimeout(30);
		assertExistingFixture();
		createGovernanceTableOnlyWhenAbsent();
		printOracleVersionAndGovernanceMetadata();
		transactionManager = new DataSourceTransactionManager(sharedDataSource);

		sqlEvidence = new SqlEvidenceInterceptor();
		PaginationInterceptor pagination = new EvidencePaginationInterceptor(sqlEvidence);
		pagination.setLimit(-1);
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		configuration.setDefaultStatementTimeout(30);
		configuration.addMapper(AuthOperationGovernanceMapper.class);
		configuration.addMapper(SmtAuthSubjectCoordMapper.class);
		configuration.addMapper(SmtAuthSourceCoordMapper.class);
		configuration.addMapper(SmtAuthResourceCoordMapper.class);
		configuration.addMapper(SmtAuthSourceResourceMapper.class);
		configuration.addMapper(SmtAuthIdentityAliasMapper.class);
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
		factory.setDataSource(sharedDataSource);
		factory.setConfiguration(configuration);
		factory.setMapperLocations(new Resource[]{
				new ClassPathResource("mapper/AuthOperationGovernanceMapper.xml"),
				new ClassPathResource("mapper/SmtAuthSubjectCoordMapper.xml"),
				new ClassPathResource("mapper/SmtAuthSourceCoordMapper.xml"),
				new ClassPathResource("mapper/SmtAuthResourceCoordMapper.xml"),
				new ClassPathResource("mapper/SmtAuthSourceResourceMapper.xml"),
				new ClassPathResource("mapper/SmtAuthIdentityAliasMapper.xml")
		});
		factory.setPlugins(new Interceptor[]{sqlEvidence, pagination});
		SqlSessionFactory sessionFactory = factory.getObject();
		SqlSessionTemplate session = new SqlSessionTemplate(sessionFactory);
		mapper = session.getMapper(AuthOperationGovernanceMapper.class);
		versions = proxied(new AuthOperationVersionService(session.getMapper(SmtAuthSubjectCoordMapper.class),
				session.getMapper(SmtAuthSourceCoordMapper.class), session.getMapper(SmtAuthResourceCoordMapper.class),
				session.getMapper(SmtAuthSourceResourceMapper.class), session.getMapper(SmtAuthIdentityAliasMapper.class)),
				AuthOperationVersionService.class);
		service = governance(mapper);

		parkId = 100000 + (int) (positiveId() % 700000000L);
		Assert.assertNotEquals("不得使用其他测试保留园区", 9001, parkId);
		marker = "gov-oracle-" + Long.toUnsignedString(positiveId(), 36);
		actor = Actor.builder().userId(710001).username("治理Oracle测试")
				.parkIds(Collections.singletonList(parkId))
				.permissions(Arrays.asList(AuthOperationGovernanceService.RETRY_PERMISSION,
						AuthOperationGovernanceService.MANUAL_PERMISSION,
						AuthOperationGovernanceService.REVIEW_PERMISSION)).build();
	}

	@AfterClass
	public static void closePool() {
		if (sharedDataSource != null) sharedDataSource.close();
		sharedDataSource = null;
	}

	@After
	public void cleanupOnlyOwnedRows() {
		if (jdbc == null || marker == null) return;
		jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_REVIEW WHERE TASK_KEY LIKE ?", marker + "%");
		for (Long targetId : targetIds) {
			jdbc.update("DELETE FROM SMT_AUTH_GOVERNANCE_ACTION WHERE TARGET_ID=?", targetId);
			jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_PHASE WHERE TARGET_ID=?", targetId);
			jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID=?", targetId);
			jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE TARGET_ID=?", targetId);
			jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID=?", targetId);
			jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=? AND PARK_ID=?", targetId, parkId);
		}
		jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN "
				+ "(SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)", parkId);
		jdbc.update("DELETE FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?", parkId);
		jdbc.update("DELETE FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?", parkId);
		jdbc.update("DELETE FROM SMT_AUTH_SUBJECT_COORD WHERE PARK_ID=?", parkId);
		for (Long batchId : batchIds) {
			jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE BATCH_ID=? AND PARK_ID=?", batchId, parkId);
			jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE ID=? AND PARK_ID=?", batchId, parkId);
		}
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?", Integer.class, parkId));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND SOURCE_ID=?", Integer.class, parkId, marker));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?", Integer.class, parkId));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?", Integer.class, parkId));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_REVIEW WHERE TASK_KEY LIKE ?", Integer.class, marker + "%"));
		System.out.println("GOVERNANCE_CLEANUP_EVIDENCE=park:" + parkId + ",ownedRows:0");
	}

	@Test
	public void retryConcurrentReplayAndManualEvidenceUseRealTransactions() throws Exception {
		Fixture fixture = fixture("concurrent");
		RetryCommand command = retry(fixture, "same-key", "确定旧尝试完全未发送");
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService workers = Executors.newFixedThreadPool(2);
		try {
			Future<ActionResult> first = workers.submit(() -> { start.await(); return service.retryKnownUnsent(actor, command); });
			Future<ActionResult> second = workers.submit(() -> { start.await(); return service.retryKnownUnsent(actor, command); });
			start.countDown();
			Set<String> outcomes = new HashSet<>(Arrays.asList(first.get(30, TimeUnit.SECONDS).getOutcome(),
					second.get(30, TimeUnit.SECONDS).getOutcome()));
			Assert.assertEquals(new HashSet<>(Arrays.asList("REQUEUED", "ALREADY_APPLIED")), outcomes);
		} finally {
			workers.shutdownNow();
			Assert.assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
		}
		Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_GOVERNANCE_ACTION WHERE TARGET_ID=?", Integer.class, fixture.targetId));
		Assert.assertEquals("EXPIRED", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?", String.class, fixture.attemptId));
		Assert.assertEquals("QUEUED", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?", String.class, fixture.targetId));
		Assert.assertNull("旧尝试已确认未发送后必须释放资源attempt blocker",
				jdbc.queryForObject("SELECT BLOCKING_ATTEMPT_ID FROM SMT_AUTH_RESOURCE_COORD WHERE ID=?",
						Long.class, fixture.resourceCoordId));
		Assert.assertNull("资源约束要求旧target blocker同步释放",
				jdbc.queryForObject("SELECT BLOCKING_TARGET_ID FROM SMT_AUTH_RESOURCE_COORD WHERE ID=?",
						Long.class, fixture.resourceCoordId));
		try {
			service.retryKnownUnsent(actor, retry(fixture, "same-key", "改变后的载荷"));
			Assert.fail("同幂等键不同规范载荷必须冲突");
		} catch (AuthOperationGovernanceConflictException expected) {
			Assert.assertTrue(expected.getMessage().contains("幂等键"));
		}

		Fixture manualFixture = fixture("manual");
		jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='VERIFYING' WHERE ID=?", manualFixture.targetId);
		jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET STATUS='VERIFYING' WHERE ID=?", manualFixture.attemptId);
		LocalDateTime observedAt = mapper.now().minusMinutes(1);
		ActionResult manual = service.recordManualVerification(actor, ManualVerificationCommand.builder()
				.targetId(manualFixture.targetId).expectedOperationVersion(manualFixture.generation)
				.expectedAttemptId(manualFixture.attemptId).expectedState("VERIFYING").idempotencyKey("manual-absent")
				.observedConclusion("PERMISSION_ABSENT").reasonText("仅记录现场观察，不改变权限状态")
				.evidenceType("OPERATOR_OBSERVATION").evidenceReference(marker + "-case")
				.evidenceBody("{\"observation\":\"现场列表未发现权限\"}").observedAt(observedAt).build());
		Assert.assertEquals("RECORDED_PENDING_VERIFICATION", manual.getOutcome());
		Assert.assertEquals("VERIFYING", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?", String.class, manualFixture.targetId));
		Assert.assertEquals("VERIFYING", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?", String.class, manualFixture.attemptId));
		IPage<ActionRow> actions = service.getTargetActions(actor, manualFixture.targetId, 1, 20);
		Assert.assertEquals(1L, actions.getTotal());
		Assert.assertTrue("动作列表不得加载证据CLOB", actions.getRecords().stream()
				.allMatch(row -> row.getEvidenceBody() == null));
		ActionRow detail = service.getTargetAction(actor, manualFixture.targetId, manual.getActionId());
		Assert.assertEquals("{\"observation\":\"现场列表未发现权限\"}", detail.getEvidenceBody());
	}

	@Test
	public void failedTargetCasRollsBackInsertedAuditAndAttemptExpiration() {
		Fixture fixture = fixture("rollback");
		AuthOperationGovernanceMapper failing = (AuthOperationGovernanceMapper) Proxy.newProxyInstance(
				AuthOperationGovernanceMapper.class.getClassLoader(), new Class[]{AuthOperationGovernanceMapper.class},
				(proxy, method, args) -> {
					if ("requeueKnownUnsentTarget".equals(method.getName())) return 0;
					try { return method.invoke(mapper, args); }
					catch (InvocationTargetException failure) { throw failure.getTargetException(); }
				});
		try {
			governance(failing).retryKnownUnsent(actor, retry(fixture, "rollback-key", "模拟目标CAS失败"));
			Assert.fail("目标CAS失败必须回滚整个动作事务");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("目标入队失败"));
		}
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_GOVERNANCE_ACTION WHERE TARGET_ID=?", Integer.class, fixture.targetId));
		Assert.assertEquals("CLAIMED", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?", String.class, fixture.attemptId));
		Assert.assertEquals("EXECUTING", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?", String.class, fixture.targetId));
	}

	@Test
	public void reviewPagesUseOraclePaginationAndKeepParkAndGlobalScopesSeparate() {
		Timestamp created = Timestamp.valueOf(mapper.now().minusMinutes(1));
		for (int i = 0; i < 105; i++) insertReview(String.format("%s-park-%03d", marker, i), parkId, created);
		for (int i = 0; i < 3; i++) insertReview(String.format("%s-global-%03d", marker, i), null, created);
		insertReview(marker + "-foreign", parkId + 1, created);

		sqlEvidence.clear();
		IPage<ReviewRow> park = service.getParkReviews(actor, parkId, 2, 100);
		Assert.assertEquals(105L, park.getTotal());
		Assert.assertEquals(5, park.getRecords().size());
		assertOracleCountAndPageSql("SMT_AUTH_TRANSPORT_REVIEW");
		Actor global = Actor.builder().userId(710002).username("全局治理Oracle测试")
				.parkIds(Collections.emptyList())
				.permissions(Collections.singletonList(AuthOperationGovernanceService.GLOBAL_REVIEW_PERMISSION)).build();
		sqlEvidence.clear();
		IPage<ReviewRow> globalPage = service.getGlobalReviews(global, 1, 2);
		Assert.assertEquals(3L, globalPage.getTotal());
		Assert.assertEquals(2, globalPage.getRecords().size());
		Assert.assertTrue(globalPage.getRecords().stream().allMatch(row -> row.getParkId() == null));
		assertOracleCountAndPageSql("SMT_AUTH_TRANSPORT_REVIEW");
		Assert.assertTrue(service.getGlobalReviews(global, 99, 100).getRecords().isEmpty());
	}

	private Fixture fixture(String suffix) {
		long batchId = ownId();
		long requestId = ownId();
		long targetId = ownId();
		long attemptId = ownId();
		batchIds.add(batchId);
		targetIds.add(targetId);
		jdbc.update("INSERT INTO SMT_AUTH_OPERATION_BATCH "
				+ "(ID,PARK_ID,IDEMPOTENCY_KEY,ACTION,SOURCE_TYPE,SOURCE_ID,SELECTION_SNAPSHOT,PAYLOAD_FINGERPRINT,"
				+ "EXPECTED_COUNT,EXPANDED_COUNT,EXPANSION_CURSOR,STATUS,ACCEPTED_AT,CREATE_TIME,UPDATE_TIME) "
				+ "VALUES (?,?,?,'DELETE','GOV_ORACLE',?,'{}',?,1,1,1,'RUNNING',SYSTIMESTAMP,SYSTIMESTAMP,SYSTIMESTAMP)",
				batchId, parkId, marker + "-batch-" + suffix, marker, marker + "-fp-" + suffix);
		SourceVersion source = versions.reserveSourceIntent(SourceIntent.builder().parkId(parkId)
				.sourceKind("STAFF_AUTH").stableKey(marker + ":" + suffix).subjectType("STAFF")
				.subjectId(marker + "-staff").sourceRowId(marker + "-row-" + suffix)
				.sourceFingerprint(marker + "-source-fp-" + suffix).intentKey(marker + "-intent-" + suffix)
				.batchId(batchId).payloadSnapshot("frozen").action("ADD")
				.window(Window.builder().from(LocalDateTime.of(2030, 1, 1, 0, 0))
						.to(LocalDateTime.of(2030, 1, 2, 0, 0)).build()).build());
		ResourceKey key = ResourceKey.builder().parkId(parkId).subjectType("STAFF")
				.subjectId(marker + "-staff").accessType("DIRECT").deviceId(marker + "-device")
				.resourceType("CARD").resourceId(marker + "-permission")
				.serviceType("1").credentialChannel("CARD").build();
		ResourceDecision decision = versions.stageContribution(ContributionCommand.builder()
				.sourceId(source.getSourceId()).sourceGeneration(source.getGeneration()).resource(key)
				.requestId(requestId).build());
		jdbc.update("INSERT INTO SMT_AUTH_DELETE_REQUEST "
				+ "(ID,BATCH_ID,PARK_ID,SUBJECT_TYPE,SOURCE_TYPE,SOURCE_ROW_ID,SOURCE_IDENTITY_KEY,IDENTITY_SNAPSHOT,"
				+ "GENERATION,STATUS,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,'STAFF','GOV_ORACLE',?,?, '{}',?,'RUNNING',SYSTIMESTAMP,SYSTIMESTAMP)",
				requestId, batchId, parkId, source.getSourceRowId(), source.getSourceId(), source.getGeneration());
		jdbc.update("INSERT INTO SMT_AUTH_OPERATION_TARGET "
				+ "(ID,BATCH_ID,REQUEST_ID,PARK_ID,TARGET_KEY,SUBJECT_TYPE,SUBJECT_ID,SUBJECT_SNAPSHOT,RESOURCE_TYPE,"
				+ "DEVICE_ID,RESOURCE_ID,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,VALID_FROM,VALID_TO,STATE,LEASE_TOKEN,LEASE_UNTIL,"
				+ "CREATE_TIME,UPDATE_TIME) VALUES (?,?,?, ?,?,'STAFF',?,'{}','CARD',?,?,'DIRECT','CARD',?,?,"
				+ "?,?, 'EXECUTING',?,SYSTIMESTAMP-INTERVAL '2' MINUTE,SYSTIMESTAMP,SYSTIMESTAMP)",
				targetId, batchId, requestId, parkId, marker + "-target-" + suffix, key.getSubjectId(),
				key.getDeviceId(), AuthOperationVersionService.canonicalTargetResourceId(key), decision.getAction(),
				decision.getGeneration(), Timestamp.valueOf(decision.getWindows().get(0).getFrom()),
				Timestamp.valueOf(decision.getWindows().get(0).getTo()), marker + "-lease-" + suffix);
		jdbc.update("INSERT INTO SMT_AUTH_OPERATION_ATTEMPT "
				+ "(ID,TARGET_ID,ATTEMPT_NO,ACCESS_TYPE,STATUS,LEASE_TOKEN,LEASE_UNTIL,CREATE_TIME,UPDATE_TIME) "
				+ "VALUES (?,?,1,'DIRECT','EXECUTING',?,SYSTIMESTAMP-INTERVAL '2' MINUTE,SYSTIMESTAMP,SYSTIMESTAMP)",
				attemptId, targetId, marker + "-lease-" + suffix);
		Binding binding = Binding.builder().sourceId(source.getSourceId()).sourceGeneration(source.getGeneration())
				.resourceId(decision.getResourceId()).resourceGeneration(decision.getGeneration())
				.requestId(requestId).targetId(targetId).attemptId(attemptId).build();
		versions.bindTarget(binding);
		versions.bindAttempt(binding);
		jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET STATUS='CLAIMED' WHERE ID=?", attemptId);
		return new Fixture(targetId, attemptId, decision.getGeneration(), decision.getResourceId());
	}

	private RetryCommand retry(Fixture fixture, String key, String reason) {
		return RetryCommand.builder().targetId(fixture.targetId).expectedOperationVersion(fixture.generation)
				.expectedAttemptId(fixture.attemptId).expectedAttemptNo(1).expectedState("EXECUTING")
				.idempotencyKey(key).reasonText(reason).build();
	}

	private void insertReview(String id, Integer reviewPark, Timestamp created) {
		jdbc.update("INSERT INTO SMT_AUTH_TRANSPORT_REVIEW "
				+ "(ID,PARK_ID,ACCESS_TYPE,DEVICE_ID,TASK_KEY,REASON,STATE,CREATE_TIME) VALUES (?,?, 'DIRECT',?,?,?,?,?)",
				id, reviewPark, marker + "-device", id, "governance-test", "OPEN", created);
	}

	private AuthOperationGovernanceService governance(AuthOperationGovernanceMapper actualMapper) {
		AuthOperationGovernanceProperties properties = new AuthOperationGovernanceProperties();
		properties.setActionsEnabled(true);
		properties.setMaxRetryAttempts(3);
		return proxied(new AuthOperationGovernanceService(actualMapper, versions, properties),
				AuthOperationGovernanceService.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T proxied(T raw, Class<T> type) {
		ProxyFactory proxy = new ProxyFactory(raw);
		proxy.setProxyTargetClass(true);
		proxy.addAdvice(new TransactionInterceptor(transactionManager, new AnnotationTransactionAttributeSource()));
		return (T) proxy.getProxy();
	}

	private void assertExistingFixture() {
		Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN "
				+ "('SMT_AUTH_OPERATION_BATCH','SMT_AUTH_DELETE_REQUEST','SMT_AUTH_OPERATION_TARGET',"
				+ "'SMT_AUTH_OPERATION_ATTEMPT','SMT_AUTH_RESULT_EVENT','SMT_AUTH_TRANSPORT_PHASE',"
				+ "'SMT_AUTH_TRANSPORT_REVIEW','SMT_AUTH_SUBJECT_COORD','SMT_AUTH_SOURCE_COORD',"
				+ "'SMT_AUTH_RESOURCE_COORD','SMT_AUTH_SOURCE_RESOURCE','SMT_AUTH_IDENTITY_ALIAS')", Integer.class);
		Assert.assertEquals("必须复用完整既有十二表，治理测试不得重建", Integer.valueOf(12), count);
	}

	private void createGovernanceTableOnlyWhenAbsent() throws Exception {
		Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_GOVERNANCE_ACTION'", Integer.class);
		Assert.assertTrue("治理动作表只能完整缺失或已存在", count == 0 || count == 1);
		if (count == 0) {
			try (Connection connection = sharedDataSource.getConnection()) {
				ScriptUtils.executeSqlScript(connection, new ClassPathResource("authoperation/governance-schema.sql"));
			}
		}
	}

	private void printOracleVersionAndGovernanceMetadata() {
		String version = jdbc.queryForObject("SELECT BANNER FROM V$VERSION WHERE ROWNUM=1", String.class);
		Integer columns = jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_GOVERNANCE_ACTION'", Integer.class);
		Integer constraints = jdbc.queryForObject("SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE TABLE_NAME='SMT_AUTH_GOVERNANCE_ACTION' "
				+ "AND STATUS='ENABLED' AND CONSTRAINT_NAME IN ('PK_AUTH_GOV_ACTION','FK_AUTH_GOV_ACTION_TARGET',"
				+ "'UK_AUTH_GOV_ACTION_KEY','CK_AUTH_GOV_ACTION_TYPE','CK_AUTH_GOV_ACTION_CONCLUSION')", Integer.class);
		Integer indexes = jdbc.queryForObject("SELECT COUNT(*) FROM USER_INDEXES WHERE TABLE_NAME='SMT_AUTH_GOVERNANCE_ACTION' "
				+ "AND INDEX_TYPE<>'LOB'", Integer.class);
		Assert.assertEquals(Integer.valueOf(24), columns);
		Assert.assertEquals(Integer.valueOf(5), constraints);
		Assert.assertEquals(Integer.valueOf(2), indexes);
		System.out.println("ORACLE_VERSION_EVIDENCE=" + version.replace('\n', ' '));
		System.out.println("GOVERNANCE_TABLE_METADATA=columns:" + columns + ",constraints:" + constraints + ",indexes:" + indexes);
	}

	private void assertOracleCountAndPageSql(String table) {
		String count = sqlEvidence.find(table, "COUNT");
		String page = sqlEvidence.find(table, "ROWNUM");
		Assert.assertNotNull("分页插件必须生成count SQL: " + sqlEvidence.sql, count);
		Assert.assertNotNull("分页插件必须生成Oracle ROWNUM SQL: " + sqlEvidence.sql, page);
		System.out.println("ORACLE_COUNT_SQL_EVIDENCE=" + count);
		System.out.println("ORACLE_PAGE_SQL_EVIDENCE=" + page);
	}

	private long ownId() {
		long value;
		do value = 100000000000000000L + positiveId() % 8000000000000000L;
		while (value == 910000000001L);
		return value;
	}

	private static long positiveId() {
		long value = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
		return value == 0 ? 1 : value;
	}

	private static final class Fixture {
		final long targetId;
		final long attemptId;
		final long generation;
		final String resourceCoordId;
		Fixture(long targetId, long attemptId, long generation, String resourceCoordId) {
			this.targetId = targetId; this.attemptId = attemptId; this.generation = generation;
			this.resourceCoordId = resourceCoordId;
		}
	}

	@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
	public static class SqlEvidenceInterceptor implements Interceptor {
		private final List<String> sql = new CopyOnWriteArrayList<>();
		@Override public Object intercept(Invocation invocation) throws Throwable {
			StatementHandler handler = (StatementHandler) invocation.getTarget();
			sql.add(handler.getBoundSql().getSql().replaceAll("\\s+", " ").trim());
			return invocation.proceed();
		}
		void add(String statement) { sql.add(statement.replaceAll("\\s+", " ").trim()); }
		void clear() { sql.clear(); }
		String find(String table, String token) {
			String a = table.toUpperCase(Locale.ROOT), b = token.toUpperCase(Locale.ROOT);
			for (String statement : sql) {
				String upper = statement.toUpperCase(Locale.ROOT);
				if (upper.contains(a) && upper.contains(b)) return statement;
			}
			return null;
		}
	}

	@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
	private static final class EvidencePaginationInterceptor extends PaginationInterceptor {
		private final SqlEvidenceInterceptor evidence;
		EvidencePaginationInterceptor(SqlEvidenceInterceptor evidence) { this.evidence = evidence; }
		@Override protected void queryTotal(boolean overflowCurrent, String sql, MappedStatement statement,
				BoundSql boundSql, IPage<?> page, Connection connection) {
			evidence.add(sql);
			super.queryTotal(overflowCurrent, sql, statement, boundSql, page, connection);
		}
	}
}
