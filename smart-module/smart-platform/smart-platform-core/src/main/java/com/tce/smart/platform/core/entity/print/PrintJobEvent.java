package com.tce.smart.platform.core.entity.print;
import lombok.Data;
/** 打印领域持久记录；物理占用与冻结快照独立保存。 */
@Data
public class PrintJobEvent {
    private String eventId;
    private String jobId;
    private String attemptId;
    private String commandId;
    private String eventType;
    private String bodyHash;
    private String responseJson;
    private String detailsJson;
    private java.sql.Timestamp createdAt;
}
