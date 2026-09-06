package com.tce.smart.platform.service.print;

import lombok.Data;

/** 受控文件服务适配边界；实现必须返回可信元数据并核验人员或任务归属。 */
public interface PrintResourceStore {
    RegisteredResource describe(String objectId);
    boolean canAccess(String actorId, RegisteredResource resource);
    byte[] read(String objectId);

    @Data
    class RegisteredResource {
        private String objectId;
        private String contentHash;
        private String mediaType;
        private Long sizeBytes;
        private String parkId;
        private String purpose;
        private String accessScope;
        private String sourceRevision;
        private String subjectId;
    }
}
