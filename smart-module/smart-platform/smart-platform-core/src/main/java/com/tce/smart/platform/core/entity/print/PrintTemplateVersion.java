package com.tce.smart.platform.core.entity.print;

import lombok.Data;
import java.sql.Timestamp;

/** 打印领域持久化记录；表结构由正式发布流程管理。 */
@Data
public class PrintTemplateVersion {
    private String templateVersionId;
    private String templateId;
    private String parkId;
    private Long versionNo;
    private String versionStatus;
    private String faceRole;
    private Integer sideCount;
    private String layoutJson;
    private String fieldSchemaJson;
    private String resourceManifestJson;
    private String pageSpecJson;
    private String validationReportJson;
    private String contentHash;
    private Long draftRevision;
    private Timestamp publishedAt;
    private String publishedBy;
    private Timestamp createdAt;
    private String createdBy;
}
