package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.*;
import com.tce.smart.platform.core.entity.SmtAuthLegacyReview;
import com.tce.smart.platform.core.entity.SmtAuthLegacyScanFlow;
import com.tce.smart.platform.core.mapper.LegacyEmployeeAccessInventoryMapper;
import com.tce.smart.platform.core.service.LegacyEmployeeAccessInventoryService;
import com.tce.smart.platform.core.service.LegacyInventoryCanonicalizer;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** 五流员工历史盘点实现；所有写入只落盘点 review 和 flow 控制表。 */
@Service
public class LegacyEmployeeAccessInventoryServiceImpl implements LegacyEmployeeAccessInventoryService {

	public static final String PARK_REVIEW_PERMISSION = "platform_auth_operation_review_view";
	public static final String GLOBAL_REVIEW_PERMISSION = "platform_auth_operation_global_review_view";
	private static final int MAX_PAGE_SIZE = 200;
	private static final int MAX_LEASE_SECONDS = 300;
	private final LegacyEmployeeAccessInventoryMapper mapper;
	private final LegacyInventoryCanonicalizer canonicalizer;
	private final boolean enabled;

	public LegacyEmployeeAccessInventoryServiceImpl(LegacyEmployeeAccessInventoryMapper mapper,
			LegacyInventoryCanonicalizer canonicalizer,
			@Value("${smart.auth-legacy-inventory.enabled:false}") boolean enabled) {
		this.mapper = Objects.requireNonNull(mapper, "历史盘点Mapper不能为空");
		this.canonicalizer = Objects.requireNonNull(canonicalizer, "历史盘点规范器不能为空");
		this.enabled = enabled;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public ScanRun openRun(InventoryScanRequest request) {
		enabled();
		InventoryScanRequest valid = scanRequest(request);
		List<SmtAuthLegacyScanFlow> active = mapper.lockActiveFlows();
		if (active != null && !active.isEmpty()) return recoverActive(active, valid.getScopeFingerprint());
		LocalDateTime now = requiredNow();
		String runId = "legacy-" + UUID.randomUUID().toString().replace("-", "");
		List<ScanFlowKey> keys = new ArrayList<>();
		for (FlowKind kind : FlowKind.values()) {
			Long physical = mapper.selectPhysicalHighWater(kind.name());
			long highWater = physical == null ? 0L : physical;
			if (highWater < 0) throw new IllegalStateException("历史主键域不是正数，不能使用当前游标合同");
			LocalDateTime updateHighWater = kind.hasUpdatePass()
					? mapper.selectUpdateHighWater(kind.name(), now) : null;
			SmtAuthLegacyScanFlow flow = new SmtAuthLegacyScanFlow();
			flow.setId(IdWorker.getId()); flow.setRunId(runId); flow.setFlowKind(kind.name());
			flow.setRequestedBy(valid.getRequestedBy()); flow.setScopeFingerprint(valid.getScopeFingerprint());
			flow.setAuditTicket(valid.getAuditTicket()); flow.setCaptureCutoff(now);
			flow.setIdHighWater(highWater); flow.setIdLastId(0L);
			flow.setUpdateHighWaterAt(updateHighWater); flow.setUpdateLastAt(null); flow.setUpdateLastId(0L);
			flow.setRevisitRequired("Y"); flow.setRevisitHighWaterId(highWater); flow.setRevisitLastId(0L);
			flow.setActivePass("ID"); flow.setIdPassDone("N");
			flow.setUpdatePassDone(updateHighWater == null ? "Y" : "N"); flow.setRevisitPassDone("N");
			flow.setPageSize(valid.getPageSize()); flow.setRowVersion(0L); flow.setFlowState("READY");
			flow.setCreatedAt(now); flow.setUpdatedAt(now);
			try {
				if (mapper.insertFlow(flow) != 1) throw new IllegalStateException("创建历史盘点流失败");
			} catch (DuplicateKeyException concurrentActiveFlow) {
				if (kind != FlowKind.CURRENT_SOURCE) throw concurrentActiveFlow;
				List<SmtAuthLegacyScanFlow> winner = mapper.lockActiveFlows();
				return recoverActive(winner == null ? new ArrayList<>() : winner, valid.getScopeFingerprint());
			}
			keys.add(flowKey(flow));
		}
		return ScanRun.builder().runId(runId).scopeFingerprint(valid.getScopeFingerprint())
				.captureCutoff(now).flows(keys).recovered(false).build();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public ScanLease claimFlow(String runId, FlowKind flowKind, String workerId,
			int leaseSeconds, long expectedRowVersion) {
		enabled();
		String validRun = text(runId, 64, "runId");
		FlowKind validKind = required(flowKind, "flowKind");
		String owner = text(workerId, 128, "workerId");
		if (leaseSeconds < 1 || leaseSeconds > MAX_LEASE_SECONDS) throw new IllegalArgumentException("leaseSeconds必须为1至300");
		SmtAuthLegacyScanFlow flow = mapper.lockFlow(validRun, validKind.name());
		LocalDateTime now = requiredNow();
		if (flow == null || !Objects.equals(flow.getRowVersion(), expectedRowVersion)
				|| !("READY".equals(flow.getFlowState()) || "ERROR".equals(flow.getFlowState())
				|| "RUNNING".equals(flow.getFlowState()) && (flow.getLeaseUntil() == null || !flow.getLeaseUntil().isAfter(now)))) {
			throw new IllegalStateException("历史盘点流不可领取或版本已变化");
		}
		String token = UUID.randomUUID().toString();
		LocalDateTime until = now.plusSeconds(leaseSeconds);
		if (mapper.claimFlow(validRun, validKind.name(), expectedRowVersion, owner, token, now, until) != 1) {
			throw new IllegalStateException("历史盘点流领取CAS失败");
		}
		flow.setLeaseOwner(owner); flow.setLeaseToken(token); flow.setLeaseUntil(until);
		flow.setFlowState("RUNNING"); flow.setRowVersion(expectedRowVersion + 1);
		return lease(flow);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public RawPage readPage(ScanLease lease, ScanPass pass, ScanCursor cursor, int limit) {
		enabled();
		validatePageSize(limit);
		validateLeaseShape(lease);
		if (pass == null || pass != lease.getActivePass()) throw new IllegalArgumentException("读取pass必须等于当前activePass");
		if (!Objects.equals(cursor, lease.getCursor())) throw new IllegalArgumentException("读取cursor不是领取时的稳定游标");
		SmtAuthLegacyScanFlow flow = mapper.lockFlow(lease.getRunId(), lease.getFlowKind().name());
		LocalDateTime now = requiredNow();
		if (!currentLease(flow, lease, cursor, now)) throw new IllegalStateException("历史盘点lease已失效");
		List<RawCandidate> rows = raw(flow, pass, cursor, limit);
		ScanCursor next = nextCursor(pass, cursor, rows);
		String fingerprint = pageFingerprint(lease.getRunId(), lease.getFlowKind(), pass, cursor, rows, now);
		return RawPage.builder().runId(lease.getRunId()).flowKind(lease.getFlowKind()).pass(pass)
				.expectedCursor(cursor).rows(new ArrayList<>(rows)).nextCursor(next)
				.pageFingerprint(fingerprint).sourceReadAt(now).passExhausted(rows.isEmpty()).build();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public CommitResult commitPage(ScanLease lease, ScanCursor expected, RawPage page,
			ScanCursor next, boolean passExhausted) {
		enabled();
		validateLeaseShape(lease);
		if (page == null || page.getRows() == null || page.getRows().size() > MAX_PAGE_SIZE
				|| !Objects.equals(page.getRunId(), lease.getRunId()) || page.getFlowKind() != lease.getFlowKind()
				|| page.getPass() != lease.getActivePass() || !Objects.equals(page.getExpectedCursor(), expected)
				|| !Objects.equals(page.getNextCursor(), next) || page.isPassExhausted() != passExhausted) {
			throw new IllegalArgumentException("提交页与领取的flow/pass/cursor不一致");
		}
		String fingerprint = pageFingerprint(page.getRunId(), page.getFlowKind(), page.getPass(), expected,
				page.getRows(), page.getSourceReadAt());
		if (!Objects.equals(fingerprint, page.getPageFingerprint())) throw new IllegalArgumentException("提交页指纹不一致");
		SmtAuthLegacyScanFlow flow = mapper.lockFlow(lease.getRunId(), lease.getFlowKind().name());
		LocalDateTime now = requiredNow();
		if (!currentLease(flow, lease, expected, now)) return stale(lease);
		if (page.getRows().isEmpty()) {
			if (!passExhausted) throw new IllegalArgumentException("空页必须明确标记passExhausted");
			if (!raw(flow, page.getPass(), expected, 1).isEmpty()) {
				return CommitResult.builder().status(CommitStatus.RETRYABLE_ERROR).rowVersion(lease.getRowVersion())
						.activePass(lease.getActivePass()).cursor(expected).build();
			}
		} else if (passExhausted) {
			throw new IllegalArgumentException("非空页不能完成pass");
		}

		List<CanonicalReview> canonical = page.getRows().stream()
				.map(row -> canonicalizer.canonicalize(page.getFlowKind(), row, page.getSourceReadAt()))
				.sorted(Comparator.comparing(CanonicalReview::getLegacyRef)).collect(Collectors.toList());
		for (CanonicalReview review : canonical) persistReview(flow, review, now);
		int changed;
		ScanPass active = page.getPass();
		if (page.getRows().isEmpty()) {
			active = nextPass(flow, page.getPass());
			changed = mapper.completePass(flow.getRunId(), flow.getFlowKind(), lease.getLeaseToken(),
					lease.getRowVersion(), page.getPass().name(), active.name(), expected, now);
		} else {
			changed = mapper.advanceCursor(flow.getRunId(), flow.getFlowKind(), lease.getLeaseToken(),
					lease.getRowVersion(), page.getPass().name(), expected, next, now);
		}
		if (changed != 1) throw new IllegalStateException("历史盘点review与cursor的同事务CAS失败");
		return CommitResult.builder().status(CommitStatus.COMMITTED).rowVersion(lease.getRowVersion() + 1)
				.activePass(active).cursor(next).build();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public FinishResult finishFlow(ScanLease lease, ScanCursor expected) {
		enabled(); validateLeaseShape(lease);
		SmtAuthLegacyScanFlow flow = mapper.lockFlow(lease.getRunId(), lease.getFlowKind().name());
		LocalDateTime now = requiredNow();
		if (!currentLease(flow, lease, expected, now)) {
			return FinishResult.builder().status(FinishStatus.STALE_LEASE).rowVersion(lease.getRowVersion()).build();
		}
		if (!("Y".equals(flow.getIdPassDone()) && "Y".equals(flow.getUpdatePassDone())
				&& "Y".equals(flow.getRevisitPassDone()))) {
			return FinishResult.builder().status(FinishStatus.NOT_EXHAUSTED).rowVersion(lease.getRowVersion()).build();
		}
		if (mapper.finishFlow(flow.getRunId(), flow.getFlowKind(), lease.getLeaseToken(),
				lease.getRowVersion(), now) != 1) throw new IllegalStateException("完成历史盘点流CAS失败");
		return FinishResult.builder().status(FinishStatus.COMPLETE).rowVersion(lease.getRowVersion() + 1).build();
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	public ReviewPage readReviews(ReviewQuery query, ServerResolvedScope scope, int limit) {
		enabled(); validatePageSize(limit);
		if (query == null || scope == null || scope.getActorUserId() == null || scope.getActorUserId() <= 0
				|| blank(scope.getActorUsername())) throw new AccessDeniedException("缺少服务端认证范围");
		long afterId = query.getAfterId() == null ? 0L : query.getAfterId();
		if (afterId < 0 || query.getReviewState() != null
				&& !new HashSet<>(java.util.Arrays.asList("DISCOVERED", "REVIEW_REQUIRED")).contains(query.getReviewState())) {
			throw new IllegalArgumentException("review查询条件无效");
		}
		List<ReviewRow> rows;
		if (scope.getScopeKind() == ScopeKind.PARK) {
			if (!scope.hasPermission(PARK_REVIEW_PERMISSION)) throw new AccessDeniedException("无园区历史review权限");
			if (scope.getAllowedParkIds() == null || scope.getAllowedParkIds().isEmpty()
					|| scope.getAllowedParkIds().stream().anyMatch(item -> item == null || item <= 0)) {
				throw new AccessDeniedException("缺少明确园区范围");
			}
			List<Integer> parks = scope.getAllowedParkIds().stream().distinct().sorted()
					.collect(Collectors.toList());
			if (query.getParkId() != null && !parks.contains(query.getParkId())) throw new AccessDeniedException("请求园区超出服务端范围");
			rows = mapper.selectParkReviews(parks, query.getParkId(), query.getReviewState(), afterId, limit + 1);
		} else if (scope.getScopeKind() == ScopeKind.GLOBAL_EXCEPTION) {
			if (!scope.hasPermission(GLOBAL_REVIEW_PERMISSION) || query.getParkId() != null) {
				throw new AccessDeniedException("全局异常review范围无效");
			}
			rows = mapper.selectExceptionalReviews(query.getReviewState(), afterId, limit + 1);
		} else {
			throw new AccessDeniedException("未知历史review范围");
		}
		if (rows == null) rows = new ArrayList<>();
		Long nextAfter = rows.size() > limit ? rows.get(limit - 1).getReviewId() : null;
		List<ReviewRow> visible = rows.size() > limit ? new ArrayList<>(rows.subList(0, limit)) : new ArrayList<>(rows);
		return ReviewPage.builder().rows(visible).nextAfterId(nextAfter).build();
	}

	private List<RawCandidate> raw(SmtAuthLegacyScanFlow flow, ScanPass pass, ScanCursor cursor, int limit) {
		List<RawCandidate> rows = mapper.selectRawPage(flow.getFlowKind(), pass.name(), cursor.getIdLastId(),
				flow.getIdHighWater(), cursor.getUpdateLastAt(), cursor.getUpdateLastId(), flow.getUpdateHighWaterAt(),
				cursor.getRevisitLastId(), flow.getRevisitHighWaterId(), DeviceTaskConstants.CARD,
				DeviceAuthTypeEnum.PERSON.getCode(), DeviceTaskConstants.CARD_STAFF_IMPORT,
				DeviceTaskConstants.CARD_APP_PERFECT, DeviceTaskConstants.UPDATE_FACE, limit);
		return rows == null ? new ArrayList<>() : rows;
	}

	private void persistReview(SmtAuthLegacyScanFlow flow, CanonicalReview value, LocalDateTime now) {
		List<SmtAuthLegacyReview> revisions = mapper.lockReviewRevisions(value.getLegacyRef());
		if (revisions == null) revisions = new ArrayList<>();
		for (SmtAuthLegacyReview existing : revisions) {
			if (!Objects.equals(existing.getRevisionFingerprint(), value.getRevisionFingerprint())) continue;
			String stable = canonicalizer.stableEvidence(existing.getEvidencePayload());
			if (!Objects.equals(existing.getRawRowPayload(), value.getRawPayload())
					|| !Objects.equals(stable, value.getStableEvidencePayload())) {
				throw new IllegalStateException("历史revision指纹碰撞或规范编码变化");
			}
			if (mapper.touchReview(existing.getId(), existing.getRowVersion(), now) != 1) {
				throw new IllegalStateException("更新历史revision观察时间失败");
			}
			return;
		}
		int revision = revisions.stream().map(SmtAuthLegacyReview::getRevisionNo)
				.filter(Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
		SmtAuthLegacyReview row = review(flow, value, revision, now);
		if (mapper.insertReview(row) != 1) throw new IllegalStateException("保存历史review revision失败");
	}

	private SmtAuthLegacyReview review(SmtAuthLegacyScanFlow flow, CanonicalReview value,
			int revision, LocalDateTime now) {
		SmtAuthLegacyReview row = new SmtAuthLegacyReview();
		row.setId(IdWorker.getId()); row.setRunFlowId(flow.getId()); row.setFlowKind(flow.getFlowKind());
		row.setRowKind(value.getRowKind()); row.setSourceTable(value.getSourceTable());
		row.setSourceRowId(value.getSourceRowId()); row.setLegacyRef(value.getLegacyRef()); row.setRevisionNo(revision);
		row.setRawColumnSetVersion(value.getRawColumnSetVersion()); row.setRawRowFormat(LegacyInventoryCanonicalizer.RAW_FORMAT);
		row.setRawRowPayload(value.getRawPayload()); row.setRawRowSha256(value.getRawSha256()); row.setRawComplete("Y");
		row.setEvidenceFormat(LegacyInventoryCanonicalizer.EVIDENCE_FORMAT); row.setEvidencePayload(value.getEvidencePayload());
		row.setEvidenceSha256(value.getEvidenceSha256()); row.setRevisionFingerprint(value.getRevisionFingerprint());
		row.setCapturedAt(value.getCapturedAt()); row.setParkId(value.getParkId()); row.setParkState(value.getParkState());
		row.setDeviceCode(value.getDeviceCode()); row.setDeviceType(value.getDeviceType()); row.setAccessType(value.getAccessType());
		row.setServiceType(value.getServiceType()); row.setServiceFamily(value.getServiceFamily()); row.setCardNo(value.getCardNo());
		row.setStaffId(value.getStaffId()); row.setIscPersonId(value.getIscPersonId()); row.setBadge(value.getBadge());
		row.setImageId(value.getImageId()); row.setAction(value.getAction()); row.setStatus(value.getStatus());
		row.setTaskType(value.getTaskType()); row.setCode(value.getCode()); row.setRelatedTaskRef(value.getRelatedTaskRef());
		row.setExternalTaskId(value.getExternalTaskId()); row.setIdentityState(value.getIdentityState());
		row.setResidueKind(value.getResidueKind()); row.setReviewState(value.getReviewState());
		row.setReviewReason(value.getReviewReason()); row.setPhysicalState("UNKNOWN");
		row.setFirstSeenAt(now); row.setLastSeenAt(now); row.setRowVersion(0L); row.setCreatedAt(now); row.setUpdatedAt(now);
		return row;
	}

	private String pageFingerprint(String runId, FlowKind kind, ScanPass pass, ScanCursor cursor,
			List<RawCandidate> rows, LocalDateTime sourceReadAt) {
		StringBuilder value = new StringBuilder(runId).append('|').append(kind).append('|').append(pass)
				.append('|').append(cursor);
		for (RawCandidate row : rows) value.append('|')
				.append(canonicalizer.canonicalize(kind, row, sourceReadAt).getRevisionFingerprint());
		return canonicalizer.sha256(value.toString());
	}

	private ScanCursor nextCursor(ScanPass pass, ScanCursor cursor, List<RawCandidate> rows) {
		if (rows.isEmpty()) return cursor;
		RawCandidate last = rows.get(rows.size() - 1);
		if (pass == ScanPass.ID) return cursor.toBuilder().idLastId(last.getId()).build();
		if (pass == ScanPass.REVISIT) return cursor.toBuilder().revisitLastId(last.getId()).build();
		if (last.getUpdateTime() == null) throw new IllegalStateException("UPDATE pass返回空更新时间");
		return cursor.toBuilder().updateLastAt(last.getUpdateTime()).updateLastId(last.getId()).build();
	}

	private ScanPass nextPass(SmtAuthLegacyScanFlow flow, ScanPass pass) {
		if (pass == ScanPass.ID && !"Y".equals(flow.getUpdatePassDone())) return ScanPass.UPDATE;
		if (pass != ScanPass.REVISIT && !"Y".equals(flow.getRevisitPassDone())) return ScanPass.REVISIT;
		return pass;
	}

	private boolean currentLease(SmtAuthLegacyScanFlow flow, ScanLease lease, ScanCursor cursor, LocalDateTime now) {
		return flow != null && "RUNNING".equals(flow.getFlowState())
				&& Objects.equals(flow.getId(), lease.getFlowId()) && Objects.equals(flow.getLeaseToken(), lease.getLeaseToken())
				&& Objects.equals(flow.getRowVersion(), lease.getRowVersion()) && flow.getLeaseUntil() != null
				&& flow.getLeaseUntil().isAfter(now) && Objects.equals(cursor(flow), cursor)
				&& Objects.equals(flow.getActivePass(), lease.getActivePass().name());
	}

	private ScanRun recoverActive(List<SmtAuthLegacyScanFlow> active, String scopeFingerprint) {
		Set<String> activeRuns = active.stream().map(SmtAuthLegacyScanFlow::getRunId).collect(Collectors.toSet());
		if (activeRuns.size() != 1 || activeRuns.contains(null)) {
			throw new SecurityException("活动历史flow不能确定唯一run");
		}
		String runId = activeRuns.iterator().next();
		List<SmtAuthLegacyScanFlow> completeRun = mapper.lockRunFlows(runId);
		return recover(completeRun == null ? new ArrayList<>() : completeRun, runId, scopeFingerprint);
	}

	private ScanRun recover(List<SmtAuthLegacyScanFlow> completeRun, String runId, String scopeFingerprint) {
		if (completeRun.size() != FlowKind.values().length) throw new IllegalStateException("未完成历史run缺少五个flow");
		Set<String> runs = completeRun.stream().map(SmtAuthLegacyScanFlow::getRunId).collect(Collectors.toSet());
		Set<String> kinds = completeRun.stream().map(SmtAuthLegacyScanFlow::getFlowKind).collect(Collectors.toSet());
		Set<LocalDateTime> cutoffs = completeRun.stream().map(SmtAuthLegacyScanFlow::getCaptureCutoff)
				.collect(Collectors.toSet());
		if (runs.size() != 1 || !runs.contains(runId) || kinds.size() != FlowKind.values().length
				|| !kinds.containsAll(java.util.Arrays.stream(FlowKind.values()).map(Enum::name).collect(Collectors.toSet()))
				|| cutoffs.size() != 1 || cutoffs.contains(null)) {
			throw new IllegalStateException("未完成历史run的flow或cutoff不完整");
		}
		if (completeRun.stream().anyMatch(item -> !Objects.equals(item.getScopeFingerprint(), scopeFingerprint))) {
			throw new SecurityException("未完成历史run与本次服务端scope不一致");
		}
		List<ScanFlowKey> keys = completeRun.stream().sorted(Comparator.comparing(SmtAuthLegacyScanFlow::getFlowKind))
				.map(this::flowKey).collect(Collectors.toList());
		SmtAuthLegacyScanFlow first = completeRun.get(0);
		return ScanRun.builder().runId(first.getRunId()).scopeFingerprint(scopeFingerprint)
				.captureCutoff(first.getCaptureCutoff()).flows(keys).recovered(true).build();
	}

	private InventoryScanRequest scanRequest(InventoryScanRequest request) {
		if (request == null) throw new IllegalArgumentException("盘点请求不能为空");
		String requestedBy = text(request.getRequestedBy(), 128, "requestedBy");
		String fingerprint = text(request.getScopeFingerprint(), 64, "scopeFingerprint");
		if (!fingerprint.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("scopeFingerprint必须是服务端SHA-256");
		String ticket = request.getAuditTicket() == null ? null : text(request.getAuditTicket(), 128, "auditTicket");
		validatePageSize(request.getPageSize());
		return InventoryScanRequest.builder().requestedBy(requestedBy).scopeFingerprint(fingerprint)
				.auditTicket(ticket).pageSize(request.getPageSize()).build();
	}

	private ScanLease lease(SmtAuthLegacyScanFlow flow) {
		return ScanLease.builder().flowId(flow.getId()).runId(flow.getRunId()).flowKind(FlowKind.valueOf(flow.getFlowKind()))
				.leaseOwner(flow.getLeaseOwner()).leaseToken(flow.getLeaseToken()).leaseUntil(flow.getLeaseUntil())
				.rowVersion(flow.getRowVersion()).activePass(ScanPass.valueOf(flow.getActivePass())).cursor(cursor(flow)).build();
	}

	private ScanCursor cursor(SmtAuthLegacyScanFlow flow) {
		return ScanCursor.builder().idLastId(orZero(flow.getIdLastId())).updateLastAt(flow.getUpdateLastAt())
				.updateLastId(orZero(flow.getUpdateLastId())).revisitLastId(orZero(flow.getRevisitLastId())).build();
	}

	private ScanFlowKey flowKey(SmtAuthLegacyScanFlow flow) {
		return ScanFlowKey.builder().flowId(flow.getId()).runId(flow.getRunId())
				.flowKind(FlowKind.valueOf(flow.getFlowKind())).rowVersion(orZero(flow.getRowVersion())).build();
	}

	private CommitResult stale(ScanLease lease) {
		return CommitResult.builder().status(CommitStatus.STALE_LEASE).rowVersion(lease.getRowVersion())
				.activePass(lease.getActivePass()).cursor(lease.getCursor()).build();
	}

	private void validateLeaseShape(ScanLease lease) {
		if (lease == null || lease.getFlowId() == null || lease.getFlowId() <= 0 || blank(lease.getRunId())
				|| lease.getFlowKind() == null || blank(lease.getLeaseToken()) || lease.getRowVersion() < 1
				|| lease.getActivePass() == null || lease.getCursor() == null) {
			throw new IllegalArgumentException("历史盘点lease不完整");
		}
	}

	private void validatePageSize(int limit) {
		if (limit < 1 || limit > MAX_PAGE_SIZE) throw new IllegalArgumentException("page size必须为1至200");
	}

	private LocalDateTime requiredNow() {
		return Objects.requireNonNull(mapper.now(), "数据库时间不能为空");
	}

	private void enabled() {
		if (!enabled) throw new AccessDeniedException("员工历史盘点尚未启用");
	}

	private static long orZero(Long value) { return value == null ? 0L : value; }
	private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
	private static <T> T required(T value, String name) {
		if (value == null) throw new IllegalArgumentException(name + "不能为空");
		return value;
	}
	private static String text(String value, int max, String name) {
		if (blank(value)) throw new IllegalArgumentException(name + "不能为空");
		String normalized = value.trim();
		if (normalized.length() > max) throw new IllegalArgumentException(name + "超过长度上限");
		return normalized;
	}
}
