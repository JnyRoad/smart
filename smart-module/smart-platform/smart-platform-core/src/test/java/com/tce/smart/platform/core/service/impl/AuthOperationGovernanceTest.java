package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.config.AuthOperationGovernanceProperties;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceConflictException;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionResult;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.Actor;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ManualVerificationCommand;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.RetryCommand;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.TargetSnapshot;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.ResourceDecision;
import com.tce.smart.platform.core.entity.SmtAuthGovernanceAction;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import com.tce.smart.platform.core.mapper.AuthOperationGovernanceMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 治理动作门禁测试；每个断言都对应一次可能造成误重发或越权的生产回归。 */
@RunWith(MockitoJUnitRunner.class)
public class AuthOperationGovernanceTest {

	private static final long TARGET_ID = 9007199254740993L;
	private static final long ATTEMPT_ID = 9007199254740995L;
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 6, 1, 0);

	@Mock
	private AuthOperationGovernanceMapper mapper;
	@Mock
	private AuthOperationVersionService versions;

	private AuthOperationGovernanceService service;
	private AuthOperationGovernanceProperties properties;
	private Actor parkActor;

	@Before
	public void setUp() {
		properties = new AuthOperationGovernanceProperties();
		properties.setActionsEnabled(true);
		properties.setMaxRetryAttempts(3);
		service = new AuthOperationGovernanceService(mapper, versions, properties);
		parkActor = Actor.builder().userId(71).username("治理员")
				.parkIds(Collections.singletonList(17))
				.permissions(Arrays.asList("platform_auth_operation_retry",
						"platform_auth_operation_manual_verify", "platform_auth_operation_review_view"))
				.build();
		when(mapper.now()).thenReturn(NOW);
		when(mapper.releaseKnownUnsentResourceAttempt(anyString(), anyLong(), anyLong(), anyLong(), any(LocalDateTime.class)))
				.thenReturn(1);
	}

	@Test
	public void expiredClaimWithoutAnySendTraceIsRequeuedAfterImmutableAudit() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		stubTarget(target, attempt(2));
		when(mapper.countAttemptTrace(ATTEMPT_ID)).thenReturn(0);
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);
		when(mapper.expireKnownUnsentAttempt(eq(ATTEMPT_ID), eq(TARGET_ID), eq("lease-2"), eq(NOW))).thenReturn(1);
		when(mapper.requeueKnownUnsentTarget(eq(TARGET_ID), eq("lease-2"), eq(7L), eq(NOW))).thenReturn(1);

		ActionResult result = service.retryKnownUnsent(parkActor, retry("retry-1", "重新领取"));

		assertThat(result.getOutcome()).isEqualTo("REQUEUED");
		ArgumentCaptor<SmtAuthGovernanceAction> audit = ArgumentCaptor.forClass(SmtAuthGovernanceAction.class);
		InOrder order = inOrder(mapper);
		order.verify(mapper).insertAction(audit.capture());
		order.verify(mapper).expireKnownUnsentAttempt(ATTEMPT_ID, TARGET_ID, "lease-2", NOW);
		order.verify(mapper).releaseKnownUnsentResourceAttempt("resource-17", 7L, TARGET_ID, ATTEMPT_ID, NOW);
		order.verify(mapper).requeueKnownUnsentTarget(TARGET_ID, "lease-2", 7L, NOW);
		assertThat(audit.getValue().getExpectedAttemptNo()).isEqualTo(2);
	}

	@Test
	public void auditWriteFailurePreventsAnyRetryStateMutation() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		stubTarget(target, attempt(2));
		when(mapper.countAttemptTrace(ATTEMPT_ID)).thenReturn(0);
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(0);

		assertThatThrownBy(() -> service.retryKnownUnsent(parkActor, retry("audit-failed", "审计写入失败")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("审计记录保存失败");
		verify(mapper, never()).expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
		verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
	}

	@Test
	public void changedWorkerAtAttemptCasStopsBeforeTargetRequeue() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		stubTarget(target, attempt(2));
		when(mapper.countAttemptTrace(ATTEMPT_ID)).thenReturn(0);
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);
		when(mapper.expireKnownUnsentAttempt(ATTEMPT_ID, TARGET_ID, "lease-2", NOW)).thenReturn(0);

		assertThatThrownBy(() -> service.retryKnownUnsent(parkActor, retry("worker-changed", "领取者已经变化")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("作废旧尝试失败");
		verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
	}

	@Test
	public void anyTransportOrResultTraceRejectsRetryWithoutChangingAttemptOrTarget() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		stubTarget(target, attempt(2));
		when(mapper.countAttemptTrace(ATTEMPT_ID)).thenReturn(1);
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);

		ActionResult result = service.retryKnownUnsent(parkActor, retry("trace-present", "存在未知发送痕迹"));

		assertThat(result.getOutcome()).isEqualTo("REJECTED");
		assertThat(result.getReasonCode()).isEqualTo("SEND_TRACE_PRESENT");
		verify(mapper, never()).expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
		verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
	}

	@Test
	public void unknownOrAcceptedAttemptCannotBeInferredAsRetryable() {
		for (String status : Arrays.asList("VERIFYING", "WAITING_CONFIRM", "FAILED_TERMINAL")) {
			reset(mapper, versions);
			when(mapper.now()).thenReturn(NOW);
			TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
			stubTarget(target, attempt(2, status));
			when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);

			ActionResult result = service.retryKnownUnsent(parkActor, retry("status-" + status, "状态不可重试"));

			assertThat(result.getOutcome()).isEqualTo("REJECTED");
			assertThat(result.getReasonCode()).isEqualTo("ATTEMPT_NOT_CLAIMED");
			verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
		}
	}

	@Test
	public void changedVersionOrResourceOwnerRejectsRetry() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target);
		when(mapper.selectActionByKey(71, "owner-change", "TARGET:" + TARGET_ID)).thenReturn(null);
		when(mapper.hasCurrentSourceBinding(TARGET_ID, "resource-17", 7L)).thenReturn(1);
		when(versions.currentDesired("resource-17")).thenReturn(decision(8L, TARGET_ID, ATTEMPT_ID));
		when(mapper.lockTarget(TARGET_ID)).thenReturn(target);
		when(mapper.lockCurrentAttempt(TARGET_ID, ATTEMPT_ID)).thenReturn(attempt(2));
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);

		ActionResult result = service.retryKnownUnsent(parkActor, retry("owner-change", "代次已经变化"));

		assertThat(result.getReasonCode()).isEqualTo("RESOURCE_VERSION_CHANGED");
		verify(mapper, never()).expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
	}

	@Test
	public void differentBlockingAttemptRejectsRetryAsOwnerChange() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target);
		when(mapper.selectActionByKey(71, "blocker-change", "TARGET:" + TARGET_ID)).thenReturn(null);
		when(mapper.hasCurrentSourceBinding(TARGET_ID, "resource-17", 7L)).thenReturn(1);
		when(versions.currentDesired("resource-17")).thenReturn(decision(7L, TARGET_ID, ATTEMPT_ID + 1));
		when(mapper.lockTarget(TARGET_ID)).thenReturn(target);
		when(mapper.lockCurrentAttempt(TARGET_ID, ATTEMPT_ID)).thenReturn(attempt(2));
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);

		ActionResult result = service.retryKnownUnsent(parkActor,
				retry("blocker-change", "资源已有另一个阻断尝试"));

		assertThat(result.getOutcome()).isEqualTo("REJECTED");
		assertThat(result.getReasonCode()).isEqualTo("RESOURCE_OWNER_CHANGED");
		verify(mapper, never()).expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
	}

	@Test
	public void persistentAttemptNumberEnforcesConfiguredLimit() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 3);
		stubTarget(target, attempt(3));
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);

		ActionResult result = service.retryKnownUnsent(parkActor, RetryCommand.builder().targetId(TARGET_ID)
				.expectedOperationVersion(7L).expectedAttemptId(ATTEMPT_ID).expectedAttemptNo(3)
				.expectedState("EXECUTING").idempotencyKey("limit").reasonText("达到尝试上限").build());

		assertThat(result.getReasonCode()).isEqualTo("MAX_ATTEMPTS_REACHED");
		verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
	}

	@Test
	public void multipleCurrentSourceBindingsStillPermitOnePhysicalTargetRetry() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		stubTarget(target, attempt(2));
		when(mapper.hasCurrentSourceBinding(TARGET_ID, "resource-17", 7L)).thenReturn(2);
		when(mapper.countAttemptTrace(ATTEMPT_ID)).thenReturn(0);
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);
		when(mapper.expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any())).thenReturn(1);
		when(mapper.requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any())).thenReturn(1);

		ActionResult result = service.retryKnownUnsent(parkActor, retry("shared-sources", "共享来源仍绑定同一物理目标"));

		assertThat(result.getOutcome()).isEqualTo("REQUEUED");
	}

	@Test
	public void versionGateUsesResourceCoordinateIdInsteadOfPhysicalTargetResourceId() {
		TargetSnapshot target = target("EXECUTING", NOW.minusSeconds(1), 2);
		target.setResourceId("permission\u001fservice\u001fchannel");
		target.setResourceCoordId("resource-17");
		stubTarget(target, attempt(2));
		when(mapper.countAttemptTrace(ATTEMPT_ID)).thenReturn(0);
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);
		when(mapper.expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any())).thenReturn(1);
		when(mapper.requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any())).thenReturn(1);

		assertThat(service.retryKnownUnsent(parkActor, retry("coordinate-id", "按协调资源执行版本门禁"))
				.getOutcome()).isEqualTo("REQUEUED");
		verify(versions).currentDesired("resource-17");
	}

	@Test
	public void changedPayloadUnderSameIdempotencyKeyIsConflict() {
		TargetSnapshot target = target("QUEUED", null, 2);
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target);
		SmtAuthGovernanceAction existing = new SmtAuthGovernanceAction();
		existing.setActorUserId(71);
		existing.setIdempotencyKey("same-key");
		existing.setSubjectKey("TARGET:" + TARGET_ID);
		existing.setRequestFingerprint("different");
		existing.setResult("REQUEUED");
		when(mapper.selectActionByKey(71, "same-key", "TARGET:" + TARGET_ID)).thenReturn(existing);

		assertThatThrownBy(() -> service.retryKnownUnsent(parkActor, retry("same-key", "改变后的原因")))
				.isInstanceOf(AuthOperationGovernanceConflictException.class)
				.hasMessageContaining("幂等键");
	}

	@Test
	public void sameSuccessfulPayloadReplaysQueuedTargetWithoutSecondMutation() {
		TargetSnapshot target = target("QUEUED", null, 2);
		RetryCommand command = retry("same-key", "原始原因");
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target);
		SmtAuthGovernanceAction existing = new SmtAuthGovernanceAction();
		existing.setId(9007199254740997L);
		existing.setActorUserId(71);
		existing.setIdempotencyKey("same-key");
		existing.setSubjectKey("TARGET:" + TARGET_ID);
		existing.setRequestFingerprint(AuthOperationGovernanceService.fingerprint(command));
		existing.setResult("REQUEUED");
		existing.setResultCode("KNOWN_UNSENT_REQUEUED");
		when(mapper.selectActionByKey(71, "same-key", "TARGET:" + TARGET_ID)).thenReturn(existing);

		ActionResult result = service.retryKnownUnsent(parkActor, command);

		assertThat(result.getOutcome()).isEqualTo("ALREADY_APPLIED");
		assertThat(result.getActionId()).isEqualTo(9007199254740997L);
		verify(mapper, never()).insertAction(any());
		verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
	}

	@Test
	public void manualAbsentObservationOnlyRecordsPendingAuditAndNeverChangesPermissionState() {
		TargetSnapshot target = target("VERIFYING", NOW.plusMinutes(1), 1);
		stubTarget(target, attempt(1, "VERIFYING"));
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);
		ManualVerificationCommand command = ManualVerificationCommand.builder()
				.targetId(TARGET_ID).expectedOperationVersion(7L).expectedAttemptId(ATTEMPT_ID)
				.expectedState("VERIFYING").idempotencyKey("manual-absent")
				.observedConclusion("PERMISSION_ABSENT").reasonText("现场核对门禁列表")
				.evidenceType("OPERATOR_OBSERVATION").evidenceReference("case-20260906")
				.evidenceBody("{\"observation\":\"现场列表未发现权限\"}")
				.observedAt(NOW.minusMinutes(2)).build();

		ActionResult result = service.recordManualVerification(parkActor, command);

		assertThat(result.getOutcome()).isEqualTo("RECORDED_PENDING_VERIFICATION");
		assertThat(result.getBeforeState()).isEqualTo("VERIFYING");
		assertThat(result.getAfterState()).isEqualTo("VERIFYING");
		verify(mapper, never()).expireKnownUnsentAttempt(anyLong(), anyLong(), anyString(), any(LocalDateTime.class));
		verify(mapper, never()).requeueKnownUnsentTarget(anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
		ArgumentCaptor<SmtAuthGovernanceAction> saved = ArgumentCaptor.forClass(SmtAuthGovernanceAction.class);
		verify(mapper).insertAction(saved.capture());
		assertThat(saved.getValue().getEvidenceSha256()).hasSize(64);
		assertThat(saved.getValue().getResult()).isEqualTo("RECORDED_PENDING_VERIFICATION");
	}

	@Test
	public void manualEvidenceRejectsFutureTimeAndOversizedUtf8BeforeAudit() {
		TargetSnapshot target = target("VERIFYING", NOW.plusMinutes(1), 1);
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target);
		ManualVerificationCommand future = manual("future", "{}", NOW.plusSeconds(1));
		assertThatThrownBy(() -> service.recordManualVerification(parkActor, future))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("晚于数据库时间");

		ManualVerificationCommand oversized = manual("oversized",
				String.join("", Collections.nCopies(6000, "中")), NOW.minusMinutes(1));
		assertThatThrownBy(() -> service.recordManualVerification(parkActor, oversized))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("16KiB");
		ManualVerificationCommand sensitive = manual("sensitive", "{\"accessToken\":\"secret\"}", NOW.minusMinutes(1));
		assertThatThrownBy(() -> service.recordManualVerification(parkActor, sensitive))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("证据正文");
		verify(mapper, never()).insertAction(any());
	}

	@Test
	public void manualEvidenceRejectsCredentialUrlBeforeAnyRejectedAudit() {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target("VERIFYING", NOW.plusMinutes(1), 1));
		ManualVerificationCommand command = ManualVerificationCommand.builder()
				.targetId(TARGET_ID).expectedOperationVersion(7L).expectedAttemptId(ATTEMPT_ID)
				.expectedState("VERIFYING").idempotencyKey("credential-reference")
				.observedConclusion("INCONCLUSIVE").reasonText("人工观察仍需继续核验")
				.evidenceType("OPERATOR_OBSERVATION")
				.evidenceReference("https://reviewer:secret@example.invalid/case?access_token=value")
				.evidenceBody("{\"observation\":\"现场列表待复核\"}").observedAt(NOW.minusMinutes(1)).build();

		assertThatThrownBy(() -> service.recordManualVerification(parkActor, command))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("证据引用");
		verify(mapper, never()).insertAction(any());
	}

	@Test
	public void manualEvidenceRejectsOpaqueImagePayloadWithoutKeywordBeforeAudit() {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target("VERIFYING", NOW.plusMinutes(1), 1));
		ManualVerificationCommand command = ManualVerificationCommand.builder()
				.targetId(TARGET_ID).expectedOperationVersion(7L).expectedAttemptId(ATTEMPT_ID)
				.expectedState("VERIFYING").idempotencyKey("binary-payload")
				.observedConclusion("INCONCLUSIVE").reasonText("人工观察仍需继续核验")
				.evidenceType("OPERATOR_OBSERVATION").evidenceReference("CASE-20260906-001")
				.evidenceBody("{\"observation\":\"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/aF8AAAAASUVORK5CYII=\"}")
				.observedAt(NOW.minusMinutes(1)).build();

		assertThatThrownBy(() -> service.recordManualVerification(parkActor, command))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("二进制");
		verify(mapper, never()).insertAction(any());
	}

	@Test
	public void manualEvidenceRejectsUnpaddedWhitespaceImagePayloadBeforeAudit() {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target("VERIFYING", NOW.plusMinutes(1), 1));
		String pngWithoutPadding = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/aF8AAAAASUVORK5CYII";
		String withWhitespace = pngWithoutPadding.substring(0, 24) + "\\n  " + pngWithoutPadding.substring(24);
		ManualVerificationCommand command = manual("binary-unpadded",
				"{\"observation\":\"" + withWhitespace + "\"}", NOW.minusMinutes(1));

		assertThatThrownBy(() -> service.recordManualVerification(parkActor, command))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("二进制");
		verify(mapper, never()).insertAction(any());
	}

	@Test
	public void manualEvidenceRejectsImageDataUriBeforeAudit() {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target("VERIFYING", NOW.plusMinutes(1), 1));
		ManualVerificationCommand command = manual("binary-data-uri",
				"{\"observation\":\"现场附件 data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/aF8AAAAASUVORK5CYII=\"}",
				NOW.minusMinutes(1));

		assertThatThrownBy(() -> service.recordManualVerification(parkActor, command))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("二进制");
		verify(mapper, never()).insertAction(any());
	}

	@Test
	public void manualEvidenceRejectsExplicitCredentialMarkerBeforeRejectedAudit() {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target("VERIFYING", NOW.plusMinutes(1), 1));
		ManualVerificationCommand command = manual("credential-observation",
				"{\"observation\":\"access_token=synthetic-test-value\"}", NOW.minusMinutes(1));

		assertThatThrownBy(() -> service.recordManualVerification(parkActor, command))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("凭据标记");
		verify(mapper, never()).insertAction(any());
	}

	@Test
	public void trimmedManualInputsUseTheSameCanonicalValuesInAuditAndFingerprint() {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target("VERIFYING", NOW.plusMinutes(1), 1));
		when(mapper.insertAction(any(SmtAuthGovernanceAction.class))).thenReturn(1);
		ManualVerificationCommand command = ManualVerificationCommand.builder()
				.targetId(TARGET_ID).expectedOperationVersion(7L).expectedAttemptId(ATTEMPT_ID)
				.expectedState(" VERIFYING ").idempotencyKey(" canonical-key ")
				.observedConclusion(" INCONCLUSIVE ").reasonText("r" + String.join("", Collections.nCopies(512, " ")))
				.evidenceType(" OPERATOR_OBSERVATION ").evidenceReference(" CASE-001 ")
				.evidenceBody("{ \"observation\" : \"现场列表待复核\" }").observedAt(NOW.minusMinutes(1)).build();

		service.recordManualVerification(parkActor, command);

		ArgumentCaptor<SmtAuthGovernanceAction> saved = ArgumentCaptor.forClass(SmtAuthGovernanceAction.class);
		verify(mapper).insertAction(saved.capture());
		assertThat(saved.getValue().getIdempotencyKey()).isEqualTo("canonical-key");
		assertThat(saved.getValue().getReasonText()).isEqualTo("r");
		assertThat(saved.getValue().getExpectedState()).isEqualTo("VERIFYING");
		assertThat(saved.getValue().getEvidenceType()).isEqualTo("OPERATOR_OBSERVATION");
		assertThat(saved.getValue().getEvidenceReference()).isEqualTo("CASE-001");
		assertThat(saved.getValue().getEvidenceBody()).isEqualTo("{\"observation\":\"现场列表待复核\"}");
		ManualVerificationCommand canonical = ManualVerificationCommand.builder()
				.targetId(TARGET_ID).expectedOperationVersion(7L).expectedAttemptId(ATTEMPT_ID)
				.expectedState("VERIFYING").idempotencyKey("canonical-key").observedConclusion("INCONCLUSIVE")
				.reasonText("r").evidenceType("OPERATOR_OBSERVATION").evidenceReference("CASE-001")
				.evidenceBody("{\"observation\":\"现场列表待复核\"}").observedAt(NOW.minusMinutes(1)).build();
		assertThat(saved.getValue().getRequestFingerprint())
				.isEqualTo(AuthOperationGovernanceService.fingerprint(canonical));
	}

	@Test
	public void parkReviewCannotUseEmptyScopeAndGlobalPermissionDoesNotGrantParkReview() {
		Actor globalOnly = Actor.builder().userId(72).username("全局审计")
				.parkIds(Collections.emptyList())
				.permissions(Collections.singletonList("platform_auth_operation_global_review_view"))
				.build();

		assertThatThrownBy(() -> service.getParkReviews(globalOnly, 17, 1, 20))
				.isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
		Page<?> page = new Page<>(1, 20);
		when(mapper.selectGlobalReviews(any(IPage.class))).thenReturn((IPage) page);
		assertThat(service.getGlobalReviews(globalOnly, 1, 20)).isSameAs(page);
		verify(mapper, never()).selectParkReviews(any(IPage.class), anyInt());
	}

	private void stubTarget(TargetSnapshot target, SmtAuthOperationAttempt attempt) {
		when(mapper.selectTargetScope(TARGET_ID)).thenReturn(target);
		when(mapper.selectActionByKey(eq(71), anyString(), eq("TARGET:" + TARGET_ID))).thenReturn(null);
		when(mapper.hasCurrentSourceBinding(TARGET_ID, "resource-17", 7L)).thenReturn(1);
		when(versions.currentDesired("resource-17")).thenReturn(decision(7L, TARGET_ID, ATTEMPT_ID));
		when(mapper.lockTarget(TARGET_ID)).thenReturn(target);
		when(mapper.lockCurrentAttempt(TARGET_ID, ATTEMPT_ID)).thenReturn(attempt);
	}

	private TargetSnapshot target(String state, LocalDateTime leaseUntil, int attemptNo) {
		return TargetSnapshot.builder().targetId(TARGET_ID).batchId(81L).parkId(17)
				.resourceCoordId("resource-17").resourceId("physical-resource-17").operationVersion(7L).state(state)
				.leaseToken("lease-2").leaseUntil(leaseUntil).latestAttemptNo(attemptNo)
				.dispatchedAt(null).build();
	}

	private SmtAuthOperationAttempt attempt(int attemptNo) {
		return attempt(attemptNo, "CLAIMED");
	}

	private SmtAuthOperationAttempt attempt(int attemptNo, String status) {
		SmtAuthOperationAttempt attempt = new SmtAuthOperationAttempt();
		attempt.setId(ATTEMPT_ID);
		attempt.setTargetId(TARGET_ID);
		attempt.setAttemptNo(attemptNo);
		attempt.setStatus(status);
		attempt.setLeaseToken("lease-2");
		attempt.setLeaseUntil(NOW.minusSeconds(1));
		return attempt;
	}

	private ResourceDecision decision(long generation, Long blockerTarget, Long blockerAttempt) {
		return ResourceDecision.builder().resourceId("resource-17").generation(generation)
				.appliedGeneration(6).blockingTargetId(blockerTarget).blockingAttemptId(blockerAttempt).build();
	}

	private RetryCommand retry(String key, String reason) {
		return RetryCommand.builder().targetId(TARGET_ID).expectedOperationVersion(7L)
				.expectedAttemptId(ATTEMPT_ID).expectedAttemptNo(2).expectedState("EXECUTING")
				.idempotencyKey(key).reasonText(reason).build();
	}

	private ManualVerificationCommand manual(String key, String body, LocalDateTime observedAt) {
		return ManualVerificationCommand.builder().targetId(TARGET_ID).expectedOperationVersion(7L)
				.expectedAttemptId(ATTEMPT_ID).expectedState("VERIFYING").idempotencyKey(key)
				.observedConclusion("INCONCLUSIVE").reasonText("人工观察仍需继续核验")
				.evidenceType("OPERATOR_OBSERVATION").evidenceReference("case-" + key)
				.evidenceBody(body).observedAt(observedAt).build();
	}
}
