package com.tce.smart.platform.core.entity;
import lombok.Data;
import java.time.LocalDateTime;
/** 同一主体来源受理与资源证据共享的永久短事务协调行。 */
@Data
public class SmtAuthSubjectCoord {
    private String id; private Integer parkId; private String subjectType; private String subjectId;
    private LocalDateTime createTime;
}
