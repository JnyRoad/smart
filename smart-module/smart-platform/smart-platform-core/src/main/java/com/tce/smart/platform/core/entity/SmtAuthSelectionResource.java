package com.tce.smart.platform.core.entity;
import lombok.Data;
import java.time.LocalDateTime;
/** 每资源一行冻结选择；不在批次 CLOB 存放人员照片。 */
@Data public class SmtAuthSelectionResource {
 private String subjectType; private Integer credentialVersion; private String credentialSnapshot;
 private Long batchId; private Long ordinal; private Long sourceOrdinal; private Integer parkId;
 private String subjectId; private String deviceId; private String accessType; private String resourceType;
 private String resourceId; private String serviceType; private String credentialChannel; private String participation;
 private LocalDateTime validFrom; private LocalDateTime validTo; private String resourceCoordId;
}
