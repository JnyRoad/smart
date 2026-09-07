package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.*;
import org.mockito.*;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import java.time.LocalDateTime;
import java.io.*;
import java.util.*;
/** 验证类型边界；同数字 ID 不能让访客命中员工待处理或 CAS。 */
public class AuthSelectionServiceTest {
 @Test public void employeePendingSqlExplicitlyScopesStaff() throws Exception {
  Configuration config=new Configuration();
  try(InputStream in=getClass().getResourceAsStream("/mapper/EmployeeAuthOperationMapper.xml")) {
   new XMLMapperBuilder(in,config,"mapper/EmployeeAuthOperationMapper.xml",config.getSqlFragments()).parse();
  }
  Map<String,Object> params=new HashMap<>();params.put("ids",Collections.singletonList(10L));params.put("park",1);params.put("subject","10");params.put("authId","9");
  for(String id:Arrays.asList("pendingSubject","pendingSource","pendingSubjects","pendingAnySubjects","pendingAuthority")) {
   String sql=config.getMappedStatement(EmployeeAuthOperationMapper.class.getName()+"."+id).getBoundSql(params).getSql();
   Assert.assertTrue(id+" 必须约束选择来源类型",sql.contains("SOURCE_KIND='STAFF_AUTH'"));
   Assert.assertTrue(id+" 必须约束主体类型",sql.contains("SUBJECT_TYPE='STAFF'"));
  }
 }

 @Test public void typedSourceUsesVisitorIdentityAndExactNineteenDigitId() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);
  AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
  VisitorHandler handler=new VisitorHandler();
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Arrays.asList(new EmployeeAuthSourceHandler(mapper),handler)));
  SmtAuthSelectionSource source=visitorSource("9223372036854775807");
  SmtAuthSelectionResource resource=visitorResource(source.getSubjectId());
  Mockito.when(mapper.source(1L,1L)).thenReturn(source);
  Mockito.when(mapper.resources(1L,0,200)).thenReturn(Collections.singletonList(resource));
  Mockito.when(workflow.stage(Mockito.any())).thenReturn(Expanded.builder().source(SourceVersion.builder().sourceId("s").generation(1).build()).binding(Binding.builder().resourceId("r").build()).build());
  Mockito.when(mapper.bindSource(Mockito.anyLong(),Mockito.anyLong(),Mockito.anyString(),Mockito.anyLong())).thenReturn(1);
  Mockito.when(mapper.bindResource(Mockito.anyLong(),Mockito.anyLong(),Mockito.anyString())).thenReturn(1);
  Assert.assertTrue(service.stageNext(1L));
  ArgumentCaptor<Shard> cap=ArgumentCaptor.forClass(Shard.class);Mockito.verify(workflow).stage(cap.capture());
  Assert.assertEquals("VISITOR",cap.getValue().getSource().getSourceKind());
  Assert.assertEquals("VISITOR",cap.getValue().getResources().get(0).getResource().getSubjectType());
  Assert.assertEquals("9223372036854775807",cap.getValue().getSource().getSubjectId());Assert.assertNull(cap.getValue().getStaffAuthId());
 }
 @Test public void unknownKindAndVersionNeverConvergeOrFallBackToStaff() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,Mockito.mock(AuthOperationWorkflowService.class));
  SmtAuthSelectionSource source=visitorSource("10");source.setState("CONVERGED");
  Mockito.when(mapper.exactSource("s",1)).thenReturn(source);
  Assert.assertFalse(service.apply(exact(source)));
  source.setSourceKind("STAFF_AUTH");source.setSubjectType("STAFF");source.setSnapshotVersion(77);
  Assert.assertFalse(service.apply(exact(source)));
  Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any());
 }
 @Test public void dispatchChecksSourceTypeAndPersistsOnlySuccessfulHandler() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);VisitorHandler handler=new VisitorHandler();
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,Mockito.mock(AuthOperationWorkflowService.class),new AuthSourceConvergenceRegistry(Collections.singletonList(handler)));
  SmtAuthSelectionSource source=visitorSource("10");Mockito.when(mapper.exactSource("s",1)).thenReturn(source);Mockito.when(mapper.complete(1L,1L)).thenReturn(1);
  Assert.assertFalse(service.apply(exact(source).toBuilder().subjectType("STAFF").build()));
  Assert.assertTrue(service.apply(exact(source)));Assert.assertEquals(1,handler.calls);
  Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any());
 }
 @Test public void incompleteSelectionCannotBindOrConverge() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);VisitorHandler handler=new VisitorHandler();
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(handler)));
  Mockito.when(mapper.unboundSelectionCount(1L)).thenReturn(1);
  try {service.bindNextLane(1L,null);Assert.fail("未写完投影不得绑定");}catch(IllegalArgumentException expected){}
  SmtAuthSelectionSource source=visitorSource("10");Mockito.when(mapper.exactSource("s",1)).thenReturn(source);
  Assert.assertFalse(service.apply(exact(source)));Assert.assertEquals(0,handler.calls);Mockito.verifyZeroInteractions(workflow);
 }
 @Test public void businessAndCredentialReloadPreserveUnicodeDatesAndIds() {
  SmtAuthSelectionSource source=visitorSource("9223372036854775807");
  VisitorBusiness business=AuthSelectionSnapshots.business(source,VisitorBusiness.class);
  Assert.assertEquals("9223372036854775807",business.getRowId());
  PersonCredential credential=new PersonCredential();credential.setTaskCardNo(business.getRowId());credential.setName("旅途访客");credential.setImageId("file-id");credential.setTaskDeviceType(1);credential.setTaskServiceType(3);
  String json=AuthSelectionSnapshots.credential(credential);
  PersonCredential reloaded=(PersonCredential)AuthSelectionSnapshots.credential(1,json);
  Assert.assertEquals(credential,reloaded);Assert.assertEquals("2026-09-05T12:30:01.123456",AuthSelectionSnapshots.windows(source).get(0).getFrom().toString());
 }

 @Test @SuppressWarnings("unchecked") public void typedAcceptanceFreezesNullAuthIdAndDeduplicatesPhysicalLanes() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(new VisitorHandler())));
  Mockito.when(workflow.acceptWithinTransaction(Mockito.any())).thenReturn(com.tce.smart.platform.core.dto.authoperation.AuthOperationBatchResult.builder().batchId(7L).build());
  Mockito.when(mapper.insertSources(Mockito.anyList())).thenAnswer(c->((List<?>)c.getArgument(0)).size());Mockito.when(mapper.insertResources(Mockito.anyList())).thenAnswer(c->((List<?>)c.getArgument(0)).size());
  VisitorBusiness business=new VisitorBusiness();business.setRowId("9223372036854775807");
  PersonCredential credential=new PersonCredential();credential.setTaskCardNo(business.getRowId());credential.setTaskDeviceType(1);credential.setTaskServiceType(3);
  ResourceInput input=ResourceInput.builder().resource(ResourceKey.builder().parkId(1).subjectType("VISITOR").subjectId(business.getRowId()).deviceId("d").accessType("DIRECT").resourceType("PERSON").resourceId(business.getRowId()).serviceType("3").credentialChannel("FACE").build()).participation("EXCLUDE").build();
  SourceSelection<VisitorBusiness> first=SourceSelection.<VisitorBusiness>builder().parkId(1).sourceKind(SourceKind.VISITOR).subjectType(SubjectType.VISITOR).subjectId(business.getRowId()).stableKey("first").sourceRowId(business.getRowId()).action("DELETE").sourceType("3").snapshotVersion(1).business(business).resource(SelectedResource.builder().input(input).credential(credential).build()).build();
  SourceSelection<VisitorBusiness> second=SourceSelection.<VisitorBusiness>builder().parkId(1).sourceKind(SourceKind.VISITOR).subjectType(SubjectType.VISITOR).subjectId(business.getRowId()).stableKey("second").sourceRowId(business.getRowId()).action("DELETE").sourceType("3").snapshotVersion(1).business(business).resource(SelectedResource.builder().input(input).credential(credential).build()).build();
  service.acceptTyped("typed",Arrays.asList(first,second),Collections.singleton(1));
  ArgumentCaptor<Selection> batch=ArgumentCaptor.forClass(Selection.class);Mockito.verify(workflow).acceptWithinTransaction(batch.capture());Assert.assertEquals(Integer.valueOf(1),batch.getValue().getExpectedCount());Assert.assertEquals(Integer.valueOf(2),batch.getValue().getSourceCount());
  ArgumentCaptor<List> rows=ArgumentCaptor.forClass(List.class);Mockito.verify(mapper).insertSources(rows.capture());SmtAuthSelectionSource row=(SmtAuthSelectionSource)rows.getValue().get(0);
  Assert.assertNull(row.getAuthId());Assert.assertEquals("9223372036854775807",row.getSubjectId());Assert.assertEquals("VISITOR",row.getSubjectType());Assert.assertEquals(Integer.valueOf(1),row.getSnapshotVersion());
  Mockito.verify(mapper,Mockito.never()).lockSubjects(Mockito.anyList());Mockito.verify(mapper,Mockito.never()).pendingSubject(Mockito.anyInt(),Mockito.anyString());
 }

 @Test public void inheritedMapperStatementsAndClobFieldsAreAvailableToOldFixture() throws Exception {
  Configuration config=new Configuration();try(InputStream in=getClass().getResourceAsStream("/mapper/EmployeeAuthOperationMapper.xml")){new XMLMapperBuilder(in,config,"employee",config.getSqlFragments()).parse();}
  String namespace=EmployeeAuthOperationMapper.class.getName()+".";
  Assert.assertTrue(config.hasStatement(namespace+"unboundSelectionCount"));Assert.assertTrue(config.hasStatement(namespace+"pendingTypedSubject"));
  Map<String,Object> params=new HashMap<>();params.put("rows",Collections.singletonList(visitorSource("9223372036854775807")));
  org.apache.ibatis.mapping.BoundSql bound=config.getMappedStatement(namespace+"insertSources").getBoundSql(params);
  Assert.assertTrue(bound.getSql().contains("BUSINESS_SNAPSHOT"));
  Assert.assertTrue(bound.getParameterMappings().stream().anyMatch(p->p.getProperty().endsWith("businessSnapshot") && p.getJdbcType()==org.apache.ibatis.type.JdbcType.CLOB));
 }
 @Test public void credentialsRejectInlineImageAndUnknownPolymorphicJson() {
  PersonCredential credential=new PersonCredential();credential.setTaskCardNo("10");credential.setTaskDeviceType(1);credential.setTaskServiceType(3);credential.setImageId("data:image/png;base64,AAAA");
  try{AuthSelectionSnapshots.credential(credential);Assert.fail("不得冻结内联照片");}catch(IllegalArgumentException expected){}
  try{AuthSelectionSnapshots.credential(1,"{\"kind\":\"PERSON\",\"credential\":{\"@class\":\"java.lang.Runtime\"}}");Assert.fail("不得解释类名");}catch(IllegalArgumentException expected){}
 }
 @Test public void newVehicleCredentialRequiresFrozenCardType() {
  try{AuthSelectionSnapshots.credential(vehicleCredential());Assert.fail("新车辆快照缺少卡类型证据必须拒绝");}catch(IllegalArgumentException expected){}
 }
 @Test public void newVehicleCredentialRejectsCardTypeOutsideExactWhitelist() {
  for(String cardType:Arrays.asList(""," ","00","01","2","7","-1","true"," 0","1 ")) {
   VehicleCredential v=vehicleCredential();v.setCardType(cardType);
   try{AuthSelectionSnapshots.credential(v);Assert.fail("新车辆快照接受了非法卡类型："+cardType);}catch(IllegalArgumentException expected){}
  }
 }
 @Test public void vehicleCardTypesZeroAndOneRoundTripWithoutDefaulting() {
  for(String cardType:Arrays.asList("0","1")) {
   VehicleCredential v=vehicleCredential();v.setCardType(cardType);
   VehicleCredential decoded=(VehicleCredential)AuthSelectionSnapshots.credential(1,AuthSelectionSnapshots.credential(v));
   Assert.assertEquals(cardType,decoded.getCardType());Assert.assertEquals("9223372036854775807",decoded.getTaskCardNo());Assert.assertEquals(v,decoded);
  }
 }
 @Test public void oldV1VehicleMissingCardTypeStaysNullAndCannotBeRefrozenWithoutEvidence() {
  for(String field:Arrays.asList("",",\"cardType\":null")) {
   VehicleCredential decoded=(VehicleCredential)AuthSelectionSnapshots.credential(1,vehicleJson(field));
   Assert.assertNull("旧快照证据缺失不得默认固定车辆1",decoded.getCardType());
   Assert.assertEquals("粤B12345",decoded.getPlate());Assert.assertEquals("9223372036854775807",decoded.getTaskCardNo());
   try{AuthSelectionSnapshots.credential(decoded);Assert.fail("旧快照缺失证据不得被重写为新快照");}catch(IllegalArgumentException expected){}
  }
 }
 @Test public void vehicleSnapshotRejectsInvalidCardTypeValues() {
  for(String cardType:Arrays.asList("","00","01","2","7"," 0","1 ")) {
   try{AuthSelectionSnapshots.credential(1,vehicleJson(",\"cardType\":\""+cardType+"\""));Assert.fail("旧版本号不能放行非法卡类型："+cardType);}catch(IllegalArgumentException expected){}
  }
 }
 @Test public void vehicleSnapshotRejectsNonStringCardTypeInsteadOfJacksonCoercion() {
  for(String jsonValue:Arrays.asList("0","1","true","{}","[]")) {
   try{AuthSelectionSnapshots.credential(1,vehicleJson(",\"cardType\":"+jsonValue));Assert.fail("卡类型必须是明确字符串："+jsonValue);}catch(IllegalArgumentException expected){}
  }
 }
 private String vehicleJson(String cardTypeField) {
  return "{\"kind\":\"VEHICLE\",\"credential\":{\"taskCardNo\":\"9223372036854775807\",\"taskDeviceType\":2,\"taskServiceType\":4,\"plate\":\"粤B12345\""+cardTypeField+"}}";
 }
 private VehicleCredential vehicleCredential() {
  VehicleCredential v=new VehicleCredential();v.setTaskCardNo("9223372036854775807");v.setTaskDeviceType(2);v.setTaskServiceType(4);v.setPlate("粤B12345");return v;
 }

 @Test public void failedSelectionBindingDoesNotAdvancePastUnboundSource() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(new VisitorHandler())));
  Mockito.when(mapper.source(1L,1)).thenReturn(visitorSource("10"));Mockito.when(mapper.resources(1L,0,200)).thenReturn(Collections.singletonList(visitorResource("10")));
  Mockito.when(workflow.stage(Mockito.any())).thenReturn(Expanded.builder().source(SourceVersion.builder().sourceId("s").generation(1).build()).binding(Binding.builder().resourceId("r").build()).build());
  try{service.stageNext(1L);Assert.fail("来源未绑定必须中止当前事务");}catch(IllegalArgumentException expected){}
  Mockito.verify(mapper,Mockito.never()).bindResource(Mockito.anyLong(),Mockito.anyLong(),Mockito.anyString());
  Mockito.when(mapper.bindSource(1L,1,"s",1)).thenReturn(1);Mockito.when(mapper.bindResource(1L,1,"r")).thenReturn(1);
  Assert.assertTrue(service.stageNext(1L));ArgumentCaptor<Shard> shards=ArgumentCaptor.forClass(Shard.class);Mockito.verify(workflow,Mockito.times(2)).stage(shards.capture());
  Assert.assertEquals(shards.getAllValues().get(0),shards.getAllValues().get(1));
 }

 @Test public void typedResourceCannotFallBackToLegacyStaffCredential() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(new VisitorHandler())));
  Mockito.when(mapper.source(1L,1)).thenReturn(visitorSource("10"));SmtAuthSelectionResource resource=visitorResource("10");resource.setCredentialVersion(0);resource.setCredentialSnapshot(null);
  Mockito.when(mapper.resources(1L,0,200)).thenReturn(Collections.singletonList(resource));
  try{service.stageNext(1L);Assert.fail("访客资源不得套用员工v0凭据");}catch(IllegalArgumentException expected){}
  Mockito.verifyZeroInteractions(workflow);
 }
 @Test public void stagedResourceIdentityCannotCrossSourceTypes() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(new VisitorHandler())));
  Mockito.when(mapper.source(1L,1)).thenReturn(visitorSource("10"));SmtAuthSelectionResource resource=visitorResource("10");resource.setSubjectType("STAFF");
  Mockito.when(mapper.resources(1L,0,200)).thenReturn(Collections.singletonList(resource));
  try{service.stageNext(1L);Assert.fail("来源与资源类型不一致必须拒绝");}catch(IllegalArgumentException expected){}
  Mockito.verifyZeroInteractions(workflow);
 }

 @Test public void unsupportedTimestampPrecisionAndMalformedCredentialAreRejected() {
  VisitorBusiness business=new VisitorBusiness();business.setRowId("10");
  try{AuthSelectionSnapshots.business(business,Collections.singletonList(Window.builder().from(LocalDateTime.parse("2026-09-05T01:02:03.123456789")).to(LocalDateTime.parse("2026-09-06T01:02:03.123456789")).build()));Assert.fail("不能静默丢失Oracle微秒之后的精度");}catch(IllegalArgumentException expected){}
  try{AuthSelectionSnapshots.credential(1,"{\"kind\":\"PERSON\",\"credential\":{\"taskDeviceType\":2}}");Assert.fail("重载凭据仍需核验明确的任务类型");}catch(IllegalArgumentException expected){}
 }
 @Test public void genericMapperAliasesResolveAllSharedMethodsWithoutCopyingSql() throws Exception {
  Configuration config=new Configuration();
  for(String name:Arrays.asList("AuthSelectionMapper","EmployeeAuthOperationMapper"))try(InputStream in=getClass().getResourceAsStream("/mapper/"+name+".xml")){new XMLMapperBuilder(in,config,name,config.getSqlFragments()).parse();}
  for(java.lang.reflect.Method method:com.tce.smart.platform.core.mapper.AuthSelectionMapper.class.getMethods())Assert.assertTrue(method.getName(),config.hasStatement(com.tce.smart.platform.core.mapper.AuthSelectionMapper.class.getName()+"."+method.getName()));
 }

 @Test public void typedConvergenceRequiresExactActionEvenWhenFingerprintMatches() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);VisitorHandler handler=new VisitorHandler();
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,Mockito.mock(AuthOperationWorkflowService.class),new AuthSourceConvergenceRegistry(Collections.singletonList(handler)));
  SmtAuthSelectionSource source=visitorSource("10");Mockito.when(mapper.exactSource("s",1)).thenReturn(source);
  Assert.assertFalse(service.apply(exact(source).toBuilder().action(null).build()));Assert.assertEquals(0,handler.calls);
 }

 @Test public void credentialTaskIdentityMustMatchFrozenResource() {
  EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
  EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(new VisitorHandler())));
  Mockito.when(mapper.source(1L,1)).thenReturn(visitorSource("10"));SmtAuthSelectionResource resource=visitorResource("10");
  PersonCredential wrong=new PersonCredential();wrong.setTaskCardNo("11");wrong.setTaskDeviceType(1);wrong.setTaskServiceType(4);resource.setCredentialSnapshot(AuthSelectionSnapshots.credential(wrong));Mockito.when(mapper.resources(1L,0,200)).thenReturn(Collections.singletonList(resource));
  try{service.stageNext(1L);Assert.fail("冻结凭据不能改变目标任务身份");}catch(IllegalArgumentException expected){}
  Mockito.verifyZeroInteractions(workflow);
 }

 @Test public void reviewWithoutResourcesKeepsRealSourceAndZeroExpectedInVerifyingBatch() {
  ReviewFixture f=new ReviewFixture();
  f.service.acceptTyped("review-empty",Collections.singletonList(reviewSource("empty",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build()),Collections.singleton(1));
  Assert.assertEquals(Integer.valueOf(0),f.selection.getExpectedCount());Assert.assertEquals(Integer.valueOf(1),f.selection.getSourceCount());
  Assert.assertEquals(1,f.sources.size());Assert.assertEquals("PENDING",f.sources.get(0).getState());
  Assert.assertEquals("MISSING_RESOURCE_EVIDENCE",f.sources.get(0).getVerificationReason());Assert.assertNull(f.sources.get(0).getSourceCoordId());
  Mockito.verify(f.mapper).markVerification(7L,"MISSING_RESOURCE_EVIDENCE");Mockito.verify(f.mapper,Mockito.never()).insertResources(Mockito.anyList());Mockito.verify(f.handler).lockAndValidate(Mockito.any());
 }
 @Test public void wholeFamilyRetainsMissingCredentialCoordinatesAndAllSources() {
  ReviewFixture f=new ReviewFixture();
  SourceSelection<VisitorBusiness> missing=reviewSource("missing",SourceSelection.VerificationReason.MISSING_CREDENTIAL_EVIDENCE).resource(reviewResource(null)).build();
  SourceSelection<VisitorBusiness> healthy=reviewSource("healthy",null).resource(reviewResource(reviewCredential())).build();
  SourceSelection<VisitorBusiness> empty=reviewSource("empty",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build();
  f.service.acceptTyped("review-family",Arrays.asList(missing,healthy,empty),Collections.singleton(1));
  Assert.assertEquals(Integer.valueOf(3),f.selection.getSourceCount());Assert.assertEquals(Integer.valueOf(1),f.selection.getExpectedCount());Assert.assertEquals(3,f.sources.size());Assert.assertEquals(2,f.resources.size());
  SmtAuthSelectionResource incomplete=f.resources.stream().filter(r->r.getCredentialSnapshot()==null).findFirst().get();
  Assert.assertEquals(Integer.valueOf(1),incomplete.getCredentialVersion());Assert.assertEquals("door",incomplete.getDeviceId());Assert.assertEquals("VISITOR",incomplete.getSubjectType());
  Mockito.verify(f.workflow,Mockito.times(1)).acceptWithinTransaction(Mockito.any());
  Mockito.verify(f.mapper).markVerification(7L,"MISSING_CREDENTIAL_EVIDENCE;MISSING_RESOURCE_EVIDENCE");
 }
 @Test public void reviewReplayRequiresIdenticalReasonAndFrozenEvidence() {
  ReviewFixture f=new ReviewFixture();SourceSelection<VisitorBusiness> s=reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build();
  f.service.acceptTyped("review-replay",Collections.singletonList(s),Collections.singleton(1));Mockito.when(f.mapper.operation("review-replay")).thenReturn(f.sources);
  f.service.acceptTyped("review-replay",Collections.singletonList(s),Collections.singleton(1));
  Mockito.verify(f.workflow,Mockito.times(1)).acceptWithinTransaction(Mockito.any());
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.INCOMPLETE_HISTORY_EVIDENCE).build(),"review-replay");
  s.getBusiness().setEvidence("新证据");assertAdmissionRejected(f,s,"review-replay");
 }
 @Test public void reviewCannotBypassUnknownVersionParentValidationParkOrWindow() {
  ReviewFixture f=new ReviewFixture();
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).snapshotVersion(77).build(),"bad-version");
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).parentRowId(null).build(),"bad-parent");
  try{f.service.acceptTyped("bad-park",Collections.singletonList(reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).parkId(2).build()),Collections.singleton(1));Assert.fail("越权核验必须拒绝");}catch(SecurityException expected){}
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).action("ADD").build(),"bad-window");
  Mockito.doThrow(new IllegalArgumentException("父归属变化")).when(f.handler).lockAndValidate(Mockito.any());
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build(),"bad-owner");
  Mockito.verify(f.workflow,Mockito.never()).acceptWithinTransaction(Mockito.any());
 }
 @Test public void reviewOnlyAllowsAbsentCredentialNotMalformedCredentialOrCoordinate() {
  ReviewFixture f=new ReviewFixture();VehicleCredential vehicle=vehicleCredential();vehicle.setCardType("7");
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_CREDENTIAL_EVIDENCE).resource(reviewResource(vehicle)).build(),"bad-card-type");
  ResourceInput bad=ResourceInput.builder().resource(ResourceKey.builder().parkId(1).subjectType("VISITOR").subjectId("10").accessType("DIRECT").resourceType("PERSON").resourceId("10").serviceType("3").credentialChannel("FACE").build()).participation("EXCLUDE").build();
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_CREDENTIAL_EVIDENCE).resource(SelectedResource.builder().input(bad).build()).build(),"missing-coordinate");
  Mockito.verify(f.workflow,Mockito.never()).acceptWithinTransaction(Mockito.any());
 }
 @Test public void reviewMarkAndSourceWriteMustAffectExactRows() {
  ReviewFixture f=new ReviewFixture();Mockito.when(f.mapper.markVerification(Mockito.anyLong(),Mockito.anyString())).thenReturn(0);
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build(),"mark-failed");
  Mockito.verify(f.mapper).markVerification(7L,"MISSING_RESOURCE_EVIDENCE");
  ReviewFixture partial=new ReviewFixture();Mockito.when(partial.mapper.insertSources(Mockito.anyList())).thenReturn(0);
  assertAdmissionRejected(partial,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build(),"insert-failed");Mockito.verify(partial.mapper,Mockito.never()).markVerification(Mockito.anyLong(),Mockito.anyString());
 }
 @Test public void reviewDoesNotPermitNewDeleteToCrossPendingOrStageAndFinish() {
  ReviewFixture f=new ReviewFixture();Mockito.when(f.mapper.pendingTypedSubject(1,"VISITOR","10")).thenReturn(1);
  assertAdmissionRejected(f,reviewSource("a",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build(),"new-delete");Mockito.verify(f.mapper).pendingTypedSubject(1,"VISITOR","10");Mockito.verify(f.workflow,Mockito.never()).acceptWithinTransaction(Mockito.any());
  Mockito.when(f.mapper.verificationReason(7L)).thenReturn("MISSING_RESOURCE_EVIDENCE");try{f.service.stageNext(7L);Assert.fail("核验批不得展开");}catch(IllegalStateException expected){}
  Mockito.when(f.mapper.unboundSelectionCount(7L)).thenReturn(1);try{f.service.finish(7L);Assert.fail("缺来源绑定不得封口");}catch(IllegalArgumentException expected){}
  Mockito.verify(f.workflow,Mockito.never()).stage(Mockito.any());Mockito.verify(f.workflow,Mockito.never()).finish(Mockito.anyLong());
 }
 @Test public void executableSourcesStillRejectMissingEvidenceAndKeepOriginalFingerprint() {
  ReviewFixture f=new ReviewFixture();assertAdmissionRejected(f,reviewSource("a",null).build(),"empty-normal");
  assertAdmissionRejected(f,reviewSource("a",null).resource(reviewResource(null)).build(),"null-normal");
  f.service.acceptTyped("normal",Collections.singletonList(reviewSource("a",null).resource(reviewResource(reviewCredential())).build()),Collections.singleton(1));
  Assert.assertEquals("f64f243c5de527f153979d38f448e2f1f34bdd196b03cc9f83d4b8a9e63262d2",f.sources.get(0).getFingerprint());Mockito.verify(f.mapper,Mockito.never()).markVerification(Mockito.anyLong(),Mockito.anyString());
 }
 private static void assertAdmissionRejected(ReviewFixture f,SourceSelection<?> source,String key) {
  try{f.service.acceptTyped(key,Collections.singletonList(source),Collections.singleton(1));Assert.fail("必须拒绝："+key);}catch(IllegalArgumentException expected){}
 }
 private static SourceSelection.SourceSelectionBuilder<VisitorBusiness> reviewSource(String key,SourceSelection.VerificationReason reason) {
  VisitorBusiness business=new VisitorBusiness();business.setRowId("10");business.setEvidence("缺失事实");
  return SourceSelection.<VisitorBusiness>builder().parkId(1).sourceKind(SourceKind.VISITOR).subjectType(SubjectType.VISITOR).subjectId("10").stableKey(key).sourceRowId("10").action("DELETE").sourceType("3").parentKind("VISITOR").parentRowId("10").snapshotVersion(1).business(business).verificationReason(reason);
 }
 private static PersonCredential reviewCredential(){PersonCredential p=new PersonCredential();p.setTaskCardNo("10");p.setTaskDeviceType(1);p.setTaskServiceType(3);return p;}
 private static SelectedResource reviewResource(Credential credential) {
  return SelectedResource.builder().credential(credential).input(ResourceInput.builder().resource(ResourceKey.builder().parkId(1).subjectType("VISITOR").subjectId("10").deviceId("door").accessType("DIRECT").resourceType("PERSON").resourceId("10").serviceType("3").credentialChannel("FACE").build()).participation("EXCLUDE").build()).build();
 }
 private static class ReviewFixture {
  final EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);final AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);final VisitorHandler handler=Mockito.spy(new VisitorHandler());
  final AuthSelectionService service=new EmployeeAuthOperationService(mapper,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(handler)));
  Selection selection;List<SmtAuthSelectionSource> sources;List<SmtAuthSelectionResource> resources;
  ReviewFixture(){Mockito.when(workflow.acceptWithinTransaction(Mockito.any())).thenAnswer(c->{selection=c.getArgument(0);return com.tce.smart.platform.core.dto.authoperation.AuthOperationBatchResult.builder().batchId(7L).build();});
   Mockito.when(mapper.insertSources(Mockito.anyList())).thenAnswer(c->{sources=c.getArgument(0);return sources.size();});Mockito.when(mapper.insertResources(Mockito.anyList())).thenAnswer(c->{resources=c.getArgument(0);return resources.size();});Mockito.when(mapper.markVerification(Mockito.anyLong(),Mockito.anyString())).thenReturn(1);}
 }
 private static SourceSnapshot exact(SmtAuthSelectionSource s){return SourceSnapshot.builder().sourceId("s").generation(1).sourceKind(s.getSourceKind()).subjectType(s.getSubjectType()).subjectId(s.getSubjectId()).stableKey(s.getStableKey()).sourceRowId(s.getSourceRowId()).fingerprint(s.getFingerprint()).action(s.getDesiredAction()).build();}
 private static SmtAuthSelectionSource visitorSource(String id) {
  SmtAuthSelectionSource s=new SmtAuthSelectionSource();s.setBatchId(1L);s.setOrdinal(1L);s.setParkId(1);s.setOperationKey("visit");s.setSourceKind("VISITOR");s.setSubjectType("VISITOR");s.setSubjectId(id);s.setSourceRowId(id);s.setStableKey(id.length()+":"+id);s.setFingerprint("fingerprint");s.setDesiredAction("DELETE");s.setState("PENDING");s.setSnapshotVersion(1);
  VisitorBusiness business=new VisitorBusiness();business.setRowId(id);
  s.setBusinessSnapshot(AuthSelectionSnapshots.business(business,Collections.singletonList(Window.builder().from(LocalDateTime.parse("2026-09-05T12:30:01.123456")).to(LocalDateTime.parse("2026-09-06T12:30:01.123456")).build())));return s;
 }
 private static SmtAuthSelectionResource visitorResource(String id) {
  SmtAuthSelectionResource r=new SmtAuthSelectionResource();r.setBatchId(1L);r.setOrdinal(1L);r.setSourceOrdinal(1L);r.setParkId(1);r.setSubjectType("VISITOR");r.setSubjectId(id);r.setDeviceId("door");r.setResourceType("PERSON");r.setResourceId(id);r.setAccessType("DIRECT");r.setServiceType("3");r.setCredentialChannel("FACE");r.setParticipation("EXCLUDE");r.setCredentialVersion(1);
  PersonCredential credential=new PersonCredential();credential.setTaskCardNo(id);credential.setTaskDeviceType(1);credential.setTaskServiceType(3);r.setCredentialSnapshot(AuthSelectionSnapshots.credential(credential));return r;
 }
 public static class VisitorBusiness implements BusinessSnapshot {
  private String evidence;public String getEvidence(){return evidence;}public void setEvidence(String v){evidence=v;}
  private String rowId;public String getRowId(){return rowId;}public void setRowId(String value){rowId=value;}
 }
 private static class VisitorHandler implements AuthSourceHandler<VisitorBusiness> {
  int calls;
  public SourceKind sourceKind(){return SourceKind.VISITOR;}
  public SubjectType subjectType(){return SubjectType.VISITOR;}
  public int snapshotVersion(){return 1;}
  public Class<VisitorBusiness> snapshotType(){return VisitorBusiness.class;}
  public void lockAndValidate(SourceSelection<VisitorBusiness> source){}
  public boolean applyExact(SmtAuthSelectionSource source,VisitorBusiness business){calls++;return source.getSourceRowId().equals(business.getRowId());}
 }
}
