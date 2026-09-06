package com.tce.smart.platform.service.print;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.entity.print.PrintStoredObject;
import com.tce.smart.platform.core.mapper.PrintObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** 复用平台Oracle存储模式，模板图片和预览共用私有对象表；无公开下载或覆盖写入。 */
@Service
public class SqlPrintObjectStore implements PrintResourceStore, PrintPreviewArtifactStore {
    private final PrintObjectMapper mapper;
    private static final class SqlPreviewBatch implements PrintPreviewArtifactStore.Batch {
        private final String previewId;
        private final String parkId;
        private final String actorId;
        private SqlPreviewBatch(String previewId,String parkId,String actorId) { this.previewId=previewId; this.parkId=parkId; this.actorId=actorId; }
    }
    public SqlPrintObjectStore(PrintObjectMapper mapper) { this.mapper=mapper; }
    @Override public RegisteredResource describe(String id) {
        PrintStoredObject object=mapper.findMetadata(id); if(object==null) return null;
        RegisteredResource result=new RegisteredResource(); result.setObjectId(object.getObjectId()); result.setParkId(object.getParkId()); result.setPurpose(object.getPurpose()); result.setAccessScope(object.getAccessScope()); result.setMediaType(object.getMediaType()); result.setContentHash(object.getContentHash()); result.setSizeBytes(object.getSizeBytes()); return result;
    }
    @Override public boolean canAccess(String actorId,RegisteredResource resource) {
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof SmartUser)) return false;
        SmartUser user=(SmartUser)auth.getPrincipal();
        return user.isEnabled() && user.isAccountNonLocked() && actorId.equals(String.valueOf(user.getId())) && "TEMPLATE".equals(resource.getAccessScope()) && Arrays.asList("LOGO","BACKGROUND").contains(resource.getPurpose()) && user.getParkIdList()!=null && user.getParkIdList().stream().filter(Objects::nonNull).anyMatch(park->String.valueOf(park).equals(resource.getParkId()));
    }
    @Override public byte[] read(String objectId) { PrintStoredObject content=mapper.readContent(objectId); return content==null ? null : content.getContentBytes(); }
    /** 调用方须先完成园区和编辑授权；生成新对象，历史版本引用永不改变。 */
    @Transactional public RegisteredResource saveTemplateImage(String park,String actor,String purpose,String mediaType,byte[] bytes) {
        String id=insert(park,actor,purpose,"TEMPLATE",null,mediaType,bytes,PrintJson.hashBytes(bytes)); return describe(id);
    }
    /** 预览服务授权后调用，参与其元数据事务，失败时对象字节一并回滚。 */
    @Override public PrintPreviewArtifactStore.Batch stage(String previewId,String parkId,String actorId) {
        PrintAccessPolicy.uuid(previewId); return new SqlPreviewBatch(previewId,parkId,actorId);
    }
    @Override @Transactional public String write(PrintPreviewArtifactStore.Batch raw,String artifactId,byte[] bytes,String hash) {
        if(!(raw instanceof SqlPreviewBatch)) throw new PrintApiException(503,"PRINT_ARTIFACT_STORE_UNAVAILABLE","预览制品暂存批次无效");
        SqlPreviewBatch batch=(SqlPreviewBatch)raw; PrintAccessPolicy.uuid(artifactId);
        if(bytes==null || bytes.length>32*1024*1024 || !PrintJson.hashBytes(bytes).equals(hash)) throw new PrintApiException(422,"PRINT_RESOURCE_HASH_MISMATCH","预览制品校验失败");
        return insert(batch.parkId,batch.actorId,"PREVIEW","PRINT_PREVIEW",batch.previewId,"application/pdf",bytes,hash);
    }
    /** 数据库事务统一提交或回滚内容；这里不额外执行外部副作用。 */
    @Override public void commit(PrintPreviewArtifactStore.Batch raw) { requirePreviewBatch(raw); }
    @Override public void abort(PrintPreviewArtifactStore.Batch raw) { requirePreviewBatch(raw); }
    private static void requirePreviewBatch(PrintPreviewArtifactStore.Batch raw) { if(!(raw instanceof SqlPreviewBatch)) throw new PrintApiException(503,"PRINT_ARTIFACT_STORE_UNAVAILABLE","预览制品暂存批次无效"); }
    private String insert(String park,String actor,String purpose,String scope,String owner,String mediaType,byte[] bytes,String hash) {
        PrintStoredObject object=new PrintStoredObject(); object.setObjectId(UUID.randomUUID().toString()); object.setParkId(park); object.setCreatedBy(actor); object.setPurpose(purpose); object.setAccessScope(scope); object.setOwnerId(owner); object.setMediaType(mediaType); object.setContentHash(hash); object.setSizeBytes((long)bytes.length); object.setCreatedAt(Timestamp.from(Instant.now())); object.setContentBytes(bytes); mapper.insertObject(object); return object.getObjectId();
    }
}
