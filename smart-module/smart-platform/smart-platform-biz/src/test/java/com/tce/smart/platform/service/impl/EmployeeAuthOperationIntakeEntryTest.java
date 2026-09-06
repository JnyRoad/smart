package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityMapper;
import com.tce.smart.platform.service.*;
import org.junit.*;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.lang.reflect.*;

/** 新人员入口必须在灰度关闭时仍核实真实来源归属。 */
public class EmployeeAuthOperationIntakeEntryTest {
 private SmtDeviceAuthorityServiceImpl service;
 private SmtDeviceAuthorityMapper groups;
 private SmtStaffDeviceAuthService rows;
 private SmtDeviceAuthorityRelationService relations;
 private EmployeeAuthOperationAdapter adapter;
 @Before public void setup() throws Exception {
  groups=Mockito.mock(SmtDeviceAuthorityMapper.class);rows=Mockito.mock(SmtStaffDeviceAuthService.class);
  relations=Mockito.mock(SmtDeviceAuthorityRelationService.class);adapter=Mockito.mock(EmployeeAuthOperationAdapter.class);
  Constructor<?> constructor=SmtDeviceAuthorityServiceImpl.class.getConstructors()[0];
  Object[] args=new Object[constructor.getParameterCount()];Class<?>[] types=constructor.getParameterTypes();
  for(int i=0;i<args.length;i++)args[i]=types[i]==SmtDeviceAuthorityMapper.class?groups:types[i]==SmtStaffDeviceAuthService.class?rows:types[i]==SmtDeviceAuthorityRelationService.class?relations:Mockito.mock(types[i]);
  service=(SmtDeviceAuthorityServiceImpl)constructor.newInstance(args);
  ReflectionTestUtils.setField(service,"baseMapper",groups);ReflectionTestUtils.setField(service,"employeeAuthOperationAdapter",adapter);
  SmtDeviceAuthority group=new SmtDeviceAuthority();group.setId(9);group.setParkId(1);group.setType(1);Mockito.when(groups.selectById(9)).thenReturn(group);
  SmtStaffDeviceAuth row=new SmtStaffDeviceAuth();row.setId(5);row.setAuthId(9);row.setStaffId(10L);Mockito.when(rows.listByIds(Mockito.anyCollection())).thenReturn(Collections.singletonList(row));
  Mockito.when(rows.removeByIds(Mockito.anyCollection())).thenReturn(true);
 }
 private DeviceAuthRelationDelReqDTO request(){DeviceAuthRelationDelReqDTO x=new DeviceAuthRelationDelReqDTO();x.setAuthId(9);x.setType(1);x.setDelIds(Collections.singletonList(5));return x;}
 private Object delete(DeviceAuthRelationDelReqDTO request,List<Integer> parks) throws Exception {
  try{return SmtDeviceAuthorityServiceImpl.class.getMethod("personRelationDeleteReceipt",DeviceAuthRelationDelReqDTO.class,List.class).invoke(service,request,parks);}
  catch(NoSuchMethodException e){Assert.fail("人员回执入口尚未实现");return null;}
  catch(InvocationTargetException e){throw (Exception)e.getCause();}
 }
 @Test public void replayEntryDoesNotReadDeletedAuthorityOrRows() {
  EmployeeAuthIntakeService intake=Mockito.mock(EmployeeAuthIntakeService.class);
  ReflectionTestUtils.setField(service,"employeeAuthIntakeService",intake);
  com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt receipt=com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt.builder().requestKey("client-key-00001").operationKey("original").mode("RELIABLE").submitted(true).replayed(true).build();
  Mockito.when(intake.submit(Mockito.any(),Mockito.eq(7),Mockito.eq(Collections.singleton(1)),Mockito.any())).thenReturn(receipt);
  Mockito.clearInvocations(groups,rows,adapter);
  Assert.assertSame("必须先查header，无需重新读取已删除的业务来源",receipt,service.personRelationDeleteIntake(request(),"client-key-00001",7,Collections.singletonList(1)));
  Mockito.verifyZeroInteractions(groups,rows,adapter);
 }
 @Test public void firstClearUsesAssignedKeyAndNoLegacy(){
  EmployeeAuthIntakeService intake=Mockito.mock(EmployeeAuthIntakeService.class);ReflectionTestUtils.setField(service,"employeeAuthIntakeService",intake);
  Mockito.when(intake.submit(Mockito.any(),Mockito.anyInt(),Mockito.anySet(),Mockito.any())).thenAnswer(i->{
   EmployeeAuthIntakeService.FirstAcceptance callback=i.getArgument(3);callback.accept("assigned-key");
   return com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt.builder().mode("NO_CHANGE").build();
  });
  Assert.assertNotNull(service.personRelationClearIntake(9,"client-key-00001",7,Collections.singletonList(1)));
  Mockito.verify(adapter).removeAuthorityOperation(9,"assigned-key");Mockito.verifyZeroInteractions(relations);
 }
 @Test public void capabilityChecksRealScopeEvenWhenDisabled(){
  try{service.personIntakeCapability(9,Collections.singletonList(2));Assert.fail("能力查询必须验证真实园区");}catch(SecurityException expected){}
 }
}
