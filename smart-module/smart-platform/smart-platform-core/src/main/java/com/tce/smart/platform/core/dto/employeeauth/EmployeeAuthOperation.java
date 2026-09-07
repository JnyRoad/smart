package com.tce.smart.platform.core.dto.employeeauth;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.ResourceInput;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import lombok.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
/** 服务器构造的员工来源投影，不接受客户端直接提交资源或园区。 */
public final class EmployeeAuthOperation {
 private EmployeeAuthOperation() { }
 @Value @Builder(toBuilder=true) public static class Source {
  Integer parkId; String subjectId; String authId; SmtStaffDeviceAuth before; SmtStaffDeviceAuth after;
  String imageId; String personSnapshot; String badge; String verificationReason; @Singular List<ResourceInput> resources;
  @Singular("evidence") List<HistoryEvidence> historyEvidence;
 }
 /** 历史原始行的最小核验投影；原服务类型与关联线索不得改写为命令。 */
 @Data @Builder(toBuilder=true) @NoArgsConstructor @AllArgsConstructor public static class HistoryEvidence {
  String origin; String rowId; String subjectId; Integer parkId; String deviceId; String accessType;
  String deviceType; String serviceType; String taskId; String externalTaskId; String personId;
  String action; String status; String createdAt; String updatedAt; String reviewCode;
  Integer evidenceVersion;
  String parkOrigin; String resourceType; String resourceId; String credentialChannel;
  String imageId; String startTime; String overTime; String windowEncoding;
  String serialNo; String code; String times; String consume; String general; String remark; String cardType;
  String badge; String optUser; String applyId; String batchId;
 }
 @Value @Builder public static class Accepted {
  String operationKey; Map<Integer,List<Long>> batches;
 }
 @Value public static class HistorySnapshot {String format; boolean complete; List<HistoryEvidence> rows;}
 private static final ObjectMapper HISTORY_JSON=new ObjectMapper().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,true);
 private static final Set<String> V1_FIELDS=new TreeSet<>(Arrays.asList("origin","rowId","subjectId","parkId","deviceId","accessType","deviceType","serviceType","taskId","externalTaskId","personId","action","status","createdAt","updatedAt","reviewCode"));
 /** 历史格式与员工业务版本0独立；旧V1字段及NULL表示逐字保持。 */
 public static String encodeHistory(List<HistoryEvidence> evidence) {
  if(evidence==null || evidence.isEmpty())return null;
  boolean v2=evidence.stream().anyMatch(e->e.getEvidenceVersion()!=null);
  try {
   List<String> rows=new ArrayList<>();for(HistoryEvidence row:evidence) {
    if(v2 && !Integer.valueOf(2).equals(row.getEvidenceVersion()))throw new IllegalArgumentException("HISTORY_EVIDENCE_VERSION_MISMATCH");
    Map<String,Object> values=historyValues(row);if(!v2)values.keySet().retainAll(V1_FIELDS);rows.add(HISTORY_JSON.writeValueAsString(values));
   }
   Collections.sort(rows);return "{\"format\":\"EMPLOYEE_HISTORY_EVIDENCE_V"+(v2?2:1)+"\",\"rows\":["+String.join(",",rows)+"]}";
  }catch(java.io.IOException failure){throw new IllegalArgumentException("员工历史证据无法编码",failure);}
 }
 /** 仅证明完整格式；真实主体、路由、原行重读和本次可信结果须由桥接方分别验证。 */
 public static HistorySnapshot decodeHistory(String value) {
  if(value==null)return new HistorySnapshot(null,false,Collections.emptyList());
  try {
   JsonNode root=HISTORY_JSON.readTree(value);String format=root.path("format").asText(null);
   if(format==null)return new HistorySnapshot(null,false,Collections.emptyList());
   boolean v2="EMPLOYEE_HISTORY_EVIDENCE_V2".equals(format);
   if(!v2 && !"EMPLOYEE_HISTORY_EVIDENCE_V1".equals(format))throw new IllegalArgumentException("HISTORY_EVIDENCE_VERSION_UNSUPPORTED");
   if(!root.path("rows").isArray())throw new IllegalArgumentException("HISTORY_EVIDENCE_ROWS_MISSING");
   if(v2 && root.path("rows").size()==0)throw new IllegalArgumentException("HISTORY_EVIDENCE_ROWS_EMPTY");
   List<HistoryEvidence> rows=new ArrayList<>();Set<String> fields=historyValues(new HistoryEvidence()).keySet();
   for(JsonNode row:root.path("rows")) {
    if(v2){if(!row.path("evidenceVersion").isInt() || row.path("evidenceVersion").intValue()!=2)throw new IllegalArgumentException("HISTORY_EVIDENCE_VERSION_MISMATCH");for(String field:fields)if(!row.has(field))throw new IllegalArgumentException("HISTORY_EVIDENCE_FIELD_MISSING:"+field);
     if(!Arrays.asList("DIRECT_TASK","DIRECT_DOWN_RECORD","ISC_TASK","ISC_DOWN_RECORD","RESOURCE_COORD").contains(row.path("origin").asText()))throw new IllegalArgumentException("HISTORY_EVIDENCE_ORIGIN_UNSUPPORTED");
     if(row.path("rowId").asText("").trim().isEmpty())throw new IllegalArgumentException("HISTORY_EVIDENCE_ROW_ID_MISSING");
     validateHistoryEncoding(row);}
    rows.add(HISTORY_JSON.treeToValue(row,HistoryEvidence.class));
   }
   return new HistorySnapshot(format,v2,Collections.unmodifiableList(rows));
  }catch(java.io.IOException failure){throw new IllegalArgumentException("员工历史证据无法解码",failure);}
 }
 /** 显式SQL NULL表示已采集为空；未知或相互矛盾的格式标签不得补默认值。 */
 private static void validateHistoryEncoding(JsonNode row) {
  String origin=row.path("origin").asText();boolean task=origin.endsWith("_TASK"),coord="RESOURCE_COORD".equals(origin);
  String parkOrigin=coord?"RESOURCE_COORD":task?"DEVICE_CURRENT":"RAW_RECORD";
  String windowEncoding=coord?null:task?"EPOCH_SECONDS":"LOCAL_TIMESTAMP_FF9";
  JsonNode park=row.get("parkOrigin"),encoding=row.get("windowEncoding");
  if(!park.isTextual() || !parkOrigin.equals(park.textValue()) || (coord?!encoding.isNull():!encoding.isTextual() || !windowEncoding.equals(encoding.textValue())))throw new IllegalArgumentException("HISTORY_EVIDENCE_ENCODING_MISMATCH");
  for(String field:Arrays.asList("startTime","overTime")) {
   JsonNode value=row.get(field);if(value.isNull())continue;
   if(coord || !value.isTextual())throw new IllegalArgumentException("HISTORY_EVIDENCE_WINDOW_ENCODING_INVALID:"+field);
   String text=value.textValue();
   try {
    if(task){if(!text.matches("-?[0-9]+"))throw new IllegalArgumentException();Long.parseLong(text);}
    else {
     if(!text.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{9}"))throw new IllegalArgumentException();
     java.time.LocalDateTime.parse(text,java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS").withResolverStyle(java.time.format.ResolverStyle.STRICT));
    }
   }catch(RuntimeException failure){throw new IllegalArgumentException("HISTORY_EVIDENCE_WINDOW_ENCODING_INVALID:"+field,failure);}
  }
 }
 /** 原行及归属证明的唯一哈希；派生核验原因不属于原行内容。 */
 public static String rawFingerprint(HistoryEvidence row) {
  try {
   Map<String,Object> values=historyValues(row);values.remove("reviewCode");byte[] bytes=("EMPLOYEE_RAW_EVIDENCE_V2:"+HISTORY_JSON.writeValueAsString(values)).getBytes(java.nio.charset.StandardCharsets.UTF_8);
   byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format("%02x",b&255));return hex.toString();
  }catch(java.io.IOException|java.security.NoSuchAlgorithmException failure){throw new IllegalArgumentException("员工原始行无法指纹化",failure);}
 }
 @SuppressWarnings("unchecked") private static Map<String,Object> historyValues(HistoryEvidence row){return new TreeMap<>((Map<String,Object>)HISTORY_JSON.convertValue(row,Map.class));}
}
