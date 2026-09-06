package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.entity.SmtAuthSelectionSource;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import org.springframework.stereotype.Component;
/** 员工 v0 五字段 CAS 保留原语义；业务写回和 selection 完成共享外层事务。 */
@Component
public class EmployeeAuthSourceHandler implements AuthSourceHandler<EmployeeAuthSourceHandler.LegacyEmployeeBusiness> {
 public static final class LegacyEmployeeBusiness implements BusinessSnapshot {}
 private final EmployeeAuthOperationMapper mapper;
 public EmployeeAuthSourceHandler(EmployeeAuthOperationMapper mapper){this.mapper=mapper;}
 public SourceKind sourceKind(){return SourceKind.STAFF_AUTH;}
 public SubjectType subjectType(){return SubjectType.STAFF;}
 public int snapshotVersion(){return 0;}
 public Class<LegacyEmployeeBusiness> snapshotType(){return LegacyEmployeeBusiness.class;}
 public void lockAndValidate(SourceSelection<LegacyEmployeeBusiness> source){throw new IllegalArgumentException("员工 v0 通过既有员工受理接口冻结");}
 public boolean applyExact(SmtAuthSelectionSource source,LegacyEmployeeBusiness unused) {
  if(!"STAFF_AUTH".equals(AuthSelectionSnapshots.kind(source)) || !"STAFF".equals(AuthSelectionSnapshots.subject(source)) || AuthSelectionSnapshots.version(source)!=0)return false;
  return ("DELETE".equals(source.getDesiredAction())?mapper.deleteExact(source):source.getOldId()==null?mapper.insertNew(source):mapper.updateExact(source))==1;
 }
}
