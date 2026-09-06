package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.RawCandidate;
import com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.ReviewRow;
import com.tce.smart.platform.core.entity.SmtAuthLegacyReview;
import com.tce.smart.platform.core.entity.SmtAuthLegacyScanFlow;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** 历史盘点两张新表及五张旧表的有界读取 Mapper。 */
public interface LegacyEmployeeAccessInventoryMapper {
	LocalDateTime now();
	List<SmtAuthLegacyScanFlow> lockActiveFlows();
	List<SmtAuthLegacyScanFlow> lockRunFlows(@Param("runId") String runId);
	SmtAuthLegacyScanFlow lockFlow(@Param("runId") String runId, @Param("flowKind") String flowKind);
	int insertFlow(SmtAuthLegacyScanFlow flow);
	Long selectPhysicalHighWater(@Param("flowKind") String flowKind);
	LocalDateTime selectUpdateHighWater(@Param("flowKind") String flowKind,
			@Param("captureCutoff") LocalDateTime captureCutoff);
	int claimFlow(@Param("runId") String runId, @Param("flowKind") String flowKind,
			@Param("expectedRowVersion") long expectedRowVersion, @Param("leaseOwner") String leaseOwner,
			@Param("leaseToken") String leaseToken, @Param("now") LocalDateTime now,
			@Param("leaseUntil") LocalDateTime leaseUntil);
	List<RawCandidate> selectRawPage(@Param("flowKind") String flowKind, @Param("pass") String pass,
			@Param("idLastId") long idLastId, @Param("idHighWater") long idHighWater,
			@Param("updateLastAt") LocalDateTime updateLastAt, @Param("updateLastId") long updateLastId,
			@Param("updateHighWaterAt") LocalDateTime updateHighWaterAt,
			@Param("revisitLastId") long revisitLastId, @Param("revisitHighWaterId") long revisitHighWaterId,
			@Param("cardDeviceType") int cardDeviceType, @Param("staffAuthorityType") int staffAuthorityType,
			@Param("staffImportService") int staffImportService,
			@Param("cardAppPerfectService") int cardAppPerfectService,
			@Param("updateFaceService") int updateFaceService, @Param("limit") int limit);
	List<SmtAuthLegacyReview> lockReviewRevisions(@Param("legacyRef") String legacyRef);
	int insertReview(SmtAuthLegacyReview review);
	int touchReview(@Param("id") Long id, @Param("expectedRowVersion") long expectedRowVersion,
			@Param("now") LocalDateTime now);
	int advanceCursor(@Param("runId") String runId, @Param("flowKind") String flowKind,
			@Param("leaseToken") String leaseToken, @Param("expectedRowVersion") long expectedRowVersion,
			@Param("pass") String pass, @Param("expected") com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.ScanCursor expected,
			@Param("next") com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.ScanCursor next,
			@Param("now") LocalDateTime now);
	int completePass(@Param("runId") String runId, @Param("flowKind") String flowKind,
			@Param("leaseToken") String leaseToken, @Param("expectedRowVersion") long expectedRowVersion,
			@Param("pass") String pass, @Param("nextPass") String nextPass,
			@Param("expected") com.tce.smart.platform.core.dto.legacyinventory.LegacyInventoryData.ScanCursor expected,
			@Param("now") LocalDateTime now);
	int finishFlow(@Param("runId") String runId, @Param("flowKind") String flowKind,
			@Param("leaseToken") String leaseToken, @Param("expectedRowVersion") long expectedRowVersion,
			@Param("now") LocalDateTime now);
	List<ReviewRow> selectParkReviews(@Param("parkIds") List<Integer> parkIds,
			@Param("requestedParkId") Integer requestedParkId, @Param("reviewState") String reviewState,
			@Param("afterId") long afterId, @Param("limit") int limit);
	List<ReviewRow> selectExceptionalReviews(@Param("reviewState") String reviewState,
			@Param("afterId") long afterId, @Param("limit") int limit);
}
