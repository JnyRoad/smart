package com.tce.smart.platform.core.dto.authversion;

import lombok.Value;
import lombok.Builder;
import lombok.Singular;
import java.time.LocalDateTime;
import java.util.List;

/** 来源、资源协调命令及不可变结果；来源代次与资源代次不能互换。 */
public final class AuthVersion {
    private AuthVersion() { }
    @Value @Builder(toBuilder = true)
    public static class Window { LocalDateTime from; LocalDateTime to; }
    @Value @Builder(toBuilder = true)
    public static class SourceIntent {
        Integer parkId; String sourceKind; String stableKey; String subjectType; String subjectId;
        String sourceRowId; String sourceFingerprint; String intentKey; Long batchId; String action;
        String payloadSnapshot; ExpectedPredecessor expectedPredecessor;
        @Singular List<Window> windows;
    }
    /** 后续撤销的精确前置证明；仅首次reserve比较当前来源，重放仍核完整意图指纹。 */
    @Value @Builder(toBuilder = true)
    public static class ExpectedPredecessor {
        String sourceId; Long generation; String sourceFingerprint; String sourceRowId;
        String intentKey; Long batchId; String action;
    }
    @Value @Builder(toBuilder = true)
    public static class SourceVersion {
        String sourceId; long generation; String sourceRowId; String sourceFingerprint;
        String action; String state; boolean expanded; boolean idempotent;
    }
    @Value @Builder(toBuilder = true)
    public static class ResourceKey {
        Integer parkId; String subjectType; String subjectId; String accessType; String deviceId;
        String resourceType; String resourceId; String serviceType; String credentialChannel;
    }
    @Value @Builder(toBuilder = true)
    public static class ContributionCommand {
        String sourceId; long sourceGeneration; ResourceKey resource; Long requestId;
        String participation; @Singular List<Window> windows;
    }
    @Value @Builder(toBuilder = true)
    public static class SourceBasis {
        String sourceId; long sourceGeneration; String sourceRowId; String sourceFingerprint;
        Long requestId; String action;
    }
    @Value @Builder(toBuilder = true)
    public static class ResourceDecision {
        String resourceId; ResourceKey resource; long generation; long appliedGeneration;
        String action; String desiredFingerprint; String basisFingerprint;
        @Singular List<Window> windows;
        @Singular List<SourceBasis> sources;
        @Singular List<SourceBasis> auditSources; boolean auditTruncated;
        Long blockingTargetId; Long blockingAttemptId; boolean requiresMultipleWindows;
        boolean changed;
    }
    @Value @Builder(toBuilder = true)
    public static class Binding {
        String sourceId; long sourceGeneration; String resourceId; long resourceGeneration;
        Long requestId; Long targetId; Long attemptId;
    }
    @Value @Builder(toBuilder = true)
    public static class BindingResult {
        String outcome; Long reuseTargetId; ResourceDecision current;
    }
    @Value @Builder(toBuilder = true)
    public static class ContributionRecovery {
        String outcome; Binding previousBinding; Binding currentBinding; ResourceDecision current;
    }
    @Value @Builder(toBuilder = true)
    public static class Evidence {
        Binding binding; boolean trusted; String action; String sourceRowId; String sourceFingerprint;
    }
    @Value @Builder(toBuilder = true)
    public static class EvidenceResult {
        String outcome; boolean mayApply; boolean compensationRequired;
        ResourceDecision current;
    }
    @Value @Builder(toBuilder = true)
    public static class AliasCommand {
        ResourceKey resource; long resourceGeneration; String aliasKind; String aliasValue;
    }
    @Value @Builder(toBuilder = true)
    public static class AliasMatch {
        String resourceId; String subjectType; String subjectId; long resourceGeneration;
    }
    @Value @Builder(toBuilder = true)
    public static class AliasResolution {
        String outcome; @Singular List<AliasMatch> matches;
    }
}
