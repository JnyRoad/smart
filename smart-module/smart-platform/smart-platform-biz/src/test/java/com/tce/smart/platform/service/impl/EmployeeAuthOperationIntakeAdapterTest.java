package com.tce.smart.platform.service.impl;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.Accepted;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import com.tce.smart.platform.core.service.impl.EmployeeAuthOperationService;
import com.tce.smart.platform.dto.authoperation.AuthOperationIntakeAcceptance;
import org.junit.*;
import org.mockito.*;
import java.util.*;
public class EmployeeAuthOperationIntakeAdapterTest {
 private final AuthOperationProperties config=new AuthOperationProperties();
 private final EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);
 private final EmployeeAuthOperationService operations=Mockito.mock(EmployeeAuthOperationService.class);
 private final EmployeeAuthOperationAdapter adapter=new EmployeeAuthOperationAdapter(config,mapper,operations){@Override protected Set<Integer> allowedParks(){return Collections.singleton(1);}};
 @Before public void setup(){
  config.setEnabled(true);config.setEnabledParks(Collections.singleton(1));
  Mockito.when(mapper.lockAuthorities(Mockito.anyList())).thenAnswer(i->i.getArgument(0));
  Mockito.when(mapper.lockSubjects(Mockito.anyList())).thenReturn(Collections.singletonList(10L));
  SmtDeviceAuthority auth=new SmtDeviceAuthority();auth.setId(9);auth.setParkId(1);auth.setType(1);
  Mockito.when(mapper.authorities(Mockito.anyList())).thenReturn(Collections.singletonList(auth));
  SmtStaffDeviceAuth source=new SmtStaffDeviceAuth();source.setId(5);source.setStaffId(10L);source.setAuthId(9);
  Mockito.when(mapper.staffSources(Mockito.anyList())).thenReturn(Collections.singletonList(source));
  Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.singletonList(source));
  Mockito.when(mapper.rowsByIds(Mockito.anyList())).thenReturn(Collections.singletonList(source));
  SmtAuthSelectionSource membership=new SmtAuthSelectionSource();membership.setSubjectId("10");membership.setParkId(1);
  Mockito.when(mapper.staffMemberships(Mockito.anyList())).thenReturn(Collections.singletonList(membership));
  SmtStaff staff=new SmtStaff();staff.setId(10L);staff.setStatus(2);Mockito.when(mapper.staff(Mockito.anyList())).thenReturn(Collections.singletonList(staff));
  Mockito.when(operations.accept(Mockito.anyString(),Mockito.anyList(),Mockito.anySet())).thenAnswer(i->Accepted.builder().operationKey(i.getArgument(0)).batches(Collections.singletonMap(1,Collections.singletonList(100L))).build());
 }
 @Test public void removeUsesAssignedOperationKeyAndReturnsFullManifest(){
  AuthOperationIntakeAcceptance result=adapter.removeRowsOperation(Collections.singletonList(5),9,"assigned-key");
  Assert.assertNotNull("请求占位操作键必须传入真实选择受理",result);
  Assert.assertEquals("assigned-key",result.getOperationKey());Assert.assertEquals(Collections.singletonMap(100L,1),result.getBatchParks());
  Assert.assertEquals(Collections.singleton(1),result.getScopeParkIds());
  Mockito.verify(operations).accept(Mockito.eq("assigned-key"),Mockito.anyList(),Mockito.anySet());
 }
 @Test public void emptyClearKeepsLockedActualScopeAndZeroChild(){
  Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.emptyList());
  AuthOperationIntakeAcceptance result=adapter.removeAuthorityOperation(9,"assigned-key");
  Assert.assertNotNull("空组也必须产生明确受理结果",result);Assert.assertEquals("NO_CHANGE",result.getOutcome());
  Assert.assertEquals(Collections.singleton(1),result.getScopeParkIds());Assert.assertTrue(result.getBatchParks().isEmpty());
  Mockito.verify(mapper,Mockito.atLeastOnce()).lockAuthorities(Collections.singletonList(9));Mockito.verifyZeroInteractions(operations);
 }
 @Test public void disabledKeyedNeverBecomesLegacy(){
  config.setEnabled(false);
  try{adapter.removeAuthorityOperation(9,"assigned-key");Assert.fail("keyed关闭灰度必须明确拒绝");}
  catch(EmployeeAuthIntakeService.IntakeException expected){Assert.assertEquals("KEYED_UNSUPPORTED",expected.getCode());}
  Mockito.verifyZeroInteractions(operations);
 }
}
