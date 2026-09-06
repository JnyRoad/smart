package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetPageQuery;
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * 权限操作管理查询测试，覆盖园区隔离、分页边界及只读投影。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class AuthOperationManagementTest {

	private static final Long BATCH_ID = 9007199254740993L;
	private static final Long TARGET_ID = 9007199254740995L;

	private AuthOperationManagementMapper managementMapper;
	private AuthOperationService operationService;
	private AuthOperationManagementService service;

	@Before
	public void setUp() {
		managementMapper = mock(AuthOperationManagementMapper.class);
		operationService = mock(AuthOperationService.class);
		service = new AuthOperationManagementService(managementMapper, operationService);
	}

	@Test
	public void batchPageRejectsEmptyParkScopeBeforeAnyDatabaseRead() {
		assertThatThrownBy(() -> service.getBatchPage(new AuthOperationBatchPageQuery(), Collections.emptyList()))
				.isInstanceOf(SmartException.class)
				.hasMessage("无可访问园区");
		verifyZeroInteractions(managementMapper, operationService);
	}

	@Test
	public void batchPageRejectsForgedParkFilterBeforeAnyDatabaseRead() {
		AuthOperationBatchPageQuery query = new AuthOperationBatchPageQuery();
		query.setParkId(2);

		assertThatThrownBy(() -> service.getBatchPage(query, Collections.singletonList(1)))
				.isInstanceOf(SmartException.class)
				.hasMessage("权限批次不存在或无访问权限");
		verifyZeroInteractions(managementMapper, operationService);
	}

	@Test
	public void batchPageRejectsInvalidPageBounds() {
		AuthOperationBatchPageQuery zeroCurrent = new AuthOperationBatchPageQuery();
		zeroCurrent.setCurrent(0);
		assertThatThrownBy(() -> service.getBatchPage(zeroCurrent, Collections.singletonList(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("current必须为正数");

		AuthOperationBatchPageQuery oversized = new AuthOperationBatchPageQuery();
		oversized.setSize(101);
		assertThatThrownBy(() -> service.getBatchPage(oversized, Collections.singletonList(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("size必须在1到100之间");

		AuthOperationBatchPageQuery excessiveOffset = new AuthOperationBatchPageQuery();
		excessiveOffset.setCurrent(Integer.MAX_VALUE);
		excessiveOffset.setSize(100);
		assertThatThrownBy(() -> service.getBatchPage(excessiveOffset, Collections.singletonList(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("分页偏移量过大");
		verifyZeroInteractions(managementMapper, operationService);
	}

	@Test
	public void batchPageUsesOnePagedQueryWithoutProgressNPlusOne() {
		AuthOperationManagementBatchRow first = batchRow(BATCH_ID, 1);
		first.setFailureReason("缺少设备依据，转人工核验");
		AuthOperationManagementBatchRow second = batchRow(BATCH_ID + 1, 2);
		Page<AuthOperationManagementBatchRow> rows = new Page<>(1, 20);
		rows.setTotal(2);
		rows.setRecords(Arrays.asList(first, second));
		when(managementMapper.selectBatchPage(any(Page.class), any(AuthOperationManagementBatchFilter.class)))
				.thenReturn(rows);

		IPage<AuthOperationBatchView> result = service.getBatchPage(new AuthOperationBatchPageQuery(),
				Arrays.asList(1, 2));

		assertThat(result.getTotal()).isEqualTo(2);
		assertThat(result.getRecords()).extracting(AuthOperationBatchView::getBatchId)
				.containsExactly("9007199254740993", "9007199254740994");
		assertThat(result.getRecords()).extracting(AuthOperationBatchView::getFailureReason)
				.containsExactly("缺少设备依据，转人工核验", null);
		verify(managementMapper).selectBatchPage(any(Page.class), any(AuthOperationManagementBatchFilter.class));
		verify(operationService, never()).getProgress(any(Long.class));
	}

	@Test
	public void batchPageConvertsLargeDatabaseIdAndUtcTimeThroughService() {
		AuthOperationManagementBatchRow row = batchRow(BATCH_ID, 1);
		Page<AuthOperationManagementBatchRow> rows = new Page<>(1, 20);
		rows.setTotal(1);
		rows.setRecords(Collections.singletonList(row));
		when(managementMapper.selectBatchPage(any(Page.class), any(AuthOperationManagementBatchFilter.class)))
				.thenReturn(rows);

		AuthOperationBatchView result = service.getBatchPage(new AuthOperationBatchPageQuery(),
				Collections.singletonList(1)).getRecords().get(0);

		assertThat(result.getBatchId()).isEqualTo("9007199254740993");
		assertThat(result.getAcceptedAt()).isEqualTo("2026-09-05T08:30:00Z");
	}

	@Test
	public void batchDetailHidesMissingAndForeignBatchWithoutReadingProgress() {
		when(managementMapper.selectBatch(eq(BATCH_ID), eq(Collections.singletonList(1)))).thenReturn(null);

		assertThatThrownBy(() -> service.getBatch(BATCH_ID, Collections.singletonList(1)))
				.isInstanceOf(SmartException.class)
				.hasMessage("权限批次不存在或无访问权限");
		verify(operationService, never()).getProgress(any(Long.class));
	}

	@Test
	public void batchDetailReadsProgressOnlyAfterParkScopePasses() {
		AuthOperationManagementBatchRow row = batchRow(BATCH_ID, 1);
		row.setFailureReason("缺少设备依据，转人工核验");
		AuthOperationProgress progress = AuthOperationProgress.builder()
				.batchId(BATCH_ID)
				.batchStatus("EXECUTING")
				.expectedCount(10)
				.expandedCount(8)
				.expansionCursor(8L)
				.totalTargetCount(10)
				.preparingCount(2)
				.queuedCount(1)
				.executingCount(1)
				.waitingConfirmCount(1)
				.verifyingCount(1)
				.confirmedCount(1)
				.convergedCount(2)
				.failedCount(1)
				.unfinishedCount(8)
				.build();
		when(managementMapper.selectBatch(BATCH_ID, Collections.singletonList(1))).thenReturn(row);
		when(operationService.getProgress(BATCH_ID)).thenReturn(progress);

		AuthOperationBatchDetailView result = service.getBatch(BATCH_ID, Collections.singletonList(1));

		assertThat(result.getBatchId()).isEqualTo("9007199254740993");
		assertThat(result.getFailureReason()).isEqualTo("缺少设备依据，转人工核验");
		assertThat(result.getProgress().getUnfinishedCount()).isEqualTo(8);
		verify(operationService).getProgress(BATCH_ID);
	}

	@Test
	public void targetPageRejectsForeignBatchBeforeTargetOrAttemptRead() {
		AuthOperationTargetPageQuery query = new AuthOperationTargetPageQuery();
		query.setBatchId(BATCH_ID);
		when(managementMapper.selectBatch(BATCH_ID, Collections.singletonList(1))).thenReturn(null);

		assertThatThrownBy(() -> service.getTargetPage(query, Collections.singletonList(1)))
				.isInstanceOf(SmartException.class)
				.hasMessage("权限批次不存在或无访问权限");
		verify(managementMapper, never()).selectTargetPage(any(Page.class), any(AuthOperationManagementTargetFilter.class));
		verify(managementMapper, never()).selectLatestAttempts(any());
	}

	@Test
	public void targetPageRejectsUnknownOrUnboundedStateList() {
		AuthOperationTargetPageQuery unknown = new AuthOperationTargetPageQuery();
		unknown.setBatchId(BATCH_ID);
		unknown.setState("FAILED,UNKNOWN");
		assertThatThrownBy(() -> service.getTargetPage(unknown, Collections.singletonList(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("未知目标状态: UNKNOWN");

		AuthOperationTargetPageQuery unbounded = new AuthOperationTargetPageQuery();
		unbounded.setBatchId(BATCH_ID);
		unbounded.setState("PREPARING,QUEUED,EXECUTING,WAITING_CONFIRM,VERIFYING,CONFIRMED,CONVERGED,FAILED,RETAINED,FAILED");
		assertThatThrownBy(() -> service.getTargetPage(unbounded, Collections.singletonList(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("目标状态最多允许9项");
		verifyZeroInteractions(managementMapper, operationService);
	}

	@Test
	public void targetPageLoadsLatestAttemptsForCurrentPageOnly() {
		AuthOperationManagementBatchRow batch = batchRow(BATCH_ID, 1);
		AuthOperationManagementTargetRow first = targetRow(TARGET_ID, "FAILED");
		AuthOperationManagementTargetRow second = targetRow(TARGET_ID + 1, "VERIFYING");
		Page<AuthOperationManagementTargetRow> rows = new Page<>(2, 2);
		rows.setTotal(6);
		rows.setRecords(Arrays.asList(first, second));
		AuthOperationManagementAttemptRow attempt = new AuthOperationManagementAttemptRow();
		attempt.setTargetId(TARGET_ID);
		attempt.setAttemptNo(3);
		attempt.setStatus("FAILED");
		attempt.setExternalBatchId("isc-batch-7");
		attempt.setExternalCommandId("isc-command-9");
		when(managementMapper.selectBatch(BATCH_ID, Collections.singletonList(1))).thenReturn(batch);
		when(managementMapper.selectTargetPage(any(Page.class), any(AuthOperationManagementTargetFilter.class)))
				.thenReturn(rows);
		when(managementMapper.selectLatestAttempts(Arrays.asList(TARGET_ID, TARGET_ID + 1)))
				.thenReturn(Collections.singletonList(attempt));
		AuthOperationTargetPageQuery query = new AuthOperationTargetPageQuery();
		query.setBatchId(BATCH_ID);
		query.setCurrent(2);
		query.setSize(2);
		query.setState("FAILED,VERIFYING");

		IPage<AuthOperationTargetView> result = service.getTargetPage(query, Collections.singletonList(1));

		assertThat(result.getTotal()).isEqualTo(6);
		assertThat(result.getRecords()).hasSize(2);
		assertThat(result.getRecords().get(0).getLatestAttemptNo()).isEqualTo(3);
		assertThat(result.getRecords().get(0).getLatestAttemptStatus()).isEqualTo("FAILED");
		assertThat(result.getRecords().get(1).getLatestAttemptNo()).isNull();
		ArgumentCaptor<AuthOperationManagementTargetFilter> filterCaptor =
				ArgumentCaptor.forClass(AuthOperationManagementTargetFilter.class);
		verify(managementMapper).selectTargetPage(any(Page.class), filterCaptor.capture());
		assertThat(filterCaptor.getValue().getStates()).containsExactly("FAILED", "VERIFYING");
		verify(managementMapper).selectLatestAttempts(Arrays.asList(TARGET_ID, TARGET_ID + 1));
	}

	@Test
	public void managementQueriesAreReadOnlyAndSerializeIdsAndUtcMetadataSafely() throws Exception {
		Transactional transaction = AuthOperationManagementService.class.getAnnotation(Transactional.class);
		assertThat(transaction).isNotNull();
		assertThat(transaction.readOnly()).isTrue();

		AuthOperationTargetView view = AuthOperationTargetView.builder()
				.targetId("9007199254740995")
				.requestId("9007199254740996")
				.batchId("9007199254740993")
				.parkId(1)
				.subjectType("STAFF")
				.subjectId("E001")
				.deviceId("D001")
				.resourceType("DOOR")
				.resourceId("R001")
				.action("DELETE")
				.version("9007199254740997")
				.state("FAILED")
				.acceptedAt("2026-09-05T08:30:00Z")
				.build();
		String json = new ObjectMapper().writeValueAsString(view);

		assertThat(json).contains("\"targetId\":\"9007199254740995\"");
		assertThat(json).contains("\"acceptedAt\":\"2026-09-05T08:30:00Z\"");
		assertThat(json).doesNotContain("selectionSnapshot", "subjectSnapshot", "evidenceBody", "leaseToken");
	}

	private AuthOperationManagementBatchRow batchRow(Long id, Integer parkId) {
		AuthOperationManagementBatchRow row = new AuthOperationManagementBatchRow();
		row.setBatchId(id);
		row.setParkId(parkId);
		row.setAction("DELETE");
		row.setSourceType("STAFF_AUTH");
		row.setSourceId("SRC-1");
		row.setStatus("EXECUTING");
		row.setExpectedCount(10);
		row.setExpandedCount(8);
		row.setAcceptedAt(LocalDateTime.of(2026, 9, 5, 8, 30));
		row.setExpansionFinishedAt(LocalDateTime.of(2026, 9, 5, 8, 31));
		row.setUpdatedAt(LocalDateTime.of(2026, 9, 5, 8, 32));
		return row;
	}

	private AuthOperationManagementTargetRow targetRow(Long id, String state) {
		AuthOperationManagementTargetRow row = new AuthOperationManagementTargetRow();
		row.setTargetId(id);
		row.setRequestId(id + 100);
		row.setBatchId(BATCH_ID);
		row.setParkId(1);
		row.setSubjectType("STAFF");
		row.setSubjectId("E001");
		row.setDeviceId("D001");
		row.setResourceType("DOOR");
		row.setResourceId("R001");
		row.setAction("DELETE");
		row.setVersion(9L);
		row.setState(state);
		row.setAcceptedAt(LocalDateTime.of(2026, 9, 5, 8, 30));
		return row;
	}
}
