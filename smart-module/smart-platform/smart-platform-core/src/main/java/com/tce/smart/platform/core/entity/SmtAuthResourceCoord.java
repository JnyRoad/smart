package com.tce.smart.platform.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 权限版本协调持久记录，禁止物理删除永久协调行。 */
@Data
public class SmtAuthResourceCoord {
    private String id;
    private Integer parkId;
    private String subjectType;
    private String subjectId;
    private String accessType;
    private String deviceId;
    private String resourceType;
    private String resourceId;
    private String serviceType;
    private String credentialChannel;
    private String resourceKey;
    private Long generation;
    private Long appliedGeneration;
    private String action;
    private String windows;
    private String desiredFingerprint;
    private String basisFingerprint;
    private Long blockingTargetId;
    private Long blockingAttemptId;
    private String blockReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
