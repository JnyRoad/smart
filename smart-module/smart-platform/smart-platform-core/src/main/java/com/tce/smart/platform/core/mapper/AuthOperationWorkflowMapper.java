package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.*;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/** 工作流的查询和条件收敛；不承担批次展开、租约或设备下发。 */
public interface AuthOperationWorkflowMapper {
    String shardFingerprint(@Param("batchId") Long batchId,@Param("previous") long previous);
    int insertShard(@Param("batchId") Long batchId,@Param("previous") long previous,@Param("next") long next,@Param("kind") String kind,
        @Param("fingerprint") String fingerprint,@Param("sourceId") String sourceId,@Param("generation") Long generation,@Param("now") LocalDateTime now);
    SmtAuthSourceResource contributionByKey(@Param("sourceId") String sourceId,@Param("generation") long generation,
        @Param("resource") com.tce.smart.platform.core.dto.authversion.AuthVersion.ResourceKey resource);
    int requestCount(@Param("batchId") Long batchId);
    int unsealedCount(@Param("batchId") Long batchId);
    List<SmtAuthSourceResource> laneContributions(@Param("batchId") Long batchId,@Param("resourceId") String resourceId);
    SmtAuthOperationTarget laneTarget(@Param("batchId") Long batchId,@Param("resourceId") String resourceId);
    List<SmtAuthSourceResource> targetContributions(@Param("targetId") Long targetId);
    SmtAuthDeleteRequest request(@Param("batchId") Long batchId,@Param("sourceId") String sourceId,@Param("generation") long generation);
    SmtAuthSourceCoord source(@Param("sourceId") String sourceId);
    SmtAuthSourceResource contribution(@Param("sourceId") String sourceId,@Param("generation") long generation,@Param("resourceId") String resourceId);
    SmtAuthSourceResource exactBinding(@Param("sourceId") String sourceId,@Param("generation") long generation,@Param("resourceId") String resourceId,@Param("targetId") Long targetId,@Param("requestId") Long requestId);
    String sourceResource(@Param("sourceId") String sourceId,@Param("generation") long generation);
    int sourcePendingRecords(@Param("sourceId") String sourceId,@Param("generation") long generation);
    int sourcePendingRequests(@Param("sourceId") String sourceId,@Param("generation") long generation);
    int convergeRequests(@Param("sourceId") String sourceId,@Param("generation") long generation,@Param("now") LocalDateTime now);
    int pendingTargetRequests(@Param("targetId") Long targetId);
    int confirmReused(@Param("targetId") Long targetId,@Param("version") long version,@Param("now") LocalDateTime now);
    Long currentTrustedEvent(@Param("resourceId") String resourceId,@Param("generation") long generation);
    int settleRetained(@Param("targetId") Long targetId,@Param("summary") String summary,@Param("now") LocalDateTime now);
    int convergeTarget(@Param("targetId") Long targetId,@Param("now") LocalDateTime now);
    int refreshBatch(@Param("batchId") Long batchId,@Param("now") LocalDateTime now);
    Long completedRecordEvent(@Param("binding") com.tce.smart.platform.core.dto.authversion.AuthVersion.Binding binding,@Param("eventId") Long eventId);
    int inheritRecordEvent(@Param("eventId") Long eventId,@Param("proofId") Long proofId,
        @Param("binding") com.tce.smart.platform.core.dto.authversion.AuthVersion.Binding binding);
    int eventRecordConverged(@Param("eventId") Long eventId);
    int finishEvent(@Param("eventId") Long eventId);
    List<Long> sourceTargets(@Param("sourceId") String sourceId,@Param("generation") long generation,@Param("after") Long after,@Param("limit") int limit);
    int currentIntentCount(@Param("resourceId") String resourceId,@Param("generation") long generation);
    List<SmtAuthSourceResource> pendingRecovery(@Param("park") int park,@Param("after") String after,@Param("limit") int limit);
}
