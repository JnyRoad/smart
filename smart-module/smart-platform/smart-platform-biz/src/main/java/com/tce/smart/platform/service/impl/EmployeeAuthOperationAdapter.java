package com.tce.smart.platform.service.impl;

import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeAcceptance;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.Source;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.HistoryEvidence;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.ResourceInput;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import com.tce.smart.platform.core.service.impl.EmployeeAuthOperationService;
import com.tce.smart.platform.core.util.PermissionValidityWindow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 旧入口只构造业务差量，统一冻结服务器选择；所有查询均为本库批量查询。 */
@Service
public class EmployeeAuthOperationAdapter {
 private static final Object LOCK_SCOPE_KEY=new Object();
 private static final class LockScope {final Set<Integer> groups=new HashSet<>();boolean subjectsLocked;}
 private final AuthOperationProperties properties;
 private final EmployeeAuthOperationMapper mapper;
 private final EmployeeAuthOperationService operations;
 public EmployeeAuthOperationAdapter(AuthOperationProperties properties,EmployeeAuthOperationMapper mapper,EmployeeAuthOperationService operations) {
  this.properties=properties;this.mapper=mapper;this.operations=operations;
 }
 @Transactional(rollbackFor=Exception.class)
 public String update(int type,UpdateDeviceAuthDTO input) {
  if(!properties.isEnabled())return null;
  if(type<1 || type>3)throw new IllegalArgumentException("授权操作类型无效");
  List<Long> staff=input.getIds().stream().map(Long::valueOf).distinct().collect(Collectors.toList());
  PermissionValidityWindow window=type==3?null:PermissionValidityWindow.resolve(input.getStartTime(),input.getEndTime());
  return submit(staff,nullable(input.getDeviceAuthIds()),Collections.emptyList(),Collections.emptyList(),type,window,Collections.emptyMap(),null);
 }
 @Transactional(rollbackFor=Exception.class)
 public Boolean diff(List<Long> staffIds,List<Integer> add,List<Integer> remove) {
  if(!properties.isEnabled())return null;
  return submit(staffIds,nullable(add),nullable(remove),Collections.emptyList(),4,PermissionValidityWindow.resolve(null,null),Collections.emptyMap(),null)==null?null:true;
 }
 /** 组织内部调用必须携带从数据库读出的真实园区，并仍核调用者权限。 */
 @Transactional(rollbackFor=Exception.class)
 public Boolean organizationDiff(List<Long> staffIds,List<Integer> add,List<Integer> remove,Integer trustedPark) {
  if(!properties.enabledForPark(trustedPark))return null;
  Set<Integer> allowed=allowedParks();if(!allowed.contains(trustedPark))throw new SecurityException("无组织所属园区权限");
  return submit(staffIds,nullable(add),nullable(remove),Collections.emptyList(),4,PermissionValidityWindow.resolve(null,null),Collections.emptyMap(),Collections.singleton(trustedPark))!=null;
 }
 @Transactional(rollbackFor=Exception.class)
 public Boolean removeRows(List<Integer> ids,Integer authId) {
  return removeRowsOperation(ids,authId)==null?null:true;
 }
 /** 保留本次受理键；旧入口仍通过 Boolean 包装保持兼容。 */
 @Transactional(rollbackFor=Exception.class)
 public String removeRowsOperation(List<Integer> ids,Integer authId) {
  if(!properties.isEnabled())return null;
  List<SmtStaffDeviceAuth> rows=load(nullable(ids),mapper::rowsByIds);
  if(rows.size()!=new HashSet<>(nullable(ids)).size())throw new IllegalArgumentException("所选来源不存在，需刷新后核验");
  if(authId!=null && rows.stream().anyMatch(r->!authId.equals(r.getAuthId())))throw new SecurityException("来源不属于指定权限组");
  List<Long> staff=rows.stream().map(SmtStaffDeviceAuth::getStaffId).distinct().collect(Collectors.toList());
  return submit(staff,Collections.emptyList(),Collections.emptyList(),nullable(ids),5,null,Collections.emptyMap(),null);
 }
 @Transactional(rollbackFor=Exception.class)
 public Boolean removeAuthority(Integer authId) {
  return removeAuthorityOperation(authId)==null?null:true;
 }
 /** 空组没有产生批次，不能返回虚构操作键。 */
 @Transactional(rollbackFor=Exception.class)
 public String removeAuthorityOperation(Integer authId) {
  if(!enabledAuthority(authId))return null;
  List<SmtStaffDeviceAuth> rows=guardedAuthorityRows(authId);
  if(rows.isEmpty())return "NO_CHANGE";
  List<Long> ids=subjects(rows);
  return submitLocked(ids,Collections.emptyList(),Collections.emptyList(),rows.stream().map(SmtStaffDeviceAuth::getId).collect(Collectors.toList()),5,null,Collections.emptyMap(),null,Collections.emptyMap(),null,load(ids,mapper::staffSources));
 }
 /** 请求占位已开启事务；只接受服务端分配的操作键。 */
 @Transactional(rollbackFor=Exception.class)
 public AuthOperationIntakeAcceptance removeRowsOperation(List<Integer> rowIds,Integer authId,String operationKey) {
  requireKeyedAuthority(authId,operationKey);
  List<Integer> selected=new ArrayList<>(new TreeSet<>(rowIds));
  List<SmtStaffDeviceAuth> rows=load(selected,mapper::rowsByIds);
  validateIntakeRows(rows,selected,authId);
  List<Long> ids=subjects(rows);
  List<SmtStaffDeviceAuth> old=lockSelection(ids,Collections.singleton(authId));
  // 锁后重读精确选中行，不能把已替换或已删除来源当成本次撤销。
  List<SmtStaffDeviceAuth> currentRows=load(selected,mapper::rowsByIds);
  validateIntakeRows(currentRows,selected,authId);
  if(!rows.stream().collect(Collectors.toMap(SmtStaffDeviceAuth::getId,SmtStaffDeviceAuth::getStaffId))
    .equals(currentRows.stream().collect(Collectors.toMap(SmtStaffDeviceAuth::getId,SmtStaffDeviceAuth::getStaffId))))
   throw new IllegalStateException("锁后所选来源主体已变化，请刷新核验");
  requireKeyedAuthority(authId,operationKey);
  return requireKeyedResult(submitLockedResult(ids,Collections.emptyList(),Collections.emptyList(),selected,5,null,
    Collections.emptyMap(),null,Collections.emptyMap(),null,old,operationKey));
 }
 @Transactional(rollbackFor=Exception.class)
 public AuthOperationIntakeAcceptance removeAuthorityOperation(Integer authId,String operationKey) {
  requireKeyedAuthority(authId,operationKey);
  List<SmtStaffDeviceAuth> rows=guardedAuthorityRows(authId);
  // 园区与类型必须来自已加锁的真实组，零成员不能丢失授权范围。
  requireKeyedAuthority(authId,operationKey);
  List<SmtDeviceAuthority> groups=mapper.authorities(Collections.singletonList(authId));
  if(groups.size()!=1 || !allowedParks().contains(groups.get(0).getParkId()))throw new SecurityException("权限组范围变化");
  if(rows.isEmpty())return AuthOperationIntakeAcceptance.builder().operationKey(operationKey).outcome("NO_CHANGE")
    .scopeParkId(groups.get(0).getParkId()).build();
  List<Long> ids=subjects(rows);
  return requireKeyedResult(submitLockedResult(ids,Collections.emptyList(),Collections.emptyList(),rows.stream().map(SmtStaffDeviceAuth::getId).collect(Collectors.toList()),
    5,null,Collections.emptyMap(),null,Collections.emptyMap(),null,load(ids,mapper::staffSources),operationKey));
 }
 private void requireKeyedAuthority(Integer authId,String operationKey) {
  if(operationKey==null || operationKey.trim().isEmpty())throw new IllegalArgumentException("服务端操作键缺失");
  if(!enabledAuthority(authId))throw new EmployeeAuthIntakeService.IntakeException("KEYED_UNSUPPORTED");
 }
 private static AuthOperationIntakeAcceptance requireKeyedResult(AuthOperationIntakeAcceptance result) {
  if(result==null)throw new EmployeeAuthIntakeService.IntakeException("KEYED_UNSUPPORTED");return result;
 }
 private static void validateIntakeRows(List<SmtStaffDeviceAuth> rows,List<Integer> selected,Integer authId) {
  if(!rows.stream().map(SmtStaffDeviceAuth::getId).collect(Collectors.toSet()).equals(new HashSet<>(selected)) || rows.size()!=selected.size())
   throw new IllegalArgumentException("所选来源不存在，需刷新后核验");
  if(rows.stream().anyMatch(row->!authId.equals(row.getAuthId())))throw new SecurityException("来源不属于指定权限组");
 }
 @Transactional(rollbackFor=Exception.class)
 public Boolean authorityDevices(Integer authId,List<String> devices) {
  if(!enabledAuthority(authId))return null;
  List<SmtStaffDeviceAuth> rows=guardedAuthorityRows(authId);
  checkNewAuthority(mapper.authorities(Collections.singletonList(authId)).get(0).getParkId(),nullable(devices));
  if(rows.isEmpty())return true;
  List<Long> ids=subjects(rows);
  return submitLocked(ids,Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),3,null,Collections.singletonMap(authId,nullable(devices)),null,Collections.emptyMap(),null,load(ids,mapper::staffSources))!=null;
 }
 @Transactional(rollbackFor=Exception.class)
 public Boolean addSource(SmtStaffDeviceAuth source) {
  if(!enabledAuthority(source.getAuthId()))return null;
  if(source.getAuthType()!=null && source.getAuthType()!=1 && source.getAuthType()!=2)throw new IllegalArgumentException("员工授权类型无效");
  PermissionValidityWindow w=PermissionValidityWindow.resolve(source.getStartTime()==null?null:date(source.getStartTime()).toString(),source.getEndTime()==null?null:date(source.getEndTime()).toString());
  return submit(Collections.singletonList(source.getStaffId()),Collections.singletonList(source.getAuthId()),Collections.emptyList(),Collections.emptyList(),1,w,Collections.emptyMap(),null,Collections.emptyMap(),source.getAuthType())!=null;
 }
 @Transactional(rollbackFor=Exception.class)
 public List<String> addBadges(Integer authId,List<String> badges,String start,String end) {
  if(!enabledAuthority(authId))return null;
  List<SmtStaff> rows=load(new ArrayList<>(new LinkedHashSet<>(badges)),mapper::staffByBadges);
  Set<String> found=rows.stream().map(SmtStaff::getBadge).collect(Collectors.toSet());
  if(!rows.isEmpty()) {
   UpdateDeviceAuthDTO dto=new UpdateDeviceAuthDTO();dto.setIds(rows.stream().map(s->String.valueOf(s.getId())).collect(Collectors.toList()));
   dto.setDeviceAuthIds(Collections.singletonList(authId));dto.setStartTime(start);dto.setEndTime(end);update(1,dto);
  }
  return badges.stream().filter(b->!found.contains(b)).collect(Collectors.toList());
 }
 public boolean enabledAuthority(Integer id) {
  if(!properties.isEnabled())return false;
  List<SmtDeviceAuthority> rows=mapper.authorities(Collections.singletonList(id));
  if(rows.size()!=1)throw new IllegalArgumentException("权限组不存在");
  SmtDeviceAuthority authority=rows.get(0);
  if(!allowedParks().contains(authority.getParkId()))throw new SecurityException("无权限组所属园区权限");
  return com.tce.smart.tool.enums.DeviceAuthTypeEnum.PERSON.getCode().equals(authority.getType()) && properties.enabledForPark(authority.getParkId());
 }
 public Integer guardedAuthorityPark(Integer id,Integer requestedPark,List<Integer> requestScope) {
  if(!properties.isEnabled())return null;
  List<SmtDeviceAuthority> rows=mapper.authorities(Collections.singletonList(id));if(rows.size()!=1)throw new IllegalArgumentException("权限组不存在");
  Integer park=rows.get(0).getParkId();if(!allowedParks().contains(park) || requestScope==null || !requestScope.contains(park) || (requestedPark!=null && !requestedPark.equals(park)))throw new SecurityException("权限组所属园区不能由请求替换");return park;
 }
 public void checkNewAuthority(Integer park,List<String> devices) {
  if(!properties.isEnabled())return;if(!allowedParks().contains(park))throw new SecurityException("无新权限组园区权限");
  List<SmtDevice> rows=load(devices,mapper::devices);if(rows.size()!=new HashSet<>(devices).size() || rows.stream().anyMatch(d->!Objects.equals(park,d.getParkId())))throw new SecurityException("新权限组设备不属于指定园区");
 }
 @Transactional(rollbackFor=Exception.class)
 public void guardAuthorityDeletion(Integer id) {
  if(!properties.isEnabled())return;
  checkAuthority(id);guardedAuthorityRows(id);
 }
 private List<SmtStaffDeviceAuth> guardedAuthorityRows(Integer id) {
  List<Long> selected=subjects(mapper.sourcesByAuthority(id));
  lockSelection(selected,Collections.singleton(id));
  List<SmtStaffDeviceAuth> rows=mapper.sourcesByAuthority(id);
  if(!selected.equals(subjects(rows)))throw new IllegalStateException("权限组成员在门禁前变化，请重试");
  Integer park=mapper.authorities(Collections.singletonList(id)).get(0).getParkId();
  if(mapper.pendingAuthority(park,String.valueOf(id))>0)throw new IllegalStateException("权限组仍有已受理来源，不能删除或改变设备选择");
  return rows;
 }
 /** 旧同步不依赖用户或灰度开关；锁主体后禁止删除任何园区的在途来源。 */
 @Transactional(rollbackFor=Exception.class)
 public void guardLegacyDeletion(List<SmtStaffDeviceAuth> rows) {
  LockScope scope=lockScope();
  for(List<Long> part:parts(subjects(rows),200)) {
   scope.subjectsLocked=true;
   if(mapper.lockSubjects(part).size()!=part.size())throw new IllegalStateException("来源员工缺失，不能物理删除");
   if(mapper.pendingAnySubjects(part)>0)throw new IllegalStateException("PENDING_SELECTION：旧删除必须等待可靠来源收敛");
  }
 }
 private List<SmtStaffDeviceAuth> lockSelection(List<Long> ids,Collection<Integer> requested) {
  Set<Integer> groups=new TreeSet<>(requested);
  for(SmtStaffDeviceAuth r:load(ids,mapper::staffSources))groups.add(r.getAuthId());
  LockScope scope=lockScope();
  if((scope.subjectsLocked || !scope.groups.isEmpty()) && !scope.groups.containsAll(groups))throw new IllegalStateException("同事务不能扩展已取得的锁集合；请按完整组集合重试");
  for(List<Integer> part:parts(new ArrayList<>(groups),200))if(mapper.lockAuthorities(part).size()!=part.size())throw new IllegalStateException("权限组已变化，来源保留并重试");
  scope.groups.addAll(groups);scope.subjectsLocked|=!ids.isEmpty();
  for(List<Long> part:parts(ids,200))if(mapper.lockSubjects(part).size()!=part.size())throw new IllegalArgumentException("选中员工不存在");
  List<SmtStaffDeviceAuth> rows=load(ids,mapper::staffSources);
  for(SmtStaffDeviceAuth row:rows)if(!groups.contains(row.getAuthId()))throw new IllegalStateException("员工来源扩展到未锁权限组，请回滚重试");
  return rows;
 }
 private static LockScope lockScope() {
  if(!org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive())return new LockScope();
  LockScope existing=(LockScope)org.springframework.transaction.support.TransactionSynchronizationManager.getResource(LOCK_SCOPE_KEY);
  if(existing!=null)return existing;
  LockScope created=new LockScope();org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(LOCK_SCOPE_KEY,created);
  org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronizationAdapter(){
   @Override public void suspend(){org.springframework.transaction.support.TransactionSynchronizationManager.unbindResourceIfPossible(LOCK_SCOPE_KEY);}
   @Override public void resume(){org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(LOCK_SCOPE_KEY,created);}
   @Override public void afterCompletion(int status){org.springframework.transaction.support.TransactionSynchronizationManager.unbindResourceIfPossible(LOCK_SCOPE_KEY);}
  });
  return created;
 }
 private static List<Long> subjects(List<SmtStaffDeviceAuth> rows) {return rows.stream().map(SmtStaffDeviceAuth::getStaffId).distinct().sorted().collect(Collectors.toList());}
 public boolean isEnabled(){return properties.isEnabled();}
 public void checkAuthority(Integer id) {if(properties.isEnabled())enabledAuthority(id);}
 protected Set<Integer> allowedParks() {
  if(SecurityUtils.getUser()==null || SecurityUtils.getUser().getParkIdList()==null || SecurityUtils.getUser().getParkIdList().isEmpty())
   throw new SecurityException("缺少明确的允许园区范围");
  return new HashSet<>(SecurityUtils.getUser().getParkIdList());
 }
 @Transactional(rollbackFor=Exception.class)
 public Boolean revokeDevice(Integer authorityId,String deviceId) {
  if(!enabledAuthority(authorityId))return null;
  List<SmtStaffDeviceAuth> rows=guardedAuthorityRows(authorityId);if(rows.isEmpty())return true;
  List<String> desired=mapper.authorityDevices(Collections.singletonList(authorityId)).stream().map(SmtDeviceAuthorityRelation::getDeviceId).filter(id->!Objects.equals(id,deviceId)).collect(Collectors.toList());
  List<Long> ids=subjects(rows);
  return submitLocked(ids,Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),3,null,
    Collections.singletonMap(authorityId,desired),null,Collections.singletonMap(authorityId,Collections.singletonList(deviceId)),null,load(ids,mapper::staffSources))!=null;
 }
 private String submit(List<Long> ids,List<Integer> adds,List<Integer> removes,List<Integer> deleteRows,int mode,
   PermissionValidityWindow window,Map<Integer,List<String>> overrides,Set<Integer> trustedScope) {
  return submit(ids,adds,removes,deleteRows,mode,window,overrides,trustedScope,Collections.emptyMap());
 }
 private String submit(List<Long> ids,List<Integer> adds,List<Integer> removes,List<Integer> deleteRows,int mode,
   PermissionValidityWindow window,Map<Integer,List<String>> overrides,Set<Integer> trustedScope,Map<Integer,List<String>> additionalOldDevices) {
  return submit(ids,adds,removes,deleteRows,mode,window,overrides,trustedScope,additionalOldDevices,null);
 }
 private String submit(List<Long> ids,List<Integer> adds,List<Integer> removes,List<Integer> deleteRows,int mode,
   PermissionValidityWindow window,Map<Integer,List<String>> overrides,Set<Integer> trustedScope,Map<Integer,List<String>> additionalOldDevices,Integer requestedAuthType) {
  if(ids.isEmpty())return "NO_CHANGE";
  ids=ids.stream().distinct().sorted().collect(Collectors.toList());
  Set<Integer> requested=new TreeSet<>(adds);requested.addAll(removes);requested.addAll(overrides.keySet());
  List<SmtStaffDeviceAuth> old=lockSelection(ids,requested);
  return submitLocked(ids,adds,removes,deleteRows,mode,window,overrides,trustedScope,additionalOldDevices,requestedAuthType,old);
 }
 /** 已持有完整组锁再持有主体锁；此方法不补拿任何组锁，避免反向锁顺序。 */
 private String submitLocked(List<Long> ids,List<Integer> adds,List<Integer> removes,List<Integer> deleteRows,int mode,
   PermissionValidityWindow window,Map<Integer,List<String>> overrides,Set<Integer> trustedScope,Map<Integer,List<String>> additionalOldDevices,Integer requestedAuthType,List<SmtStaffDeviceAuth> old) {
  AuthOperationIntakeAcceptance result=submitLockedResult(ids,adds,removes,deleteRows,mode,window,overrides,trustedScope,additionalOldDevices,requestedAuthType,old,UUID.randomUUID().toString());
  return result==null?null:"NO_CHANGE".equals(result.getOutcome())?"NO_CHANGE":result.getOperationKey();
 }
 private AuthOperationIntakeAcceptance submitLockedResult(List<Long> ids,List<Integer> adds,List<Integer> removes,List<Integer> deleteRows,int mode,
   PermissionValidityWindow window,Map<Integer,List<String>> overrides,Set<Integer> trustedScope,Map<Integer,List<String>> additionalOldDevices,Integer requestedAuthType,List<SmtStaffDeviceAuth> old,String operationKey) {
  Set<Integer> authIds=old.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toSet());authIds.addAll(adds);authIds.addAll(removes);authIds.addAll(overrides.keySet());
  Map<Integer,SmtDeviceAuthority> authorities=load(new ArrayList<>(authIds),mapper::authorities).stream().collect(Collectors.toMap(SmtDeviceAuthority::getId,Function.identity()));
  if(authorities.size()!=authIds.size())throw new IllegalArgumentException("权限组缺失，来源保留待核验");
  Set<Integer> parks=authorities.values().stream().map(SmtDeviceAuthority::getParkId).collect(Collectors.toSet());
  if(parks.stream().noneMatch(properties::enabledForPark))return null;
  if(parks.stream().anyMatch(p->!properties.enabledForPark(p)))throw new IllegalStateException("同次操作涉及未启用园区，请按园区拆分");
  Set<Integer> allowed=allowedParks();if(!allowed.containsAll(parks) || (trustedScope!=null && !trustedScope.containsAll(parks)))throw new SecurityException("所选来源超出允许园区");
  Map<String,Set<Integer>> memberships=new HashMap<>();
  for(SmtAuthSelectionSource row:load(ids,mapper::staffMemberships))memberships.computeIfAbsent(row.getSubjectId(),k->new HashSet<>()).add(row.getParkId());
  Map<Long,SmtStaff> staff=load(ids,mapper::staff).stream().collect(Collectors.toMap(SmtStaff::getId,Function.identity()));
  List<SmtDeviceAuthorityRelation> relations=load(new ArrayList<>(authIds),mapper::authorityDevices);
  Set<String> deviceIds=relations.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toSet());overrides.values().forEach(deviceIds::addAll);
  List<SmtAuthSelectionResource> historical=new ArrayList<>();
  for(List<Long> part:parts(ids,200))historical.addAll(mapper.historicalResources(part,new ArrayList<>(parks)));
  List<HistoryEvidence> historicalEvidence=new ArrayList<>();
  for(List<Long> part:parts(ids,200))historicalEvidence.addAll(mapper.historicalReviewEvidence(part,new ArrayList<>(parks)));
  historical.forEach(h->deviceIds.add(h.getDeviceId()));
  historicalEvidence.forEach(h->deviceIds.add(h.getDeviceId()));deviceIds.remove(null);
  Map<String,SmtDevice> devices=load(new ArrayList<>(deviceIds),mapper::devices).stream().collect(Collectors.toMap(SmtDevice::getId,Function.identity()));
  Map<String,String> unmapped=new HashMap<>();
  for(SmtAuthSelectionSource missing:load(ids,mapper::unmappedTaskSubjects))unmapped.merge(missing.getSubjectId(),missing.getVerificationReason(),(a,b)->a+";"+b);
  Map<String,List<SmtAuthSelectionResource>> historicalBySubject=historical.stream().collect(Collectors.groupingBy(h->h.getParkId()+":"+h.getSubjectId()));
  Map<Long,List<SmtStaffDeviceAuth>> current=old.stream().collect(Collectors.groupingBy(SmtStaffDeviceAuth::getStaffId));
  Map<Integer,List<String>> byAuthority=relations.stream().collect(Collectors.groupingBy(SmtDeviceAuthorityRelation::getAuthorityId,Collectors.mapping(SmtDeviceAuthorityRelation::getDeviceId,Collectors.toList())));
  List<Source> frozen=new ArrayList<>();
  for(Long id:ids) {
   SmtStaff person=staff.get(id);if(person==null)throw new IllegalArgumentException("员工不存在，不能清理来源");
   Map<Integer,SmtStaffDeviceAuth> before=new LinkedHashMap<>();
   for(SmtStaffDeviceAuth row:current.getOrDefault(id,Collections.emptyList()))if(before.put(row.getAuthId(),row)!=null)throw new IllegalArgumentException("员工同权限组有重复来源，需先核验");
   Map<Integer,SmtStaffDeviceAuth> after=new LinkedHashMap<>();
   if(mode!=2)after.putAll(before);
   if(mode==4)removes.forEach(after::remove);
   if(mode==5)for(SmtStaffDeviceAuth row:before.values())if(deleteRows.contains(row.getId()))after.remove(row.getAuthId());
   if(mode!=3 && mode!=5)for(Integer auth:adds)if(mode==2 || !after.containsKey(auth)) {
    SmtStaffDeviceAuth added=newRow(id,auth,window);Integer authType=requestedAuthType;
    if(authType==null)authType=before.containsKey(auth)?before.get(auth).getAuthType():Integer.valueOf(Integer.valueOf(1).equals(authorities.get(auth).getAreaType())?2:1);
    added.setAuthType(authType);after.put(auth,added);
   }
   Set<Integer> all=new LinkedHashSet<>(before.keySet());all.addAll(after.keySet());
   for(Integer auth:all) {
    int park=authorities.get(auth).getParkId();
    if(!memberships.getOrDefault(String.valueOf(id),Collections.emptySet()).contains(park))throw new SecurityException("员工所属园区与权限组不一致");
    SmtStaffDeviceAuth oldRow=before.get(auth),newRow=after.get(auth);
    if(newRow!=null) {
     if(com.tce.smart.tool.enums.StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(person.getStatus()))throw new IllegalStateException("离职员工授权尚未接入本阶段");
     newRow=copyWithWindow(newRow);
    }
    List<String> oldDevices=new ArrayList<>(byAuthority.getOrDefault(auth,Collections.emptyList()));oldDevices.addAll(additionalOldDevices.getOrDefault(auth,Collections.emptyList()));
    List<String> newDevices=newRow==null?Collections.emptyList():overrides.getOrDefault(auth,oldDevices);
    Set<String> desiredDevices=new HashSet<>(newDevices);
    Set<String> allDevices=new LinkedHashSet<>(oldDevices);allDevices.addAll(newDevices);
    Map<String,ResourceInput> resources=new LinkedHashMap<>();
    Set<String> reviewReasons=new TreeSet<>();
    if(newRow!=null && (person.getFacePicId()==null || person.getFacePicId().trim().isEmpty()))reviewReasons.add("MISSING_CREDENTIAL_REFERENCE");
    List<HistoryEvidence> evidence=new ArrayList<>();Map<String,String> rawRows=new HashMap<>();
    for(HistoryEvidence raw:historicalEvidence)if(Objects.equals(raw.getSubjectId(),String.valueOf(id)) && (raw.getParkId()==null || Objects.equals(raw.getParkId(),park))) {
     if("RESOURCE_COORD".equals(raw.getOrigin()) && "7".equals(raw.getServiceType()) && "PERSON".equals(raw.getResourceType()) && "FACE".equals(raw.getCredentialChannel()))throw new IllegalStateException("RELEASE_BLOCKED_NONCANONICAL_COORD:"+raw.getRowId());
     String rowKey=raw.getOrigin()+":"+raw.getRowId();String rawHash=com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.rawFingerprint(raw);String previous=rawRows.putIfAbsent(rowKey,rawHash);
     if(previous!=null){if(!previous.equals(rawHash))throw new IllegalStateException("HISTORY_EVIDENCE_CONFLICT:"+rowKey);continue;}
     Set<String> reasons=historyReasons(raw.getServiceType(),raw.getParkId(),raw.getDeviceId(),raw.getAccessType(),devices);
     reviewReasons.addAll(reasons);evidence.add(raw.toBuilder().reviewCode(reasons.isEmpty()?null:String.join(";",reasons)).build());
    }
    for(String deviceId:allDevices) {
     SmtDevice d=devices.get(deviceId);
     if(d==null){reviewReasons.add("MISSING_DEVICE_COORDINATE");continue;}
     if(!Objects.equals(park,d.getParkId())) {
      // 仅撤去有本园区原始行佐证的旧设备；显式请求保留或新增的设备仍必须通过园区校验。
      boolean historicalRemoval=oldRow!=null && !desiredDevices.contains(deviceId) && evidence.stream().anyMatch(e->
       e.getRowId()!=null && Objects.equals(e.getParkId(),park) && Objects.equals(e.getDeviceId(),deviceId));
      if(historicalRemoval){reviewReasons.add("MISSING_DEVICE_COORDINATE");continue;}
      throw new SecurityException("设备所属园区与来源不一致");
     }
     ResourceKey k=ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(String.valueOf(id)).deviceId(deviceId)
      .accessType(Integer.valueOf(1).equals(d.getIsSync())?"ISC":"DIRECT").resourceType("PERSON").resourceId(String.valueOf(id))
      .serviceType("1").credentialChannel("FACE").build();
     put(resources,k,desiredDevices.contains(deviceId),newRow);
    }
    for(SmtAuthSelectionResource h:historicalBySubject.getOrDefault(park+":"+id,Collections.emptyList())) {
     Set<String> reasons=historyReasons(h.getServiceType(),h.getParkId(),h.getDeviceId(),h.getAccessType(),devices);
     reviewReasons.addAll(reasons);
     if(!reasons.isEmpty())continue;
     ResourceKey k=ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(String.valueOf(id)).deviceId(h.getDeviceId()).accessType(h.getAccessType())
      .resourceType(h.getResourceType()).resourceId(h.getResourceId()).serviceType("PERSON".equals(h.getResourceType()) && "FACE".equals(h.getCredentialChannel()) && ("1".equals(h.getServiceType()) || "7".equals(h.getServiceType()))?"1":h.getServiceType()).credentialChannel(h.getCredentialChannel()).build();
     // 历史接入或别名坐标只保留原本存在的通道；当前设备没有该授权时明确排除。
     put(resources,k,desiredDevices.contains(h.getDeviceId()),newRow);
    }
    if(unmapped.containsKey(String.valueOf(id)))reviewReasons.add("MISSING_DEVICE_HISTORY");
    if(resources.isEmpty())reviewReasons.add("MISSING_DEVICE_COORDINATE");
    String verificationReason=reviewReason(reviewReasons);
    frozen.add(Source.builder().parkId(park).subjectId(String.valueOf(id)).authId(String.valueOf(auth)).before(oldRow).after(newRow)
     .imageId(person.getFacePicId()).badge(person.getBadge()).personSnapshot(cn.hutool.json.JSONUtil.createObj().put("personName",person.getName()).put("gender",person.getSex()==null?0:person.getSex()==0?1:2).toString()).verificationReason(verificationReason).historyEvidence(evidence).resources(new ArrayList<>(resources.values())).build());
   }
  }
  // 权限组/员工锁不覆盖设备和路由；发现本次读取期间的变化只冻结核验，后续外发仍须重验实际路由。
  Map<String,SmtDevice> checked=load(new ArrayList<>(deviceIds),mapper::devices).stream().collect(Collectors.toMap(SmtDevice::getId,Function.identity()));
  Set<String> changedDevices=new HashSet<>();for(String device:deviceIds)if(!Objects.equals(deviceBasis(devices.get(device)),deviceBasis(checked.get(device))))changedDevices.add(device);
  Set<String> changedSubjects=new HashSet<>();for(Source source:frozen)if(source.getResources().stream().anyMatch(r->changedDevices.contains(r.getResource().getDeviceId())) || source.getHistoryEvidence().stream().anyMatch(e->changedDevices.contains(e.getDeviceId())))changedSubjects.add(source.getParkId()+":"+source.getSubjectId());
  for(int i=0;i<frozen.size();i++){Source source=frozen.get(i);if(changedSubjects.contains(source.getParkId()+":"+source.getSubjectId())){Set<String> reasons=new TreeSet<>();if(source.getVerificationReason()!=null)reasons.addAll(Arrays.asList(source.getVerificationReason().split(";")));reasons.add("MISSING_DEVICE_COORDINATE:DEVICE_CHANGED_DURING_FREEZE");frozen.set(i,source.toBuilder().verificationReason(reviewReason(reasons)).build());}}
  AuthOperationIntakeAcceptance.AuthOperationIntakeAcceptanceBuilder result=AuthOperationIntakeAcceptance.builder().operationKey(operationKey).scopeParkIds(parks);
  if(frozen.isEmpty())return result.outcome("NO_CHANGE").build();
  com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.Accepted accepted=operations.accept(operationKey,frozen,allowed);
  result.operationKey(accepted.getOperationKey()).outcome("ACCEPTED");
  accepted.getBatches().forEach((park,batches)->batches.forEach(batch->result.batch(batch,park)));
  return result.build();
 }
 /** APP 完善缺业务证明；历史坐标必须还能映射到同园区、同接入的真实设备。 */
 private static Set<String> historyReasons(String service,Integer park,String device,String access,Map<String,SmtDevice> devices) {
  Set<String> reasons=new TreeSet<>();
  if(String.valueOf(DeviceTaskConstants.CARD_APP_PERFECT).equals(service))reasons.add("APP_PERFECT_REVIEW");
  else if(!String.valueOf(DeviceTaskConstants.CARD_STAFF_IMPORT).equals(service) && !String.valueOf(DeviceTaskConstants.UPDATE_FACE).equals(service))reasons.add("MISSING_DEVICE_HISTORY:UNSUPPORTED_SERVICE");
  SmtDevice current=devices.get(device);
  if(park==null || current==null || !Objects.equals(park,current.getParkId()) ||
    !Objects.equals(access,Integer.valueOf(1).equals(current.getIsSync())?"ISC":"DIRECT"))reasons.add("MISSING_DEVICE_COORDINATE");
  return reasons;
 }
 private static List<Object> deviceBasis(SmtDevice device){return device==null?null:Arrays.asList(device.getParkId(),device.getIsSync(),device.getChannelNo(),device.getDeviceCapability());}
 private static String reviewReason(Set<String> reasons) {
  if(reasons.isEmpty())return null;
  List<String> ordered=new ArrayList<>(reasons);
  // 短码不容纳原始行列表；所有问题明细进入冻结 CLOB，主码优先保持可隔离原因。
  ordered.sort(Comparator.comparingInt((String r)->r.startsWith("APP_PERFECT_REVIEW")?0:r.startsWith("MISSING_DEVICE_HISTORY")?1:r.startsWith("MISSING_DEVICE")?2:3).thenComparing(Function.identity()));
  return String.join(";",ordered);
 }
 private static void put(Map<String,ResourceInput> out,ResourceKey key,boolean include,SmtStaffDeviceAuth after) {
  ResourceInput.ResourceInputBuilder b=ResourceInput.builder().resource(key).participation(include?"INCLUDE":"EXCLUDE");
  if(include)b.window(Window.builder().from(date(after.getStartTime()).atStartOfDay()).to(date(after.getEndTime()).plusDays(1).atStartOfDay().minusSeconds(1)).build());
  ResourceInput value=b.build();ResourceInput previous=out.putIfAbsent(key.toString(),value);
  if(previous!=null && (!Objects.equals(previous.getParticipation(),value.getParticipation()) || !Objects.equals(previous.getWindows(),value.getWindows())))throw new IllegalStateException("CANONICAL_SELECTION_CONFLICT:"+key.getDeviceId());
 }
 private static SmtStaffDeviceAuth newRow(Long id,Integer auth,PermissionValidityWindow w) {SmtStaffDeviceAuth r=new SmtStaffDeviceAuth();r.setStaffId(id);r.setAuthId(auth);r.setCreateTime(new Date());r.setStartTime(w.getStartDateTime());r.setEndTime(w.getEndDateTime());return r;}
 private static SmtStaffDeviceAuth copyWithWindow(SmtStaffDeviceAuth r) {SmtStaffDeviceAuth copy=new SmtStaffDeviceAuth();org.springframework.beans.BeanUtils.copyProperties(r,copy);PermissionValidityWindow w=PermissionValidityWindow.resolve(r.getStartTime()==null?null:date(r.getStartTime()).toString(),r.getEndTime()==null?null:date(r.getEndTime()).toString());copy.setStartTime(w.getStartDateTime());copy.setEndTime(w.getEndDateTime());return copy;}
 private static LocalDate date(Date d){return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();}
 private static <T> List<T> nullable(List<T> x){return x==null?Collections.emptyList():x;}
 private static <T,R> List<R> load(List<T> ids,Function<List<T>,List<R>> loader){List<R> out=new ArrayList<>();for(List<T> part:parts(ids,200))out.addAll(loader.apply(part));return out;}
 private static <T> List<List<T>> parts(List<T> xs,int size){List<List<T>> out=new ArrayList<>();for(int i=0;i<xs.size();i+=size)out.add(xs.subList(i,Math.min(i+size,xs.size())));return out;}
}
