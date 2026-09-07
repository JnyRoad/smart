package com.tce.smart.platform.core.mapper;
import com.tce.smart.platform.core.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.*;
/** 员工选择快照、主体互斥及来源条件写回，全部在本库完成。 */
public interface EmployeeAuthOperationMapper extends AuthSelectionMapper {
 List<Long> lockSubjects(@Param("ids") List<Long> ids);
 List<Integer> lockAuthorities(@Param("ids") List<Integer> ids);
 int pendingAnySubjects(@Param("ids") List<Long> ids);
 List<SmtAuthSelectionSource> unmappedTaskSubjects(@Param("ids") List<Long> ids);
 int pendingSubjects(@Param("park") int park,@Param("ids") List<Long> ids);
 int pendingSubject(@Param("park") int park,@Param("subject") String subject);
 int pendingAuthority(@Param("park") int park,@Param("authId") String authId);
 int pendingSource(@Param("park") int park,@Param("subject") String subject,@Param("authId") String authId);
 List<SmtDevice> devices(@Param("ids") List<String> ids);
 int deleteExact(@Param("s") SmtAuthSelectionSource source);
 int updateExact(@Param("s") SmtAuthSelectionSource source);
 int insertNew(@Param("s") SmtAuthSelectionSource source);
 List<SmtStaffDeviceAuth> rowsByIds(@Param("ids") List<Integer> ids);
 List<SmtStaffDeviceAuth> sourcesByAuthority(@Param("id") Integer id);
 List<SmtAuthSelectionSource> staffMemberships(@Param("ids") List<Long> ids);
 List<SmtStaff> staffByBadges(@Param("ids") List<String> ids);
 List<SmtStaffDeviceAuth> staffSources(@Param("ids") List<Long> ids);
 List<SmtDeviceAuthority> authorities(@Param("ids") List<Integer> ids);
 List<SmtDeviceAuthorityRelation> authorityDevices(@Param("ids") List<Integer> ids);
 List<SmtStaff> staff(@Param("ids") List<Long> ids);
 List<Integer> staffParks(@Param("subject") String subject);
 List<com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.HistoryEvidence> historicalReviewEvidence(@Param("ids") List<Long> ids,@Param("parks") List<Integer> parks);
 List<SmtAuthSelectionResource> historicalResources(@Param("ids") List<Long> ids,@Param("parks") List<Integer> parks);
}
