package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.Accepted;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.AuthSelectionMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
/** 单一选择协调实现；由员工兼容子类提供唯一 Spring Bean，继承方法接受外部事务代理。 */
public class AuthSelectionService implements ConvergenceHandler {
 private final AuthSelectionMapper mapper;
 private final AuthOperationWorkflowService workflow;
 private final AuthSourceConvergenceRegistry registry;
 protected AuthSelectionService(AuthSelectionMapper mapper,AuthOperationWorkflowService workflow,AuthSourceConvergenceRegistry registry) {
  this.mapper=mapper;this.workflow=workflow;this.registry=registry;
 }
 /** 服务器来源已完整枚举后在本库冻结；业务 handler 按自身真实父行执行锁与核验。 */
 @Transactional(rollbackFor=Exception.class)
 public Accepted acceptTyped(String key,List<? extends SourceSelection<?>> sources,Set<Integer> allowedParks) {
  text(key,100,"操作标识");require(sources!=null && !sources.isEmpty(),"没有可冻结来源");
  List<SourceSelection<?>> sorted=new ArrayList<>(sources);
  for(SourceSelection<?> s:sorted)validate(s,allowedParks);
  sorted.sort(Comparator.comparing((SourceSelection<?> s)->s.getParkId()).thenComparing(s->s.getSourceKind().name()).thenComparing(SourceSelection::getSubjectId).thenComparing(SourceSelection::getStableKey));
  Map<String,String> fingerprints=new LinkedHashMap<>();
  for(SourceSelection<?> s:sorted)registry.lockAndValidate(s);
  for(SourceSelection<?> s:sorted){String id=identity(s);require(fingerprints.put(id,fingerprint(s))==null,"同一种类稳定来源重复");}
  List<SmtAuthSelectionSource> previous=mapper.operation(key);Map<Integer,List<Long>> batches=new LinkedHashMap<>();
  if(!previous.isEmpty()) {
   require(previous.size()==sorted.size(),"幂等选择来源数改变");
   for(SmtAuthSelectionSource row:previous){require(Objects.equals(row.getFingerprint(),fingerprints.get(identity(row))),"幂等冻结完整指纹改变");List<Long> ids=batches.computeIfAbsent(row.getParkId(),p->new ArrayList<>());if(!ids.contains(row.getBatchId()))ids.add(row.getBatchId());}
   return Accepted.builder().operationKey(key).batches(batches).build();
  }
  Set<String> checked=new HashSet<>();
  for(SourceSelection<?> s:sorted)if(checked.add(tuple(s.getParkId(),s.getSubjectType(),s.getSubjectId())))
   require(mapper.pendingTypedSubject(s.getParkId(),s.getSubjectType().name(),s.getSubjectId())==0,"PENDING_SELECTION：同类型主体已有操作");
  Map<Integer,List<SourceSelection<?>>> parks=sorted.stream().collect(Collectors.groupingBy(SourceSelection::getParkId,TreeMap::new,Collectors.toList()));
  int group=0;
  for(List<SourceSelection<?>> selected:parks.values()) {
   SourceSelection<?> first=selected.get(0);Set<String> lanes=new HashSet<>();StringBuilder manifest=new StringBuilder();
   for(SourceSelection<?> s:selected){require(Objects.equals(first.getSourceType(),s.getSourceType()),"同园区批次的触发原因编码不一致");manifest.append(fingerprints.get(identity(s)));for(SelectedResource r:s.getResources())lanes.add(lane(r.getInput().getResource()));}
   Long batch=workflow.acceptWithinTransaction(Selection.builder().parkId(first.getParkId()).idempotencyKey(key+":"+first.getParkId()+":"+(++group)).sourceType(first.getSourceType()).sourceId(key)
    .action(selected.stream().allMatch(s->"DELETE".equals(s.getAction()))?"DELETE":"ADD").snapshot("TYPED_SELECTION_V1:"+digest(manifest.toString())).expectedCount(lanes.size()).sourceCount(selected.size()).build()).getBatchId();
   List<SmtAuthSelectionSource> frozen=new ArrayList<>();List<SmtAuthSelectionResource> resources=new ArrayList<>();long ordinal=0,resourceOrdinal=0;
   for(SourceSelection<?> s:selected) {
    SmtAuthSelectionSource row=new SmtAuthSelectionSource();row.setBatchId(batch);row.setOrdinal(++ordinal);row.setOperationKey(key);row.setParkId(s.getParkId());row.setSourceKind(s.getSourceKind().name());row.setSubjectType(s.getSubjectType().name());row.setSubjectId(s.getSubjectId());row.setAuthId(emptyToNull(s.getAuthId()));row.setStableKey(s.getStableKey());row.setSourceRowId(s.getSourceRowId());row.setDesiredAction(s.getAction());row.setSnapshotVersion(s.getSnapshotVersion());row.setBusinessSnapshot(AuthSelectionSnapshots.business(s.getBusiness(),s.getWindows()));row.setParentKind(emptyToNull(s.getParentKind()));row.setParentRowId(emptyToNull(s.getParentRowId()));row.setFingerprint(fingerprints.get(identity(s)));row.setState("PENDING");row.setVerificationReason(s.getVerificationReason()==null?null:s.getVerificationReason().name());frozen.add(row);
    for(SelectedResource r:s.getResources()) {
     ResourceKey k=r.getInput().getResource();SmtAuthSelectionResource rr=new SmtAuthSelectionResource();rr.setBatchId(batch);rr.setOrdinal(++resourceOrdinal);rr.setSourceOrdinal(ordinal);rr.setParkId(k.getParkId());rr.setSubjectType(k.getSubjectType());rr.setSubjectId(k.getSubjectId());rr.setDeviceId(k.getDeviceId());rr.setAccessType(k.getAccessType());rr.setResourceType(k.getResourceType());rr.setResourceId(k.getResourceId());rr.setServiceType(k.getServiceType());rr.setCredentialChannel(k.getCredentialChannel());rr.setParticipation(r.getInput().getParticipation());rr.setCredentialVersion(1);rr.setCredentialSnapshot(credentialSnapshot(r));
     if(!r.getInput().getWindows().isEmpty()){rr.setValidFrom(r.getInput().getWindows().get(0).getFrom());rr.setValidTo(r.getInput().getWindows().get(0).getTo());}resources.add(rr);
    }
   }
   for(int i=0;i<frozen.size();i+=200){List<SmtAuthSelectionSource> page=frozen.subList(i,Math.min(i+200,frozen.size()));require(mapper.insertSources(page)==page.size(),"冻结来源未完整写入");}
   for(int i=0;i<resources.size();i+=200){List<SmtAuthSelectionResource> page=resources.subList(i,Math.min(i+200,resources.size()));require(mapper.insertResources(page)==page.size(),"冻结资源未完整写入");}
   String reason=selected.stream().map(SourceSelection::getVerificationReason).filter(Objects::nonNull).map(Enum::name).distinct().sorted().collect(Collectors.joining(";"));
   // 完整家庭仍保留全部来源和真实资源；缺证据的批次只受理为核验，不能进入展开。
   if(!reason.isEmpty())require(mapper.markVerification(batch,reason)==1,"核验批次状态未落库");
   batches.computeIfAbsent(first.getParkId(),p->new ArrayList<>()).add(batch);
  }
  return Accepted.builder().operationKey(key).batches(Collections.unmodifiableMap(batches)).build();
 }
 private static void validate(SourceSelection<?> s,Set<Integer> parks) {
  require(s!=null && s.getSourceKind()!=null && s.getSubjectType()!=null && s.getSnapshotVersion()==1 && s.getBusiness()!=null,"缺少强类型来源或版本");
  if(parks==null || !parks.contains(s.getParkId()))throw new SecurityException("来源园区不在允许范围");
  text(s.getSubjectId(),256,"主体ID");text(s.getStableKey(),300,"稳定来源键");text(s.getSourceRowId(),400,"原行ID");text(s.getSourceType(),16,"来源原因");
  optional(s.getAuthId(),128,"权限ID");optional(s.getParentKind(),64,"父种类");optional(s.getParentRowId(),128,"父ID");require((emptyToNull(s.getParentKind())==null)==(emptyToNull(s.getParentRowId())==null),"父来源字段必须同时提供");
  require("ADD".equals(s.getAction()) || "DELETE".equals(s.getAction()),"来源动作无效");require(!s.getResources().isEmpty() || s.getVerificationReason()!=null,"NEEDS_VERIFICATION：缺少资源依据");
  if("ADD".equals(s.getAction()))require(!s.getWindows().isEmpty(),"新增缺少来源窗口");Set<String> own=new HashSet<>();
  for(SelectedResource r:s.getResources()) {
   require(r!=null && r.getInput()!=null && r.getInput().getResource()!=null,"冻结资源为空");ResourceKey k=r.getInput().getResource();
   require(Objects.equals(k.getParkId(),s.getParkId()) && Objects.equals(k.getSubjectType(),s.getSubjectType().name()) && Objects.equals(k.getSubjectId(),s.getSubjectId()),"冻结资源类型与来源主体不一致");
   require(own.add(lane(k)),"同来源物理资源重复");require(r.getInput().getWindows().size()<=1,"不能拉宽资源窗口");
   for(Window window:r.getInput().getWindows())AuthSelectionSnapshots.validateWindow(window);
   if(s.getVerificationReason()!=null)validateReviewResource(r);
   if(r.getCredential()==null)require(s.getVerificationReason()!=null,"NEEDS_VERIFICATION：缺少凭据依据");
   else {AuthSelectionSnapshots.credential(r.getCredential());validateCredentialCoordinate(r.getCredential(),k.getResourceType(),k.getSubjectId(),k.getServiceType());}
  }
 }
 private static String fingerprint(SourceSelection<?> s) {
  StringBuilder out=new StringBuilder(tuple(s.getParkId(),s.getSourceKind(),s.getSubjectType(),s.getSubjectId(),emptyToNull(s.getAuthId()),s.getStableKey(),s.getSourceRowId(),s.getAction(),s.getSourceType(),emptyToNull(s.getParentKind()),emptyToNull(s.getParentRowId()),s.getSnapshotVersion(),AuthSelectionSnapshots.business(s.getBusiness(),s.getWindows())));
  // 无原因时不追加任何字节，保持既有v1可执行投影的完整指纹。
  if(s.getVerificationReason()!=null)out.append(tuple("VERIFICATION_REASON",s.getVerificationReason().name()));
  for(SelectedResource r:s.getResources())out.append(tuple(lane(r.getInput().getResource()),r.getInput().getParticipation(),r.getInput().getWindows(),credentialSnapshot(r)));return digest(out.toString());
 }
 /** 仅核验来源可以没有凭据JSON；版本仍为1，绝不伪造可执行凭据或降为员工v0。 */
 private static String credentialSnapshot(SelectedResource r){return r.getCredential()==null?null:AuthSelectionSnapshots.credential(r.getCredential());}
 private static void validateReviewResource(SelectedResource r) {
  ResourceKey k=r.getInput().getResource();text(k.getDeviceId(),128,"核验资源设备ID");text(k.getAccessType(),16,"核验资源接入类型");text(k.getResourceType(),32,"核验资源种类");
  text(k.getResourceId(),256,"核验资源ID");text(k.getServiceType(),64,"核验资源服务类型");text(k.getCredentialChannel(),64,"核验资源凭据通道");
  require(Arrays.asList("DIRECT","ISC").contains(k.getAccessType()) && Arrays.asList("PERSON","VEHICLE").contains(k.getResourceType()),"核验资源坐标类型无效");
  require(Arrays.asList("INCLUDE","EXCLUDE").contains(r.getInput().getParticipation()),"核验资源贡献意图无效");
 }
 private static String identity(SourceSelection<?> s){return tuple(s.getParkId(),s.getSourceKind(),s.getStableKey());}
 private static String identity(SmtAuthSelectionSource s){return tuple(s.getParkId(),AuthSelectionSnapshots.kind(s),s.getStableKey());}
 private static String lane(ResourceKey k){return tuple(k.getParkId(),k.getSubjectType(),k.getSubjectId(),k.getDeviceId(),k.getAccessType(),k.getResourceType(),k.getResourceId(),k.getServiceType(),k.getCredentialChannel());}
 private static String tuple(Object... values){StringBuilder b=new StringBuilder();for(Object v:values){String x=v==null?null:String.valueOf(v);b.append(x==null?"-1:":x.length()+":"+x);}return b.toString();}
 private static String digest(String s){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte x:bytes)b.append(String.format("%02x",x&255));return b.toString();}catch(Exception e){throw new IllegalStateException(e);}}
 private static String emptyToNull(String value){return value==null || value.isEmpty()?null:value;}
 private static void text(String value,int bytes,String field){require(value!=null && !value.trim().isEmpty() && value.getBytes(StandardCharsets.UTF_8).length<=bytes,field+"不能为空或超过Oracle字节上限");}
 private static void optional(String value,int bytes,String field){if(emptyToNull(value)!=null)text(value,bytes,field);}
 /** 一个调用最多登记同来源 200 条冻结资源；恢复时读取数据库游标，不重读权限组设备。 */
 @Transactional(rollbackFor=Exception.class)
 public boolean stageNext(Long batch) {
  String reason=mapper.verificationReason(batch);if(reason!=null)throw new IllegalStateException(reason);
  long cursor=mapper.selectionCursor(batch);
  List<SmtAuthSelectionResource> rows=mapper.resources(batch,cursor,200);
  if(rows.isEmpty())return false;
  long sourceOrdinal=rows.get(0).getSourceOrdinal();
  int end=0;while(end<rows.size() && rows.get(end).getSourceOrdinal()==sourceOrdinal)end++;rows=new ArrayList<>(rows.subList(0,end));
  SmtAuthSelectionSource s=mapper.source(batch,sourceOrdinal);
  long next=rows.get(rows.size()-1).getOrdinal();
  List<SmtAuthSelectionResource> lookahead=mapper.resources(batch,next,1);
  boolean last=lookahead.isEmpty() || lookahead.get(0).getSourceOrdinal()!=sourceOrdinal;
  require(s!=null && registry.handler(s)!=null,"NEEDS_VERIFICATION：未注册来源类型或版本");
  for(SmtAuthSelectionResource row:rows) {
   require(Objects.equals(row.getParkId(),s.getParkId()) && Objects.equals(AuthSelectionSnapshots.resourceSubject(row),AuthSelectionSnapshots.subject(s)) && Objects.equals(row.getSubjectId(),s.getSubjectId()),"冻结来源与资源主体类型不一致");
   if(AuthSelectionSnapshots.version(s)==1)require(Objects.equals(row.getCredentialVersion(),1) && row.getCredentialSnapshot()!=null,"新来源必须使用版本化凭据快照");
  }
  SourceIntent intent=SourceIntent.builder().parkId(s.getParkId()).sourceKind(AuthSelectionSnapshots.kind(s)).stableKey(s.getStableKey())
   .subjectType(AuthSelectionSnapshots.subject(s)).subjectId(s.getSubjectId()).sourceRowId(s.getSourceRowId()).sourceFingerprint(s.getFingerprint())
   .intentKey(s.getOperationKey()+":"+sourceOrdinal).batchId(batch).action(s.getDesiredAction()).payloadSnapshot(("STAFF_AUTH".equals(AuthSelectionSnapshots.kind(s))?"EMPLOYEE_SELECTION:":"SELECTION:")+batch+":"+sourceOrdinal)
   .windows(AuthSelectionSnapshots.windows(s)).build();
  List<ResourceInput> inputs=rows.stream().map(AuthSelectionService::input).collect(Collectors.toList());
  Expanded expanded=workflow.stage(Shard.builder().batchId(batch).previousCursor(cursor).nextCursor(next).source(intent)
   .staffAuthId(s.getAuthId()).resources(inputs).finalSourcePage(last).build());
  // 工作流分片独立事务先提交；本事务绑定失败后仍按选择表未绑定游标重放相同分片。
  require(mapper.bindSource(batch,sourceOrdinal,expanded.getSource().getSourceId(),expanded.getSource().getGeneration())==1,"来源绑定未落库");
  for(int i=0;i<rows.size();i++)require(mapper.bindResource(batch,rows.get(i).getOrdinal(),expanded.getBindings().get(i).getResourceId())==1,"资源绑定未落库");
  return true;
 }
 /** 每次仅绑定一个去重物理 lane，最后由调用方显式 finish。 */
 public String bindNextLane(Long batch,String after) {
  require(mapper.unboundSelectionCount(batch)==0,"全部选择来源及资源持久绑定后才能创建物理目标");
  List<String> lanes=mapper.lanes(batch,after,1);if(lanes.isEmpty())return null;
  long cursor=mapper.cursor(batch);workflow.bindLane(batch,lanes.get(0),cursor,cursor+1);return lanes.get(0);
 }
 public List<Long> pendingExpansionBatches(List<Integer> parks,Long after,int limit) {require(parks!=null && !parks.isEmpty() && parks.size()<=200 && limit>0 && limit<=100,"展开批次分页参数无效");return mapper.pendingExpansionBatches(parks,after,limit);}
 public void finish(Long batch) {require(mapper.unboundSelectionCount(batch)==0,"选择投影尚未写完");workflow.finish(batch);}
 public List<SmtAuthSelectionSource> sourcesForTarget(Long targetId) {return mapper.sourcesForTarget(targetId);}
 public List<SmtAuthSelectionResource> resourcesForTarget(Long targetId) {return mapper.resourcesForTarget(targetId);}
 public int sourceVerificationCount(Long batch) {return mapper.sourceVerificationCount(batch);}
 public List<SmtAuthSelectionSource> verificationSources(Long batch,long after,int limit) {require(limit>0 && limit<=200,"核验来源分页上限200");return mapper.verificationSources(batch,after,limit);}
 /** 工作流完成证据门禁后按精确类型分派，handler 与完成标记在同一事务中。 */
 @Override @Transactional(rollbackFor=Exception.class)
 public boolean apply(SourceSnapshot snapshot) {
  SmtAuthSelectionSource s=mapper.exactSource(snapshot.getSourceId(),snapshot.getGeneration());
  if(s==null || registry.handler(s)==null || !Objects.equals(s.getFingerprint(),snapshot.getFingerprint())
   || !Objects.equals(s.getSourceRowId(),snapshot.getSourceRowId()) || !Objects.equals(AuthSelectionSnapshots.kind(s),snapshot.getSourceKind())
   || !Objects.equals(s.getSubjectId(),snapshot.getSubjectId()) || !Objects.equals(s.getStableKey(),snapshot.getStableKey())
   || (snapshot.getSubjectType()!=null && !Objects.equals(AuthSelectionSnapshots.subject(s),snapshot.getSubjectType()))
   || (AuthSelectionSnapshots.version(s)!=0 && snapshot.getSubjectType()==null)
   || (!"ADD".equals(s.getDesiredAction()) && !"DELETE".equals(s.getDesiredAction()))
   || (AuthSelectionSnapshots.version(s)!=0 && snapshot.getAction()==null)
   || (snapshot.getAction()!=null && !Objects.equals(s.getDesiredAction(),snapshot.getAction()))
   || mapper.unboundSelectionCount(s.getBatchId())!=0)return false;
  if("CONVERGED".equals(s.getState()))return true;
  if(!"PENDING".equals(s.getState()) || s.getVerificationReason()!=null)return false;
  if(!registry.apply(s))return false;
  // 业务 CAS 已成功时完成标记失败必须回滚，不能以 false 提交半次收敛。
  require(mapper.complete(s.getBatchId(),s.getOrdinal())==1,"来源完成标记未落库");return true;
 }
 private static ResourceInput input(SmtAuthSelectionResource r) {
  String type=AuthSelectionSnapshots.resourceSubject(r);
  if(r.getCredentialVersion()!=null && r.getCredentialVersion()!=0) {
   Credential credential=AuthSelectionSnapshots.credential(r.getCredentialVersion(),r.getCredentialSnapshot());
   validateCredentialCoordinate(credential,r.getResourceType(),r.getSubjectId(),r.getServiceType());
  }
  ResourceInput.ResourceInputBuilder b=ResourceInput.builder().resource(ResourceKey.builder().parkId(r.getParkId()).subjectType(type).subjectId(r.getSubjectId())
   .deviceId(r.getDeviceId()).accessType(r.getAccessType()).resourceType(r.getResourceType()).resourceId(r.getResourceId())
   .serviceType(r.getServiceType()).credentialChannel(r.getCredentialChannel()).build()).participation(r.getParticipation());
  if(r.getValidFrom()!=null)b.window(Window.builder().from(r.getValidFrom()).to(r.getValidTo()).build());return b.build();
 }
 private static void validateCredentialCoordinate(Credential credential,String type,String subjectId,String serviceType) {
  require((credential instanceof PersonCredential && "PERSON".equals(type)) || (credential instanceof VehicleCredential && "VEHICLE".equals(type)),"资源种类与冻结凭据不一致");
  String card=credential instanceof PersonCredential?((PersonCredential)credential).getTaskCardNo():((VehicleCredential)credential).getTaskCardNo();
  Integer service=credential instanceof PersonCredential?((PersonCredential)credential).getTaskServiceType():((VehicleCredential)credential).getTaskServiceType();
  require(Objects.equals(card,subjectId) && Objects.equals(String.valueOf(service),serviceType),"冻结凭据与目标任务身份不一致");
 }
 protected static void require(boolean b,String message){if(!b)throw new IllegalArgumentException(message);}
}
