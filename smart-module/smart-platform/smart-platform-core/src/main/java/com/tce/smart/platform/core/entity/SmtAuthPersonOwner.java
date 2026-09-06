package com.tce.smart.platform.core.entity;
import lombok.Data;
/** 同实例冻结主体的首建协调，不以设备、操作或照片划分唯一归属。 */
@Data public class SmtAuthPersonOwner {
 private String id,instanceId,subjectType,subjectId,operationKey,identityHash,identityCanonical,state,ownerToken,requestKey,personId,reason;
 private java.time.LocalDateTime createTime,updateTime;
 private Integer originPark; private Long ownerPhaseId;
}
