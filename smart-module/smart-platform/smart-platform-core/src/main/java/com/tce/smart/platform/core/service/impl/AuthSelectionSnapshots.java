package com.tce.smart.platform.core.service.impl;
import com.fasterxml.jackson.databind.*;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.Window;
import com.tce.smart.platform.core.entity.*;
import lombok.Data;
import java.time.*;
import java.util.*;
/** 固定外壳与显式 DTO 解码，不启用 Jackson 默认多态或 JSON 类名加载。 */
public final class AuthSelectionSnapshots {
 private static final ObjectMapper JSON=new ObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
 private AuthSelectionSnapshots(){}
 @Data public static class BusinessEnvelope { private JsonNode business; private List<Period> windows; }
 @Data public static class Period { private String from; private String to; }
 @Data public static class CredentialEnvelope { private String kind; private JsonNode credential; }
 public static String business(BusinessSnapshot business,List<Window> windows) {
  BusinessEnvelope out=new BusinessEnvelope();out.setBusiness(JSON.valueToTree(business));out.setWindows(new ArrayList<>());
  for(Window w:windows){validateWindow(w);Period p=new Period();p.setFrom(w.getFrom().toString());p.setTo(w.getTo().toString());out.getWindows().add(p);}return write(out);
 }
 public static <B extends BusinessSnapshot> B business(SmtAuthSelectionSource row,Class<B> type) {
  try {return JSON.treeToValue(read(row.getBusinessSnapshot(),BusinessEnvelope.class).getBusiness(),type);}catch(Exception e){throw invalid(e);}
 }
 public static List<Window> windows(SmtAuthSelectionSource row) {
  if(version(row)==0) {
   if("DELETE".equals(row.getDesiredAction()))return Collections.emptyList();
   if(row.getNewStartTime()==null || row.getNewEndTime()==null)throw new IllegalArgumentException("ADD 缺少冻结有效期");
   LocalDateTime from=local(row.getNewStartTime()),to=local(row.getNewEndTime()).toLocalDate().plusDays(1).atStartOfDay().minusSeconds(1);
   return Collections.singletonList(Window.builder().from(from).to(to).build());
  }
  List<Window> out=new ArrayList<>();BusinessEnvelope envelope=read(row.getBusinessSnapshot(),BusinessEnvelope.class);
  if(envelope.getWindows()==null)throw new IllegalArgumentException("缺少冻结窗口");
  for(Period p:envelope.getWindows())out.add(Window.builder().from(LocalDateTime.parse(p.getFrom())).to(LocalDateTime.parse(p.getTo())).build());return out;
 }
 public static String credential(Credential credential) {
  if(credential==null || (credential.getClass()!=PersonCredential.class && credential.getClass()!=VehicleCredential.class))throw new IllegalArgumentException("未注册凭据 DTO");
  validateCredential(credential,true);
  CredentialEnvelope out=new CredentialEnvelope();out.setKind(credential instanceof PersonCredential?"PERSON":"VEHICLE");out.setCredential(JSON.valueToTree(credential));return write(out);
 }
 public static Credential credential(Integer version,String json) {
  if(!Objects.equals(version,1))throw new IllegalArgumentException("未知凭据快照版本");
  CredentialEnvelope e=read(json,CredentialEnvelope.class);
  try {if("PERSON".equals(e.getKind())){PersonCredential p=JSON.treeToValue(e.getCredential(),PersonCredential.class);validateCredential(p,false);return p;}
   if("VEHICLE".equals(e.getKind())){
    JsonNode cardType=e.getCredential().get("cardType");
    if(cardType!=null && !cardType.isNull() && !cardType.isTextual())throw new IllegalArgumentException("车辆卡类型必须为明确字符串");
    // 旧v1未记录卡类型时保留null；下游必须按证据不足处理，不得补员工默认值。
    VehicleCredential v=JSON.treeToValue(e.getCredential(),VehicleCredential.class);validateCredential(v,false);return v;
   }
  }catch(Exception ex){throw invalid(ex);}throw new IllegalArgumentException("未知凭据种类");
 }
 static void validateWindow(Window w) {
  if(w==null || w.getFrom()==null || w.getTo()==null || !w.getTo().isAfter(w.getFrom()) || w.getFrom().getNano()%1000!=0 || w.getTo().getNano()%1000!=0)throw new IllegalArgumentException("窗口必须有效并匹配Oracle微秒精度");
 }
 private static void validateCredential(Credential credential,boolean newSnapshot) {
  if(credential instanceof PersonCredential) {
   PersonCredential p=(PersonCredential)credential;
   check(p.getTaskCardNo(),256,true);check(p.getName(),256,false);check(p.getImageId(),256,false);check(p.getBadge(),512,false);check(p.getCertificateNo(),256,false);check(p.getPersonId(),256,false);check(p.getCardType(),64,false);
   if(!Objects.equals(p.getTaskDeviceType(),1) || p.getTaskServiceType()==null)throw new IllegalArgumentException("人员任务类型不完整");
   if(p.getImageId()!=null && (p.getImageId().startsWith("data:") || p.getImageId().contains("base64,")))throw new IllegalArgumentException("照片只能冻结文件引用");
  } else {
   VehicleCredential v=(VehicleCredential)credential;check(v.getTaskCardNo(),256,true);check(v.getPlate(),128,true);check(v.getPersonId(),256,false);
   if(!Objects.equals(v.getTaskDeviceType(),2) || v.getTaskServiceType()==null)throw new IllegalArgumentException("车辆任务类型不完整");
   if((newSnapshot || v.getCardType()!=null) && !"0".equals(v.getCardType()) && !"1".equals(v.getCardType()))throw new IllegalArgumentException("车辆卡类型冻结证据必须为0或1");
  }
 }
 private static void check(String value,int bytes,boolean required) {
  if((required && (value==null || value.trim().isEmpty())) || (value!=null && value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>bytes))throw new IllegalArgumentException("凭据字段为空或超长");
 }
 /** 仅全部扩展身份字段都缺失的旧投影才能双读为员工。显式未知值绝不回落。 */
 public static boolean legacy(SmtAuthSelectionSource row){return row.getSourceKind()==null && row.getSubjectType()==null && row.getSnapshotVersion()==null && row.getBusinessSnapshot()==null && row.getParentKind()==null && row.getParentRowId()==null;}
 public static String kind(SmtAuthSelectionSource row){return legacy(row)?"STAFF_AUTH":row.getSourceKind();}
 public static String subject(SmtAuthSelectionSource row){return legacy(row)?"STAFF":row.getSubjectType();}
 public static int version(SmtAuthSelectionSource row){return legacy(row)?0:row.getSnapshotVersion()==null?-1:row.getSnapshotVersion();}
 public static String resourceSubject(SmtAuthSelectionResource row){return row.getSubjectType()==null && row.getCredentialVersion()==null && row.getCredentialSnapshot()==null?"STAFF":row.getSubjectType();}
 private static LocalDateTime local(Date d){return LocalDateTime.ofInstant(d.toInstant(),ZoneId.systemDefault());}
 private static String write(Object value){try{return JSON.writeValueAsString(value);}catch(Exception e){throw invalid(e);}}
 private static <T> T read(String json,Class<T> type){try{if(json==null)throw new IllegalArgumentException("缺少快照");return JSON.readValue(json,type);}catch(Exception e){throw invalid(e);}}
 private static IllegalArgumentException invalid(Exception e){return new IllegalArgumentException("冻结快照无法按白名单解析",e);}
}
