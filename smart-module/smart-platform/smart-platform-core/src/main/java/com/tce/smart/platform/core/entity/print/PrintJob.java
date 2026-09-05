package com.tce.smart.platform.core.entity.print;
import lombok.Data;
/** 打印领域持久记录；物理占用与冻结快照独立保存。 */
@Data
public class PrintJob {
    private String jobId;
    private String parkId;
    private String createdBy;
    private String printerProfileId;
    private String deviceIdentity;
    private String subjectType;
    private String subjectId;
    private String printItemType;
    private String printMode;
    private String status;
    private String snapshotJson;
    private String templateSnapshotHash;
    private String printerSnapshotHash;
    private String currentAttemptId;
    private String claimId;
    private String clientInstanceId;
    private String artifactsJson;
    private String operatorCheckId;
    private String errorCode;
    private String stateJson;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;
    private java.sql.Timestamp leaseExpiresAt;
}
