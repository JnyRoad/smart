package com.tce.smart.platform.core.client.supplier;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/** 规格 008 Phase 13 供应商首次状态与受限查询的真实 Oracle 验收。 */
public class JdbcSupplierAccessStorePhase13OracleTest {

	private static final Instant BASE_TIME = Instant.parse("2026-09-05T12:00:00Z");
	private static final String PREFIX = "T063_"
			+ UUID.randomUUID().toString().replace("-", "").substring(0, 18) + "_";
	private static final String AREA = PREFIX + "AREA";
	private static final String POST = PREFIX + "POST";
	private static final String OTHER_POST = PREFIX + "POST_OTHER";
	private static final String LIST_POST = PREFIX + "POST_LIST";
	private static final String LIST_OTHER_POST = PREFIX + "POST_LIST_OTHER";
	private static final String OPERATOR = PREFIX + "OPERATOR";

	private static DataSource dataSource;

	@BeforeClass
	public static void connectAndPrepareSchema() throws Exception {
		dataSource = SupplierOracleTestSupport.connectAndPrepare();
	}

	@AfterClass
	public static void cleanOwnSyntheticRows() throws Exception {
		SupplierOracleTestSupport.cleanSyntheticRows(dataSource, PREFIX);
	}

	@Test
	public void firstEnterInitializesUnknownAndExplicitEventDeterminesPresence() throws Exception {
		String person = id("FIRST_ENTER_PERSON");
		SupplierQualificationSnapshot qualification = qualification(id("FIRST_ENTER_BADGE"), person, true);

		SupplierVerification verification = store().verifyOrInitialize(qualification, executionOperator(),
				mapping(POST), BASE_TIME, id("FIRST_ENTER_VERIFY"));

		Assert.assertEquals(SupplierPresence.UNKNOWN, verification.getPresence());
		Assert.assertEquals(0L, verification.getPresenceVersion());
		Assert.assertEquals(SupplierPresence.UNKNOWN,
				SupplierOracleTestSupport.readPresence(dataSource, person, AREA).getPresence());

		SupplierPassageResult result = store().record(id("FIRST_ENTER_SCOPE"), "first-enter",
				verification.getVerificationId(), qualification, executionOperator(), mapping(POST),
				SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), id("FIRST_ENTER_EVENT"));
		Assert.assertEquals(SupplierPresence.INSIDE, result.getPresence().getPresence());
		Assert.assertEquals(1L, result.getPresence().getVersion());
	}

	@Test
	public void firstLeaveInitializesUnknownWithoutInferringOutside() throws Exception {
		String person = id("FIRST_LEAVE_PERSON");
		SupplierQualificationSnapshot qualification = qualification(id("FIRST_LEAVE_BADGE"), person, true);
		SupplierVerification verification = store().verifyOrInitialize(qualification, executionOperator(),
				mapping(POST), BASE_TIME, id("FIRST_LEAVE_VERIFY"));

		SupplierPassageResult result = store().record(id("FIRST_LEAVE_SCOPE"), "first-leave",
				verification.getVerificationId(), qualification, executionOperator(), mapping(POST),
				SupplierDirection.LEAVE, BASE_TIME.plusSeconds(1), id("FIRST_LEAVE_EVENT"));

		Assert.assertEquals(SupplierPresence.UNKNOWN, verification.getPresence());
		Assert.assertEquals(SupplierPresence.OUTSIDE, result.getPresence().getPresence());
		Assert.assertEquals(1L, result.getPresence().getVersion());
	}

	@Test
	public void invalidQualificationOrPermissionDoesNotCreatePresenceOrVerification() throws Exception {
		String inactivePerson = id("INACTIVE_PERSON");
		String inactiveVerification = id("INACTIVE_VERIFY");
		expectRule(SupplierRuleViolation.Code.INACTIVE_QUALIFICATION, new CheckedAction() {
			@Override
			public void run() {
				store().verifyOrInitialize(qualification(id("INACTIVE_BADGE"), inactivePerson, false),
						executionOperator(), mapping(POST), BASE_TIME, inactiveVerification);
			}
		});
		Assert.assertNull(SupplierOracleTestSupport.readPresence(dataSource, inactivePerson, AREA));
		Assert.assertEquals(0,
				SupplierOracleTestSupport.countVerifications(dataSource, inactiveVerification));

		String unauthorizedPerson = id("UNAUTHORIZED_PERSON");
		String unauthorizedVerification = id("UNAUTHORIZED_VERIFY");
		SupplierOperator unauthorized = SupplierOperator.authenticated(OPERATOR,
				Collections.<String>emptySet(), set(POST));
		expectRule(SupplierRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				store().verifyOrInitialize(qualification(id("UNAUTHORIZED_BADGE"), unauthorizedPerson, true),
						unauthorized, mapping(POST), BASE_TIME, unauthorizedVerification);
			}
		});
		Assert.assertNull(SupplierOracleTestSupport.readPresence(dataSource, unauthorizedPerson, AREA));
		Assert.assertEquals(0,
				SupplierOracleTestSupport.countVerifications(dataSource, unauthorizedVerification));
	}

	@Test
	public void badgeReplacementUsesInitializedPersonAreaState() throws Exception {
		String person = id("SWAP_PERSON");
		SupplierQualificationSnapshot first = qualification(id("SWAP_BADGE_A"), person, true);
		SupplierVerification firstVerification = store().verifyOrInitialize(first, executionOperator(),
				mapping(POST), BASE_TIME, id("SWAP_VERIFY_A"));
		store().record(id("SWAP_SCOPE"), "enter", firstVerification.getVerificationId(), first,
				executionOperator(), mapping(POST), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1),
				id("SWAP_EVENT"));

		SupplierQualificationSnapshot replacement = qualification(id("SWAP_BADGE_B"), person, true);
		SupplierVerification replacementVerification = store().verifyOrInitialize(replacement,
				executionOperator(), mapping(POST), BASE_TIME.plusSeconds(2), id("SWAP_VERIFY_B"));

		Assert.assertEquals(SupplierPresence.INSIDE, replacementVerification.getPresence());
		Assert.assertEquals(1L, replacementVerification.getPresenceVersion());
	}

	@Test
	public void concurrentFirstVerificationCreatesOneUnknownStateAndTwoUsableVerifications()
			throws Exception {
		String person = id("RACE_PERSON");
		SupplierQualificationSnapshot qualification = qualification(id("RACE_BADGE"), person, true);
		Outcome<SupplierVerification>[] outcomes = runConcurrently(
				verifyCall(qualification, id("RACE_VERIFY_A")),
				verifyCall(qualification, id("RACE_VERIFY_B")));

		Assert.assertNull(outcomes[0].error);
		Assert.assertNull(outcomes[1].error);
		Assert.assertEquals(SupplierPresence.UNKNOWN, outcomes[0].value.getPresence());
		Assert.assertEquals(SupplierPresence.UNKNOWN, outcomes[1].value.getPresence());
		SupplierOracleTestSupport.PresenceRow presence =
				SupplierOracleTestSupport.readPresence(dataSource, person, AREA);
		Assert.assertEquals(SupplierPresence.UNKNOWN, presence.getPresence());
		Assert.assertEquals(0L, presence.getVersion());
		Assert.assertEquals(1, SupplierOracleTestSupport.countVerifications(dataSource,
				id("RACE_VERIFY_A")));
		Assert.assertEquals(1, SupplierOracleTestSupport.countVerifications(dataSource,
				id("RACE_VERIFY_B")));
	}

	@Test
	public void verificationLookupRestoresSnapshotAndMissingIdReturnsNull() throws Exception {
		String person = id("LOOKUP_PERSON");
		String verificationId = id("LOOKUP_VERIFY");
		SupplierQualificationSnapshot qualification = qualification(id("LOOKUP_BADGE"), person, true);
		store().verifyOrInitialize(qualification, executionOperator(), mapping(POST), BASE_TIME,
				verificationId);

		SupplierVerification restored = store().findVerification(verificationId);

		Assert.assertEquals(verificationId, restored.getVerificationId());
		Assert.assertEquals(OPERATOR, restored.getOperatorId());
		Assert.assertEquals(POST, restored.getPostId());
		Assert.assertEquals(AREA, restored.getAreaId());
		Assert.assertEquals(SupplierPresence.UNKNOWN, restored.getPresence());
		Assert.assertEquals(person, restored.getQualificationSnapshot().getPersonId());
		Assert.assertEquals("供应商人员", restored.getQualificationSnapshot().getPersonName());
		Assert.assertNull(store().findVerification(id("LOOKUP_MISSING")));
	}

	@Test
	public void eventListRequiresPostScopeFiltersRowsAndUsesStableDescendingOrder() throws Exception {
		SupplierPassageEvent first = createFirstEvent("LIST_A", LIST_POST, id("LIST_EVENT_A"));
		SupplierPassageEvent second = createFirstEvent("LIST_B", LIST_OTHER_POST, id("LIST_EVENT_B"));

		List<SupplierPassageEvent> all = store().listEvents(set(LIST_POST, LIST_OTHER_POST), 100);
		Assert.assertEquals(Arrays.asList(second.getEventId(), first.getEventId()),
				Arrays.asList(all.get(0).getEventId(), all.get(1).getEventId()));
		Assert.assertEquals("供应商人员", all.get(0).getQualificationSnapshot().getPersonName());

		List<SupplierPassageEvent> allowed = store().listEvents(set(LIST_POST), 1);
		Assert.assertEquals(1, allowed.size());
		Assert.assertEquals(first.getEventId(), allowed.get(0).getEventId());
		Assert.assertEquals(Collections.emptyList(), store().listEvents(Collections.<String>emptySet(), 10));
		expectPersistence(SupplierPersistenceException.Code.INVALID_INPUT, new CheckedAction() {
			@Override
			public void run() {
				store().listEvents(null, 10);
			}
		});
		expectPersistence(SupplierPersistenceException.Code.INVALID_INPUT, new CheckedAction() {
			@Override
			public void run() {
				store().listEvents(set(LIST_POST), 0);
			}
		});
		expectPersistence(SupplierPersistenceException.Code.INVALID_INPUT, new CheckedAction() {
			@Override
			public void run() {
				store().listEvents(set(LIST_POST), 101);
			}
		});
	}

	private static SupplierPassageEvent createFirstEvent(String suffix, String postId, String eventId) {
		String person = id(suffix + "_PERSON");
		SupplierQualificationSnapshot qualification = qualification(id(suffix + "_BADGE"), person, true);
		SupplierOperator operator = SupplierOperator.authenticated(OPERATOR, set("supplier:execute"),
				set(postId));
		SupplierVerification verification = store().verifyOrInitialize(qualification, operator,
				mapping(postId), BASE_TIME, id(suffix + "_VERIFY"));
		return store().record(id(suffix + "_SCOPE"), suffix, verification.getVerificationId(), qualification,
				operator, mapping(postId), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), eventId).getEvent();
	}

	private static Callable<SupplierVerification> verifyCall(SupplierQualificationSnapshot qualification,
			String verificationId) {
		return new Callable<SupplierVerification>() {
			@Override
			public SupplierVerification call() {
				return store().verifyOrInitialize(qualification, executionOperator(), mapping(POST), BASE_TIME,
						verificationId);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static <T> Outcome<T>[] runConcurrently(Callable<T> first, Callable<T> second)
			throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		try {
			Future<Outcome<T>> firstFuture = executor.submit(outcome(first, ready, start));
			Future<Outcome<T>> secondFuture = executor.submit(outcome(second, ready, start));
			Assert.assertTrue("并发任务未就绪", ready.await(10, TimeUnit.SECONDS));
			start.countDown();
			return new Outcome[] { firstFuture.get(30, TimeUnit.SECONDS),
					secondFuture.get(30, TimeUnit.SECONDS) };
		} finally {
			executor.shutdownNow();
			Assert.assertTrue("并发测试线程未结束", executor.awaitTermination(10, TimeUnit.SECONDS));
		}
	}

	private static <T> Callable<Outcome<T>> outcome(Callable<T> work, CountDownLatch ready,
			CountDownLatch start) {
		return new Callable<Outcome<T>>() {
			@Override
			public Outcome<T> call() {
				ready.countDown();
				try {
					start.await();
					return Outcome.success(work.call());
				} catch (Throwable error) {
					return Outcome.failure(error);
				}
			}
		};
	}

	private static JdbcSupplierAccessStore store() {
		return new JdbcSupplierAccessStore(dataSource);
	}

	private static SupplierQualificationSnapshot qualification(String badgeId, String personId,
			boolean active) {
		return SupplierQualificationSnapshot.fromTrustedSource(badgeId, personId, id("COMPANY"),
				id("ADMISSION"), active, active, active, active, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA), "供应商人员", "供应商公司", "photo://synthetic",
				"10000000000", "被访人员", "10000000001");
	}

	private static SupplierOperator executionOperator() {
		return SupplierOperator.authenticated(OPERATOR, set("supplier:execute"), set(POST));
	}

	private static SupplierPostAreaMapping mapping(String postId) {
		return SupplierPostAreaMapping.fromTrustedDirectory(postId, AREA);
	}

	private static String id(String suffix) {
		return PREFIX + suffix;
	}

	private static Set<String> set(String... values) {
		return new LinkedHashSet<>(Arrays.asList(values));
	}

	private static void expectRule(SupplierRuleViolation.Code code, CheckedAction action) {
		try {
			action.run();
			Assert.fail("预期领域规则拒绝：" + code);
		} catch (SupplierRuleViolation error) {
			Assert.assertEquals(code, error.getCode());
		} catch (Exception error) {
			throw new AssertionError("预期领域规则拒绝，实际异常类型为 " + error.getClass().getName(), error);
		}
	}

	private static void expectPersistence(SupplierPersistenceException.Code code, CheckedAction action) {
		try {
			action.run();
			Assert.fail("预期持久化拒绝：" + code);
		} catch (SupplierPersistenceException error) {
			Assert.assertEquals(code, error.getCode());
		} catch (Exception error) {
			throw new AssertionError("预期持久化拒绝，实际异常类型为 " + error.getClass().getName(), error);
		}
	}

	private interface CheckedAction {
		void run() throws Exception;
	}

	private static final class Outcome<T> {
		private final T value;
		private final Throwable error;

		private Outcome(T value, Throwable error) {
			this.value = value;
			this.error = error;
		}

		private static <T> Outcome<T> success(T value) {
			return new Outcome<>(value, null);
		}

		private static <T> Outcome<T> failure(Throwable error) {
			return new Outcome<>(null, error);
		}
	}
}
