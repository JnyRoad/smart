package com.tce.smart.platform.core.entity.print;

import lombok.Data;
import java.sql.Timestamp;

/** 打印领域持久化记录；表结构由正式发布流程管理。 */
@Data
public class PrintTemplatePair {
    private String pairId;
    private String parkId;
    private String name;
    private String printItemType;
    private String personType;
    private String classificationCode;
    private String frontTemplateVersionId;
    private String backTemplateVersionId;
    private Long revision;
    private String status;
    private String createdBy;
    private Timestamp createdAt;
    private String updatedBy;
    private Timestamp updatedAt;
    private Timestamp archivedAt;
}
