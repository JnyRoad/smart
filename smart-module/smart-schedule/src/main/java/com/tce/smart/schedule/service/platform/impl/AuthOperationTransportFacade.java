package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.*;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget;
import com.tce.smart.platform.core.dto.authtransport.AuthTransport.Run;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.impl.AuthOperationTransportService;
import com.tce.smart.platform.core.service.impl.AuthOperationTransportPolicy;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** 三条独立预算通道，禁止事务内HTTP；每组最多200目标，每次明细只拉一页。 */
@Service
@Transactional(propagation=Propagation.NEVER)
public class AuthOperationTransportFacade {
 private final AuthOperationTransportService transport;private final RemoteDispatcherService remote;private final SmtImageService images;
 private final AuthOperationSchedulerProperties routing;
 @Value("${smart.org.xc-hpo-index-code:}") private String xcOrg;
 @Value("${smart.org.hf-index-code:}") private String hfOrg;
 @Value("${smart.xc-park-id:0}") private Integer xcPark;
 public AuthOperationTransportFacade(AuthOperationTransportService transport,@org.springframework.beans.factory.annotation.Qualifier("authOperationRemoteDispatcher") RemoteDispatcherService remote,SmtImageService images,AuthOperationSchedulerProperties routing) {
  this.transport=transport;this.remote=remote;this.images=images;this.routing=routing;
 }
 public Run submit(int park,String instance,List<AuthOperationClaimedTarget> claims,int httpBudget) {
  Budget budget=new Budget(httpBudget);require(claims!=null&&claims.size()<=200,"领取目标上限200");String access=access(park,instance);
  List<SmtAuthTransportPhase> all=new ArrayList<>();boolean held=false;
  for(AuthOperationClaimedTarget c:claims){require(access.equals(c.getAccessType()),"领取目标接入类型不符");
   try {String reuse=transport.reuseBeforePrepare(park,c);if("REUSE_APPLIED".equals(reuse))continue;
    if(Arrays.asList("BLOCKED","REUSE_PENDING").contains(reuse)){transport.deferClaim(park,c);continue;}
    SmtAuthTransportPhase p=transport.prepare(park,instance,c);if(AuthOperationTransportPolicy.maySend(p))all.add(p);}
   catch(AuthOperationTransportService.WaitingForOwner e){transport.deferClaim(park,c);}
   catch(IllegalArgumentException e){transport.verifyClaim(park,c,verificationReason(e));held=true;}
  }
  if(all.isEmpty()&&held)return run("VERIFYING",0,budget,null,1);
  return executePending(park,instance,access,all,budget);
 }
 public Run resumePrepared(int park,String instance,String priority,Long after,int limit,int httpBudget) {
  String access=access(park,instance);bounded(limit);transport.expireIntents(park,instance,limit);Budget budget=new Budget(httpBudget);
  return executePending(park,instance,access,transport.prepared(park,instance,priority,after,limit),budget);
 }
 private Run executePending(int park,String instance,String access,List<SmtAuthTransportPhase> all,Budget budget) {
  int processed=0;String outcome="IDLE";
  List<SmtAuthTransportPhase> ready=new ArrayList<>();for(SmtAuthTransportPhase p:all)if(transport.resumeReady(park,instance,p.getId()))ready.add(p);else outcome="VERIFYING";
  all=ready;
  if("DIRECT".equals(access)) {
   for(SmtAuthTransportPhase p:all){if(!budget.available())break;
    Object payload=directPayload(p);if(payload==null){transport.block(park,instance,p.getId(),"MISSING_FROZEN_IMAGE");outcome="VERIFYING";continue;}
    List<SmtAuthTransportPhase> started=safeBegin(park,instance,Collections.singletonList(p),null);if(started.isEmpty()){outcome="VERIFYING";continue;}SmtAuthTransportPhase sending=started.get(0);
    try {Result<String> result=dispatch(park,sending.getRequestKey(),"DELETE".equals(p.getAction())?EventEnum.DEVICE_DELETE_CARD:EventEnum.DEVICE_ADD_CARD,payload,p.getDeviceId(),budget);
     if(result!=null&&result.isSuccess()){transport.accepted(park,instance,ids(started),p.getSerialNo());outcome="WAITING_CONFIRM";}
     else {transport.unknown(park,instance,ids(started),"DIRECT_RESPONSE_UNKNOWN");outcome="UNKNOWN";}
    }catch(Exception ex){transport.unknown(park,instance,ids(started),"DIRECT_EXCEPTION_UNKNOWN");outcome="UNKNOWN";}processed++;
   }
  } else if(!all.isEmpty()&&budget.available()) {
   List<SmtAuthTransportPhase> unresolved=new ArrayList<>(),personReady=new ArrayList<>();
   for(SmtAuthTransportPhase p:all){
    if("ADD".equals(p.getAction())){com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity identity=transport.preparePersonIdentity(park,instance,p.getId(),Objects.equals(xcPark,park)?xcOrg:hfOrg);
     require(identity!=null,"人员协调结果缺失");
     if("WAITING_PERSON".equals(identity.getOutcome())){outcome="WAITING_PERSON";continue;}
     if("VERIFYING".equals(identity.getOutcome())){outcome="VERIFYING";continue;}
     if(present(identity.getPersonId())){require(!present(p.getPersonId())||p.getPersonId().equals(identity.getPersonId()),"冻结身份不可替换");p.setPersonId(identity.getPersonId());}
    }
    personReady.add(p);SmtAuthTransportPhase asset=transport.acceptedAsset(park,instance,p.getId(),"ISC_PERSON");
    if(asset!=null){if(present(p.getPersonId()))require(p.getPersonId().equals(asset.getExternalId()),"持久身份冲突");p.setPersonId(asset.getExternalId());}
    if(!present(p.getPersonId()))unresolved.add(p);
   }
   all=personReady;
   Map<String,JSONObject> people=unresolved.isEmpty()?Collections.emptyMap():people(park,unresolved,budget);if(people==null)return run("BACKOFF",0,budget,null,1);
   Map<String,String> resolved=new HashMap<>();Map<String,List<SmtAuthTransportPhase>> groups=new LinkedHashMap<>();
   for(SmtAuthTransportPhase p:all) {try {
    if(!present(p.getBadge())){transport.block(park,instance,p.getId(),"MISSING_FROZEN_BADGE");outcome="VERIFYING";continue;}
    String assetKey=p.getBadge()+":"+p.getAction()+":"+String.valueOf(p.getImageId());
    String person=present(p.getPersonId())?p.getPersonId():resolved.get(assetKey);
    if(person==null){JSONObject found=people.get(p.getBadge());
     if(found!=null&&Boolean.TRUE.equals(found.getBool("ambiguous"))){transport.block(park,instance,p.getId(),"AMBIGUOUS_ISC_IDENTITY");outcome="VERIFYING";continue;}
     if(found!=null&&present(found.getStr("personId"))){transport.block(park,instance,p.getId(),"ISC_IDENTITY_UNPROVEN");outcome="VERIFYING";continue;}
     if("ADD".equals(p.getAction()))person=ensurePersonAndFace(park,instance,p,found,person,budget);
     if(present(person))resolved.put(assetKey,person);
    }
    if(present(p.getPersonId())&&"ADD".equals(p.getAction()))person=ensurePersonAndFace(park,instance,p,people.get(p.getBadge()),person,budget);
    if(!present(person)){if(budget.available())transport.block(park,instance,p.getId(),"ISC_IDENTITY_OR_ASSET_UNVERIFIED");outcome="VERIFYING";continue;}
    p.setPersonId(person);groups.computeIfAbsent(AuthOperationTransportPolicy.groupKey(p),k->new ArrayList<>()).add(p);
   }catch(AuthOperationTransportService.PhaseRejected e){
    if(e.isContended())outcome="WAITING_ASSET";
    else if(e.isConfiguration()){require(Objects.equals(e.getPhaseId(),p.getId()),"配置核验归属不符");transport.rejectPrepared(park,instance,p.getId(),e.getMessage());outcome="VERIFYING";}
    else outcome=transport.rejectAsset(park,instance,e.getPhaseId(),p.getId(),e.getMessage())?"VERIFYING":"WAITING_ASSET";
   }
   }
   for(List<SmtAuthTransportPhase> group:groups.values()) {
    if(!budget.available())break;
    Map<Long,String> personIds=new HashMap<>();for(SmtAuthTransportPhase p:group)personIds.put(p.getId(),p.getPersonId());
    List<SmtAuthTransportPhase> started=safeBegin(park,instance,group,personIds);if(started.isEmpty()){outcome="VERIFYING";continue;}SmtAuthTransportPhase p=started.get(0);
    Map<String,Object> data=new HashMap<>();data.put("resourceInfos",Collections.singletonList(resource(p)));data.put("personDatas",Collections.singletonList(personData(started)));
    data.put("startTime",iso(p.getStartTime()));data.put("endTime",iso(p.getOverTime()));
    try {Result<String> result=dispatch(park,p.getRequestKey(),"DELETE".equals(p.getAction())?EventEnum.ISC_AUTH_CONFIG_DEL:EventEnum.ISC_AUTH_CONFIG_ADD,data,null,budget);
     String external=external(result);if(present(external)){transport.accepted(park,instance,ids(started),external);outcome="WAITING_CONFIG";}else{transport.unknown(park,instance,ids(started),"CONFIG_RESPONSE_UNKNOWN");outcome="UNKNOWN";}
    }catch(Exception ex){transport.unknown(park,instance,ids(started),"CONFIG_EXCEPTION_UNKNOWN");outcome="UNKNOWN";}processed+=started.size();
   }
  }
  return run(outcome,processed,budget,all.isEmpty()?null:all.get(all.size()-1).getId(),1);
 }
 public Run submitPreparedExact(int park,String instance,List<Long> ids,int httpBudget){String access=access(park,instance);return executePending(park,instance,access,transport.exactPhases(park,instance,ids,"ISC".equals(access)?"ISC_CONFIG":"DIRECT_SEND","PREPARED"),new Budget(httpBudget));}
 public Run advanceConfigExact(int park,String instance,List<Long> ids,int httpBudget){validate(park,instance,"ISC");return advanceConfigRows(park,instance,null,ids.size(),httpBudget,transport.exactPhases(park,instance,ids,"ISC_CONFIG","ACCEPTED"),false);}
 public Run downloadExact(int park,String instance,List<Long> ids,int httpBudget){validate(park,instance,"ISC");Budget budget=new Budget(httpBudget);String outcome=downloadRows(park,instance,transport.exactPhases(park,instance,ids,"ISC_DOWNLOAD","PREPARED"),budget);return run(outcome,0,budget,null,1);}
 public Run readReceiptExact(int park,String instance,List<Long> ids,int httpBudget){validate(park,instance,"ISC");return readReceiptRows(park,instance,null,ids.size(),httpBudget,transport.exactPhases(park,instance,ids,"ISC_DOWNLOAD","ACCEPTED"));}
 public Run advanceConfig(int park,String instance,Long after,int limit,int httpBudget) {return advanceConfigRows(park,instance,after,limit,httpBudget,transport.scan(park,instance,"ISC_CONFIG","ACCEPTED",after,limit),true);}
 private Run advanceConfigRows(int park,String instance,Long after,int limit,int httpBudget,List<SmtAuthTransportPhase> rows,boolean autoDownload) {
  validate(park,instance,"ISC");bounded(limit);transport.expireIntents(park,instance,limit);Budget budget=new Budget(httpBudget);int processed=0;Long cursor=null;String outcome="IDLE";
  Set<String> visited=new HashSet<>();
  for(SmtAuthTransportPhase p:rows) {
   if(!budget.available())break;if(!visited.add(p.getRequestKey())){cursor=p.getId();continue;}
   List<SmtAuthTransportPhase> group=transport.group(p);
   Result<String> response=dispatch(park,random(),EventEnum.ISC_AUTH_CONFIG_PROCESS_GET,Collections.singletonMap("taskId",p.getExternalId()),null,budget);
   if(!ok(response)){outcome="BACKOFF";cursor=cursor==null?after:cursor;break;}JSONObject obj=JSONUtil.parseObj(response.getData());
   if(!Boolean.TRUE.equals(obj.getBool("isFinished"))){outcome="WAITING_CONFIG";cursor=p.getId();continue;}
   if(!Boolean.TRUE.equals(obj.getBool("isConfigFinished"))||obj.getInt("failedNum",0)>0||obj.getInt("successedNum",0)<=0){for(SmtAuthTransportPhase x:group)transport.block(park,instance,x.getId(),"CONFIG_RESULT_UNVERIFIED");outcome="VERIFYING";cursor=p.getId();continue;}
   transport.prepareDownload(park,instance,ids(group));processed+=group.size();cursor=p.getId();
  }
  if(autoDownload&&budget.available())outcome=downloadRows(park,instance,transport.scan(park,instance,"ISC_DOWNLOAD","PREPARED",null,limit),budget);
  return run(outcome,processed,budget,cursor,1);
 }
 private String downloadRows(int park,String instance,List<SmtAuthTransportPhase> rows,Budget budget){String outcome="IDLE";
  Map<String,List<SmtAuthTransportPhase>> ready=new LinkedHashMap<>();
  for(SmtAuthTransportPhase p:rows)if(transport.resumeReady(park,instance,p.getId()))ready.computeIfAbsent(AuthOperationTransportPolicy.groupKey(p),k->new ArrayList<>()).add(p);
  for(List<SmtAuthTransportPhase> group:ready.values()){
   if(!budget.available())break;Map<Long,String> persons=new HashMap<>();for(SmtAuthTransportPhase p:group)persons.put(p.getId(),p.getPersonId());
   List<SmtAuthTransportPhase> started=safeBegin(park,instance,group,persons);if(started.isEmpty()){outcome="VERIFYING";continue;}SmtAuthTransportPhase p=started.get(0);
   Map<String,Object> data=new HashMap<>();data.put("taskType",5);data.put("resourceInfos",Collections.singletonList(resource(p)));
   try {String external=external(dispatch(park,p.getRequestKey(),EventEnum.ISC_AUTH_CONFIG_DOWN,data,null,budget));
    if(present(external)){transport.accepted(park,instance,ids(started),external);outcome="WAITING_CONFIRM";}else{transport.unknown(park,instance,ids(started),"DOWNLOAD_RESPONSE_UNKNOWN");outcome="UNKNOWN";}
   }catch(Exception ex){transport.unknown(park,instance,ids(started),"DOWNLOAD_EXCEPTION_UNKNOWN");outcome="UNKNOWN";}
  }
  return outcome;
 }
 public Run readReceipt(int park,String instance,Long after,int limit,int httpBudget) {return readReceiptRows(park,instance,after,limit,httpBudget,transport.scan(park,instance,"ISC_DOWNLOAD","ACCEPTED",after,limit));}
 private Run readReceiptRows(int park,String instance,Long after,int limit,int httpBudget,List<SmtAuthTransportPhase> rows) {
  validate(park,instance,"ISC");bounded(limit);transport.expireIntents(park,instance,limit);Budget budget=new Budget(httpBudget);int processed=0,nextPage=1;Long cursor=null;String outcome="IDLE";
  Set<String> visited=new HashSet<>();
  for(SmtAuthTransportPhase p:rows) {
   if(!budget.available())break;if(!visited.add(p.getRequestKey())){cursor=p.getId();continue;}
   List<SmtAuthTransportPhase> group=transport.group(p);int page=p.getPageNo()==null?1:p.getPageNo();
   for(SmtAuthTransportPhase member:group)if(!"FINISHED".equals(member.getState()))require(Objects.equals(member.getPageNo(),page),"同请求组明细游标不一致");
   Map<String,Object> data=new HashMap<>();data.put("taskId",p.getExternalId());data.put("resourceInfo",resource(p));data.put("pageNo",page);data.put("pageSize",200);
   Result<String> response=dispatch(park,random(),EventEnum.ISC_TASK_RECORD_DETAIL_GET,data,null,budget);
   if(!ok(response)){outcome="BACKOFF";cursor=cursor==null?after:cursor;break;}JSONObject obj=JSONUtil.parseObj(response.getData());JSONArray list=obj.getJSONArray("list");
   if(list==null||list.isEmpty()){for(SmtAuthTransportPhase member:group)transport.verifyResult(park,instance,member.getId(),"EMPTY_DEVICE_DETAIL_UNVERIFIED");outcome="VERIFYING";cursor=p.getId();continue;}
   if(list.size()>200)throw new IllegalStateException("ISC返回明细超过请求页上限");
   // 每条事件独立短事务；中途失败不推进整组页号，已成功事件可幂等重放。
   for(int i=0;i<list.size();i++){JSONObject row=list.getJSONObject(i);String person=row.getStr("personId");
    for(SmtAuthTransportPhase member:group)if(!"FINISHED".equals(member.getState())&&Objects.equals(person,member.getPersonId())){
     boolean success="0".equals(row.getStr("persondownloadResult"));
     transport.receipt(park,instance,member.getId(),person,p.getDeviceId(),p.getExternalId(),p.getRequestKey()+":"+page+":"+i+":"+member.getAttemptId(),success,"personResult="+row.getStr("persondownloadResult"));processed++;
    }
   }
   Integer total=obj.getInt("total");boolean more=list.size()==200&&(total==null||page*200<total);nextPage=more?page+1:1;
   transport.advancePage(park,instance,ids(group),page,nextPage);outcome=more?"MORE":"WAITING_CONFIRM";cursor=p.getId();
  }
  return run(outcome,processed,budget,cursor,nextPage);
 }
 private Map<String,JSONObject> people(int park,List<SmtAuthTransportPhase> all,Budget budget) {
  Set<String> badges=new LinkedHashSet<>();for(SmtAuthTransportPhase p:all)if(present(p.getBadge()))badges.add(p.getBadge());
  if(badges.isEmpty())return Collections.emptyMap();Map<String,Object> data=new HashMap<>();data.put("paramName","jobNo");data.put("paramValue",badges);
  Result<String> response=dispatch(park,random(),EventEnum.ISC_PERSON_GET,data,null,budget);if(!ok(response))return null;
  JSONArray list=JSONUtil.parseObj(response.getData()).getJSONArray("list");if(list==null||list.size()>200)return null;
  Map<String,JSONObject> map=new HashMap<>();Set<String> ambiguous=new HashSet<>();for(int i=0;i<list.size();i++){JSONObject p=list.getJSONObject(i);String badge=p.getStr("jobNo");if(!badges.contains(badge)||p.getInt("status",0)<0)continue;if(map.put(badge,p)!=null)ambiguous.add(badge);}
  for(String bad:ambiguous){JSONObject marker=new JSONObject();marker.put("ambiguous",true);map.put(bad,marker);}return map;
 }
 private String ensurePersonAndFace(int park,String instance,SmtAuthTransportPhase p,JSONObject found,String person,Budget budget) {
  SmtAuthTransportPhase acceptedPerson=transport.acceptedAsset(park,instance,p.getId(),"ISC_PERSON");
  if(acceptedPerson!=null){require(!present(person)||person.equals(acceptedPerson.getExternalId()),"人员接受证明冲突");return acceptedPerson.getExternalId();}
  SmtAuthTransportPhase acceptedFace=transport.acceptedAsset(park,instance,p.getId(),"ISC_FACE");
  if(acceptedFace!=null){require(present(person)&&person.equals(acceptedFace.getExternalId())&&person.equals(acceptedFace.getPersonId()),"照片接受证明冲突");return person;}
  if(!present(p.getImageId()))return null;
  String image=images.getImageBase64ByCode(p.getImageId());if(!present(image))return null;
  if(!present(person)){
   if(!budget.available()||!present(p.getPersonSnapshot()))return null;JSONObject snapshot=JSONUtil.parseObj(p.getPersonSnapshot());
   String org=Objects.equals(xcPark,park)?xcOrg:hfOrg;if(!present(snapshot.getStr("personName"))||!present(org))return null;
   SmtAuthTransportPhase asset=transport.prepareAsset(park,instance,p.getId(),"ISC_PERSON",org);
   if("ACCEPTED".equals(asset.getState()))return transport.acceptedAsset(park,instance,p.getId(),"ISC_PERSON").getExternalId();
   if("INTENT".equals(asset.getState()))throw new AuthOperationTransportService.PhaseRejected(asset.getId(),"PHASE_ALREADY_CLAIMED");
   if(!AuthOperationTransportPolicy.maySend(asset))return null;
   List<SmtAuthTransportPhase> begun=transport.begin(park,instance,Collections.singletonList(asset.getId()),null);asset=begun.get(0);
   Map<String,Object> data=new HashMap<>();data.put("personId",p.getBadge());data.put("jobNo",p.getBadge());data.put("personName",snapshot.getStr("personName"));data.put("gender",snapshot.getInt("gender",0));data.put("orgIndexCode",asset.getOrgIndexCode());data.put("faces",Collections.singletonList(Collections.singletonMap("faceData",image)));
   try {Result<String> response=dispatch(park,asset.getRequestKey(),EventEnum.ISC_PERSON_ADD,data,null,budget);String id=ok(response)?JSONUtil.parseObj(response.getData()).getStr("personId"):null;
    if(!present(id)){transport.unknown(park,instance,ids(begun),"PERSON_RESPONSE_UNKNOWN");throw new AuthOperationTransportService.PhaseRejected(asset.getId(),"PHASE_ALREADY_CLAIMED");}transport.accepted(park,instance,ids(begun),id);return id;
   }catch(AuthOperationTransportService.PhaseRejected waiting){throw waiting;}catch(Exception e){transport.unknown(park,instance,ids(begun),"PERSON_EXCEPTION_UNKNOWN");throw new AuthOperationTransportService.PhaseRejected(asset.getId(),"PHASE_ALREADY_CLAIMED");}
  }
  JSONArray photos=found==null?null:found.getJSONArray("personPhoto");if(photos!=null&&!photos.isEmpty())return person;
  if(!budget.available())return null;SmtAuthTransportPhase asset=transport.prepareAsset(park,instance,p.getId(),"ISC_FACE",null);
  if("ACCEPTED".equals(asset.getState())){SmtAuthTransportPhase proof=transport.acceptedAsset(park,instance,p.getId(),"ISC_FACE");require(proof!=null&&person.equals(proof.getExternalId())&&person.equals(proof.getPersonId()),"照片接受证明冲突");return person;}
  if("INTENT".equals(asset.getState()))throw new AuthOperationTransportService.PhaseRejected(asset.getId(),"PHASE_ALREADY_CLAIMED");
  if(!AuthOperationTransportPolicy.maySend(asset))return null;
  List<SmtAuthTransportPhase> begun=transport.begin(park,instance,Collections.singletonList(asset.getId()),Collections.singletonMap(asset.getId(),person));asset=begun.get(0);
  Map<String,Object> data=new HashMap<>();data.put("personId",person);data.put("faceData",image);
  try {Result<String> response=dispatch(park,asset.getRequestKey(),EventEnum.ISC_FACE_ADD,data,null,budget);if(!ok(response)){transport.unknown(park,instance,ids(begun),"FACE_RESPONSE_UNKNOWN");return null;}
   transport.accepted(park,instance,ids(begun),person);return person;
  }catch(Exception e){transport.unknown(park,instance,ids(begun),"FACE_EXCEPTION_UNKNOWN");return null;}
 }
 private List<SmtAuthTransportPhase> safeBegin(int park,String instance,List<SmtAuthTransportPhase> group,Map<Long,String> people){
  List<SmtAuthTransportPhase> remaining=new ArrayList<>(group);
  while(!remaining.isEmpty()){
   try{return transport.begin(park,instance,ids(remaining),people);}
   catch(AuthOperationTransportService.PhaseRejected e){
    boolean removed=remaining.removeIf(p->Objects.equals(p.getId(),e.getPhaseId()));
    if(!removed)throw e;
    // 仅PREPARED可被隔离，竞争者已写入的INTENT或ACCEPTED不会被覆盖。
    if(!e.isContended())transport.rejectPrepared(park,instance,e.getPhaseId(),e.getMessage());
   }
  }return Collections.emptyList();
 }

 private Object directPayload(SmtAuthTransportPhase p) {
  if("DELETE".equals(p.getAction())){CardDelDTO d=new CardDelDTO();d.setCardNo(p.getCardNo());d.setDeviceCode(p.getDeviceId());d.setReqId(Integer.valueOf(p.getTaskId()));d.setSerialNo(p.getSerialNo());return d;}
  if(!present(p.getImageId()))return null;String image=images.getImageBase64ByCode(p.getImageId());if(!present(image))return null;
  CardDTO d=new CardDTO();d.setCardNo(p.getCardNo());d.setDeviceCode(p.getDeviceId());d.setReqId(Integer.valueOf(p.getTaskId()));d.setSerialNo(p.getSerialNo());d.setFaceImage(image);d.setCardType(1);
  if(present(p.getPersonSnapshot()))d.setPersonName(JSONUtil.parseObj(p.getPersonSnapshot()).getStr("personName"));
  CardDTO.CardValid valid=new CardDTO.CardValid();valid.setStartTime(p.getStartTime());valid.setEndTime(p.getOverTime());d.setValidTime(valid);return d;
 }
 private Result<String> dispatch(int park,String request,EventEnum event,Object data,String device,Budget budget){budget.use();DispatcherDTO<Object> dto=new DispatcherDTO<>();dto.setParkId(park);dto.setDeviceId(device);dto.setEventId(request);dto.setEventType(event.getCode());dto.setData(data);return remote.dispatch(dto,SecurityConstants.FROM_IN);}
 private String access(int park,String instance){for(AuthOperationSchedulerProperties.Instance i:routing.getInstances())if(instance.equals(i.getId())){validate(park,instance,i.getAccessType());return i.getAccessType();}throw new IllegalArgumentException("未知接入实例");}
 private void validate(int park,String instance,String access){require(Objects.equals(routing.resolve(park,access).getId(),instance),"园区不属于接入实例");}
 private static Map<String,Object> resource(SmtAuthTransportPhase p){Map<String,Object> r=new HashMap<>();r.put("resourceIndexCode",p.getDeviceId());r.put("resourceType","acsDevice");r.put("channelNos",p.getChannelNo()==null?Collections.singletonList(1):Collections.singletonList(p.getChannelNo()));return r;}
 private static Map<String,Object> personData(List<SmtAuthTransportPhase> phases){Map<String,Object> d=new HashMap<>();d.put("personDataType","person");d.put("indexCodes",phases.stream().map(SmtAuthTransportPhase::getPersonId).distinct().collect(Collectors.toList()));return d;}
 private static List<Long> ids(List<SmtAuthTransportPhase> phases){return phases.stream().map(SmtAuthTransportPhase::getId).collect(Collectors.toList());}
 private static boolean ok(Result<String> r){return r!=null&&r.isSuccess()&&present(r.getData());}
 private static String external(Result<String> r){return ok(r)?JSONUtil.parseObj(r.getData()).getStr("taskId"):null;}
 private static String iso(Long seconds){return Instant.ofEpochSecond(seconds==null?0:seconds).atOffset(ZoneOffset.ofHours(8)).toString();}
 private static String random(){return UUID.randomUUID().toString().replace("-","");}
 private static boolean present(String s){return s!=null&&!s.trim().isEmpty();}
 private static String verificationReason(IllegalArgumentException e){String message=String.valueOf(e.getMessage());
  if(message.contains("UNSUPPORTED_SOURCE"))return "UNSUPPORTED_SOURCE";
  if(message.contains("MULTI_WINDOW_UNSUPPORTED"))return "MULTI_WINDOW_UNSUPPORTED";
  if(message.contains("CHANNEL")||message.contains("设备"))return "DEVICE_COORDINATE_UNVERIFIED";
  if(message.contains("IDENTITY")||message.contains("身份")||message.contains("凭据"))return "FROZEN_IDENTITY_CONFLICT";
  return "FROZEN_PROJECTION_UNVERIFIED";
 }
 private static void bounded(int n){require(n>0&&n<=200,"分页上限200");}
 private static void require(boolean ok,String s){if(!ok)throw new IllegalArgumentException(s);}
 private static Run run(String outcome,int processed,Budget budget,Long cursor,int page){return Run.builder().outcome(outcome).processed(processed).httpUsed(budget.used).nextCursor(cursor).nextPage(page).build();}
 private static class Budget {final int max;int used;Budget(int max){require(max>=0&&max<=10000,"HTTP预算越界");this.max=max;}boolean available(){return used<max;}void use(){require(available(),"HTTP预算耗尽");used++;}}
}
