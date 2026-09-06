package com.tce.smart.platform.core.entity;
import lombok.Data;
import java.time.LocalDateTime;
/** 物理凭据永久归属，不以租约过期或撤权成功自动释放。 */
@Data public class SmtAuthDirectClaim {
 private String id,deviceId,keyKind,keyValue,subjectType,subjectId;
 private Integer parkId; private String instanceId,resourceId,wireHash;
 private Long firstPhaseId; private String proofKind; private LocalDateTime createTime;
}
