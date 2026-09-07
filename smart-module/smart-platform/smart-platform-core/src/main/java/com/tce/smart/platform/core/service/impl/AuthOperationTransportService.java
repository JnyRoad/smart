package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.*;
import java.util.*;

/** 接入本库短事务。外部请求只能由事务外的调度适配器执行。 */
@Service
public class AuthOperationTransportService {
 private final AuthOperationTransportMapper phases;
 @org.springframework.beans.factory.annotation.Autowired private AuthOperationPersonOwnerMapper personOwners;
 private final AuthOperationWorkflowService workflow;
 private final AuthOperationWorkflowMapper bindings;
 private final AuthOperationVersionService versions;
 private final SmtAuthOperationTargetMapper targets;
 private final SmtAuthOperationAttemptMapper attempts;
 private final SmtDeviceMapper devices;
 private final SmtDeviceTaskMapper directTasks;
 private final SmtIscDeviceTaskMapper iscTasks;
 private final EmployeeAuthOperationService employee;
 private final AuthOperationProperties properties;
 private final DirectTaskCompletionService directCompletion;
 private final IscTaskCompletionService iscCompletion;
 public AuthOperationTransportService(AuthOperationTransportMapper phases,AuthOperationWorkflowService workflow,
  AuthOperationWorkflowMapper bindings,AuthOperationVersionService versions,SmtAuthOperationTargetMapper targets,
  SmtAuthOperationAttemptMapper attempts,SmtDeviceMapper devices,SmtDeviceTaskMapper directTasks,SmtIscDeviceTaskMapper iscTasks,
  EmployeeAuthOperationService employee,AuthOperationProperties properties,DirectTaskCompletionService directCompletion,IscTaskCompletionService iscCompletion) {
  this.phases=phases;this.workflow=workflow;this.bindings=bindings;this.versions=versions;this.targets=targets;this.attempts=attempts;
  this.devices=devices;this.directTasks=directTasks;this.iscTasks=iscTasks;this.employee=employee;this.properties=properties;
  this.directCompletion=directCompletion;this.iscCompletion=iscCompletion;
 }
 public static class WaitingForOwner extends IllegalArgumentException {public WaitingForOwner(String outcome){super(outcome);}}
 /** 复用已完成物理证据在任务创建事务外执行，不为合法等待伪造核验失败。 */
 @Transactional(propagation=Propagation.NEVER)
 public String reuseBeforePrepare(int park,AuthOperationClaimedTarget claim){
  SmtAuthOperationTarget t=required(targets.selectById(claim.getTargetId()),"目标不存在");require(Objects.equals(t.getParkId(),park)&&Objects.equals(t.getLeaseToken(),claim.getLeaseToken()),"复用目标归属不符");
  if(t.getLegacyTaskId()!=null&&!phases.byTask(t.getAccessType(),t.getLegacyTaskId()).isEmpty())return "READY";
  List<SmtAuthSourceResource> rows=bindings.targetContributions(t.getId());require(!rows.isEmpty()&&rows.size()<=1000,"缺少有界来源绑定");
  for(SmtAuthSourceResource c:rows){String outcome=workflow.reuse(binding(c,claim.getAttemptId())).getOutcome();if(!"REUSE_APPLIED".equals(outcome))return outcome;}
  for(SmtAuthSourceResource c:rows)workflow.convergeSource(SourceSnapshot.builder().sourceId(c.getSourceCoordId()).generation(c.getSourceGeneration()).sourceRowId(c.getSourceRowId()).fingerprint(c.getSourceFingerprint()).build(),employee);
  workflow.refreshTarget(t.getId());return "REUSE_APPLIED";
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void deferClaim(int park,AuthOperationClaimedTarget claim){
  SmtAuthOperationTarget t=required(targets.selectById(claim.getTargetId()),"目标不存在");require(Objects.equals(t.getParkId(),park),"等待目标园区不符");
  if(phases.deferAttempt(claim)==1)require(phases.deferTarget(claim)==1,"等待目标租约变化");
 }
 /** 即使当前不存在record，仍从服务器冻结投影创建独立删除任务。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public SmtAuthTransportPhase prepare(int park,String instance,AuthOperationClaimedTarget claim) {
  require(properties.enabledForPark(park),"园区未开启新接入");
  SmtAuthOperationTarget t=required(targets.selectById(claim.getTargetId()),"目标不存在");
  SmtAuthOperationAttempt a=required(attempts.selectByIdAndTarget(claim.getAttemptId(),t.getId()),"尝试不存在");
  require(Objects.equals(t.getParkId(),park)&&Objects.equals(a.getLeaseToken(),claim.getLeaseToken())&&Objects.equals(a.getAttemptNo(),claim.getAttemptNo()),"目标园区或尝试租约不符");
  String phase="ISC".equals(t.getAccessType())?"ISC_CONFIG":"DIRECT_SEND";
  SmtAuthTransportPhase old=phases.phase(a.getId(),phase);
  if(old!=null) { require(instance.equals(old.getInstanceId()),"历史接入实例不可变");return old; }
  List<SmtAuthSourceResource> contributions=bindings.targetContributions(t.getId());
  require(!contributions.isEmpty()&&contributions.size()<=1000,"目标缺少有界历史来源绑定");
  SmtAuthSourceResource c=contributions.get(0);
  Binding b=binding(c,a.getId());ResourceDecision d=versions.currentDesired(c.getResourceCoordId());
  require("STAFF".equals(d.getResource().getSubjectType())&&"PERSON".equals(t.getResourceType()),"UNSUPPORTED_SOURCE：仅支持可信员工人员投影");
  List<SmtAuthSelectionSource> frozen=employee.sourcesForTarget(t.getId());
  require(!frozen.isEmpty(),"缺少员工冻结来源");
  SmtAuthSelectionSource source=frozen.get(0);
  for(SmtAuthSelectionSource s:frozen)require(Objects.equals(source.getSubjectId(),s.getSubjectId())&&Objects.equals(source.getBadge(),s.getBadge())&&Objects.equals(source.getImageId(),s.getImageId())&&Objects.equals(source.getPersonSnapshot(),s.getPersonSnapshot()),"冻结凭据冲突，必须核验");
  SmtDevice device=required(devices.selectById(t.getDeviceId()),"设备不存在");
  require(Objects.equals(device.getParkId(),park),"设备园区不符");
  require(Objects.equals(t.getAccessType(),Integer.valueOf(1).equals(device.getIsSync())?"ISC":"DIRECT"),"设备接入类型变化，必须核验");
  require(!d.isRequiresMultipleWindows(),"MULTI_WINDOW_UNSUPPORTED");
  require("FACE".equals(d.getResource().getCredentialChannel()),"UNSUPPORTED_CREDENTIAL_CHANNEL");
  require(!"ISC".equals(t.getAccessType())||(device.getChannelNo()!=null&&device.getChannelNo()>0),"ISC_DEVICE_CHANNEL_UNVERIFIED");
  SmtAuthTransportPhase p=new SmtAuthTransportPhase();p.setId(IdWorker.getId());p.setTargetId(t.getId());p.setAttemptId(a.getId());p.setAttemptNo(a.getAttemptNo());p.setLeaseToken(a.getLeaseToken());
  p.setSourceId(b.getSourceId());p.setSourceGeneration(b.getSourceGeneration());p.setResourceId(b.getResourceId());p.setResourceGeneration(b.getResourceGeneration());p.setRequestId(b.getRequestId());
  p.setParkId(park);p.setInstanceId(instance);p.setAccessType(t.getAccessType());p.setPhase(phase);p.setState("PREPARED");p.setSerialNo(UUID.randomUUID().toString().replace("-",""));
  p.setDeviceId(t.getDeviceId());p.setSubjectId(t.getSubjectId());p.setSubjectType(t.getSubjectType());p.setAction(t.getAction());p.setResourceType(t.getResourceType());
  p.setServiceType(d.getResource().getServiceType());p.setCredentialChannel(d.getResource().getCredentialChannel());p.setCardNo(t.getSubjectId());p.setBadge(source.getBadge());p.setImageId(source.getImageId());p.setPersonSnapshot(source.getPersonSnapshot());
  if("ISC".equals(p.getAccessType())){List<String> identities=phases.knownPersons(p);require(identities.size()<=1,"ISC_IDENTITY_CONFLICT");if(!identities.isEmpty())p.setPersonId(identities.get(0));}
  if(!"DELETE".equals(t.getAction())){
   require(t.getValidFrom()!=null&&t.getValidTo()!=null,"权限时间窗缺失");
   p.setStartTime(t.getValidFrom().toEpochSecond(ZoneOffset.UTC));p.setOverTime(t.getValidTo().toEpochSecond(ZoneOffset.UTC));
  }
  p.setChannelNo(device.getChannelNo());p.setPageNo(1);p.setCreateTime(now());p.setUpdateTime(now());
  require(p.getServiceType()!=null&&p.getServiceType().matches("[0-9]+"),"冻结业务类型缺失");
  if("ISC".equals(p.getAccessType())) {SmtIscDeviceTask task=iscTask(p);task.setId(IdWorker.getId());require(iscTasks.insert(task)==1,"ISC任务插入失败");p.setTaskId(String.valueOf(task.getId()));}
  else {SmtDeviceTask task=directTask(p);require(directTasks.insert(task)==1&&task.getId()!=null,"直连任务AUTO插入失败");p.setTaskId(String.valueOf(task.getId()));}
  Prepared prepared=workflow.prepareWithinTransaction(b,submission(p));
  if(!"READY".equals(prepared.getOutcome()))throw new WaitingForOwner(prepared.getOutcome());
  // 一个物理尝试覆盖全部共享来源，后续证据必须能逐来源核对同一真实attempt。
  for(SmtAuthSourceResource shared:contributions)if(!Objects.equals(shared.getSourceCoordId(),b.getSourceId())||!Objects.equals(shared.getRequestId(),b.getRequestId())) {
   require(Objects.equals(shared.getResourceCoordId(),b.getResourceId())&&Objects.equals(shared.getResourceGeneration(),b.getResourceGeneration()),"共享来源跨资源或代次");
   versions.bindAttempt(binding(shared,a.getId()));
  }
  require(phases.insert(p)==1,"阶段插入失败");return p;
 }
 /** PERSON协调始终先锁稳定owner，再核主体/资源；外调必须在此事务提交后。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity preparePersonIdentity(int park,String instance,Long configId,String org){
  return personIdentity(owned(configId,park,instance),org);
 }
 private com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity personIdentity(SmtAuthTransportPhase c,String requestedOrg){
  require("ISC_CONFIG".equals(c.getPhase())&&"ISC".equals(c.getAccessType())&&"ADD".equals(c.getAction()),"人员协调仅适用于ISC新增");
  if(phases.routeTableInstalled()!=1||phases.routeMatches(c)!=1)return personVerify(c,"ISC_INSTANCE_SCOPE_UNVERIFIED");
  String key=personHash(personTuple(c.getInstanceId(),c.getSubjectType(),c.getSubjectId()));SmtAuthPersonOwner owner=personOwners.lock(key);
  if(owner!=null)require(Objects.equals(owner.getInstanceId(),c.getInstanceId())&&Objects.equals(owner.getSubjectType(),c.getSubjectType())&&Objects.equals(owner.getSubjectId(),c.getSubjectId()),"人员协调自然键冲突");
  if(owner==null&&present(c.getPersonId()))return personResult("KNOWN_LOCAL",c.getPersonId(),null,null);
  if(owner!=null&&"UNKNOWN".equals(owner.getState()))return personResult("WAITING_PERSON",null,owner.getOwnerPhaseId(),owner.getReason());
  if(owner!=null&&"VERIFYING".equals(owner.getState()))return personVerify(c,owner.getReason());
  if(owner!=null&&present(c.getPersonId())){
   if("ACCEPTED".equals(owner.getState())&&Objects.equals(owner.getPersonId(),c.getPersonId()))return personResult("KNOWN_LOCAL",c.getPersonId(),null,null);
   if(!"ACCEPTED".equals(owner.getState()))return personResult("WAITING_PERSON",null,owner.getOwnerPhaseId(),"PERSON_OWNER_PENDING");
   return personVerify(c,"ISC_IDENTITY_CONFLICT");
  }
  List<SmtAuthSelectionSource> sources=employee.sourcesForTarget(c.getTargetId());String operation=null;
  if(sources.isEmpty()||sources.size()>1000)return personVerify(c,"PERSON_OPERATION_SCOPE_AMBIGUOUS");
  for(SmtAuthSelectionSource source:sources){if(!present(source.getOperationKey())||!Objects.equals(c.getSubjectId(),source.getSubjectId())||(operation!=null&&!operation.equals(source.getOperationKey())))return personVerify(c,"PERSON_OPERATION_SCOPE_AMBIGUOUS");operation=source.getOperationKey();}
  String org=present(c.getOrgIndexCode())?c.getOrgIndexCode():requestedOrg;
  if(!present(c.getBadge())||!present(c.getPersonSnapshot())||!present(org))return personVerify(c,"PERSON_FROZEN_IDENTITY_MISSING");
  cn.hutool.json.JSONObject json=cn.hutool.json.JSONUtil.parseObj(c.getPersonSnapshot());String name=json.getStr("personName");Integer gender=json.getInt("gender",0);
  if(!present(name)||gender<0||gender>2)return personVerify(c,"PERSON_FROZEN_IDENTITY_MISSING");
  String canonical=personTuple(c.getBadge(),name,String.valueOf(gender),org);require(canonical.length()<=2048&&operation.length()<=128,"人员协调投影越界");String hash=personHash(canonical);
  if(owner==null){
   SmtAuthPersonOwner fresh=new SmtAuthPersonOwner();fresh.setId(key);fresh.setInstanceId(c.getInstanceId());fresh.setSubjectType(c.getSubjectType());fresh.setSubjectId(c.getSubjectId());fresh.setOperationKey(operation);fresh.setIdentityCanonical(canonical);fresh.setIdentityHash(hash);fresh.setOriginPark(c.getParkId());fresh.setOwnerToken(c.getLeaseToken());fresh.setState("PREPARED");
   try{personOwners.insert(fresh);owner=fresh;}catch(org.springframework.dao.DuplicateKeyException raced){owner=required(personOwners.lock(key),"人员协调竞争结果缺失");}
   if(owner==fresh){List<SmtAuthTransportPhase> history=personOwners.history(c);if(!history.isEmpty()){
    boolean unknown=false;for(SmtAuthTransportPhase old:history)unknown|=Arrays.asList("INTENT","UNKNOWN").contains(old.getState());
    String reason=unknown?"LEGACY_PERSON_UNKNOWN":"LEGACY_PERSON_HISTORY_UNVERIFIED";personOwners.mark(key,"PREPARED",unknown?"UNKNOWN":"VERIFYING",reason);
    return unknown?personResult("WAITING_PERSON",null,null,reason):personVerify(c,reason);
   }}
  }
  require(Objects.equals(owner.getInstanceId(),c.getInstanceId())&&Objects.equals(owner.getSubjectType(),c.getSubjectType())&&Objects.equals(owner.getSubjectId(),c.getSubjectId()),"人员协调自然键冲突");
  if("UNKNOWN".equals(owner.getState()))return personResult("WAITING_PERSON",null,owner.getOwnerPhaseId(),owner.getReason());
  if("VERIFYING".equals(owner.getState()))return personVerify(c,owner.getReason());
  if(!Objects.equals(operation,owner.getOperationKey()))return personVerify(c,"PERSON_OTHER_OPERATION_UNVERIFIED");
  if(!Objects.equals(hash,owner.getIdentityHash())||!Objects.equals(canonical,owner.getIdentityCanonical()))return personVerify(c,"PERSON_FROZEN_IDENTITY_CONFLICT");
  if(owner.getOwnerPhaseId()!=null&&"PREPARED".equals(owner.getState())){
   SmtAuthTransportPhase asset=required(phases.byId(owner.getOwnerPhaseId()),"人员owner阶段缺失");
   try{require("PREPARED".equals(asset.getState()),"人员owner阶段不可恢复");requirePersonParent(asset);assertOwner(asset,required(attempts.selectByIdAndTarget(asset.getAttemptId(),asset.getTargetId()),"人员owner尝试缺失"),required(targets.selectById(asset.getTargetId()),"人员owner目标缺失"));}
   catch(IllegalArgumentException invalid){personOwners.mark(owner.getId(),"PREPARED","VERIFYING","PERSON_OWNER_INVALID");return personVerify(c,"PERSON_OWNER_INVALID");}
  }
  assertOwner(c,required(attempts.selectByIdAndTarget(c.getAttemptId(),c.getTargetId()),"尝试不存在"),required(targets.selectById(c.getTargetId()),"目标不存在"));
  if("ACCEPTED".equals(owner.getState())){
   SmtAuthTransportPhase proof=required(phases.byId(owner.getOwnerPhaseId()),"人员证明缺失");
   try{require("ISC_PERSON".equals(proof.getPhase())&&"ACCEPTED".equals(proof.getState())&&Objects.equals(proof.getExternalId(),owner.getPersonId())&&Objects.equals(proof.getRequestKey(),owner.getRequestKey())&&Objects.equals(proof.getLeaseToken(),owner.getOwnerToken())&&Objects.equals(proof.getSubjectId(),c.getSubjectId())&&Objects.equals(proof.getSubjectType(),c.getSubjectType())&&Objects.equals(proof.getInstanceId(),c.getInstanceId())&&Objects.equals(proof.getParkId(),owner.getOriginPark())&&phases.routeMatches(proof)==1,"人员接受证明归属冲突");
   require(phases.conflictingPerson(c,owner.getPersonId())==0&&phases.ownsPerson(c,owner.getPersonId())==1,"ISC_IDENTITY_CONFLICT");}catch(IllegalArgumentException invalid){return personVerify(c,"PERSON_ACCEPTED_PROOF_UNVERIFIED");}
   require(phases.personBinding(c,operation,hash,org,owner.getPersonId(),proof.getId())==1,"人员证明绑定冲突");return personResult("REUSE_CREATED_IDENTITY",owner.getPersonId(),proof.getId(),null);
  }
  require(phases.personBinding(c,operation,hash,org,null,null)==1,"人员创建投影绑定冲突");c.setPersonOperationKey(operation);c.setPersonIdentityHash(hash);c.setOrgIndexCode(org);
  if(owner.getOwnerPhaseId()==null){SmtAuthTransportPhase asset=newAsset(c,"ISC_PERSON",org);require(personOwners.setPhase(owner.getId(),asset.getId())==1,"人员owner阶段绑定失败");owner.setOwnerPhaseId(asset.getId());}
  SmtAuthTransportPhase asset=required(phases.byId(owner.getOwnerPhaseId()),"人员owner阶段缺失");
  return Objects.equals(asset.getAttemptId(),c.getAttemptId())&&"PREPARED".equals(owner.getState())?personResult("OWNER_NEEDS_LOOKUP",null,asset.getId(),null):personResult("WAITING_PERSON",null,asset.getId(),"PERSON_OWNER_PENDING");
 }
 private com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity personVerify(SmtAuthTransportPhase c,String reason){if("PREPARED".equals(c.getState())&&phases.transition(c.getId(),"PREPARED","VERIFYING",c.getExternalId(),reason)==1){phases.hold(c,reason);phases.holdAttempt(c,reason);}return personResult("VERIFYING",null,null,reason);}
 private static com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity personResult(String result,String person,Long proof,String reason){return com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity.builder().outcome(result).personId(person).proofPhaseId(proof).reason(reason).build();}
 private static String personTuple(String... values){StringBuilder b=new StringBuilder();for(String v:values){require(v!=null,"人员协调键缺失");b.append(v.length()).append(':').append(v);}return b.toString();}
 private static String personHash(String value){try{byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));StringBuilder result=new StringBuilder();for(byte b:bytes)result.append(String.format("%02x",b&255));return result.toString();}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private SmtAuthPersonOwner personOwner(SmtAuthTransportPhase p){return "ISC_PERSON".equals(p.getPhase())?personOwners.lockByPhase(p.getId()):null;}
 /** 批量核验先按稳定键锁完所有owner，之后才允许获取阶段/目标锁。 */
 private void lockPersonOwners(int park,String instance,List<Long> ids){SortedSet<String> keys=new TreeSet<>();for(Long id:ids){SmtAuthTransportPhase p=owned(id,park,instance);if("ISC_PERSON".equals(p.getPhase()))keys.add(personHash(personTuple(p.getInstanceId(),p.getSubjectType(),p.getSubjectId())));}for(String key:keys)personOwners.lock(key);}
 public List<SmtAuthPersonOwner> personReviews(String instance,String after,int limit){require(present(instance)&&limit>0&&limit<=200,"人员核验分页越界");return personOwners.reviews(instance,after,limit);}
 private void requirePersonParent(SmtAuthTransportPhase asset){SmtAuthTransportPhase parent=required(phases.phase(asset.getAttemptId(),"ISC_CONFIG"),"人员owner父配置缺失");require(AuthOperationTransportPolicy.maySend(parent)&&Objects.equals(parent.getTargetId(),asset.getTargetId())&&Objects.equals(parent.getLeaseToken(),asset.getLeaseToken()),"PERSON_OWNER_INVALID");}
 private SmtAuthPersonOwner associatedPersonOwner(SmtAuthTransportPhase p){SmtAuthTransportPhase asset="ISC_PERSON".equals(p.getPhase())?p:phases.phase(p.getAttemptId(),"ISC_PERSON");return asset==null?null:personOwner(asset);}
 private void ownerMatches(SmtAuthPersonOwner owner,SmtAuthTransportPhase p){require(owner!=null&&Objects.equals(owner.getOwnerPhaseId(),p.getId())&&Objects.equals(owner.getOwnerToken(),p.getLeaseToken())&&Objects.equals(owner.getInstanceId(),p.getInstanceId())&&Objects.equals(owner.getSubjectId(),p.getSubjectId())&&Objects.equals(owner.getSubjectType(),p.getSubjectType()),"PERSON_OWNER_UNVERIFIED");}
 private SmtAuthTransportPhase newAsset(SmtAuthTransportPhase c,String kind,String org){SmtAuthTransportPhase p=new SmtAuthTransportPhase();BeanUtils.copyProperties(c,p);p.setId(IdWorker.getId());p.setPhase(kind);p.setState("PREPARED");p.setOrgIndexCode(org);p.setRequestKey(null);p.setExternalId(null);p.setCreateTime(now());require(phases.insert(p)==1,"资产阶段保存失败");return p;}
 /** 只有一个工作者能把整组PREPARED变成INTENT；提交后才可外调。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public List<SmtAuthTransportPhase> begin(int park,String instance,List<Long> ids,Map<Long,String> persons) {
  require(ids!=null&&!ids.isEmpty()&&ids.size()<=200,"单组最多200个目标");String request=UUID.randomUUID().toString().replace("-","");
  for(Long id:ids)if("ISC_PERSON".equals(owned(id,park,instance).getPhase()))require(ids.size()==1,"建人接口只允许一个真实owner");
  List<SmtAuthTransportPhase> result=new ArrayList<>();String key=null;
  for(Long id:ids) {try {SmtAuthTransportPhase p=owned(id,park,instance);require(AuthOperationTransportPolicy.maySend(p),"PHASE_ALREADY_CLAIMED");
   SmtAuthPersonOwner owner=personOwner(p);if("ISC_PERSON".equals(p.getPhase())){require(ids.size()==1,"建人接口只允许一个真实owner");ownerMatches(owner,p);require("PREPARED".equals(owner.getState()),"PHASE_ALREADY_CLAIMED");requirePersonParent(p);}
   SmtAuthOperationAttempt a=required(attempts.selectByIdAndTarget(p.getAttemptId(),p.getTargetId()),"尝试不存在");
   SmtAuthOperationTarget t=required(targets.selectById(p.getTargetId()),"目标不存在");
   assertOwner(p,a,t);
   if("ISC_DOWNLOAD".equals(p.getPhase()))require("CONFIRMED".equals(required(phases.phase(p.getAttemptId(),"ISC_CONFIG"),"缺少配置阶段").getState()),"配置缺少持久完成证据");
   String current=AuthOperationTransportPolicy.groupKey(p);if(key==null)key=current;else require(key.equals(current),"目标不兼容，禁止混组");
   if("ISC".equals(p.getAccessType()))require(phases.routeTableInstalled()==1&&phases.routeMatches(p)==1,"ISC_INSTANCE_SCOPE_UNVERIFIED");
   String person=persons==null?null:persons.get(id);
   if("ISC".equals(p.getAccessType())&&!"ISC_PERSON".equals(p.getPhase()))require(present(person),"缺少可信ISC人员ID");
   if(present(p.getPersonId()))require(Objects.equals(p.getPersonId(),person),"ISC_IDENTITY_CONFLICT");
   if(present(person)){require(phases.conflictingPerson(p,person)==0,"ISC_IDENTITY_CONFLICT");phases.claimPerson(p,person);require(phases.ownsPerson(p,person)==1,"ISC_IDENTITY_CONFLICT");}
   if(owner!=null)require(personOwners.start(owner.getId(),p.getId(),p.getLeaseToken(),request)==1,"PHASE_ALREADY_CLAIMED");
   require(phases.start(id,request,person)==1,"PHASE_ALREADY_CLAIMED");p.setState("INTENT");p.setRequestKey(request);p.setPersonId(person);
   if("DIRECT".equals(p.getAccessType())) {require(phases.associate(p,p.getSerialNo())==1,"真实直连命令关联失败");require(phases.waiting(p)==1,"直连待确认状态冲突");}
   result.add(p);
   }catch(org.springframework.dao.DuplicateKeyException e){throw new PhaseRejected(id,"ISC_IDENTITY_CONFLICT");}
   catch(IllegalArgumentException e){throw new PhaseRejected(id,e.getMessage());}
  }return result;
 }
 /** 事务已整体回滚，调用方只隔离精确失败成员，再批量领取其余成员。 */
 public static final class PhaseRejected extends IllegalArgumentException {
  private final Long phaseId;private final boolean configuration;
  public PhaseRejected(Long phaseId,String reason){this(phaseId,reason,false);}
  private PhaseRejected(Long phaseId,String reason,boolean configuration){super(reason);this.phaseId=phaseId;this.configuration=configuration;}
  public static PhaseRejected configuration(Long configId,String reason){return new PhaseRejected(configId,reason,true);}
  public boolean isConfiguration(){return configuration;}
  public Long getPhaseId(){return phaseId;}
  public boolean isContended(){return "PHASE_ALREADY_CLAIMED".equals(getMessage());}
 }
 /** 只判定无提交意图阶段的首次恢复；不可恢复单项离开恢复页，不影响后续健康目标。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public boolean resumeReady(int park,String instance,Long id){SmtAuthTransportPhase p=owned(id,park,instance);
  if(!AuthOperationTransportPolicy.maySend(p))return false;
  try{assertOwner(p,required(attempts.selectByIdAndTarget(p.getAttemptId(),p.getTargetId()),"尝试不存在"),required(targets.selectById(p.getTargetId()),"目标不存在"));return true;}
  catch(IllegalArgumentException e){phases.transition(id,"PREPARED","VERIFYING",p.getExternalId(),"PREPARED_OWNER_UNVERIFIED");phases.hold(p,"PREPARED_OWNER_UNVERIFIED");phases.holdAttempt(p,"PREPARED_OWNER_UNVERIFIED");return false;}
 }
 private void assertOwner(SmtAuthTransportPhase p,SmtAuthOperationAttempt a,SmtAuthOperationTarget t){
  ResourceDecision r=versions.currentDesired(p.getResourceId());
  require(p.getRequestKey()==null&&Objects.equals(a.getLeaseToken(),p.getLeaseToken())&&Objects.equals(t.getLeaseToken(),p.getLeaseToken())
   &&Objects.equals(t.getOperationVersion(),p.getResourceGeneration())&&Objects.equals(a.getTaskId(),p.getTaskId())
   &&Arrays.asList("EXECUTING","WAITING_CONFIRM","VERIFYING").contains(t.getState())&&Arrays.asList("SUBMITTING","WAITING_CONFIRM","VERIFYING").contains(a.getStatus())
   &&Objects.equals(r.getBlockingAttemptId(),p.getAttemptId())&&r.getGeneration()==p.getResourceGeneration(),"PREPARED_OWNER_UNVERIFIED");
 }
 /** 精确资产接受证明只属于当前尝试及其冻结主体、图片和实例。 */
 public SmtAuthTransportPhase acceptedAsset(int park,String instance,Long configId,String phase){
  SmtAuthTransportPhase c=owned(configId,park,instance),p=phases.phase(c.getAttemptId(),phase);if(p==null||!"ACCEPTED".equals(p.getState()))return null;
  require(Objects.equals(c.getTargetId(),p.getTargetId())&&Objects.equals(c.getSubjectId(),p.getSubjectId())&&Objects.equals(c.getSubjectType(),p.getSubjectType())&&Objects.equals(c.getImageId(),p.getImageId())&&Objects.equals(c.getInstanceId(),p.getInstanceId())&&Objects.equals(c.getParkId(),p.getParkId())&&Objects.equals(c.getTaskId(),p.getTaskId())&&Objects.equals(c.getLeaseToken(),p.getLeaseToken())&&present(p.getRequestKey())&&present(p.getExternalId()),"资产接受证明归属冲突");return p;
 }
 /** 前置配置仅登记配置阶段；最终下载号与attempt、旧任务原子关联。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void accepted(int park,String instance,List<Long> ids,String external) {
  require(present(external),"外部ID不能为空");require(ids.size()>0&&ids.size()<=200,"阶段数量越界");
  lockPersonOwners(park,instance,ids);
  for(Long id:ids) {SmtAuthTransportPhase p=owned(id,park,instance);SmtAuthPersonOwner owner=personOwner(p);
   if("ISC_PERSON".equals(p.getPhase())){ownerMatches(owner,p);require(Objects.equals(owner.getRequestKey(),p.getRequestKey()),"人员响应请求归属冲突");require(phases.conflictingPerson(p,external)==0,"ISC_IDENTITY_CONFLICT");phases.claimPerson(p,external);require(phases.ownsPerson(p,external)==1,"ISC_IDENTITY_CONFLICT");require(personOwners.accepted(owner.getId(),p.getId(),p.getLeaseToken(),p.getRequestKey(),external)==1,"人员接受证明冲突");}
   if("FINISHED".equals(p.getState())&&Objects.equals(external,p.getExternalId()))continue;
   require(Arrays.asList("INTENT","UNKNOWN").contains(p.getState()),"外部响应不属于已发送阶段");
   require(phases.transition(id,p.getState(),"ACCEPTED",external,null)==1,"外部ID或状态冲突");
   if("ISC_DOWNLOAD".equals(p.getPhase())) {
    if(phases.associate(p,external)==1)require(phases.waiting(p)==1,"下载待确认状态冲突");
    require(phases.updateIscTask(p,external)==1,"ISC下载任务关联失败");
   }
  }
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void unknown(int park,String instance,List<Long> ids,String reason) {
  require(ids.size()<=200,"阶段数量越界");lockPersonOwners(park,instance,ids);for(Long id:ids) {SmtAuthTransportPhase p=owned(id,park,instance);
   SmtAuthPersonOwner owner=personOwner(p);if(owner!=null){ownerMatches(owner,p);if("INTENT".equals(owner.getState()))require(personOwners.mark(owner.getId(),"INTENT","UNKNOWN",reason)==1,"人员未知状态冲突");}
   if("INTENT".equals(p.getState())){require(phases.transition(id,"INTENT","UNKNOWN",p.getExternalId(),reason)==1,"未知状态冲突");phases.hold(p,reason);phases.holdAttempt(p,reason);}
  }
 }
 /** 配置已完成只创建待发送下载阶段；已有INTENT/UNKNOWN绝不重建。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public List<SmtAuthTransportPhase> prepareDownload(int park,String instance,List<Long> configIds) {
  require(configIds.size()>0&&configIds.size()<=200,"配置组数量越界");List<SmtAuthTransportPhase> result=new ArrayList<>();
  for(Long id:configIds) {SmtAuthTransportPhase c=owned(id,park,instance);require("ISC_CONFIG".equals(c.getPhase())&&Arrays.asList("ACCEPTED","CONFIRMED").contains(c.getState()),"配置尚未受理");
   if("ACCEPTED".equals(c.getState()))require(phases.transition(c.getId(),"ACCEPTED","CONFIRMED",c.getExternalId(),null)==1,"配置完成证据冲突");
   SmtAuthTransportPhase p=phases.phase(c.getAttemptId(),"ISC_DOWNLOAD");if(p==null) {p=new SmtAuthTransportPhase();BeanUtils.copyProperties(c,p);p.setId(IdWorker.getId());p.setPhase("ISC_DOWNLOAD");p.setState("PREPARED");p.setExternalId(null);p.setRequestKey(null);p.setCreateTime(now());require(phases.insert(p)==1,"下载阶段保存失败");}result.add(p);
  }return result;
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public SmtAuthTransportPhase prepareAsset(int park,String instance,Long configId,String phase,String org) {
  require(Arrays.asList("ISC_PERSON","ISC_FACE").contains(phase),"不支持的资产阶段");
  SmtAuthTransportPhase c=owned(configId,park,instance);require("ISC_CONFIG".equals(c.getPhase())&&"ADD".equals(c.getAction()),"删除不能创建人员资产");
  if("ISC_PERSON".equals(phase)){com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity identity=personIdentity(c,org);SmtAuthTransportPhase own=phases.phase(c.getAttemptId(),phase);if(own!=null&&Arrays.asList("OWNER_NEEDS_LOOKUP","REUSE_CREATED_IDENTITY","KNOWN_LOCAL").contains(identity.getOutcome()))return own;throw PhaseRejected.configuration(c.getId(),"WAITING_PERSON".equals(identity.getOutcome())?"PHASE_ALREADY_CLAIMED":present(identity.getReason())?identity.getReason():"PERSON_OWNER_UNVERIFIED");}
  SmtAuthTransportPhase p=phases.phase(c.getAttemptId(),phase);return p==null?newAsset(c,phase,org):p;
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void rejectPrepared(int park,String instance,Long id,String reason){SmtAuthTransportPhase p=owned(id,park,instance);SmtAuthPersonOwner owner=associatedPersonOwner(p);
  if("PREPARED".equals(p.getState())&&phases.transition(id,"PREPARED","VERIFYING",p.getExternalId(),reason)==1){if(owner!=null)personOwners.mark(owner.getId(),"PREPARED","VERIFYING",reason);phases.hold(p,reason);phases.holdAttempt(p,reason);}
 }
 /** 资产仍由本事务成功隔离时，才允许向同一尝试的父配置传播；竞争输家不影响赢家。 */
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public boolean rejectAsset(int park,String instance,Long assetId,Long configId,String reason){
  SmtAuthTransportPhase asset=owned(assetId,park,instance),config=owned(configId,park,instance);
  require(Arrays.asList("ISC_PERSON","ISC_FACE").contains(asset.getPhase())&&"ISC_CONFIG".equals(config.getPhase())
   &&Objects.equals(asset.getAttemptId(),config.getAttemptId())&&Objects.equals(asset.getTargetId(),config.getTargetId())
   &&Objects.equals(asset.getTaskId(),config.getTaskId())&&Objects.equals(asset.getLeaseToken(),config.getLeaseToken()),"资产与父配置归属不符");
  SmtAuthPersonOwner owner=associatedPersonOwner(asset);
  if(phases.transition(assetId,"PREPARED","VERIFYING",asset.getExternalId(),reason)!=1)return false;
  if(owner!=null)personOwners.mark(owner.getId(),"PREPARED","VERIFYING",reason);
  if(phases.transition(configId,"PREPARED","VERIFYING",config.getExternalId(),reason)!=1)return false;
  phases.hold(config,reason);phases.holdAttempt(config,reason);return true;
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void block(int park,String instance,Long id,String reason) {SmtAuthTransportPhase p=owned(id,park,instance);SmtAuthTransportPhase asset="ISC_CONFIG".equals(p.getPhase())?phases.phase(p.getAttemptId(),"ISC_PERSON"):p;SmtAuthPersonOwner owner=asset==null?null:personOwner(asset);if(owner!=null&&"PREPARED".equals(owner.getState()))personOwners.mark(owner.getId(),"PREPARED","VERIFYING",reason);require(phases.transition(id,p.getState(),"VERIFYING",p.getExternalId(),reason)==1,"核验状态冲突");phases.hold(p,reason);phases.holdAttempt(p,reason);}
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public Received receipt(int park,String instance,Long phaseId,String person,String device,String external,String eventKey,boolean success,String body) {
  SmtAuthTransportPhase p=owned(phaseId,park,instance);
  require(Objects.equals(device,p.getDeviceId()),"设备不匹配");
  boolean isc="ISC".equals(p.getAccessType());
  require(!isc||("ISC_DOWNLOAD".equals(p.getPhase())&&Objects.equals(person,p.getPersonId())),"ISC人员或阶段不匹配");
  require(external!=null&&!external.trim().isEmpty()&&Objects.equals(external,isc?p.getExternalId():p.getSerialNo()),"外部回执号不匹配");
  // 与工作流一致先锁主体/资源，再补旧attempt的真实外部号，避免倒序锁。
  versions.currentDesired(p.getResourceId());
  if(isc){SmtAuthOperationAttempt historical=required(attempts.selectByIdAndTarget(p.getAttemptId(),p.getTargetId()),"历史尝试不存在");
   if(historical.getExternalBatchId()==null){require(phases.historicalExternal(p,external)==1,"缺少精确已接受阶段，不能补历史外部号");}
   else require(Objects.equals(historical.getExternalBatchId(),external),"历史外部号冲突，不能覆盖");
  }
  AuthOperationReceiptCommand event=AuthOperationReceiptCommand.builder().targetId(p.getTargetId()).attemptId(p.getAttemptId()).attemptNo(p.getAttemptNo()).leaseToken(p.getLeaseToken())
   .accessType(p.getAccessType()).operationVersion(p.getResourceGeneration()).externalBatchId(isc?external:null).externalCommandId(isc?null:external)
   .eventNamespace("TRANSPORT").eventKey(eventKey).evidenceType("DEVICE_ACK").resultStatus(success?"SUCCESS":"UNKNOWN").trustedDeviceEvidence(success).evidenceBody(body).build();
  List<SmtAuthSourceResource> shared=bindings.targetContributions(p.getTargetId());
  require(!shared.isEmpty()&&shared.size()<=1000,"回执缺少有界历史来源集合");
  TargetEvidenceHandler targetHandler=e->{
   try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)) {
    if(isc) {SmtIscDeviceTask task=required(iscTasks.selectById(Long.valueOf(p.getTaskId())),"ISC旧任务不存在");task.setParkId(park);return iscCompletion.completeSuccess(task,"可信版本回执");}
    SmtDeviceTask task=required(directTasks.selectById(Integer.valueOf(p.getTaskId())),"直连旧任务不存在");task.setParkId(park);return directCompletion.completeSuccess(task,com.tce.smart.tool.enums.DeviceTaskEnum.DEVICE_OK.getCode(),"可信版本回执",null);
   }
  };
  Received received=null;boolean converged=true,physicalSettled=success;
  for(SmtAuthSourceResource c:shared){
   require(Objects.equals(c.getResourceCoordId(),p.getResourceId()),"回执共享来源跨资源");
   received=workflow.receiveWithinTransaction(binding(c,p.getAttemptId()),event,targetHandler,employee);
   converged=converged&&received.isSourceConverged();
   AuthOperationReceiptResult proof=received.getReceipt();
   physicalSettled=physicalSettled && received.isPhysicalSettled() && proof!=null && proof.getEventId()!=null
    && Objects.equals(proof.getTargetId(),p.getTargetId()) && Objects.equals(proof.getAttemptId(),p.getAttemptId());
  }
  if(physicalSettled&&!"FINISHED".equals(p.getState())&&phases.transition(p.getId(),p.getState(),"FINISHED",external,null)!=1) {
   // UPDATE清除MyBatis会话缓存；仅竞争失败者重读真实终态，仍保持资源锁→阶段写入顺序。
   SmtAuthTransportPhase finished=phases.byId(p.getId());
   require(finished!=null&&"FINISHED".equals(finished.getState())&&samePhysicalPhase(p,finished)
    && Objects.equals(external,isc?finished.getExternalId():finished.getSerialNo())
    && (isc||Objects.equals(external,finished.getExternalId())),"物理证据已结算阶段结束冲突");
  }
  return Received.builder().receipt(received.getReceipt()).evidence(received.getEvidence()).sourceConverged(converged).physicalSettled(physicalSettled).build();
 }
 // 精确复核回执与执行坐标；同一资源的新attempt、外部号或旧任务均不得借用另一个阶段的终态。
 private boolean samePhysicalPhase(SmtAuthTransportPhase a,SmtAuthTransportPhase b) {
  return Objects.equals(a.getId(),b.getId())
   && Objects.equals(a.getTargetId(),b.getTargetId())
   && Objects.equals(a.getAttemptId(),b.getAttemptId())
   && Objects.equals(a.getAttemptNo(),b.getAttemptNo())
   && Objects.equals(a.getLeaseToken(),b.getLeaseToken())
   && Objects.equals(a.getSourceId(),b.getSourceId())
   && Objects.equals(a.getSourceGeneration(),b.getSourceGeneration())
   && Objects.equals(a.getResourceId(),b.getResourceId())
   && Objects.equals(a.getResourceGeneration(),b.getResourceGeneration())
   && Objects.equals(a.getRequestId(),b.getRequestId())
   && Objects.equals(a.getParkId(),b.getParkId())
   && Objects.equals(a.getInstanceId(),b.getInstanceId())
   && Objects.equals(a.getAccessType(),b.getAccessType())
   && Objects.equals(a.getPhase(),b.getPhase())
   && Objects.equals(a.getTaskId(),b.getTaskId())
   && Objects.equals(a.getSerialNo(),b.getSerialNo())
   && Objects.equals(a.getRequestKey(),b.getRequestKey())
   && Objects.equals(a.getDeviceId(),b.getDeviceId())
   && Objects.equals(a.getSubjectType(),b.getSubjectType())
   && Objects.equals(a.getSubjectId(),b.getSubjectId())
   && Objects.equals(a.getAction(),b.getAction())
   && Objects.equals(a.getResourceType(),b.getResourceType())
   && Objects.equals(a.getServiceType(),b.getServiceType())
   && Objects.equals(a.getCredentialChannel(),b.getCredentialChannel())
   && Objects.equals(a.getCardNo(),b.getCardNo())
   && Objects.equals(a.getBadge(),b.getBadge())
   && Objects.equals(a.getPersonId(),b.getPersonId())
   && Objects.equals(a.getImageId(),b.getImageId())
   && Objects.equals(a.getPersonSnapshot(),b.getPersonSnapshot())
   && Objects.equals(a.getOrgIndexCode(),b.getOrgIndexCode())
   && Objects.equals(a.getStartTime(),b.getStartTime())
   && Objects.equals(a.getOverTime(),b.getOverTime())
   && Objects.equals(a.getChannelNo(),b.getChannelNo());
 }
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void verifyResult(int park,String instance,Long id,String reason){SmtAuthTransportPhase p=owned(id,park,instance);phases.hold(p,reason);phases.holdAttempt(p,reason);}
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void advancePage(int park,String instance,List<Long> ids,int old,int next) {for(Long id:ids){SmtAuthTransportPhase p=owned(id,park,instance);if(!"FINISHED".equals(p.getState()))require(phases.page(id,old,next)==1,"回执页游标冲突");}}
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public void verifyClaim(int park,AuthOperationClaimedTarget claim,String reason) {
  SmtAuthOperationTarget target=required(targets.selectById(claim.getTargetId()),"目标不存在");
  SmtAuthOperationAttempt attempt=required(attempts.selectByIdAndTarget(claim.getAttemptId(),claim.getTargetId()),"尝试不存在");
  require(Objects.equals(target.getParkId(),park)&&Objects.equals(attempt.getLeaseToken(),claim.getLeaseToken()),"核验目标园区或租约不符");
  SmtAuthTransportPhase p=new SmtAuthTransportPhase();p.setTargetId(claim.getTargetId());p.setAttemptId(claim.getAttemptId());p.setLeaseToken(claim.getLeaseToken());
  phases.hold(p,reason);phases.holdAttempt(p,reason);
 }
 public List<SmtAuthTransportPhase> prepared(int park,String instance,String priority,Long after,int limit){require(Arrays.asList("ADD","DELETE").contains(priority)&&limit>0&&limit<=200,"恢复分页参数无效");return phases.prepared(park,instance,priority,after,limit);}
 @Transactional(propagation=Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
 public int expireIntents(int park,String instance,int limit) {
  require(limit>0&&limit<=200,"意图核验分页越界");int changed=0;
  List<SmtAuthTransportPhase> stale=phases.staleIntents(park,instance,limit);List<Long> staleIds=new ArrayList<>();for(SmtAuthTransportPhase p:stale)staleIds.add(p.getId());lockPersonOwners(park,instance,staleIds);
  for(SmtAuthTransportPhase p:stale){SmtAuthPersonOwner owner=personOwner(p);if(owner!=null)ownerMatches(owner,p);if(phases.transition(p.getId(),"INTENT","UNKNOWN",p.getExternalId(),"SUBMISSION_RESPONSE_LOST")==1){if(owner!=null)personOwners.mark(owner.getId(),"INTENT","UNKNOWN","SUBMISSION_RESPONSE_LOST");phases.hold(p,"SUBMISSION_RESPONSE_LOST");phases.holdAttempt(p,"SUBMISSION_RESPONSE_LOST");changed++;}}
  return changed;
 }
 public List<SmtAuthTransportPhase> exactPhases(int park,String instance,List<Long> ids,String phase,String state){
  require(ids!=null&&!ids.isEmpty()&&ids.size()<=200,"精确阶段上限200");List<SmtAuthTransportPhase> rows=new ArrayList<>();String device=null,group=null;
  for(Long id:new LinkedHashSet<>(ids)){SmtAuthTransportPhase p=owned(id,park,instance);require(phase.equals(p.getPhase())&&state.equals(p.getState()),"精确阶段类型或状态不符");if(device==null)device=p.getDeviceId();else require(Objects.equals(device,p.getDeviceId()),"设备预算禁止混入其他设备");String compatibility=AuthOperationTransportPolicy.groupKey(p);if(group==null)group=compatibility;else require(group.equals(compatibility),"精确批次包含不兼容阶段");rows.add(p);}return rows;
 }
 public List<SmtAuthTransportPhase> scan(int park,String instance,String phase,String state,Long after,int limit) {require(limit>0&&limit<=200,"阶段分页越界");return phases.scan(park,instance,phase,state,after,limit);}
 public List<SmtAuthTransportPhase> group(SmtAuthTransportPhase p) {List<SmtAuthTransportPhase> result=phases.group(p.getParkId(),p.getInstanceId(),p.getRequestKey(),p.getPhase());require(result.size()<=200,"历史请求组超出上限");return result;}
 public List<SmtAuthTransportPhase> byTask(String access,String task) {return phases.byTask(access,task);}
 private SmtAuthTransportPhase owned(Long id,int park,String instance) {SmtAuthTransportPhase p=required(phases.byId(id),"阶段不存在");require(p.getParkId()==park&&instance.equals(p.getInstanceId()),"阶段园区或接入实例不匹配");return p;}
 private static Binding binding(SmtAuthSourceResource c,Long a) {return Binding.builder().sourceId(c.getSourceCoordId()).sourceGeneration(c.getSourceGeneration()).resourceId(c.getResourceCoordId()).resourceGeneration(c.getResourceGeneration()).requestId(c.getRequestId()).targetId(c.getTargetId()).attemptId(a).build();}
 private static Binding binding(SmtAuthTransportPhase p) {return Binding.builder().sourceId(p.getSourceId()).sourceGeneration(p.getSourceGeneration()).resourceId(p.getResourceId()).resourceGeneration(p.getResourceGeneration()).requestId(p.getRequestId()).targetId(p.getTargetId()).attemptId(p.getAttemptId()).build();}
 private static AuthOperationSubmissionCommand submission(SmtAuthTransportPhase p) {return AuthOperationSubmissionCommand.builder().targetId(p.getTargetId()).attemptId(p.getAttemptId()).attemptNo(p.getAttemptNo()).leaseToken(p.getLeaseToken()).accessType(p.getAccessType()).taskId(p.getTaskId()).build();}
 private static SmtIscDeviceTask iscTask(SmtAuthTransportPhase p) {SmtIscDeviceTask t=new SmtIscDeviceTask();t.setAction("DELETE".equals(p.getAction())?2:1);t.setStatus(3);t.setDeviceType(1);t.setDeviceCode(p.getDeviceId());t.setCardNo(p.getCardNo());t.setBadge(p.getBadge());t.setImageId(p.getImageId());t.setStartTime(p.getStartTime());t.setOverTime(p.getOverTime());t.setServiceType(Integer.valueOf(p.getServiceType()));t.setTimes(0);t.setCreateTime(now());return t;}
 private static SmtDeviceTask directTask(SmtAuthTransportPhase p) {SmtDeviceTask t=new SmtDeviceTask();t.setAction("DELETE".equals(p.getAction())?2:1);t.setStatus(3);t.setDeviceType(1);t.setDeviceCode(p.getDeviceId());t.setCardNo(p.getCardNo());t.setImageId(p.getImageId());t.setStartTime(p.getStartTime());t.setOverTime(p.getOverTime());t.setServiceType(Integer.valueOf(p.getServiceType()));t.setSerialNo(p.getSerialNo());t.setCardType(1);t.setTimes(0);t.setCreateTime(now());return t;}
 private static boolean present(String x){return x!=null&&!x.trim().isEmpty();}
 private static void require(boolean ok,String message){if(!ok)throw new IllegalArgumentException(message);}
 private static <T>T required(T x,String message){require(x!=null,message);return x;}
 private static LocalDateTime now(){return LocalDateTime.now(ZoneOffset.UTC);}
}
