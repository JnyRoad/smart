package com.tce.smart.platform.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 权限版本协调持久记录，禁止物理删除永久协调行。 */
@Data
public class SmtAuthSourceCoord {
    private String id;
    private Integer parkId;
    private String sourceKind;
    private String stableKey;
    private String subjectType;
    private String subjectId;
    private String sourceRowId;
    private String sourceFingerprint;
    private Long generation;
    private String intentKey;
    private String intentFingerprint;
    private Long batchId;
    private String action;
    private String state;
    private Integer expanded;
    private String windows;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
