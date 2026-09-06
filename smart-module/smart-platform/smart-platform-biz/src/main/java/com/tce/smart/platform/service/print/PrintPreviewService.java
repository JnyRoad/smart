package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.tce.smart.platform.api.dto.req.print.PrintPreviewRequest;
import com.tce.smart.platform.api.dto.req.print.PrintTemplateRequest;
import com.tce.smart.platform.core.entity.print.*;
import com.tce.smart.platform.core.mapper.PrintPreviewMapper;
import com.tce.smart.platform.core.mapper.PrintTemplateMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** 预览冻结模板内容与组合指针后再渲染，制品不能成为正式打印任务。 */
@Service
public class PrintPreviewService {
    private final PrintTemplateMapper templates;
    private final PrintPreviewMapper previews;
    private final PrintAccessPolicy access;
    private final PrintTemplateValidator validator;
    private final PrintRendererClient renderer;
    private final PrintPreviewArtifactStore files;
    private final TransactionTemplate transactions;
    public PrintPreviewService(PrintTemplateMapper templates, PrintPreviewMapper previews, PrintAccessPolicy access, PrintTemplateValidator validator, PrintRendererClient renderer, @Nullable PrintPreviewArtifactStore files, PlatformTransactionManager transactions) {
        this.templates=templates; this.previews=previews; this.access=access; this.validator=validator; this.renderer=renderer; this.files=files; this.transactions=new TransactionTemplate(transactions);
    }
    public Map<String,Object> templatePreview(String templateId, PrintPreviewRequest request) {
        access.user(); requireFiles(); PrintAccessPolicy.uuid(templateId);
        Snapshot snapshot=transactions.execute(status->{
            PrintTemplate template=templates.lockTemplate(templateId); if(template==null) throw new PrintApiException(404,"TEMPLATE_NOT_FOUND","模板不存在"); access.require("preview",template.getParkId());
            PrintTemplateVersion version=version(request.getVersionId(),template.getParkId());
            if(!templateId.equals(version.getTemplateId())) PrintTemplateValidator.invalid(template.getFaceRole(),"versionId","TEMPLATE_VERSION_MISMATCH");
            if("DRAFT".equals(version.getVersionStatus()) && !Objects.equals(template.getCurrentDraftVersionId(),version.getTemplateVersionId())) PrintTemplateValidator.invalid(template.getFaceRole(),"versionId","CURRENT_DRAFT_REQUIRED");
            validate(template,version); Snapshot result=new Snapshot(); result.park=template.getParkId(); result.printItemType=template.getPrintItemType(); result.versions=Collections.singletonList(version); result.details.put("templateId",templateId); result.details.put("face",version.getFaceRole()); result.details.put("draftRevision",version.getDraftRevision()); return result;
        });
        return render(snapshot,request.getSampleData());
    }
    public Map<String,Object> pairPreview(String pairId, PrintPreviewRequest request) {
        access.user(); requireFiles(); PrintAccessPolicy.uuid(pairId);
        Snapshot snapshot=transactions.execute(status->{
            PrintTemplatePair pair=templates.lockTemplatePair(pairId); if(pair==null) throw new PrintApiException(404,"TEMPLATE_PAIR_NOT_FOUND","组合不存在"); access.require("preview",pair.getParkId());
            if(request.getRevision()!=null && !request.getRevision().equals(pair.getRevision())) throw new PrintApiException(409,"PAIR_REVISION_CONFLICT","组合修订已变化",Collections.singletonMap("currentRevision",pair.getRevision()));
            if(!"ACTIVE".equals(pair.getStatus())) throw new PrintApiException(409,"PAIR_ARCHIVED","组合已归档");
            PrintTemplateVersion front=version(pair.getFrontTemplateVersionId(),pair.getParkId()),back=version(pair.getBackTemplateVersionId(),pair.getParkId());
            if(!"PUBLISHED".equals(front.getVersionStatus()) || !"PUBLISHED".equals(back.getVersionStatus()) || !"FRONT".equals(front.getFaceRole()) || !"BACK".equals(back.getFaceRole())) PrintTemplateValidator.invalid(null,"pair","PUBLISHED_PAIR_REQUIRED");
            for(PrintTemplateVersion version:Arrays.asList(front,back)) {
                PrintTemplate template=templates.findTemplate(version.getTemplateId());
                if(template==null || !pair.getParkId().equals(template.getParkId())) throw new PrintApiException(403,"PRINT_SCOPE_DENIED","模板归属不匹配");
                if(!pair.getPrintItemType().equals(template.getPrintItemType()) || !pair.getPersonType().equals(template.getPersonType()) || !pair.getClassificationCode().equals(template.getClassificationCode())) PrintTemplateValidator.invalid(version.getFaceRole(),"pair","PAIR_VERSION_MISMATCH"); validate(template,version);
            }
            JsonNode a=PrintJson.read(front.getPageSpecJson()),b=PrintJson.read(back.getPageSpecJson());
            // 与组合保存使用相同的物理尺寸规则，54与54.0不应产生不同的兼容性结论。
            for(String field:Arrays.asList("widthMm","heightMm")) if(Math.abs(a.path(field).asDouble()-b.path(field).asDouble())>0.000001) PrintTemplateValidator.invalid(null,field,"PAIR_PAGE_MISMATCH");
            for(String field:Arrays.asList("orientation","mediaType","mediaSpec")) if(!a.path(field).equals(b.path(field))) PrintTemplateValidator.invalid(null,field,"PAIR_PAGE_MISMATCH");
            Snapshot result=new Snapshot(); result.park=pair.getParkId(); result.printItemType=pair.getPrintItemType(); result.versions=Arrays.asList(front,back); result.details.put("pairId",pairId); result.details.put("pairRevision",pair.getRevision()); return result;
        });
        return render(snapshot,request.getSampleData());
    }
    private Map<String,Object> render(Snapshot snapshot,JsonNode sampleData) {
        if(sampleData==null || sampleData.isNull()) sampleData=PrintJson.object();
        PrintJson.limit(sampleData,1024*1024); access.rejectUnsafeJson(sampleData);
        if(!sampleData.isObject()) throw new PrintApiException(422,"TEMPLATE_VALIDATION_FAILED","示例数据必须为对象");
        Set<String> allowed=new HashSet<>(); for(PrintTemplateVersion version:snapshot.versions) for(JsonNode field:PrintJson.read(version.getFieldSchemaJson()).path("fields")) allowed.add(field.path("key").asText());
        Iterator<Map.Entry<String,JsonNode>> fields=sampleData.fields();
        while(fields.hasNext()) { Map.Entry<String,JsonNode> field=fields.next(); if(!PrintTemplateValidator.FIELD_KEYS.contains(field.getKey()) || !allowed.contains(field.getKey())) throw new PrintApiException(403,"PRINT_SCOPE_DENIED","仅支持模板白名单中的合成预览字段"); if(!field.getValue().isTextual() || field.getValue().asText().length()>512) PrintTemplateValidator.invalid(null,field.getKey(),"INVALID_SAMPLE_VALUE"); }
        String previewId=UUID.randomUUID().toString(); Map<String,Object> details=new LinkedHashMap<>(snapshot.details); details.put("previewId",previewId); details.put("sideCount",snapshot.versions.size()); details.put("pageCount",snapshot.versions.size()); details.put("createdAt",Instant.now().toString());
        List<Map<String,Object>> sources=new ArrayList<>(); for(PrintTemplateVersion version:snapshot.versions) { Map<String,Object> source=new LinkedHashMap<>(); source.put("face",version.getFaceRole()); source.put("templateVersionId",version.getTemplateVersionId()); source.put("contentHash",version.getContentHash()); source.put("draftRevision",version.getDraftRevision()); sources.add(source); } details.put("faceSources",sources);
        JsonNode rendered=null;
        try {
            rendered=renderer.renderPreview(previewId,snapshot.printItemType,snapshot.versions,sampleData);
        } catch(PrintApiException failure) {
            // 服务故障或权限错误不能登记成模板失败；只有422校验诊断可作为失败预览。
            if(failure.getStatus()!=422) throw failure;
            details.put("status","RENDER_FAILED"); details.put("errorCode",failure.getCode()); details.put("violations",failure.getDetails().getOrDefault("violations",Collections.emptyList())); details.put("artifacts",Collections.emptyList());
        }
        // 渲染在事务外完成；全部制品与预览记录必须共同提交，任一步保存失败都终止暂存批次。
        final JsonNode output=rendered;
        if(output==null) return transactions.execute(status->{
            PrintPreview preview=new PrintPreview(); preview.setPreviewId(previewId); preview.setParkId(snapshot.park); preview.setCreatedBy(access.actor()); preview.setCreatedAt(Timestamp.from(Instant.now())); preview.setStatus((String)details.get("status")); preview.setDetailsJson(PrintJson.canonical(details));
            previews.insertPreview(preview); return publicDetails(preview);
        });
        PrintPreviewArtifactStore.Batch batch=files.stage(previewId,snapshot.park,access.actor());
        try { return transactions.execute(status->{
            if(output!=null) {
                List<Map<String,Object>> artifacts=new ArrayList<>();
                for(JsonNode artifact:output.path("artifacts")) artifacts.add(store(batch,artifact));
                details.put("artifacts",artifacts);
                if(output.hasNonNull("combinedArtifact")) details.put("combinedArtifact",store(batch,output.get("combinedArtifact")));
                files.commit(batch);
                details.put("status","READY");
            }
            PrintPreview preview=new PrintPreview(); preview.setPreviewId(previewId); preview.setParkId(snapshot.park); preview.setCreatedBy(access.actor()); preview.setCreatedAt(Timestamp.from(Instant.now())); preview.setStatus((String)details.get("status")); preview.setDetailsJson(PrintJson.canonical(details));
            previews.insertPreview(preview); return publicDetails(preview);
        }); } catch(RuntimeException failure) { try { files.abort(batch); } catch(RuntimeException abortFailure) { failure.addSuppressed(abortFailure); } throw failure; }
    }

    private Map<String,Object> store(PrintPreviewArtifactStore.Batch batch,JsonNode artifact) {
        Map<String,Object> result=PrintJson.map(artifact); byte[] bytes=Base64.getDecoder().decode(artifact.path("contentBase64").asText()); String id=artifact.path("artifactId").asText(); PrintAccessPolicy.uuid(id);
        String objectId=files.write(batch,id,bytes,artifact.path("sha256").asText());
        if(objectId==null || !objectId.matches("[A-Za-z0-9_.:-]{1,128}")) throw new PrintApiException(503,"PRINT_ARTIFACT_STORE_UNAVAILABLE","制品存储未返回受控对象标识");
        result.remove("contentBase64"); result.put("objectId",objectId); return result;
    }
    public Map<String,Object> detail(String id) { return publicDetails(authorized(id)); }
    public byte[] readArtifact(String previewId,String artifactId) {
        PrintPreview preview=authorized(previewId); access.require("resource",preview.getParkId()); requireFiles(); PrintAccessPolicy.uuid(artifactId);
        if(!"READY".equals(preview.getStatus())) throw new PrintApiException(409,"PRINT_PREVIEW_NOT_READY","预览尚未就绪");
        JsonNode details=PrintJson.read(preview.getDetailsJson()); List<JsonNode> candidates=new ArrayList<>(); details.path("artifacts").forEach(candidates::add); if(details.hasNonNull("combinedArtifact")) candidates.add(details.get("combinedArtifact"));
        for(JsonNode artifact:candidates) if(artifactId.equals(artifact.path("artifactId").asText())) {
            byte[] bytes=files.read(artifact.path("objectId").asText()); if(bytes==null || bytes.length>32*1024*1024 || bytes.length!=artifact.path("bytes").asLong() || !PrintJson.hashBytes(bytes).equals(artifact.path("sha256").asText())) throw new PrintApiException(422,"PRINT_RESOURCE_HASH_MISMATCH","预览制品校验失败"); return bytes;
        }
        throw new PrintApiException(403,"PRINT_SCOPE_DENIED","制品不属于该预览");
    }
    private PrintPreview authorized(String id) { access.user(); PrintAccessPolicy.uuid(id); PrintPreview preview=previews.findPreview(id); if(preview==null) throw new PrintApiException(404,"PRINT_PREVIEW_NOT_FOUND","预览不存在"); access.require("preview",preview.getParkId()); if(!access.actor().equals(preview.getCreatedBy())) throw new PrintApiException(403,"PRINT_SCOPE_DENIED","无权读取他人的预览"); return preview; }
    private PrintTemplateVersion version(String id,String park) { PrintAccessPolicy.uuid(id); PrintTemplateVersion version=templates.findTemplateVersion(id); if(version==null) throw new PrintApiException(404,"TEMPLATE_VERSION_NOT_FOUND","模板版本不存在"); if(!park.equals(version.getParkId())) throw new PrintApiException(403,"PRINT_SCOPE_DENIED","模板版本不属于本园区"); PrintTemplateValidator.integrity(version); return version; }
    private void validate(PrintTemplate template,PrintTemplateVersion version) { PrintTemplateRequest request=new PrintTemplateRequest(); request.setName(template.getName()); request.setPrintItemType(template.getPrintItemType()); request.setPersonType(template.getPersonType()); request.setClassificationCode(template.getClassificationCode()); request.setFaceRole(version.getFaceRole()); request.setSideCount(version.getSideCount()); request.setLayoutJson(PrintJson.read(version.getLayoutJson())); request.setFieldSchemaJson(PrintJson.read(version.getFieldSchemaJson())); request.setPageSpecJson(PrintJson.read(version.getPageSpecJson())); request.setResourceManifest(PrintJson.read(version.getResourceManifestJson())); validator.validate(template.getParkId(),request); }
    private void requireFiles() { if(files==null) throw new PrintApiException(503,"PRINT_ARTIFACT_STORE_NOT_CONFIGURED","预览制品文件服务尚未配置"); }
    private Map<String,Object> publicDetails(PrintPreview preview) { Map<String,Object> result=PrintJson.map(PrintJson.read(preview.getDetailsJson())); List<?> artifacts=(List<?>)result.get("artifacts"); if(artifacts!=null) for(Object artifact:artifacts) publicArtifact(preview.getPreviewId(),(Map<String,Object>)artifact); if(result.get("combinedArtifact")!=null) publicArtifact(preview.getPreviewId(),(Map<String,Object>)result.get("combinedArtifact")); return result; }
    private void publicArtifact(String previewId,Map<String,Object> artifact) { artifact.remove("objectId"); artifact.put("downloadPath","/platform/print/v1/previews/"+previewId+"/artifacts/"+artifact.get("artifactId")); }
    private static class Snapshot { String park; String printItemType; List<PrintTemplateVersion> versions; Map<String,Object> details=new LinkedHashMap<>(); }
}
