package com.tce.smart.schedule.service.platform.impl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget;
import com.tce.smart.platform.core.dto.authtransport.AuthTransport.Run;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.impl.AuthOperationTransportService;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import org.junit.*;
import org.mockito.*;
import java.util.*;
import static org.mockito.Mockito.*;
/** 真实适配器使用受控外部协议，所有请求次数和payload均来自运行结果。 */
public class AuthOperationTransportFacadeTest {
 private AuthOperationTransportService store;private RemoteDispatcherService remote;private SmtImageService images;private AuthOperationTransportFacade facade;
 @Before public void setup(){store=mock(AuthOperationTransportService.class);remote=mock(RemoteDispatcherService.class);images=mock(SmtImageService.class);
  AuthOperationSchedulerProperties props=new AuthOperationSchedulerProperties();AuthOperationSchedulerProperties.Instance i=new AuthOperationSchedulerProperties.Instance();i.setId("shared-isc");i.setAccessType("ISC");i.setParks(Arrays.asList(1,2));props.setInstances(Collections.singletonList(i));
  when(store.resumeReady(anyInt(),anyString(),anyLong())).thenReturn(true);
  when(store.preparePersonIdentity(anyInt(),anyString(),anyLong(),anyString())).thenReturn(com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity.builder().outcome("OWNER_NEEDS_LOOKUP").build());
  facade=new AuthOperationTransportFacade(store,remote,images,props);org.springframework.test.util.ReflectionTestUtils.setField(facade,"xcPark",1);org.springframework.test.util.ReflectionTestUtils.setField(facade,"xcOrg","org-1");
 }
 @Test public void twoKnownDeletesShareOneBatchConfig(){SmtAuthTransportPhase a=phase(10L,"a"),b=phase(20L,"b");a.setPersonId("pa");b.setPersonId("pb");List<AuthOperationClaimedTarget> claims=claims(a,b);
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"config-1\"}"));
  when(store.begin(eq(1),eq("shared-isc"),anyList(),anyMap())).thenAnswer(inv->{a.setRequestKey("request-1");a.setPersonId("pa");b.setPersonId("pb");return Arrays.asList(a,b);});
  Run result=facade.submit(1,"shared-isc",claims,2);Assert.assertEquals(1,result.getHttpUsed());Assert.assertEquals(2,result.getProcessed());
  ArgumentCaptor<DispatcherDTO> calls=ArgumentCaptor.forClass(DispatcherDTO.class);verify(remote,times(1)).dispatch(calls.capture(),anyString());Assert.assertEquals(EventEnum.ISC_AUTH_CONFIG_DEL.getCode(),calls.getAllValues().get(0).getEventType());
  Map body=(Map)calls.getAllValues().get(0).getData();Map people=(Map)((List)body.get("personDatas")).get(0);Assert.assertEquals(Arrays.asList("pa","pb"),people.get("indexCodes"));verify(store).accepted(1,"shared-isc",Arrays.asList(10L,20L),"config-1");
 }
	@Test public void iscTimestampKeepsSecondsMillisecondsAndOffset() throws Exception {
		java.lang.reflect.Method iso=AuthOperationTransportFacade.class.getDeclaredMethod("iso",Long.class);iso.setAccessible(true);
		Assert.assertEquals("1970-01-01T08:00:00.000+08:00",iso.invoke(null,0L));
		try {iso.invoke(null,new Object[]{null});Assert.fail("缺失窗口不得序列化为1970时间");} catch (java.lang.reflect.InvocationTargetException expected) {Assert.assertTrue(expected.getCause() instanceof IllegalArgumentException);}
	}
 @Test public void ambiguousLookupMustNeverCreateAnotherPerson(){SmtAuthTransportPhase a=phase(10L,"a");a.setAction("ADD");a.setImageId("image");a.setPersonSnapshot("{\"personName\":\"合成\",\"gender\":0}");
  List<AuthOperationClaimedTarget> claims=claims(a);when(images.getImageBase64ByCode("image")).thenReturn("image-base64");
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"list\":[{\"jobNo\":\"a\",\"personId\":\"p1\"},{\"jobNo\":\"a\",\"personId\":\"p2\"}]}"));
  SmtAuthTransportPhase asset=phase(11L,"a");when(store.prepareAsset(anyInt(),anyString(),anyLong(),anyString(),anyString())).thenReturn(asset);when(store.begin(anyInt(),anyString(),anyList(),isNull())).thenReturn(Collections.singletonList(asset));
  facade.submit(1,"shared-isc",claims,3);verify(remote,times(1)).dispatch(any(),anyString());verify(store,never()).prepareAsset(anyInt(),anyString(),anyLong(),anyString(),anyString());
 }
 @Test public void normalNewEmployeeUsesFrozenPersonSnapshotThenBatchesConfig(){SmtAuthTransportPhase a=phase(10L,"a");a.setAction("ADD");a.setImageId("image");a.setPersonSnapshot("{\"personName\":\"冻结姓名\",\"gender\":1}");List<AuthOperationClaimedTarget> claims=claims(a);
  when(images.getImageBase64ByCode("image")).thenReturn("synthetic-image");SmtAuthTransportPhase asset=phase(11L,"a");asset.setPhase("ISC_PERSON");asset.setOrgIndexCode("org-1");
  when(store.prepareAsset(1,"shared-isc",10L,"ISC_PERSON","org-1")).thenReturn(asset);
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),isNull())).thenReturn(Collections.singletonList(asset));
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenAnswer(inv->{a.setPersonId("created-person");a.setRequestKey("config-request");return Collections.singletonList(a);});
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"list\":[]}"),Result.success("{\"personId\":\"created-person\"}"),Result.success("{\"taskId\":\"config-new\"}"));
  Run r=facade.submit(1,"shared-isc",claims,3);Assert.assertEquals(3,r.getHttpUsed());Assert.assertEquals("WAITING_CONFIG",r.getOutcome());
  ArgumentCaptor<DispatcherDTO> calls=ArgumentCaptor.forClass(DispatcherDTO.class);verify(remote,times(3)).dispatch(calls.capture(),anyString());Assert.assertEquals(EventEnum.ISC_PERSON_ADD.getCode(),calls.getAllValues().get(1).getEventType());Map person=(Map)calls.getAllValues().get(1).getData();Assert.assertEquals("冻结姓名",person.get("personName"));Assert.assertEquals("org-1",person.get("orgIndexCode"));Assert.assertFalse(person.containsKey("certificateNo"));
 }
 @Test public void unknownConfigIsRetainedAndSecondSubmissionDoesNotSendAgain(){SmtAuthTransportPhase a=phase(10L,"a");a.setPersonId("pa");List<AuthOperationClaimedTarget> claims=claims(a);
  when(remote.dispatch(any(),anyString())).thenReturn((Result<String>)null);
  when(store.begin(eq(1),eq("shared-isc"),anyList(),anyMap())).thenAnswer(inv->{a.setRequestKey("request");a.setPersonId("pa");return Collections.singletonList(a);});
  doAnswer(inv->{a.setState("UNKNOWN");return null;}).when(store).unknown(anyInt(),anyString(),anyList(),anyString());
  Assert.assertEquals("UNKNOWN",facade.submit(1,"shared-isc",claims,2).getOutcome());facade.submit(1,"shared-isc",claims,2);verify(remote,times(1)).dispatch(any(),anyString());
 }
 @Test public void oneReceiptPageHandlesMixedMembersAndNeverAccumulatesAllPages(){SmtAuthTransportPhase a=phase(10L,"a"),b=phase(20L,"b");a.setPersonId("pa");b.setPersonId("pb");a.setPhase("ISC_DOWNLOAD");a.setExternalId("download-1");a.setPageNo(4);b.setPageNo(4);a.setRequestKey("r");b.setRequestKey("r");
  when(store.scan(1,"shared-isc","ISC_DOWNLOAD","ACCEPTED",null,200)).thenReturn(Arrays.asList(a,b));when(store.group(a)).thenReturn(Arrays.asList(a,b));
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"total\":2,\"list\":[{\"personId\":\"pa\",\"persondownloadResult\":\"0\"},{\"personId\":\"pb\",\"persondownloadResult\":\"1\"}]}"));
  Run result=facade.readReceipt(1,"shared-isc",null,200,1);Assert.assertEquals(1,result.getHttpUsed());Assert.assertEquals(2,result.getProcessed());
  verify(store).receipt(eq(1),eq("shared-isc"),eq(10L),eq("pa"),eq("device"),eq("download-1"),anyString(),eq(true),anyString());verify(store).receipt(eq(1),eq("shared-isc"),eq(20L),eq("pb"),eq("device"),eq("download-1"),anyString(),eq(false),anyString());verify(store).advancePage(1,"shared-isc",Arrays.asList(10L,20L),4,1);
 }
 @Test public void receiptFailureDoesNotAdvanceDurablePage(){SmtAuthTransportPhase p=phase(10L,"a");p.setPersonId("pa");p.setPageNo(3);p.setRequestKey("r");p.setExternalId("d");
  when(store.scan(1,"shared-isc","ISC_DOWNLOAD","ACCEPTED",null,200)).thenReturn(Collections.singletonList(p));when(store.group(p)).thenReturn(Collections.singletonList(p));when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"total\":1,\"list\":[{\"personId\":\"pa\",\"persondownloadResult\":\"0\"}]}"));
  when(store.receipt(anyInt(),anyString(),anyLong(),anyString(),anyString(),anyString(),anyString(),anyBoolean(),anyString())).thenThrow(new IllegalStateException("record failed"));
  try{facade.readReceipt(1,"shared-isc",null,200,1);Assert.fail();}catch(IllegalStateException expected){}verify(store,never()).advancePage(anyInt(),anyString(),anyList(),anyInt(),anyInt());
 }
 @Test public void emptyScanWrapsAndForeignInstanceIsRejected(){Assert.assertNull(facade.readReceipt(1,"shared-isc",10L,200,1).getNextCursor());try{facade.readReceipt(1,"foreign",null,200,1);Assert.fail();}catch(IllegalArgumentException expected){}verifyZeroInteractions(remote);}
 @Test public void knownPersonSurvivesEmptyOrReassignedJobNumber(){
  SmtAuthTransportPhase a=phase(10L,"a");a.setPersonId("person-A");List<AuthOperationClaimedTarget> claims=claims(a);
  when(remote.dispatch(any(),anyString())).thenAnswer(inv->{DispatcherDTO dto=inv.getArgument(0);if(EventEnum.ISC_PERSON_GET.getCode().equals(dto.getEventType()))return Result.success("{\"list\":[{\"jobNo\":\"a\",\"personId\":\"person-B\"}]}");return Result.success("{\"taskId\":\"config-A\"}");});
  when(store.begin(eq(1),eq("shared-isc"),anyList(),anyMap())).thenReturn(Collections.singletonList(a));
  facade.submit(1,"shared-isc",claims,2);
  ArgumentCaptor<Map> people=ArgumentCaptor.forClass(Map.class);verify(store).begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),people.capture());Assert.assertEquals("person-A",people.getValue().get(10L));
 }
 @Test public void jobNumberAloneCannotAuthorizeUnknownSubject(){
  SmtAuthTransportPhase a=phase(10L,"a");List<AuthOperationClaimedTarget> claims=claims(a);
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"list\":[{\"jobNo\":\"a\",\"personId\":\"person-B\"}]}"));
  facade.submit(1,"shared-isc",claims,2);verify(store,never()).begin(anyInt(),anyString(),anyList(),anyMap());verify(store).block(1,"shared-isc",10L,"ISC_IDENTITY_UNPROVEN");
 }
 @Test public void acceptedPersonAssetResumesDespiteEmptyLookupWithoutRecreation(){
  SmtAuthTransportPhase a=phase(10L,"a");a.setAction("ADD");a.setImageId("image");a.setPersonSnapshot("{\"personName\":\"冻结姓名\",\"gender\":1}");List<AuthOperationClaimedTarget> claims=claims(a);
  SmtAuthTransportPhase asset=phase(11L,"a");asset.setPhase("ISC_PERSON");asset.setState("ACCEPTED");asset.setExternalId("created-person");asset.setImageId("image");asset.setOrgIndexCode("org-1");
  when(store.acceptedAsset(1,"shared-isc",10L,"ISC_PERSON")).thenReturn(asset);
  when(images.getImageBase64ByCode("image")).thenReturn("image");when(store.prepareAsset(1,"shared-isc",10L,"ISC_PERSON","org-1")).thenReturn(asset);
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"config\"}"));
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenReturn(Collections.singletonList(a));
  Assert.assertEquals("WAITING_CONFIG",facade.submit(1,"shared-isc",claims,2).getOutcome());verify(store,never()).begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),any());
 }
 @Test public void staleFirstPreparedDoesNotStopHealthySecondAndExactNeverScans(){
  SmtAuthTransportPhase bad=phase(10L,"a"),good=phase(20L,"b");good.setPersonId("pb");
  when(store.exactPhases(1,"shared-isc",Arrays.asList(10L,20L),"ISC_CONFIG","PREPARED")).thenReturn(Arrays.asList(bad,good));
  when(store.resumeReady(1,"shared-isc",10L)).thenReturn(false);when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(20L)),anyMap())).thenReturn(Collections.singletonList(good));
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"config\"}"));
  Assert.assertEquals(1,facade.submitPreparedExact(1,"shared-isc",Arrays.asList(10L,20L),1).getProcessed());
  verify(store,never()).scan(anyInt(),anyString(),anyString(),anyString(),any(),anyInt());verify(store,never()).begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap());
 }
 @Test public void acceptedFaceResumesWithoutReadingImageOrRecreatingAsset(){
  SmtAuthTransportPhase a=phase(10L,"a");a.setAction("ADD");a.setPersonId("pa");a.setImageId("frozen-image");claims(a);
  SmtAuthTransportPhase face=phase(11L,"a");face.setState("ACCEPTED");face.setExternalId("pa");face.setPersonId("pa");
  when(store.acceptedAsset(1,"shared-isc",10L,"ISC_FACE")).thenReturn(face);
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenReturn(Collections.singletonList(a));when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"config\"}"));
  Assert.assertEquals("WAITING_CONFIG",facade.submit(1,"shared-isc",claims(a),1).getOutcome());verifyZeroInteractions(images);verify(store,never()).prepareAsset(anyInt(),anyString(),anyLong(),anyString(),any());
 }
 @Test public void exactConfigProgressNeverSendsPreparedDownload(){
  SmtAuthTransportPhase a=phase(10L,"a");a.setState("ACCEPTED");a.setRequestKey("group");a.setExternalId("config");
  when(store.exactPhases(1,"shared-isc",Collections.singletonList(10L),"ISC_CONFIG","ACCEPTED")).thenReturn(Collections.singletonList(a));when(store.group(a)).thenReturn(Collections.singletonList(a));
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"isFinished\":true,\"isConfigFinished\":true,\"failedNum\":0,\"successedNum\":1}"));
  Assert.assertEquals(1,facade.advanceConfigExact(1,"shared-isc",Collections.singletonList(10L),1).getHttpUsed());verify(store).prepareDownload(1,"shared-isc",Collections.singletonList(10L));verify(store,never()).scan(anyInt(),anyString(),anyString(),anyString(),any(),anyInt());verify(store,never()).begin(anyInt(),anyString(),anyList(),anyMap());
 }
 @Test public void blockedClaimDefersAndNeverBecomesProjectionVerification(){
  SmtAuthTransportPhase a=phase(10L,"a");List<AuthOperationClaimedTarget> rows=claims(a);when(store.reuseBeforePrepare(1,rows.get(0))).thenReturn("BLOCKED");
  facade.submit(1,"shared-isc",rows,3);verify(store).deferClaim(1,rows.get(0));verify(store,never()).verifyClaim(anyInt(),any(),anyString());verifyZeroInteractions(remote);
 }
 @Test public void personAcceptedAtBudgetBoundaryContinuesNextRunWithoutLookupVisibility(){
  SmtAuthTransportPhase a=phase(10L,"a");a.setAction("ADD");a.setImageId("image");a.setPersonSnapshot("{\"personName\":\"合成\",\"gender\":0}");List<AuthOperationClaimedTarget> rows=claims(a);
  SmtAuthTransportPhase asset=phase(11L,"a");asset.setPhase("ISC_PERSON");asset.setOrgIndexCode("org-1");
  when(store.acceptedAsset(1,"shared-isc",10L,"ISC_PERSON")).thenAnswer(inv->"ACCEPTED".equals(asset.getState())?asset:null);
  when(store.prepareAsset(1,"shared-isc",10L,"ISC_PERSON","org-1")).thenReturn(asset);when(images.getImageBase64ByCode("image")).thenReturn("image-data");
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),isNull())).thenReturn(Collections.singletonList(asset));when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenReturn(Collections.singletonList(a));
  doAnswer(inv->{asset.setState("ACCEPTED");asset.setExternalId("created");return null;}).when(store).accepted(1,"shared-isc",Collections.singletonList(11L),"created");
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"list\":[]}"),Result.success("{\"personId\":\"created\"}"),Result.success("{\"taskId\":\"config\"}"));
  Assert.assertEquals(2,facade.submit(1,"shared-isc",rows,2).getHttpUsed());Assert.assertEquals("WAITING_CONFIG",facade.submit(1,"shared-isc",rows,1).getOutcome());
  verify(store,times(1)).begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),isNull());verify(remote,times(3)).dispatch(any(),anyString());verify(store,never()).block(anyInt(),anyString(),anyLong(),anyString());
 }
 @Test public void faceAcceptedAtBudgetBoundaryContinuesWithoutRepeatedPhotoWrite(){
  SmtAuthTransportPhase a=phase(10L,"a");a.setAction("ADD");a.setPersonId("pa");a.setImageId("image");List<AuthOperationClaimedTarget> rows=claims(a);
  SmtAuthTransportPhase face=phase(11L,"a");face.setPhase("ISC_FACE");face.setPersonId("pa");
  when(store.acceptedAsset(1,"shared-isc",10L,"ISC_FACE")).thenAnswer(inv->"ACCEPTED".equals(face.getState())?face:null);
  when(store.prepareAsset(1,"shared-isc",10L,"ISC_FACE",null)).thenReturn(face);when(images.getImageBase64ByCode("image")).thenReturn("image-data");
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),anyMap())).thenReturn(Collections.singletonList(face));when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenReturn(Collections.singletonList(a));
  doAnswer(inv->{face.setState("ACCEPTED");face.setExternalId("pa");return null;}).when(store).accepted(1,"shared-isc",Collections.singletonList(11L),"pa");
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{}"),Result.success("{\"taskId\":\"config\"}"));
  Assert.assertEquals(1,facade.submit(1,"shared-isc",rows,1).getHttpUsed());Assert.assertEquals("WAITING_CONFIG",facade.submit(1,"shared-isc",rows,1).getOutcome());verify(store,times(1)).begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),anyMap());verify(remote,times(2)).dispatch(any(),anyString());
 }
 @Test public void oneFaceIdentityConflictDoesNotStopTwoHealthyMembersFromBatching(){
  SmtAuthTransportPhase bad=phase(10L,"a"),b=phase(20L,"b"),c=phase(30L,"c");for(SmtAuthTransportPhase p:Arrays.asList(bad,b,c)){p.setAction("ADD");p.setPersonId("person-"+p.getBadge());p.setImageId("image");}
  SmtAuthTransportPhase face=phase(11L,"a");face.setPhase("ISC_FACE");when(images.getImageBase64ByCode("image")).thenReturn("image");when(store.prepareAsset(1,"shared-isc",10L,"ISC_FACE",null)).thenReturn(face);
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),anyMap())).thenThrow(new AuthOperationTransportService.PhaseRejected(11L,"ISC_IDENTITY_CONFLICT"));
  for(SmtAuthTransportPhase p:Arrays.asList(b,c)){SmtAuthTransportPhase proof=phase(p.getId()+1,p.getBadge());proof.setState("ACCEPTED");proof.setPersonId(p.getPersonId());proof.setExternalId(p.getPersonId());when(store.acceptedAsset(1,"shared-isc",p.getId(),"ISC_FACE")).thenReturn(proof);}
  when(store.begin(eq(1),eq("shared-isc"),eq(Arrays.asList(20L,30L)),anyMap())).thenReturn(Arrays.asList(b,c));when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"healthy-config\"}"));
  Assert.assertEquals(2,facade.submit(1,"shared-isc",claims(bad,b,c),3).getProcessed());verify(store).rejectAsset(1,"shared-isc",11L,10L,"ISC_IDENTITY_CONFLICT");verify(remote,times(1)).dispatch(any(),anyString());verify(store,never()).rejectPrepared(eq(1),eq("shared-isc"),eq(20L),anyString());
 }
 @Test public void oneDeleteIdentityConflictRejectsOnlyThatMemberAndKeepsBatch(){
  SmtAuthTransportPhase bad=phase(10L,"a"),b=phase(20L,"b"),c=phase(30L,"c");for(SmtAuthTransportPhase p:Arrays.asList(bad,b,c))p.setPersonId("person-"+p.getBadge());
  when(store.begin(eq(1),eq("shared-isc"),eq(Arrays.asList(10L,20L,30L)),anyMap())).thenThrow(new AuthOperationTransportService.PhaseRejected(10L,"ISC_IDENTITY_CONFLICT"));
  when(store.begin(eq(1),eq("shared-isc"),eq(Arrays.asList(20L,30L)),anyMap())).thenReturn(Arrays.asList(b,c));when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"healthy-config\"}"));
  Assert.assertEquals(2,facade.submit(1,"shared-isc",claims(bad,b,c),1).getProcessed());verify(store).rejectPrepared(1,"shared-isc",10L,"ISC_IDENTITY_CONFLICT");verify(store,never()).rejectPrepared(eq(1),eq("shared-isc"),eq(20L),anyString());verify(store,never()).rejectPrepared(eq(1),eq("shared-isc"),eq(30L),anyString());
  ArgumentCaptor<DispatcherDTO> call=ArgumentCaptor.forClass(DispatcherDTO.class);verify(remote,times(1)).dispatch(call.capture(),anyString());Map data=(Map)call.getValue().getData();Assert.assertEquals(Arrays.asList("person-b","person-c"),((Map)((List)data.get("personDatas")).get(0)).get("indexCodes"));
 }
 @Test public void losingFaceBeginLeavesParentPreparedUntilWinnerIsAccepted(){assertFaceRaceDoesNotPoisonParent(true);}
 @Test public void observingFaceIntentLeavesParentPreparedUntilWinnerIsAccepted(){assertFaceRaceDoesNotPoisonParent(false);}
 private void assertFaceRaceDoesNotPoisonParent(boolean stalePrepared){
  SmtAuthTransportPhase config=phase(10L,"a");config.setAction("ADD");config.setPersonId("person-a");config.setImageId("image");List<AuthOperationClaimedTarget> claims=claims(config);
  SmtAuthTransportPhase asset=phase(11L,"a");asset.setPhase("ISC_FACE");asset.setPersonId("person-a");asset.setState(stalePrepared?"PREPARED":"INTENT");
  when(images.getImageBase64ByCode("image")).thenReturn("image");when(store.prepareAsset(1,"shared-isc",10L,"ISC_FACE",null)).thenReturn(asset);
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),anyMap())).thenAnswer(inv->{asset.setState("INTENT");throw new AuthOperationTransportService.PhaseRejected(11L,"PHASE_ALREADY_CLAIMED");});
  doAnswer(inv->{SmtAuthTransportPhase p=Long.valueOf(10).equals(inv.getArgument(2))?config:asset;if("PREPARED".equals(p.getState()))p.setState("VERIFYING");return null;}).when(store).rejectPrepared(anyInt(),anyString(),anyLong(),anyString());
  doAnswer(inv->{config.setState("VERIFYING");return null;}).when(store).block(eq(1),eq("shared-isc"),eq(10L),anyString());
  Assert.assertEquals(0,facade.submit(1,"shared-isc",claims,2).getHttpUsed());Assert.assertEquals("PREPARED",config.getState());Assert.assertEquals("INTENT",asset.getState());
  // 胜者接受人脸后，原配置仍能正常首次发送；输家没有建立第二个资产请求。
  asset.setState("ACCEPTED");asset.setExternalId("person-a");when(store.acceptedAsset(1,"shared-isc",10L,"ISC_FACE")).thenReturn(asset);
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenAnswer(inv->{Assert.assertEquals("PREPARED",config.getState());config.setState("INTENT");config.setRequestKey("winner-config");return Collections.singletonList(config);});
  when(remote.dispatch(any(),anyString())).thenReturn(Result.success("{\"taskId\":\"config-after-face\"}"));
  Run resumed=facade.submit(1,"shared-isc",claims,1);Assert.assertEquals("WAITING_CONFIG",resumed.getOutcome());Assert.assertEquals(1,resumed.getHttpUsed());verify(remote,times(1)).dispatch(any(),anyString());verify(store,never()).rejectPrepared(anyInt(),anyString(),anyLong(),anyString());verify(store,never()).block(eq(1),eq("shared-isc"),eq(10L),anyString());verify(store,never()).rejectAsset(anyInt(),anyString(),anyLong(),anyLong(),anyString());
 }
 @Test public void twoActualFacadeWorkersKeepWinningAssetAndParentUsable() throws Exception {
  SmtAuthTransportPhase config=phase(10L,"a");config.setAction("ADD");config.setImageId("image");config.setPersonId("person-a");List<AuthOperationClaimedTarget> claims=claims(config);
  java.util.concurrent.atomic.AtomicReference<String> assetState=new java.util.concurrent.atomic.AtomicReference<>("PREPARED");
  java.util.concurrent.atomic.AtomicInteger faceHttp=new java.util.concurrent.atomic.AtomicInteger(),configHttp=new java.util.concurrent.atomic.AtomicInteger();
  java.util.concurrent.CountDownLatch bothPrepared=new java.util.concurrent.CountDownLatch(2),faceEntered=new java.util.concurrent.CountDownLatch(1),releaseFace=new java.util.concurrent.CountDownLatch(1);
  when(images.getImageBase64ByCode("image")).thenReturn("image");
  when(store.prepareAsset(1,"shared-isc",10L,"ISC_FACE",null)).thenAnswer(inv->{SmtAuthTransportPhase f=phase(11L,"a");f.setPhase("ISC_FACE");bothPrepared.countDown();Assert.assertTrue(bothPrepared.await(3,java.util.concurrent.TimeUnit.SECONDS));return f;});
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(11L)),anyMap())).thenAnswer(inv->{if(!assetState.compareAndSet("PREPARED","INTENT"))throw new AuthOperationTransportService.PhaseRejected(11L,"PHASE_ALREADY_CLAIMED");SmtAuthTransportPhase f=phase(11L,"a");f.setState("INTENT");f.setRequestKey("one-face-request");return Collections.singletonList(f);});
  doAnswer(inv->{if(Long.valueOf(10).equals(inv.getArgument(2))&&"PREPARED".equals(config.getState()))config.setState("VERIFYING");return null;}).when(store).rejectPrepared(anyInt(),anyString(),anyLong(),anyString());
  doAnswer(inv->{List<Long> accepted=inv.getArgument(2);if(accepted.contains(11L))assetState.set("ACCEPTED");else config.setState("ACCEPTED");return null;}).when(store).accepted(anyInt(),anyString(),anyList(),anyString());
  when(store.begin(eq(1),eq("shared-isc"),eq(Collections.singletonList(10L)),anyMap())).thenAnswer(inv->{Assert.assertEquals("PREPARED",config.getState());config.setState("INTENT");config.setRequestKey("one-config-request");return Collections.singletonList(config);});
  when(remote.dispatch(any(),anyString())).thenAnswer(inv->{DispatcherDTO dto=inv.getArgument(0);if(EventEnum.ISC_FACE_ADD.getCode().equals(dto.getEventType())){faceHttp.incrementAndGet();faceEntered.countDown();Assert.assertTrue(releaseFace.await(3,java.util.concurrent.TimeUnit.SECONDS));return Result.success("{}");}configHttp.incrementAndGet();return Result.success("{\"taskId\":\"config-once\"}");});
  java.util.concurrent.ExecutorService workers=java.util.concurrent.Executors.newFixedThreadPool(2);java.util.concurrent.CompletionService<Run> completed=new java.util.concurrent.ExecutorCompletionService<>(workers);
  try{completed.submit(()->facade.submit(1,"shared-isc",claims,2));completed.submit(()->facade.submit(1,"shared-isc",claims,2));Assert.assertTrue(faceEntered.await(3,java.util.concurrent.TimeUnit.SECONDS));
   java.util.concurrent.Future<Run> loser=completed.poll(3,java.util.concurrent.TimeUnit.SECONDS);Assert.assertNotNull(loser);Assert.assertEquals(0,loser.get().getHttpUsed());Assert.assertEquals("PREPARED",config.getState());Assert.assertEquals("INTENT",assetState.get());Assert.assertEquals(1,faceHttp.get());
   releaseFace.countDown();java.util.concurrent.Future<Run> winner=completed.poll(3,java.util.concurrent.TimeUnit.SECONDS);Assert.assertNotNull(winner);Assert.assertEquals("WAITING_CONFIG",winner.get().getOutcome());Assert.assertEquals("ACCEPTED",assetState.get());Assert.assertEquals("ACCEPTED",config.getState());Assert.assertEquals(1,faceHttp.get());Assert.assertEquals(1,configHttp.get());verify(store,never()).rejectAsset(anyInt(),anyString(),anyLong(),anyLong(),anyString());
  }finally{releaseFace.countDown();workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(3,java.util.concurrent.TimeUnit.SECONDS));}
 }
 @Test public void assetRejectionPropagatesOnlyAfterItsPreparedCasWins(){
  com.tce.smart.platform.core.mapper.AuthOperationTransportMapper mapper=mock(com.tce.smart.platform.core.mapper.AuthOperationTransportMapper.class);
  AuthOperationTransportService real=new AuthOperationTransportService(mapper,null,null,null,null,null,null,null,null,null,null,null,null);
  SmtAuthTransportPhase config=phase(10L,"a"),asset=phase(11L,"a");asset.setPhase("ISC_FACE");asset.setTargetId(config.getTargetId());asset.setAttemptId(config.getAttemptId());
  when(mapper.byId(10L)).thenReturn(config);when(mapper.byId(11L)).thenReturn(asset);
  asset.setState("INTENT");try{real.begin(1,"shared-isc",Collections.singletonList(11L),Collections.singletonMap(11L,"person-a"));Assert.fail();}catch(AuthOperationTransportService.PhaseRejected expected){Assert.assertTrue(expected.isContended());}asset.setState("PREPARED");
  // 即使读取到了旧PREPARED，CAS输家也不能写父配置或所属目标、尝试。
  when(mapper.transition(11L,"PREPARED","VERIFYING",null,"ISC_IDENTITY_CONFLICT")).thenReturn(0);
  Assert.assertFalse(real.rejectAsset(1,"shared-isc",11L,10L,"ISC_IDENTITY_CONFLICT"));verify(mapper,never()).transition(eq(10L),anyString(),anyString(),any(),anyString());verify(mapper,never()).hold(any(),anyString());verify(mapper,never()).holdAttempt(any(),anyString());
  when(mapper.transition(11L,"PREPARED","VERIFYING",null,"ISC_IDENTITY_CONFLICT")).thenReturn(1);when(mapper.transition(10L,"PREPARED","VERIFYING",null,"ISC_IDENTITY_CONFLICT")).thenReturn(1);
  Assert.assertTrue(real.rejectAsset(1,"shared-isc",11L,10L,"ISC_IDENTITY_CONFLICT"));verify(mapper).hold(config,"ISC_IDENTITY_CONFLICT");verify(mapper).holdAttempt(config,"ISC_IDENTITY_CONFLICT");
 }
 private List<AuthOperationClaimedTarget> claims(SmtAuthTransportPhase... phases){List<AuthOperationClaimedTarget> result=new ArrayList<>();for(SmtAuthTransportPhase p:phases){AuthOperationClaimedTarget c=AuthOperationClaimedTarget.builder().targetId(p.getTargetId()).accessType("ISC").build();result.add(c);when(store.prepare(1,"shared-isc",c)).thenReturn(p);}return result;}
 private SmtAuthTransportPhase phase(Long id,String badge){SmtAuthTransportPhase p=new SmtAuthTransportPhase();p.setId(id);p.setTargetId(id+100);p.setAttemptId(id+200);p.setParkId(1);p.setInstanceId("shared-isc");p.setAccessType("ISC");p.setPhase("ISC_CONFIG");p.setState("PREPARED");p.setBadge(badge);p.setAction("DELETE");p.setDeviceId("device");p.setStartTime(1L);p.setOverTime(2L);p.setChannelNo(1);p.setPageNo(1);return p;}
}
