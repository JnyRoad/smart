package com.tce.smart.platform.core.dto.authworkflow;

import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authoperation.*;
import lombok.*;
import java.util.List;

/** 仅由服务器冻结投影构造；sourceKind 是业务表种类，sourceType 是批次原因编码。 */
public final class AuthWorkflow {
    private AuthWorkflow() { }
    /** 员工稳定来源只由 staffId 与 authId 组成，物理行号只属于来源快照。 */
    public static String staffStableKey(String staffId,String authId) {
        if(staffId==null || staffId.trim().isEmpty() || authId==null || authId.trim().isEmpty())
            throw new IllegalArgumentException("staffId与authId不能为空");
        return staffId.length()+":"+staffId+authId.length()+":"+authId;
    }
    @Value @Builder(toBuilder=true)
    public static class Selection {
        Integer parkId; String idempotencyKey; String action; String sourceType; String sourceId;
        String snapshot; Integer expectedCount; @Builder.Default Integer sourceCount=1;
    }
    @Value @Builder(toBuilder=true)
    public static class ResourceInput {
        ResourceKey resource; String participation; @Singular List<Window> windows;
    }
    /** 每个原子分片只含一个主体的一个来源，最多 200 个资源；游标由冻结投影给出。 */
    @Value @Builder(toBuilder=true)
    public static class Shard {
        Long batchId; long previousCursor; long nextCursor; SourceIntent source; String staffAuthId;
        boolean finalSourcePage; @Singular List<ResourceInput> resources;
    }
    @Value @Builder
    public static class Expanded {
        SourceVersion source; Long requestId; AuthOperationExpansionResult expansion;
        @Singular List<Binding> bindings;
    }
    @Value @Builder(toBuilder=true)
    public static class SourceSnapshot {
        String sourceId; long generation; String sourceRowId; String fingerprint;
        String sourceKind; String stableKey; String subjectType; String subjectId; String action;
    }
    /** Handler 必须使用这份快照做本库条件写入；不得调用 HTTP 或按卡号寻找当前来源。 */
    public interface ConvergenceHandler {
        boolean apply(SourceSnapshot exactSnapshot);
    }
    /** 每条当前可信目标证据的本库写回接缝；业务来源最终收敛另行判定。 */
    public interface TargetEvidenceHandler {
        boolean apply(TargetEvidence evidence);
    }
    @Value @Builder
    public static class TargetEvidence {
        Binding binding; Long eventId; String taskId; String action; ResourceDecision current; SourceSnapshot source;
    }
    @Value @Builder
    public static class Prepared {
        String outcome; Binding binding; AuthOperationSubmissionResult submission;
    }
    @Value @Builder
    public static class Received {
        AuthOperationReceiptResult receipt; EvidenceResult evidence; boolean sourceConverged;
        /** 仅表示本条可信物理证据已结算，不替代跨设备来源及请求的收敛条件。 */
        boolean physicalSettled;
    }
    @Value @Builder
    public static class Recovery {
        String outcome; Long compensationBatchId; Binding binding;
    }
}
