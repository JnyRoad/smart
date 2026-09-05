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
 @Scheduled(fixedDelayString="${smart.print.execution.render-poll-ms:3000}") public void poll(){if(!config.isRenderWorkerEnabled())return;for(PrintJob j:db.queuedJobs(new RowBounds(0,10)))render(j.getJobId());}
 public void render(String id){tx.atomic(()->{PrintJob j=db.lockJob(id);if(j==null||!Arrays.asList("QUEUED","RENDERING").contains(j.getStatus()))return null;j.setStatus("RENDERING");db.updateJob(j);JsonNode result;
  try{result=renderer.renderFrozen(PrintJson.read(j.getSnapshotJson()).path("renderRequest"));}
  catch(PrintApiException failure){j.setStatus("RENDER_FAILED");j.setErrorCode(failure.getCode());j.setUpdatedAt(now());db.updateJob(j);tx.audit("print-renderer",j.getParkId(),"JOB_RENDER_FAILED",id,PrintJobService.auditSummary(j,Collections.emptyMap()));return null;}
  ObjectNode stored=store(id,j.getParkId(),result);j.setArtifactsJson(PrintJson.canonical(stored));j.setStatus("READY");j.setUpdatedAt(now());db.updateJob(j);tx.audit("print-renderer",j.getParkId(),"JOB_RENDER_READY",id,PrintJobService.auditSummary(j,Collections.emptyMap()));return null;});}
 ObjectNode store(String owner,String park,JsonNode output){ObjectNode result=PrintJson.object();com.fasterxml.jackson.databind.node.ArrayNode artifacts=result.putArray("artifacts");for(JsonNode a:output.path("artifacts"))artifacts.add(storeOne(owner,park,a));if(output.hasNonNull("combinedArtifact"))result.set("combinedArtifact",storeOne(owner,park,output.get("combinedArtifact")));return result;}
 ObjectNode storeOne(String owner,String park,JsonNode artifact){ObjectNode copy=artifact.deepCopy();byte[] bytes;try{bytes=Base64.getDecoder().decode(copy.path("contentBase64").asText());}catch(Exception e){throw error(422,"RENDER_VALIDATION_FAILED");}if(bytes.length>32*1024*1024||!PrintJson.hashBytes(bytes).equals(copy.path("sha256").asText()))throw error(422,"PRINT_RESOURCE_HASH_MISMATCH");PrintAccessPolicy.uuid(copy.path("artifactId").asText());PrintJobArtifact row=new PrintJobArtifact();row.setArtifactId(copy.path("artifactId").asText());row.setOwnerId(owner);row.setParkId(park);row.setFace(copy.path("face").asText("combined"));row.setContentHash(copy.path("sha256").asText());row.setContentBytes(bytes);db.insertJobArtifact(row);copy.remove("contentBase64");return copy;}
}
