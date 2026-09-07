package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementAttemptRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementBatchFilter;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementBatchRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementTargetFilter;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationManagementTargetRow;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationProgress;
import com.tce.smart.platform.core.mapper.AuthOperationManagementMapper;
import com.tce.smart.platform.core.service.impl.AuthOperationService;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchDetailView;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchPageQuery;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchView;
import com.tce.smart.platform.dto.authoperation.AuthOperationProgressView;
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetPageQuery;
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 权限操作管理端只读查询服务。
 */
@Service
@Transactional(readOnly = true)
public class AuthOperationManagementService {

	private static final int DEFAULT_CURRENT = 1;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	private static final long MAX_OFFSET = Integer.MAX_VALUE;
	private static final String HIDDEN_BATCH_MESSAGE = "权限批次不存在或无访问权限";
	private static final Set<String> TARGET_STATES;

	static {
		Set<String> states = new LinkedHashSet<>();
		Collections.addAll(states, "PREPARING", "QUEUED", "EXECUTING", "WAITING_CONFIRM",
				"VERIFYING", "CONFIRMED", "CONVERGED", "FAILED", "RETAINED");
		TARGET_STATES = Collections.unmodifiableSet(states);
	}

	private final AuthOperationManagementMapper managementMapper;
	private final AuthOperationService operationService;

	public AuthOperationManagementService(AuthOperationManagementMapper managementMapper,
			AuthOperationService operationService) {
		if (managementMapper == null || operationService == null) {
			throw new IllegalArgumentException("查询依赖不能为空");
		}
		this.managementMapper = managementMapper;
		this.operationService = operationService;
	}

	public IPage<AuthOperationBatchView> getBatchPage(AuthOperationBatchPageQuery query,
			List<Integer> allowedParkIds) {
		List<Integer> parks = allowedParks(allowedParkIds);
		AuthOperationBatchPageQuery actual = query == null ? new AuthOperationBatchPageQuery() : query;
		PageSpec pageSpec = pageSpec(actual.getCurrent(), actual.getSize());
		Integer parkId = actual.getParkId();
		if (parkId != null) {
			if (parkId <= 0) {
				throw new IllegalArgumentException("园区ID必须为正数");
			}
			if (!parks.contains(parkId)) {
				throw hiddenBatch();
			}
		}
		AuthOperationManagementBatchFilter filter = AuthOperationManagementBatchFilter.builder()
				.allowedParkIds(parks)
				.parkId(parkId)
				.action(text(actual.getAction()))
				.status(text(actual.getStatus()))
				.sourceType(text(actual.getSourceType()))
				.sourceId(text(actual.getSourceId()))
				.build();
		Page<AuthOperationManagementBatchRow> page = new Page<>(pageSpec.current, pageSpec.size);
		IPage<AuthOperationManagementBatchRow> rows = managementMapper.selectBatchPage(page, filter);
		return mapBatchPage(rows, page);
	}

	public AuthOperationBatchDetailView getBatch(Long batchId, List<Integer> allowedParkIds) {
		List<Integer> parks = allowedParks(allowedParkIds);
		Long id = positive(batchId, "批次ID");
		AuthOperationManagementBatchRow batch = requiredBatch(id, parks);
		AuthOperationProgress progress = operationService.getProgress(id);
		return toBatchDetail(batch, progress);
	}

	public IPage<AuthOperationTargetView> getTargetPage(AuthOperationTargetPageQuery query,
			List<Integer> allowedParkIds) {
		if (query == null) {
			throw new IllegalArgumentException("目标分页请求不能为空");
		}
		Long batchId = positive(query.getBatchId(), "批次ID");
		PageSpec pageSpec = pageSpec(query.getCurrent(), query.getSize());
		List<String> states = targetStates(query.getState());
		List<Integer> parks = allowedParks(allowedParkIds);
		AuthOperationManagementBatchRow batch = requiredBatch(batchId, parks);
		AuthOperationManagementTargetFilter filter = AuthOperationManagementTargetFilter.builder()
				.batchId(batchId)
				.parkId(batch.getParkId())
				.states(states)
				.deviceId(text(query.getDeviceId()))
				.subjectType(text(query.getSubjectType()))
				.build();
		Page<AuthOperationManagementTargetRow> page = new Page<>(pageSpec.current, pageSpec.size);
		IPage<AuthOperationManagementTargetRow> rows = managementMapper.selectTargetPage(page, filter);
		if (rows == null || rows.getRecords() == null || rows.getRecords().isEmpty()) {
			Page<AuthOperationTargetView> empty = copyPage(rows, page);
			empty.setRecords(Collections.emptyList());
			return empty;
		}
		List<Long> targetIds = new ArrayList<>(rows.getRecords().size());
		for (AuthOperationManagementTargetRow row : rows.getRecords()) {
			if (row == null || row.getTargetId() == null) {
				throw new IllegalStateException("目标分页返回了无标识记录");
			}
			targetIds.add(row.getTargetId());
		}
		List<AuthOperationManagementAttemptRow> attempts = managementMapper.selectLatestAttempts(targetIds);
		Map<Long, AuthOperationManagementAttemptRow> attemptByTarget = new LinkedHashMap<>();
		if (attempts != null) {
			for (AuthOperationManagementAttemptRow attempt : attempts) {
				if (attempt != null && attempt.getTargetId() != null
						&& !attemptByTarget.containsKey(attempt.getTargetId())) {
					attemptByTarget.put(attempt.getTargetId(), attempt);
				}
			}
		}
		List<AuthOperationTargetView> views = new ArrayList<>(rows.getRecords().size());
		for (AuthOperationManagementTargetRow row : rows.getRecords()) {
			views.add(toTargetView(row, attemptByTarget.get(row.getTargetId())));
		}
		Page<AuthOperationTargetView> result = copyPage(rows, page);
		result.setRecords(views);
		return result;
	}

	private IPage<AuthOperationBatchView> mapBatchPage(IPage<AuthOperationManagementBatchRow> rows,
			Page<AuthOperationManagementBatchRow> requestedPage) {
		List<AuthOperationBatchView> views = new ArrayList<>();
		if (rows != null && rows.getRecords() != null) {
			for (AuthOperationManagementBatchRow row : rows.getRecords()) {
				if (row != null) {
					views.add(toBatchView(row));
				}
			}
		}
		Page<AuthOperationBatchView> result = copyPage(rows, requestedPage);
		result.setRecords(views);
		return result;
	}

	private AuthOperationManagementBatchRow requiredBatch(Long batchId, List<Integer> parks) {
		AuthOperationManagementBatchRow batch = managementMapper.selectBatch(batchId, parks);
		if (batch == null) {
			throw hiddenBatch();
		}
		return batch;
	}

	private AuthOperationBatchView toBatchView(AuthOperationManagementBatchRow row) {
		return AuthOperationBatchView.builder()
				.batchId(id(row.getBatchId()))
				.parkId(row.getParkId())
				.action(row.getAction())
				.sourceType(row.getSourceType())
				.sourceId(row.getSourceId())
				.status(row.getStatus())
				.failureReason(row.getFailureReason())
				.expectedCount(row.getExpectedCount())
				.expandedCount(row.getExpandedCount())
				.acceptedAt(utc(row.getAcceptedAt()))
				.expansionFinishedAt(utc(row.getExpansionFinishedAt()))
				.updatedAt(utc(row.getUpdatedAt()))
				.build();
	}

	private AuthOperationBatchDetailView toBatchDetail(AuthOperationManagementBatchRow row,
			AuthOperationProgress progress) {
		return AuthOperationBatchDetailView.builder()
				.batchId(id(row.getBatchId()))
				.parkId(row.getParkId())
				.action(row.getAction())
				.sourceType(row.getSourceType())
				.sourceId(row.getSourceId())
				.status(row.getStatus())
				.failureReason(row.getFailureReason())
				.expectedCount(row.getExpectedCount())
				.expandedCount(row.getExpandedCount())
				.acceptedAt(utc(row.getAcceptedAt()))
				.expansionFinishedAt(utc(row.getExpansionFinishedAt()))
				.updatedAt(utc(row.getUpdatedAt()))
				.progress(toProgressView(progress))
				.build();
	}

	private AuthOperationProgressView toProgressView(AuthOperationProgress progress) {
		if (progress == null) {
			throw new IllegalStateException("批次进度不能为空");
		}
		return AuthOperationProgressView.builder()
				.batchId(id(progress.getBatchId()))
				.batchStatus(progress.getBatchStatus())
				.expectedCount(progress.getExpectedCount())
				.expandedCount(progress.getExpandedCount())
				.expansionCursor(id(progress.getExpansionCursor()))
				.totalTargetCount(progress.getTotalTargetCount())
				.preparingCount(progress.getPreparingCount())
				.queuedCount(progress.getQueuedCount())
				.executingCount(progress.getExecutingCount())
				.waitingConfirmCount(progress.getWaitingConfirmCount())
				.verifyingCount(progress.getVerifyingCount())
				.confirmedCount(progress.getConfirmedCount())
				.convergedCount(progress.getConvergedCount())
				.failedCount(progress.getFailedCount())
				.unfinishedCount(progress.getUnfinishedCount())
				.build();
	}

	private AuthOperationTargetView toTargetView(AuthOperationManagementTargetRow row,
			AuthOperationManagementAttemptRow attempt) {
		return AuthOperationTargetView.builder()
				.targetId(id(row.getTargetId()))
				.requestId(id(row.getRequestId()))
				.batchId(id(row.getBatchId()))
				.parkId(row.getParkId())
				.subjectType(row.getSubjectType())
				.subjectId(row.getSubjectId())
				.deviceId(row.getDeviceId())
				.resourceType(row.getResourceType())
				.resourceId(row.getResourceId())
				.action(row.getAction())
				.version(id(row.getVersion()))
				.state(row.getState())
				.failureReason(row.getFailureReason())
				.acceptedAt(utc(row.getAcceptedAt()))
				.dispatchedAt(utc(row.getDispatchedAt()))
				.confirmedAt(utc(row.getConfirmedAt()))
				.convergedAt(utc(row.getConvergedAt()))
				.nextAttemptAt(utc(row.getNextAttemptAt()))
				.latestAttemptNo(attempt == null ? null : attempt.getAttemptNo())
				.latestAttemptStatus(attempt == null ? null : attempt.getStatus())
				.latestExternalBatchId(attempt == null ? null : attempt.getExternalBatchId())
				.latestExternalCommandId(attempt == null ? null : attempt.getExternalCommandId())
				.build();
	}

	private List<Integer> allowedParks(List<Integer> values) {
		if (values == null || values.isEmpty()) {
			throw new SmartException("无可访问园区");
		}
		LinkedHashSet<Integer> parks = new LinkedHashSet<>();
		for (Integer value : values) {
			if (value == null || value <= 0) {
				throw new SmartException("无可访问园区");
			}
			parks.add(value);
		}
		return Collections.unmodifiableList(new ArrayList<>(parks));
	}

	private List<String> targetStates(String value) {
		String normalized = text(value);
		if (normalized == null) {
			return Collections.emptyList();
		}
		String[] parts = normalized.split(",", -1);
		if (parts.length > TARGET_STATES.size()) {
			throw new IllegalArgumentException("目标状态最多允许9项");
		}
		LinkedHashSet<String> states = new LinkedHashSet<>();
		for (String part : parts) {
			String state = text(part);
			if (state == null) {
				throw new IllegalArgumentException("目标状态不能为空");
			}
			state = state.toUpperCase(Locale.ROOT);
			if (!TARGET_STATES.contains(state)) {
				throw new IllegalArgumentException("未知目标状态: " + state);
			}
			states.add(state);
		}
		return Collections.unmodifiableList(new ArrayList<>(states));
	}

	private PageSpec pageSpec(Integer currentValue, Integer sizeValue) {
		int current = currentValue == null ? DEFAULT_CURRENT : currentValue;
		int size = sizeValue == null ? DEFAULT_SIZE : sizeValue;
		if (current <= 0) {
			throw new IllegalArgumentException("current必须为正数");
		}
		if (size <= 0 || size > MAX_SIZE) {
			throw new IllegalArgumentException("size必须在1到100之间");
		}
		long offset = (current - 1L) * size;
		if (offset > MAX_OFFSET) {
			throw new IllegalArgumentException("分页偏移量过大");
		}
		return new PageSpec(current, size);
	}

	private static Long positive(Long value, String field) {
		if (value == null || value <= 0) {
			throw new IllegalArgumentException(field + "必须为正数");
		}
		return value;
	}

	private static String text(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	private static String id(Long value) {
		return value == null ? null : value.toString();
	}

	private static String utc(LocalDateTime value) {
		// 队列表中的 LocalDateTime 按冻结契约保存 UTC，此处显式补回时区标识。
		return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
	}

	private static SmartException hiddenBatch() {
		return new SmartException(HIDDEN_BATCH_MESSAGE);
	}

	private static <T> Page<T> copyPage(IPage<?> source, Page<?> requestedPage) {
		long current = source == null ? requestedPage.getCurrent() : source.getCurrent();
		long size = source == null ? requestedPage.getSize() : source.getSize();
		long total = source == null ? 0 : source.getTotal();
		Page<T> result = new Page<>(current, size);
		result.setTotal(total);
		return result;
	}

	private static final class PageSpec {
		private final int current;
		private final int size;

		private PageSpec(int current, int size) {
			this.current = current;
			this.size = size;
		}
	}
}
