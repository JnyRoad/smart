package com.tce.smart.platform.core.client.release;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfidentialReleaseWorkflowTest {

	private static final Instant BASE_TIME = Instant.parse("2026-09-05T08:00:00Z");
	private static final String RELEASE_ID = "release-001";
	private static final String APPLICANT_ID = "applicant-001";
	private static final String APPROVER_ID = "approver-001";
	private static final String ORIGIN_POST = "post-origin";
	private static final String DESTINATION_POST = "post-destination";

	private final ConfidentialReleaseWorkflow workflow = new ConfidentialReleaseWorkflow();

	@Test
	public void createsPendingSnapshotAndNormalizesImmutableCollections() {
		List<String> materials = new ArrayList<>(Collections.singletonList("  保密图纸  "));
		List<String> seals = new ArrayList<>(Arrays.asList(" 00017 ", "SEAL-2"));
		ReleaseApplicationRequest request = request("技术资料", "跨区交接", materials, seals,
				ORIGIN_POST, DESTINATION_POST);

		ConfidentialRelease created = workflow.create(request, creationContext(APPROVER_ID), BASE_TIME,
				RELEASE_ID, "event-create");

		materials.add("不得写入快照");
		seals.set(0, "9");
		Assert.assertEquals(ReleaseStatus.PENDING, created.getStatus());
		Assert.assertEquals(1L, created.getVersion());
		Assert.assertEquals(APPLICANT_ID, created.getApplicantId());
		Assert.assertEquals(APPROVER_ID, created.getAssignedApproverId());
		Assert.assertEquals(Collections.singletonList("保密图纸"), created.getMaterials());
		Assert.assertEquals(Arrays.asList("00017", "SEAL-2"), created.getSealCodes());
		Assert.assertEquals(1, created.getAuditTrail().size());
		Assert.assertEquals("event-create", created.getAuditTrail().get(0).getEventId());
		Assert.assertEquals(ReleaseAction.CREATE, created.getAuditTrail().get(0).getAction());
		Assert.assertEquals(BASE_TIME, created.getAuditTrail().get(0).getOccurredAt());
		assertUnmodifiable(created.getMaterials());
		assertUnmodifiable(created.getSealCodes());
		assertUnmodifiable(created.getAuditTrail());
	}

	@Test
	public void createRequiresApplyPermissionFromAuthenticatedPrincipal() {
		ReleasePrincipal applicant = ReleasePrincipal.authenticated(APPLICANT_ID,
				Collections.<String>emptySet(), Collections.<String>emptySet());
		ReleaseCreationContext context = ReleaseCreationContext.verified(applicant,
				set(ORIGIN_POST, DESTINATION_POST), APPROVER_ID);

		expectViolation(ReleaseRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				workflow.create(validRequest(), context, BASE_TIME, RELEASE_ID, "event-create");
			}
		});
	}

	@Test
	public void createRequiresTitleReasonAndAtLeastOneMaterial() {
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_INPUT,
				request(" ", "原因", Collections.singletonList("图纸"), Collections.<String>emptyList(),
						ORIGIN_POST, DESTINATION_POST));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_INPUT,
				request("标题", " ", Collections.singletonList("图纸"), Collections.<String>emptyList(),
						ORIGIN_POST, DESTINATION_POST));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_INPUT,
				request("标题", "原因", Collections.<String>emptyList(), Collections.<String>emptyList(),
						ORIGIN_POST, DESTINATION_POST));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_INPUT,
				request("标题", "原因", Collections.singletonList("  "), Collections.<String>emptyList(),
						ORIGIN_POST, DESTINATION_POST));
	}

	@Test
	public void createRequiresDistinctPostsFromServerCandidateSet() {
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_ROUTE,
				request("标题", "原因", Collections.singletonList("图纸"), Collections.<String>emptyList(),
						"post-unknown", DESTINATION_POST));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_ROUTE,
				request("标题", "原因", Collections.singletonList("图纸"), Collections.<String>emptyList(),
						ORIGIN_POST, "post-unknown"));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_ROUTE,
				request("标题", "原因", Collections.singletonList("图纸"), Collections.<String>emptyList(),
						ORIGIN_POST, ORIGIN_POST));
	}

	@Test
	public void createRejectsBlankDuplicateAndControlCharacterSealCodes() {
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_SEAL,
				request("标题", "原因", Collections.singletonList("图纸"), Collections.singletonList("  "),
						ORIGIN_POST, DESTINATION_POST));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_SEAL,
				request("标题", "原因", Collections.singletonList("图纸"), Arrays.asList("0007", " 0007 "),
						ORIGIN_POST, DESTINATION_POST));
		expectCreateViolation(ReleaseRuleViolation.Code.INVALID_SEAL,
				request("标题", "原因", Collections.singletonList("图纸"), Collections.singletonList("00\n07"),
						ORIGIN_POST, DESTINATION_POST));
	}

	@Test
	public void assignedApproverCanApproveWithoutMutatingPendingSnapshot() {
		ConfidentialRelease pending = createPending(APPROVER_ID);
		ReleasePrincipal approver = principal(APPROVER_ID, "item-pass:approve");

		ConfidentialRelease approved = workflow.approve(pending, approver, 1L, BASE_TIME.plusSeconds(1),
				"event-approve");

		Assert.assertEquals(ReleaseStatus.PENDING, pending.getStatus());
		Assert.assertEquals(1L, pending.getVersion());
		Assert.assertEquals(1, pending.getAuditTrail().size());
		Assert.assertEquals(ReleaseStatus.APPROVED, approved.getStatus());
		Assert.assertEquals(2L, approved.getVersion());
		Assert.assertEquals(2, approved.getAuditTrail().size());
		ReleaseAuditEvent event = approved.getAuditTrail().get(1);
		Assert.assertEquals("event-approve", event.getEventId());
		Assert.assertEquals(ReleaseAction.APPROVE, event.getAction());
		Assert.assertEquals(ReleaseStatus.PENDING, event.getFromStatus());
		Assert.assertEquals(ReleaseStatus.APPROVED, event.getToStatus());
	}

	@Test
	public void approvalRequiresCurrentAssignedApprover() {
		ConfidentialRelease pending = createPending(APPROVER_ID);

		expectViolation(ReleaseRuleViolation.Code.NOT_ASSIGNED_APPROVER, new CheckedAction() {
			@Override
			public void run() {
				workflow.approve(pending, principal("other-approver", "item-pass:approve"), 1L,
						BASE_TIME.plusSeconds(1), "event-approve");
			}
		});
	}

	@Test
	public void approvalRequiresApprovePermission() {
		ConfidentialRelease pending = createPending(APPROVER_ID);
		ReleasePrincipal approverWithoutPermission = principal(APPROVER_ID);

		expectViolation(ReleaseRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				workflow.approve(pending, approverWithoutPermission, 1L, BASE_TIME.plusSeconds(1),
						"event-approve");
			}
		});
	}

	@Test
	public void applicantCannotApproveOwnRelease() {
		ConfidentialRelease pending = createPending(APPLICANT_ID);

		expectViolation(ReleaseRuleViolation.Code.SELF_APPROVAL, new CheckedAction() {
			@Override
			public void run() {
				workflow.approve(pending, principal(APPLICANT_ID, "item-pass:approve"), 1L,
						BASE_TIME.plusSeconds(1), "event-approve");
			}
		});
	}

	@Test
	public void rejectRequiresNonBlankReasonAndRecordsIt() {
		ConfidentialRelease pending = createPending(APPROVER_ID);
		ReleasePrincipal approver = principal(APPROVER_ID, "item-pass:approve");

		expectViolation(ReleaseRuleViolation.Code.INVALID_REJECTION_REASON, new CheckedAction() {
			@Override
			public void run() {
				workflow.reject(pending, approver, 1L, " \t ", BASE_TIME.plusSeconds(1), "event-reject");
			}
		});

		ConfidentialRelease rejected = workflow.reject(pending, approver, 1L, " 资料不完整 ",
				BASE_TIME.plusSeconds(1), "event-reject");
		Assert.assertEquals(ReleaseStatus.REJECTED, rejected.getStatus());
		Assert.assertEquals("资料不完整", rejected.getAuditTrail().get(1).getReason());
	}

	@Test
	public void approvalActionsOnlyAcceptPendingState() {
		ConfidentialRelease approved = createApproved();
		ReleasePrincipal approver = principal(APPROVER_ID, "item-pass:approve");

		expectViolation(ReleaseRuleViolation.Code.INVALID_STATUS, new CheckedAction() {
			@Override
			public void run() {
				workflow.approve(approved, approver, 2L, BASE_TIME.plusSeconds(2), "event-approve-again");
			}
		});
		expectViolation(ReleaseRuleViolation.Code.INVALID_STATUS, new CheckedAction() {
			@Override
			public void run() {
				workflow.reject(approved, approver, 2L, "不再允许驳回", BASE_TIME.plusSeconds(2),
						"event-reject-late");
			}
		});
	}

	@Test
	public void transitionRequiresExpectedVersion() {
		ConfidentialRelease pending = createPending(APPROVER_ID);

		expectViolation(ReleaseRuleViolation.Code.VERSION_CONFLICT, new CheckedAction() {
			@Override
			public void run() {
				workflow.approve(pending, principal(APPROVER_ID, "item-pass:approve"), 0L,
						BASE_TIME.plusSeconds(1), "event-approve");
			}
		});
	}

	@Test
	public void departRequiresExecutePermissionAndOriginPostAuthorization() {
		ConfidentialRelease approved = createApproved();
		CardEvidence security = securityEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
				"security-001", BASE_TIME.plusSeconds(2));

		expectViolation(ReleaseRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, principal("security-001"), 2L, EscortMode.POSITIONING_LOCK,
						"00042", security, null, BASE_TIME.plusSeconds(2), "event-depart");
			}
		});
		expectViolation(ReleaseRuleViolation.Code.UNAUTHORIZED_POST, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, principal("security-001", set("item-pass:execute"),
						set(DESTINATION_POST)), 2L, EscortMode.POSITIONING_LOCK, "00042", security, null,
						BASE_TIME.plusSeconds(2), "event-depart");
			}
		});
	}

	@Test
	public void securityEvidenceBindsRoleHolderReleasePostActionOperatorAndShortLifetime() {
		ConfidentialRelease approved = createApproved();
		ReleasePrincipal operator = executionPrincipal("security-001", ORIGIN_POST);
		Instant now = BASE_TIME.plusSeconds(2);
		List<CardEvidence> invalidEvidence = Arrays.asList(
				evidence(CardRole.ESCORT, "security-001", RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
						"security-001", now.minusSeconds(10), now.plusSeconds(30)),
				evidence(CardRole.SECURITY_CHECK, "other-holder", RELEASE_ID, ORIGIN_POST,
						ReleaseAction.DEPART, "security-001", now.minusSeconds(10), now.plusSeconds(30)),
				evidence(CardRole.SECURITY_CHECK, "security-001", "other-release", ORIGIN_POST,
						ReleaseAction.DEPART, "security-001", now.minusSeconds(10), now.plusSeconds(30)),
				evidence(CardRole.SECURITY_CHECK, "security-001", RELEASE_ID, DESTINATION_POST,
						ReleaseAction.DEPART, "security-001", now.minusSeconds(10), now.plusSeconds(30)),
				evidence(CardRole.SECURITY_CHECK, "security-001", RELEASE_ID, ORIGIN_POST,
						ReleaseAction.ARRIVE, "security-001", now.minusSeconds(10), now.plusSeconds(30)),
				evidence(CardRole.SECURITY_CHECK, "security-001", RELEASE_ID, ORIGIN_POST,
						ReleaseAction.DEPART, "other-operator", now.minusSeconds(10), now.plusSeconds(30)),
				evidence(CardRole.SECURITY_CHECK, "security-001", RELEASE_ID, ORIGIN_POST,
						ReleaseAction.DEPART, "security-001", now.minusSeconds(60), now.minusSeconds(1)),
				evidence(CardRole.SECURITY_CHECK, "security-001", RELEASE_ID, ORIGIN_POST,
						ReleaseAction.DEPART, "security-001", now.minusSeconds(10), now),
				evidence(CardRole.SECURITY_CHECK, "security-001", RELEASE_ID, ORIGIN_POST,
						ReleaseAction.DEPART, "security-001", now.minusSeconds(1), now.plusSeconds(301))
		);

		for (final CardEvidence invalid : invalidEvidence) {
			expectViolation(ReleaseRuleViolation.Code.INVALID_CARD_EVIDENCE, new CheckedAction() {
				@Override
				public void run() {
					workflow.depart(approved, operator, 2L, EscortMode.POSITIONING_LOCK, "00042",
							invalid, null, now, "event-depart");
				}
			});
		}
	}

	@Test
	public void positioningLockDepartureMovesToTransportingAndPreservesLeadingZero() {
		ConfidentialRelease approved = createApproved();
		Instant now = BASE_TIME.plusSeconds(2);
		ConfidentialRelease transporting = workflow.depart(approved,
				executionPrincipal("security-001", ORIGIN_POST), 2L, EscortMode.POSITIONING_LOCK,
				" 00042 ", securityEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
						"security-001", now), null, now, "event-depart");

		Assert.assertEquals(ReleaseStatus.APPROVED, approved.getStatus());
		Assert.assertEquals(ReleaseStatus.TRANSPORTING, transporting.getStatus());
		Assert.assertEquals(EscortMode.POSITIONING_LOCK, transporting.getEscortMode());
		Assert.assertEquals("00042", transporting.getPositioningLockId());
		Assert.assertEquals(3L, transporting.getVersion());
		ReleaseAuditEvent departEvent = transporting.getAuditTrail().get(2);
		Assert.assertEquals(ReleaseAction.DEPART, departEvent.getAction());
		Assert.assertEquals(ORIGIN_POST, departEvent.getPostId());
		Assert.assertEquals(CardRole.SECURITY_CHECK, departEvent.getSecurityEvidence().getRole());
		Assert.assertNull(departEvent.getEscortEvidence());
	}

	@Test
	public void positioningLockModeRequiresOnlyNonBlankLockId() {
		ConfidentialRelease approved = createApproved();
		Instant now = BASE_TIME.plusSeconds(2);
		ReleasePrincipal operator = executionPrincipal("security-001", ORIGIN_POST);
		CardEvidence security = securityEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
				"security-001", now);
		CardEvidence escort = escortEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
				"security-001", now);

		expectViolation(ReleaseRuleViolation.Code.INVALID_ESCORT, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, operator, 2L, EscortMode.POSITIONING_LOCK, " ", security,
						null, now, "event-depart");
			}
		});
		expectViolation(ReleaseRuleViolation.Code.INVALID_ESCORT, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, operator, 2L, EscortMode.POSITIONING_LOCK, "00042", security,
						escort, now, "event-depart");
			}
		});
	}

	@Test
	public void escortCardDepartureRequiresDistinctEscortRoleEvidence() {
		ConfidentialRelease approved = createApproved();
		Instant now = BASE_TIME.plusSeconds(2);
		ReleasePrincipal operator = executionPrincipal("security-001", ORIGIN_POST);
		CardEvidence security = securityEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
				"security-001", now);

		expectViolation(ReleaseRuleViolation.Code.INVALID_ESCORT, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, operator, 2L, EscortMode.ESCORT_CARD, null, security,
						null, now, "event-depart");
			}
		});
		expectViolation(ReleaseRuleViolation.Code.INVALID_ESCORT, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, operator, 2L, EscortMode.ESCORT_CARD, null, security,
						security, now, "event-depart");
			}
		});
		expectViolation(ReleaseRuleViolation.Code.INVALID_ESCORT, new CheckedAction() {
			@Override
			public void run() {
				workflow.depart(approved, operator, 2L, EscortMode.ESCORT_CARD, null, security,
						evidence(CardRole.ESCORT, "security-001", RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART,
								"security-001", now.minusSeconds(10), now.plusSeconds(30)),
						now, "event-depart");
			}
		});

		ConfidentialRelease transporting = workflow.depart(approved, operator, 2L,
				EscortMode.ESCORT_CARD, null, security,
				escortEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART, "security-001", now),
				now, "event-depart");
		Assert.assertEquals(ReleaseStatus.TRANSPORTING, transporting.getStatus());
		Assert.assertEquals(EscortMode.ESCORT_CARD, transporting.getEscortMode());
		Assert.assertNull(transporting.getPositioningLockId());
		ReleaseAuditEvent departEvent = transporting.getAuditTrail().get(2);
		Assert.assertEquals(CardRole.SECURITY_CHECK, departEvent.getSecurityEvidence().getRole());
		Assert.assertEquals(CardRole.ESCORT, departEvent.getEscortEvidence().getRole());
	}

	@Test
	public void arrivalRequiresExecutePermissionAndDestinationPostAuthorization() {
		ConfidentialRelease transporting = createTransportingWithLock("00042");
		Instant now = BASE_TIME.plusSeconds(3);
		CardEvidence security = securityEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
				"security-002", now);

		expectViolation(ReleaseRuleViolation.Code.MISSING_PERMISSION, new CheckedAction() {
			@Override
			public void run() {
				workflow.arrive(transporting, principal("security-002"), 3L,
						EscortMode.POSITIONING_LOCK, "00042", security, null, now, "event-arrive");
			}
		});
		expectViolation(ReleaseRuleViolation.Code.UNAUTHORIZED_POST, new CheckedAction() {
			@Override
			public void run() {
				workflow.arrive(transporting, executionPrincipal("security-002", ORIGIN_POST), 3L,
						EscortMode.POSITIONING_LOCK, "00042", security, null, now, "event-arrive");
			}
		});
	}

	@Test
	public void arrivalRequiresTransportingState() {
		ConfidentialRelease approved = createApproved();
		Instant now = BASE_TIME.plusSeconds(2);

		expectViolation(ReleaseRuleViolation.Code.INVALID_STATUS, new CheckedAction() {
			@Override
			public void run() {
				workflow.arrive(approved, executionPrincipal("security-002", DESTINATION_POST), 2L,
						EscortMode.POSITIONING_LOCK, "00042",
						securityEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
								"security-002", now), null, now, "event-arrive");
			}
		});
	}

	@Test
	public void arrivalRevalidatesSecurityAndCompletesLockFlow() {
		ConfidentialRelease transporting = createTransportingWithLock("00042");
		Instant now = BASE_TIME.plusSeconds(3);

		ConfidentialRelease completed = workflow.arrive(transporting,
				executionPrincipal("security-002", DESTINATION_POST), 3L, EscortMode.POSITIONING_LOCK,
				"00042", securityEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
						"security-002", now), null, now, "event-arrive");

		Assert.assertEquals(ReleaseStatus.TRANSPORTING, transporting.getStatus());
		Assert.assertEquals(ReleaseStatus.COMPLETED, completed.getStatus());
		Assert.assertEquals(4L, completed.getVersion());
		Assert.assertEquals(4, completed.getAuditTrail().size());
		ReleaseAuditEvent arriveEvent = completed.getAuditTrail().get(3);
		Assert.assertEquals(ReleaseAction.ARRIVE, arriveEvent.getAction());
		Assert.assertEquals(DESTINATION_POST, arriveEvent.getPostId());
		Assert.assertEquals(ReleaseAction.ARRIVE, arriveEvent.getSecurityEvidence().getAction());
		Assert.assertNull(arriveEvent.getEscortEvidence());
	}

	@Test
	public void arrivalCannotChangeEscortMode() {
		ConfidentialRelease transporting = createTransportingWithLock("00042");
		Instant now = BASE_TIME.plusSeconds(3);
		ReleasePrincipal operator = executionPrincipal("security-002", DESTINATION_POST);

		expectViolation(ReleaseRuleViolation.Code.ESCORT_METHOD_CHANGED, new CheckedAction() {
			@Override
			public void run() {
				workflow.arrive(transporting, operator, 3L, EscortMode.ESCORT_CARD, null,
						securityEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
								"security-002", now),
						escortEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
								"security-002", now), now, "event-arrive");
			}
		});
	}

	@Test
	public void arrivalCannotChangeLockIdEvenWhenNumericValueMatches() {
		ConfidentialRelease transporting = createTransportingWithLock("00042");
		Instant now = BASE_TIME.plusSeconds(3);

		expectViolation(ReleaseRuleViolation.Code.LOCK_ID_CHANGED, new CheckedAction() {
			@Override
			public void run() {
				workflow.arrive(transporting, executionPrincipal("security-002", DESTINATION_POST), 3L,
						EscortMode.POSITIONING_LOCK, "42",
						securityEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
								"security-002", now), null, now, "event-arrive");
			}
		});
	}

	@Test
	public void escortCardArrivalRequiresFreshEscortEvidence() {
		ConfidentialRelease approved = createApproved();
		Instant departTime = BASE_TIME.plusSeconds(2);
		ConfidentialRelease transporting = workflow.depart(approved,
				executionPrincipal("security-001", ORIGIN_POST), 2L, EscortMode.ESCORT_CARD, null,
				securityEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART, "security-001", departTime),
				escortEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART, "security-001", departTime),
				departTime, "event-depart");
		Instant arriveTime = BASE_TIME.plusSeconds(3);
		ReleasePrincipal destinationOperator = executionPrincipal("security-002", DESTINATION_POST);
		CardEvidence security = securityEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
				"security-002", arriveTime);

		expectViolation(ReleaseRuleViolation.Code.INVALID_ESCORT, new CheckedAction() {
			@Override
			public void run() {
				workflow.arrive(transporting, destinationOperator, 3L, EscortMode.ESCORT_CARD, null,
						security, null, arriveTime, "event-arrive");
			}
		});

		ConfidentialRelease completed = workflow.arrive(transporting, destinationOperator, 3L,
				EscortMode.ESCORT_CARD, null, security,
				escortEvidence(RELEASE_ID, DESTINATION_POST, ReleaseAction.ARRIVE,
						"security-002", arriveTime), arriveTime, "event-arrive");
		Assert.assertEquals(ReleaseStatus.COMPLETED, completed.getStatus());
	}

	private ConfidentialRelease createPending(String assignedApproverId) {
		return workflow.create(validRequest(), creationContext(assignedApproverId), BASE_TIME,
				RELEASE_ID, "event-create");
	}

	private ConfidentialRelease createApproved() {
		ConfidentialRelease pending = createPending(APPROVER_ID);
		return workflow.approve(pending, principal(APPROVER_ID, "item-pass:approve"), 1L,
				BASE_TIME.plusSeconds(1), "event-approve");
	}

	private ConfidentialRelease createTransportingWithLock(String lockId) {
		ConfidentialRelease approved = createApproved();
		Instant now = BASE_TIME.plusSeconds(2);
		return workflow.depart(approved, executionPrincipal("security-001", ORIGIN_POST), 2L,
				EscortMode.POSITIONING_LOCK, lockId,
				securityEvidence(RELEASE_ID, ORIGIN_POST, ReleaseAction.DEPART, "security-001", now),
				null, now, "event-depart");
	}

	private ReleaseApplicationRequest validRequest() {
		return request("技术资料", "跨区交接", Collections.singletonList("保密图纸"),
				Arrays.asList("00017", "SEAL-2"), ORIGIN_POST, DESTINATION_POST);
	}

	private ReleaseApplicationRequest request(String title, String reason, List<String> materials,
			List<String> seals, String originPost, String destinationPost) {
		return new ReleaseApplicationRequest(title, reason, materials, seals, originPost, destinationPost);
	}

	private ReleaseCreationContext creationContext(String assignedApproverId) {
		return ReleaseCreationContext.verified(principal(APPLICANT_ID, "item-pass:apply"),
				set(ORIGIN_POST, DESTINATION_POST), assignedApproverId);
	}

	private ReleasePrincipal principal(String actorId, String... permissions) {
		return principal(actorId, set(permissions), Collections.<String>emptySet());
	}

	private ReleasePrincipal principal(String actorId, Set<String> permissions, Set<String> posts) {
		return ReleasePrincipal.authenticated(actorId, permissions, posts);
	}

	private ReleasePrincipal executionPrincipal(String actorId, String postId) {
		return principal(actorId, set("item-pass:execute"), set(postId));
	}

	private CardEvidence securityEvidence(String releaseId, String postId, ReleaseAction action,
			String operatorId, Instant now) {
		return evidence(CardRole.SECURITY_CHECK, operatorId, releaseId, postId, action, operatorId,
				now.minusSeconds(10), now.plusSeconds(30));
	}

	private CardEvidence escortEvidence(String releaseId, String postId, ReleaseAction action,
			String operatorId, Instant now) {
		return evidence(CardRole.ESCORT, "escort-001", releaseId, postId, action, operatorId,
				now.minusSeconds(10), now.plusSeconds(30));
	}

	private CardEvidence evidence(CardRole role, String holderId, String releaseId, String postId,
			ReleaseAction action, String operatorId, Instant verifiedAt, Instant validUntil) {
		String evidenceId = "card-" + role.name().toLowerCase() + "-" + action.name().toLowerCase();
		return CardEvidence.verified(evidenceId, role, holderId, releaseId, postId, action,
				operatorId, verifiedAt, validUntil);
	}

	private void expectCreateViolation(ReleaseRuleViolation.Code code, final ReleaseApplicationRequest request) {
		expectViolation(code, new CheckedAction() {
			@Override
			public void run() {
				workflow.create(request, creationContext(APPROVER_ID), BASE_TIME, RELEASE_ID, "event-create");
			}
		});
	}

	private void expectViolation(ReleaseRuleViolation.Code code, CheckedAction action) {
		try {
			action.run();
			Assert.fail("预期领域规则拒绝操作");
		} catch (ReleaseRuleViolation violation) {
			Assert.assertEquals(code, violation.getCode());
		}
	}

	private void assertUnmodifiable(final List<?> values) {
		try {
			values.clear();
			Assert.fail("预期集合不可修改");
		} catch (UnsupportedOperationException expected) {
			// 不可变集合符合领域快照契约。
		}
	}

	private static Set<String> set(String... values) {
		return new LinkedHashSet<>(Arrays.asList(values));
	}

	private interface CheckedAction {
		void run();
	}
}
