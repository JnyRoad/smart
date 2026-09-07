package com.tce.smart.platform.core.entity;
import lombok.Data;
import java.time.LocalDateTime;
/** 每次尝试的不可替换接入阶段；配置号与下载号分别永久保留。 */
@Data public class SmtAuthTransportPhase {
 private Long id; private Long targetId; private Long attemptId; private Integer attemptNo; private String leaseToken;
 private String sourceId; private Long sourceGeneration; private String resourceId; private Long resourceGeneration; private Long requestId;
 private Integer parkId; private String instanceId; private String accessType; private String phase; private String state;
 private String taskId; private String serialNo; private String requestKey; private String externalId; private String errorCode;
 private String deviceId; private String subjectType; private String subjectId; private String action; private String resourceType;
 private String serviceType; private String credentialChannel; private String cardNo; private String badge; private String personId; private String imageId;
 private Integer credentialVersion; private String credentialSnapshot;
 private String personSnapshot; private String orgIndexCode;
 private String personOperationKey; private String personIdentityHash; private Long personProofPhaseId;
 private Long startTime; private Long overTime; private Integer channelNo; private Integer pageNo;
 private LocalDateTime createTime; private LocalDateTime updateTime;
}
