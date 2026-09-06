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
public class EmployeeAuthOperationReceiptTest {
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
 @Test public void disabledRolloutStillRejectsForeignGroup() throws Exception {reject(request(),Collections.singletonList(2),SecurityException.class);}
 @Test public void disabledRolloutStillRejectsMissingUserScope() throws Exception {reject(request(),Collections.emptyList(),SecurityException.class);}
 @Test public void disabledRolloutStillRejectsForeignRow() throws Exception {
  SmtStaffDeviceAuth row=new SmtStaffDeviceAuth();row.setId(5);row.setAuthId(8);Mockito.when(rows.listByIds(Mockito.anyCollection())).thenReturn(Collections.singletonList(row));
  reject(request(),Collections.singletonList(1),SecurityException.class);
 }
 @Test public void vehicleGroupCannotEnterPersonReceipt() throws Exception {
  groups.selectById(9).setType(3);reject(request(),Collections.singletonList(1),IllegalArgumentException.class);
 }
 @Test public void emptyRowsCannotBecomeSuccessfulReceipt() throws Exception {
  DeviceAuthRelationDelReqDTO x=request();x.setDelIds(Collections.emptyList());reject(x,Collections.singletonList(1),IllegalArgumentException.class);
 }
 @Test public void disabledRolloutReturnsExplicitLegacyReceipt() throws Exception {
  Object receipt=delete(request(),Collections.singletonList(1));
  Assert.assertEquals("LEGACY",receipt.getClass().getMethod("getMode").invoke(receipt));
  Assert.assertEquals(true,receipt.getClass().getMethod("isSubmitted").invoke(receipt));
  Assert.assertNull(receipt.getClass().getMethod("getOperationKey").invoke(receipt));
  Mockito.verify(rows).removeByIds(Collections.singletonList(5));
 }
 private void reject(DeviceAuthRelationDelReqDTO request,List<Integer> parks,Class<?> error) throws Exception {
  try{delete(request,parks);Assert.fail("必须拒绝不合法人员删除");}catch(Exception e){Assert.assertTrue(e.toString(),error.isInstance(e));}
  Mockito.verify(rows,Mockito.never()).removeByIds(Mockito.anyCollection());Mockito.verifyZeroInteractions(relations);
 }
 @Test public void reliableReceiptCannotFallThroughToLegacy() throws Exception {
  Mockito.when(adapter.removeRowsOperation(Collections.singletonList(5),9)).thenReturn("exact-operation-key");
  Object receipt=delete(request(),Collections.singletonList(1));
  Assert.assertEquals("RELIABLE",receipt.getClass().getMethod("getMode").invoke(receipt));
  Assert.assertEquals("exact-operation-key",receipt.getClass().getMethod("getOperationKey").invoke(receipt));
  Mockito.verify(adapter,Mockito.times(1)).removeRowsOperation(Collections.singletonList(5),9);
  Mockito.verify(rows,Mockito.never()).removeByIds(Mockito.anyCollection());Mockito.verifyZeroInteractions(relations);
 }
 @Test public void legacyFalseMustRemainUnsubmitted() throws Exception {
  Mockito.when(rows.removeByIds(Mockito.anyCollection())).thenReturn(false);
  Object receipt=delete(request(),Collections.singletonList(1));
  Assert.assertEquals(false,receipt.getClass().getMethod("isSubmitted").invoke(receipt));
 }
 @Test public void missingSelectionRowRejectsWholeOperation() throws Exception {
  Mockito.when(rows.listByIds(Mockito.anyCollection())).thenReturn(Collections.emptyList());
  reject(request(),Collections.singletonList(1),IllegalArgumentException.class);
 }
 @Test public void clearReceiptDistinguishesNoChangeFromAccepted() {
  Mockito.when(adapter.removeAuthorityOperation(9)).thenReturn("NO_CHANGE");
  com.tce.smart.platform.dto.authoperation.AuthOperationReceipt none=service.personRelationClearReceipt(9,Collections.singletonList(1));
  Assert.assertEquals("NO_CHANGE",none.getMode());Assert.assertFalse(none.isSubmitted());Assert.assertNull(none.getOperationKey());
  Mockito.when(adapter.removeAuthorityOperation(9)).thenReturn("clear-operation");
  Assert.assertEquals("clear-operation",service.personRelationClearReceipt(9,Collections.singletonList(1)).getOperationKey());
  Mockito.verifyZeroInteractions(relations);Mockito.verify(rows,Mockito.never()).removeByAuthId(Mockito.anyInt());
 }
}
