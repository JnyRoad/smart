package com.tce.smart.platform.core.client.supplier;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
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

/** 供应商进出仓储在规格 008 独立 Oracle schema 上的事务验收。 */
public class JdbcSupplierAccessStoreOracleTest {

	private static final Instant BASE_TIME = Instant.parse("2026-09-05T11:00:00Z");
	private static final String PREFIX = "T059_"
			+ UUID.randomUUID().toString().replace("-", "").substring(0, 18) + "_";
	private static final String AREA = PREFIX + "AREA";
	private static final String OTHER_AREA = PREFIX + "AREA_OTHER";
	private static final String POST = PREFIX + "POST";
	private static final String OTHER_POST = PREFIX + "POST_OTHER";
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
	public void newConnectionsPersistCompleteEnterAndLeaveWithImmutableOriginalReply() throws Exception {
		String person = id("FLOW_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("FLOW_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierOperator operator = executionOperator();
		SupplierPostAreaMapping mapping = mapping();

		SupplierVerification enterProof = store().verify(qualification, operator, mapping, BASE_TIME,
				id("FLOW_VERIFY_ENTER"));
		Assert.assertEquals(SupplierPresence.OUTSIDE, enterProof.getPresence());
		Assert.assertEquals(0L, enterProof.getPresenceVersion());

		String enterScope = id("FLOW_SCOPE_ENTER");
		SupplierPassageResult entered = store().record(enterScope, "enter-key", enterProof.getVerificationId(),
				qualification, operator, mapping, SupplierDirection.ENTER, BASE_TIME.plusSeconds(1),
				id("FLOW_EVENT_ENTER"));
		Assert.assertEquals(SupplierPresence.INSIDE, entered.getPresence().getPresence());
		Assert.assertEquals(1L, entered.getPresence().getVersion());

		SupplierVerification leaveProof = store().verify(qualification, operator, mapping,
				BASE_TIME.plusSeconds(2), id("FLOW_VERIFY_LEAVE"));
		Assert.assertEquals(SupplierPresence.INSIDE, leaveProof.getPresence());
		Assert.assertEquals(1L, leaveProof.getPresenceVersion());

		String leaveScope = id("FLOW_SCOPE_LEAVE");
		SupplierPassageResult left = store().record(leaveScope, "leave-key", leaveProof.getVerificationId(),
				qualification, operator, mapping, SupplierDirection.LEAVE, BASE_TIME.plusSeconds(3),
				id("FLOW_EVENT_LEAVE"));
		Assert.assertEquals(SupplierPresence.OUTSIDE, left.getPresence().getPresence());
		Assert.assertEquals(2L, left.getPresence().getVersion());

		SupplierPassageResult replay = store().record(leaveScope, "leave-key", leaveProof.getVerificationId(),
				qualification, operator, mapping, SupplierDirection.LEAVE, BASE_TIME.plusSeconds(40),
				id("FLOW_EVENT_IGNORED"));
		Assert.assertEquals(left.getEvent().getEventId(), replay.getEvent().getEventId());
		Assert.assertEquals(left.getEvent().getOccurredAt(), replay.getEvent().getOccurredAt());
		Assert.assertEquals("供应商 \"甲\"\\现场\n人员", replay.getEvent().getQualificationSnapshot().getPersonName());
		Assert.assertEquals(set(AREA, OTHER_AREA),
				replay.getEvent().getQualificationSnapshot().getAuthorizedAreaIds());
		assertUnmodifiable(replay.getEvent().getQualificationSnapshot().getAuthorizedAreaIds());

		SupplierOracleTestSupport.PresenceRow persisted =
				SupplierOracleTestSupport.readPresence(dataSource, person, AREA);
		Assert.assertEquals(SupplierPresence.OUTSIDE, persisted.getPresence());
		Assert.assertEquals(2L, persisted.getVersion());
		Assert.assertEquals(2, SupplierOracleTestSupport.countEvents(dataSource, person));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, enterScope));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, leaveScope));
	}

	@Test
	public void badgeReplacementUsesPersonAreaStateAndCannotRepeatDirection() throws Exception {
		String person = id("SWAP_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierOperator operator = executionOperator();
		SupplierQualificationSnapshot first = qualification(id("SWAP_BADGE_A"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierVerification firstProof = store().verify(first, operator, mapping(), BASE_TIME,
				id("SWAP_VERIFY_A"));
		store().record(id("SWAP_SCOPE_ENTER"), "enter", firstProof.getVerificationId(), first, operator,
				mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), id("SWAP_EVENT_ENTER"));

		SupplierQualificationSnapshot replacement = qualification(id("SWAP_BADGE_B"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierVerification replacementProof = store().verify(replacement, operator, mapping(),
				BASE_TIME.plusSeconds(2), id("SWAP_VERIFY_B"));
		Assert.assertEquals(SupplierPresence.INSIDE, replacementProof.getPresence());
		Assert.assertEquals(1L, replacementProof.getPresenceVersion());

		String rejectedScope = id("SWAP_SCOPE_REPEAT");
		expectRule(SupplierRuleViolation.Code.DUPLICATE_DIRECTION, new CheckedAction() {
			@Override
			public void run() {
				store().record(rejectedScope, "repeat-enter", replacementProof.getVerificationId(), replacement,
						operator, mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(3),
						id("SWAP_EVENT_REPEAT"));
			}
		});
		Assert.assertFalse(SupplierOracleTestSupport.isVerificationConsumed(dataSource,
				replacementProof.getVerificationId()));
		Assert.assertEquals(0, SupplierOracleTestSupport.countCommands(dataSource, rejectedScope));

		store().record(id("SWAP_SCOPE_LEAVE"), "leave", replacementProof.getVerificationId(), replacement,
				operator, mapping(), SupplierDirection.LEAVE, BASE_TIME.plusSeconds(4), id("SWAP_EVENT_LEAVE"));
		SupplierOracleTestSupport.PresenceRow persisted =
				SupplierOracleTestSupport.readPresence(dataSource, person, AREA);
		Assert.assertEquals(SupplierPresence.OUTSIDE, persisted.getPresence());
		Assert.assertEquals(2L, persisted.getVersion());
	}

	@Test
	public void verifyRejectsMissingPersonAreaBaselineWithoutCreatingOne() throws Exception {
		String person = id("MISSING_PERSON");
		String verificationId = id("MISSING_VERIFY");
		expectPersistence(SupplierPersistenceException.Code.PRESENCE_NOT_INITIALIZED, new CheckedAction() {
			@Override
			public void run() {
				store().verify(qualification(id("MISSING_BADGE"), person, true, BASE_TIME.plusSeconds(3600)),
						executionOperator(), mapping(), BASE_TIME, verificationId);
			}
		});
		Assert.assertNull(SupplierOracleTestSupport.readPresence(dataSource, person, AREA));
		Assert.assertEquals(0, SupplierOracleTestSupport.countVerifications(dataSource, verificationId));
	}

	@Test
	public void expiredVerificationRollsBackCommandStateAndConsumption() throws Exception {
		String person = id("EXPIRED_PROOF_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("EXPIRED_PROOF_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierVerification proof = store().verify(qualification, executionOperator(), mapping(), BASE_TIME,
				id("EXPIRED_PROOF_VERIFY"));
		String scope = id("EXPIRED_PROOF_SCOPE");

		expectRule(SupplierRuleViolation.Code.VERIFICATION_EXPIRED, new CheckedAction() {
			@Override
			public void run() {
				store().record(scope, "expired", proof.getVerificationId(), qualification, executionOperator(),
						mapping(), SupplierDirection.ENTER, proof.getExpiresAt(), id("EXPIRED_PROOF_EVENT"));
			}
		});
		assertUnchangedOutside(person, proof.getVerificationId(), scope);
	}

	@Test
	public void recordRechecksCurrentQualificationRevocationAndExpiry() throws Exception {
		String revokedPerson = id("REVOKED_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, revokedPerson, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot original = qualification(id("REVOKED_BADGE"), revokedPerson, true,
				BASE_TIME.plusSeconds(3600));
		SupplierVerification revokedProof = store().verify(original, executionOperator(), mapping(), BASE_TIME,
				id("REVOKED_VERIFY"));
		SupplierQualificationSnapshot revoked = qualification(id("REVOKED_BADGE"), revokedPerson, false,
				BASE_TIME.plusSeconds(3600));
		String revokedScope = id("REVOKED_SCOPE");
		expectRule(SupplierRuleViolation.Code.INACTIVE_QUALIFICATION, new CheckedAction() {
			@Override
			public void run() {
				store().record(revokedScope, "revoked", revokedProof.getVerificationId(), revoked,
						executionOperator(), mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1),
						id("REVOKED_EVENT"));
			}
		});
		assertUnchangedOutside(revokedPerson, revokedProof.getVerificationId(), revokedScope);

		String expiredPerson = id("QUAL_EXPIRED_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, expiredPerson, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot initiallyValid = qualification(id("QUAL_EXPIRED_BADGE"), expiredPerson,
				true, BASE_TIME.plusSeconds(60));
		SupplierVerification expiredQualificationProof = store().verify(initiallyValid, executionOperator(),
				mapping(), BASE_TIME, id("QUAL_EXPIRED_VERIFY"));
		String expiredScope = id("QUAL_EXPIRED_SCOPE");
		expectRule(SupplierRuleViolation.Code.QUALIFICATION_EXPIRED, new CheckedAction() {
			@Override
			public void run() {
				store().record(expiredScope, "qualification-expired",
						expiredQualificationProof.getVerificationId(), initiallyValid, executionOperator(), mapping(),
						SupplierDirection.ENTER, BASE_TIME.plusSeconds(60), id("QUAL_EXPIRED_EVENT"));
			}
		});
		assertUnchangedOutside(expiredPerson, expiredQualificationProof.getVerificationId(), expiredScope);
	}

	@Test
	public void recordRejectsVerificationBoundToAnotherPost() throws Exception {
		String person = id("POST_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("POST_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierOperator operator = operator(OPERATOR, set("supplier:execute"), set(POST, OTHER_POST));
		SupplierVerification proof = store().verify(qualification, operator, mapping(), BASE_TIME,
				id("POST_VERIFY"));
		SupplierPostAreaMapping wrongPost = SupplierPostAreaMapping.fromTrustedDirectory(OTHER_POST, AREA);
		String scope = id("POST_SCOPE");

		expectRule(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, new CheckedAction() {
			@Override
		public void run() {
				store().record(scope, "wrong-post", proof.getVerificationId(), qualification, operator, wrongPost,
						SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), id("POST_EVENT"));
			}
		});
		assertUnchangedOutside(person, proof.getVerificationId(), scope);
	}

	@Test
	public void sameProofDifferentKeysConcurrentAllowsExactlyOnePassage() throws Exception {
		String person = id("PROOF_RACE_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("PROOF_RACE_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierOperator operator = executionOperator();
		SupplierVerification proof = store().verify(qualification, operator, mapping(), BASE_TIME,
				id("PROOF_RACE_VERIFY"));
		String scope = id("PROOF_RACE_SCOPE");

		Outcome<SupplierPassageResult>[] outcomes = runConcurrently(
				recordCall(scope, "key-a", proof, qualification, operator, BASE_TIME.plusSeconds(1),
						id("PROOF_RACE_EVENT_A")),
				recordCall(scope, "key-b", proof, qualification, operator, BASE_TIME.plusSeconds(1),
						id("PROOF_RACE_EVENT_B")));
		Assert.assertEquals(1, successCount(outcomes));
		Assert.assertEquals(1, persistenceFailureCount(outcomes,
				SupplierPersistenceException.Code.VERIFICATION_CONSUMED));
		SupplierOracleTestSupport.PresenceRow persisted =
				SupplierOracleTestSupport.readPresence(dataSource, person, AREA);
		Assert.assertEquals(SupplierPresence.INSIDE, persisted.getPresence());
		Assert.assertEquals(1L, persisted.getVersion());
		Assert.assertEquals(1, SupplierOracleTestSupport.countEvents(dataSource, person));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, scope));
	}

	@Test
	public void sameActorScopeAndKeyConcurrentReturnsOneOriginalReply() throws Exception {
		String person = id("KEY_RACE_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("KEY_RACE_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierOperator operator = executionOperator();
		SupplierVerification proof = store().verify(qualification, operator, mapping(), BASE_TIME,
				id("KEY_RACE_VERIFY"));
		String scope = id("KEY_RACE_SCOPE");

		Outcome<SupplierPassageResult>[] outcomes = runConcurrently(
				recordCall(scope, "same-key", proof, qualification, operator, BASE_TIME.plusSeconds(1),
						id("KEY_RACE_EVENT_A")),
				recordCall(scope, "same-key", proof, qualification, operator, BASE_TIME.plusSeconds(2),
						id("KEY_RACE_EVENT_B")));
		Assert.assertEquals(2, successCount(outcomes));
		SupplierPassageResult first = outcomes[0].value;
		SupplierPassageResult second = outcomes[1].value;
		Assert.assertEquals(first.getEvent().getEventId(), second.getEvent().getEventId());
		Assert.assertEquals(first.getEvent().getOccurredAt(), second.getEvent().getOccurredAt());
		Assert.assertEquals(1, SupplierOracleTestSupport.countEvents(dataSource, person));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, scope));
	}

	@Test
	public void replayReturnsOriginalButStillRechecksCurrentPermissionAndBinding() throws Exception {
		String person = id("REPLAY_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("REPLAY_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierOperator operator = operator(OPERATOR, set("supplier:execute"), set(POST, OTHER_POST));
		SupplierVerification proof = store().verify(qualification, operator, mapping(), BASE_TIME,
				id("REPLAY_VERIFY"));
		String scope = id("REPLAY_SCOPE");
		SupplierPassageResult original = store().record(scope, "same-key", proof.getVerificationId(),
				qualification, operator, mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1),
				id("REPLAY_EVENT"));

		SupplierPassageResult replay = store().record(scope, "same-key", proof.getVerificationId(),
				qualification, operator, mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(20),
				id("REPLAY_EVENT_IGNORED"));
		Assert.assertEquals(original.getEvent().getEventId(), replay.getEvent().getEventId());

		SupplierOperator permissionRevoked = operator(OPERATOR, Collections.<String>emptySet(), set(POST));
		expectRule(SupplierRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				store().record(scope, "same-key", proof.getVerificationId(), qualification, permissionRevoked,
						mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(21), id("REPLAY_NO_PERMISSION"));
			}
		});

		SupplierPostAreaMapping wrongBinding = SupplierPostAreaMapping.fromTrustedDirectory(OTHER_POST, AREA);
		expectRule(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, new CheckedAction() {
			@Override
			public void run() {
				store().record(scope, "same-key", proof.getVerificationId(), qualification, operator, wrongBinding,
						SupplierDirection.ENTER, BASE_TIME.plusSeconds(22), id("REPLAY_WRONG_POST"));
			}
		});
		Assert.assertEquals(1, SupplierOracleTestSupport.countEvents(dataSource, person));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, scope));
	}

	@Test
	public void sameIdempotencyKeyRejectsDifferentRequest() throws Exception {
		String person = id("KEY_CONFLICT_PERSON");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, person, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot qualification = qualification(id("KEY_CONFLICT_BADGE"), person, true,
				BASE_TIME.plusSeconds(3600));
		SupplierVerification proof = store().verify(qualification, executionOperator(), mapping(), BASE_TIME,
				id("KEY_CONFLICT_VERIFY"));
		String scope = id("KEY_CONFLICT_SCOPE");
		store().record(scope, "same-key", proof.getVerificationId(), qualification, executionOperator(), mapping(),
				SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), id("KEY_CONFLICT_EVENT"));

		expectPersistence(SupplierPersistenceException.Code.IDEMPOTENCY_CONFLICT, new CheckedAction() {
			@Override
			public void run() {
				store().record(scope, "same-key", proof.getVerificationId(), qualification, executionOperator(),
						mapping(), SupplierDirection.LEAVE, BASE_TIME.plusSeconds(2), id("KEY_CONFLICT_OTHER_EVENT"));
			}
		});
		Assert.assertEquals(1, SupplierOracleTestSupport.countEvents(dataSource, person));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, scope));
	}

	@Test
	public void uniqueEventFailureRollsBackPresenceProofEventAndCommandAtomically() throws Exception {
		String sharedEventId = id("ATOMIC_SHARED_EVENT");
		String firstPerson = id("ATOMIC_PERSON_A");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, firstPerson, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot firstQualification = qualification(id("ATOMIC_BADGE_A"), firstPerson,
				true, BASE_TIME.plusSeconds(3600));
		SupplierVerification firstProof = store().verify(firstQualification, executionOperator(), mapping(),
				BASE_TIME, id("ATOMIC_VERIFY_A"));
		store().record(id("ATOMIC_SCOPE_A"), "first", firstProof.getVerificationId(), firstQualification,
				executionOperator(), mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), sharedEventId);

		String secondPerson = id("ATOMIC_PERSON_B");
		SupplierOracleTestSupport.seedOutsideBaseline(dataSource, secondPerson, AREA, BASE_TIME.minusSeconds(1));
		SupplierQualificationSnapshot secondQualification = qualification(id("ATOMIC_BADGE_B"), secondPerson,
				true, BASE_TIME.plusSeconds(3600));
		SupplierVerification secondProof = store().verify(secondQualification, executionOperator(), mapping(),
				BASE_TIME, id("ATOMIC_VERIFY_B"));
		String secondScope = id("ATOMIC_SCOPE_B");

		expectPersistence(SupplierPersistenceException.Code.STORAGE_FAILURE, new CheckedAction() {
			@Override
			public void run() {
				store().record(secondScope, "second", secondProof.getVerificationId(), secondQualification,
						executionOperator(), mapping(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), sharedEventId);
			}
		});
		assertUnchangedOutside(secondPerson, secondProof.getVerificationId(), secondScope);
		Assert.assertEquals(0, SupplierOracleTestSupport.countEvents(dataSource, secondPerson));

		SupplierPassageResult retry = store().record(secondScope, "second", secondProof.getVerificationId(),
				secondQualification, executionOperator(), mapping(), SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(2), id("ATOMIC_RETRY_EVENT"));
		Assert.assertEquals(SupplierPresence.INSIDE, retry.getPresence().getPresence());
		Assert.assertEquals(1, SupplierOracleTestSupport.countEvents(dataSource, secondPerson));
		Assert.assertEquals(1, SupplierOracleTestSupport.countCommands(dataSource, secondScope));
	}

	private static JdbcSupplierAccessStore store() {
		return new JdbcSupplierAccessStore(dataSource);
	}

	private static Callable<SupplierPassageResult> recordCall(String scope, String key,
			SupplierVerification proof, SupplierQualificationSnapshot qualification,
			SupplierOperator operator, Instant now, String eventId) {
		return new Callable<SupplierPassageResult>() {
			@Override
			public SupplierPassageResult call() {
				return store().record(scope, key, proof.getVerificationId(), qualification, operator, mapping(),
						SupplierDirection.ENTER, now, eventId);
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

	private static int successCount(Outcome<?>[] outcomes) {
		int count = 0;
		for (Outcome<?> outcome : outcomes) {
			if (outcome.error == null) {
				count++;
			}
		}
		return count;
	}

	private static int persistenceFailureCount(Outcome<?>[] outcomes,
			SupplierPersistenceException.Code expected) {
		int count = 0;
		for (Outcome<?> outcome : outcomes) {
			if (outcome.error instanceof SupplierPersistenceException
					&& ((SupplierPersistenceException) outcome.error).getCode() == expected) {
				count++;
			}
		}
		return count;
	}

	private static void assertUnchangedOutside(String person, String verificationId, String scope)
			throws Exception {
		SupplierOracleTestSupport.PresenceRow persisted =
				SupplierOracleTestSupport.readPresence(dataSource, person, AREA);
		Assert.assertEquals(SupplierPresence.OUTSIDE, persisted.getPresence());
		Assert.assertEquals(0L, persisted.getVersion());
		Assert.assertFalse(SupplierOracleTestSupport.isVerificationConsumed(dataSource, verificationId));
		Assert.assertEquals(0, SupplierOracleTestSupport.countEvents(dataSource, person));
		Assert.assertEquals(0, SupplierOracleTestSupport.countCommands(dataSource, scope));
	}

	private static SupplierQualificationSnapshot qualification(String badgeId, String personId,
			boolean active, Instant validUntil) {
		return SupplierQualificationSnapshot.fromTrustedSource(badgeId, personId, id("COMPANY"),
				id("ADMISSION"), active, active, active, active, true, BASE_TIME.minusSeconds(60), validUntil,
				set(OTHER_AREA, AREA), "供应商 \"甲\"\\现场\n人员", "供应商公司", "photo://synthetic",
				"10000000000", "被访人员", "10000000001");
	}

	private static SupplierOperator executionOperator() {
		return operator(OPERATOR, set("supplier:execute"), set(POST));
	}

	private static SupplierOperator operator(String operatorId, Set<String> permissions, Set<String> posts) {
		return SupplierOperator.authenticated(operatorId, permissions, posts);
	}

	private static SupplierPostAreaMapping mapping() {
		return SupplierPostAreaMapping.fromTrustedDirectory(POST, AREA);
	}

	private static String id(String suffix) {
		return PREFIX + suffix;
	}

	private static Set<String> set(String... values) {
		return new LinkedHashSet<>(Arrays.asList(values));
	}

	private static void assertUnmodifiable(Set<String> values) {
		try {
			values.add(id("MUTATION"));
			Assert.fail("持久化恢复后的集合必须不可变");
		} catch (UnsupportedOperationException expected) {
			// expected
		}
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
