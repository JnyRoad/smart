package com.tce.smart.platform.core.client.supplier;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SupplierAccessWorkflowTest {

	private static final Instant BASE_TIME = Instant.parse("2026-09-05T08:00:00Z");
	private static final String BADGE_ID = "badge-001";
	private static final String PERSON_ID = "person-001";
	private static final String COMPANY_ID = "company-001";
	private static final String ADMISSION_ID = "admission-001";
	private static final String OPERATOR_ID = "operator-001";
	private static final String POST_ID = "post-east-gate";
	private static final String AREA_ID = "area-confidential-east";
	private static final String OTHER_POST_ID = "post-west-gate";
	private static final String OTHER_AREA_ID = "area-confidential-west";

	private final SupplierAccessWorkflow workflow = new SupplierAccessWorkflow();

	@Test
	public void verifyBindsTrustedQualificationOperatorPostAreaAndPresenceVersion() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierPresenceSnapshot presence = presence(PERSON_ID, AREA_ID, SupplierPresence.OUTSIDE, 7L);

		SupplierVerification verification = workflow.verify(qualification, executionOperator(), mapping(),
				presence, BASE_TIME, "verification-001");

		Assert.assertEquals("verification-001", verification.getVerificationId());
		Assert.assertEquals(OPERATOR_ID, verification.getOperatorId());
		Assert.assertEquals(POST_ID, verification.getPostId());
		Assert.assertEquals(AREA_ID, verification.getAreaId());
		Assert.assertEquals(BADGE_ID, verification.getQualificationSnapshot().getBadgeId());
		Assert.assertEquals(PERSON_ID, verification.getQualificationSnapshot().getPersonId());
		Assert.assertEquals(COMPANY_ID, verification.getQualificationSnapshot().getCompanyId());
		Assert.assertEquals(ADMISSION_ID, verification.getQualificationSnapshot().getAdmissionId());
		Assert.assertEquals("供应商人员", verification.getQualificationSnapshot().getPersonName());
		Assert.assertEquals("供应商公司", verification.getQualificationSnapshot().getCompanyName());
		Assert.assertEquals("https://example.invalid/photo/person-001",
				verification.getQualificationSnapshot().getPhotoUrl());
		Assert.assertEquals("13800000000", verification.getQualificationSnapshot().getPersonPhone());
		Assert.assertEquals("被访员工", verification.getQualificationSnapshot().getHostName());
		Assert.assertEquals("13900000000", verification.getQualificationSnapshot().getHostPhone());
		Assert.assertEquals(SupplierPresence.OUTSIDE, verification.getPresence());
		Assert.assertEquals(7L, verification.getPresenceVersion());
		Assert.assertEquals(BASE_TIME, verification.getVerifiedAt());
		Assert.assertEquals(BASE_TIME.plusSeconds(300), verification.getExpiresAt());
	}

	@Test
	public void verifyUsesEarlierQualificationEndAndTreatsWindowAsHalfOpen() {
		SupplierQualificationSnapshot shortQualification = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME, BASE_TIME.plusSeconds(120),
				set(AREA_ID));

		SupplierVerification verification = workflow.verify(shortQualification, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-short");
		Assert.assertEquals(BASE_TIME.plusSeconds(120), verification.getExpiresAt());

		expectVerifyViolation(SupplierRuleViolation.Code.QUALIFICATION_NOT_YET_VALID, shortQualification,
				BASE_TIME.minusMillis(1));
		expectVerifyViolation(SupplierRuleViolation.Code.QUALIFICATION_EXPIRED, shortQualification,
				BASE_TIME.plusSeconds(120));
	}

	@Test
	public void verifyRejectsInactiveBadgePersonCompanyOrAdmission() {
		for (final SupplierQualificationSnapshot inactive : Arrays.asList(
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, false, true, true, true, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)),
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, false, true, true, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)),
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, true, false, true, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)),
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, true, true, false, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)))) {
			expectVerifyViolation(SupplierRuleViolation.Code.INACTIVE_QUALIFICATION, inactive, BASE_TIME);
		}
	}

	@Test
	public void verifyRejectsAdmissionWithoutCurrentApproval() {
		SupplierQualificationSnapshot unapproved = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, false, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));

		expectVerifyViolation(SupplierRuleViolation.Code.ADMISSION_NOT_APPROVED, unapproved, BASE_TIME);
	}

	@Test
	public void verifyRequiresExecutePermissionAndAuthorizedPost() {
		final SupplierQualificationSnapshot qualification = validQualification();
		final SupplierPresenceSnapshot presence = outsidePresence();

		expectViolation(SupplierRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				workflow.verify(qualification, operator(OPERATOR_ID, Collections.<String>emptySet(), set(POST_ID)),
						mapping(), presence, BASE_TIME, "verification-no-permission");
			}
		});
		expectViolation(SupplierRuleViolation.Code.UNAUTHORIZED_POST, new CheckedAction() {
			@Override
			public void run() {
				workflow.verify(qualification, operator(OPERATOR_ID, set("supplier:execute"), set(OTHER_POST_ID)),
						mapping(), presence, BASE_TIME, "verification-wrong-post");
			}
		});
	}

	@Test
	public void verifyRequiresTrustedMappedAreaWithinQualification() {
		final SupplierPostAreaMapping wrongArea = SupplierPostAreaMapping.fromTrustedDirectory(POST_ID,
				OTHER_AREA_ID);

		expectViolation(SupplierRuleViolation.Code.AREA_NOT_AUTHORIZED, new CheckedAction() {
			@Override
			public void run() {
				workflow.verify(validQualification(), executionOperator(), wrongArea,
						presence(PERSON_ID, OTHER_AREA_ID, SupplierPresence.OUTSIDE, 7L), BASE_TIME,
						"verification-wrong-area");
			}
		});
	}

	@Test
	public void verifyRequiresPresenceForSamePersonAndArea() {
		final SupplierQualificationSnapshot qualification = validQualification();

		expectViolation(SupplierRuleViolation.Code.PRESENCE_MISMATCH, new CheckedAction() {
			@Override
			public void run() {
				workflow.verify(qualification, executionOperator(), mapping(),
						presence("person-other", AREA_ID, SupplierPresence.OUTSIDE, 7L), BASE_TIME,
						"verification-wrong-person");
			}
		});
		expectViolation(SupplierRuleViolation.Code.PRESENCE_MISMATCH, new CheckedAction() {
			@Override
			public void run() {
				workflow.verify(qualification, executionOperator(), mapping(),
						presence(PERSON_ID, OTHER_AREA_ID, SupplierPresence.OUTSIDE, 7L), BASE_TIME,
						"verification-wrong-presence-area");
			}
		});
	}

	@Test
	public void trustedCollectionsAreDefensivelyCopiedAndUnmodifiable() {
		Set<String> areas = set(AREA_ID);
		Set<String> permissions = set("supplier:execute");
		Set<String> posts = set(POST_ID);
		SupplierQualificationSnapshot qualification = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), areas);
		SupplierOperator operator = operator(OPERATOR_ID, permissions, posts);

		areas.add(OTHER_AREA_ID);
		permissions.clear();
		posts.add(OTHER_POST_ID);

		Assert.assertEquals(set(AREA_ID), qualification.getAuthorizedAreaIds());
		Assert.assertEquals(set("supplier:execute"), operator.getPermissions());
		Assert.assertEquals(set(POST_ID), operator.getAuthorizedPostIds());
		assertUnmodifiable(qualification.getAuthorizedAreaIds());
		assertUnmodifiable(operator.getPermissions());
		assertUnmodifiable(operator.getAuthorizedPostIds());
	}

	@Test
	public void recordEnterCreatesNextPresenceAndIndependentImmutableEvent() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierOperator operator = executionOperator();
		SupplierPostAreaMapping mapping = mapping();
		SupplierPresenceSnapshot currentPresence = outsidePresence();
		SupplierVerification verification = workflow.verify(qualification, operator, mapping, currentPresence,
				BASE_TIME, "verification-enter");
		Instant eventTime = BASE_TIME.plusSeconds(30);

		SupplierPassageResult result = workflow.record(verification, qualification, operator, mapping,
				currentPresence, SupplierDirection.ENTER, eventTime, "event-enter");

		Assert.assertEquals(SupplierPresence.OUTSIDE, currentPresence.getPresence());
		Assert.assertEquals(7L, currentPresence.getVersion());
		Assert.assertEquals(SupplierPresence.INSIDE, result.getPresence().getPresence());
		Assert.assertEquals(8L, result.getPresence().getVersion());
		Assert.assertEquals(PERSON_ID, result.getPresence().getPersonId());
		Assert.assertEquals(AREA_ID, result.getPresence().getAreaId());

		SupplierPassageEvent event = result.getEvent();
		Assert.assertEquals("event-enter", event.getEventId());
		Assert.assertEquals("verification-enter", event.getVerificationId());
		Assert.assertEquals(OPERATOR_ID, event.getOperatorId());
		Assert.assertEquals(POST_ID, event.getPostId());
		Assert.assertEquals(AREA_ID, event.getAreaId());
		Assert.assertEquals(SupplierDirection.ENTER, event.getDirection());
		Assert.assertEquals(eventTime, event.getOccurredAt());
		Assert.assertEquals(8L, event.getVersion());
		Assert.assertEquals(BADGE_ID, event.getQualificationSnapshot().getBadgeId());
		Assert.assertEquals(PERSON_ID, event.getQualificationSnapshot().getPersonId());
		Assert.assertEquals(COMPANY_ID, event.getQualificationSnapshot().getCompanyId());
		Assert.assertEquals(ADMISSION_ID, event.getQualificationSnapshot().getAdmissionId());
		assertUnmodifiable(event.getQualificationSnapshot().getAuthorizedAreaIds());
	}

	@Test
	public void firstRecordFromUnknownAcceptsEitherExplicitDirection() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierPresenceSnapshot unknown = presence(PERSON_ID, AREA_ID, SupplierPresence.UNKNOWN, 0L);
		SupplierVerification enterVerification = workflow.verify(qualification, executionOperator(), mapping(),
				unknown, BASE_TIME, "verification-first-enter");
		SupplierPassageResult entered = workflow.record(enterVerification, qualification, executionOperator(),
				mapping(), unknown, SupplierDirection.ENTER, BASE_TIME.plusSeconds(1), "event-first-enter");

		Assert.assertEquals(SupplierPresence.INSIDE, entered.getPresence().getPresence());
		Assert.assertEquals(1L, entered.getPresence().getVersion());

		SupplierVerification leaveVerification = workflow.verify(qualification, executionOperator(), mapping(),
				unknown, BASE_TIME, "verification-first-leave");
		SupplierPassageResult left = workflow.record(leaveVerification, qualification, executionOperator(),
				mapping(), unknown, SupplierDirection.LEAVE, BASE_TIME.plusSeconds(1), "event-first-leave");

		Assert.assertEquals(SupplierPresence.OUTSIDE, left.getPresence().getPresence());
		Assert.assertEquals(1L, left.getPresence().getVersion());
	}

	@Test
	public void recordLeaveCreatesOutsideStateFromInside() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierPresenceSnapshot inside = presence(PERSON_ID, AREA_ID, SupplierPresence.INSIDE, 8L);
		SupplierVerification verification = workflow.verify(qualification, executionOperator(), mapping(),
				inside, BASE_TIME, "verification-leave");

		SupplierPassageResult result = workflow.record(verification, qualification, executionOperator(),
				mapping(), inside, SupplierDirection.LEAVE, BASE_TIME.plusSeconds(30), "event-leave");

		Assert.assertEquals(SupplierPresence.OUTSIDE, result.getPresence().getPresence());
		Assert.assertEquals(9L, result.getPresence().getVersion());
		Assert.assertEquals(SupplierDirection.LEAVE, result.getEvent().getDirection());
	}

	@Test
	public void badgeSwapCannotBypassPersonAreaPresenceOrRepeatSameDirection() {
		SupplierQualificationSnapshot replacementBadge = qualification("badge-002", PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));
		SupplierPresenceSnapshot inside = presence(PERSON_ID, AREA_ID, SupplierPresence.INSIDE, 8L);
		final SupplierVerification replacementVerification = workflow.verify(replacementBadge,
				executionOperator(), mapping(), inside, BASE_TIME, "verification-replacement-badge");

		expectRecordViolation(SupplierRuleViolation.Code.DUPLICATE_DIRECTION, replacementVerification,
				replacementBadge, executionOperator(), mapping(), inside, SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(30));

		SupplierQualificationSnapshot qualification = validQualification();
		SupplierPresenceSnapshot outside = outsidePresence();
		SupplierVerification outsideVerification = workflow.verify(qualification, executionOperator(), mapping(),
				outside, BASE_TIME, "verification-outside");
		expectRecordViolation(SupplierRuleViolation.Code.DUPLICATE_DIRECTION, outsideVerification,
				qualification, executionOperator(), mapping(), outside, SupplierDirection.LEAVE,
				BASE_TIME.plusSeconds(30));
	}

	@Test
	public void recordRevalidatesCurrentQualificationFlagsApprovalAndArea() {
		SupplierQualificationSnapshot original = validQualification();
		SupplierVerification verification = workflow.verify(original, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-revalidate");

		for (SupplierQualificationSnapshot inactive : Arrays.asList(
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, false, true, true, true, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)),
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, false, true, true, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)),
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, true, false, true, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)),
				qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, true, true, false, true,
						BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID)))) {
			expectRecordViolation(SupplierRuleViolation.Code.INACTIVE_QUALIFICATION, verification, inactive,
					executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER,
					BASE_TIME.plusSeconds(30));
		}

		SupplierQualificationSnapshot approvalRevoked = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, false, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));
		expectRecordViolation(SupplierRuleViolation.Code.ADMISSION_NOT_APPROVED, verification,
				approvalRevoked, executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(30));

		SupplierQualificationSnapshot areaWithdrawn = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(OTHER_AREA_ID));
		expectRecordViolation(SupplierRuleViolation.Code.AREA_NOT_AUTHORIZED, verification, areaWithdrawn,
				executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(30));
	}

	@Test
	public void recordRevalidatesCurrentQualificationHalfOpenWindow() {
		SupplierQualificationSnapshot original = validQualification();
		SupplierVerification verification = workflow.verify(original, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-window");
		SupplierQualificationSnapshot notYetValid = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.plusSeconds(31),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));
		SupplierQualificationSnapshot expired = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(30), set(AREA_ID));

		expectRecordViolation(SupplierRuleViolation.Code.QUALIFICATION_NOT_YET_VALID, verification,
				notYetValid, executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(30));
		expectRecordViolation(SupplierRuleViolation.Code.QUALIFICATION_EXPIRED, verification, expired,
				executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(30));
	}

	@Test
	public void recordRejectsVerificationAtItsExclusiveExpiryBoundary() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierVerification verification = workflow.verify(qualification, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-expiry");

		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_EXPIRED, verification, qualification,
				executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER,
				BASE_TIME.plusSeconds(300));
	}

	@Test
	public void recordRejectsMismatchedOperatorPostAreaAndQualificationIdentity() {
		SupplierQualificationSnapshot original = validQualification();
		SupplierVerification verification = workflow.verify(original, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-binding");
		Instant recordTime = BASE_TIME.plusSeconds(30);

		SupplierOperator otherOperator = operator("operator-other", set("supplier:execute"), set(POST_ID));
		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, verification, original,
				otherOperator, mapping(), outsidePresence(), SupplierDirection.ENTER, recordTime);

		SupplierPostAreaMapping otherPost = SupplierPostAreaMapping.fromTrustedDirectory(OTHER_POST_ID,
				AREA_ID);
		SupplierOperator operatorAtOtherPost = operator(OPERATOR_ID, set("supplier:execute"),
				set(OTHER_POST_ID));
		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, verification, original,
				operatorAtOtherPost, otherPost, outsidePresence(), SupplierDirection.ENTER, recordTime);

		SupplierQualificationSnapshot otherBadge = qualification("badge-other", PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));
		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, verification, otherBadge,
				executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER, recordTime);

		SupplierQualificationSnapshot otherPerson = qualification(BADGE_ID, "person-other", COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));
		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, verification, otherPerson,
				executionOperator(), mapping(),
				presence("person-other", AREA_ID, SupplierPresence.OUTSIDE, 7L), SupplierDirection.ENTER,
				recordTime);

		SupplierQualificationSnapshot otherAdmission = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				"admission-other", true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(AREA_ID));
		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, verification, otherAdmission,
				executionOperator(), mapping(), outsidePresence(), SupplierDirection.ENTER, recordTime);

		SupplierQualificationSnapshot otherAreaQualification = qualification(BADGE_ID, PERSON_ID, COMPANY_ID,
				ADMISSION_ID, true, true, true, true, true, BASE_TIME.minusSeconds(60),
				BASE_TIME.plusSeconds(3600), set(OTHER_AREA_ID));
		SupplierPostAreaMapping otherArea = SupplierPostAreaMapping.fromTrustedDirectory(POST_ID,
				OTHER_AREA_ID);
		expectRecordViolation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, verification,
				otherAreaQualification, executionOperator(), otherArea,
				presence(PERSON_ID, OTHER_AREA_ID, SupplierPresence.OUTSIDE, 7L), SupplierDirection.ENTER,
				recordTime);
	}

	@Test
	public void recordRejectsWrongPresenceSubjectAreaAndOldVersion() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierVerification verification = workflow.verify(qualification, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-version");
		Instant recordTime = BASE_TIME.plusSeconds(30);

		expectRecordViolation(SupplierRuleViolation.Code.PRESENCE_MISMATCH, verification, qualification,
				executionOperator(), mapping(),
				presence("person-other", AREA_ID, SupplierPresence.OUTSIDE, 7L), SupplierDirection.ENTER,
				recordTime);
		expectRecordViolation(SupplierRuleViolation.Code.PRESENCE_MISMATCH, verification, qualification,
				executionOperator(), mapping(),
				presence(PERSON_ID, OTHER_AREA_ID, SupplierPresence.OUTSIDE, 7L), SupplierDirection.ENTER,
				recordTime);
		expectRecordViolation(SupplierRuleViolation.Code.VERSION_CONFLICT, verification, qualification,
				executionOperator(), mapping(),
				presence(PERSON_ID, AREA_ID, SupplierPresence.OUTSIDE, 8L), SupplierDirection.ENTER,
				recordTime);
	}

	@Test
	public void recordRechecksCurrentOperatorPermissionAndPostAuthorization() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierVerification verification = workflow.verify(qualification, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-authority");

		expectRecordViolation(SupplierRuleViolation.Code.MISSING_PERMISSION, verification, qualification,
				operator(OPERATOR_ID, Collections.<String>emptySet(), set(POST_ID)), mapping(), outsidePresence(),
				SupplierDirection.ENTER, BASE_TIME.plusSeconds(30));
		expectRecordViolation(SupplierRuleViolation.Code.UNAUTHORIZED_POST, verification, qualification,
				operator(OPERATOR_ID, set("supplier:execute"), set(OTHER_POST_ID)), mapping(), outsidePresence(),
				SupplierDirection.ENTER, BASE_TIME.plusSeconds(30));
	}

	@Test
	public void successfulRecordMakesVerificationOldForTheNextVersion() {
		SupplierQualificationSnapshot qualification = validQualification();
		SupplierVerification verification = workflow.verify(qualification, executionOperator(), mapping(),
				outsidePresence(), BASE_TIME, "verification-once");
		SupplierPassageResult first = workflow.record(verification, qualification, executionOperator(), mapping(),
				outsidePresence(), SupplierDirection.ENTER, BASE_TIME.plusSeconds(30), "event-first");

		expectRecordViolation(SupplierRuleViolation.Code.VERSION_CONFLICT, verification, qualification,
				executionOperator(), mapping(), first.getPresence(), SupplierDirection.LEAVE,
				BASE_TIME.plusSeconds(31));
	}

	private void expectVerifyViolation(SupplierRuleViolation.Code code,
			final SupplierQualificationSnapshot qualification, final Instant now) {
		expectViolation(code, new CheckedAction() {
			@Override
			public void run() {
				workflow.verify(qualification, executionOperator(), mapping(), outsidePresence(), now,
						"verification-invalid");
			}
		});
	}

	private void expectRecordViolation(SupplierRuleViolation.Code code,
			final SupplierVerification verification, final SupplierQualificationSnapshot qualification,
			final SupplierOperator operator, final SupplierPostAreaMapping postArea,
			final SupplierPresenceSnapshot presence, final SupplierDirection direction, final Instant now) {
		expectViolation(code, new CheckedAction() {
			@Override
			public void run() {
				workflow.record(verification, qualification, operator, postArea, presence, direction, now,
						"event-invalid");
			}
		});
	}

	private void expectViolation(SupplierRuleViolation.Code code, CheckedAction action) {
		try {
			action.run();
			Assert.fail("预期供应商通行规则拒绝操作");
		} catch (SupplierRuleViolation violation) {
			Assert.assertEquals(code, violation.getCode());
		}
	}

	private SupplierQualificationSnapshot validQualification() {
		return qualification(BADGE_ID, PERSON_ID, COMPANY_ID, ADMISSION_ID, true, true, true, true, true,
				BASE_TIME.minusSeconds(60), BASE_TIME.plusSeconds(3600), set(AREA_ID));
	}

	private SupplierQualificationSnapshot qualification(String badgeId, String personId, String companyId,
			String admissionId, boolean badgeActive, boolean personActive, boolean companyActive,
			boolean admissionActive, boolean admissionApproved, Instant validFrom, Instant validUntil,
			Set<String> authorizedAreaIds) {
		return SupplierQualificationSnapshot.fromTrustedSource(badgeId, personId, companyId, admissionId,
				badgeActive, personActive, companyActive, admissionActive, admissionApproved, validFrom,
				validUntil, authorizedAreaIds, "供应商人员", "供应商公司",
				"https://example.invalid/photo/person-001", "13800000000", "被访员工", "13900000000");
	}

	private SupplierOperator executionOperator() {
		return operator(OPERATOR_ID, set("supplier:execute"), set(POST_ID));
	}

	private SupplierOperator operator(String operatorId, Set<String> permissions, Set<String> postIds) {
		return SupplierOperator.authenticated(operatorId, permissions, postIds);
	}

	private SupplierPostAreaMapping mapping() {
		return SupplierPostAreaMapping.fromTrustedDirectory(POST_ID, AREA_ID);
	}

	private SupplierPresenceSnapshot outsidePresence() {
		return presence(PERSON_ID, AREA_ID, SupplierPresence.OUTSIDE, 7L);
	}

	private SupplierPresenceSnapshot presence(String personId, String areaId, SupplierPresence value,
			long version) {
		return SupplierPresenceSnapshot.current(personId, areaId, value, version);
	}

	private void assertUnmodifiable(final Set<String> values) {
		try {
			values.clear();
			Assert.fail("预期集合不可修改");
		} catch (UnsupportedOperationException expected) {
			// 不可变集合符合可信快照契约。
		}
	}

	private static Set<String> set(String... values) {
		return new LinkedHashSet<>(Arrays.asList(values));
	}

	private interface CheckedAction {
		void run();
	}
}
