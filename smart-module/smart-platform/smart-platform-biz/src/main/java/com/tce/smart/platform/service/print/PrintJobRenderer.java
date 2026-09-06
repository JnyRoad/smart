package com.tce.smart.platform.service.print;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.entity.print.*;
import com.tce.smart.platform.core.mapper.PrintJobMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;
import static com.tce.smart.platform.service.print.PrintJobTransactions.*;
/** 调度仅读取已提交记录；数据库行锁防止跨实例重复生成，绝不提交设备命令。 */
@Component public class PrintJobRenderer {
 final PrintJobMapper db;final PrintJobTransactions tx;final PrintRendererClient renderer;final PrintExecutionProperties config;
 public PrintJobRenderer(PrintJobMapper db,PrintJobTransactions tx,PrintRendererClient renderer,PrintExecutionProperties config){this.db=db;this.tx=tx;this.renderer=renderer;this.config=config;}
 @Scheduled(fixedDelayString="${smart.print.execution.render-poll-ms:3000}") public void poll(){if(!config.isRenderWorkerEnabled())return;for(PrintJob j:db.queuedJobs(new RowBounds(0,10)))try{render(j.getJobId());}catch(RuntimeException ignored){}}
 /** HTTP 渲染绝不占用数据库行锁；渲染中断留为失败记录，不能让另一实例重复出图。 */
 public void render(String id){JsonNode request=tx.atomic(()->{PrintJob j=db.lockJob(id);if(j==null||!"QUEUED".equals(j.getStatus()))return null;j.setStatus("RENDERING");j.setUpdatedAt(now());db.updateJob(j);return PrintJson.read(j.getSnapshotJson()).path("renderRequest").deepCopy();});if(request==null)return;
  try{JsonNode result=renderer.renderFrozen(request);tx.atomic(()->{PrintJob j=db.lockJob(id);if(j==null||!"RENDERING".equals(j.getStatus()))return null;ObjectNode stored=store(id,j.getParkId(),result);j.setArtifactsJson(PrintJson.canonical(stored));j.setStatus("READY");j.setUpdatedAt(now());db.updateJob(j);tx.audit("print-renderer",j.getParkId(),"JOB_RENDER_READY",id,PrintJobService.auditSummary(j,Collections.emptyMap()));return null;});}
  catch(PrintApiException failure){failed(id,failure.getCode());}
  catch(RuntimeException failure){failed(id,"RENDERER_UNEXPECTED_FAILURE");}
 }
 private void failed(String id,String code){tx.atomic(()->{PrintJob j=db.lockJob(id);if(j==null||!"RENDERING".equals(j.getStatus()))return null;j.setStatus("RENDER_FAILED");j.setActiveSubjectKey(null);j.setErrorCode(code);j.setUpdatedAt(now());db.updateJob(j);tx.audit("print-renderer",j.getParkId(),"JOB_RENDER_FAILED",id,PrintJobService.auditSummary(j,Collections.emptyMap()));return null;});}
 ObjectNode store(String owner,String park,JsonNode output){ObjectNode result=PrintJson.object();com.fasterxml.jackson.databind.node.ArrayNode artifacts=result.putArray("artifacts");for(JsonNode a:output.path("artifacts"))artifacts.add(storeOne(owner,park,a));if(output.hasNonNull("combinedArtifact"))result.set("combinedArtifact",storeOne(owner,park,output.get("combinedArtifact")));return result;}
 ObjectNode storeOne(String owner,String park,JsonNode artifact){ObjectNode copy=artifact.deepCopy();byte[] bytes;try{bytes=Base64.getDecoder().decode(copy.path("contentBase64").asText());}catch(Exception e){throw error(422,"RENDER_VALIDATION_FAILED");}if(bytes.length>32*1024*1024||!PrintJson.hashBytes(bytes).equals(copy.path("sha256").asText()))throw error(422,"PRINT_RESOURCE_HASH_MISMATCH");PrintAccessPolicy.uuid(copy.path("artifactId").asText());PrintJobArtifact row=new PrintJobArtifact();row.setArtifactId(copy.path("artifactId").asText());row.setOwnerId(owner);row.setParkId(park);row.setFace(copy.path("face").asText("combined"));row.setContentHash(copy.path("sha256").asText());row.setContentBytes(bytes);db.insertJobArtifact(row);copy.remove("contentBase64");return copy;}
}
