package com.tce.smart.platform.core.entity;
import lombok.Data;
import java.time.LocalDateTime;
/** 请求受理身份；最终结果与全部子批次在同一本库事务提交。 */
@Data
public class SmtAuthRequestIntake {
 private String operationKey;
 private Integer actorId;
 private String requestKey;
 private String requestKind;
 private Integer fingerprintVersion;
 private String requestFingerprint;
 private String authScope;
 private String outcome;
 private Integer childCount;
 private String childManifestHash;
 private LocalDateTime createTime;
 private LocalDateTime acceptedAt;
}
