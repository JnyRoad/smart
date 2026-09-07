package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.authselection.*;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.dto.authselection.VisitorAuthSnapshot.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.VisitorAuthOperationMapper;
import org.springframework.transaction.annotation.*;
import java.util.*;
import java.time.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.Window;
import org.springframework.beans.factory.annotation.Value;
/** 三个固定来源共用的权限事实核验；不负责设备发送、父状态写回或预约删除。 */
abstract class VisitorAuthSourceSupport implements AuthSourceHandler<VisitorAuthSnapshot> {
 protected final VisitorAuthOperationMapper mapper;
 @Value("${spring.visitor.put-offset-hour:2}") private int leadHours=2;
 VisitorAuthSourceSupport(VisitorAuthOperationMapper mapper){this.mapper=Objects.requireNonNull(mapper);}
 public int snapshotVersion(){return 1;}
 public Class<VisitorAuthSnapshot> snapshotType(){return VisitorAuthSnapshot.class;}
 /** 外部受理事务先持父锁，再按随行ID持子锁；此处不获取版本主体锁。 */
 @Transactional(propagation=Propagation.MANDATORY,rollbackFor=Exception.class)
 public void lockAndValidate(SourceSelection<VisitorAuthSnapshot> s){
  require(s!=null && s.getSourceKind()==sourceKind() && s.getSubjectType()==subjectType() && s.getSnapshotVersion()==1,"访客来源类型不匹配");
  VisitorAuthSnapshot b=s.getBusiness();require(identity(s.getParkId(),s.getSubjectId(),s.getSourceRowId(),s.getStableKey(),s.getParentKind(),s.getParentRowId(),b),"访客来源或父归属不一致");
  SmtVisitor parent=lockAndMatch(b);require(parent!=null,"访客权限事实已变化");
  require(Objects.equals(b.getEvidence().getRawStatus(),parent.getStatus()) && Objects.equals(b.getEvidence().getRawDelFlag(),parent.getDelFlag()),"受理审计不是当前父行事实");
  require(Objects.equals(b.getEvidence().getLeadHours(),leadHours),"提前有效期必须来自服务器配置");
  require(selectionWindow(s,b),"来源有效期与父预约或设备资源不一致");
  require(permit(s.getAction(),b,parent,true),"访客授权依据或触发范围不成立");
 }
 /** 工作流已先锁版本主体；这里只锁父与子核验，不改变业务状态或抹除历史。 */
 @Transactional(propagation=Propagation.MANDATORY,rollbackFor=Exception.class)
 public boolean applyExact(SmtAuthSelectionSource s,VisitorAuthSnapshot b){
  if(s==null || !sourceKind().name().equals(s.getSourceKind()) || !subjectType().name().equals(s.getSubjectType()) || !Objects.equals(s.getSnapshotVersion(),1)
   || !"PENDING".equals(s.getState()) || s.getVerificationReason()!=null || s.getBatchId()==null || s.getOrdinal()==null
   || !identity(s.getParkId(),s.getSubjectId(),s.getSourceRowId(),s.getStableKey(),s.getParentKind(),s.getParentRowId(),b))return false;
  SmtAuthSelectionSource stored=mapper.frozenSource(s.getBatchId(),s.getOrdinal());
  // 不能以调用方临时构造的批准DTO替代本库受理时保存的批准事实。
  if(stored==null || !stored.equals(s) || !Objects.equals(b,AuthSelectionSnapshots.business(stored,VisitorAuthSnapshot.class)))return false;
  SmtVisitor parent=lockAndMatch(b);return parent!=null && permit(s.getDesiredAction(),b,parent,false);
 }
 private SmtVisitor lockAndMatch(VisitorAuthSnapshot b){
  ParentFacts p=b.getParent();SmtVisitor live=mapper.lockParent(p.getParkId(),p.getId());
  if(live==null || !p.equals(parentFacts(live)))return null;
  if(sourceKind()==SourceKind.VISITOR_FELLOW){SmtFellowVisitor f=mapper.lockFellow(p.getId(),b.getFellow().getId());if(f==null || !b.getFellow().equals(fellowFacts(f)))return null;}
  return live;
 }
 private boolean identity(Integer park,String subject,String row,String stable,String parentKind,String parentRow,VisitorAuthSnapshot b){
  if(b==null || b.getParent()==null || b.getEvidence()==null || b.getEvidence().getLeadHours()==null || b.getEvidence().getLeadHours()<0)return false;
  ParentFacts p=b.getParent();if(p.getId()==null || p.getId()<=0 || p.getParkId()==null || p.getParkId()<=0 || p.getCreateTime()==null || p.getStartTime()==null || p.getEndTime()==null || !validTimes(p))return false;
  if(!Objects.equals(park,p.getParkId()) || !"VISITOR".equals(parentKind) || !p.getId().toString().equals(parentRow))return false;
  Long id=p.getId();String expected=part(id);
  if(sourceKind()==SourceKind.VISITOR_FELLOW){FellowFacts f=b.getFellow();if(f==null || f.getId()==null || f.getId()<=0 || !p.getId().equals(f.getVisitorId()))return false;id=f.getId();expected+=part(id);}
  else if(b.getFellow()!=null)return false;
  if(sourceKind()==SourceKind.VISITOR_VEHICLE && (!Objects.equals(p.getIsVehicle(),1) || p.getVehiclePlate()==null || p.getVehiclePlate().trim().isEmpty()))return false;
  return id.toString().equals(subject) && id.toString().equals(row) && expected.equals(stable);
 }
 private boolean permit(String action,VisitorAuthSnapshot b,SmtVisitor live,boolean accepting){
  Evidence e=b.getEvidence();ParentFacts p=b.getParent();
  if("ADD".equals(action))return e.getTrigger()==Trigger.APPROVAL && e.getApprovalBasis()==ApprovalBasis.DATABASE_PARENT_STATUS_ZERO
   && Objects.equals(e.getRawStatus(),0) && e.getEventId()==null
   && (accepting?Objects.equals(live.getStatus(),0) && LocalDateTime.parse(p.getEndTime()).isAfter(LocalDateTime.now()):Arrays.asList(0,3,5).contains(live.getStatus()));
  if(!"DELETE".equals(action) || e.getApprovalBasis()!=ApprovalBasis.NOT_APPLICABLE)return false;
  if(e.getTrigger()==Trigger.EXPIRY)return e.getEventId()==null && !LocalDateTime.parse(p.getEndTime()).isAfter(LocalDateTime.now());
  if(Objects.equals(p.getCause(),5) || e.getEventId()==null || e.getEventId()<=0)return false;
  if(e.getTrigger()==Trigger.VEHICLE_EXIT && sourceKind()!=SourceKind.VISITOR_FELLOW)
   return Objects.equals(p.getIsVehicle(),1) && mapper.vehicleExitEvidence(p.getParkId(),p.getId(),e.getEventId(),java.sql.Timestamp.valueOf(from(b)),java.sql.Timestamp.valueOf(LocalDateTime.parse(p.getEndTime())))==1;
  if(e.getTrigger()==Trigger.FELLOW_EXIT && sourceKind()==SourceKind.VISITOR_FELLOW)
   return mapper.fellowExitEvidence(p.getParkId(),b.getFellow().getId(),e.getEventId(),java.sql.Timestamp.valueOf(from(b)),java.sql.Timestamp.valueOf(LocalDateTime.parse(p.getEndTime())))==1;
  return false;
 }
 private boolean selectionWindow(SourceSelection<VisitorAuthSnapshot> s,VisitorAuthSnapshot b){
  if("DELETE".equals(s.getAction()))return s.getWindows().isEmpty() && s.getResources().stream().allMatch(r->r.getInput().getWindows().isEmpty() && "EXCLUDE".equals(r.getInput().getParticipation()));
  LocalDateTime start=from(b),end=LocalDateTime.parse(b.getParent().getEndTime());
  if(s.getWindows().size()!=1 || !start.equals(s.getWindows().get(0).getFrom()) || !end.equals(s.getWindows().get(0).getTo()))return false;
  for(SelectedResource resource:s.getResources())for(Window w:resource.getInput().getWindows())if(w.getFrom().isBefore(start) || w.getTo().isAfter(end))return false;
  return true;
 }
 private static LocalDateTime from(VisitorAuthSnapshot b){return LocalDateTime.parse(b.getParent().getStartTime()).minusHours(b.getEvidence().getLeadHours());}
 private static String part(Long value){String s=value.toString();return s.length()+":"+s;}
 private static void require(boolean condition,String message){if(!condition)throw new IllegalArgumentException(message);}
 private static ParentFacts parentFacts(SmtVisitor row){ParentFacts facts=new ParentFacts();org.springframework.beans.BeanUtils.copyProperties(row,facts);facts.setCreateTime(time(row.getCreateTime()));facts.setStartTime(time(row.getStartTime()));facts.setEndTime(time(row.getEndTime()));return facts;}
 private static String time(Date date){if(date==null)return null;return (date instanceof java.sql.Timestamp?((java.sql.Timestamp)date).toLocalDateTime():LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault())).toString();}
 private static boolean validTimes(ParentFacts p){try{LocalDateTime c=LocalDateTime.parse(p.getCreateTime()),f=LocalDateTime.parse(p.getStartTime()),t=LocalDateTime.parse(p.getEndTime());return c.getNano()%1000==0 && f.getNano()%1000==0 && t.getNano()%1000==0 && !t.isBefore(f);}catch(java.time.DateTimeException e){return false;}}
 private static FellowFacts fellowFacts(SmtFellowVisitor row){FellowFacts facts=new FellowFacts();org.springframework.beans.BeanUtils.copyProperties(row,facts);return facts;}
}
