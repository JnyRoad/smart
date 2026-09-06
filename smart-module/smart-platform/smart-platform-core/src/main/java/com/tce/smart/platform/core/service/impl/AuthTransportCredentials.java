package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import org.springframework.beans.BeanUtils;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** 只读已冻结来源与阶段；返回新 DTO，不修改旧员工投影或读取当前业务资料。 */
public final class AuthTransportCredentials {
 private AuthTransportCredentials(){}
 public static Credential fromPhase(SmtAuthTransportPhase p){
  require(p!=null,"MISSING_FROZEN_PHASE");Credential c;
  if(p.getCredentialVersion()==null&&p.getCredentialSnapshot()==null){
   require("STAFF".equals(p.getSubjectType()),"UNKNOWN_CREDENTIAL_VERSION");
   PersonCredential v=new PersonCredential();v.setTaskCardNo(p.getCardNo());v.setTaskDeviceType(1);v.setTaskServiceType(number(p.getServiceType()));v.setCardType("1");v.setBadge(p.getBadge());v.setImageId(p.getImageId());c=v;
  }else c=AuthSelectionSnapshots.credential(p.getCredentialVersion(),p.getCredentialSnapshot());
  validate(p,c);return c;
 }
 /** 所有贡献、来源及资源必须成完整集合；任何一个冲突都不能任取首行覆盖。 */
 public static SmtAuthTransportPhase freeze(SmtAuthTransportPhase p,List<SmtAuthSourceResource> bindings,List<SmtAuthSelectionSource> sources,List<SmtAuthSelectionResource> resources){
  require(p!=null&&bindings!=null&&sources!=null&&resources!=null&&!bindings.isEmpty()&&bindings.size()<=1000&&sources.size()<=1000&&resources.size()<=1000,"FROZEN_PROJECTION_UNVERIFIED");
  Set<String> usedSources=new HashSet<>(),usedResources=new HashSet<>();SmtAuthTransportPhase chosen=null;
  for(SmtAuthSourceResource b:bindings){
   require(b!=null&&eq(b.getTargetId(),p.getTargetId())&&eq(b.getResourceCoordId(),p.getResourceId())&&eq(b.getResourceGeneration(),p.getResourceGeneration()),"FROZEN_BINDING_MISMATCH");
   List<SmtAuthSelectionSource> matches=new ArrayList<>();for(SmtAuthSelectionSource s:sources)if(eq(s.getSourceCoordId(),b.getSourceCoordId())&&eq(s.getSourceGeneration(),b.getSourceGeneration()))matches.add(s);
   require(matches.size()==1,"FROZEN_SOURCE_SET_MISMATCH");SmtAuthSelectionSource s=matches.get(0);
   require(eq(s.getParkId(),p.getParkId())&&eq(s.getSubjectId(),p.getSubjectId())&&eq(AuthSelectionSnapshots.subject(s),p.getSubjectType())&&eq(s.getSourceRowId(),b.getSourceRowId())&&eq(s.getFingerprint(),b.getSourceFingerprint()),"FROZEN_SOURCE_MISMATCH");
   require(sourceMatches(s),"UNSUPPORTED_SOURCE");usedSources.add(sourceKey(s));int count=0;
   for(SmtAuthSelectionResource r:resources)if(eq(r.getBatchId(),s.getBatchId())&&eq(r.getSourceOrdinal(),s.getOrdinal())){
    count++;require(eq(r.getParkId(),p.getParkId())&&eq(AuthSelectionSnapshots.resourceSubject(r),p.getSubjectType())&&eq(r.getSubjectId(),p.getSubjectId())&&eq(r.getResourceCoordId(),p.getResourceId())&&eq(r.getDeviceId(),p.getDeviceId())&&eq(r.getAccessType(),p.getAccessType())&&eq(r.getResourceType(),p.getResourceType())&&eq(r.getServiceType(),p.getServiceType())&&eq(r.getCredentialChannel(),p.getCredentialChannel()),"FROZEN_RESOURCE_MISMATCH");
    require(sameEpoch(r.getValidFrom(),p.getStartTime())&&sameEpoch(r.getValidTo(),p.getOverTime()),"FROZEN_WINDOW_MISMATCH");
    SmtAuthTransportPhase copy=new SmtAuthTransportPhase();BeanUtils.copyProperties(p,copy);
    boolean legacy=r.getSubjectType()==null&&r.getCredentialVersion()==null&&r.getCredentialSnapshot()==null;
    boolean staffZero=explicitStaffZero(s)&&"STAFF".equals(r.getSubjectType())&&Integer.valueOf(0).equals(r.getCredentialVersion())&&r.getCredentialSnapshot()==null;
    if(legacy||staffZero){require(((legacy&&AuthSelectionSnapshots.legacy(s))||staffZero)&&"STAFF".equals(p.getSubjectType()),"UNKNOWN_CREDENTIAL_VERSION");copy.setCredentialVersion(null);copy.setCredentialSnapshot(null);copy.setCardNo(s.getSubjectId());copy.setBadge(s.getBadge());copy.setImageId(s.getImageId());copy.setPersonSnapshot(s.getPersonSnapshot());}
    else {require(!explicitStaffZero(s),"UNKNOWN_CREDENTIAL_VERSION");Credential c=AuthSelectionSnapshots.credential(r.getCredentialVersion(),r.getCredentialSnapshot());copy.setCredentialVersion(1);copy.setCredentialSnapshot(AuthSelectionSnapshots.credential(c));copy.setCardNo(card(c));if(c instanceof PersonCredential){copy.setImageId(((PersonCredential)c).getImageId());copy.setBadge(((PersonCredential)c).getBadge());}}
    fromPhase(copy);
    if(chosen==null)chosen=copy;else require(Objects.equals(chosen.getCredentialVersion(),copy.getCredentialVersion())&&Objects.equals(chosen.getCredentialSnapshot(),copy.getCredentialSnapshot())&&eq(chosen.getCardNo(),copy.getCardNo())&&Objects.equals(chosen.getBadge(),copy.getBadge())&&Objects.equals(chosen.getImageId(),copy.getImageId())&&Objects.equals(chosen.getPersonSnapshot(),copy.getPersonSnapshot()),"FROZEN_CREDENTIAL_CONFLICT");
    require(usedResources.add(r.getBatchId()+":"+r.getOrdinal()),"FROZEN_RESOURCE_DUPLICATE");
   }require(count>0,"FROZEN_RESOURCE_MISSING");
  }
  require(usedSources.size()==sources.size()&&usedResources.size()==resources.size(),"FROZEN_PROJECTION_EXTRA_ROWS");return chosen;
 }
 /** 员工来源版本0与资源版本0必须成对；历史证据格式不改变凭据版本。 */
 private static boolean explicitStaffZero(SmtAuthSelectionSource s){return "STAFF_AUTH".equals(s.getSourceKind())&&"STAFF".equals(s.getSubjectType())&&Integer.valueOf(0).equals(s.getSnapshotVersion())&&s.getParentKind()==null&&s.getParentRowId()==null;}
 private static boolean sourceMatches(SmtAuthSelectionSource s){String k=AuthSelectionSnapshots.kind(s),t=AuthSelectionSnapshots.subject(s);if(AuthSelectionSnapshots.legacy(s)||explicitStaffZero(s))return true;if(AuthSelectionSnapshots.version(s)!=1)return false;return ("STAFF_AUTH".equals(k)&&"STAFF".equals(t))||("VEHICLE_APPLY".equals(k)&&"VEHICLE".equals(t))||eq(k,t)&&Arrays.asList("VISITOR","VISITOR_FELLOW","VISITOR_VEHICLE","ADMITTANCE_FELLOW","ADMITTANCE_VEHICLE").contains(k);}
 private static void validate(SmtAuthTransportPhase p,Credential c){
  text(p.getSubjectType(),32,true);text(p.getSubjectId(),128,true);text(p.getDeviceId(),128,true);text(p.getResourceId(),64,true);text(p.getInstanceId(),128,true);
  require(Arrays.asList("ADD","DELETE").contains(p.getAction())&&Arrays.asList("DIRECT","ISC").contains(p.getAccessType())&&p.getParkId()!=null&&p.getParkId()>0,"FROZEN_COORDINATES_MISSING");
  text(card(c),128,true);require(eq(card(c),p.getCardNo()),"FROZEN_CARD_MISMATCH");
  if(c instanceof PersonCredential){PersonCredential x=(PersonCredential)c;require("PERSON".equals(p.getResourceType())&&"FACE".equals(p.getCredentialChannel())&&Objects.equals(x.getTaskDeviceType(),1)&&Objects.equals(x.getTaskServiceType(),number(p.getServiceType())),"FROZEN_PERSON_CAPABILITY_MISMATCH");
   require(Arrays.asList("STAFF","VISITOR","VISITOR_FELLOW","ADMITTANCE_FELLOW").contains(p.getSubjectType()),"FROZEN_PERSON_SUBJECT_MISMATCH");text(x.getImageId(),128,false);text(x.getPersonId(),128,false);text(x.getBadge(),128,false);
   if(!"STAFF".equals(p.getSubjectType()))require("7".equals(x.getCardType())&&Objects.equals(x.getTaskServiceType(),"ADMITTANCE_FELLOW".equals(p.getSubjectType())?6:3),"FROZEN_PERSON_CAPABILITY_MISMATCH");
  }else if(c instanceof VehicleCredential){VehicleCredential x=(VehicleCredential)c;require("VEHICLE".equals(p.getResourceType())&&"PLATE".equals(p.getCredentialChannel())&&Objects.equals(x.getTaskDeviceType(),2)&&Objects.equals(x.getTaskServiceType(),number(p.getServiceType())),"FROZEN_VEHICLE_CAPABILITY_MISMATCH");
   require(Arrays.asList("VEHICLE","VISITOR_VEHICLE","ADMITTANCE_VEHICLE").contains(p.getSubjectType()),"FROZEN_VEHICLE_SUBJECT_MISMATCH");require(Objects.equals(x.getTaskServiceType(),"VEHICLE".equals(p.getSubjectType())?1:"VISITOR_VEHICLE".equals(p.getSubjectType())?4:6),"FROZEN_VEHICLE_CAPABILITY_MISMATCH");require("0".equals(x.getCardType())||"1".equals(x.getCardType()),"MISSING_FROZEN_CARD_TYPE");text(x.getPlate(),128,true);text(x.getPersonId(),128,false);
  }else throw new IllegalArgumentException("UNKNOWN_CREDENTIAL_KIND");
 }
 static String card(Credential c){return c instanceof PersonCredential?((PersonCredential)c).getTaskCardNo():((VehicleCredential)c).getTaskCardNo();}
 static void text(String s,int max,boolean required){require((!required||s!=null&&!s.trim().isEmpty())&&(s==null||s.getBytes(StandardCharsets.UTF_8).length<=max),"FROZEN_CREDENTIAL_TOO_LONG_OR_MISSING");}
 private static boolean sameEpoch(java.time.LocalDateTime t,Long seconds){return t==null?seconds==null||seconds==0:Objects.equals(t.toEpochSecond(java.time.ZoneOffset.UTC),seconds);}
 private static Integer number(String s){require(s!=null&&s.matches("[0-9]+"),"FROZEN_SERVICE_TYPE_MISSING");try{return Integer.valueOf(s);}catch(NumberFormatException e){throw new IllegalArgumentException("FROZEN_SERVICE_TYPE_TOO_LONG");}}
 private static String sourceKey(SmtAuthSelectionSource s){return s.getBatchId()+":"+s.getOrdinal();}
 private static boolean eq(Object a,Object b){return a!=null&&a.equals(b);}
 private static void require(boolean b,String reason){if(!b)throw new IllegalArgumentException(reason);}
}
