package com.tce.smart.platform.core.service;

import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.*;

/** 员工历史盘点内部接口；只发现和审计，不发送、不补偿。 */
public interface LegacyEmployeeAccessInventoryService {
	ScanRun openRun(InventoryScanRequest request);
	ScanLease claimFlow(String runId, FlowKind flowKind, String workerId, int leaseSeconds, long expectedRowVersion);
	RawPage readPage(ScanLease lease, ScanPass pass, ScanCursor cursor, int limit);
	CommitResult commitPage(ScanLease lease, ScanCursor expected, RawPage page,
			ScanCursor next, boolean passExhausted);
	FinishResult finishFlow(ScanLease lease, ScanCursor expected);
	ReviewPage readReviews(ReviewQuery query, ServerResolvedScope scope, int limit);
}
