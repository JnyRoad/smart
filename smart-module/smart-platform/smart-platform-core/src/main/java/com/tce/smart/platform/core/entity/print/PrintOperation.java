package com.tce.smart.platform.core.entity.print;

import lombok.Data;
import java.sql.Timestamp;

/** 打印领域持久化记录；表结构由正式发布流程管理。 */
@Data
public class PrintOperation {
    private String operationId;
    private String principalId;
    private String idempotencyKey;
    private String bodyHash;
    private String responseJson;
    private Timestamp createdAt;
}
