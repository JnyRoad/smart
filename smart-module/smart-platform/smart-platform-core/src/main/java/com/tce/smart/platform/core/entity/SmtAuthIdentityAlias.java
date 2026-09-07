package com.tce.smart.platform.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 权限版本协调持久记录，禁止物理删除永久协调行。 */
@Data
public class SmtAuthIdentityAlias {
    private String id;
    private Integer parkId;
    private String accessType;
    private String deviceId;
    private String resourceType;
    private String resourceValue;
    private String serviceType;
    private String credentialChannel;
    private String subjectType;
    private String subjectId;
    private String resourceCoordId;
    private Long resourceGeneration;
    private String aliasKind;
    private String aliasValue;
    private LocalDateTime createTime;
}
