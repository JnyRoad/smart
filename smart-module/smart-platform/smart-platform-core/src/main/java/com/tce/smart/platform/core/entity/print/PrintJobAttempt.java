package com.tce.smart.platform.core.entity.print;
import lombok.Data;
/** 打印领域持久记录；物理占用与冻结快照独立保存。 */
@Data
public class PrintJobAttempt {
    private String attemptId;
    private String jobId;
    private String commandId;
    private String face;
    private String stateJson;
    private Integer attemptNo;
}
