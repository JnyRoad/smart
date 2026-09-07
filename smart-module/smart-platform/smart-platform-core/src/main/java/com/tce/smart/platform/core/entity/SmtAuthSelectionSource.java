package com.tce.smart.platform.core.entity;
import lombok.Data;
import java.util.Date;
/** 冻结来源选择及收敛快照；受理后立即阻止旧任务删除同一主体来源。 */
@Data public class SmtAuthSelectionSource {
 private String sourceKind; private String subjectType; private Integer snapshotVersion; private String businessSnapshot; private String parentKind; private String parentRowId;
 private Long batchId; private Long ordinal; private String operationKey; private Integer parkId;
 private String subjectId; private String authId; private String stableKey; private String sourceRowId;
 private String fingerprint; private String desiredAction; private String imageId; private String personSnapshot; private String badge; private String state; private String verificationReason;
 private Integer oldId; private Date oldCreateTime; private Date oldStartTime; private Date oldEndTime; private Integer oldAuthType;
 private Date newCreateTime; private Date newStartTime; private Date newEndTime; private Integer newAuthType;
 private String sourceCoordId; private Long sourceGeneration;
}
