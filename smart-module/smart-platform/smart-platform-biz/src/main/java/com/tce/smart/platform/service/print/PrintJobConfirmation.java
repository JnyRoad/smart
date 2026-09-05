package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.entity.print.PrintJobPreview;
import com.tce.smart.platform.core.mapper.PrintJobMapper;
import java.util.Arrays;
import java.util.UUID;

/** 服务端将已成功预览的可信内容与创建时重新冻结的内容关联。 */
final class PrintJobConfirmation {
 private PrintJobConfirmation() {}
 static ObjectNode evidence(ObjectNode frozen, JsonNode printer, String mode) {
  ObjectNode semantic=frozen.deepCopy();
  semantic.remove(Arrays.asList("renderRequest","resolution","printerSnapshot"));
  ObjectNode resolution=frozen.path("resolution").isObject()?((ObjectNode)frozen.get("resolution")).deepCopy():PrintJson.object();
  stripConfirmation(resolution);
  if(resolution.path("selection").isObject())stripConfirmation((ObjectNode)resolution.get("selection"));
  ObjectNode render=frozen.path("renderRequest").isObject()?((ObjectNode)frozen.get("renderRequest")).deepCopy():PrintJson.object();
  render.remove(Arrays.asList("requestId","jobId","previewId","purpose"));
  for(JsonNode face:render.path("faceSources"))for(JsonNode resource:face.path("resourceManifest"))
   if(resource.isObject()&&"personPhoto".equals(resource.path("bindingKey").asText()))((ObjectNode)resource).remove("objectId");
  semantic.set("resolution",resolution);semantic.set("renderRequest",render);semantic.set("printerSnapshot",printer);semantic.put("printMode",mode);
  return PrintJson.object().put("version",1).put("fingerprint",PrintJson.hash(semantic));
 }
 private static void stripConfirmation(ObjectNode value){value.remove(Arrays.asList("manualSelectionConfirmed","confirmedBy","confirmedAt"));}
 static PrintJobPreview require(PrintJobMapper db,String previewId,String park,String actor){
  try{if(previewId==null||!UUID.fromString(previewId).toString().equals(previewId))throw required();}catch(IllegalArgumentException e){throw required();}
  PrintJobPreview preview=db.findJobPreview(previewId);
  if(preview==null)throw required();
  if(!park.equals(preview.getParkId())||!actor.equals(preview.getCreatedBy()))throw PrintJobTransactions.error(403,"PRINT_SCOPE_DENIED");
  JsonNode details=PrintJson.read(preview.getDetailsJson());
  if(!"READY".equals(details.path("status").asText())||details.at("/confirmation/version").asInt()!=1||!details.at("/confirmation/fingerprint").isTextual())throw required();
  return preview;
 }
 static void verify(PrintJobPreview preview,ObjectNode frozen,JsonNode printer,String mode){
  if(!evidence(frozen,printer,mode).path("fingerprint").asText().equals(PrintJson.read(preview.getDetailsJson()).at("/confirmation/fingerprint").asText()))
   throw new PrintApiException(409,"PRINT_PREVIEW_STALE","打印内容或设备配置已变化，请重新预览后确认打印");
 }
 private static PrintApiException required(){return new PrintApiException(422,"PRINT_PREVIEW_REQUIRED","请先完成当前人员的打印预览，再确认打印");}
}
