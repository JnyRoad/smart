package com.tce.smart.platform.core.client.release;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 保密物品放行仓储的真实 Oracle 集成测试。
 *
 * 默认测试套件不会连接数据库。只有显式开启开关后才读取被忽略的本机配置；
 * 此时缺少配置必须失败，不能跳过后误报通过。
 */
public class JdbcConfidentialReleaseStoreOracleTest {

	private static final String ENABLE_PROPERTY = "smart.client.008.oracle.test";
	private static final String ENV_FILE_PROPERTY = "smart.client.008.oracle.envFile";
	private static final String RELEASE_SQL = "db/client008/V001__release.sql";
	private static final String EXPECTED_USER = "SMART_CLIENT_008";
	private static final String EXPECTED_PDB = "FREEPDB1";
	private static final String PREFIX = "R" + UUID.randomUUID().toString().replace("-", "").substring(0, 18);
	private static final Instant BASE_TIME = Instant.parse("2026-09-05T10:00:00Z");

	private static DataSource dataSource;

	@BeforeClass
	public static void connectAndInitializeSchema() throws Exception {
		dataSource = null;
		Assume.assumeTrue("Oracle 集成测试默认关闭", Boolean.parseBoolean(System.getProperty(ENABLE_PROPERTY, "false")));
		String envFile = System.getProperty(ENV_FILE_PROPERTY);
		if (envFile == null || envFile.trim().isEmpty()) {
			Assert.fail("显式启用 Oracle 集成测试时必须设置 " + ENV_FILE_PROPERTY);
		}

		Properties local = loadLocalEnvironment(Paths.get(envFile));
		String port = required(local, "SMART_CLIENT_008_ORACLE_HOST_PORT");
		String user = required(local, "SMART_CLIENT_008_ORACLE_APP_USER");
		String password = required(local, "SMART_CLIENT_008_ORACLE_APP_PASSWORD");
		Assert.assertEquals("Oracle 端口不属于本任务目标", "15218", port);
		Assert.assertEquals(EXPECTED_USER, user);
		Class.forName("oracle.jdbc.OracleDriver");
		DataSource candidate = new DriverManagerDataSource(
				"jdbc:oracle:thin:@//127.0.0.1:" + port + "/" + EXPECTED_PDB, user, password);

		try (Connection connection = candidate.getConnection()) {
			verifyTarget(connection);
			initializeOrVerifyReleaseTables(connection);
		}
		// 只有目标与结构校验全部通过，清理阶段才允许取得连接。
		dataSource = candidate;
	}

	@AfterClass
	public static void cleanOwnRows() throws Exception {
		if (dataSource == null) {
			return;
		}
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			try {
				deleteByPrefix(connection, "DELETE FROM SMT_CLIENT_RELEASE_COMMAND WHERE SERVICE_SCOPE LIKE ?", PREFIX + "%");
				deleteByPrefix(connection, "DELETE FROM SMT_CLIENT_RELEASE_EVENT WHERE RELEASE_ID LIKE ?", PREFIX + "%");
				deleteByPrefix(connection, "DELETE FROM SMT_CLIENT_RELEASE WHERE RELEASE_ID LIKE ?", PREFIX + "%");
				connection.commit();
			} catch (Exception error) {
				connection.rollback();
				throw error;
			}
		}
	}

	@Test
	public void newStoreInstancesRestoreAndCompleteApproveRejectDepartArriveFlows() throws Exception {
		String releaseId = PREFIX + "_FLOW";
		ReleaseApplicationRequest request = request("技术资料");
		ReleaseCreationContext creation = ReleaseCreationContext.verified(
				principal(PREFIX + "_APPLICANT", "item-pass:apply"), posts("POST-A", "POST-B"),
				PREFIX + "_APPROVER");

		ConfidentialRelease pending = store().create(PREFIX + "_CREATE_SCOPE", "create-1", request,
				creation, BASE_TIME, releaseId, PREFIX + "_EVT_CREATE");
		Assert.assertEquals(ReleaseStatus.PENDING, pending.getStatus());

		ConfidentialRelease approved = store().approve(PREFIX + "_APPROVE_SCOPE", "approve-1", releaseId,
				principal(PREFIX + "_APPROVER", "item-pass:approve"), 1L, BASE_TIME.plusSeconds(1),
				PREFIX + "_EVT_APPROVE");
		Assert.assertEquals(ReleaseStatus.APPROVED, approved.getStatus());

		ConfidentialRelease transporting = store().depart(PREFIX + "_DEPART_SCOPE", "depart-1", releaseId,
				executionPrincipal(PREFIX + "_SECURITY_A", "POST-A"), 2L, EscortMode.POSITIONING_LOCK,
				"00042", securityEvidence(releaseId, "POST-A", ReleaseAction.DEPART,
						PREFIX + "_SECURITY_A", BASE_TIME.plusSeconds(2)), null, BASE_TIME.plusSeconds(2),
				PREFIX + "_EVT_DEPART");
		Assert.assertEquals(ReleaseStatus.TRANSPORTING, transporting.getStatus());

		ConfidentialRelease completed = store().arrive(PREFIX + "_ARRIVE_SCOPE", "arrive-1", releaseId,
				executionPrincipal(PREFIX + "_SECURITY_B", "POST-B"), 3L, EscortMode.POSITIONING_LOCK,
				"00042", securityEvidence(releaseId, "POST-B", ReleaseAction.ARRIVE,
						PREFIX + "_SECURITY_B", BASE_TIME.plusSeconds(3)), null, BASE_TIME.plusSeconds(3),
				PREFIX + "_EVT_ARRIVE");
		Assert.assertEquals(ReleaseStatus.COMPLETED, completed.getStatus());

		ConfidentialRelease restored = store().find(releaseId);
		Assert.assertNotNull(restored);
		Assert.assertEquals(4L, restored.getVersion());
		Assert.assertEquals(4, restored.getAuditTrail().size());
		Assert.assertEquals(Arrays.asList("00017", "SEAL-2"), restored.getSealCodes());
		Assert.assertEquals("00042", restored.getPositioningLockId());
		Assert.assertEquals(ReleaseAction.ARRIVE, restored.getAuditTrail().get(3).getAction());
		Assert.assertEquals(CardRole.SECURITY_CHECK,
				restored.getAuditTrail().get(3).getSecurityEvidence().getRole());

		String rejectedId = PREFIX + "_REJECT";
		store().create(PREFIX + "_CREATE_SCOPE", "create-reject", request("待驳回资料"), creation,
				BASE_TIME, rejectedId, PREFIX + "_EVT_CREATE_REJECT");
		ConfidentialRelease rejected = store().reject(PREFIX + "_REJECT_SCOPE", "reject-1", rejectedId,
				principal(PREFIX + "_APPROVER", "item-pass:approve"), 1L, "资料不完整",
				BASE_TIME.plusSeconds(1), PREFIX + "_EVT_REJECT");
		Assert.assertEquals(ReleaseStatus.REJECTED, rejected.getStatus());
		Assert.assertEquals("资料不完整", rejected.getAuditTrail().get(1).getReason());
	}

	@Test
	public void recentListReturnsOnlyPersistedSnapshotsWithinBoundedLimit() throws Exception {
		String firstId = PREFIX + "_LIST_A";
		String secondId = PREFIX + "_LIST_B";
		ReleaseCreationContext context = ReleaseCreationContext.verified(
				principal(PREFIX + "_LIST_APPLICANT", "item-pass:apply"), posts("POST-A", "POST-B"),
				PREFIX + "_LIST_APPROVER");
		store().create(PREFIX + "_LIST_SCOPE_A", "list-a", request("列表A"), context,
				BASE_TIME.plusSeconds(10000), firstId, PREFIX + "_LIST_EVENT_A");
		store().create(PREFIX + "_LIST_SCOPE_B", "list-b", request("列表B"), context,
				BASE_TIME.plusSeconds(10001), secondId, PREFIX + "_LIST_EVENT_B");

		List<ConfidentialRelease> one = store().listRecent(1);
		Assert.assertEquals(1, one.size());
		Assert.assertEquals(secondId, one.get(0).getReleaseId());
		List<ConfidentialRelease> all = store().listRecent(200);
		Assert.assertTrue(all.stream().anyMatch(item -> firstId.equals(item.getReleaseId())));
		Assert.assertTrue(all.stream().anyMatch(item -> secondId.equals(item.getReleaseId())));
		try {
			store().listRecent(0);
			Assert.fail("数量为零必须拒绝");
		} catch (IllegalArgumentException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void concurrentSameCreateKeyReturnsOneOriginalResultWithoutDuplicateRows() throws Exception {
		String actorId = PREFIX + "_SAME_ACTOR";
		String scope = PREFIX + "_SAME_SCOPE";
		ReleaseCreationContext context = ReleaseCreationContext.verified(
				principal(actorId, "item-pass:apply"), posts("POST-A", "POST-B"), PREFIX + "_APPROVER");
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<ConfidentialRelease> first = executor.submit(createTask(ready, start, scope, "same-key",
					PREFIX + "_SAME_A", PREFIX + "_SAME_EVENT_A", context));
			Future<ConfidentialRelease> second = executor.submit(createTask(ready, start, scope, "same-key",
					PREFIX + "_SAME_B", PREFIX + "_SAME_EVENT_B", context));
			Assert.assertTrue("并发任务未及时就绪", ready.await(10, TimeUnit.SECONDS));
			start.countDown();

			ConfidentialRelease firstResult = first.get(30, TimeUnit.SECONDS);
			ConfidentialRelease secondResult = second.get(30, TimeUnit.SECONDS);
			Assert.assertEquals(firstResult.getReleaseId(), secondResult.getReleaseId());
			Assert.assertEquals(firstResult.getAuditTrail().get(0).getEventId(),
					secondResult.getAuditTrail().get(0).getEventId());
			Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE WHERE RELEASE_ID IN (?, ?)",
					PREFIX + "_SAME_A", PREFIX + "_SAME_B"));
			Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_EVENT WHERE RELEASE_ID IN (?, ?)",
					PREFIX + "_SAME_A", PREFIX + "_SAME_B"));
			Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_COMMAND WHERE SERVICE_SCOPE = ?",
					scope));
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void concurrentDifferentKeysOnSameVersionAllowOnlyOneTransition() throws Exception {
		String releaseId = PREFIX + "_CAS";
		String actorId = PREFIX + "_CAS_APPROVER";
		createPending(releaseId, actorId, "cas-create");
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<ConfidentialRelease> first = executor.submit(approveTask(ready, start, releaseId, actorId,
					"cas-key-a", PREFIX + "_CAS_EVENT_A"));
			Future<ConfidentialRelease> second = executor.submit(approveTask(ready, start, releaseId, actorId,
					"cas-key-b", PREFIX + "_CAS_EVENT_B"));
			Assert.assertTrue("并发任务未及时就绪", ready.await(10, TimeUnit.SECONDS));
			start.countDown();

			int success = 0;
			int versionConflict = 0;
			for (Future<ConfidentialRelease> future : Arrays.asList(first, second)) {
				try {
					Assert.assertEquals(2L, future.get(30, TimeUnit.SECONDS).getVersion());
					success++;
				} catch (ExecutionException error) {
					Throwable cause = error.getCause();
					if (cause instanceof ReleaseRuleViolation
							&& ((ReleaseRuleViolation) cause).getCode() == ReleaseRuleViolation.Code.VERSION_CONFLICT) {
						versionConflict++;
					} else {
						throw error;
					}
				}
			}
			Assert.assertEquals(1, success);
			Assert.assertEquals(1, versionConflict);
			Assert.assertEquals(2L, store().find(releaseId).getVersion());
			Assert.assertEquals(2L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_EVENT WHERE RELEASE_ID = ?", releaseId));
			Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_COMMAND "
					+ "WHERE SERVICE_SCOPE = ?", PREFIX + "_CAS_APPROVE"));
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void oldKeyReturnsOriginalResponseButCurrentPermissionAndPostRemainRequired() throws Exception {
		String releaseId = PREFIX + "_OLD_KEY";
		String approverId = PREFIX + "_OLD_APPROVER";
		createPending(releaseId, approverId, "old-create");
		ConfidentialRelease originalApproval = store().approve(PREFIX + "_OLD_APPROVE", "old-approve",
				releaseId, principal(approverId, "item-pass:approve"), 1L, BASE_TIME.plusSeconds(1),
				PREFIX + "_OLD_APPROVE_EVENT");
		store().depart(PREFIX + "_OLD_DEPART", "old-depart", releaseId,
				executionPrincipal(PREFIX + "_OLD_SECURITY", "POST-A"), 2L, EscortMode.POSITIONING_LOCK,
				"LOCK-OLD", securityEvidence(releaseId, "POST-A", ReleaseAction.DEPART,
						PREFIX + "_OLD_SECURITY", BASE_TIME.plusSeconds(2)), null, BASE_TIME.plusSeconds(2),
				PREFIX + "_OLD_DEPART_EVENT");

		ConfidentialRelease replayed = store().approve(PREFIX + "_OLD_APPROVE", "old-approve", releaseId,
				principal(approverId, "item-pass:approve"), 1L, BASE_TIME.plusSeconds(50), PREFIX + "_IGNORED_EVENT");
		Assert.assertEquals(originalApproval.getVersion(), replayed.getVersion());
		Assert.assertEquals(originalApproval.getStatus(), replayed.getStatus());
		Assert.assertEquals(3L, store().find(releaseId).getVersion());

		expectRule(ReleaseRuleViolation.Code.MISSING_PERMISSION, () -> store().approve(
				PREFIX + "_OLD_APPROVE", "old-approve", releaseId, principal(approverId), 1L,
				BASE_TIME.plusSeconds(51), PREFIX + "_IGNORED_EVENT_2"));

		ConfidentialRelease originalDepart = store().depart(PREFIX + "_OLD_DEPART", "old-depart", releaseId,
				executionPrincipal(PREFIX + "_OLD_SECURITY", "POST-A"), 2L, EscortMode.POSITIONING_LOCK,
				"LOCK-OLD", securityEvidence(releaseId, "POST-A", ReleaseAction.DEPART,
						PREFIX + "_OLD_SECURITY", BASE_TIME.plusSeconds(2)), null, BASE_TIME.plusSeconds(90),
				PREFIX + "_IGNORED_DEPART_EVENT");
		Assert.assertEquals(3L, originalDepart.getVersion());
		expectRule(ReleaseRuleViolation.Code.UNAUTHORIZED_POST, () -> store().depart(
				PREFIX + "_OLD_DEPART", "old-depart", releaseId,
				ReleasePrincipal.authenticated(PREFIX + "_OLD_SECURITY", posts("item-pass:execute"),
						Collections.<String>emptySet()), 2L, EscortMode.POSITIONING_LOCK, "LOCK-OLD",
				securityEvidence(releaseId, "POST-A", ReleaseAction.DEPART,
						PREFIX + "_OLD_SECURITY", BASE_TIME.plusSeconds(2)), null, BASE_TIME.plusSeconds(91),
				PREFIX + "_IGNORED_DEPART_EVENT_2"));
	}

	@Test
	public void sameKeyWithDifferentRequestIsRejectedAndKeepsOriginalRelease() throws Exception {
		String actorId = PREFIX + "_DIFF_ACTOR";
		String scope = PREFIX + "_DIFF_SCOPE";
		ReleaseCreationContext context = ReleaseCreationContext.verified(
				principal(actorId, "item-pass:apply"), posts("POST-A", "POST-B"), PREFIX + "_DIFF_APPROVER");
		ConfidentialRelease original = store().create(scope, "same-key", request("原始标题"), context,
				BASE_TIME, PREFIX + "_DIFF_ORIGINAL", PREFIX + "_DIFF_EVENT");

		expectIdempotencyConflict(() -> store().create(scope, "same-key", request("不同标题"), context,
				BASE_TIME.plusSeconds(1), PREFIX + "_DIFF_OTHER", PREFIX + "_DIFF_OTHER_EVENT"));
		Assert.assertEquals(original.getReleaseId(), store().find(original.getReleaseId()).getReleaseId());
		Assert.assertNull(store().find(PREFIX + "_DIFF_OTHER"));
		Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_COMMAND WHERE SERVICE_SCOPE = ?", scope));
	}

	@Test
	public void eventConstraintFailureRollsBackSnapshotAndCommandThenAllowsCleanRetry() throws Exception {
		String releaseId = PREFIX + "_ROLLBACK";
		String approverId = PREFIX + "_ROLLBACK_APPROVER";
		String duplicateEventId = PREFIX + "_ROLLBACK_EVENT";
		createPending(releaseId, approverId, "rollback-create", duplicateEventId);

		expectSqlFailure("SMT_CREL_EVT_PK", () -> store().approve(PREFIX + "_ROLLBACK_APPROVE", "rollback-key", releaseId,
				principal(approverId, "item-pass:approve"), 1L, BASE_TIME.plusSeconds(1), duplicateEventId));
		Assert.assertEquals(1L, store().find(releaseId).getVersion());
		Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_EVENT WHERE RELEASE_ID = ?", releaseId));
		Assert.assertEquals(0L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_COMMAND WHERE SERVICE_SCOPE = ?",
				PREFIX + "_ROLLBACK_APPROVE"));

		ConfidentialRelease approved = store().approve(PREFIX + "_ROLLBACK_APPROVE", "rollback-key", releaseId,
				principal(approverId, "item-pass:approve"), 1L, BASE_TIME.plusSeconds(2),
				PREFIX + "_ROLLBACK_RETRY_EVENT");
		Assert.assertEquals(2L, approved.getVersion());
	}

	@Test
	public void domainRejectionLeavesNoSnapshotEventOrCommandFragments() throws Exception {
		String releaseId = PREFIX + "_DOMAIN_REJECT";
		String approverId = PREFIX + "_DOMAIN_APPROVER";
		createPending(releaseId, approverId, "domain-create");

		expectRule(ReleaseRuleViolation.Code.MISSING_PERMISSION, () -> store().approve(
				PREFIX + "_DOMAIN_APPROVE", "domain-key", releaseId, principal(approverId), 1L,
				BASE_TIME.plusSeconds(1), PREFIX + "_DOMAIN_EVENT"));
		Assert.assertEquals(1L, store().find(releaseId).getVersion());
		Assert.assertEquals(1L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_EVENT WHERE RELEASE_ID = ?", releaseId));
		Assert.assertEquals(0L, count("SELECT COUNT(*) FROM SMT_CLIENT_RELEASE_COMMAND WHERE SERVICE_SCOPE = ?",
				PREFIX + "_DOMAIN_APPROVE"));
	}

	@Test
	public void restoredEscortCardEvidenceRetainsAllExplicitFields() throws Exception {
		String releaseId = PREFIX + "_ESCORT";
		String approverId = PREFIX + "_ESCORT_APPROVER";
		String operatorId = PREFIX + "_ESCORT_SECURITY";
		createPending(releaseId, approverId, "escort-create");
		store().approve(PREFIX + "_ESCORT_APPROVE", "escort-approve", releaseId,
				principal(approverId, "item-pass:approve"), 1L, BASE_TIME.plusSeconds(1), PREFIX + "_ESCORT_APPROVE_EVENT");
		Instant now = BASE_TIME.plusSeconds(2);
		CardEvidence security = securityEvidence(releaseId, "POST-A", ReleaseAction.DEPART, operatorId, now);
		CardEvidence escort = CardEvidence.verified(PREFIX + "_ESCORT_CARD", CardRole.ESCORT,
				PREFIX + "_ESCORT_HOLDER", releaseId, "POST-A", ReleaseAction.DEPART, operatorId,
				now.minusSeconds(4), now.plusSeconds(50));
		store().depart(PREFIX + "_ESCORT_DEPART", "escort-depart", releaseId,
				executionPrincipal(operatorId, "POST-A"), 2L, EscortMode.ESCORT_CARD, null, security, escort,
				now, PREFIX + "_ESCORT_DEPART_EVENT");

		ReleaseAuditEvent restored = store().find(releaseId).getAuditTrail().get(2);
		Assert.assertEquals(CardRole.ESCORT, restored.getEscortEvidence().getRole());
		Assert.assertEquals(PREFIX + "_ESCORT_HOLDER", restored.getEscortEvidence().getHolderId());
		Assert.assertEquals(now.minusSeconds(4), restored.getEscortEvidence().getVerifiedAt());
		Assert.assertEquals(now.plusSeconds(50), restored.getEscortEvidence().getValidUntil());
	}

	@Test
	public void persistenceBoundaryUsesDomainNormalizedIdentifiers() throws Exception {
		String releaseId = PREFIX + "_NORMALIZED";
		String approverId = PREFIX + "_NORMALIZED_APPROVER";
		ReleaseCreationContext context = ReleaseCreationContext.verified(
				principal(PREFIX + "_NORMALIZED_APPLICANT", "item-pass:apply"), posts("POST-A", "POST-B"),
				approverId);

		ConfidentialRelease pending = store().create("  " + PREFIX + "_NORMALIZED_CREATE  ",
				"  normalized-create  ", request("规范化边界"), context, BASE_TIME,
				"  " + releaseId + "  ", "  " + PREFIX + "_NORMALIZED_CREATE_EVENT  ");
		Assert.assertEquals(releaseId, pending.getReleaseId());
		ConfidentialRelease approved = store().approve("  " + PREFIX + "_NORMALIZED_APPROVE  ",
				"  normalized-approve  ", "  " + releaseId + "  ",
				principal("  " + approverId + "  ", "item-pass:approve"), 1L, BASE_TIME.plusSeconds(1),
				"  " + PREFIX + "_NORMALIZED_APPROVE_EVENT  ");

		Assert.assertEquals(ReleaseStatus.APPROVED, approved.getStatus());
		Assert.assertEquals(2L, store().find("  " + releaseId + "  ").getVersion());
		Assert.assertEquals(approverId, approved.getAuditTrail().get(1).getActorId());
		ConfidentialRelease retried = store().approve(PREFIX + "_NORMALIZED_APPROVE",
				"normalized-approve", releaseId, principal(approverId, "item-pass:approve"),
				1L, BASE_TIME.plusSeconds(2), PREFIX + "_NORMALIZED_RETRY_EVENT");
		Assert.assertEquals(approved.getAuditTrail().get(1).getEventId(),
				retried.getAuditTrail().get(1).getEventId());
	}

	private Callable<ConfidentialRelease> createTask(CountDownLatch ready, CountDownLatch start,
			String scope, String key, String releaseId, String eventId, ReleaseCreationContext context) {
		return () -> {
			ready.countDown();
			Assert.assertTrue(start.await(10, TimeUnit.SECONDS));
			return store().create(scope, key, request("并发同键"), context, BASE_TIME, releaseId, eventId);
		};
	}

	private Callable<ConfidentialRelease> approveTask(CountDownLatch ready, CountDownLatch start,
			String releaseId, String actorId, String key, String eventId) {
		return () -> {
			ready.countDown();
			Assert.assertTrue(start.await(10, TimeUnit.SECONDS));
			return store().approve(PREFIX + "_CAS_APPROVE", key, releaseId,
					principal(actorId, "item-pass:approve"), 1L, BASE_TIME.plusSeconds(1), eventId);
		};
	}

	private ConfidentialRelease createPending(String releaseId, String approverId, String key) throws Exception {
		return createPending(releaseId, approverId, key, PREFIX + "_CREATE_EVENT_" + key.toUpperCase());
	}

	private ConfidentialRelease createPending(String releaseId, String approverId, String key, String eventId)
			throws Exception {
		ReleaseCreationContext context = ReleaseCreationContext.verified(
				principal(PREFIX + "_APPLICANT_" + key, "item-pass:apply"), posts("POST-A", "POST-B"), approverId);
		return store().create(PREFIX + "_CREATE_" + key, key, request("测试单据"), context,
				BASE_TIME, releaseId, eventId);
	}

	private long count(String sql, String... parameters) throws Exception {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int index = 0; index < parameters.length; index++) {
				statement.setString(index + 1, parameters[index]);
			}
			try (ResultSet rows = statement.executeQuery()) {
				Assert.assertTrue(rows.next());
				return rows.getLong(1);
			}
		}
	}

	private void expectRule(ReleaseRuleViolation.Code code, CheckedAction action) throws Exception {
		try {
			action.run();
			Assert.fail("预期领域规则拒绝：" + code);
		} catch (ReleaseRuleViolation error) {
			Assert.assertEquals(code, error.getCode());
		}
	}

	private void expectIdempotencyConflict(CheckedAction action) throws Exception {
		try {
			action.run();
			Assert.fail("预期幂等请求冲突");
		} catch (JdbcConfidentialReleaseStore.IdempotencyConflictException expected) {
			Assert.assertNotNull(expected.getMessage());
		}
	}

	private void expectSqlFailure(String constraint, CheckedAction action) throws Exception {
		try {
			action.run();
			Assert.fail("预期数据库约束失败");
		} catch (SQLException expected) {
			Assert.assertTrue("应保留真实失败约束，不得吞成幂等命中",
					expected.getMessage() != null && expected.getMessage().contains(constraint));
		}
	}

	private interface CheckedAction {
		void run() throws Exception;
	}

	private static JdbcConfidentialReleaseStore store() {
		return new JdbcConfidentialReleaseStore(dataSource);
	}

	private static ReleaseApplicationRequest request(String title) {
		return new ReleaseApplicationRequest(title, "跨区交接", Collections.singletonList("保密图纸"),
				Arrays.asList("00017", "SEAL-2"), "POST-A", "POST-B");
	}

	private static ReleasePrincipal principal(String actorId, String... permissions) {
		return ReleasePrincipal.authenticated(actorId,
				new java.util.LinkedHashSet<>(Arrays.asList(permissions)), Collections.<String>emptySet());
	}

	private static ReleasePrincipal executionPrincipal(String actorId, String postId) {
		return ReleasePrincipal.authenticated(actorId, posts("item-pass:execute"), posts(postId));
	}

	private static Set<String> posts(String... values) {
		return new java.util.LinkedHashSet<>(Arrays.asList(values));
	}

	private static CardEvidence securityEvidence(String releaseId, String postId, ReleaseAction action,
			String operatorId, Instant now) {
		return CardEvidence.verified(PREFIX + "_CARD_" + action + "_" + postId, CardRole.SECURITY_CHECK,
				operatorId, releaseId, postId, action, operatorId, now.minusSeconds(5), now.plusSeconds(60));
	}

	private static Properties loadLocalEnvironment(Path path) throws Exception {
		Path expected = expectedLocalEnvironment();
		Assert.assertEquals("只允许读取本任务本机配置", expected, path.toAbsolutePath().normalize());
		Assert.assertTrue("本机配置必须是普通文件", Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS));
		Assert.assertEquals("本机配置权限必须是0600",
				EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
				Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS));
		Properties values = new Properties();
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			values.load(reader);
		}
		return values;
	}

	private static Path expectedLocalEnvironment() {
		Path directory = Paths.get("").toAbsolutePath().normalize();
		while (directory != null) {
			if (Files.isDirectory(directory.resolve("specs/008-unified-client-foundation"))) {
				return directory.resolve("docker/client-integration/.env.client-local");
			}
			directory = directory.getParent();
		}
		throw new AssertionError("当前工作目录不属于规格008工作区");
	}

	private static String required(Properties values, String key) {
		String value = values.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			Assert.fail("Oracle 集成测试配置缺少：" + key);
		}
		return value;
	}

	private static void verifyTarget(Connection connection) throws Exception {
		DatabaseMetaData metadata = connection.getMetaData();
		Assert.assertTrue("必须连接真实 Oracle", metadata.getDatabaseProductName().contains("Oracle"));
		Assert.assertTrue("数据库版本必须匹配本任务已核实实例",
				metadata.getDatabaseProductVersion().contains("23.26.3.0.0"));
		try (Statement statement = connection.createStatement();
				ResultSet rows = statement.executeQuery("SELECT SYS_CONTEXT('USERENV','CON_NAME'), "
						+ "SYS_CONTEXT('USERENV','CURRENT_SCHEMA'), SYS_CONTEXT('USERENV','SESSION_USER') FROM DUAL")) {
			Assert.assertTrue(rows.next());
			Assert.assertEquals(EXPECTED_PDB, rows.getString(1));
			Assert.assertEquals(EXPECTED_USER, rows.getString(2));
			Assert.assertEquals(EXPECTED_USER, rows.getString(3));
		}
	}

	private static void initializeOrVerifyReleaseTables(Connection connection) throws Exception {
		List<String> expected = Arrays.asList(
				"SMT_CLIENT_RELEASE", "SMT_CLIENT_RELEASE_COMMAND", "SMT_CLIENT_RELEASE_EVENT");
		List<String> actual = new ArrayList<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'SMT_CLIENT_RELEASE%' ORDER BY TABLE_NAME");
				ResultSet rows = statement.executeQuery()) {
			while (rows.next()) {
				actual.add(rows.getString(1));
			}
		}
		if (actual.isEmpty()) {
			executeSqlResource(connection, RELEASE_SQL);
			actual = expected;
		}
		Assert.assertEquals("放行目标表只能全部不存在或全部精确存在", expected, actual);
		verifyColumns(connection, "SMT_CLIENT_RELEASE", columns(
				"RELEASE_ID", "VARCHAR2", "STATUS", "VARCHAR2", "RELEASE_VERSION", "NUMBER",
				"SNAPSHOT_JSON", "CLOB", "CREATED_AT", "TIMESTAMP(6)", "UPDATED_AT", "TIMESTAMP(6)"));
		verifyColumns(connection, "SMT_CLIENT_RELEASE_EVENT", columns(
				"EVENT_ID", "VARCHAR2", "RELEASE_ID", "VARCHAR2", "RELEASE_VERSION", "NUMBER",
				"ACTION", "VARCHAR2", "ACTOR_ID", "VARCHAR2", "OCCURRED_AT", "TIMESTAMP(6)",
				"EVENT_JSON", "CLOB"));
		verifyColumns(connection, "SMT_CLIENT_RELEASE_COMMAND", columns(
				"SERVICE_SCOPE", "VARCHAR2", "ACTOR_ID", "VARCHAR2", "IDEMPOTENCY_KEY", "VARCHAR2",
				"REQUEST_DIGEST", "VARCHAR2", "RELEASE_ID", "VARCHAR2", "RESPONSE_JSON", "CLOB",
				"CREATED_AT", "TIMESTAMP(6)"));
		verifyConstraints(connection, expectedConstraints());
		verifyConstraintColumns(connection, expectedConstraintColumns());
		verifyEventForeignKeyTarget(connection);
	}

	private static Map<String, String> columns(String... pairs) {
		Map<String, String> result = new LinkedHashMap<>();
		for (int index = 0; index < pairs.length; index += 2) {
			result.put(pairs[index], pairs[index + 1]);
		}
		return result;
	}

	private static void verifyColumns(Connection connection, String table, Map<String, String> expected)
			throws Exception {
		Map<String, String> actual = new LinkedHashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT COLUMN_NAME, DATA_TYPE, CHAR_LENGTH, DATA_PRECISION, DATA_SCALE, NULLABLE, CHAR_USED "
						+ "FROM USER_TAB_COLUMNS "
						+ "WHERE TABLE_NAME = ? ORDER BY COLUMN_ID")) {
			statement.setString(1, table);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					String column = rows.getString(1);
					String type = rows.getString(2);
					if ("VARCHAR2".equals(type)) {
						Assert.assertEquals("字符列必须按CHAR声明长度", "C", rows.getString(7));
					}
					String definition = type + "|" + rows.getInt(3) + "|" + nullableNumber(rows, 4)
							+ "|" + nullableNumber(rows, 5) + "|" + rows.getString(6);
					Assert.assertEquals(table + "." + column + " 类型、长度、精度或可空性不匹配",
							expectedColumnDefinition(column, type), definition);
					actual.put(column, type);
				}
			}
		}
		Assert.assertEquals(table + " 列定义不匹配", expected, actual);
	}

	private static String nullableNumber(ResultSet rows, int column) throws SQLException {
		int value = rows.getInt(column);
		return rows.wasNull() ? "-" : String.valueOf(value);
	}

	private static String expectedColumnDefinition(String column, String type) {
		String nullable = "RESPONSE_JSON".equals(column) ? "Y" : "N";
		if ("VARCHAR2".equals(type)) {
			int length;
			if ("STATUS".equals(column) || "ACTION".equals(column)) {
				length = 32;
			} else if ("REQUEST_DIGEST".equals(column)) {
				length = 64;
			} else if ("IDEMPOTENCY_KEY".equals(column)) {
				length = 200;
			} else {
				length = 128;
			}
			return "VARCHAR2|" + length + "|-|-|" + nullable;
		}
		if ("NUMBER".equals(type)) {
			return "NUMBER|0|19|0|" + nullable;
		}
		if ("TIMESTAMP(6)".equals(type)) {
			return "TIMESTAMP(6)|0|-|6|" + nullable;
		}
		return type + "|0|-|-|" + nullable;
	}

	private static Map<String, String> expectedConstraints() {
		Map<String, String> result = new LinkedHashMap<>();
		result.put("SMT_CREL_PK", "P");
		result.put("SMT_CREL_VER_CK", "C");
		result.put("SMT_CREL_EVT_PK", "P");
		result.put("SMT_CREL_EVT_REL_FK", "R");
		result.put("SMT_CREL_EVT_VER_UK", "U");
		result.put("SMT_CREL_EVT_VER_CK", "C");
		result.put("SMT_CREL_CMD_PK", "P");
		result.put("SMT_CREL_CMD_DIG_CK", "C");
		return result;
	}

	private static void verifyConstraints(Connection connection, Map<String, String> expected) throws Exception {
		Map<String, String> actual = new LinkedHashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE, STATUS, VALIDATED, DEFERRABLE FROM USER_CONSTRAINTS "
						+ "WHERE CONSTRAINT_NAME LIKE 'SMT_CREL%' ORDER BY CONSTRAINT_NAME");
				ResultSet rows = statement.executeQuery()) {
			while (rows.next()) {
				String name = rows.getString(1);
				Assert.assertEquals(name + "必须启用", "ENABLED", rows.getString(3));
				Assert.assertEquals(name + "必须经过验证", "VALIDATED", rows.getString(4));
				Assert.assertEquals(name + "不能延迟检查", "NOT DEFERRABLE", rows.getString(5));
				actual.put(name, rows.getString(2));
			}
		}
		Assert.assertEquals("放行表约束不匹配", expected, actual);
	}

	private static Map<String, String> expectedConstraintColumns() {
		Map<String, String> result = new LinkedHashMap<>();
		result.put("SMT_CREL_CMD_PK", "SERVICE_SCOPE,ACTOR_ID,IDEMPOTENCY_KEY");
		result.put("SMT_CREL_EVT_PK", "EVENT_ID");
		result.put("SMT_CREL_EVT_REL_FK", "RELEASE_ID");
		result.put("SMT_CREL_EVT_VER_UK", "RELEASE_ID,RELEASE_VERSION");
		result.put("SMT_CREL_PK", "RELEASE_ID");
		return result;
	}

	private static void verifyConstraintColumns(Connection connection, Map<String, String> expected)
			throws Exception {
		Map<String, StringBuilder> grouped = new LinkedHashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT CONSTRAINT_NAME, COLUMN_NAME FROM USER_CONS_COLUMNS "
						+ "WHERE CONSTRAINT_NAME LIKE 'SMT_CREL%' ORDER BY CONSTRAINT_NAME, POSITION");
				ResultSet rows = statement.executeQuery()) {
			while (rows.next()) {
				String name = rows.getString(1);
				if (!expected.containsKey(name)) {
					continue;
				}
				StringBuilder columns = grouped.computeIfAbsent(name, ignored -> new StringBuilder());
				if (columns.length() > 0) {
					columns.append(',');
				}
				columns.append(rows.getString(2));
			}
		}
		Map<String, String> actual = new LinkedHashMap<>();
		for (Map.Entry<String, StringBuilder> entry : grouped.entrySet()) {
			actual.put(entry.getKey(), entry.getValue().toString());
		}
		Assert.assertEquals("主键、外键或唯一约束列顺序不匹配", expected, actual);
	}

	private static void verifyEventForeignKeyTarget(Connection connection) throws Exception {
		try (PreparedStatement statement = connection.prepareStatement(
				"SELECT REFERENCED.CONSTRAINT_NAME FROM USER_CONSTRAINTS CHILD "
						+ "JOIN USER_CONSTRAINTS REFERENCED ON REFERENCED.CONSTRAINT_NAME = CHILD.R_CONSTRAINT_NAME "
						+ "WHERE CHILD.CONSTRAINT_NAME = 'SMT_CREL_EVT_REL_FK'");
				ResultSet rows = statement.executeQuery()) {
			Assert.assertTrue("缺少事件到放行主表的外键目标", rows.next());
			Assert.assertEquals("SMT_CREL_PK", rows.getString(1));
			Assert.assertFalse(rows.next());
		}
	}

	private static void executeSqlResource(Connection connection, String resource) throws Exception {
		InputStream stream = JdbcConfidentialReleaseStoreOracleTest.class.getClassLoader()
				.getResourceAsStream(resource);
		Assert.assertNotNull("缺少数据库初始化资源：" + resource, stream);
		StringBuilder sql = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("--")) {
					continue;
				}
				sql.append(line).append('\n');
				if (trimmed.endsWith(";")) {
					String statementSql = sql.substring(0, sql.lastIndexOf(";"));
					try (Statement statement = connection.createStatement()) {
						statement.execute(statementSql);
					}
					sql.setLength(0);
				}
			}
		}
		Assert.assertEquals("SQL 资源末尾存在未执行内容", "", sql.toString().trim());
	}

	private static void deleteByPrefix(Connection connection, String sql, String prefix) throws Exception {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, prefix);
			statement.executeUpdate();
		}
	}

	private static final class DriverManagerDataSource implements DataSource {

		private final String url;
		private final String user;
		private final String password;

		private DriverManagerDataSource(String url, String user, String password) {
			this.url = url;
			this.user = user;
			this.password = password;
		}

		@Override
		public Connection getConnection() throws SQLException {
			return DriverManager.getConnection(url, user, password);
		}

		@Override
		public Connection getConnection(String username, String suppliedPassword) throws SQLException {
			return DriverManager.getConnection(url, username, suppliedPassword);
		}

		@Override
		public java.io.PrintWriter getLogWriter() throws SQLException {
			return DriverManager.getLogWriter();
		}

		@Override
		public void setLogWriter(java.io.PrintWriter out) throws SQLException {
			DriverManager.setLogWriter(out);
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException {
			DriverManager.setLoginTimeout(seconds);
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			return DriverManager.getLoginTimeout();
		}

		@Override
		public Logger getParentLogger() {
			return Logger.getLogger("global");
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			throw new SQLException("不支持 unwrap");
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) {
			return false;
		}
	}
}
