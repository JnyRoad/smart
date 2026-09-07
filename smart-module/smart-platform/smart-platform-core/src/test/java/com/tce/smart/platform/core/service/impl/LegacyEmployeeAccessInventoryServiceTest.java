package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.*;
import com.tce.smart.platform.core.entity.SmtAuthLegacyReview;
import com.tce.smart.platform.core.entity.SmtAuthLegacyScanFlow;
import com.tce.smart.platform.core.mapper.LegacyEmployeeAccessInventoryMapper;
import com.tce.smart.platform.core.service.LegacyInventoryCanonicalizer;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 历史盘点的游标、revision、权限范围和旧枚举回归。 */
@RunWith(MockitoJUnitRunner.class)
public class LegacyEmployeeAccessInventoryServiceTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 6, 4, 0);
	private static final String SCOPE = String.join("", Collections.nCopies(64, "a"));
	@Mock private LegacyEmployeeAccessInventoryMapper mapper;
	private LegacyInventoryCanonicalizer canonicalizer;
	private LegacyEmployeeAccessInventoryServiceImpl service;

	@Before
	public void setUp() {
		canonicalizer = new LegacyInventoryCanonicalizer();
		service = new LegacyEmployeeAccessInventoryServiceImpl(mapper, canonicalizer, true);
	}

	@Test
	public void openRunCreatesExactlyFiveIndependentPhysicalHighWatersAndFullRevisits() {
		when(mapper.lockActiveFlows()).thenReturn(Collections.emptyList());
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectPhysicalHighWater(anyString())).thenAnswer(call -> 100L + FlowKind.valueOf(call.getArgument(0)).ordinal());
		when(mapper.selectUpdateHighWater(eq("ISC_TASK"), eq(NOW))).thenReturn(NOW.minusMinutes(2));
		when(mapper.selectUpdateHighWater(eq("DIRECT_TASK"), eq(NOW))).thenReturn(NOW.minusMinutes(1));
		when(mapper.insertFlow(any())).thenReturn(1);

		ScanRun run = service.openRun(InventoryScanRequest.builder().requestedBy("scanner")
				.scopeFingerprint(SCOPE).auditTicket("T022").pageSize(200).build());

		assertThat(run.getFlows()).hasSize(5);
		ArgumentCaptor<SmtAuthLegacyScanFlow> saved = ArgumentCaptor.forClass(SmtAuthLegacyScanFlow.class);
		verify(mapper, times(5)).insertFlow(saved.capture());
		assertThat(saved.getAllValues()).extracting(SmtAuthLegacyScanFlow::getFlowKind)
				.containsExactly("CURRENT_SOURCE", "ISC_TASK", "ISC_DOWN", "DIRECT_TASK", "DIRECT_DOWN");
		assertThat(saved.getAllValues()).allMatch(item -> "Y".equals(item.getRevisitRequired())
				&& "N".equals(item.getIdPassDone()) && item.getIdLastId() == 0L
				&& item.getIdHighWater() >= 100L && item.getRevisitHighWaterId().equals(item.getIdHighWater()));
		assertThat(saved.getAllValues().stream().filter(item -> item.getUpdateHighWaterAt() == null))
				.allMatch(item -> "Y".equals(item.getUpdatePassDone()));
	}

	@Test
	public void concurrentFirstFlowUniqueConflictRecoversTheSingleExistingFiveFlowRun() {
		List<SmtAuthLegacyScanFlow> existing = new ArrayList<>();
		for (FlowKind kind : FlowKind.values()) {
			SmtAuthLegacyScanFlow item = flow(kind, ScanPass.ID, 0L);
			item.setId(100L + kind.ordinal()); item.setRunId("winner-run"); item.setScopeFingerprint(SCOPE);
			item.setCaptureCutoff(NOW.minusMinutes(1)); item.setFlowState("READY"); item.setLeaseToken(null);
			existing.add(item);
		}
		when(mapper.lockActiveFlows()).thenReturn(Collections.emptyList()).thenReturn(existing);
		when(mapper.lockRunFlows("winner-run")).thenReturn(existing);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectPhysicalHighWater("CURRENT_SOURCE")).thenReturn(10L);
		when(mapper.insertFlow(any())).thenThrow(new org.springframework.dao.DuplicateKeyException("active flow"));

		ScanRun recovered = service.openRun(InventoryScanRequest.builder().requestedBy("scanner")
				.scopeFingerprint(SCOPE).pageSize(100).build());

		assertThat(recovered.isRecovered()).isTrue();
		assertThat(recovered.getRunId()).isEqualTo("winner-run");
		assertThat(recovered.getFlows()).hasSize(5);
		verify(mapper, times(2)).lockActiveFlows();
		verify(mapper, times(1)).insertFlow(any());
	}

	@Test
	public void openRunRecoversACompleteFiveFlowRunWhenOneFlowAlreadyCompleted() {
		List<SmtAuthLegacyScanFlow> completeRun = new ArrayList<>();
		for (FlowKind kind : FlowKind.values()) {
			SmtAuthLegacyScanFlow item = flow(kind, ScanPass.ID, 3L);
			item.setId(200L + kind.ordinal()); item.setRunId("partial-run"); item.setScopeFingerprint(SCOPE);
			item.setCaptureCutoff(NOW.minusMinutes(5)); item.setFlowState("READY"); item.setLeaseToken(null);
			completeRun.add(item);
		}
		completeRun.get(0).setFlowState("COMPLETE");
		completeRun.get(0).setCompletedAt(NOW.minusMinutes(1));
		List<SmtAuthLegacyScanFlow> active = new ArrayList<>(completeRun.subList(1, completeRun.size()));
		when(mapper.lockActiveFlows()).thenReturn(active);
		when(mapper.lockRunFlows("partial-run")).thenReturn(completeRun);

		ScanRun recovered = service.openRun(InventoryScanRequest.builder().requestedBy("scanner")
				.scopeFingerprint(SCOPE).pageSize(100).build());

		assertThat(recovered.isRecovered()).isTrue();
		assertThat(recovered.getRunId()).isEqualTo("partial-run");
		assertThat(recovered.getFlows()).hasSize(5);
		assertThat(completeRun.get(0).getFlowState()).isEqualTo("COMPLETE");
		verify(mapper).lockRunFlows("partial-run");
		verify(mapper, never()).insertFlow(any());
	}

	@Test
	public void openRunRejectsARecoveredRunWhoseFiveFlowsDoNotShareOneCutoff() {
		List<SmtAuthLegacyScanFlow> completeRun = new ArrayList<>();
		for (FlowKind kind : FlowKind.values()) {
			SmtAuthLegacyScanFlow item = flow(kind, ScanPass.ID, 3L);
			item.setId(300L + kind.ordinal()); item.setRunId("corrupt-run"); item.setScopeFingerprint(SCOPE);
			item.setCaptureCutoff(NOW.minusMinutes(5)); item.setFlowState("READY"); item.setLeaseToken(null);
			completeRun.add(item);
		}
		completeRun.get(0).setFlowState("COMPLETE");
		completeRun.get(4).setCaptureCutoff(NOW.minusMinutes(4));
		when(mapper.lockActiveFlows()).thenReturn(new ArrayList<>(completeRun.subList(1, completeRun.size())));
		when(mapper.lockRunFlows("corrupt-run")).thenReturn(completeRun);

		assertThatThrownBy(() -> service.openRun(InventoryScanRequest.builder().requestedBy("scanner")
				.scopeFingerprint(SCOPE).pageSize(100).build()))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("cutoff");
		verify(mapper, never()).insertFlow(any());
	}

	@Test
	public void currentSourceBindsRealPersonAuthorityTypeAndNeverVisitorTypeTwo() {
		assertThat(DeviceAuthTypeEnum.PERSON.getCode()).isEqualTo(1);
		assertThat(DeviceAuthTypeEnum.VISITOR.getCode()).isEqualTo(2);
		SmtAuthLegacyScanFlow flow = flow(FlowKind.CURRENT_SOURCE, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		when(mapper.lockFlow("run-1", "CURRENT_SOURCE")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.emptyList());

		service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);

		verify(mapper).selectRawPage(eq("CURRENT_SOURCE"), eq("ID"), eq(0L), eq(100L), isNull(), eq(0L), isNull(),
				eq(0L), eq(100L), eq(DeviceTaskConstants.CARD), eq(DeviceAuthTypeEnum.PERSON.getCode()),
				eq(DeviceTaskConstants.CARD_STAFF_IMPORT), eq(DeviceTaskConstants.CARD_APP_PERFECT),
				eq(DeviceTaskConstants.UPDATE_FACE), eq(200));
		verify(mapper, never()).selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), eq(2), anyInt(), anyInt(), anyInt(), anyInt());
	}

	@Test
	public void claimRotatesAnExpiredFlowTokenUnderExactRowVersion() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.ISC_TASK, ScanPass.ID, 4L);
		flow.setLeaseUntil(NOW.minusSeconds(1));
		when(mapper.lockFlow("run-1", "ISC_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.claimFlow(eq("run-1"), eq("ISC_TASK"), eq(4L), eq("worker-2"),
				anyString(), eq(NOW), eq(NOW.plusSeconds(30)))).thenReturn(1);

		ScanLease claimed = service.claimFlow("run-1", FlowKind.ISC_TASK, "worker-2", 30, 4L);

		assertThat(claimed.getLeaseToken()).isNotEqualTo("lease-token");
		assertThat(claimed.getRowVersion()).isEqualTo(5L);
		assertThat(claimed.getLeaseUntil()).isEqualTo(NOW.plusSeconds(30));
	}

	@Test
	public void unexpiredFlowCannotBeClaimedByAnotherWorker() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.ISC_TASK, ScanPass.ID, 4L);
		when(mapper.lockFlow("run-1", "ISC_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);

		assertThatThrownBy(() -> service.claimFlow("run-1", FlowKind.ISC_TASK, "worker-2", 30, 4L))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("不可领取");
		verify(mapper, never()).claimFlow(anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any());
	}

	@Test
	public void canonicalRawContainsEveryKnownColumnIncludingNullAndKeepsServiceTwoForReview() throws Exception {
		RawCandidate serviceTwo = RawCandidate.builder().id(12L).deviceType(DeviceTaskConstants.CARD)
				.serviceType(DeviceTaskConstants.CARD_APP_PERFECT).cardNo("staff-12").build();
		CanonicalReview review = canonicalizer.canonicalize(FlowKind.ISC_TASK, serviceTwo, NOW);

		com.fasterxml.jackson.databind.JsonNode raw = new com.fasterxml.jackson.databind.ObjectMapper().readTree(review.getRawPayload());
		assertThat(raw.size()).isEqualTo(23);
		assertThat(raw.has("ID")).isTrue();
		assertThat(raw.get("REMARK").isNull()).isTrue();
		assertThat(raw.get("PERSON_ID").isNull()).isTrue();
		assertThat(review.getServiceFamily()).isEqualTo("APP_PERFECT_REVIEW");
		assertThat(review.getReviewState()).isEqualTo("REVIEW_REQUIRED");
		assertThat(review.getPhysicalState()).isEqualTo("UNKNOWN");
	}

	@Test
	public void everyFlowCanonicalIncludesItsEntireVersionedKnownColumnSet() throws Exception {
		Map<FlowKind, Integer> expected = new LinkedHashMap<>();
		expected.put(FlowKind.CURRENT_SOURCE, 7);
		expected.put(FlowKind.ISC_TASK, 23);
		expected.put(FlowKind.ISC_DOWN, 18);
		expected.put(FlowKind.DIRECT_TASK, 19);
		expected.put(FlowKind.DIRECT_DOWN, 15);
		com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();
		for (Map.Entry<FlowKind, Integer> item : expected.entrySet()) {
			CanonicalReview review = canonicalizer.canonicalize(item.getKey(), RawCandidate.builder().id(1L).build(), NOW);
			assertThat(json.readTree(review.getRawPayload()).size()).as(item.getKey().name()).isEqualTo(item.getValue());
			assertThat(review.getRawColumnSetVersion()).isEqualTo(item.getKey().rawColumnSetVersion());
			assertThat(review.getRawSha256()).hasSize(64);
		}
	}

	@Test
	public void missingIdentityAndParkRemainVisibleReviewWithPhysicalUnknownContract() {
		RawCandidate candidate = RawCandidate.builder().id(17L).deviceType(1).serviceType(7)
				.deviceCode("deleted-device").staffCandidateCount(0).deviceParkCount(0).build();
		CanonicalReview review = canonicalizer.canonicalize(FlowKind.DIRECT_TASK, candidate, NOW);

		assertThat(review.getParkState()).isEqualTo("UNKNOWN");
		assertThat(review.getIdentityState()).isEqualTo("UNKNOWN");
		assertThat(review.getResidueKind()).isEqualTo("TASK_ONLY");
		assertThat(review.getReviewReason()).contains("PARK_UNKNOWN", "IDENTITY_UNKNOWN", "TASK_ONLY");
	}

	@Test
	public void multipleDeviceParksAlwaysConflictEvenWhenPersistentParkEqualsMinimumCandidate() {
		RawCandidate current = RawCandidate.builder().id(18L).evidenceParkId(17)
				.deviceParkMin(17).deviceParkCount(2).staffCandidateId(71L).staffCandidateCount(1).build();
		RawCandidate iscDown = RawCandidate.builder().id(19L).parkId(17)
				.deviceParkMin(17).deviceParkCount(2).staffCandidateId(71L).staffCandidateCount(1).build();
		RawCandidate directDown = RawCandidate.builder().id(20L).parkId(17)
				.deviceParkMin(17).deviceParkCount(2).staffCandidateId(71L).staffCandidateCount(1).build();

		CanonicalReview currentReview = canonicalizer.canonicalize(FlowKind.CURRENT_SOURCE, current, NOW);
		CanonicalReview iscReview = canonicalizer.canonicalize(FlowKind.ISC_DOWN, iscDown, NOW);
		CanonicalReview directReview = canonicalizer.canonicalize(FlowKind.DIRECT_DOWN, directDown, NOW);

		assertThat(Arrays.asList(currentReview, iscReview, directReview))
				.extracting(CanonicalReview::getParkState).containsOnly("CONFLICT");
		assertThat(Arrays.asList(currentReview, iscReview, directReview))
				.extracting(CanonicalReview::getParkId).containsOnlyNulls();
	}

	@Test
	public void stableEvidenceIgnoresCollectionTimeButFullEvidenceHashChanges() {
		RawCandidate candidate = knownCandidate(21L);
		CanonicalReview first = canonicalizer.canonicalize(FlowKind.DIRECT_TASK, candidate, NOW);
		CanonicalReview later = canonicalizer.canonicalize(FlowKind.DIRECT_TASK, candidate, NOW.plusMinutes(5));

		assertThat(first.getEvidenceSha256()).isNotEqualTo(later.getEvidenceSha256());
		assertThat(first.getStableEvidencePayload()).isEqualTo(later.getStableEvidencePayload());
		assertThat(first.getRevisionFingerprint()).isEqualTo(later.getRevisionFingerprint());
	}

	@Test
	public void emptyIdPassNeedsARealSecondEmptyReadBeforeCompletionCas() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_DOWN, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		when(mapper.lockFlow("run-1", "DIRECT_DOWN")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.emptyList());
		when(mapper.completePass(anyString(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any()))
				.thenReturn(1);

		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		CommitResult result = service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), true);

		assertThat(result.getStatus()).isEqualTo(CommitStatus.COMMITTED);
		assertThat(result.getActivePass()).isEqualTo(ScanPass.REVISIT);
		verify(mapper, times(2)).selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
	}

	@Test
	public void emptyPageCannotAdvancePastThePersistedCursor() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_DOWN, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		when(mapper.lockFlow("run-1", "DIRECT_DOWN")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.emptyList());

		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		ScanCursor different = ScanCursor.builder().idLastId(1L).updateLastId(0L).revisitLastId(0L).build();

		assertThatThrownBy(() -> service.commitPage(lease, lease.getCursor(), page, different, true))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("空页的next cursor必须等于expected cursor");
		verify(mapper, never()).completePass(anyString(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any());
	}

	@Test
	public void fabricatedEmptyPageCannotMarkPassDoneWhenTransactionalRecheckFindsRow() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_DOWN, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		when(mapper.lockFlow("run-1", "DIRECT_DOWN")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.emptyList()).thenReturn(Collections.singletonList(knownCandidate(1L)));

		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		CommitResult result = service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), true);

		assertThat(result.getStatus()).isEqualTo(CommitStatus.RETRYABLE_ERROR);
		verify(mapper, never()).completePass(anyString(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any());
		verify(mapper, never()).insertReview(any());
	}

	@Test
	public void sameStableEvidenceOnlyTouchesRevisionWhileChangedRawCreatesNextRevision() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_TASK, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		RawCandidate candidate = knownCandidate(31L);
		when(mapper.lockFlow("run-1", "DIRECT_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.singletonList(candidate));
		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		CanonicalReview canonical = canonicalizer.canonicalize(FlowKind.DIRECT_TASK, candidate, NOW.minusDays(1));
		SmtAuthLegacyReview existing = existing(canonical, 1);
		when(mapper.lockReviewRevisions("DIRECT_TASK:31")).thenReturn(Collections.singletonList(existing));
		when(mapper.touchReview(existing.getId(), existing.getRowVersion(), NOW)).thenReturn(1);
		when(mapper.advanceCursor(anyString(), anyString(), anyString(), anyLong(), anyString(), any(), any(), any())).thenReturn(1);

		CommitResult same = service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);

		assertThat(same.getStatus()).isEqualTo(CommitStatus.COMMITTED);
		verify(mapper).touchReview(existing.getId(), 0L, NOW);
		verify(mapper, never()).insertReview(any());
	}

	@Test
	public void newRevisionUsesTheStableSourceReadTimeAndPhysicalStateUnknown() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_TASK, ScanPass.ID, 1L);
		flow.setLeaseUntil(NOW.plusMinutes(5));
		ScanLease lease = lease(flow);
		RawCandidate candidate = knownCandidate(41L);
		when(mapper.lockFlow("run-1", "DIRECT_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW, NOW.plusMinutes(1));
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.singletonList(candidate));
		when(mapper.lockReviewRevisions("DIRECT_TASK:41")).thenReturn(Collections.emptyList());
		when(mapper.insertReview(any())).thenReturn(1);
		when(mapper.advanceCursor(anyString(), anyString(), anyString(), anyLong(), anyString(), any(), any(), any())).thenReturn(1);

		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);

		ArgumentCaptor<SmtAuthLegacyReview> saved = ArgumentCaptor.forClass(SmtAuthLegacyReview.class);
		verify(mapper).insertReview(saved.capture());
		assertThat(saved.getValue().getCapturedAt()).isEqualTo(NOW);
		assertThat(saved.getValue().getPhysicalState()).isEqualTo("UNKNOWN");
	}

	@Test
	public void changedRawCreatesNextRevisionWithoutOverwritingTheOldPayload() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_TASK, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		RawCandidate changed = knownCandidate(51L); changed.setRemark("new-raw-value");
		CanonicalReview oldCanonical = canonicalizer.canonicalize(FlowKind.DIRECT_TASK, knownCandidate(51L), NOW.minusDays(1));
		SmtAuthLegacyReview old = existing(oldCanonical, 1);
		when(mapper.lockFlow("run-1", "DIRECT_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.singletonList(changed));
		when(mapper.lockReviewRevisions("DIRECT_TASK:51")).thenReturn(Collections.singletonList(old));
		when(mapper.insertReview(any())).thenReturn(1);
		when(mapper.advanceCursor(anyString(), anyString(), anyString(), anyLong(), anyString(), any(), any(), any())).thenReturn(1);

		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false);

		ArgumentCaptor<SmtAuthLegacyReview> saved = ArgumentCaptor.forClass(SmtAuthLegacyReview.class);
		verify(mapper).insertReview(saved.capture());
		assertThat(saved.getValue().getRevisionNo()).isEqualTo(2);
		assertThat(saved.getValue().getRawRowPayload()).contains("new-raw-value");
		assertThat(old.getRawRowPayload()).doesNotContain("new-raw-value");
		verify(mapper, never()).touchReview(anyLong(), anyLong(), any());
	}

	@Test
	public void equalFingerprintWithDifferentCanonicalContentStopsTheWholePage() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_TASK, ScanPass.ID, 1L);
		ScanLease lease = lease(flow);
		RawCandidate candidate = knownCandidate(61L);
		CanonicalReview canonical = canonicalizer.canonicalize(FlowKind.DIRECT_TASK, candidate, NOW);
		SmtAuthLegacyReview corrupt = existing(canonical, 1); corrupt.setRawRowPayload("{}");
		when(mapper.lockFlow("run-1", "DIRECT_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);
		when(mapper.selectRawPage(anyString(), anyString(), anyLong(), anyLong(), nullable(LocalDateTime.class),
				anyLong(), nullable(LocalDateTime.class), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt()))
				.thenReturn(Collections.singletonList(candidate));
		when(mapper.lockReviewRevisions("DIRECT_TASK:61")).thenReturn(Collections.singletonList(corrupt));

		RawPage page = service.readPage(lease, ScanPass.ID, lease.getCursor(), 200);
		assertThatThrownBy(() -> service.commitPage(lease, lease.getCursor(), page, page.getNextCursor(), false))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("指纹碰撞");
		verify(mapper, never()).advanceCursor(anyString(), anyString(), anyString(), anyLong(), anyString(), any(), any(), any());
		verify(mapper, never()).touchReview(anyLong(), anyLong(), any());
	}

	@Test
	public void staleLeaseNeverWritesReviewOrCursor() {
		SmtAuthLegacyScanFlow stale = flow(FlowKind.ISC_DOWN, ScanPass.ID, 2L);
		ScanLease old = lease(flow(FlowKind.ISC_DOWN, ScanPass.ID, 1L));
		RawPage page = RawPage.builder().runId("run-1").flowKind(FlowKind.ISC_DOWN).pass(ScanPass.ID)
				.expectedCursor(old.getCursor()).rows(Collections.emptyList()).nextCursor(old.getCursor())
				.sourceReadAt(NOW).passExhausted(true).pageFingerprint(canonicalizer.sha256(
						"run-1|ISC_DOWN|ID|" + old.getCursor())).build();
		when(mapper.now()).thenReturn(NOW);
		when(mapper.lockFlow("run-1", "ISC_DOWN")).thenReturn(stale);

		CommitResult result = service.commitPage(old, old.getCursor(), page, old.getCursor(), true);

		assertThat(result.getStatus()).isEqualTo(CommitStatus.STALE_LEASE);
		verify(mapper, never()).insertReview(any());
		verify(mapper, never()).completePass(anyString(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any());
	}

	@Test
	public void commitRejectsLeaseThatExpiresWhileWaitingForTheFlowLock() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.ISC_DOWN, ScanPass.ID, 1L);
		flow.setLeaseUntil(NOW.plusMinutes(1));
		ScanLease lease = lease(flow);
		RawPage page = RawPage.builder().runId("run-1").flowKind(FlowKind.ISC_DOWN).pass(ScanPass.ID)
				.expectedCursor(lease.getCursor()).rows(Collections.emptyList()).nextCursor(lease.getCursor())
				.sourceReadAt(NOW).passExhausted(true).pageFingerprint(canonicalizer.sha256(
						"run-1|ISC_DOWN|ID|" + lease.getCursor())).build();
		AtomicBoolean lockObtained = new AtomicBoolean(false);
		when(mapper.now()).thenAnswer(call -> lockObtained.get() ? NOW.plusMinutes(2) : NOW);
		when(mapper.lockFlow("run-1", "ISC_DOWN")).thenAnswer(call -> {
			lockObtained.set(true);
			return flow;
		});

		CommitResult result = service.commitPage(lease, lease.getCursor(), page, lease.getCursor(), true);

		assertThat(result.getStatus()).isEqualTo(CommitStatus.STALE_LEASE);
		verify(mapper, never()).completePass(anyString(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any());
		verify(mapper, never()).insertReview(any());
	}

	@Test
	public void commitAndFinishCasUseDatabaseClockAtExecutionAfterTransactionalWork() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.addMapper(LegacyEmployeeAccessInventoryMapper.class);
		try (InputStream input = getClass().getClassLoader()
				.getResourceAsStream("mapper/LegacyEmployeeAccessInventoryMapper.xml")) {
			assertThat(input).isNotNull();
			new XMLMapperBuilder(input, configuration, "mapper/LegacyEmployeeAccessInventoryMapper.xml",
					configuration.getSqlFragments()).parse();
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("pass", "ID");
		parameters.put("expected", ScanCursor.builder().idLastId(0L).updateLastId(0L).revisitLastId(0L).build());
		parameters.put("next", ScanCursor.builder().idLastId(1L).updateLastId(0L).revisitLastId(0L).build());
		String namespace = LegacyEmployeeAccessInventoryMapper.class.getName() + ".";
		String advance = configuration.getMappedStatement(namespace + "advanceCursor")
				.getBoundSql(parameters).getSql().replaceAll("\\s+", " ");
		String complete = configuration.getMappedStatement(namespace + "completePass")
				.getBoundSql(parameters).getSql().replaceAll("\\s+", " ");
		String finish = configuration.getMappedStatement(namespace + "finishFlow")
				.getBoundSql(parameters).getSql().replaceAll("\\s+", " ");

		assertThat(advance).contains("LEASE_UNTIL > SYSTIMESTAMP");
		assertThat(complete).contains("LEASE_UNTIL > SYSTIMESTAMP");
		assertThat(finish).contains("LEASE_UNTIL > SYSTIMESTAMP");
	}

	@Test
	public void finishRequiresAllEnabledPassesToBeDone() {
		SmtAuthLegacyScanFlow flow = flow(FlowKind.DIRECT_TASK, ScanPass.REVISIT, 7L);
		flow.setIdPassDone("Y"); flow.setUpdatePassDone("Y"); flow.setRevisitPassDone("N");
		ScanLease lease = lease(flow);
		when(mapper.lockFlow("run-1", "DIRECT_TASK")).thenReturn(flow);
		when(mapper.now()).thenReturn(NOW);

		FinishResult result = service.finishFlow(lease, lease.getCursor());

		assertThat(result.getStatus()).isEqualTo(FinishStatus.NOT_EXHAUSTED);
		verify(mapper, never()).finishFlow(anyString(), anyString(), anyString(), anyLong(), any());
	}

	@Test
	public void reviewScopesUseExactExistingPermissionsAndNeverExposeClobs() {
		ReviewQuery query = ReviewQuery.builder().afterId(0L).build();
		ServerResolvedScope park = ServerResolvedScope.park(7, "park-reviewer", Arrays.asList(2, 1, 2),
				Collections.singleton(LegacyEmployeeAccessInventoryServiceImpl.PARK_REVIEW_PERMISSION));
		when(mapper.selectParkReviews(Arrays.asList(1, 2), null, null, 0L, 201)).thenReturn(Collections.emptyList());

		service.readReviews(query, park, 200);

		verify(mapper).selectParkReviews(Arrays.asList(1, 2), null, null, 0L, 201);
		verify(mapper, never()).selectExceptionalReviews(any(), anyLong(), anyInt());

		ServerResolvedScope global = ServerResolvedScope.globalException(8, "global-reviewer",
				Collections.singleton(LegacyEmployeeAccessInventoryServiceImpl.GLOBAL_REVIEW_PERMISSION));
		when(mapper.selectExceptionalReviews(null, 0L, 21)).thenReturn(Collections.emptyList());
		service.readReviews(query, global, 20);
		verify(mapper).selectExceptionalReviews(null, 0L, 21);
	}

	@Test
	public void globalPermissionDoesNotOpenKnownParkOrRequestedParkFilter() {
		ServerResolvedScope global = ServerResolvedScope.globalException(8, "global-reviewer",
				Collections.singleton(LegacyEmployeeAccessInventoryServiceImpl.GLOBAL_REVIEW_PERMISSION));
		assertThatThrownBy(() -> service.readReviews(ReviewQuery.builder().parkId(1).build(), global, 20))
				.isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
		verifyZeroInteractions(mapper);
	}

	@Test
	public void missingExactReviewPermissionDefaultsToDenied() {
		ServerResolvedScope park = ServerResolvedScope.park(7, "actor", Collections.singletonList(1), Collections.emptySet());
		assertThatThrownBy(() -> service.readReviews(ReviewQuery.builder().build(), park, 20))
				.isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
		verifyZeroInteractions(mapper);
	}

	@Test
	public void malformedServerParkScopeWithNullEntryDefaultsToDenied() {
		ServerResolvedScope malformed = ServerResolvedScope.park(7, "actor", Arrays.asList(1, null),
				Collections.singleton(LegacyEmployeeAccessInventoryServiceImpl.PARK_REVIEW_PERMISSION));
		assertThatThrownBy(() -> service.readReviews(ReviewQuery.builder().build(), malformed, 20))
				.isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
		verifyZeroInteractions(mapper);
	}

	@Test
	public void limitOverTwoHundredFailsBeforeAnyReadOrWrite() {
		assertThatThrownBy(() -> service.readReviews(ReviewQuery.builder().build(),
				ServerResolvedScope.park(7, "actor", Collections.singletonList(1),
						Collections.singleton(LegacyEmployeeAccessInventoryServiceImpl.PARK_REVIEW_PERMISSION)), 201))
				.isInstanceOf(IllegalArgumentException.class);
		verifyZeroInteractions(mapper);
	}

	@Test
	public void mapperXmlParsesEveryInventoryStatementWithoutDatabase() throws Exception {
		Configuration configuration = new Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.addMapper(LegacyEmployeeAccessInventoryMapper.class);
		try (InputStream input = getClass().getClassLoader()
				.getResourceAsStream("mapper/LegacyEmployeeAccessInventoryMapper.xml")) {
			assertThat(input).isNotNull();
			new XMLMapperBuilder(input, configuration, "mapper/LegacyEmployeeAccessInventoryMapper.xml",
					configuration.getSqlFragments()).parse();
		}
		assertThat(configuration.hasStatement(LegacyEmployeeAccessInventoryMapper.class.getName() + ".selectRawPage"))
				.isTrue();
		assertThat(configuration.hasStatement(LegacyEmployeeAccessInventoryMapper.class.getName() + ".lockRunFlows"))
				.isTrue();
		assertThat(configuration.hasStatement(LegacyEmployeeAccessInventoryMapper.class.getName() + ".completePass"))
				.isTrue();
		assertThat(configuration.hasStatement(LegacyEmployeeAccessInventoryMapper.class.getName() + ".selectExceptionalReviews"))
				.isTrue();
	}

	private SmtAuthLegacyScanFlow flow(FlowKind kind, ScanPass pass, long version) {
		SmtAuthLegacyScanFlow flow = new SmtAuthLegacyScanFlow();
		flow.setId(91L); flow.setRunId("run-1"); flow.setFlowKind(kind.name()); flow.setFlowState("RUNNING");
		flow.setActivePass(pass.name()); flow.setIdHighWater(100L); flow.setIdLastId(0L);
		flow.setUpdateHighWaterAt(kind.hasUpdatePass() ? NOW.minusMinutes(1) : null);
		flow.setUpdateLastAt(null); flow.setUpdateLastId(0L); flow.setRevisitHighWaterId(100L); flow.setRevisitLastId(0L);
		flow.setIdPassDone("N"); flow.setUpdatePassDone(kind.hasUpdatePass() ? "N" : "Y"); flow.setRevisitPassDone("N");
		flow.setRevisitRequired("Y"); flow.setLeaseOwner("worker"); flow.setLeaseToken("lease-token");
		flow.setLeaseUntil(NOW.plusMinutes(1)); flow.setRowVersion(version);
		return flow;
	}

	private ScanLease lease(SmtAuthLegacyScanFlow flow) {
		return ScanLease.builder().flowId(flow.getId()).runId(flow.getRunId()).flowKind(FlowKind.valueOf(flow.getFlowKind()))
				.leaseOwner(flow.getLeaseOwner()).leaseToken(flow.getLeaseToken()).leaseUntil(flow.getLeaseUntil())
				.rowVersion(flow.getRowVersion()).activePass(ScanPass.valueOf(flow.getActivePass()))
				.cursor(ScanCursor.builder().idLastId(flow.getIdLastId()).updateLastAt(flow.getUpdateLastAt())
						.updateLastId(flow.getUpdateLastId()).revisitLastId(flow.getRevisitLastId()).build()).build();
	}

	private RawCandidate knownCandidate(long id) {
		return RawCandidate.builder().id(id).deviceType(1).serviceType(7).deviceCode("device-1")
				.cardNo("71").deviceParkMin(1).deviceParkCount(1).staffCandidateId(71L).staffCandidateCount(1)
				.createTime(NOW.minusDays(2)).updateTime(NOW.minusMinutes(2)).build();
	}

	private SmtAuthLegacyReview existing(CanonicalReview canonical, int revision) {
		SmtAuthLegacyReview row = new SmtAuthLegacyReview();
		row.setId(701L); row.setLegacyRef(canonical.getLegacyRef()); row.setRevisionNo(revision);
		row.setRevisionFingerprint(canonical.getRevisionFingerprint()); row.setRawRowPayload(canonical.getRawPayload());
		row.setEvidencePayload(canonical.getEvidencePayload()); row.setRowVersion(0L);
		return row;
	}
}
