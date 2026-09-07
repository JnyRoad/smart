package com.tce.smart.platform.service.impl;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import com.tce.smart.platform.core.service.impl.EmployeeAuthOperationService;
import org.junit.*;
import org.mockito.*;
import java.util.*;
public class EmployeeAuthOperationAdapterTest {
 private final AuthOperationProperties config=new AuthOperationProperties();
 private final EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);
 private final EmployeeAuthOperationService operations=Mockito.mock(EmployeeAuthOperationService.class);
 private final EmployeeAuthOperationAdapter adapter=new EmployeeAuthOperationAdapter(config,mapper,operations){@Override protected Set<Integer> allowedParks(){return Collections.singleton(1);}};
 @Before public void setup(){
  config.setEnabled(true);config.setEnabledParks(Collections.singleton(1));
  Mockito.when(mapper.lockAuthorities(Mockito.anyList())).thenAnswer(i->i.getArgument(0));
  Mockito.when(mapper.lockSubjects(Mockito.anyList())).thenReturn(Collections.singletonList(10L));
  SmtStaffDeviceAuth source=new SmtStaffDeviceAuth();source.setId(5);source.setStaffId(10L);source.setAuthId(9);
  Mockito.when(mapper.staffSources(Mockito.anyList())).thenReturn(Collections.singletonList(source));Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.singletonList(source));
  SmtDeviceAuthority authority=new SmtDeviceAuthority();authority.setId(9);authority.setParkId(1);authority.setType(1);Mockito.when(mapper.authorities(Mockito.anyList())).thenReturn(Collections.singletonList(authority));
  SmtAuthSelectionSource membership=new SmtAuthSelectionSource();membership.setSubjectId("10");membership.setParkId(1);Mockito.when(mapper.staffMemberships(Mockito.anyList())).thenReturn(Collections.singletonList(membership));
  SmtStaff staff=new SmtStaff();staff.setId(10L);staff.setStatus(2);staff.setFacePicId("photo-reference");Mockito.when(mapper.staff(Mockito.anyList())).thenReturn(Collections.singletonList(staff));
  List<SmtDeviceAuthorityRelation> relations=new ArrayList<>();for(String id:Arrays.asList("D1","D2")){SmtDeviceAuthorityRelation r=new SmtDeviceAuthorityRelation();r.setAuthorityId(9);r.setDeviceId(id);relations.add(r);}Mockito.when(mapper.authorityDevices(Mockito.anyList())).thenReturn(relations);
  List<SmtDevice> devices=new ArrayList<>();for(String id:Arrays.asList("D1","D2","D3")){SmtDevice d=new SmtDevice();d.setId(id);d.setParkId(1);d.setIsSync(0);devices.add(d);}Mockito.when(mapper.devices(Mockito.anyList())).thenAnswer(i->{List<String> requested=i.getArgument(0);List<SmtDevice> found=new ArrayList<>();for(SmtDevice d:devices)if(requested.contains(d.getId()))found.add(d);return found;});
  Mockito.when(operations.accept(Mockito.anyString(),Mockito.anyList(),Mockito.anySet())).thenReturn(Accepted.builder().operationKey("accepted").batches(Collections.singletonMap(1,Collections.singletonList(100L))).build());
 }
 @Test public void overwriteFreezesOldDeviceBeforeNewMappingAndKeepsProbationStaff() {
  Assert.assertTrue(adapter.authorityDevices(9,Arrays.asList("D2","D3")));
  ArgumentCaptor<List> captor=ArgumentCaptor.forClass(List.class);Mockito.verify(operations).accept(Mockito.anyString(),captor.capture(),Mockito.eq(Collections.singleton(1)));
  Source source=(Source)captor.getValue().get(0);Map<String,String> actions=new HashMap<>();source.getResources().forEach(r->actions.put(r.getResource().getDeviceId(),r.getParticipation()));
  Assert.assertEquals("EXCLUDE",actions.get("D1"));Assert.assertEquals("INCLUDE",actions.get("D2"));Assert.assertEquals("INCLUDE",actions.get("D3"));
  Assert.assertEquals(Integer.valueOf(5),source.getBefore().getId());Assert.assertEquals("photo-reference",source.getImageId());
 }
 @Test public void vehicleAuthorityDoesNotConsumeLegacyEmployeeRoute() {
  SmtDeviceAuthority a=new SmtDeviceAuthority();a.setId(9);a.setType(3);a.setParkId(1);Mockito.when(mapper.authorities(Mockito.anyList())).thenReturn(Collections.singletonList(a));
  Assert.assertNull(adapter.removeAuthority(9));Mockito.verifyZeroInteractions(operations);
 }
 @Test public void disabledRolloutDoesNotQueryOrMutate() {
  config.setEnabled(false);UpdateDeviceAuthDTO input=new UpdateDeviceAuthDTO();Assert.assertNull(adapter.update(2,input));Mockito.verifyZeroInteractions(operations);
 }
 @Test public void addWithoutPhotoOrResourcesKeepsDeviceReasonPrimary() {missingPhotoAndResources(1);}
 @Test public void overwriteWithoutPhotoOrResourcesKeepsDeviceReasonPrimary() {missingPhotoAndResources(2);}
 private void missingPhotoAndResources(int mode) {
  SmtStaff person=new SmtStaff();person.setId(10L);person.setStatus(1);Mockito.when(mapper.staff(Mockito.anyList())).thenReturn(Collections.singletonList(person));
  Mockito.when(mapper.authorityDevices(Mockito.anyList())).thenReturn(Collections.emptyList());
  UpdateDeviceAuthDTO input=new UpdateDeviceAuthDTO();input.setIds(Collections.singletonList("10"));input.setDeviceAuthIds(Collections.singletonList(9));
  adapter.update(mode,input);ArgumentCaptor<List> c=ArgumentCaptor.forClass(List.class);Mockito.verify(operations).accept(Mockito.anyString(),c.capture(),Mockito.anySet());
  Source source=(Source)c.getValue().get(0);Assert.assertTrue(source.getResources().isEmpty());
  Assert.assertTrue(source.getVerificationReason().startsWith("MISSING_DEVICE"));Assert.assertTrue(source.getVerificationReason().contains("MISSING_CREDENTIAL_REFERENCE"));
 }
 @Test public void emptyAuthorityRejectsForeignDevice() {
  Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.emptyList());
  SmtDevice foreign=new SmtDevice();foreign.setId("foreign");foreign.setParkId(2);Mockito.when(mapper.devices(Mockito.anyList())).thenReturn(Collections.singletonList(foreign));
  try{adapter.authorityDevices(9,Collections.singletonList("foreign"));Assert.fail("空组也必须拒绝跨园区设备");}catch(SecurityException expected){}
 }
 @Test public void emptyAuthorityRejectsMissingDevice() {
  Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.emptyList());
  try{adapter.authorityDevices(9,Collections.singletonList("missing"));Assert.fail("空组也必须拒绝不存在设备");}catch(SecurityException expected){}
 }
 @Test public void emptyAuthorityAcceptsOwnedDeviceUnderAuthorityLock() {
  Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.emptyList());
  Assert.assertTrue(adapter.authorityDevices(9,Collections.singletonList("D1")));
  Mockito.verify(mapper,Mockito.atLeastOnce()).lockAuthorities(Collections.singletonList(9));
 }
 @Test public void sharedAuthorityLockPrecedesSubjectLock() {
  adapter.authorityDevices(9,Collections.singletonList("D2"));
  InOrder order=Mockito.inOrder(mapper);order.verify(mapper).lockAuthorities(Collections.singletonList(9));order.verify(mapper).lockSubjects(Collections.singletonList(10L));
 }
 @Test public void unmappedHistoricalTaskIsNotLostWhenHealthyDeviceExists() {
  SmtAuthSelectionSource missing=new SmtAuthSelectionSource();missing.setSubjectId("10");missing.setVerificationReason("MISSING_DEVICE_HISTORY:DIRECT:77:1");
  Mockito.when(mapper.unmappedTaskSubjects(Mockito.anyList())).thenReturn(Collections.singletonList(missing));
  adapter.authorityDevices(9,Collections.singletonList("D2"));ArgumentCaptor<List> c=ArgumentCaptor.forClass(List.class);Mockito.verify(operations).accept(Mockito.anyString(),c.capture(),Mockito.anySet());
  Assert.assertTrue(((Source)c.getValue().get(0)).getVerificationReason().startsWith("MISSING_DEVICE_HISTORY"));
 }
 @Test public void legacyWrapperCannotDeletePendingWhenRolloutIsOff() {
  config.setEnabled(false);Mockito.when(mapper.pendingAnySubjects(Mockito.anyList())).thenReturn(1);
  com.tce.smart.platform.core.mapper.SmtStaffDeviceAuthMapper legacy=Mockito.mock(com.tce.smart.platform.core.mapper.SmtStaffDeviceAuthMapper.class);
  List<SmtStaffDeviceAuth> selected=mapper.staffSources(Collections.singletonList(10L));Mockito.when(legacy.selectList(Mockito.any())).thenReturn(selected);Mockito.when(legacy.delete(Mockito.any())).thenReturn(1);
  SmtStaffDeviceAuthServiceImpl entry=new SmtStaffDeviceAuthServiceImpl(legacy,null,null,null,null,null,null,null,null,null);
  org.springframework.test.util.ReflectionTestUtils.setField(entry,"baseMapper",legacy);org.springframework.test.util.ReflectionTestUtils.setField(entry,"employeeAuthOperationAdapter",new EmployeeAuthOperationAdapter(config,mapper,operations));
  try{entry.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SmtStaffDeviceAuth>().eq("STAFF_ID",10L));Assert.fail("关闭灰度后历史pending仍不能物理删除");}catch(IllegalStateException expected){}
  Mockito.verify(legacy,Mockito.never()).delete(Mockito.any());
 }
 @Test public void sourceExpandingToUnlockedGroupRollsBackWithoutTakingReverseLock() {
  List<SmtStaffDeviceAuth> original=mapper.staffSources(Collections.singletonList(10L));
  SmtStaffDeviceAuth expanded=new SmtStaffDeviceAuth();expanded.setStaffId(10L);expanded.setAuthId(8);
  Mockito.when(mapper.staffSources(Mockito.anyList())).thenReturn(original,Collections.singletonList(expanded));
  SmtStaffDeviceAuth add=new SmtStaffDeviceAuth();add.setStaffId(10L);add.setAuthId(9);
  try{adapter.addSource(add);Assert.fail("锁后出现新组必须回滚重试");}catch(IllegalStateException expected){}
  Mockito.verify(mapper,Mockito.times(1)).lockAuthorities(Collections.singletonList(9));Mockito.verifyZeroInteractions(operations);
 }
 @Test public void secondEntryInSameTransactionCannotAddAuthorityAfterSubjectLock() {
  org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
  try {
   adapter.authorityDevices(9,Collections.singletonList("D2"));
   SmtStaffDeviceAuth changed=new SmtStaffDeviceAuth();changed.setStaffId(10L);changed.setAuthId(8);Mockito.when(mapper.staffSources(Mockito.anyList())).thenReturn(Collections.singletonList(changed));
   SmtStaffDeviceAuth added=new SmtStaffDeviceAuth();added.setStaffId(10L);added.setAuthId(9);
   try{adapter.addSource(added);Assert.fail("同事务第二入口不得倒序加锁");}catch(IllegalStateException expected){}
   Mockito.verify(mapper,Mockito.never()).lockAuthorities(Arrays.asList(8,9));
  } finally {
   for(org.springframework.transaction.support.TransactionSynchronization sync:org.springframework.transaction.support.TransactionSynchronizationManager.getSynchronizations())sync.afterCompletion(1);
   org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
  }
 }
 @Test public void deleteReceiptKeepsActualOperationKey() throws Exception {
  List<SmtStaffDeviceAuth> selected=mapper.sourcesByAuthority(9);
  Mockito.when(mapper.rowsByIds(Collections.singletonList(5))).thenReturn(selected);
  Assert.assertEquals("accepted", operation("removeRowsOperation",new Class[]{List.class,Integer.class},Arrays.asList(5),9));
 }
 @Test public void clearReceiptKeepsActualOperationKey() throws Exception {
  Assert.assertEquals("accepted",operation("removeAuthorityOperation",new Class[]{Integer.class},9));
 }
 @Test public void emptyClearReceiptHasNoInventedOperationKey() throws Exception {
  Mockito.when(mapper.sourcesByAuthority(9)).thenReturn(Collections.emptyList());
  Assert.assertEquals("NO_CHANGE",operation("removeAuthorityOperation",new Class[]{Integer.class},9));
  Mockito.verifyZeroInteractions(operations);
 }
 private Object operation(String name,Class[] types,Object... args) throws Exception {
  try{return EmployeeAuthOperationAdapter.class.getMethod(name,types).invoke(adapter,args);}
  catch(NoSuchMethodException e){Assert.fail("受理入口必须保留真实操作键："+name);return null;}
 }
 @Test public void visitorAuthorityCannotEnterEmployeeGuardedOperations() {
  SmtDeviceAuthority visitor=new SmtDeviceAuthority();visitor.setId(9);visitor.setParkId(1);visitor.setType(2);
  Mockito.when(mapper.authorities(Mockito.anyList())).thenReturn(Collections.singletonList(visitor));
  Assert.assertNull(adapter.removeAuthority(9));Assert.assertNull(adapter.authorityDevices(9,Collections.singletonList("D1")));
  Assert.assertNull(adapter.revokeDevice(9,"D1"));SmtStaffDeviceAuth source=new SmtStaffDeviceAuth();source.setAuthId(9);
  Assert.assertNull(adapter.addSource(source));Mockito.verifyZeroInteractions(operations);
 }
 @Test public void personAuthorityRevokeAndAddReachReliableIntake() {
  Assert.assertTrue(adapter.revokeDevice(9,"D1"));
  SmtStaffDeviceAuth source=new SmtStaffDeviceAuth();source.setAuthId(9);source.setStaffId(10L);
  Assert.assertTrue(adapter.addSource(source));Mockito.verify(operations,Mockito.times(2)).accept(Mockito.anyString(),Mockito.anyList(),Mockito.anySet());
 }
 @Test public void selectedRowsMustBelongToRequestedAuthority() {
  SmtStaffDeviceAuth foreign=new SmtStaffDeviceAuth();foreign.setId(5);foreign.setAuthId(8);foreign.setStaffId(10L);
  Mockito.when(mapper.rowsByIds(Mockito.anyList())).thenReturn(Collections.singletonList(foreign));
  try{adapter.removeRowsOperation(Collections.singletonList(5),9);Assert.fail("必须拒绝外组来源");}catch(SecurityException expected){}
  Mockito.verifyZeroInteractions(operations);
 }
 @Test public void appPerfectHistoricalResourceCannotBecomeExecutableFace() {
  Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(history("2","D3")));
  adapter.removeAuthority(9);Source frozen=acceptedSource();
  Assert.assertTrue("APP完善必须在受理阶段核验",frozen.getVerificationReason()!=null && frozen.getVerificationReason().startsWith("APP_PERFECT_REVIEW"));
  Assert.assertFalse(frozen.getResources().stream().anyMatch(r->"2".equals(r.getResource().getServiceType())));
 }
 @Test public void deletedHistoricalFaceDeviceKeepsSourceInVerification() {
  Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(history("7","deleted-device")));
  adapter.removeAuthority(9);Source frozen=acceptedSource();
  Assert.assertTrue("历史7缺设备也必须核验",frozen.getVerificationReason()!=null && frozen.getVerificationReason().startsWith("MISSING_DEVICE"));
  Assert.assertFalse(frozen.getResources().stream().anyMatch(r->"deleted-device".equals(r.getResource().getDeviceId())));
 }
 @Test public void historicalFaceServiceSevenSurvivesRemovedRelation() {
  Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(history("7","D3")));
  adapter.removeAuthority(9);Source frozen=acceptedSource();
  Assert.assertTrue(frozen.getResources().stream().anyMatch(r->"1".equals(r.getResource().getServiceType()) && "D3".equals(r.getResource().getDeviceId()) && "EXCLUDE".equals(r.getParticipation())));
 }
 @Test public void currentOneAndHistoricalSevenKeepSingleLaneAndRawRows() {
  Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Arrays.asList(history("1","D1"),history("7","D1")));
  HistoryEvidence raw=HistoryEvidence.builder().evidenceVersion(2).origin("DIRECT_DOWN_RECORD").rowId("71").subjectId("10").parkId(1).deviceId("D1").accessType("DIRECT").deviceType("1").serviceType("7").imageId("raw-photo").taskId("55").build();
  Mockito.when(mapper.historicalReviewEvidence(Mockito.anyList(),Mockito.anyList())).thenReturn(Arrays.asList(raw,raw));
  adapter.authorityDevices(9,Collections.singletonList("D1"));Source frozen=acceptedSource();
  Assert.assertEquals(1,frozen.getResources().stream().filter(r->"D1".equals(r.getResource().getDeviceId())).count());Assert.assertTrue(frozen.getResources().stream().filter(r->"D1".equals(r.getResource().getDeviceId())).allMatch(r->"1".equals(r.getResource().getServiceType()) && "INCLUDE".equals(r.getParticipation())));
  Assert.assertEquals(1,frozen.getHistoryEvidence().size());Assert.assertEquals("7",frozen.getHistoryEvidence().get(0).getServiceType());Assert.assertEquals("raw-photo",frozen.getHistoryEvidence().get(0).getImageId());
 }
 @Test public void conflictingSameRawRowCannotOverwriteFrozenEvidence() {
  HistoryEvidence a=HistoryEvidence.builder().origin("DIRECT_DOWN_RECORD").rowId("71").subjectId("10").parkId(1).deviceId("D1").accessType("DIRECT").serviceType("7").imageId("old").build();
  Mockito.when(mapper.historicalReviewEvidence(Mockito.anyList(),Mockito.anyList())).thenReturn(Arrays.asList(a,a.toBuilder().imageId("replacement").build()));
  try{adapter.removeAuthority(9);Assert.fail("同原行内容冲突不得选最后一条");}catch(IllegalStateException expected){Assert.assertTrue(expected.getMessage().contains("HISTORY_EVIDENCE_CONFLICT"));}Mockito.verifyZeroInteractions(operations);
 }
 @Test public void newModelSevenCoordBlocksActivationButDoesNotRewriteCoord() {
  HistoryEvidence coord=HistoryEvidence.builder().origin("RESOURCE_COORD").rowId("R7").subjectId("10").parkId(1).deviceId("D1").accessType("DIRECT").serviceType("7").resourceType("PERSON").resourceId("10").credentialChannel("FACE").build();
  Mockito.when(mapper.historicalReviewEvidence(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(coord));
  try{adapter.removeAuthority(9);Assert.fail("新模型R7不能当原始业务7归一");}catch(IllegalStateException expected){Assert.assertTrue(expected.getMessage().contains("RELEASE_BLOCKED_NONCANONICAL_COORD"));}Mockito.verifyZeroInteractions(operations);
 }
 @Test public void changedDeviceChannelDuringFreezeKeepsSourceForVerification() {
  List<SmtDevice> old=mapper.devices(Arrays.asList("D1","D2"));List<SmtDevice> changed=new ArrayList<>();for(SmtDevice d:old){SmtDevice copy=new SmtDevice();copy.setId(d.getId());copy.setParkId(d.getParkId());copy.setIsSync(d.getIsSync());copy.setChannelNo("D1".equals(d.getId())?2:null);changed.add(copy);}
  Mockito.when(mapper.devices(Mockito.anyList())).thenReturn(old,changed);adapter.removeAuthority(9);
  Assert.assertTrue(acceptedSource().getVerificationReason()!=null && acceptedSource().getVerificationReason().contains("DEVICE_CHANGED_DURING_FREEZE"));
 }
 @Test public void revokeOneSharedSourceKeepsOtherSourceWindowOnSingleCanonicalLane() {
  SmtStaffDeviceAuth a=mapper.staffSources(Collections.singletonList(10L)).get(0);SmtStaffDeviceAuth b=new SmtStaffDeviceAuth();b.setId(6);b.setAuthId(8);b.setStaffId(10L);b.setStartTime(java.sql.Timestamp.valueOf("2026-09-02 00:00:00"));b.setEndTime(java.sql.Timestamp.valueOf("2026-09-10 00:00:00"));
  Mockito.when(mapper.staffSources(Mockito.anyList())).thenReturn(Arrays.asList(a,b));SmtDeviceAuthority aa=new SmtDeviceAuthority();aa.setId(9);aa.setParkId(1);aa.setType(1);SmtDeviceAuthority bb=new SmtDeviceAuthority();bb.setId(8);bb.setParkId(1);bb.setType(1);Mockito.when(mapper.authorities(Mockito.anyList())).thenAnswer(call->{List<Integer> ids=call.getArgument(0);List<SmtDeviceAuthority> found=new ArrayList<>();for(SmtDeviceAuthority auth:Arrays.asList(aa,bb))if(ids.contains(auth.getId()))found.add(auth);return found;});
  List<SmtDeviceAuthorityRelation> relations=new ArrayList<>();for(int auth:Arrays.asList(8,9)){SmtDeviceAuthorityRelation r=new SmtDeviceAuthorityRelation();r.setAuthorityId(auth);r.setDeviceId("D1");relations.add(r);}Mockito.when(mapper.authorityDevices(Mockito.anyList())).thenReturn(relations);Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(history("7","D1")));
  adapter.removeAuthority(9);ArgumentCaptor<List> captured=ArgumentCaptor.forClass(List.class);Mockito.verify(operations).accept(Mockito.anyString(),captured.capture(),Mockito.anySet());Assert.assertEquals(2,captured.getValue().size());
  for(Object value:captured.getValue()){Source source=(Source)value;Assert.assertEquals(1,source.getResources().size());Assert.assertEquals("1",source.getResources().get(0).getResource().getServiceType());if("9".equals(source.getAuthId()))Assert.assertEquals("EXCLUDE",source.getResources().get(0).getParticipation());else{Assert.assertEquals("INCLUDE",source.getResources().get(0).getParticipation());Assert.assertEquals(java.time.LocalDateTime.of(2026,9,2,0,0),source.getResources().get(0).getWindows().get(0).getFrom());}}
 }
 private SmtAuthSelectionResource history(String service,String device) {
  SmtAuthSelectionResource r=new SmtAuthSelectionResource();r.setParkId(1);r.setSubjectId("10");r.setDeviceId(device);r.setAccessType("DIRECT");r.setResourceType("PERSON");r.setResourceId("10");r.setServiceType(service);r.setCredentialChannel("FACE");return r;
 }
 private Source acceptedSource() {
  ArgumentCaptor<List> captured=ArgumentCaptor.forClass(List.class);Mockito.verify(operations).accept(Mockito.anyString(),captured.capture(),Mockito.anySet());return (Source)captured.getValue().get(0);
 }

 @Test public void everyAppAndMissingHistoryRowSurvivesWithBothReasons() {
  HistoryEvidence app=HistoryEvidence.builder().origin("ISC_TASK").rowId("71").subjectId("10").parkId(1).deviceId("D3").accessType("ISC").serviceType("2").deviceType("1").externalTaskId("isc-71").build();
  HistoryEvidence missing=HistoryEvidence.builder().origin("DIRECT_TASK").rowId("72").subjectId("10").deviceId("gone").accessType("DIRECT").serviceType("7").deviceType("1").build();
  HistoryEvidence second=app.toBuilder().rowId("73").externalTaskId("isc-73").build();
  Mockito.when(mapper.historicalReviewEvidence(Mockito.anyList(),Mockito.anyList())).thenReturn(Arrays.asList(app,missing,second));
  adapter.removeAuthority(9);Source frozen=acceptedSource();
  Assert.assertTrue(frozen.getVerificationReason().startsWith("APP_PERFECT_REVIEW"));Assert.assertTrue(frozen.getVerificationReason().contains("MISSING_DEVICE"));
  Assert.assertEquals(3,frozen.getHistoryEvidence().size());
  Assert.assertEquals(new HashSet<>(Arrays.asList("71","72","73")),frozen.getHistoryEvidence().stream().map(HistoryEvidence::getRowId).collect(java.util.stream.Collectors.toSet()));
  Assert.assertTrue(frozen.getHistoryEvidence().stream().anyMatch(e->e.getParkId()==null && "7".equals(e.getServiceType())));
  Assert.assertFalse(frozen.getResources().stream().anyMatch(r->"2".equals(r.getResource().getServiceType()) || "gone".equals(r.getResource().getDeviceId())));
 }
 @Test public void foreignParkHistoryDoesNotContaminateCurrentPark() {
  HistoryEvidence foreign=HistoryEvidence.builder().origin("ISC_DOWN_RECORD").rowId("90").subjectId("10").parkId(2).deviceId("D3").serviceType("2").build();
  Mockito.when(mapper.historicalReviewEvidence(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(foreign));
  adapter.removeAuthority(9);Assert.assertNull(acceptedSource().getVerificationReason());Assert.assertTrue(acceptedSource().getHistoryEvidence().isEmpty());
 }
 @Test public void historicalDeviceMovedToAnotherParkCannotReceiveOldParkCommand() {
  Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(history("7","D3")));
  SmtDevice moved=new SmtDevice();moved.setId("D3");moved.setParkId(2);moved.setIsSync(0);
  Mockito.when(mapper.devices(Mockito.anyList())).thenReturn(Collections.singletonList(moved));
  adapter.removeAuthority(9);Source frozen=acceptedSource();Assert.assertTrue(frozen.getVerificationReason().startsWith("MISSING_DEVICE"));
  Assert.assertFalse(frozen.getResources().stream().anyMatch(r->"D3".equals(r.getResource().getDeviceId())));
 }

 @Test public void retainedOldRelationWithMovedDeviceFreezesHistoricalEvidence() {retainedOldRelationReview(2);}
 @Test public void retainedOldRelationWithUnknownDeviceParkFreezesHistoricalEvidence() {retainedOldRelationReview(null);}
 private void retainedOldRelationReview(Integer currentPark) {
  retainedHistory(currentPark);Assert.assertTrue(adapter.removeAuthority(9));Source source=acceptedSource();
  Assert.assertTrue(source.getVerificationReason().startsWith("MISSING_DEVICE"));Assert.assertEquals(1,source.getHistoryEvidence().size());
  Assert.assertEquals("71",source.getHistoryEvidence().get(0).getRowId());Assert.assertEquals(Integer.valueOf(1),source.getHistoryEvidence().get(0).getParkId());
  Assert.assertFalse(source.getResources().stream().anyMatch(r->"D1".equals(r.getResource().getDeviceId())));
 }
 @Test public void removingMovedOldDeviceDuringLocalReplacementFreezesReview() {
  retainedHistory(2);Assert.assertTrue(adapter.authorityDevices(9,Collections.singletonList("D2")));Source source=acceptedSource();
  Assert.assertTrue(source.getVerificationReason().startsWith("MISSING_DEVICE"));
  Assert.assertFalse(source.getResources().stream().anyMatch(r->"D1".equals(r.getResource().getDeviceId())));
 }
 @Test public void historicalEvidenceDoesNotAuthorizeRequestedForeignReplacement() {
  retainedHistory(2);try{adapter.authorityDevices(9,Collections.singletonList("D1"));Assert.fail("历史不授权显式外园区设备");}catch(SecurityException expected){}
  Mockito.verifyZeroInteractions(operations);
 }
 @Test public void historicalEvidenceDoesNotAuthorizeForeignDeviceInDesiredGrant() {
  retainedHistory(2);UpdateDeviceAuthDTO request=new UpdateDeviceAuthDTO();request.setIds(Collections.singletonList("10"));request.setDeviceAuthIds(Collections.singletonList(9));
  try{adapter.update(1,request);Assert.fail("历史不授权当前新增或保留的外园区设备");}catch(SecurityException expected){}
  Mockito.verifyZeroInteractions(operations);
 }
 private void retainedHistory(Integer currentPark) {
  HistoryEvidence old=HistoryEvidence.builder().origin("DIRECT_DOWN_RECORD").rowId("71").subjectId("10").parkId(1).deviceId("D1").accessType("DIRECT").serviceType("7").deviceType("1").build();
  Mockito.when(mapper.historicalReviewEvidence(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(old));
  Mockito.when(mapper.historicalResources(Mockito.anyList(),Mockito.anyList())).thenReturn(Collections.singletonList(history("7","D1")));
  Mockito.when(mapper.devices(Mockito.anyList())).thenAnswer(i->{List<SmtDevice> out=new ArrayList<>();for(String id:(List<String>)i.getArgument(0)){if(!Arrays.asList("D1","D2","D3").contains(id))continue;SmtDevice device=new SmtDevice();device.setId(id);device.setParkId("D1".equals(id)?currentPark:Integer.valueOf(1));device.setIsSync(0);out.add(device);}return out;});
 }
}
