package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationAppendCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationBatchResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationExpansionResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationProgress;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationRequestCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationStateCount;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmissionCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmissionResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmitCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationTargetCommand;
import com.tce.smart.platform.core.entity.SmtAuthDeleteRequest;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import com.tce.smart.platform.core.entity.SmtAuthOperationBatch;
import com.tce.smart.platform.core.entity.SmtAuthOperationTarget;
import com.tce.smart.platform.core.entity.SmtAuthResultEvent;
import com.tce.smart.platform.core.mapper.SmtAuthDeleteRequestMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationAttemptMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationBatchMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationTargetMapper;
import com.tce.smart.platform.core.mapper.SmtAuthResultEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 权限操作持久批次服务。
 *
 * <p>所有队列时间都从注入的 UTC 时钟取得。外部调用只能发生在本服务事务提交之后；
 * 本服务只负责持久登记、租约和证据状态，不替代具体接入适配器。</p>
 */
@Service
public class AuthOperationService {

	private static final int MAX_TARGETS_PER_SHARD = 1000;
	private static final int MAX_REQUESTS_PER_SHARD = 1000;
	private static final int MAX_CLAIM_COUNT = 1000;
	private static final long MAX_LEASE_SECONDS = 86400L;

	private static final String PREPARING = "PREPARING";
	private static final String QUEUED = "QUEUED";
	private static final String EXECUTING = "EXECUTING";
	private static final String WAITING_CONFIRM = "WAITING_CONFIRM";
	private static final String VERIFYING = "VERIFYING";
	private static final String CONFIRMED = "CONFIRMED";
	private static final String CONVERGED = "CONVERGED";
	private static final String FAILED = "FAILED";
	private static final String CLAIMED = "CLAIMED";
	private static final String SUBMITTING = "SUBMITTING";
	private static final String SUCCESS = "SUCCESS";
	private static final String ISC = "ISC";
	private static final String DIRECT = "DIRECT";
	private static final String ORPHAN_SOURCE_TYPE = "7";

	private final SmtAuthOperationBatchMapper batchMapper;
	private final SmtAuthDeleteRequestMapper deleteRequestMapper;
	private final SmtAuthOperationTargetMapper targetMapper;
	private final SmtAuthOperationAttemptMapper attemptMapper;
	private final SmtAuthResultEventMapper eventMapper;
	private final Clock clock;

	/**
	 * Spring 默认构造器使用 UTC 时钟。
	 */
	@Autowired
	public AuthOperationService(SmtAuthOperationBatchMapper batchMapper,
			SmtAuthDeleteRequestMapper deleteRequestMapper,
			SmtAuthOperationTargetMapper targetMapper,
			SmtAuthOperationAttemptMapper attemptMapper,
			SmtAuthResultEventMapper eventMapper) {
		this(batchMapper, deleteRequestMapper, targetMapper, attemptMapper, eventMapper, Clock.systemUTC());
	}

	/**
	 * 测试或批处理调用可注入固定 UTC 时钟，避免宿主时区影响队列时间。
	 */
	public AuthOperationService(SmtAuthOperationBatchMapper batchMapper,
			SmtAuthDeleteRequestMapper deleteRequestMapper,
			SmtAuthOperationTargetMapper targetMapper,
			SmtAuthOperationAttemptMapper attemptMapper,
			SmtAuthResultEventMapper eventMapper, Clock clock) {
		this.batchMapper = Objects.requireNonNull(batchMapper, "批次 Mapper 不能为空");
		this.deleteRequestMapper = Objects.requireNonNull(deleteRequestMapper, "请求 Mapper 不能为空");
		this.targetMapper = Objects.requireNonNull(targetMapper, "目标 Mapper 不能为空");
		this.attemptMapper = Objects.requireNonNull(attemptMapper, "尝试 Mapper 不能为空");
		this.eventMapper = Objects.requireNonNull(eventMapper, "事件 Mapper 不能为空");
		this.clock = Objects.requireNonNull(clock, "时钟不能为空");
	}

	/**
	 * 受理一个冻结选择依据的业务批次。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationBatchResult submit(AuthOperationSubmitCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("受理命令不能为空");
		}
		Integer parkId = positive(command.getParkId(), "园区ID");
		String idempotencyKey = text(command.getIdempotencyKey(), "幂等键");
		String action = text(command.getAction(), "动作");
		String sourceType = text(command.getSourceType(), "来源类型");
		String sourceId = optionalText(command.getSourceId());
		String selectionSnapshot = text(command.getSelectionSnapshot(), "稳定选择快照");
		String payloadFingerprint = text(command.getPayloadFingerprint(), "payload指纹");
		Integer expectedCount = nonNegative(command.getExpectedCount(), "预期数量");

		String canonicalFingerprint = canonicalFingerprint(action, sourceType, sourceId,
				selectionSnapshot, expectedCount);
		validateCanonicalFingerprint(payloadFingerprint, canonicalFingerprint);

		SmtAuthOperationBatch existing = batchMapper.selectByParkAndIdempotency(parkId, idempotencyKey);
		if (existing != null) {
			assertSameBatchIntent(existing, parkId, action, sourceType, sourceId,
					selectionSnapshot, payloadFingerprint, expectedCount);
			return toBatchResult(existing, true);
		}

		LocalDateTime acceptedAt = now();
		SmtAuthOperationBatch batch = new SmtAuthOperationBatch();
		batch.setId(IdWorker.getId());
		batch.setParkId(parkId);
		batch.setIdempotencyKey(idempotencyKey);
		batch.setAction(action);
		batch.setSourceType(sourceType);
		batch.setSourceId(sourceId);
		batch.setSelectionSnapshot(selectionSnapshot);
		batch.setPayloadFingerprint(payloadFingerprint);
		batch.setExpectedCount(expectedCount);
		batch.setExpandedCount(0);
		batch.setExpansionCursor(0L);
		batch.setStatus(PREPARING);
		batch.setAcceptedAt(acceptedAt);
		batch.setCreateTime(acceptedAt);
		batch.setUpdateTime(acceptedAt);
		try {
			if (batchMapper.insert(batch) != 1) {
				throw new IllegalStateException("权限批次保存失败");
			}
		} catch (DuplicateKeyException duplicate) {
			SmtAuthOperationBatch concurrent = batchMapper.selectByParkAndIdempotency(parkId, idempotencyKey);
			if (concurrent != null) {
				assertSameBatchIntent(concurrent, parkId, action, sourceType, sourceId,
						selectionSnapshot, payloadFingerprint, expectedCount);
				return toBatchResult(concurrent, true);
			}
			throw duplicate;
		}
		return toBatchResult(batch, false);
	}

	/**
	 * 以 request 种子和目标组成一个有界、可重试的展开分片。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationExpansionResult appendTargets(AuthOperationAppendCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("展开分片命令不能为空");
		}
		Long batchId = positive(command.getBatchId(), "批次ID");
		long previousCursor = nonNegativeLong(command.getPreviousCursor(), "前置游标");
		long nextCursor = nonNegativeLong(command.getNextCursor(), "下一游标");
		if (nextCursor < previousCursor) {
			throw new IllegalArgumentException("下一游标不能小于前置游标");
		}
		List<AuthOperationRequestCommand> requests = listOrEmpty(command.getRequests());
		List<AuthOperationTargetCommand> targets = listOrEmpty(command.getTargets());
		if (requests.size() > MAX_REQUESTS_PER_SHARD) {
			throw new IllegalArgumentException("单分片请求数量超过上限");
		}
		if (targets.size() > MAX_TARGETS_PER_SHARD) {
			throw new IllegalArgumentException("单分片目标数量超过上限");
		}

		SmtAuthOperationBatch batch = requiredBatch(batchId);
		if (!PREPARING.equals(batch.getStatus())) {
			throw new IllegalStateException("批次不在展开阶段: " + batchId);
		}
		long currentCursor = defaultLong(batch.getExpansionCursor());
		int currentExpandedCount = defaultInt(batch.getExpandedCount());
		if (previousCursor > currentCursor) {
			throw new IllegalArgumentException("前置游标超过持久游标");
		}
		if (currentCursor == previousCursor && nextCursor == previousCursor
				&& (!requests.isEmpty() || !targets.isEmpty())) {
			throw new IllegalArgumentException("有输入内容时游标必须前进");
		}
		if (currentCursor > previousCursor && nextCursor != currentCursor) {
			throw new IllegalArgumentException("重复分片的下一游标必须等于持久游标");
		}

		Map<Long, AuthOperationRequestCommand> requestCommands = indexRequestCommands(requests);
		Map<String, AuthOperationTargetCommand> targetCommands = indexTargetCommands(targets);
		Set<Long> requestIds = new LinkedHashSet<>(requestCommands.keySet());
		for (AuthOperationTargetCommand target : targets) {
			if (target.getRequestId() != null) {
				requestIds.add(target.getRequestId());
			}
		}
		Map<Long, SmtAuthDeleteRequest> existingRequests = loadRequests(batch, requestIds);

		for (AuthOperationRequestCommand request : requests) {
			validateRequestCommand(request, batch);
			Long requestId = positive(request.getId(), "预分配请求ID");
			SmtAuthDeleteRequest existing = existingRequests.get(requestId);
			if (existing == null) {
				if (currentCursor > previousCursor) {
					throw new IllegalArgumentException("重复分片缺少已持久请求: " + requestId);
				}
				SmtAuthDeleteRequest inserted = insertRequest(request, batch);
				existingRequests.put(requestId, inserted);
			} else {
				assertSameRequest(existing, request, batch);
			}
		}

		for (AuthOperationTargetCommand target : targets) {
			validateTargetCommand(target, batch, existingRequests);
		}

		Map<String, SmtAuthOperationTarget> existingTargets = loadTargets(batchId, targetCommands.keySet());
		if (currentCursor > previousCursor) {
			for (AuthOperationTargetCommand target : targets) {
				SmtAuthOperationTarget existing = existingTargets.get(target.getTargetKey());
				if (existing == null) {
					throw new IllegalArgumentException("重复分片缺少已持久目标: " + target.getTargetKey());
				}
				assertSameTarget(existing, target, batch);
			}
			return expansionResult(batch, previousCursor, currentCursor, 0, currentExpandedCount);
		}

		int appendedCount = 0;
		for (AuthOperationTargetCommand target : targets) {
			SmtAuthOperationTarget existing = existingTargets.get(target.getTargetKey());
			if (existing != null) {
				assertSameTarget(existing, target, batch);
				continue;
			}
			SmtAuthOperationTarget inserted = insertTarget(target, batch);
			existingTargets.put(target.getTargetKey(), inserted);
			appendedCount++;
		}
		LocalDateTime updatedAt = now();
		if (batchMapper.advanceExpansion(batchId, previousCursor, appendedCount,
				nextCursor, updatedAt) != 1) {
			throw new IllegalStateException("展开游标并发冲突，批次未推进: " + batchId);
		}
		return expansionResult(batch, previousCursor, nextCursor,
				appendedCount, currentExpandedCount + appendedCount);
	}

	/**
	 * 校验展开数量并将目标从 PREPARING 放入可领取队列。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationBatchResult finishExpansion(Long batchId, Integer expectedCount) {
		Long id = positive(batchId, "批次ID");
		int expected = nonNegative(expectedCount, "预期数量");
		SmtAuthOperationBatch batch = requiredBatch(id);
		int actual = safeCount(targetMapper.countByBatchId(id));
		int expanded = defaultInt(batch.getExpandedCount());
		int frozenExpected = defaultInt(batch.getExpectedCount());
		if (expected != frozenExpected || actual != frozenExpected || expanded != frozenExpected) {
			throw new IllegalArgumentException("展开数量与受理时冻结预期不一致: frozenExpected=" + frozenExpected
					+ ", suppliedExpected=" + expected + ", expanded=" + expanded + ", actual=" + actual);
		}
		String status = expected == 0 ? VERIFYING : QUEUED;
		if (!PREPARING.equals(batch.getStatus())) {
			if (status.equals(batch.getStatus())) {
				return toBatchResult(batch, true);
			}
			throw new IllegalStateException("批次不在可结束的展开阶段: " + id);
		}
		LocalDateTime finishedAt = now();
		if (expected > 0) {
			int queued = targetMapper.queueByBatchId(id, finishedAt);
			if (queued != actual) {
				throw new IllegalStateException("目标入队数量不一致: expected=" + actual + ", queued=" + queued);
			}
		}
		if (batchMapper.finishExpansion(id, frozenExpected, status, finishedAt) != 1) {
			throw new IllegalStateException("展开结束状态并发冲突: " + id);
		}
		batch.setStatus(status);
		batch.setExpansionFinishedAt(finishedAt);
		return toBatchResult(batch, false);
	}

	/**
	 * 领取目标并在同一事务内创建尝试记录。
	 */
	@Transactional(rollbackFor = Exception.class)
	public List<AuthOperationClaimedTarget> claim(AuthOperationClaimCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("领取命令不能为空");
		}
		Integer parkId = positive(command.getParkId(), "园区ID");
		String operationQueue = text(command.getOperationQueue(), "操作队列");
		int maxCount = positive(command.getMaxCount(), "领取数量");
		if (maxCount > MAX_CLAIM_COUNT) {
			throw new IllegalArgumentException("领取数量超过上限");
		}
		long leaseSeconds = positiveLong(command.getLeaseSeconds(), "租约秒数");
		if (leaseSeconds > MAX_LEASE_SECONDS) {
			throw new IllegalArgumentException("租约时长超过上限");
		}
		LocalDateTime claimedAt = now();
		LocalDateTime leaseUntil = claimedAt.plusSeconds(leaseSeconds);
		List<SmtAuthOperationTarget> candidates;
		if (command.getTargetIds() != null) {
			if (command.getTargetIds().size() > 200 || maxCount > 200) {
				throw new IllegalArgumentException("精确候选数量超过上限");
			}
			java.util.Set<Long> exact = new java.util.LinkedHashSet<>();
			for (Long targetId : command.getTargetIds()) exact.add(positive(targetId, "目标ID"));
			String accessType = normalizeAccessType(command.getAccessType());
			if (exact.isEmpty()) return Collections.emptyList();
			candidates = targetMapper.selectExactClaimCandidates(parkId, operationQueue, accessType,
					new ArrayList<>(exact), claimedAt, maxCount);
		} else {
			if (command.getAccessType() != null) throw new IllegalArgumentException("接入筛选必须同时提供精确候选集合");
			candidates = targetMapper.selectClaimCandidates(parkId, operationQueue, claimedAt, maxCount);
		}
		if (candidates == null || candidates.isEmpty()) {
			return Collections.emptyList();
		}
		List<AuthOperationClaimedTarget> result = new ArrayList<>();
		for (SmtAuthOperationTarget candidate : candidates) {
			if (result.size() >= maxCount || candidate == null || candidate.getId() == null) {
				continue;
			}
			String leaseToken = UUID.randomUUID().toString();
			if (targetMapper.claimByLease(candidate.getId(), QUEUED, leaseToken,
					claimedAt, leaseUntil) != 1) {
				continue;
			}
			Integer maxAttemptNo = attemptMapper.selectMaxAttemptNo(candidate.getId());
			int attemptNo = defaultInt(maxAttemptNo) + 1;
			SmtAuthOperationAttempt attempt = new SmtAuthOperationAttempt();
			attempt.setId(IdWorker.getId());
			attempt.setTargetId(candidate.getId());
			attempt.setAttemptNo(attemptNo);
			attempt.setAccessType(candidate.getAccessType());
			attempt.setTaskId(candidate.getLegacyTaskId());
			attempt.setStatus(CLAIMED);
			attempt.setLeaseToken(leaseToken);
			attempt.setLeaseUntil(leaseUntil);
			attempt.setCreateTime(claimedAt);
			attempt.setUpdateTime(claimedAt);
			if (attemptMapper.insert(attempt) != 1) {
				throw new IllegalStateException("领取后尝试记录保存失败: targetId=" + candidate.getId());
			}
			candidate.setState(EXECUTING);
			candidate.setLeaseToken(leaseToken);
			candidate.setLeaseUntil(leaseUntil);
			result.add(AuthOperationClaimedTarget.builder()
					.targetId(candidate.getId())
					.attemptId(attempt.getId())
					.attemptNo(attemptNo)
					.targetKey(candidate.getTargetKey())
					.subjectType(candidate.getSubjectType())
					.subjectId(candidate.getSubjectId())
					.deviceId(candidate.getDeviceId())
					.resourceType(candidate.getResourceType())
					.resourceId(candidate.getResourceId())
					.accessType(candidate.getAccessType())
					.operationVersion(candidate.getOperationVersion())
					.state(EXECUTING)
					.leaseToken(leaseToken)
					.leaseUntil(leaseUntil)
					.build());
		}
		return result;
	}

	/**
	 * 外部调用前登记原任务号并把尝试置为 SUBMITTING。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationSubmissionResult prepareSubmission(AuthOperationSubmissionCommand command) {
		SubmissionContext context = validateSubmission(command, false);
		LocalDateTime updatedAt = now();
		int updated = attemptMapper.prepareSubmission(context.attempt.getId(), context.target.getId(),
				command.getLeaseToken(), command.getTaskId(), updatedAt);
		if (updated == 1) {
			context.attempt.setStatus(SUBMITTING);
			context.attempt.setTaskId(command.getTaskId());
		}
		return toSubmissionResult(context, updated == 1,
				updated == 1 ? SUBMITTING : context.attempt.getStatus(), null, null);
	}

	/**
	 * 外部受理成功后登记完整外部批次号/命令号并进入待确认状态。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationSubmissionResult markSubmitted(AuthOperationSubmissionCommand command) {
		SubmissionContext context = validateSubmission(command, true);
		String externalBatchId = optionalText(command.getExternalBatchId());
		String externalCommandId = optionalText(command.getExternalCommandId());
		assertExistingExternal(context.attempt, externalBatchId, externalCommandId);
		LocalDateTime updatedAt = now();
		int updated = attemptMapper.markSubmitted(context.attempt.getId(), context.target.getId(),
				command.getLeaseToken(), command.getTaskId(), externalBatchId, externalCommandId, updatedAt);
		if (updated == 1) {
			if (targetMapper.markWaitingConfirmByLease(context.target.getId(), command.getLeaseToken(), updatedAt) != 1) {
				throw new IllegalStateException("外部受理已登记但目标租约无法进入待确认: " + context.target.getId());
			}
			context.attempt.setStatus(WAITING_CONFIRM);
			context.attempt.setTaskId(command.getTaskId());
			context.attempt.setExternalBatchId(externalBatchId);
			context.attempt.setExternalCommandId(externalCommandId);
		}
		return toSubmissionResult(context, updated == 1,
				updated == 1 ? WAITING_CONFIRM : context.attempt.getStatus(),
				externalBatchId, externalCommandId);
	}

	/**
	 * 便于接入层以参数方式调用的提交前登记重载。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationSubmissionResult prepareSubmission(Long targetId, Long attemptId, Integer attemptNo,
			String leaseToken, String accessType, String taskId) {
		return prepareSubmission(AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(attemptId).attemptNo(attemptNo).leaseToken(leaseToken)
				.accessType(accessType).taskId(taskId).build());
	}

	/**
	 * 便于接入层以参数方式调用的外部受理登记重载。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationSubmissionResult markSubmitted(Long targetId, Long attemptId, Integer attemptNo,
			String leaseToken, String accessType, String taskId, String externalBatchId,
			String externalCommandId) {
		return markSubmitted(AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(attemptId).attemptNo(attemptNo).leaseToken(leaseToken)
				.accessType(accessType).taskId(taskId).externalBatchId(externalBatchId)
				.externalCommandId(externalCommandId).build());
	}

	/**
	 * 记录外部结果证据。回执本身不接受调用方宣称本地已收敛。
	 */
	@Transactional(rollbackFor = Exception.class)
	public AuthOperationReceiptResult recordReceipt(AuthOperationReceiptCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("回执命令不能为空");
		}
		Long targetId = positive(command.getTargetId(), "目标ID");
		Long attemptId = positive(command.getAttemptId(), "尝试ID");
		int attemptNo = positive(command.getAttemptNo(), "尝试号");
		String leaseToken = text(command.getLeaseToken(), "租约令牌");
		String accessType = normalizeAccessType(command.getAccessType());
		String eventNamespace = text(command.getEventNamespace(), "事件命名空间");
		String eventKey = text(command.getEventKey(), "事件键");
		String evidenceType = text(command.getEvidenceType(), "证据类型");
		String resultStatus = text(command.getResultStatus(), "结果状态");
		Long operationVersion = positive(command.getOperationVersion(), "操作版本");

		SmtAuthOperationTarget target = targetMapper.selectById(targetId);
		if (target == null) {
			throw new IllegalArgumentException("目标不存在: " + targetId);
		}
		SmtAuthOperationAttempt attempt = attemptMapper.selectByIdAndTarget(attemptId, targetId);
		if (attempt == null) {
			throw new IllegalArgumentException("尝试不存在或不属于目标: " + attemptId);
		}
		if (!Objects.equals(attempt.getAttemptNo(), attemptNo)) {
			throw new IllegalArgumentException("尝试号不匹配: " + attemptId);
		}
		if (!sameAccessType(attempt.getAccessType(), accessType)) {
			throw new IllegalArgumentException("回执接入类型不匹配: " + attemptId);
		}
		if (!Objects.equals(normalize(attempt.getLeaseToken()), leaseToken)) {
			throw new IllegalArgumentException("回执租约令牌不匹配: " + attemptId);
		}
		if (!sameAccessType(target.getAccessType(), accessType)) {
			throw new IllegalArgumentException("目标接入类型不匹配: " + targetId);
		}
		assertReceiptExternalIdentifiers(attempt, command);

		SmtAuthResultEvent existing = eventMapper.selectByAttemptAndEventKey(attemptId, eventKey);
		if (existing != null) {
			assertSameEvent(existing, command, targetId, attemptId, accessType, operationVersion);
			return receiptResult(target, existing.getId(), attemptId, true);
		}

		boolean terminalTarget = isTerminalState(target.getState());
		boolean externalComplete = completeExternalIdentifiers(accessType, attempt, command);
		boolean versionMatches = Objects.equals(target.getOperationVersion(), operationVersion);
		boolean trustedSuccess = externalComplete && versionMatches
				&& command.isTrustedDeviceEvidence() && SUCCESS.equalsIgnoreCase(resultStatus);
		String targetState = terminalTarget ? target.getState() : (trustedSuccess ? CONFIRMED : VERIFYING);
		String failureReason = terminalTarget
				? "目标已终态(" + target.getState() + ")，证据仅留存未参与状态归并"
				: receiptFailureReason(resultStatus, externalComplete, versionMatches,
						command.isTrustedDeviceEvidence());
		LocalDateTime receivedAt = now();
		SmtAuthResultEvent event = new SmtAuthResultEvent();
		event.setId(IdWorker.getId());
		event.setTargetId(targetId);
		event.setAttemptId(attemptId);
		event.setEventKey(eventKey);
		event.setEventNamespace(eventNamespace);
		event.setAccessType(accessType);
		event.setExternalBatchId(optionalText(command.getExternalBatchId()));
		event.setExternalCommandId(optionalText(command.getExternalCommandId()));
		event.setOperationVersion(operationVersion);
		event.setEvidenceType(evidenceType);
		event.setResultStatus(resultStatus);
		event.setEvidenceBody(command.getEvidenceBody());
		event.setReceivedAt(receivedAt);
		event.setConverged("N");
		event.setFailureReason(failureReason);
		try {
			if (eventMapper.insert(event) != 1) {
				throw new IllegalStateException("结果证据保存失败: " + eventKey);
			}
		} catch (DuplicateKeyException duplicate) {
			SmtAuthResultEvent concurrent = eventMapper.selectByAttemptAndEventKey(attemptId, eventKey);
			if (concurrent != null) {
				assertSameEvent(concurrent, command, targetId, attemptId, accessType, operationVersion);
				return receiptResult(target, concurrent.getId(), attemptId, true);
			}
			throw duplicate;
		}
		if (terminalTarget) {
			// 终态目标的新证据只作为审计留存，不能改写任何历史尝试的状态、时间或事件指针。
			return receiptResult(target, event.getId(), attemptId, false);
		}
		if (attemptMapper.markReceipt(attemptId, targetId, targetState, event.getId(),
				failureReason, receivedAt) != 1) {
			// 事件已经保存；较弱或较旧状态不能覆盖已确认尝试，也不能让证据因状态 CAS 未命中而回滚。
			SmtAuthOperationTarget current = targetMapper.selectById(targetId);
			return receiptResult(current == null ? target : current, event.getId(), attemptId, false);
		}
		int stateUpdated = targetMapper.updateStateByLease(targetId, leaseToken, targetState,
				receivedAt, failureReason);
		if (stateUpdated != 1) {
			// 证据和尝试历史仍保留，但旧租约不能覆盖新归属。
			return receiptResult(target, event.getId(), attemptId, false);
		}
		target.setState(targetState);
		return AuthOperationReceiptResult.builder()
				.targetId(targetId)
				.attemptId(attemptId)
				.eventId(event.getId())
				.state(targetState)
				.confirmed(CONFIRMED.equals(targetState))
				.converged(false)
				.duplicate(false)
				.build();
	}

	/**
	 * 从目标状态实时重算批次进度。
	 */
	@Transactional(readOnly = true)
	public AuthOperationProgress getProgress(Long batchId) {
		Long id = positive(batchId, "批次ID");
		SmtAuthOperationBatch batch = requiredBatch(id);
		Map<String, Integer> counts = new HashMap<>();
		List<AuthOperationStateCount> grouped = targetMapper.countByBatchIdGroupByState(id);
		if (grouped != null) {
			for (AuthOperationStateCount row : grouped) {
				if (row != null && row.getState() != null) {
					counts.put(row.getState(), safeCount(row.getTargetCount()));
				}
			}
		}
		int frozenExpected = defaultInt(batch.getExpectedCount());
		int expanded = defaultInt(batch.getExpandedCount());
		int unexpanded = Math.max(0, frozenExpected - expanded);
		int preparing = countState(counts, PREPARING) + unexpanded;
		int queued = countState(counts, QUEUED);
		int executing = countState(counts, EXECUTING);
		int waitingConfirm = countState(counts, WAITING_CONFIRM);
		int verifying = countState(counts, VERIFYING);
		int confirmed = countState(counts, CONFIRMED);
		int converged = countState(counts, CONVERGED);
		int failed = countState(counts, FAILED);
		int expandedTargetTotal = 0;
		for (Integer value : counts.values()) {
			expandedTargetTotal += safeCount(value);
		}
		int total = expandedTargetTotal + unexpanded;
		return AuthOperationProgress.builder()
				.batchId(id)
				.batchStatus(batch.getStatus())
				.expectedCount(batch.getExpectedCount())
				.expandedCount(batch.getExpandedCount())
				.expansionCursor(batch.getExpansionCursor())
				.totalTargetCount(total)
				.preparingCount(preparing)
				.queuedCount(queued)
				.executingCount(executing)
				.waitingConfirmCount(waitingConfirm)
				.verifyingCount(verifying)
				.confirmedCount(confirmed)
				.convergedCount(converged)
				.failedCount(failed)
				// 未知、CONFIRMED、FAILED 和 RETAINED（本阶段没有受控来源证据）均仍未完成。
				.unfinishedCount(Math.max(0, total - converged))
				.build();
	}

	/**
	 * 查找租约已到期且仍未完成的目标；WAITING_CONFIRM 不会被当作 QUEUED 重发。
	 */
	@Transactional(readOnly = true)
	public List<SmtAuthOperationTarget> findExpiredUnfinishedTargets(Integer parkId,
			LocalDateTime now, Integer limit) {
		Integer id = positive(parkId, "园区ID");
		int boundedLimit = positive(limit, "查询数量");
		if (boundedLimit > MAX_CLAIM_COUNT) {
			throw new IllegalArgumentException("查询数量超过上限");
		}
		List<SmtAuthOperationTarget> result = targetMapper.selectExpiredUnfinished(
				id, now == null ? now() : now, boundedLimit);
		return result == null ? Collections.emptyList() : result;
	}

	private SubmissionContext validateSubmission(AuthOperationSubmissionCommand command, boolean external) {
		if (command == null) {
			throw new IllegalArgumentException("提交登记命令不能为空");
		}
		Long targetId = positive(command.getTargetId(), "目标ID");
		Long attemptId = positive(command.getAttemptId(), "尝试ID");
		positive(command.getAttemptNo(), "尝试号");
		String leaseToken = text(command.getLeaseToken(), "租约令牌");
		String accessType = normalizeAccessType(command.getAccessType());
		String taskId = text(command.getTaskId(), "原任务号");
		if (external) {
			validateExternalIdentifiers(accessType, command);
		}
		SmtAuthOperationTarget target = targetMapper.selectById(targetId);
		if (target == null) {
			throw new IllegalArgumentException("目标不存在: " + targetId);
		}
		SmtAuthOperationAttempt attempt = attemptMapper.selectByIdAndTarget(attemptId, targetId);
		if (attempt == null) {
			throw new IllegalArgumentException("尝试不存在或不属于目标: " + attemptId);
		}
		if (!Objects.equals(attempt.getAttemptNo(), command.getAttemptNo())) {
			throw new IllegalArgumentException("尝试号不匹配: " + attemptId);
		}
		if (!Objects.equals(normalize(attempt.getLeaseToken()), leaseToken)) {
			throw new IllegalArgumentException("提交登记租约令牌不匹配: " + attemptId);
		}
		if (!sameAccessType(attempt.getAccessType(), accessType)
				|| !sameAccessType(target.getAccessType(), accessType)) {
			throw new IllegalArgumentException("提交登记接入类型不匹配: " + attemptId);
		}
		return new SubmissionContext(target, attempt);
	}

	private AuthOperationSubmissionResult toSubmissionResult(SubmissionContext context, boolean persisted,
			String status, String externalBatchId, String externalCommandId) {
		return AuthOperationSubmissionResult.builder()
				.targetId(context.target.getId())
				.attemptId(context.attempt.getId())
				.attemptNo(context.attempt.getAttemptNo())
				.status(status)
				.taskId(context.attempt.getTaskId())
				.externalBatchId(externalBatchId == null ? context.attempt.getExternalBatchId() : externalBatchId)
				.externalCommandId(externalCommandId == null ? context.attempt.getExternalCommandId() : externalCommandId)
				.persisted(persisted)
				.build();
	}

	private void assertExistingExternal(SmtAuthOperationAttempt attempt, String externalBatchId,
			String externalCommandId) {
		if (hasText(attempt.getExternalBatchId())
				&& !Objects.equals(normalize(attempt.getExternalBatchId()), normalize(externalBatchId))) {
			throw new IllegalArgumentException("外部批次号与已登记值冲突");
		}
		if (hasText(attempt.getExternalCommandId())
				&& !Objects.equals(normalize(attempt.getExternalCommandId()), normalize(externalCommandId))) {
			throw new IllegalArgumentException("外部命令号与已登记值冲突");
		}
	}

	private void validateExternalIdentifiers(String accessType, AuthOperationSubmissionCommand command) {
		if (ISC.equals(accessType) && !hasText(command.getExternalBatchId())) {
			throw new IllegalArgumentException("ISC必须提供真实外部批次号/任务号");
		}
		if (DIRECT.equals(accessType) && !hasText(command.getExternalCommandId())) {
			throw new IllegalArgumentException("DIRECT必须提供真实外部命令流水号");
		}
	}

	private boolean completeExternalIdentifiers(String accessType, SmtAuthOperationAttempt attempt,
			AuthOperationReceiptCommand command) {
		if (ISC.equals(accessType)) {
			return hasText(command.getExternalBatchId()) && hasText(attempt.getExternalBatchId())
					&& Objects.equals(normalize(command.getExternalBatchId()), normalize(attempt.getExternalBatchId()));
		}
		if (DIRECT.equals(accessType)) {
			return hasText(command.getExternalCommandId()) && hasText(attempt.getExternalCommandId())
					&& Objects.equals(normalize(command.getExternalCommandId()), normalize(attempt.getExternalCommandId()));
		}
		return false;
	}

	private void assertReceiptExternalIdentifiers(SmtAuthOperationAttempt attempt,
			AuthOperationReceiptCommand command) {
		if (hasText(command.getExternalBatchId()) && hasText(attempt.getExternalBatchId())
				&& !Objects.equals(normalize(command.getExternalBatchId()), normalize(attempt.getExternalBatchId()))) {
			throw new IllegalArgumentException("回执外部批次号不匹配");
		}
		if (hasText(command.getExternalCommandId()) && hasText(attempt.getExternalCommandId())
				&& !Objects.equals(normalize(command.getExternalCommandId()), normalize(attempt.getExternalCommandId()))) {
			throw new IllegalArgumentException("回执外部命令号不匹配");
		}
	}

	private static boolean isTerminalState(String state) {
		return CONFIRMED.equals(state) || CONVERGED.equals(state) || FAILED.equals(state);
	}

	private void assertSameEvent(SmtAuthResultEvent existing, AuthOperationReceiptCommand command,
			Long targetId, Long attemptId, String accessType, Long operationVersion) {
		if (!Objects.equals(existing.getTargetId(), targetId)
				|| !Objects.equals(existing.getAttemptId(), attemptId)
				|| !Objects.equals(normalize(existing.getEventNamespace()), normalize(command.getEventNamespace()))
				|| !Objects.equals(normalize(existing.getAccessType()), accessType)
				|| !Objects.equals(normalize(existing.getExternalBatchId()), optionalText(command.getExternalBatchId()))
				|| !Objects.equals(normalize(existing.getExternalCommandId()), optionalText(command.getExternalCommandId()))
				|| !Objects.equals(existing.getOperationVersion(), operationVersion)
				|| !Objects.equals(normalize(existing.getEvidenceType()), normalize(command.getEvidenceType()))
				|| !Objects.equals(normalize(existing.getResultStatus()), normalize(command.getResultStatus()))
				|| !Objects.equals(existing.getEvidenceBody(), command.getEvidenceBody())) {
			throw new IllegalArgumentException("同一尝试事件键的证据内容冲突");
		}
	}

	private String receiptFailureReason(String resultStatus, boolean externalComplete,
			boolean versionMatches, boolean trustedEvidence) {
		if (!SUCCESS.equalsIgnoreCase(resultStatus)) {
			return "外部结果未成功: " + resultStatus;
		}
		if (!externalComplete) {
			return "缺少本次接入所需外部标识或持久映射";
		}
		if (!versionMatches) {
			return "操作版本不匹配";
		}
		if (!trustedEvidence) {
			return "缺少可信设备证据";
		}
		return null;
	}

	private AuthOperationReceiptResult receiptResult(SmtAuthOperationTarget target, Long eventId,
			Long attemptId, boolean duplicate) {
		String state = target.getState();
		return AuthOperationReceiptResult.builder()
				.targetId(target.getId())
				.attemptId(attemptId)
				.eventId(eventId)
				.state(state)
				.confirmed(CONFIRMED.equals(state))
				.converged(CONVERGED.equals(state))
				.duplicate(duplicate)
				.build();
	}

	private Map<Long, AuthOperationRequestCommand> indexRequestCommands(
			List<AuthOperationRequestCommand> requests) {
		Map<Long, AuthOperationRequestCommand> result = new LinkedHashMap<>();
		for (AuthOperationRequestCommand request : requests) {
			if (request == null) {
				throw new IllegalArgumentException("请求种子不能为空");
			}
			Long id = positive(request.getId(), "预分配请求ID");
			AuthOperationRequestCommand previous = result.put(id, request);
			if (previous != null && !sameRequestCommand(previous, request)) {
				throw new IllegalArgumentException("同分片请求ID内容冲突: " + id);
			}
		}
		return result;
	}

	private Map<String, AuthOperationTargetCommand> indexTargetCommands(
			List<AuthOperationTargetCommand> targets) {
		Map<String, AuthOperationTargetCommand> result = new LinkedHashMap<>();
		for (AuthOperationTargetCommand target : targets) {
			if (target == null) {
				throw new IllegalArgumentException("目标不能为空");
			}
			String key = text(target.getTargetKey(), "目标键");
			AuthOperationTargetCommand previous = result.put(key, target);
			if (previous != null && !sameTargetCommand(previous, target)) {
				throw new IllegalArgumentException("同分片目标键内容冲突: " + key);
			}
		}
		return result;
	}

	private Map<Long, SmtAuthDeleteRequest> loadRequests(SmtAuthOperationBatch batch, Set<Long> requestIds) {
	if (requestIds.isEmpty()) {
			return new HashMap<>();
		}
		List<SmtAuthDeleteRequest> rows = deleteRequestMapper.selectByBatchIdAndIds(
				batch.getId(), batch.getParkId(), new ArrayList<>(requestIds));
		Map<Long, SmtAuthDeleteRequest> result = new HashMap<>();
		if (rows != null) {
			for (SmtAuthDeleteRequest row : rows) {
				if (row != null && row.getId() != null) {
					result.put(row.getId(), row);
				}
			}
		}
		return result;
	}

	private Map<String, SmtAuthOperationTarget> loadTargets(Long batchId, Set<String> targetKeys) {
		if (targetKeys.isEmpty()) {
			return new HashMap<>();
		}
		List<SmtAuthOperationTarget> rows = targetMapper.selectByBatchIdAndTargetKeys(
				batchId, new ArrayList<>(targetKeys));
		Map<String, SmtAuthOperationTarget> result = new HashMap<>();
		if (rows != null) {
			for (SmtAuthOperationTarget row : rows) {
				if (row != null && row.getTargetKey() != null) {
					result.put(row.getTargetKey(), row);
				}
			}
		}
		return result;
	}

	private SmtAuthDeleteRequest insertRequest(AuthOperationRequestCommand command,
			SmtAuthOperationBatch batch) {
		LocalDateTime createdAt = now();
		SmtAuthDeleteRequest request = new SmtAuthDeleteRequest();
		request.setId(command.getId());
		request.setBatchId(batch.getId());
		request.setParkId(batch.getParkId());
		request.setSubjectType(text(command.getSubjectType(), "主体类型"));
		request.setSourceType(text(command.getSourceType(), "来源类型"));
		request.setSourceRowId(optionalText(command.getSourceRowId()));
		request.setSourceIdentityKey(text(command.getSourceIdentityKey(), "来源身份键"));
		request.setIdentitySnapshot(text(command.getIdentitySnapshot(), "身份快照"));
		request.setGeneration(nonNegativeLong(command.getGeneration(), "代次"));
		request.setDeadlineAt(command.getDeadlineAt());
		request.setStatus(PREPARING);
		request.setCreateTime(createdAt);
		request.setUpdateTime(createdAt);
		try {
			if (deleteRequestMapper.insert(request) != 1) {
				throw new IllegalStateException("删除请求保存失败: " + request.getId());
			}
		} catch (DuplicateKeyException duplicate) {
			throw new IllegalArgumentException("同来源请求已存在但内容未完成核验: " + request.getId(), duplicate);
		}
		return request;
	}

	private SmtAuthOperationTarget insertTarget(AuthOperationTargetCommand command,
			SmtAuthOperationBatch batch) {
		LocalDateTime createdAt = now();
		SmtAuthOperationTarget target = new SmtAuthOperationTarget();
		target.setId(command.getId() == null ? IdWorker.getId() : command.getId());
		target.setBatchId(batch.getId());
		target.setRequestId(command.getRequestId());
		target.setParkId(batch.getParkId());
		target.setTargetKey(text(command.getTargetKey(), "目标键"));
		target.setSubjectType(text(command.getSubjectType(), "主体类型"));
		target.setSubjectId(text(command.getSubjectId(), "主体ID"));
		target.setSubjectSnapshot(command.getSubjectSnapshot());
		target.setResourceType(text(command.getResourceType(), "资源类型"));
		target.setDeviceId(text(command.getDeviceId(), "设备ID"));
		target.setResourceId(text(command.getResourceId(), "资源ID"));
		target.setAccessType(text(command.getAccessType(), "接入类型"));
		target.setOperationQueue(text(command.getOperationQueue(), "操作队列"));
		target.setAction(text(command.getAction(), "动作"));
		target.setValidFrom(command.getValidFrom());
		target.setValidTo(command.getValidTo());
		target.setOperationVersion(positive(command.getOperationVersion(), "操作版本"));
		target.setLegacyTaskId(optionalText(command.getLegacyTaskId()));
		target.setState(PREPARING);
		target.setAcceptedAt(createdAt);
		target.setCreateTime(createdAt);
		target.setUpdateTime(createdAt);
		try {
			if (targetMapper.insert(target) != 1) {
				throw new IllegalStateException("权限目标保存失败: " + target.getTargetKey());
			}
		} catch (DuplicateKeyException duplicate) {
			throw new IllegalArgumentException("目标唯一键冲突，拒绝静默丢弃输入: " + target.getTargetKey(), duplicate);
		}
		return target;
	}

	private void validateRequestCommand(AuthOperationRequestCommand command,
			SmtAuthOperationBatch batch) {
		positive(command.getId(), "预分配请求ID");
		if (command.getParkId() != null && !Objects.equals(command.getParkId(), batch.getParkId())) {
			throw new IllegalArgumentException("请求园区与批次不一致: " + command.getId());
		}
		text(command.getSubjectType(), "主体类型");
		String sourceType = text(command.getSourceType(), "来源类型");
		if (!ORPHAN_SOURCE_TYPE.equals(sourceType)) {
			text(command.getSourceRowId(), "来源行号");
		}
		text(command.getSourceIdentityKey(), "来源身份键");
		text(command.getIdentitySnapshot(), "身份快照");
		nonNegativeLong(command.getGeneration(), "代次");
	}

	private void validateTargetCommand(AuthOperationTargetCommand command,
			SmtAuthOperationBatch batch, Map<Long, SmtAuthDeleteRequest> requests) {
		if (command.getParkId() != null && !Objects.equals(command.getParkId(), batch.getParkId())) {
			throw new IllegalArgumentException("目标园区与批次不一致: " + command.getTargetKey());
		}
		String action = text(command.getAction(), "动作");
		// 批次记录业务意图；共享来源撤销可能需要物理 ADD 来保留剩余精确窗口。
		if (!"ADD".equals(action) && !"DELETE".equals(action)) {
			throw new IllegalArgumentException("不支持的物理目标动作: " + action);
		}
		Long requestId = command.getRequestId();
		if ("DELETE".equalsIgnoreCase(action) && requestId == null) {
			throw new IllegalArgumentException("DELETE目标必须关联持久请求: " + command.getTargetKey());
		}
		if (requestId != null && !requests.containsKey(requestId)) {
			throw new IllegalArgumentException("目标引用的请求不属于当前批次/园区: " + requestId);
		}
		text(command.getTargetKey(), "目标键");
		text(command.getSubjectType(), "主体类型");
		text(command.getSubjectId(), "主体ID");
		text(command.getResourceType(), "资源类型");
		text(command.getDeviceId(), "设备ID");
		text(command.getResourceId(), "资源ID");
		text(command.getAccessType(), "接入类型");
		text(command.getOperationQueue(), "操作队列");
		positive(command.getOperationVersion(), "操作版本");
		if (command.getValidFrom() != null && command.getValidTo() != null
				&& command.getValidTo().isBefore(command.getValidFrom())) {
			throw new IllegalArgumentException("目标有效期结束时间早于开始时间: " + command.getTargetKey());
		}
	}

	private void assertSameRequest(SmtAuthDeleteRequest existing,
			AuthOperationRequestCommand command, SmtAuthOperationBatch batch) {
		if (!Objects.equals(existing.getId(), command.getId())
				|| !Objects.equals(existing.getBatchId(), batch.getId())
				|| !Objects.equals(existing.getParkId(), batch.getParkId())
				|| !Objects.equals(normalize(existing.getSubjectType()), normalize(command.getSubjectType()))
				|| !Objects.equals(normalize(existing.getSourceType()), normalize(command.getSourceType()))
				|| !Objects.equals(normalize(existing.getSourceRowId()), optionalText(command.getSourceRowId()))
				|| !Objects.equals(normalize(existing.getSourceIdentityKey()), normalize(command.getSourceIdentityKey()))
				|| !Objects.equals(existing.getIdentitySnapshot(), command.getIdentitySnapshot())
				|| !Objects.equals(existing.getGeneration(), command.getGeneration())
				|| !Objects.equals(existing.getDeadlineAt(), command.getDeadlineAt())) {
			throw new IllegalArgumentException("同一请求ID的来源/主体快照内容冲突: " + command.getId());
		}
	}

	private void assertSameTarget(SmtAuthOperationTarget existing,
			AuthOperationTargetCommand command, SmtAuthOperationBatch batch) {
		if ((command.getId() != null && !Objects.equals(existing.getId(), command.getId()))
				|| !Objects.equals(existing.getBatchId(), batch.getId())
				|| !Objects.equals(existing.getParkId(), batch.getParkId())
				|| !Objects.equals(existing.getRequestId(), command.getRequestId())
				|| !Objects.equals(normalize(existing.getTargetKey()), normalize(command.getTargetKey()))
				|| !Objects.equals(normalize(existing.getSubjectType()), normalize(command.getSubjectType()))
				|| !Objects.equals(normalize(existing.getSubjectId()), normalize(command.getSubjectId()))
				|| !Objects.equals(existing.getSubjectSnapshot(), command.getSubjectSnapshot())
				|| !Objects.equals(normalize(existing.getResourceType()), normalize(command.getResourceType()))
				|| !Objects.equals(normalize(existing.getDeviceId()), normalize(command.getDeviceId()))
				|| !Objects.equals(normalize(existing.getResourceId()), normalize(command.getResourceId()))
				|| !Objects.equals(normalize(existing.getAccessType()), normalize(command.getAccessType()))
				|| !Objects.equals(normalize(existing.getOperationQueue()), normalize(command.getOperationQueue()))
				|| !Objects.equals(normalize(existing.getAction()), normalize(command.getAction()))
				|| !Objects.equals(existing.getValidFrom(), command.getValidFrom())
				|| !Objects.equals(existing.getValidTo(), command.getValidTo())
				|| !Objects.equals(existing.getOperationVersion(), command.getOperationVersion())
				|| !Objects.equals(normalize(existing.getLegacyTaskId()), optionalText(command.getLegacyTaskId()))) {
			throw new IllegalArgumentException("重复分片目标内容冲突: " + command.getTargetKey());
		}
	}

	private boolean sameRequestCommand(AuthOperationRequestCommand left,
			AuthOperationRequestCommand right) {
		return Objects.equals(left.getId(), right.getId())
				&& Objects.equals(left.getParkId(), right.getParkId())
				&& Objects.equals(left.getSubjectType(), right.getSubjectType())
				&& Objects.equals(left.getSourceType(), right.getSourceType())
				&& Objects.equals(left.getSourceRowId(), right.getSourceRowId())
				&& Objects.equals(left.getSourceIdentityKey(), right.getSourceIdentityKey())
				&& Objects.equals(left.getIdentitySnapshot(), right.getIdentitySnapshot())
				&& Objects.equals(left.getGeneration(), right.getGeneration())
				&& Objects.equals(left.getDeadlineAt(), right.getDeadlineAt());
	}

	private boolean sameTargetCommand(AuthOperationTargetCommand left,
			AuthOperationTargetCommand right) {
		return Objects.equals(left.getId(), right.getId())
				&& Objects.equals(left.getRequestId(), right.getRequestId())
				&& Objects.equals(left.getParkId(), right.getParkId())
				&& Objects.equals(left.getTargetKey(), right.getTargetKey())
				&& Objects.equals(left.getSubjectType(), right.getSubjectType())
				&& Objects.equals(left.getSubjectId(), right.getSubjectId())
				&& Objects.equals(left.getSubjectSnapshot(), right.getSubjectSnapshot())
				&& Objects.equals(left.getResourceType(), right.getResourceType())
				&& Objects.equals(left.getDeviceId(), right.getDeviceId())
				&& Objects.equals(left.getResourceId(), right.getResourceId())
				&& Objects.equals(left.getAccessType(), right.getAccessType())
				&& Objects.equals(left.getOperationQueue(), right.getOperationQueue())
				&& Objects.equals(left.getAction(), right.getAction())
				&& Objects.equals(left.getValidFrom(), right.getValidFrom())
				&& Objects.equals(left.getValidTo(), right.getValidTo())
				&& Objects.equals(left.getOperationVersion(), right.getOperationVersion())
				&& Objects.equals(left.getLegacyTaskId(), right.getLegacyTaskId());
	}

	private void assertSameBatchIntent(SmtAuthOperationBatch existing, Integer parkId,
			String action, String sourceType, String sourceId, String selectionSnapshot,
			String payloadFingerprint, Integer expectedCount) {
		if (!Objects.equals(existing.getParkId(), parkId)
				|| !Objects.equals(normalize(existing.getAction()), action)
				|| !Objects.equals(normalize(existing.getSourceType()), sourceType)
				|| !Objects.equals(normalize(existing.getSourceId()), sourceId)
				|| !Objects.equals(existing.getSelectionSnapshot(), selectionSnapshot)
				|| !Objects.equals(normalize(existing.getPayloadFingerprint()), payloadFingerprint)
				|| !Objects.equals(existing.getExpectedCount(), expectedCount)) {
			throw new IllegalArgumentException("同幂等键的指纹或完整业务意图冲突");
		}
	}

	private String canonicalFingerprint(String action, String sourceType, String sourceId,
			String selectionSnapshot, Integer expectedCount) {
		String material = fingerprintPart(action) + fingerprintPart(sourceType) + fingerprintPart(sourceId)
				+ fingerprintPart(selectionSnapshot) + fingerprintPart(String.valueOf(expectedCount));
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(material.getBytes(StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				result.append(Character.forDigit((value >>> 4) & 0x0f, 16));
				result.append(Character.forDigit(value & 0x0f, 16));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException("JVM不支持SHA-256", impossible);
		}
	}

	private String fingerprintPart(String value) {
		return value == null ? "-1:" : value.length() + ":" + value;
	}

	private void validateCanonicalFingerprint(String supplied, String canonical) {
		if (supplied.matches("(?i)[0-9a-f]{64}") && !supplied.equalsIgnoreCase(canonical)) {
			throw new IllegalArgumentException("payload指纹与完整业务意图不一致");
		}
	}

	private AuthOperationBatchResult toBatchResult(SmtAuthOperationBatch batch, boolean idempotent) {
		return AuthOperationBatchResult.builder()
				.batchId(batch.getId())
				.status(batch.getStatus())
				.expectedCount(batch.getExpectedCount())
				.expandedCount(batch.getExpandedCount())
				.expansionCursor(batch.getExpansionCursor())
				.idempotent(idempotent)
				.build();
	}

	private AuthOperationExpansionResult expansionResult(SmtAuthOperationBatch batch,
			long previousCursor, long nextCursor, int appendedCount, int expandedCount) {
		return AuthOperationExpansionResult.builder()
				.batchId(batch.getId())
				.previousCursor(previousCursor)
				.nextCursor(nextCursor)
				.appendedCount(appendedCount)
				.expandedCount(expandedCount)
				.status(batch.getStatus())
				.build();
	}

	private SmtAuthOperationBatch requiredBatch(Long batchId) {
		SmtAuthOperationBatch batch = batchMapper.selectById(batchId);
		if (batch == null) {
			throw new IllegalArgumentException("批次不存在: " + batchId);
		}
		return batch;
	}

	private static int countState(Map<String, Integer> counts, String state) {
		return safeCount(counts.get(state));
	}

	private LocalDateTime now() {
		return LocalDateTime.now(clock);
	}

	private static <T> List<T> listOrEmpty(List<T> value) {
		return value == null ? Collections.emptyList() : value;
	}

	private static String text(String value, String field) {
		String normalized = normalize(value);
		if (normalized == null) {
			throw new IllegalArgumentException(field + "不能为空");
		}
		return normalized;
	}

	private static String optionalText(String value) {
		return normalize(value);
	}

	private static String normalizeAccessType(String value) {
		String normalized = text(value, "接入类型").toUpperCase(Locale.ROOT);
		if (!ISC.equals(normalized) && !DIRECT.equals(normalized)) {
			throw new IllegalArgumentException("未知接入类型，不能直接登记: " + normalized);
		}
		return normalized;
	}

	private static boolean sameAccessType(String left, String right) {
		return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
	}

	private static boolean hasText(String value) {
		return normalize(value) != null;
	}

	private static String normalize(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private static int safeCount(Integer value) {
		return value == null ? 0 : value;
	}

	private static int defaultInt(Integer value) {
		return value == null ? 0 : value;
	}

	private static long defaultLong(Long value) {
		return value == null ? 0L : value;
	}

	private static Integer positive(Integer value, String field) {
		if (value == null || value <= 0) {
			throw new IllegalArgumentException(field + "必须为正数");
		}
		return value;
	}

	private static Long positive(Long value, String field) {
		if (value == null || value <= 0L) {
			throw new IllegalArgumentException(field + "必须为正数");
		}
		return value;
	}

	private static long positiveLong(Long value, String field) {
		if (value == null || value <= 0L) {
			throw new IllegalArgumentException(field + "必须为正数");
		}
		return value;
	}

	private static int nonNegative(Integer value, String field) {
		if (value == null || value < 0) {
			throw new IllegalArgumentException(field + "不能为负数");
		}
		return value;
	}

	private static long nonNegativeLong(Long value, String field) {
		if (value == null || value < 0L) {
			throw new IllegalArgumentException(field + "不能为负数");
		}
		return value;
	}

	private static final class SubmissionContext {
		private final SmtAuthOperationTarget target;
		private final SmtAuthOperationAttempt attempt;

		private SubmissionContext(SmtAuthOperationTarget target, SmtAuthOperationAttempt attempt) {
			this.target = target;
			this.attempt = attempt;
		}
	}
}
