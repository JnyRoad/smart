package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** 员工受理与分页展开；不得在受理事务中创建设备任务或调用远程服务。 */
@Service
public class EmployeeAuthOperationService extends AuthSelectionService {
 private final EmployeeAuthOperationMapper mapper;
 private final AuthOperationWorkflowService workflow;
 public EmployeeAuthOperationService(EmployeeAuthOperationMapper mapper,AuthOperationWorkflowService workflow) {
  this(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(new EmployeeAuthSourceHandler(mapper))));
 }
 @org.springframework.beans.factory.annotation.Autowired
 public EmployeeAuthOperationService(EmployeeAuthOperationMapper mapper,AuthOperationWorkflowService workflow,AuthSourceConvergenceRegistry registry) {
  super(mapper,workflow,registry);this.mapper=mapper;this.workflow=workflow;
 }
 /** 所有园区共用操作 key；同事务落批次、选择与待处理标记。 */
 @Transactional(rollbackFor=Exception.class)
 public Accepted accept(String key,List<Source> sources,Set<Integer> allowedParks) {
  require(key!=null && key.length()<=100 && !key.trim().isEmpty(),"操作标识无效");
  require(sources!=null && !sources.isEmpty(),"没有可冻结来源");
  Set<String> unique=new HashSet<>();
  for(Source s:sources) {
   if(allowedParks==null || !allowedParks.contains(s.getParkId()))throw new SecurityException("所选来源园区不在允许范围");
   require(s.getPersonSnapshot()==null || s.getPersonSnapshot().length()<=2048,"人员最小冻结投影超长");
   require(s.getBefore()!=null || s.getAfter()!=null,"来源投影不能为空");
   validateRelation(s,s.getBefore());validateRelation(s,s.getAfter());
   require(unique.add(s.getParkId()+":"+AuthWorkflow.staffStableKey(s.getSubjectId(),s.getAuthId())),"同一稳定来源存在重复关系，必须先核验");
   require(!s.getResources().isEmpty() || requiresReview(s),"NEEDS_VERIFICATION：缺少历史设备与当前设备依据，来源保留");
   for(ResourceInput r:s.getResources()) {
    ResourceKey k=r.getResource();require(k!=null && Objects.equals(k.getParkId(),s.getParkId()) && "STAFF".equals(k.getSubjectType())
     && Objects.equals(k.getSubjectId(),s.getSubjectId()),"冻结资源与员工主体不一致");
    require(!String.valueOf(DeviceTaskConstants.CARD_APP_PERFECT).equals(k.getServiceType()),"APP_PERFECT_REVIEW：APP完善只能作为核验证据，不能作为执行资源");
    require(r.getWindows().size()<=1,"单条冻结贡献不能拉宽多个窗口");
   }
  }
  List<Long> subjects=sources.stream().map(s->Long.valueOf(s.getSubjectId())).distinct().sorted().collect(Collectors.toList());
  for(List<Long> part:parts(subjects,200))require(mapper.lockSubjects(part).size()==part.size(),"员工不存在，不能受理");
  List<SmtAuthSelectionSource> previous=mapper.operation(key);
  if(!previous.isEmpty()) {
   Map<String,String> expected=new HashMap<>();for(Source source:sources)expected.put(source.getParkId()+":"+AuthWorkflow.staffStableKey(source.getSubjectId(),source.getAuthId()),fingerprint(source));
   require(previous.size()==sources.size(),"幂等操作的冻结来源数量已改变");
   Map<Integer,List<Long>> existing=new LinkedHashMap<>();
   for(SmtAuthSelectionSource row:previous) {
    require(Objects.equals(row.getFingerprint(),expected.get(row.getParkId()+":"+row.getStableKey())),"幂等操作的完整冻结指纹已改变");
    List<Long> ids=existing.computeIfAbsent(row.getParkId(),k->new ArrayList<>());if(!ids.contains(row.getBatchId()))ids.add(row.getBatchId());
   }
   return Accepted.builder().operationKey(key).batches(existing).build();
  }
  for(Integer park:sources.stream().map(Source::getParkId).collect(Collectors.toSet())) {
   List<Long> parkSubjects=sources.stream().filter(s->Objects.equals(park,s.getParkId())).map(s->Long.valueOf(s.getSubjectId())).distinct().sorted().collect(Collectors.toList());
   for(List<Long> part:parts(parkSubjects,200))if(mapper.pendingSubjects(park,part)>0)
    throw new IllegalStateException("PENDING_SELECTION：员工已有待处理操作，禁止覆盖或复活来源");
  }
  Map<Integer,List<Source>> parks=sources.stream().collect(Collectors.groupingBy(Source::getParkId,TreeMap::new,Collectors.toList()));
  List<List<Source>> groups=new ArrayList<>();
  for(List<Source> parkSources:parks.values()) {
   List<Source> healthy=new ArrayList<>();List<List<Source>> isolated=new ArrayList<>();
   Map<String,List<Source>> subjectsInPark=parkSources.stream().collect(Collectors.groupingBy(Source::getSubjectId,TreeMap::new,Collectors.toList()));
   for(List<Source> subject:subjectsInPark.values())if(subject.stream().anyMatch(EmployeeAuthOperationService::requiresReview))isolated.add(subject);else healthy.addAll(subject);
   if(!healthy.isEmpty())groups.add(healthy);groups.addAll(isolated);
  }
  Map<Integer,List<Long>> batches=new LinkedHashMap<>();int groupOrdinal=0;
  for(List<Source> selected:groups) {
   selected.sort(Comparator.comparing(Source::getSubjectId).thenComparing(Source::getAuthId));
   Integer park=selected.get(0).getParkId();
   Set<String> lanes=new HashSet<>();StringBuilder manifest=new StringBuilder();
   for(Source s:selected) {manifest.append(fingerprint(s));for(ResourceInput r:s.getResources())lanes.add(laneKey(r.getResource()));}
   String action=selected.stream().allMatch(s->s.getAfter()==null)?"DELETE":"ADD";
   Long batch=workflow.acceptWithinTransaction(Selection.builder().parkId(park).idempotencyKey(key+":"+park+":"+(++groupOrdinal))
    .action(action).sourceType("1").sourceId(key).snapshot("EMPLOYEE_SELECTION_V1:"+hash(manifest.toString()))
    .expectedCount(lanes.size()).sourceCount(selected.size()).build()).getBatchId();
   List<SmtAuthSelectionSource> sourceRows=new ArrayList<>();List<SmtAuthSelectionResource> resources=new ArrayList<>();
   long sourceOrdinal=0,ordinal=0;
   for(Source s:selected) {
    SmtAuthSelectionSource row=freeze(s,batch,++sourceOrdinal,key);sourceRows.add(row);
    Set<String> own=new HashSet<>();
    for(ResourceInput r:s.getResources()) {
     require(own.add(laneKey(r.getResource())),"同来源设备坐标重复");
     SmtAuthSelectionResource rr=freeze(r,batch,sourceOrdinal,++ordinal);resources.add(rr);
    }
   }
   for(List<SmtAuthSelectionSource> part:parts(sourceRows,200))mapper.insertSources(part);
   for(List<SmtAuthSelectionResource> part:parts(resources,200))mapper.insertResources(part);
   batches.computeIfAbsent(park,k->new ArrayList<>()).add(batch);
   if(selected.stream().anyMatch(EmployeeAuthOperationService::requiresReview))require(mapper.markVerification(batch,selected.stream().map(Source::getVerificationReason).filter(Objects::nonNull).flatMap(r->Arrays.stream(r.split(";"))).distinct().sorted().collect(Collectors.joining(";")))==1,"隔离核验状态未落库");
  }
  return Accepted.builder().operationKey(key).batches(Collections.unmodifiableMap(batches)).build();
 }
 public boolean pendingSource(int park,String subject,String authId) {return mapper.pendingSource(park,subject,authId)>0;}
 public boolean pendingSubject(int park,String subject) {return mapper.pendingSubject(park,subject)>0;}
 public Map<Integer,List<Long>> batches(String operationKey,Set<Integer> allowedParks) {
  Map<Integer,List<Long>> result=new LinkedHashMap<>();
  for(SmtAuthSelectionSource s:mapper.operation(operationKey)) {
   if(allowedParks==null || !allowedParks.contains(s.getParkId()))throw new SecurityException("操作涉及未授权园区");
   List<Long> park=result.computeIfAbsent(s.getParkId(),k->new ArrayList<>());if(!park.contains(s.getBatchId()))park.add(s.getBatchId());
  }
  return result;
 }
 private static SmtAuthSelectionSource freeze(Source s,Long batch,long ordinal,String key) {
  SmtAuthSelectionSource x=new SmtAuthSelectionSource();x.setBatchId(batch);x.setOrdinal(ordinal);x.setOperationKey(key);x.setParkId(s.getParkId());
  x.setSourceKind("STAFF_AUTH");x.setSubjectType("STAFF");x.setSnapshotVersion(0);x.setSubjectId(s.getSubjectId());x.setAuthId(s.getAuthId());x.setStableKey(AuthWorkflow.staffStableKey(s.getSubjectId(),s.getAuthId()));
  x.setSourceRowId(s.getBefore()==null?"NEW:"+x.getStableKey():String.valueOf(s.getBefore().getId()));x.setFingerprint(fingerprint(s));
  x.setDesiredAction(s.getAfter()==null?"DELETE":"ADD");x.setImageId(s.getImageId());x.setPersonSnapshot(s.getPersonSnapshot());x.setBadge(s.getBadge());x.setState("PENDING");x.setVerificationReason(s.getVerificationReason());x.setBusinessSnapshot(historySnapshot(s));
  SmtStaffDeviceAuth old=s.getBefore(),n=s.getAfter();
  if(old!=null){x.setOldId(old.getId());x.setOldCreateTime(old.getCreateTime());x.setOldStartTime(old.getStartTime());x.setOldEndTime(old.getEndTime());x.setOldAuthType(old.getAuthType());}
  if(n!=null){x.setNewCreateTime(n.getCreateTime());x.setNewStartTime(n.getStartTime());x.setNewEndTime(n.getEndTime());x.setNewAuthType(n.getAuthType());}
  return x;
 }
 private static SmtAuthSelectionResource freeze(ResourceInput r,Long batch,long source,long ordinal) {
  ResourceKey k=r.getResource();SmtAuthSelectionResource x=new SmtAuthSelectionResource();x.setBatchId(batch);x.setOrdinal(ordinal);x.setSourceOrdinal(source);
  x.setSubjectType("STAFF");x.setCredentialVersion(0);x.setParkId(k.getParkId());x.setSubjectId(k.getSubjectId());x.setDeviceId(k.getDeviceId());x.setAccessType(k.getAccessType());x.setResourceType(k.getResourceType());
  x.setResourceId(k.getResourceId());x.setServiceType(k.getServiceType());x.setCredentialChannel(k.getCredentialChannel());x.setParticipation(r.getParticipation());
  if(!r.getWindows().isEmpty()){x.setValidFrom(r.getWindows().get(0).getFrom());x.setValidTo(r.getWindows().get(0).getTo());}return x;
 }
 public static String fingerprint(Source s) {
  StringBuilder b=new StringBuilder();b.append(part(s.getParkId())).append(part(s.getSubjectId())).append(part(s.getAuthId()));
  append(b,s.getBefore());append(b,s.getAfter());b.append(part(s.getImageId())).append(part(s.getPersonSnapshot())).append(part(s.getBadge())).append(part(s.getVerificationReason()));
  List<ResourceInput> ordered=new ArrayList<>(s.getResources());
  if(s.getHistoryEvidence().stream().anyMatch(e->Integer.valueOf(2).equals(e.getEvidenceVersion())))ordered.sort(Comparator.comparing(r->laneKey(r.getResource())));
  for(ResourceInput r:ordered)b.append(part(laneKey(r.getResource()))).append(part(r.getParticipation())).append(part(r.getWindows()));
  if(!s.getHistoryEvidence().isEmpty())b.append(part(historySnapshot(s)));
  return hash(b.toString());
 }
 private static void validateRelation(Source s,SmtStaffDeviceAuth r) {
  if(r!=null)require(Objects.equals(s.getSubjectId(),String.valueOf(r.getStaffId())) && Objects.equals(s.getAuthId(),String.valueOf(r.getAuthId())),"员工关系与稳定来源不一致");
 }
 private static boolean requiresReview(Source s){return s.getVerificationReason()!=null && (s.getVerificationReason().startsWith("MISSING_DEVICE") || s.getVerificationReason().startsWith("APP_PERFECT_REVIEW"));}
 /** 属性与行序规范化，原始服务、原任务引用及每一行均参与冻结指纹。 */
 private static String historySnapshot(Source s) {
  return com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.encodeHistory(s.getHistoryEvidence());
 }
 private static String part(Object value){if(value==null)return "-1:";String s=String.valueOf(value);return s.length()+":"+s;}
 private static void append(StringBuilder b,SmtStaffDeviceAuth x) {if(x==null){b.append("-1:");return;}b.append(part(x.getId())).append(part(x.getStaffId())).append(part(x.getAuthId())).append(part(time(x.getCreateTime()))).append(part(time(x.getStartTime()))).append(part(time(x.getEndTime()))).append(part(x.getAuthType()));}
 private static Long time(Date d){return d==null?null:d.getTime();}
 private static LocalDateTime local(Date d){return d==null?null:LocalDateTime.ofInstant(d.toInstant(),ZoneId.systemDefault());}
 private static String laneKey(ResourceKey k){return part(k.getParkId())+part(k.getSubjectType())+part(k.getSubjectId())+part(k.getDeviceId())+part(k.getAccessType())+part(k.getResourceType())+part(k.getResourceId())+part(k.getServiceType())+part(k.getCredentialChannel());}
 private static String hash(String s){try{byte[] bs=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:bs)b.append(String.format("%02x",x&255));return b.toString();}catch(Exception e){throw new IllegalStateException(e);}}
 protected static void require(boolean b,String m){if(!b)throw new IllegalArgumentException(m);}
 private static <T> List<List<T>> parts(List<T> xs,int size){List<List<T>> out=new ArrayList<>();for(int i=0;i<xs.size();i+=size)out.add(xs.subList(i,Math.min(i+size,xs.size())));return out;}
}
