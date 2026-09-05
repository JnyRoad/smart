package com.tce.smart.platform.service.print;

/** 预览制品的受控文件服务适配边界；写入必须持久、校验完整 hash 并保留归属。 */
public interface PrintPreviewArtifactStore {
    String write(String previewId, String parkId, String actorId, String artifactId, byte[] bytes, String hash);
    byte[] read(String objectId);
}
