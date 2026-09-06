package com.tce.smart.platform.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 权限版本协调持久记录，禁止物理删除永久协调行。 */
@Data
public class SmtAuthSourceResource {
    private String id;
    private Long bindingRevision;
    private String sourceAction;
    private Long windowCount;
    private Long windowLength;
    private String currentSourceRowId;
    private String currentSourceFingerprint;
    private String intentKey;
    private String intentFingerprint;
    private String sourceCoordId;
    private Long sourceGeneration;
    private String resourceCoordId;
    private Long resourceGeneration;
    private String sourceRowId;
    private String sourceFingerprint;
    private String windows;
    private String action;
    private String state;
    private Long requestId;
    private Long targetId;
    private Long attemptId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
