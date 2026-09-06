package com.tce.smart.platform.service.print;

/** 预览制品的受控文件服务适配边界；写入必须持久、校验完整 hash 并保留归属。 */
public interface PrintPreviewArtifactStore {
    /**
     * 外部存储实现只能在批次提交时使制品可见；数据库元数据提交失败时 {@link #abort(Batch)}
     * 必须删除已写入内容。SQL 实现参与调用方事务，提交和终止均为无操作，由数据库回滚字节。
     */
    interface Batch { }
    Batch stage(String previewId, String parkId, String actorId);
    String write(Batch batch, String artifactId, byte[] bytes, String hash);
    void commit(Batch batch);
    void abort(Batch batch);
    byte[] read(String objectId);
}
