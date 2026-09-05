package com.tce.smart.platform.core.entity.print;

import lombok.Data;
import java.sql.Timestamp;

/** 打印领域持久化记录；表结构由正式发布流程管理。 */
@Data
public class PrintAudit {
    private String auditId;
    private String parkId;
    private String actorId;
    private String action;
    private String objectId;
    private String detailsJson;
    private Timestamp createdAt;
}
