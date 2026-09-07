package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.*;
import com.tce.smart.platform.core.entity.SmtAuthLegacyReview;
import com.tce.smart.platform.core.mapper.LegacyEmployeeAccessInventoryMapper;
import com.tce.smart.platform.core.service.LegacyEmployeeAccessInventoryService;
import com.tce.smart.platform.core.service.LegacyInventoryCanonicalizer;
import com.tce.smart.platform.core.service.impl.LegacyEmployeeAccessInventoryServiceImpl;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** 在合成 Oracle 上验证五流 raw、同事务 cursor/revision 与读取范围。 */
public class LegacyEmployeeAccessInventoryOracleTest {

	private static HikariDataSource dataSource;
	private JdbcTemplate jdbc;
	private LegacyEmployeeAccessInventoryMapper mapper;
	private LegacyEmployeeAccessInventoryService service;
	private long base;
	private int parkId;
	private String marker;
	private final List<String> runIds = new ArrayList<>();
	private final Map<String, List<Long>> owned = new LinkedHashMap<>();

	@Before
	public void setUp() throws Exception {
		String url = System.getenv("SMART_AUTH_ORACLE_URL");
		Assume.assumeTrue("显式启用本机合成Oracle才执行", url != null && !url.isEmpty());
		Assert.assertTrue(url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));
		Assert.assertEquals("SMART_AUTH_TEST", System.getenv("SMART_AUTH_ORACLE_USER"));
		if (dataSource == null) {
			dataSource = new HikariDataSource(); dataSource.setJdbcUrl(url);
			dataSource.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
			dataSource.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
			dataSource.setDriverClassName("oracle.jdbc.OracleDriver"); dataSource.setMaximumPoolSize(4);
			dataSource.setMinimumIdle(0); dataSource.setPoolName("legacy-inventory-test");
		}
		jdbc = new JdbcTemplate(dataSource); jdbc.setQueryTimeout(30);
		assertSchemaAlreadyExists();
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setMapUnderscoreToCamelCase(true); configuration.setJdbcTypeForNull(JdbcType.NULL);
		configuration.setDefaultStatementTimeout(30); configuration.addMapper(LegacyEmployeeAccessInventoryMapper.class);
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean(); factory.setDataSource(dataSource);
		factory.setConfiguration(configuration); factory.setMapperLocations(new Resource[]{
				new ClassPathResource("mapper/LegacyEmployeeAccessInventoryMapper.xml")});
		SqlSessionFactory sqlSessionFactory = factory.getObject();
		mapper = new SqlSessionTemplate(sqlSessionFactory).getMapper(LegacyEmployeeAccessInventoryMapper.class);
		LegacyEmployeeAccessInventoryServiceImpl target = new LegacyEmployeeAccessInventoryServiceImpl(
				mapper, new LegacyInventoryCanonicalizer(), true);
		ProxyFactory proxy = new ProxyFactory(target); proxy.setInterfaces(LegacyEmployeeAccessInventoryService.class);
		proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(dataSource),
				new AnnotationTransactionAttributeSource()));
		service = (LegacyEmployeeAccessInventoryService) proxy.getProxy();
		base = 6000000000L + Math.floorMod(System.nanoTime(), 500000000L);
		parkId = 100000 + (int) Math.floorMod(System.nanoTime(), 700000000L);
		Assert.assertNotEquals(9001, parkId);
		marker = "legacy-" + Long.toUnsignedString(System.nanoTime(), 36);
	}

	@After
	public void cleanupOwnedRows() {
		if (jdbc == null) return;
		for (String runId : runIds) {
			jdbc.update("DELETE FROM SMT_AUTH_LEGACY_REVIEW WHERE RUN_FLOW_ID IN (SELECT ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?)", runId);
			jdbc.update("DELETE FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?", runId);
		}
		deleteOwned("SMT_STAFF_DEVICE_AUTH"); deleteOwned("SMT_ISC_DOWN_RECORD");
		deleteOwned("SMT_ISC_DEVICE_TASK"); deleteOwned("SMT_TASK_DOWN_RECORD"); deleteOwned("SMT_DEVICE_TASK");
		deleteOwned("SMT_DEVICE_AUTHORITY_RELATION"); deleteOwned("SMT_DEVICE_AUTHORITY"); deleteOwned("SMT_STAFF");
		jdbc.update("DELETE FROM SMT_DEVICE WHERE DEVICE_CODE LIKE ?", marker + "%");
		for (String runId : runIds) Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?", Integer.class, runId));
		System.out.println("LEGACY_INVENTORY_CLEANUP=marker:" + marker + ",ownedRows:0");
	}

	@AfterClass
	public static void closePool() {
		if (dataSource != null) dataSource.close();
		dataSource = null;
	}

	@Test
	public void sourceFlowIncludesPersonTypeOneAndExcludesVisitorTypeTwo() {
		long staff = own("SMT_STAFF", base + 101);
		long personAuthority = own("SMT_DEVICE_AUTHORITY", base + 201);
		long visitorAuthority = own("SMT_DEVICE_AUTHORITY", base + 202);
		long personSource = own("SMT_STAFF_DEVICE_AUTH", base + 1);
		long visitorSource = own("SMT_STAFF_DEVICE_AUTH", base + 2);
		jdbc.update("INSERT INTO SMT_STAFF(ID,BADGE,CREATE_TIME) VALUES(?,?,SYSTIMESTAMP)", staff, marker + "-badge");
		jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,AUTHORITY_NAME,TYPE,PARK_ID,CREATE_TIME) VALUES(?,?,?,?,SYSTIMESTAMP)",
				personAuthority, marker + "-person", DeviceAuthTypeEnum.PERSON.getCode(), parkId);
		jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,AUTHORITY_NAME,TYPE,PARK_ID,CREATE_TIME) VALUES(?,?,?,?,SYSTIMESTAMP)",
				visitorAuthority, marker + "-visitor", DeviceAuthTypeEnum.VISITOR.getCode(), parkId);
		jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,AUTH_TYPE) VALUES(?,?,?,SYSTIMESTAMP,?)",
				personSource, staff, personAuthority, 1);
		jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,AUTH_TYPE) VALUES(?,?,?,SYSTIMESTAMP,?)",
				visitorSource, staff, visitorAuthority, 1);

		ScanRun run = openRun(); restrict(run, FlowKind.CURRENT_SOURCE, base, visitorSource);
		ScanLease lease = claim(run, FlowKind.CURRENT_SOURCE);
		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		Assert.assertEquals(1, page.getRows().size()); Assert.assertEquals(Long.valueOf(personSource), page.getRows().get(0).getId());
		CommitResult committed = service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);
		Assert.assertEquals(CommitStatus.COMMITTED, committed.getStatus());
		String raw = jdbc.queryForObject("SELECT RAW_ROW_PAYLOAD FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?",
				String.class, "CURRENT_SOURCE:" + personSource);
		Assert.assertTrue(raw.contains("\"AUTH_TYPE\":1"));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?", Integer.class,
					"CURRENT_SOURCE:" + visitorSource));
	}

	@Test
	public void persistentMinimumParkStillClassifiesThreePhysicalFlowsAsConflictWhenTwoParksExist() {
		long staff = own("SMT_STAFF", base + 501);
		long authority = own("SMT_DEVICE_AUTHORITY", base + 502);
		long source = own("SMT_STAFF_DEVICE_AUTH", base + 503);
		String deviceCode = marker + "-conflict-code";
		String firstDevice = marker + "-conflict-a";
		String secondDevice = marker + "-conflict-b";
		jdbc.update("INSERT INTO SMT_STAFF(ID,BADGE,CREATE_TIME) VALUES(?,?,SYSTIMESTAMP)", staff, marker + "-conflict-staff");
		jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,AUTHORITY_NAME,TYPE,PARK_ID,CREATE_TIME) VALUES(?,?,?,?,SYSTIMESTAMP)",
				authority, marker + "-conflict-authority", DeviceAuthTypeEnum.PERSON.getCode(), parkId);
		jdbc.update("INSERT INTO SMT_DEVICE(ID,DEVICE_CODE,PARK_ID,IS_SYNC) VALUES(?,?,?,0)", firstDevice, deviceCode, parkId);
		jdbc.update("INSERT INTO SMT_DEVICE(ID,DEVICE_CODE,PARK_ID,IS_SYNC) VALUES(?,?,?,0)", secondDevice, deviceCode, parkId + 1);
		long firstRelation = own("SMT_DEVICE_AUTHORITY_RELATION", base + 504);
		long secondRelation = own("SMT_DEVICE_AUTHORITY_RELATION", base + 505);
		jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",
				firstRelation, authority, firstDevice, parkId);
		jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",
				secondRelation, authority, secondDevice, parkId + 1);
		jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,AUTH_TYPE) VALUES(?,?,?,SYSTIMESTAMP,1)",
				source, staff, authority);
		long iscDown = base + 506; insertIscDown(iscDown, 7, "conflict");
		long directDown = base + 507; insertDirectDown(directDown, 7, "conflict");
		jdbc.update("UPDATE SMT_ISC_DOWN_RECORD SET DEVICE_CODE=?,PARK_ID=? WHERE ID=?", deviceCode, parkId, iscDown);
		jdbc.update("UPDATE SMT_TASK_DOWN_RECORD SET DEVICE_CODE=?,PARK_ID=? WHERE ID=?", deviceCode, parkId, directDown);

		ScanRun run = openRun();
		Map<FlowKind, Long> rows = new LinkedHashMap<>();
		rows.put(FlowKind.CURRENT_SOURCE, source); rows.put(FlowKind.ISC_DOWN, iscDown); rows.put(FlowKind.DIRECT_DOWN, directDown);
		for (Map.Entry<FlowKind, Long> item : rows.entrySet()) {
			restrict(run, item.getKey(), item.getValue() - 1, item.getValue());
			ScanLease lease = claim(run, item.getKey());
			RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
			Assert.assertEquals(Integer.valueOf(2), page.getRows().get(0).getDeviceParkCount());
			service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);
		}
		Assert.assertEquals(Integer.valueOf(3), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE RUN_FLOW_ID IN (SELECT ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?) AND PARK_STATE='CONFLICT' AND PARK_ID IS NULL",
				Integer.class, run.getRunId()));
	}

	@Test
	public void fourLegacyTablesKeepIndependentRawServicesOneTwoSevenAndUnknownPhysicalState() throws Exception {
		for (int serviceType : new int[]{1, 2, 7}) {
			insertIscTask(base + 10 + serviceType, serviceType, "isc-task");
			insertIscDown(base + 20 + serviceType, serviceType, "isc-down");
			insertDirectTask(base + 30 + serviceType, serviceType, "direct-task");
			insertDirectDown(base + 40 + serviceType, serviceType, "direct-down");
		}
		ScanRun run = openRun();
		for (FlowKind kind : Arrays.asList(FlowKind.ISC_TASK, FlowKind.ISC_DOWN, FlowKind.DIRECT_TASK, FlowKind.DIRECT_DOWN)) {
			long low = base + (kind == FlowKind.ISC_TASK ? 10 : kind == FlowKind.ISC_DOWN ? 20 : kind == FlowKind.DIRECT_TASK ? 30 : 40);
			restrict(run, kind, low, low + 7);
			ScanLease lease = claim(run, kind);
			RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
			Assert.assertEquals(kind.name(), 3, page.getRows().size());
			service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);
		}
		Assert.assertEquals(Integer.valueOf(12), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE RUN_FLOW_ID IN (SELECT ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?)",
				Integer.class, run.getRunId()));
		Assert.assertEquals(Integer.valueOf(12), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE PHYSICAL_STATE='UNKNOWN' AND RUN_FLOW_ID IN (SELECT ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?)",
				Integer.class, run.getRunId()));
		Assert.assertEquals(Integer.valueOf(4), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE SERVICE_TYPE=2 AND SERVICE_FAMILY='APP_PERFECT_REVIEW' AND RUN_FLOW_ID IN (SELECT ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=?)",
				Integer.class, run.getRunId()));
		String raw = jdbc.queryForObject("SELECT RAW_ROW_PAYLOAD FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?",
				String.class, "ISC_TASK:" + (base + 17));
		String hash = jdbc.queryForObject("SELECT RAW_ROW_SHA256 FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?",
				String.class, "ISC_TASK:" + (base + 17));
		Assert.assertEquals(sha256(raw), hash); Assert.assertTrue(raw.contains("\"PERSON_ID\":null"));

		ReviewPage exceptional = service.readReviews(ReviewQuery.builder().afterId(0L).build(),
				ServerResolvedScope.globalException(1, "oracle-global", Collections.singleton(
						LegacyEmployeeAccessInventoryServiceImpl.GLOBAL_REVIEW_PERMISSION)), 200);
		Assert.assertFalse(exceptional.getRows().isEmpty());
	}

	@Test
	public void repeatedStableEvidenceTouchesOneRevisionAndChangedRawCreatesTheNext() {
		long taskId = base + 71; insertDirectTask(taskId, 7, "stable");
		ScanRun run = openRun(); restrict(run, FlowKind.DIRECT_TASK, base + 70, taskId);
		ScanLease lease = claim(run, FlowKind.DIRECT_TASK);
		RawPage idPage = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		lease = advanced(lease, service.commitPage(lease, lease.getCursor(), idPage, idPage.getNextCursor(), false));
		RawPage idEmpty = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		lease = advanced(lease, service.commitPage(lease, lease.getCursor(), idEmpty, idEmpty.getNextCursor(), true));
		Assert.assertEquals(ScanPass.UPDATE, lease.getActivePass());
		RawPage updatePage = service.readPage(lease, ScanPass.UPDATE, lease.getCursor(), 200);
		lease = advanced(lease, service.commitPage(lease, lease.getCursor(), updatePage, updatePage.getNextCursor(), false));
		Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?", Integer.class, "DIRECT_TASK:" + taskId));
		RawPage updateEmpty = service.readPage(lease, ScanPass.UPDATE, lease.getCursor(), 200);
		lease = advanced(lease, service.commitPage(lease, lease.getCursor(), updateEmpty, updateEmpty.getNextCursor(), true));
		Assert.assertEquals(ScanPass.REVISIT, lease.getActivePass());
		String oldRaw = jdbc.queryForObject("SELECT RAW_ROW_PAYLOAD FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=? AND REVISION_NO=1",
				String.class, "DIRECT_TASK:" + taskId);
		jdbc.update("UPDATE SMT_DEVICE_TASK SET REMARK=? WHERE ID=?", marker + "-changed", taskId);
		RawPage revisit = service.readPage(lease, ScanPass.REVISIT, lease.getCursor(), 200);
		service.commitPage(lease, lease.getCursor(), revisit, revisit.getNextCursor(), false);
		Assert.assertEquals(Integer.valueOf(2), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?", Integer.class, "DIRECT_TASK:" + taskId));
		String oldAfter = jdbc.queryForObject("SELECT RAW_ROW_PAYLOAD FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=? AND REVISION_NO=1",
				String.class, "DIRECT_TASK:" + taskId);
		String changed = jdbc.queryForObject("SELECT RAW_ROW_PAYLOAD FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=? AND REVISION_NO=2",
				String.class, "DIRECT_TASK:" + taskId);
		Assert.assertEquals(oldRaw, oldAfter); Assert.assertTrue(changed.contains(marker + "-changed"));
	}

	@Test
	public void openRunRecoversAllFiveRowsWithoutReactivatingAnAlreadyCompleteFlow() {
		ScanRun run = openRun();
		jdbc.update("UPDATE SMT_AUTH_LEGACY_SCAN_FLOW SET FLOW_STATE='COMPLETE',COMPLETED_AT=SYSTIMESTAMP,LEASE_OWNER=NULL,LEASE_TOKEN=NULL,LEASE_UNTIL=NULL WHERE RUN_ID=? AND FLOW_KIND='CURRENT_SOURCE'",
				run.getRunId());

		ScanRun recovered = service.openRun(scanRequest());

		Assert.assertTrue(recovered.isRecovered()); Assert.assertEquals(run.getRunId(), recovered.getRunId());
		Assert.assertEquals(5, recovered.getFlows().size());
		Assert.assertEquals("COMPLETE", jdbc.queryForObject(
				"SELECT FLOW_STATE FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=? AND FLOW_KIND='CURRENT_SOURCE'",
				String.class, run.getRunId()));
	}

	@Test
	public void commitWaitsForFlowLockThenRejectsTheLeaseUsingPostLockDatabaseTime() throws Exception {
		long taskId = base + 601; insertDirectTask(taskId, 7, "lock-wait");
		ScanRun run = openRun(); restrict(run, FlowKind.DIRECT_TASK, taskId - 1, taskId);
		ScanLease lease = service.claimFlow(run.getRunId(), FlowKind.DIRECT_TASK, marker + "-lock-worker", 2,
				run.getFlows().stream().filter(item -> item.getFlowKind() == FlowKind.DIRECT_TASK).findFirst().get().getRowVersion());
		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		String clientIdentifier = "legacy_wait_" + Long.toUnsignedString(System.nanoTime(), 36);
		Path request = requiredProbePath("SMART_AUTH_LEGACY_WAIT_REQUEST");
		Path evidence = requiredProbePath("SMART_AUTH_LEGACY_WAIT_EVIDENCE");
		Files.write(request, Arrays.asList(clientIdentifier, run.getRunId(), FlowKind.DIRECT_TASK.name()),
				StandardCharsets.UTF_8);
		LegacyEmployeeAccessInventoryService identifiedService = transactionalService(
				identifiedMapper(clientIdentifier, run.getRunId(), FlowKind.DIRECT_TASK));
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try (Connection blocker = dataSource.getConnection()) {
			blocker.setAutoCommit(false);
			try (PreparedStatement lock = blocker.prepareStatement(
					"SELECT ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=? AND FLOW_KIND='DIRECT_TASK' FOR UPDATE")) {
				lock.setString(1, run.getRunId());
				try (ResultSet ignored = lock.executeQuery()) { Assert.assertTrue(ignored.next()); }
			}
			Future<CommitResult> pending = executor.submit(() -> identifiedService.commitPage(
					lease, lease.getCursor(), page, page.getNextCursor(), false));
			String observed = awaitWaitEvidence(evidence, clientIdentifier, 15, TimeUnit.SECONDS);
			awaitDatabaseTimeAfter(lease.getLeaseUntil(), 5, TimeUnit.SECONDS);
			blocker.rollback();
			CommitResult result = pending.get(10, TimeUnit.SECONDS);
			Assert.assertEquals(CommitStatus.STALE_LEASE, result.getStatus());
			Assert.assertTrue(observed.contains("|enq: TX - row lock contention|WAITING|Application|"));
			System.out.println("LEGACY_INVENTORY_LOCK_WAIT_EVIDENCE=" + observed);
		} finally {
			executor.shutdownNow(); executor.awaitTermination(5, TimeUnit.SECONDS);
		}
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?", Integer.class, "DIRECT_TASK:" + taskId));
	}

	@Test
	public void reviewWorkCrossingLeaseExpiryRollsBackBeforeCursorCanAdvance() throws Exception {
		long taskId = base + 701; insertDirectTask(taskId, 7, "cross-lease");
		ScanRun run = openRun(); restrict(run, FlowKind.DIRECT_TASK, taskId - 1, taskId);
		ScanLease lease = service.claimFlow(run.getRunId(), FlowKind.DIRECT_TASK, marker + "-slow-worker", 2,
				run.getFlows().stream().filter(item -> item.getFlowKind() == FlowKind.DIRECT_TASK).findFirst().get().getRowVersion());
		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		LegacyEmployeeAccessInventoryMapper slowMapper = (LegacyEmployeeAccessInventoryMapper) Proxy.newProxyInstance(
				LegacyEmployeeAccessInventoryMapper.class.getClassLoader(),
				new Class<?>[]{LegacyEmployeeAccessInventoryMapper.class}, (proxy, method, args) -> {
					if ("lockReviewRevisions".equals(method.getName())) Thread.sleep(2300L);
					try {
						return method.invoke(mapper, args);
					} catch (InvocationTargetException failure) {
						throw failure.getCause();
					}
				});
		LegacyEmployeeAccessInventoryService slowService = transactionalService(slowMapper);
		try {
			slowService.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);
			Assert.fail("租约跨越事务内review写入后必须由执行时数据库时间阻止cursor CAS");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("同事务CAS失败"));
		}
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(*) FROM SMT_AUTH_LEGACY_REVIEW WHERE LEGACY_REF=?", Integer.class, "DIRECT_TASK:" + taskId));
		Assert.assertEquals(Long.valueOf(taskId - 1), jdbc.queryForObject(
				"SELECT ID_LAST_ID FROM SMT_AUTH_LEGACY_SCAN_FLOW WHERE RUN_ID=? AND FLOW_KIND='DIRECT_TASK'",
				Long.class, run.getRunId()));
	}

	private ScanRun openRun() {
		ScanRun run = service.openRun(scanRequest());
		runIds.add(run.getRunId()); return run;
	}

	private InventoryScanRequest scanRequest() {
		return InventoryScanRequest.builder().requestedBy("oracle-inventory")
				.scopeFingerprint(String.join("", Collections.nCopies(64, "b"))).auditTicket(marker).pageSize(200).build();
	}

	private LegacyEmployeeAccessInventoryService transactionalService(LegacyEmployeeAccessInventoryMapper targetMapper) {
		LegacyEmployeeAccessInventoryServiceImpl target = new LegacyEmployeeAccessInventoryServiceImpl(
				targetMapper, new LegacyInventoryCanonicalizer(), true);
		ProxyFactory proxy = new ProxyFactory(target); proxy.setInterfaces(LegacyEmployeeAccessInventoryService.class);
		proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(dataSource),
				new AnnotationTransactionAttributeSource()));
		return (LegacyEmployeeAccessInventoryService) proxy.getProxy();
	}

	private LegacyEmployeeAccessInventoryMapper identifiedMapper(String clientIdentifier,
			String runId, FlowKind flowKind) {
		AtomicBoolean identifierSet = new AtomicBoolean(false);
		return (LegacyEmployeeAccessInventoryMapper) Proxy.newProxyInstance(
				LegacyEmployeeAccessInventoryMapper.class.getClassLoader(),
				new Class<?>[]{LegacyEmployeeAccessInventoryMapper.class}, (proxy, method, args) -> {
					boolean targetLock = "lockFlow".equals(method.getName()) && args != null && args.length == 2
							&& Objects.equals(runId, args[0]) && Objects.equals(flowKind.name(), args[1]);
					if (targetLock) {
						setClientIdentifier(clientIdentifier);
						identifierSet.set(true);
					}
					try {
						Object result = method.invoke(mapper, args);
						if ("now".equals(method.getName()) && identifierSet.compareAndSet(true, false)) {
							clearClientIdentifier();
						}
						return result;
					} catch (InvocationTargetException failure) {
						if (identifierSet.compareAndSet(true, false)) clearClientIdentifier();
						throw failure.getCause();
					}
				});
	}

	private void setClientIdentifier(String clientIdentifier) {
		jdbc.execute((ConnectionCallback<Void>) connection -> {
			try (CallableStatement call = connection.prepareCall("BEGIN DBMS_SESSION.SET_IDENTIFIER(?); END;")) {
				call.setString(1, clientIdentifier); call.execute();
			}
			return null;
		});
	}

	private void clearClientIdentifier() {
		jdbc.execute((ConnectionCallback<Void>) connection -> {
			try (CallableStatement call = connection.prepareCall("BEGIN DBMS_SESSION.CLEAR_IDENTIFIER; END;")) {
				call.execute();
			}
			return null;
		});
	}

	private Path requiredProbePath(String name) {
		String value = System.getenv(name);
		Assert.assertNotNull(name + "必须由固定runner提供", value);
		Path path = Paths.get(value);
		Assert.assertTrue(name + "必须为绝对路径", path.isAbsolute());
		Assert.assertTrue(name + "必须位于测试临时目录", path.toAbsolutePath().normalize().startsWith(testTempRoot()));
		return path;
	}

	private static Path testTempRoot() {
		String configured = System.getenv("SMART_AUTH_TEST_TMPDIR");
		return Paths.get(configured == null || configured.trim().isEmpty() ? System.getProperty("java.io.tmpdir") : configured)
				.toAbsolutePath().normalize();
	}

	private String awaitWaitEvidence(Path evidence, String clientIdentifier, long timeout, TimeUnit unit) throws Exception {
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		while (System.nanoTime() < deadline) {
			if (Files.isRegularFile(evidence)) {
				String observed = new String(Files.readAllBytes(evidence), StandardCharsets.UTF_8).trim();
				if (observed.startsWith("WAIT|" + clientIdentifier + "|")) return observed;
			}
			Thread.sleep(25L);
		}
		Assert.fail("未观察到当前fixture目标flow的Oracle row-lock wait证据");
		return null;
	}

	private void awaitDatabaseTimeAfter(LocalDateTime leaseUntil, long timeout, TimeUnit unit) throws Exception {
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		while (System.nanoTime() < deadline) {
			LocalDateTime databaseNow = mapper.now();
			if (databaseNow != null && databaseNow.isAfter(leaseUntil)) return;
			Thread.sleep(25L);
		}
		Assert.fail("已观察锁等待，但数据库时间未在有界窗口跨过leaseUntil");
	}

	private void restrict(ScanRun run, FlowKind kind, long last, long high) {
		jdbc.update("UPDATE SMT_AUTH_LEGACY_SCAN_FLOW SET ID_LAST_ID=?,ID_HIGH_WATER=?,REVISIT_LAST_ID=?,REVISIT_HIGH_WATER_ID=? WHERE RUN_ID=? AND FLOW_KIND=?",
				last, high, last, high, run.getRunId(), kind.name());
	}

	private ScanLease claim(ScanRun run, FlowKind kind) {
		long version = run.getFlows().stream().filter(item -> item.getFlowKind() == kind).findFirst().get().getRowVersion();
		return service.claimFlow(run.getRunId(), kind, marker + "-worker", 120, version);
	}

	private ScanLease advanced(ScanLease lease, CommitResult result) {
		return lease.toBuilder().rowVersion(result.getRowVersion()).activePass(result.getActivePass())
				.cursor(result.getCursor()).build();
	}

	private void insertIscTask(long id, int service, String suffix) {
		own("SMT_ISC_DEVICE_TASK", id);
		jdbc.update("INSERT INTO SMT_ISC_DEVICE_TASK(ID,ACTION,STATUS,DEVICE_TYPE,DEVICE_CODE,CARD_NO,SERVICE_TYPE,REMARK,CREATE_TIME,UPDATE_TIME) VALUES(?,1,0,1,?,?,?, ?,SYSTIMESTAMP-1,SYSTIMESTAMP-1/24)",
				id, marker + "-missing-device", marker + "-missing-staff", service, marker + "-" + suffix + "-" + service);
	}

	private void insertIscDown(long id, int service, String suffix) {
		own("SMT_ISC_DOWN_RECORD", id);
		jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,DEVICE_CODE,CARD_NO,SERVICE_TYPE,REMARK,CREATE_TIME) VALUES(?,?,1,1,?,?,?,?,SYSTIMESTAMP-1)",
				id, parkId, marker + "-missing-device", marker + "-missing-staff", service, marker + "-" + suffix + "-" + service);
	}

	private void insertDirectTask(long id, int service, String suffix) {
		own("SMT_DEVICE_TASK", id);
		jdbc.update("INSERT INTO SMT_DEVICE_TASK(ID,ACTION,STATUS,DEVICE_TYPE,DEVICE_CODE,CARD_NO,SERVICE_TYPE,REMARK,CREATE_TIME,UPDATE_TIME) VALUES(?,1,0,1,?,?,?,?,SYSTIMESTAMP-1,SYSTIMESTAMP-1/24)",
				id, marker + "-missing-device", marker + "-missing-staff", service, marker + "-" + suffix + "-" + service);
	}

	private void insertDirectDown(long id, int service, String suffix) {
		own("SMT_TASK_DOWN_RECORD", id);
		jdbc.update("INSERT INTO SMT_TASK_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,DEVICE_CODE,CARD_NO,SERVICE_TYPE,REMARK,CREATE_TIME) VALUES(?,?,1,1,?,?,?,?,SYSTIMESTAMP-1)",
				id, parkId, marker + "-missing-device", marker + "-missing-staff", service, marker + "-" + suffix + "-" + service);
	}

	private long own(String table, long id) { owned.computeIfAbsent(table, ignored -> new ArrayList<>()).add(id); return id; }

	private void deleteOwned(String table) {
		for (Long id : owned.getOrDefault(table, Collections.emptyList())) jdbc.update("DELETE FROM " + table + " WHERE ID=?", id);
	}

	private void assertSchemaAlreadyExists() {
		Assert.assertEquals(Integer.valueOf(2), jdbc.queryForObject(
				"SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('SMT_AUTH_LEGACY_SCAN_FLOW','SMT_AUTH_LEGACY_REVIEW')", Integer.class));
		Assert.assertEquals(Integer.valueOf(31), jdbc.queryForObject(
				"SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_LEGACY_SCAN_FLOW'", Integer.class));
		Assert.assertEquals(Integer.valueOf(47), jdbc.queryForObject(
				"SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_LEGACY_REVIEW'", Integer.class));
		System.out.println("LEGACY_INVENTORY_ORACLE_VERSION=" + jdbc.queryForObject(
				"SELECT BANNER_FULL FROM V$VERSION WHERE ROWNUM=1", String.class).replace('\n', ' '));
	}

	private static String sha256(String value) throws Exception {
		byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		StringBuilder result = new StringBuilder(); for (byte item : digest) result.append(String.format("%02x", item & 255));
		return result.toString();
	}
}
