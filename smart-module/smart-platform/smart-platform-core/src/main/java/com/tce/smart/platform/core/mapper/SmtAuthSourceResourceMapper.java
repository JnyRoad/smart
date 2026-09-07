package com.tce.smart.platform.core.mapper;

import com.tce.smart.platform.core.entity.SmtAuthSourceResource;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 权限版本协调的有界查询与短事务行锁。 */
public interface SmtAuthSourceResourceMapper {
    SmtAuthSourceResource selectById(@Param("id") String id);
    int insert(SmtAuthSourceResource row);
    int update(SmtAuthSourceResource row);
    List<SmtAuthSourceResource> currentForResource(@Param("resourceId") String resourceId);
    List<SmtAuthSourceResource> currentForSource(@Param("sourceId") String sourceId, @Param("generation") long generation);
    int unstagedCount(@Param("sourceId") String sourceId, @Param("generation") long generation);
    SmtAuthSourceResource historicalIntent(@Param("sourceId") String sourceId, @Param("intentKey") String intentKey);
    List<SmtAuthSourceResource> pendingForSource(@Param("sourceId") String sourceId, @Param("generation") long generation, @Param("after") String after, @Param("limit") int limit);
    int historicalRequestCount(@Param("sourceId") String sourceId, @Param("generation") long generation, @Param("resourceId") String resourceId, @Param("requestId") Long requestId);
    int historicalBindingCount(@Param("sourceId") String sourceId, @Param("generation") long generation, @Param("resourceId") String resourceId);
    int updateRequest(@Param("id") String id, @Param("expectedRequestId") Long expectedRequestId, @Param("requestId") Long requestId);
    int staleEvidenceCount(@Param("resourceId") String resourceId, @Param("attemptId") Long attemptId);
    Long targetGeneration(@Param("targetId") Long targetId);
    Long executionOwner(@Param("resourceId") String resourceId, @Param("generation") long generation);
    SmtAuthSourceResource bindingSnapshot(@Param("binding") com.tce.smart.platform.core.dto.authversion.AuthVersion.Binding binding);
    List<SmtAuthSourceResource> auditForResource(@Param("resourceId") String resourceId, @Param("after") String after, @Param("limit") int limit);
    String windowById(@Param("id") String id);
    int targetWindowCount(@Param("targetId") Long targetId, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
    int ownershipCount(@Param("binding") com.tce.smart.platform.core.dto.authversion.AuthVersion.Binding binding,
        @Param("source") com.tce.smart.platform.core.entity.SmtAuthSourceCoord source,
        @Param("resource") com.tce.smart.platform.core.entity.SmtAuthResourceCoord resource,
        @Param("targetResourceId") String targetResourceId, @Param("batchId") Long batchId,
        @Param("action") String action, @Param("attempt") boolean attempt);
}
