package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authoperation.AuthOperationAppendCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationBatchResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationExpansionResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationProgress;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmissionCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmissionResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmitCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationStateCount;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationTargetCommand;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import com.tce.smart.platform.core.entity.SmtAuthOperationBatch;
import com.tce.smart.platform.core.entity.SmtAuthOperationTarget;
import com.tce.smart.platform.core.entity.SmtAuthResultEvent;
import com.tce.smart.platform.core.entity.SmtAuthDeleteRequest;
import com.tce.smart.platform.core.mapper.SmtAuthDeleteRequestMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationAttemptMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationBatchMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationTargetMapper;
import com.tce.smart.platform.core.mapper.SmtAuthResultEventMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * 权限批次持久服务的行为契约测试。
 */
public class AuthOperationServiceTest {

    @Test
    public void deleteIntentCanAppendPhysicalAddToRetainAnotherSource() {
        SmtAuthOperationBatchMapper batches = Mockito.mock(SmtAuthOperationBatchMapper.class);
        SmtAuthOperationTargetMapper targets = Mockito.mock(SmtAuthOperationTargetMapper.class);
        Mockito.when(batches.selectById(101L)).thenReturn(batch(101L, "PREPARING", "fp-1"));
        Mockito.when(targets.insert(Mockito.any(SmtAuthOperationTarget.class))).thenReturn(1);
        Mockito.when(batches.advanceExpansion(Mockito.eq(101L), Mockito.eq(0L), Mockito.eq(1),
            Mockito.eq(1L), Mockito.any(LocalDateTime.class))).thenReturn(1);
        AuthOperationExpansionResult result = service(batches, targets).appendTargets(AuthOperationAppendCommand.builder()
            .batchId(101L).previousCursor(0L).nextCursor(1L)
            .target(targetCommand("retained-window", "device-1").toBuilder().action("ADD").build()).build());
        Assert.assertEquals(Integer.valueOf(1), result.getAppendedCount());
    }

	@Test
	public void submitWithSameBusinessKeyAndFingerprintIsIdempotent() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationBatch existing = batch(101L, "PREPARING", "fp-1");
		Mockito.when(batchMapper.selectByParkAndIdempotency(7, "operation-1")).thenReturn(existing);
		AuthOperationService service = service(batchMapper);

		AuthOperationBatchResult result = service.submit(submit("operation-1", "fp-1"));

		Assert.assertEquals(Long.valueOf(101L), result.getBatchId());
		Assert.assertTrue(result.isIdempotent());
		Mockito.verify(batchMapper, Mockito.never()).insert(Mockito.any(SmtAuthOperationBatch.class));
	}

	@Test
	public void submitWithSameBusinessKeyAndDifferentFingerprintIsRejected() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		Mockito.when(batchMapper.selectByParkAndIdempotency(7, "operation-1"))
				.thenReturn(batch(101L, "PREPARING", "fp-1"));
		AuthOperationService service = service(batchMapper);

		try {
			service.submit(submit("operation-1", "fp-2"));
			Assert.fail("同幂等键的不同意图必须拒绝");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("指纹"));
		}
	}

	@Test
	public void submitWithSameFingerprintAndChangedActionIsRejected() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		Mockito.when(batchMapper.selectByParkAndIdempotency(7, "operation-1"))
				.thenReturn(batch(101L, "PREPARING", "fp-1"));
		AuthOperationService service = service(batchMapper);

		try {
			service.submit(submit("operation-1", "fp-1").toBuilder().action("ADD").build());
			Assert.fail("同 fingerprint 改变动作必须拒绝");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("完整业务意图"));
		}
	}

	@Test
	public void appendRetryWithAlreadyPersistedShardDoesNotRecount() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationBatch batch = batch(101L, "PREPARING", "fp-1");
		batch.setExpansionCursor(5L);
		batch.setExpandedCount(1);
		SmtAuthOperationTarget persisted = target(201L, 101L, "target-1", "device-1");
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch);
		Mockito.when(targetMapper.selectByBatchIdAndTargetKeys(Mockito.eq(101L), Mockito.anyList()))
				.thenReturn(Collections.singletonList(persisted));
		AuthOperationService service = service(batchMapper, targetMapper);

		AuthOperationExpansionResult result = service.appendTargets(AuthOperationAppendCommand.builder()
				.batchId(101L)
				.previousCursor(0L)
				.nextCursor(5L)
				.target(targetCommand("target-1", "device-1"))
				.build());

		Assert.assertEquals(Integer.valueOf(0), result.getAppendedCount());
		Assert.assertEquals(Integer.valueOf(1), result.getExpandedCount());
		Mockito.verify(targetMapper, Mockito.never()).insert(Mockito.any(SmtAuthOperationTarget.class));
		Mockito.verify(batchMapper, Mockito.never()).advanceExpansion(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.anyInt(), Mockito.anyLong(), Mockito.any(LocalDateTime.class));
	}

	@Test
	public void appendRejectsSameTargetKeyWithChangedDevice() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch(101L, "PREPARING", "fp-1"));
		Mockito.when(targetMapper.selectByBatchIdAndTargetKeys(Mockito.eq(101L), Mockito.anyList()))
				.thenReturn(Collections.singletonList(target(201L, 101L, "target-1", "device-old")));
		AuthOperationService service = service(batchMapper, targetMapper);

		try {
			service.appendTargets(AuthOperationAppendCommand.builder()
					.batchId(101L).previousCursor(0L).nextCursor(1L)
					.target(targetCommand("target-1", "device-new"))
					.build());
			Assert.fail("已持久目标内容变化必须拒绝");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("target-1"));
		}
	}

	@Test
	public void finishZeroTargetsStaysInVerification() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationBatch batch = batch(101L, "PREPARING", "fp-1");
		batch.setExpectedCount(0);
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch);
		Mockito.when(targetMapper.countByBatchId(101L)).thenReturn(0);
		Mockito.when(batchMapper.finishExpansion(Mockito.eq(101L), Mockito.eq(0), Mockito.eq("VERIFYING"),
				Mockito.any(LocalDateTime.class)))
				.thenReturn(1);
		AuthOperationService service = service(batchMapper, targetMapper);

		AuthOperationBatchResult result = service.finishExpansion(101L, 0);

		Assert.assertEquals("VERIFYING", result.getStatus());
		Mockito.verify(batchMapper).finishExpansion(Mockito.eq(101L), Mockito.eq(0), Mockito.eq("VERIFYING"),
				Mockito.any(LocalDateTime.class));
	}

	@Test
	public void finishExpansionCannotLowerFrozenExpectedCount() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationBatch batch = batch(101L, "PREPARING", "fp-1");
		batch.setExpectedCount(100);
		batch.setExpandedCount(1);
		batch.setExpansionCursor(1L);
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch);
		Mockito.when(targetMapper.countByBatchId(101L)).thenReturn(1);
		Mockito.when(targetMapper.queueByBatchId(Mockito.eq(101L), Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(batchMapper.finishExpansion(Mockito.eq(101L), Mockito.eq(1), Mockito.eq("QUEUED"),
				Mockito.any(LocalDateTime.class))).thenReturn(1);
		AuthOperationService service = service(batchMapper, targetMapper);

		try {
			service.finishExpansion(101L, 1);
			Assert.fail("调用方不能降低受理时冻结的预期数量");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("冻结"));
		}
		Mockito.verify(targetMapper, Mockito.never()).queueByBatchId(Mockito.anyLong(), Mockito.any(LocalDateTime.class));
		Mockito.verify(batchMapper, Mockito.never()).finishExpansion(Mockito.anyLong(), Mockito.anyInt(),
				Mockito.anyString(), Mockito.any(LocalDateTime.class));
	}

	@Test
	public void iscSubmissionUsesOnlyTheRealExternalBatchIdentifier() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("EXECUTING");
		target.setLeaseToken("lease-1");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-1");
		attempt.setAttemptNo(1);
		attempt.setAccessType("ISC");
		attempt.setTaskId("isc-task-1");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(attemptMapper.markSubmitted(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.isNull(String.class), Mockito.any(LocalDateTime.class)))
				.thenReturn(1);
		Mockito.when(targetMapper.markWaitingConfirmByLease(Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.any(LocalDateTime.class))).thenReturn(1);
		AuthOperationService service = service(targetMapper, attemptMapper);

		AuthOperationSubmissionCommand command = AuthOperationSubmissionCommand.builder()
				.targetId(201L).attemptId(301L).attemptNo(1).leaseToken("lease-1").accessType("ISC")
				.taskId("isc-task-1").externalBatchId("isc-task-1").build();
		AuthOperationSubmissionResult result = service.markSubmitted(command);

		Assert.assertTrue(result.isPersisted());
		Assert.assertEquals("WAITING_CONFIRM", result.getStatus());
		Mockito.verify(attemptMapper).markSubmitted(Mockito.eq(301L), Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.eq("isc-task-1"), Mockito.eq("isc-task-1"), Mockito.isNull(String.class),
				Mockito.any(LocalDateTime.class));
	}

	@Test
	public void directSubmissionUsesOnlyTheRealExternalCommandIdentifier() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setAccessType("DIRECT");
		target.setState("EXECUTING");
		target.setLeaseToken("lease-1");
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-1");
		attempt.setAttemptNo(1);
		attempt.setAccessType("DIRECT");
		attempt.setTaskId("legacy-task-1");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(attemptMapper.markSubmitted(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyString(), Mockito.isNull(String.class), Mockito.anyString(), Mockito.any(LocalDateTime.class)))
				.thenReturn(1);
		Mockito.when(targetMapper.markWaitingConfirmByLease(Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.any(LocalDateTime.class))).thenReturn(1);
		AuthOperationService service = service(targetMapper, attemptMapper);

		AuthOperationSubmissionResult result = service.markSubmitted(AuthOperationSubmissionCommand.builder()
				.targetId(201L).attemptId(301L).attemptNo(1).leaseToken("lease-1").accessType("DIRECT")
				.taskId("legacy-task-1").externalCommandId("serial-1").build());

		Assert.assertTrue(result.isPersisted());
		Assert.assertEquals("WAITING_CONFIRM", result.getStatus());
		Mockito.verify(attemptMapper).markSubmitted(Mockito.eq(301L), Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.eq("legacy-task-1"), Mockito.isNull(String.class), Mockito.eq("serial-1"),
				Mockito.any(LocalDateTime.class));
	}

	@Test
	public void claimCreatesAttemptOnlyAfterLeaseCompareAndSwapWins() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthOperationTarget candidate = target(201L, 101L, "target-1", "device-1");
		candidate.setParkId(7);
		candidate.setOperationQueue("DELETE");
		candidate.setState("QUEUED");
		candidate.setAccessType("ISC");
		candidate.setOperationVersion(3L);
		Mockito.when(targetMapper.selectClaimCandidates(Mockito.eq(7), Mockito.eq("DELETE"),
				Mockito.any(LocalDateTime.class), Mockito.eq(1))).thenReturn(Collections.singletonList(candidate));
		Mockito.when(targetMapper.claimByLease(Mockito.eq(201L), Mockito.eq("QUEUED"),
				Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
				.thenReturn(1);
		Mockito.when(attemptMapper.selectMaxAttemptNo(201L)).thenReturn(0);
		Mockito.when(attemptMapper.insert(Mockito.any(SmtAuthOperationAttempt.class))).thenReturn(1);
		AuthOperationService service = service(targetMapper, attemptMapper);

		AuthOperationClaimedTarget claimed = service.claim(AuthOperationClaimCommand.builder()
				.parkId(7).operationQueue("DELETE").maxCount(1).leaseSeconds(30L).build()).get(0);

		Assert.assertEquals(Long.valueOf(201L), claimed.getTargetId());
		Assert.assertEquals(Integer.valueOf(1), claimed.getAttemptNo());
		Assert.assertNotNull(claimed.getLeaseToken());
		Mockito.verify(attemptMapper).insert(Mockito.any(SmtAuthOperationAttempt.class));
	}

	@Test
	public void receiptWithoutExternalIdentifiersCannotConfirm() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthResultEventMapper eventMapper = Mockito.mock(SmtAuthResultEventMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("EXECUTING");
		target.setLeaseToken("lease-1");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-1");
		attempt.setAttemptNo(1);
		attempt.setAccessType("ISC");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(eventMapper.selectByAttemptAndEventKey(301L, "event-1")).thenReturn(null);
		Mockito.when(eventMapper.insert(Mockito.any(SmtAuthResultEvent.class))).thenReturn(1);
		Mockito.when(attemptMapper.markReceipt(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(targetMapper.updateStateByLease(Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.eq("VERIFYING"), Mockito.any(LocalDateTime.class), Mockito.any())).thenReturn(1);
		AuthOperationService service = service(targetMapper, attemptMapper, eventMapper);

		AuthOperationReceiptResult result = service.recordReceipt(receipt(null, null, 3L, "lease-1"));

		Assert.assertFalse(result.isConfirmed());
		Assert.assertEquals("VERIFYING", result.getState());
		Mockito.verify(targetMapper).updateStateByLease(Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.eq("VERIFYING"), Mockito.any(LocalDateTime.class), Mockito.any());
	}

	@Test
	public void receiptWithVersionMismatchCannotConfirmEvenWithTrustedSuccess() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthResultEventMapper eventMapper = Mockito.mock(SmtAuthResultEventMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("EXECUTING");
		target.setLeaseToken("lease-1");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-1");
		attempt.setAttemptNo(1);
		attempt.setAccessType("ISC");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(eventMapper.selectByAttemptAndEventKey(301L, "event-1")).thenReturn(null);
		Mockito.when(eventMapper.insert(Mockito.any(SmtAuthResultEvent.class))).thenReturn(1);
		Mockito.when(attemptMapper.markReceipt(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(targetMapper.updateStateByLease(Mockito.eq(201L), Mockito.eq("lease-1"),
				Mockito.eq("VERIFYING"), Mockito.any(LocalDateTime.class), Mockito.any())).thenReturn(1);
		AuthOperationService service = service(targetMapper, attemptMapper, eventMapper);

		AuthOperationReceiptResult result = service.recordReceipt(receipt("external-batch", "external-command", 4L,
				"lease-1").toBuilder().trustedDeviceEvidence(true).build());

		Assert.assertFalse(result.isConfirmed());
		Assert.assertEquals("VERIFYING", result.getState());
	}

	@Test
	public void staleAttemptReceiptCannotOverwriteNewLease() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthResultEventMapper eventMapper = Mockito.mock(SmtAuthResultEventMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("EXECUTING");
		target.setLeaseToken("lease-new");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-old");
		attempt.setAttemptNo(1);
		attempt.setAccessType("ISC");
		attempt.setExternalBatchId("external-batch");
		attempt.setExternalCommandId("external-command");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(eventMapper.selectByAttemptAndEventKey(301L, "event-1")).thenReturn(null);
		Mockito.when(eventMapper.insert(Mockito.any(SmtAuthResultEvent.class))).thenReturn(1);
		Mockito.when(attemptMapper.markReceipt(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class))).thenReturn(1);
		Mockito.when(targetMapper.updateStateByLease(Mockito.eq(201L), Mockito.eq("lease-old"),
				Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any())).thenReturn(0);
		AuthOperationService service = service(targetMapper, attemptMapper, eventMapper);

		AuthOperationReceiptResult result = service.recordReceipt(receipt("external-batch", "external-command", 3L,
				"lease-old").toBuilder().trustedDeviceEvidence(true).build());

		Assert.assertFalse(result.isConfirmed());
		Mockito.verify(targetMapper).updateStateByLease(Mockito.eq(201L), Mockito.eq("lease-old"),
				Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any());
	}

	@Test
	public void terminalReceiptIsRetainedWithoutChangingCurrentState() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthResultEventMapper eventMapper = Mockito.mock(SmtAuthResultEventMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("CONFIRMED");
		target.setLeaseToken("lease-1");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-1");
		attempt.setAttemptNo(1);
		attempt.setAccessType("ISC");
		attempt.setExternalBatchId("external-batch");
		attempt.setExternalCommandId("external-command");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(eventMapper.selectByAttemptAndEventKey(301L, "event-1")).thenReturn(null);
		Mockito.when(eventMapper.insert(Mockito.any(SmtAuthResultEvent.class))).thenReturn(1);
		Mockito.when(attemptMapper.markReceipt(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class))).thenReturn(0);
		AuthOperationService service = service(targetMapper, attemptMapper, eventMapper);

		AuthOperationReceiptResult result = service.recordReceipt(receipt("external-batch", "external-command", 3L,
				"lease-1"));

		Assert.assertEquals("CONFIRMED", result.getState());
		Assert.assertFalse(result.isDuplicate());
		Mockito.verify(eventMapper).insert(Mockito.any(SmtAuthResultEvent.class));
		Mockito.verify(attemptMapper, Mockito.never()).markReceipt(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class));
		Mockito.verify(targetMapper, Mockito.never()).updateStateByLease(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any());
	}

	@Test
	public void terminalReceiptDoesNotUpdateOlderVerifyingAttempt() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthResultEventMapper eventMapper = Mockito.mock(SmtAuthResultEventMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("CONFIRMED");
		target.setLeaseToken("lease-new");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt olderAttempt = attempt(301L, 201L, "lease-old");
		olderAttempt.setAttemptNo(1);
		olderAttempt.setAccessType("ISC");
		olderAttempt.setStatus("VERIFYING");
		olderAttempt.setExternalBatchId("external-batch");
		olderAttempt.setExternalCommandId("external-command");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(olderAttempt);
		Mockito.when(eventMapper.selectByAttemptAndEventKey(301L, "event-old-late")).thenReturn(null);
		Mockito.when(eventMapper.insert(Mockito.any(SmtAuthResultEvent.class))).thenReturn(1);
		AuthOperationService service = service(targetMapper, attemptMapper, eventMapper);

		AuthOperationReceiptResult result = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(201L).attemptId(301L).attemptNo(1).leaseToken("lease-old").accessType("ISC")
				.externalBatchId("external-batch").externalCommandId("external-command")
				.operationVersion(3L).eventNamespace("ISC:batch").eventKey("event-old-late")
				.evidenceType("DEVICE_ACK").resultStatus("FAILED").evidenceBody("{\"ok\":false}")
				.trustedDeviceEvidence(false).build());

		Assert.assertEquals("CONFIRMED", result.getState());
		Assert.assertTrue(result.isConfirmed());
		Mockito.verify(eventMapper).insert(Mockito.any(SmtAuthResultEvent.class));
		Mockito.verify(attemptMapper, Mockito.never()).markReceipt(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class));
		Mockito.verify(targetMapper, Mockito.never()).updateStateByLease(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any());
	}

	@Test
	public void confirmedAttemptIgnoresWeakStateDowngradeButKeepsEvent() {
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationAttemptMapper attemptMapper = Mockito.mock(SmtAuthOperationAttemptMapper.class);
		SmtAuthResultEventMapper eventMapper = Mockito.mock(SmtAuthResultEventMapper.class);
		SmtAuthOperationTarget target = target(201L, 101L, "target-1", "device-1");
		target.setState("CONFIRMED");
		target.setLeaseToken("lease-1");
		target.setOperationVersion(3L);
		SmtAuthOperationAttempt attempt = attempt(301L, 201L, "lease-1");
		attempt.setAttemptNo(1);
		attempt.setAccessType("ISC");
		attempt.setStatus("CONFIRMED");
		attempt.setExternalBatchId("external-batch");
		attempt.setExternalCommandId("external-command");
		Mockito.when(targetMapper.selectById(201L)).thenReturn(target);
		Mockito.when(attemptMapper.selectByIdAndTarget(301L, 201L)).thenReturn(attempt);
		Mockito.when(eventMapper.selectByAttemptAndEventKey(301L, "event-1")).thenReturn(null);
		Mockito.when(eventMapper.insert(Mockito.any(SmtAuthResultEvent.class))).thenReturn(1);
		Mockito.when(attemptMapper.markReceipt(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class))).thenReturn(0);
		AuthOperationService service = service(targetMapper, attemptMapper, eventMapper);

		AuthOperationReceiptResult result = service.recordReceipt(receipt(null, null, 3L, "lease-1")
				.toBuilder().trustedDeviceEvidence(false).build());

		Assert.assertEquals("CONFIRMED", result.getState());
		Assert.assertTrue(result.isConfirmed());
		Mockito.verify(eventMapper).insert(Mockito.any(SmtAuthResultEvent.class));
		Mockito.verify(attemptMapper, Mockito.never()).markReceipt(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.any(LocalDateTime.class));
		Mockito.verify(targetMapper, Mockito.never()).updateStateByLease(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(LocalDateTime.class), Mockito.any());
	}

	@Test
	public void progressUsesRecomputedTargetCounts() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationBatch batch = batch(101L, "QUEUED", "fp-1");
		batch.setExpectedCount(3);
		batch.setExpandedCount(3);
		batch.setExpansionCursor(9L);
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch);
		Mockito.when(targetMapper.countByBatchIdGroupByState(101L)).thenReturn(Arrays.asList(
				stateCount("QUEUED", 1), stateCount("EXECUTING", 1), stateCount("VERIFYING", 1)));
		AuthOperationService service = service(batchMapper, targetMapper);

		AuthOperationProgress progress = service.getProgress(101L);

		Assert.assertEquals(Integer.valueOf(3), progress.getTotalTargetCount());
		Assert.assertEquals(Integer.valueOf(1), progress.getQueuedCount());
		Assert.assertEquals(Integer.valueOf(1), progress.getExecutingCount());
		Assert.assertEquals(Integer.valueOf(1), progress.getVerifyingCount());
		Assert.assertEquals(Integer.valueOf(3), progress.getUnfinishedCount());
	}

	@Test
	public void progressIncludesUnexpandedAndKeepsConfirmedAndFailedUnfinished() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationBatch batch = batch(101L, "PREPARING", "fp-1");
		batch.setExpectedCount(100);
		batch.setExpandedCount(3);
		batch.setExpansionCursor(3L);
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch);
		Mockito.when(targetMapper.countByBatchIdGroupByState(101L)).thenReturn(Arrays.asList(
				stateCount("PREPARING", 1), stateCount("CONFIRMED", 1), stateCount("FAILED", 1)));
		AuthOperationService service = service(batchMapper, targetMapper);

		AuthOperationProgress progress = service.getProgress(101L);

		Assert.assertEquals(Integer.valueOf(100), progress.getTotalTargetCount());
		Assert.assertEquals(Integer.valueOf(98), progress.getPreparingCount());
		Assert.assertEquals(Integer.valueOf(100), progress.getUnfinishedCount());
	}

	@Test
	public void progressCountsEntireFrozenBatchBeforeAnyTargetIsExpanded() {
		SmtAuthOperationBatchMapper batchMapper = Mockito.mock(SmtAuthOperationBatchMapper.class);
		SmtAuthOperationTargetMapper targetMapper = Mockito.mock(SmtAuthOperationTargetMapper.class);
		SmtAuthOperationBatch batch = batch(101L, "PREPARING", "fp-1");
		batch.setExpectedCount(100);
		batch.setExpandedCount(0);
		batch.setExpansionCursor(0L);
		Mockito.when(batchMapper.selectById(101L)).thenReturn(batch);
		Mockito.when(targetMapper.countByBatchIdGroupByState(101L)).thenReturn(Collections.emptyList());
		AuthOperationService service = service(batchMapper, targetMapper);

		AuthOperationProgress progress = service.getProgress(101L);

		Assert.assertEquals(Integer.valueOf(100), progress.getTotalTargetCount());
		Assert.assertEquals(Integer.valueOf(100), progress.getPreparingCount());
		Assert.assertEquals(Integer.valueOf(100), progress.getUnfinishedCount());
	}

	private AuthOperationService service(SmtAuthOperationBatchMapper batchMapper) {
		return service(batchMapper, Mockito.mock(SmtAuthOperationTargetMapper.class));
	}

	private AuthOperationService service(SmtAuthOperationBatchMapper batchMapper,
			SmtAuthOperationTargetMapper targetMapper) {
		return service(batchMapper, targetMapper, Mockito.mock(SmtAuthOperationAttemptMapper.class),
				Mockito.mock(SmtAuthResultEventMapper.class));
	}

	private AuthOperationService service(SmtAuthOperationTargetMapper targetMapper,
			SmtAuthOperationAttemptMapper attemptMapper) {
		return service(Mockito.mock(SmtAuthOperationBatchMapper.class), targetMapper, attemptMapper,
				Mockito.mock(SmtAuthResultEventMapper.class));
	}

	private AuthOperationService service(SmtAuthOperationTargetMapper targetMapper,
			SmtAuthOperationAttemptMapper attemptMapper, SmtAuthResultEventMapper eventMapper) {
		return service(Mockito.mock(SmtAuthOperationBatchMapper.class), targetMapper, attemptMapper, eventMapper);
	}

	private AuthOperationService service(SmtAuthOperationBatchMapper batchMapper,
			SmtAuthOperationTargetMapper targetMapper, SmtAuthOperationAttemptMapper attemptMapper,
			SmtAuthResultEventMapper eventMapper) {
		SmtAuthDeleteRequestMapper requestMapper = Mockito.mock(SmtAuthDeleteRequestMapper.class);
		Mockito.when(requestMapper.selectByBatchIdAndIds(Mockito.eq(101L), Mockito.eq(7), Mockito.anyList()))
				.thenReturn(Collections.singletonList(request(401L)));
		return new AuthOperationService(batchMapper, requestMapper, targetMapper,
				attemptMapper, eventMapper);
	}

	private AuthOperationSubmitCommand submit(String idempotencyKey, String fingerprint) {
		return AuthOperationSubmitCommand.builder()
				.parkId(7).idempotencyKey(idempotencyKey).action("DELETE").sourceType("STAFF")
				.sourceId("staff-1").selectionSnapshot("{\"staffId\":\"staff-1\"}")
				.payloadFingerprint(fingerprint).expectedCount(1).build();
	}

	private AuthOperationReceiptCommand receipt(String externalBatchId, String externalCommandId,
			Long operationVersion, String leaseToken) {
		return AuthOperationReceiptCommand.builder()
				.targetId(201L).attemptId(301L).attemptNo(1).leaseToken(leaseToken).accessType("ISC")
				.externalBatchId(externalBatchId).externalCommandId(externalCommandId)
				.operationVersion(operationVersion).eventNamespace("ISC:batch")
				.eventKey("event-1").evidenceType("DEVICE_ACK").resultStatus("SUCCESS")
				.evidenceBody("{\"ok\":true}").build();
	}

	private SmtAuthOperationBatch batch(Long id, String status, String fingerprint) {
		SmtAuthOperationBatch batch = new SmtAuthOperationBatch();
		batch.setId(id);
		batch.setParkId(7);
		batch.setIdempotencyKey("operation-1");
		batch.setAction("DELETE");
		batch.setSourceType("STAFF");
		batch.setSourceId("staff-1");
		batch.setSelectionSnapshot("{\"staffId\":\"staff-1\"}");
		batch.setPayloadFingerprint(fingerprint);
		batch.setExpectedCount(1);
		batch.setExpandedCount(0);
		batch.setExpansionCursor(0L);
		batch.setStatus(status);
		return batch;
	}

	private AuthOperationTargetCommand targetCommand(String targetKey, String deviceId) {
		return AuthOperationTargetCommand.builder()
				.parkId(7).requestId(401L).targetKey(targetKey).subjectType("STAFF").subjectId("staff-1")
				.resourceType("PERMISSION").deviceId(deviceId).resourceId("resource-1")
				.accessType("ISC").operationQueue("DELETE").action("DELETE")
				.operationVersion(3L).build();
	}

	private SmtAuthOperationTarget target(Long id, Long batchId, String targetKey, String deviceId) {
		SmtAuthOperationTarget target = new SmtAuthOperationTarget();
		target.setId(id);
		target.setBatchId(batchId);
		target.setRequestId(401L);
		target.setParkId(7);
		target.setTargetKey(targetKey);
		target.setSubjectType("STAFF");
		target.setSubjectId("staff-1");
		target.setResourceType("PERMISSION");
		target.setDeviceId(deviceId);
		target.setResourceId("resource-1");
		target.setAccessType("ISC");
		target.setOperationQueue("DELETE");
		target.setAction("DELETE");
		target.setOperationVersion(3L);
		target.setState("PREPARING");
		return target;
	}

	private SmtAuthOperationAttempt attempt(Long id, Long targetId, String leaseToken) {
		SmtAuthOperationAttempt attempt = new SmtAuthOperationAttempt();
		attempt.setId(id);
		attempt.setTargetId(targetId);
		attempt.setLeaseToken(leaseToken);
		attempt.setStatus("CLAIMED");
		return attempt;
	}

	private SmtAuthDeleteRequest request(Long id) {
		SmtAuthDeleteRequest request = new SmtAuthDeleteRequest();
		request.setId(id);
		request.setBatchId(101L);
		request.setParkId(7);
		request.setSubjectType("STAFF");
		request.setSourceType("STAFF");
		request.setSourceRowId("staff-1");
		request.setSourceIdentityKey("staff-1");
		request.setIdentitySnapshot("{\"staffId\":\"staff-1\"}");
		request.setGeneration(0L);
		return request;
	}

	private AuthOperationStateCount stateCount(String state, int targetCount) {
		AuthOperationStateCount result = new AuthOperationStateCount();
		result.setState(state);
		result.setTargetCount(targetCount);
		return result;
	}
}
