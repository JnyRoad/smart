package com.tce.smart.platform.core.service.impl;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationBatchResult;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import org.junit.*;
import org.mockito.*;
import java.util.*;
public class EmployeeAuthOperationTest {
 @Test public void v2DecoderRejectsWrongOrMissingEncodingForEveryKnownOrigin() {
  for(String origin:Arrays.asList("DIRECT_TASK","ISC_TASK","DIRECT_DOWN_RECORD","ISC_DOWN_RECORD","RESOURCE_COORD")) {
   HistoryEvidence valid=validHistoryEncoding(origin);
   for(String bad:Arrays.asList(null,"BOGUS","EPOCH_SECONDS","LOCAL_TIMESTAMP_FF9"))if(!Objects.equals(bad,valid.getWindowEncoding()))rejectHistoryEncoding(valid.toBuilder().windowEncoding(bad).build());
   for(String bad:Arrays.asList(null,"BOGUS","DEVICE_CURRENT","RAW_RECORD","RESOURCE_COORD"))if(!Objects.equals(bad,valid.getParkOrigin()))rejectHistoryEncoding(valid.toBuilder().parkOrigin(bad).build());
  }
 }
 @Test public void v2DecoderKeepsCollectedSqlNullWindowsAndUnknownParkWithoutInventingValues() {
  for(String origin:Arrays.asList("DIRECT_TASK","ISC_TASK","DIRECT_DOWN_RECORD","ISC_DOWN_RECORD","RESOURCE_COORD")) {
   HistorySnapshot decoded=decodeEvidence(validHistoryEncoding(origin));Assert.assertTrue(decoded.isComplete());
   Assert.assertNull(decoded.getRows().get(0).getParkId());Assert.assertNull(decoded.getRows().get(0).getStartTime());Assert.assertNull(decoded.getRows().get(0).getOverTime());
  }
 }
 @Test public void v2DecoderRejectsCrossUnitAndMalformedNonNullWindows() {
  for(String origin:Arrays.asList("DIRECT_TASK","ISC_TASK"))for(String bad:Arrays.asList("2026-09-01T00:00:00.123456789","1.5","9223372036854775808",""))rejectHistoryEncoding(validHistoryEncoding(origin).toBuilder().startTime(bad).build());
  for(String origin:Arrays.asList("DIRECT_DOWN_RECORD","ISC_DOWN_RECORD"))for(String bad:Arrays.asList("1788220800","2026-02-30T00:00:00.123456789","2026-09-01T00:00:00.123","2026-09-01T00:00:00.123456789Z",""))rejectHistoryEncoding(validHistoryEncoding(origin).toBuilder().overTime(bad).build());
  rejectHistoryEncoding(validHistoryEncoding("RESOURCE_COORD").toBuilder().startTime("1788220800").build());
 }
 @Test public void v2DecoderPreservesCorrectNonNullWindowRepresentationsExactly() {
  for(String origin:Arrays.asList("DIRECT_TASK","ISC_TASK","DIRECT_DOWN_RECORD","ISC_DOWN_RECORD")) {
   String value=origin.endsWith("_TASK")?"1788220800":"2026-09-01T00:00:00.123456789";
   HistorySnapshot decoded=decodeEvidence(validHistoryEncoding(origin).toBuilder().startTime(value).overTime(value).build());Assert.assertTrue(decoded.isComplete());Assert.assertEquals(value,decoded.getRows().get(0).getStartTime());Assert.assertEquals(value,decoded.getRows().get(0).getOverTime());
  }
 }
 private static HistoryEvidence validHistoryEncoding(String origin){boolean task=origin.endsWith("_TASK"),coord="RESOURCE_COORD".equals(origin);return HistoryEvidence.builder().evidenceVersion(2).origin(origin).rowId("9007199254740993123").parkOrigin(coord?"RESOURCE_COORD":task?"DEVICE_CURRENT":"RAW_RECORD").windowEncoding(coord?null:task?"EPOCH_SECONDS":"LOCAL_TIMESTAMP_FF9").build();}
 private static HistorySnapshot decodeEvidence(HistoryEvidence value){return com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.decodeHistory(com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.encodeHistory(Collections.singletonList(value)));}
 private static void rejectHistoryEncoding(HistoryEvidence value){try{decodeEvidence(value);Assert.fail("错误或未知的来源编码不能成为完整V2");}catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("ENCODING"));}}
 @Test public void completeNewEvidenceUsesV2WhileEmployeeVersionsStayZero() throws Exception {
  HistoryEvidence raw=HistoryEvidence.builder().evidenceVersion(2).origin("DIRECT_TASK").rowId("7").serviceType("7").imageId("photo").serialNo("serial").times("4").startTime("1788220800").windowEncoding("EPOCH_SECONDS").build();
  String json=historyJson(source(1,Collections.emptyList()).toBuilder().evidence(raw).build());
  Assert.assertTrue(json.contains("EMPLOYEE_HISTORY_EVIDENCE_V2"));Assert.assertTrue(json.contains("\"evidenceVersion\":2"));
  Assert.assertEquals(0,new EmployeeAuthSourceHandler(mapper).snapshotVersion());
 }
 @Test public void legacyV1SerializationKeepsExactOriginalBytes() throws Exception {
  HistoryEvidence raw=HistoryEvidence.builder().origin("DIRECT_TASK").rowId("7").serviceType("7").build();
  String expected="{\"format\":\"EMPLOYEE_HISTORY_EVIDENCE_V1\",\"rows\":[{\"accessType\":null,\"action\":null,\"createdAt\":null,\"deviceId\":null,\"deviceType\":null,\"externalTaskId\":null,\"origin\":\"DIRECT_TASK\",\"parkId\":null,\"personId\":null,\"reviewCode\":null,\"rowId\":\"7\",\"serviceType\":\"7\",\"status\":null,\"subjectId\":null,\"taskId\":null,\"updatedAt\":null}]}";
  Assert.assertEquals(expected,historyJson(source(1,Collections.emptyList()).toBuilder().evidence(raw).build()));
 }
 @Test public void v2FrozenRecordReplacementFieldsAllChangeSourceFingerprint() {
  HistoryEvidence original=HistoryEvidence.builder().evidenceVersion(2).origin("ISC_DOWN_RECORD").rowId("9007199254740993123").serviceType("7").taskId("9007199254740993222").imageId("old").startTime("2026-09-01T00:00:00.123456789").overTime("2026-09-30T23:59:59.000000000").createdAt("2026-09-01T00:00:00.123456789").build();
  Source base=source(1,Collections.emptyList()).toBuilder().evidence(original).build();
  for(HistoryEvidence changed:Arrays.asList(original.toBuilder().imageId("new").build(),original.toBuilder().taskId("9007199254740993223").build(),original.toBuilder().startTime("2026-09-01T00:00:00.123456790").build(),original.toBuilder().times("1").build(),original.toBuilder().serialNo("different").build()))Assert.assertNotEquals(EmployeeAuthOperationService.fingerprint(base),EmployeeAuthOperationService.fingerprint(base.toBuilder().clearHistoryEvidence().evidence(changed).build()));
 }
 private static String historyJson(Source source) throws Exception {java.lang.reflect.Method m=EmployeeAuthOperationService.class.getDeclaredMethod("historySnapshot",Source.class);m.setAccessible(true);return (String)m.invoke(null,source);}
 @Test public void decoderSeparatesLegacyIncompleteAndV2MissingColumns() {
  com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.HistorySnapshot legacy=com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.decodeHistory("{\"format\":\"EMPLOYEE_HISTORY_EVIDENCE_V1\",\"rows\":[{\"origin\":\"DIRECT_TASK\",\"rowId\":\"7\"}]}");Assert.assertFalse(legacy.isComplete());
  try{com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.decodeHistory("{\"format\":\"EMPLOYEE_HISTORY_EVIDENCE_V2\",\"rows\":[{\"evidenceVersion\":2,\"origin\":\"DIRECT_TASK\",\"rowId\":\"7\"}]}");Assert.fail("漏采字段不可充当SQL NULL");}catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("FIELD_MISSING"));}
 }
 @Test public void decoderRejectsUnknownOriginEvenWithCompleteColumns() {
  HistoryEvidence row=HistoryEvidence.builder().evidenceVersion(2).origin("OTHER_FAMILY").rowId("7").build();String json=com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.encodeHistory(Collections.singletonList(row));
  try{com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.decodeHistory(json);Assert.fail("完整字段不能冒充已支持的来源");}catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("ORIGIN"));}
 }
 @Test public void decoderCannotCallEmptyV2Complete() {
  try{com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.decodeHistory("{\"format\":\"EMPLOYEE_HISTORY_EVIDENCE_V2\",\"rows\":[]}");Assert.fail("无行不能当完整历史证明");}catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("EMPTY"));}
 }
 @Test public void rawFingerprintIgnoresOnlyReviewClassificationAndKeepsRawWindow() {
  HistoryEvidence row=HistoryEvidence.builder().evidenceVersion(2).origin("ISC_DOWN_RECORD").rowId("7").windowEncoding("LOCAL_TIMESTAMP_FF9").startTime("2026-09-01T00:00:00.123456789").build();
  Assert.assertEquals(com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.rawFingerprint(row),com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.rawFingerprint(row.toBuilder().reviewCode("APP_PERFECT_REVIEW").build()));
  Assert.assertNotEquals(com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.rawFingerprint(row),com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.rawFingerprint(row.toBuilder().startTime("2026-09-01T00:00:00.123456790").build()));
 }
 private final EmployeeAuthOperationMapper mapper=Mockito.mock(EmployeeAuthOperationMapper.class);
 private final AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
 private final EmployeeAuthOperationService service=new EmployeeAuthOperationService(mapper,workflow);
 @Test public void emptyTargetsNeverSucceedOrDeleteTheSource() {
  try { service.accept("op-empty",Collections.singletonList(source(1,Collections.emptyList())),Collections.singleton(1));Assert.fail("缺目标必须保持待核验"); }
  catch(IllegalArgumentException expected) { Mockito.verifyZeroInteractions(workflow);Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any()); }
 }
 @Test public void unauthorizedParkFailsBeforePersisting() {
  try { service.accept("op-denied",Collections.singletonList(source(2,Collections.singletonList(resource(2)))),Collections.singleton(1));Assert.fail("越权园区不能受理"); }
  catch(SecurityException expected) { Mockito.verifyZeroInteractions(workflow);Mockito.verify(mapper,Mockito.never()).insertSources(Mockito.anyList()); }
 }
 @Test public void pendingAcceptedSelectionRejectsDifferentIntentBeforeStage() {
  Mockito.when(mapper.lockSubjects(Mockito.anyList())).thenReturn(Collections.singletonList(10L));
  Mockito.when(mapper.pendingSubjects(1,Collections.singletonList(10L))).thenReturn(1);
  try { service.accept("op-other",Collections.singletonList(source(1,Collections.singletonList(resource(1)))),Collections.singleton(1));Assert.fail("已受理来源不能重排"); }
  catch(IllegalStateException expected) { Mockito.verifyZeroInteractions(workflow); }
 }
 @Test public void changedFingerprintCannotDeleteBusinessRow() {
  SmtAuthSelectionSource s=new SmtAuthSelectionSource();s.setFingerprint("frozen");s.setSourceRowId("5");
  Mockito.when(mapper.exactSource("s",1)).thenReturn(s);
  Assert.assertFalse(service.apply(SourceSnapshot.builder().sourceId("s").generation(1).sourceRowId("5").fingerprint("changed").build()));
  Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any());
 }
 @Test public void sourceWindowChangedFailsConditionalConvergence() {
  SmtAuthSelectionSource s=new SmtAuthSelectionSource();s.setFingerprint("frozen");s.setSourceRowId("5");s.setDesiredAction("DELETE");s.setSubjectId("10");s.setStableKey("2:101:9");
  s.setState("PENDING");
  Mockito.when(mapper.exactSource("s",1)).thenReturn(s);
  Mockito.when(mapper.deleteExact(s)).thenReturn(0);
  Assert.assertFalse(service.apply(SourceSnapshot.builder().sourceId("s").generation(1).sourceKind("STAFF_AUTH").subjectId("10").stableKey(s.getStableKey()).sourceRowId("5").fingerprint("frozen").build()));
  Mockito.verify(mapper,Mockito.times(1)).deleteExact(s);
  Mockito.verify(mapper,Mockito.never()).complete(Mockito.any(),Mockito.anyLong());
 }

 @Test public void acceptanceReturnsEveryParkAndFreezesWithoutExpandingTasks() {
  Mockito.when(mapper.lockSubjects(Mockito.anyList())).thenReturn(Collections.singletonList(10L));
  Mockito.when(workflow.acceptWithinTransaction(Mockito.any())).thenAnswer(call->{Selection selected=call.getArgument(0);return AuthOperationBatchResult.builder().batchId(selected.getParkId().longValue()+100).build();});
  Accepted accepted=service.accept("all-parks",Arrays.asList(source(1,Collections.singletonList(resource(1))),source(2,Collections.singletonList(resource(2)))),new HashSet<>(Arrays.asList(1,2)));
  Assert.assertEquals(Collections.singletonList(101L),accepted.getBatches().get(1));Assert.assertEquals(Collections.singletonList(102L),accepted.getBatches().get(2));
  Mockito.verify(mapper,Mockito.times(2)).insertSources(Mockito.anyList());Mockito.verify(mapper,Mockito.times(2)).insertResources(Mockito.anyList());
  Mockito.verify(workflow,Mockito.never()).stage(Mockito.any());Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any());
 }
 @Test public void workflowFailureDoesNotDeleteOrModifyBusinessSource() {
  Mockito.when(mapper.lockSubjects(Mockito.anyList())).thenReturn(Collections.singletonList(10L));
  Mockito.when(workflow.acceptWithinTransaction(Mockito.any())).thenThrow(new IllegalStateException("batch failure"));
  try {service.accept("failed",Collections.singletonList(source(1,Collections.singletonList(resource(1)))),Collections.singleton(1));Assert.fail("失败应传播");}
  catch(IllegalStateException expected){Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any());Mockito.verify(mapper,Mockito.never()).insertSources(Mockito.anyList());}
 }
 @Test public void stagePaginates201ResourcesAndSealsOnlyLastPage() {
  Mockito.when(mapper.bindSource(Mockito.anyLong(),Mockito.anyLong(),Mockito.anyString(),Mockito.anyLong())).thenReturn(1);
  Mockito.when(mapper.bindResource(Mockito.anyLong(),Mockito.anyLong(),Mockito.anyString())).thenReturn(1);
  SmtAuthSelectionSource source=new SmtAuthSelectionSource();source.setBatchId(1L);source.setOrdinal(1L);source.setParkId(1);source.setSubjectId("10");source.setAuthId("9");source.setStableKey("2:101:9");source.setDesiredAction("DELETE");source.setOperationKey("operation");source.setSourceRowId("5");source.setFingerprint("fingerprint");
  Mockito.when(mapper.source(1L,1)).thenReturn(source);
  List<SmtAuthSelectionResource> all=new ArrayList<>();
  for(long i=1;i<=201;i++){SmtAuthSelectionResource r=new SmtAuthSelectionResource();r.setOrdinal(i);r.setSourceOrdinal(1L);r.setParkId(1);r.setSubjectId("10");r.setDeviceId("d"+i);r.setAccessType("DIRECT");r.setResourceType("PERSON");r.setResourceId("10");r.setServiceType("1");r.setCredentialChannel("FACE");r.setParticipation("EXCLUDE");all.add(r);}
  Mockito.when(mapper.resources(Mockito.eq(1L),Mockito.anyLong(),Mockito.anyInt())).thenAnswer(c->{int from=((Long)c.getArgument(1)).intValue(),size=c.getArgument(2);return new ArrayList<>(all.subList(Math.min(from,201),Math.min(from+size,201)));});
  Mockito.when(workflow.stage(Mockito.any())).thenAnswer(c->{Shard shard=c.getArgument(0);Expanded.ExpandedBuilder b=Expanded.builder().source(SourceVersion.builder().sourceId("coord").generation(1).build());for(int i=0;i<shard.getResources().size();i++)b.binding(Binding.builder().resourceId("coord"+i).build());return b.build();});
  Assert.assertTrue(service.stageNext(1L));Mockito.when(mapper.selectionCursor(1L)).thenReturn(200L);Assert.assertTrue(service.stageNext(1L));
  org.mockito.ArgumentCaptor<Shard> captor=org.mockito.ArgumentCaptor.forClass(Shard.class);Mockito.verify(workflow,Mockito.times(2)).stage(captor.capture());
  Assert.assertEquals(200,captor.getAllValues().get(0).getResources().size());Assert.assertFalse(captor.getAllValues().get(0).isFinalSourcePage());
  Assert.assertEquals(1,captor.getAllValues().get(1).getResources().size());Assert.assertTrue(captor.getAllValues().get(1).isFinalSourcePage());
 }
 private Source source(int park,List<ResourceInput> resources) {
  SmtStaffDeviceAuth before=new SmtStaffDeviceAuth();before.setId(5);before.setStaffId(10L);before.setAuthId(9);
  return Source.builder().parkId(park).subjectId("10").authId("9").before(before).resources(resources).build();
 }
 private ResourceInput resource(int park) { return ResourceInput.builder().resource(ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId("10").accessType("DIRECT").deviceId("d").resourceType("PERSON").resourceId("10").serviceType("1").credentialChannel("FACE").build()).participation("EXCLUDE").build(); }
 @Test public void appPerfectWithoutExecutableResourcesStillFreezesVerification() {
  prepareReviewAcceptance();
  service.accept("app-only",Collections.singletonList(source(1,Collections.emptyList()).toBuilder().verificationReason("APP_PERFECT_REVIEW").build()),Collections.singleton(1));
  Mockito.verify(mapper).markVerification(Mockito.anyLong(),Mockito.contains("APP_PERFECT_REVIEW"));Mockito.verify(mapper).insertSources(Mockito.anyList());
  Mockito.verify(mapper,Mockito.never()).deleteExact(Mockito.any());
 }
 @Test public void appPerfectEmployeeIsolatedWhileHealthyEmployeeKeepsPreparing() {
  prepareReviewAcceptance();Source bad=source(1,Collections.singletonList(resource(1))).toBuilder().verificationReason("APP_PERFECT_REVIEW").build();
  SmtStaffDeviceAuth healthyRow=new SmtStaffDeviceAuth();healthyRow.setId(6);healthyRow.setStaffId(11L);healthyRow.setAuthId(9);
  ResourceInput healthyResource=ResourceInput.builder().resource(resource(1).getResource().toBuilder().subjectId("11").resourceId("11").build()).participation("EXCLUDE").build();
  Source good=bad.toBuilder().subjectId("11").before(healthyRow).after(null).verificationReason(null).clearResources().resources(Collections.singletonList(healthyResource)).build();
  service.accept("mixed",Arrays.asList(bad,good),Collections.singleton(1));
  Mockito.verify(workflow,Mockito.times(2)).acceptWithinTransaction(Mockito.any());Mockito.verify(mapper,Mockito.times(1)).markVerification(Mockito.anyLong(),Mockito.contains("APP_PERFECT_REVIEW"));
 }
 @Test public void verificationBatchStopsBeforeAnyStageWork() {
  Mockito.when(mapper.verificationReason(7L)).thenReturn("APP_PERFECT_REVIEW");
  try {service.stageNext(7L);Assert.fail("核验批次不得展开");}catch(IllegalStateException expected){}
  Mockito.verify(workflow,Mockito.never()).stage(Mockito.any());Mockito.verify(mapper,Mockito.never()).resources(Mockito.anyLong(),Mockito.anyLong(),Mockito.anyInt());
 }
 private void prepareReviewAcceptance() {
  Mockito.when(mapper.lockSubjects(Mockito.anyList())).thenAnswer(i->i.getArgument(0));
  Mockito.when(mapper.markVerification(Mockito.anyLong(),Mockito.anyString())).thenReturn(1);
  final long[] id={100};Mockito.when(workflow.acceptWithinTransaction(Mockito.any())).thenAnswer(i->AuthOperationBatchResult.builder().batchId(++id[0]).build());
 }

 @Test public void evidenceOrderIsCanonicalButChangedRawTaskChangesFingerprint() {
  HistoryEvidence a=HistoryEvidence.builder().origin("ISC_TASK").rowId("1").serviceType("2").externalTaskId("raw-task").build();
  HistoryEvidence b=a.toBuilder().rowId("2").serviceType("7").reviewCode("MISSING_DEVICE_COORDINATE").build();
  Source one=source(1,Collections.emptyList()).toBuilder().verificationReason("APP_PERFECT_REVIEW").historyEvidence(Arrays.asList(a,b)).build();
  Source reverse=one.toBuilder().clearHistoryEvidence().historyEvidence(Arrays.asList(b,a)).build();
  Assert.assertEquals(EmployeeAuthOperationService.fingerprint(one),EmployeeAuthOperationService.fingerprint(reverse));
  Source changed=one.toBuilder().clearHistoryEvidence().historyEvidence(Arrays.asList(a.toBuilder().externalTaskId("changed-task").build(),b)).build();
  Assert.assertNotEquals(EmployeeAuthOperationService.fingerprint(one),EmployeeAuthOperationService.fingerprint(changed));
 }
 @Test public void evidenceCLOBKeepsAllRowsAndRawServiceWhileAllSubjectSourcesStayIsolated() {
  prepareReviewAcceptance();HistoryEvidence e=HistoryEvidence.builder().origin("ISC_TASK").rowId("7").serviceType("2").externalTaskId("lookup-task").reviewCode("APP_PERFECT_REVIEW").build();
  Source first=source(1,Collections.emptyList()).toBuilder().verificationReason("APP_PERFECT_REVIEW").evidence(e).evidence(e.toBuilder().rowId("8").build()).build();
  SmtStaffDeviceAuth otherRow=new SmtStaffDeviceAuth();otherRow.setId(6);otherRow.setStaffId(10L);otherRow.setAuthId(8);
  Source other=source(1,Collections.singletonList(resource(1))).toBuilder().authId("8").before(otherRow).build();
  service.accept("all-real-sources",Arrays.asList(first,other),Collections.singleton(1));
  ArgumentCaptor<List> rows=ArgumentCaptor.forClass(List.class);Mockito.verify(mapper).insertSources(rows.capture());Assert.assertEquals(2,rows.getValue().size());
  SmtAuthSelectionSource frozen=(SmtAuthSelectionSource)rows.getValue().stream().filter(x->"9".equals(((SmtAuthSelectionSource)x).getAuthId())).findFirst().get();
  Assert.assertTrue(frozen.getBusinessSnapshot().contains("lookup-task"));Assert.assertTrue(frozen.getBusinessSnapshot().contains("EMPLOYEE_HISTORY_EVIDENCE_V1"));
  try {com.fasterxml.jackson.databind.JsonNode evidence=new com.fasterxml.jackson.databind.ObjectMapper().readTree(frozen.getBusinessSnapshot()).get("rows");Assert.assertEquals(2,evidence.size());Assert.assertEquals("2",evidence.get(0).get("serviceType").asText());}catch(java.io.IOException error){throw new AssertionError(error);}
  Mockito.verify(mapper).markVerification(Mockito.anyLong(),Mockito.contains("APP_PERFECT_REVIEW"));Mockito.verify(workflow,Mockito.never()).stage(Mockito.any());
 }
 @Test public void rawAppPerfectResourceCannotBypassEvidenceAdapter() {
  prepareReviewAcceptance();ResourceInput app=ResourceInput.builder().resource(resource(1).getResource().toBuilder().serviceType("2").build()).participation("EXCLUDE").build();
  try {service.accept("raw-app",Collections.singletonList(source(1,Collections.singletonList(app))),Collections.singleton(1));Assert.fail("APP完善不能作为可执行资源受理");}catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("APP_PERFECT_REVIEW"));}
  Mockito.verifyZeroInteractions(workflow);
 }
 @Test public void mapperBindsRealServiceConstantsAndCompleteHistoricalEvidenceProjection() throws Exception {
  org.apache.ibatis.session.Configuration config=new org.apache.ibatis.session.Configuration();
  try(java.io.InputStream in=getClass().getResourceAsStream("/mapper/EmployeeAuthOperationMapper.xml")) {Assert.assertNotNull(in);new org.apache.ibatis.builder.xml.XMLMapperBuilder(in,config,"history-mapper",config.getSqlFragments()).parse();}
  Map<String,Object> parameters=new HashMap<>();parameters.put("ids",Collections.singletonList(10L));parameters.put("parks",Collections.singletonList(1));
  String namespace="com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper.";
  org.apache.ibatis.mapping.BoundSql resources=config.getMappedStatement(namespace+"historicalResources").getBoundSql(parameters);
  Assert.assertEquals(com.tce.smart.tool.constant.DeviceTaskConstants.CARD_STAFF_IMPORT,resources.getAdditionalParameter("employeeService"));
  Assert.assertEquals(com.tce.smart.tool.constant.DeviceTaskConstants.UPDATE_FACE,resources.getAdditionalParameter("faceUpdateService"));
  org.apache.ibatis.mapping.BoundSql evidence=config.getMappedStatement(namespace+"historicalReviewEvidence").getBoundSql(parameters);
  Assert.assertEquals(com.tce.smart.tool.constant.DeviceTaskConstants.CARD_APP_PERFECT,evidence.getAdditionalParameter("appPerfectService"));
  Assert.assertEquals(4,evidence.getSql().split("UNION ALL",-1).length-1);
  Assert.assertTrue(evidence.getSql().contains("T.SERIAL_NO"));Assert.assertTrue(evidence.getSql().contains("T.TIMES"));Assert.assertTrue(evidence.getSql().contains("T.IMAGE_ID"));Assert.assertTrue(evidence.getSql().contains("TIMESTAMP(9)"));
  Assert.assertFalse(evidence.getSql().contains("SELECT DISTINCT"));Assert.assertFalse(evidence.getSql().contains("MIN("));
  Set<String> fields=new HashSet<>();config.getMappedStatement(namespace+"historicalReviewEvidence").getResultMaps().get(0).getResultMappings().forEach(m->fields.add(m.getProperty()));
  Assert.assertTrue(fields.containsAll(Arrays.asList("origin","rowId","serviceType","parkId","taskId","externalTaskId","personId","status","createdAt")));
 }
}
