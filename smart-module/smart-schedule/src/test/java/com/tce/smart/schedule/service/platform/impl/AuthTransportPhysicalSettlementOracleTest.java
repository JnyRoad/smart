package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import org.junit.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.*;
import static org.mockito.Mockito.*;

/** 真实Oracle物理结算；远端仅返回受控明细，所有事件、record和source均走生产事务。 */
public class AuthTransportPhysicalSettlementOracleTest extends AuthTransportPhysicalSettlementFixture {
    private final List<SmtAuthTransportPhase> downloads=new ArrayList<>();
    private final List<Integer> pages=new ArrayList<>();
    private final Map<String,Integer> http=new LinkedHashMap<>();
    private String mode="FULL";
    private AuthOperationTransportFacade facade;
    private String instance(){return "capacity-"+park;}
    @Test public void oneDeviceFinishesBeforeItsSourceOtherDeviceAndIsNotPolledAgain() {
        prepare(1,2,false);SmtAuthTransportPhase a=downloads.get(0),b=downloads.get(1);
        read(a);Assert.assertEquals("FINISHED",state(a));Assert.assertEquals("CONFIRMED",targetState(a));Assert.assertEquals(1,sourceCount());
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID=? AND CONVERGED='Y'",Integer.class,a.getAttemptId()));
        Assert.assertFalse(transport.scan(park,instance(),"ISC_DOWNLOAD","ACCEPTED",null,200).stream().anyMatch(p->p.getId().equals(a.getId())));
        read(b);Assert.assertEquals(0,sourceCount());service.refreshTarget(a.getTargetId());Assert.assertEquals("CONVERGED",targetState(a));Assert.assertEquals("CONVERGED",targetState(b));
        int before=http.values().stream().mapToInt(Integer::intValue).sum();facade.readReceipt(park,instance(),null,200,1);
        Assert.assertEquals(before,http.values().stream().mapToInt(Integer::intValue).sum());Assert.assertEquals(Integer.valueOf(1),http.get(a.getExternalId()));
    }
    @Test public void partialSharedRequestFinishesOnlyPresentMember() {
        prepare(2,1,false);SmtAuthTransportPhase a=downloads.get(0),b=downloads.get(1);mode="FIRST";read(a);
        Assert.assertEquals("FINISHED",state(a));Assert.assertEquals("ACCEPTED",state(b));Assert.assertEquals(1,sourceCount());
        mode="FULL";read(b);Assert.assertEquals("FINISHED",state(b));Assert.assertEquals(0,sourceCount());Assert.assertEquals(2,eventCount());
    }
    @Test public void page200AndRestartKeepUnfinishedMembersCursorIndependent() {
        prepare(2,1,false);SmtAuthTransportPhase a=downloads.get(0),b=downloads.get(1);mode="PAGE200";read(a);
        Assert.assertEquals(0,eventCount());Assert.assertEquals(Integer.valueOf(2),phases.byId(a.getId()).getPageNo());
        facade=newFacade();mode="FIRST";read(a);Assert.assertEquals(Arrays.asList(1,2),pages);Assert.assertEquals("FINISHED",state(a));
        Assert.assertEquals(Integer.valueOf(2),phases.byId(a.getId()).getPageNo());Assert.assertEquals(Integer.valueOf(1),phases.byId(b.getId()).getPageNo());
        facade=newFacade();mode="FULL";read(b);Assert.assertEquals(Arrays.asList(1,2,1),pages);Assert.assertEquals("FINISHED",state(b));Assert.assertEquals(2,eventCount());
    }
    @Test public void sharedTargetSettlesBothPhysicalContributionsWhileOneSourceStillWaits() {
        prepare(1,2,true);SmtAuthTransportPhase shared=downloads.stream().filter(p->jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_RESOURCE WHERE TARGET_ID=? AND BINDING_REVISION=0",Integer.class,p.getTargetId())==2).findFirst().get();
        Received r=ack(shared,"shared");Assert.assertTrue(r.isPhysicalSettled());Assert.assertFalse(r.isSourceConverged());Assert.assertEquals("FINISHED",state(shared));Assert.assertEquals(1,sourceCount());
        Assert.assertEquals("CONFIRMED",targetState(shared));
        for(SmtAuthTransportPhase p:downloads)if(!p.getId().equals(shared.getId()))ack(p,"remaining");
        Assert.assertEquals(0,sourceCount());service.refreshTarget(shared.getTargetId());Assert.assertEquals("CONVERGED",targetState(shared));
    }
    @Test public void recordFailureRollsBackPhysicalEvidenceThenReplaySettles() {
        prepare(1,2,false);SmtAuthTransportPhase p=downloads.get(0);failRecord=true;
        try{ack(p,"record-rollback");Assert.fail("record失败必须回滚");}catch(RuntimeException expected){Assert.assertTrue(causes(expected,"合成record失败"));}
        Assert.assertEquals(0,eventCount());Assert.assertEquals("ACCEPTED",state(p));Assert.assertEquals(1,sourceCount());
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE DEVICE_CODE=? AND CARD_NO=?",Integer.class,p.getDeviceId(),p.getCardNo()));
        failRecord=false;Assert.assertTrue(ack(p,"record-rollback").isPhysicalSettled());Assert.assertEquals("FINISHED",state(p));Assert.assertEquals(1,eventCount());
    }
    @Test public void staleAckSettlesOnlyAfterDurableObservationAndReplayPreservesCompensation() {
        prepare(1,1,false);SmtAuthTransportPhase p=downloads.get(0);ResourceKey key=versions.currentDesired(p.getResourceId()).getResource();
        long batch=service.accept(Selection.builder().parkId(park).idempotencyKey("physical-next").action("ADD").sourceType("1").snapshot("synthetic-next").expectedCount(1).sourceCount(1).build()).getBatchId();
        Window window=Window.builder().from(LocalDateTime.of(2026,10,1,0,0)).to(LocalDateTime.of(2026,11,1,0,0)).build();
        SourceIntent source=SourceIntent.builder().parkId(park).batchId(batch).sourceKind("STAFF_AUTH").stableKey(AuthWorkflow.staffStableKey(String.valueOf(employeeId),String.valueOf(park+1)))
            .subjectType("STAFF").subjectId(String.valueOf(employeeId)).sourceRowId("physical-next-row").sourceFingerprint("physical-next-fp").intentKey("physical-next-intent").action("ADD").payloadSnapshot("synthetic-next").window(window).build();
        service.stage(Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(source).staffAuthId(String.valueOf(park+1)).finalSourcePage(true).resource(ResourceInput.builder().resource(key).participation("INCLUDE").window(window).build()).build());
        service.bindLane(batch,p.getResourceId(),1,2);service.finish(batch);
        Received r=ack(p,"stale");Assert.assertTrue(r.isPhysicalSettled());Assert.assertFalse(r.isSourceConverged());Assert.assertEquals("STALE_COMPENSATE",r.getEvidence().getOutcome());Assert.assertEquals("FINISHED",state(p));
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_RESOURCE WHERE RESOURCE_COORD_ID=? AND ATTEMPT_ID=? AND STATE='STALE_OBSERVED' AND BINDING_REVISION<0",Integer.class,p.getResourceId(),p.getAttemptId()));
        long generation=versions.currentDesired(p.getResourceId()).getGeneration();Assert.assertTrue(generation>p.getResourceGeneration());Assert.assertEquals(1,sourceCount());
        Received again=ack(p,"stale");Assert.assertTrue(again.isPhysicalSettled());Assert.assertTrue(again.getReceipt().isDuplicate());Assert.assertEquals("STALE_REPLAY",again.getEvidence().getOutcome());Assert.assertEquals(generation,versions.currentDesired(p.getResourceId()).getGeneration());
        SmtAuthSourceResource original=selectionForTarget(p);Recovery recovery=service.recoverPending(original.getSourceCoordId(),original.getSourceGeneration(),p.getResourceId());
        Assert.assertNotNull("旧副作用后继补偿必须仍可恢复",recovery.getBinding());Assert.assertEquals(1,eventCount());Assert.assertEquals(1,sourceCount());
    }
    @Test public void failureAfterFinishedSqlRollsBackWholeReceiptBeforeNextProcessReplays() {
        prepare(1,1,false);SmtAuthTransportPhase p=downloads.get(0);failFinish=true;
        try{ack(p,"finish-rollback");Assert.fail("phase结束提交前注入应失败");}catch(RuntimeException expected){Assert.assertTrue(causes(expected,"合成phase结束提交前失败"));}
        Assert.assertEquals(0,eventCount());Assert.assertEquals("ACCEPTED",state(p));Assert.assertEquals(1,sourceCount());
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE DEVICE_CODE=? AND CARD_NO=?",Integer.class,p.getDeviceId(),p.getCardNo()));
        failFinish=false;facade=newFacade();read(p);Assert.assertEquals("FINISHED",state(p));Assert.assertEquals(0,sourceCount());Assert.assertEquals(1,eventCount());
    }
    @Test public void concurrentSamePhaseTrustedAckFinishesIdempotentlyAfterBothReadAccepted() throws Exception {
        prepare(1,2,false);SmtAuthTransportPhase p=downloads.get(0);gatedPhase=p.getId();phaseReaders=new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.ExecutorService workers=java.util.concurrent.Executors.newFixedThreadPool(2);
        try{
            java.util.concurrent.Future<Received> first=workers.submit(()->ack(p,"concurrent-same-event"));
            java.util.concurrent.Future<Received> second=workers.submit(()->ack(p,"concurrent-same-event"));
            Assert.assertTrue(first.get(10,java.util.concurrent.TimeUnit.SECONDS).isPhysicalSettled());Assert.assertTrue(second.get(10,java.util.concurrent.TimeUnit.SECONDS).isPhysicalSettled());
            Assert.assertEquals(0,phaseReaders.getCount());Assert.assertEquals("FINISHED",state(p));Assert.assertEquals(1,eventCount());Assert.assertEquals(1,sourceCount());Assert.assertEquals("CONFIRMED",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,p.getTargetId()));
        }finally{gatedPhase=null;workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(3,java.util.concurrent.TimeUnit.SECONDS));}
    }
    private void prepare(int people,int devices,boolean shared) {
        jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_STATE(INSTANCE_ID) VALUES(?)",instance());jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES(?,'ISC',?)",park,instance());
        jdbc.update("UPDATE SMT_DEVICE SET IS_SYNC=1,CHANNEL_NO=1 WHERE PARK_ID=?",park);
        for(int d=1;d<devices;d++){String device="physical-device-"+park+"-"+d;jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC,CHANNEL_NO) VALUES(?,?,1,1)",device,park);jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park+1000+d,park,device,park);}
        List<Integer> sourceIds=new ArrayList<>();sourceIds.add(park);
        for(int person=1;person<people;person++){long id=employeeId+person;int row=park+100+person;jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) VALUES(?,?,1,?,?,?)",id,"employee-test-"+park,"image-ref-"+id,"badge-"+id,"合成人员");jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",row,id,park);sourceIds.add(row);}
        List<String> deviceIds=jdbc.queryForList("SELECT ID FROM SMT_DEVICE WHERE PARK_ID=? ORDER BY ID",String.class,park);
        if(shared){jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",park+1,park);jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",park+1,employeeId,park+1);sourceIds.add(park+1);int n=0;for(String device:deviceIds)jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park+2000+n++,park+1,device,park);jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE AUTHORITY_ID=? AND DEVICE_ID=?",park,deviceIds.get(1));}
        for(int person=0;person<people;person++)for(String device:deviceIds)jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,SERVICE_TYPE,DEVICE_CODE,CARD_NO,PERSON_ID,BADGE,IMAGE_ID,TASK_TYPE,CREATE_TIME,START_TIME,OVER_TIME) VALUES(?,?,1,1,1,?,?,?,?,?,1,TIMESTAMP '2026-09-01 09:00:00',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 23:59:59')",com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(),park,device,String.valueOf(employeeId+person),"person-"+(employeeId+person),"badge-"+(employeeId+person),"image-ref-"+(employeeId+person));
        long batch;
        if(shared){
            // 直接验证Employee.accept的合法服务器冻结选择[A]/[A,B]；不冒充legacy Adapter会生成这一范围。
            List<com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.Source> selected=new ArrayList<>();
            for(Integer row:sourceIds){SmtStaffDeviceAuth auth=generatedStaff.selectById(row);List<ResourceInput> inputs=new ArrayList<>();
                for(String device:deviceIds){if(row.equals(park)&&!device.equals(deviceIds.get(0)))continue;inputs.add(ResourceInput.builder().resource(ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(String.valueOf(employeeId)).deviceId(device).accessType("ISC").resourceType("PERSON").resourceId(String.valueOf(employeeId)).serviceType("1").credentialChannel("FACE").build()).participation("EXCLUDE").build());}
                selected.add(com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.Source.builder().parkId(park).subjectId(String.valueOf(employeeId)).authId(String.valueOf(auth.getAuthId())).before(auth).badge("badge-"+park).imageId("image-ref-"+park).resources(inputs).build());
            }
            batch=employee.accept("physical-shared-"+park,selected,Collections.singleton(park)).getBatches().get(park).get(0);
        }else{entry.removeAuthToDevice(sourceIds,Collections.emptyList());batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);}
        while(employee.stageNext(batch)){}while(employee.bindNextLane(batch,null)!=null){}employee.finish(batch);
        List<Long> targets=jdbc.queryForList("SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=? ORDER BY ID",Long.class,batch);
        List<AuthOperationClaimedTarget> claims=service.claim(AuthOperationClaimCommand.builder().parkId(park).accessType("ISC").operationQueue("AUTH").targetIds(targets).maxCount(targets.size()).leaseSeconds(300L).build());Assert.assertEquals(people*devices,claims.size());
        Map<String,List<SmtAuthTransportPhase>> groups=new LinkedHashMap<>();for(AuthOperationClaimedTarget claim:claims){SmtAuthTransportPhase p=transport.prepare(park,instance(),claim);groups.computeIfAbsent(p.getDeviceId(),key->new ArrayList<>()).add(p);}
        for(List<SmtAuthTransportPhase> group:groups.values()){
            Map<Long,String> persons=new HashMap<>();List<Long> ids=new ArrayList<>();for(SmtAuthTransportPhase p:group){persons.put(p.getId(),"person-"+p.getCardNo());ids.add(p.getId());}
            String external="physical-"+group.get(0).getId();transport.begin(park,instance(),ids,persons);transport.accepted(park,instance(),ids,"config-"+external);
            List<SmtAuthTransportPhase> down=transport.prepareDownload(park,instance(),ids);persons.clear();ids.clear();for(SmtAuthTransportPhase p:down){persons.put(p.getId(),p.getPersonId());ids.add(p.getId());}
            transport.begin(park,instance(),ids,persons);transport.accepted(park,instance(),ids,external);for(SmtAuthTransportPhase p:down)downloads.add(phases.byId(p.getId()));
        }
        facade=newFacade();
    }
    private AuthOperationTransportFacade newFacade() {
        RemoteDispatcherService remote=mock(RemoteDispatcherService.class);when(remote.dispatch(any(),anyString())).thenAnswer(call->{
            Assert.assertFalse(TransactionSynchronizationManager.isActualTransactionActive());DispatcherDTO<?> request=call.getArgument(0);Assert.assertEquals(EventEnum.ISC_TASK_RECORD_DETAIL_GET.getCode(),request.getEventType());JSONObject data=JSONUtil.parseObj(request.getData());int page=data.getInt("pageNo");Assert.assertEquals(Integer.valueOf(200),data.getInt("pageSize"));pages.add(page);String external=data.getStr("taskId");http.merge(external,1,Integer::sum);JSONArray rows=new JSONArray();List<SmtAuthTransportPhase> group=new ArrayList<>();for(SmtAuthTransportPhase p:downloads)if(p.getExternalId().equals(external))group.add(p);
            if(mode.equals("PAGE200")){for(int i=0;i<200;i++)rows.add(new JSONObject().put("personId","unrelated-"+i).put("persondownloadResult","0"));}
            else for(int n=0;n<group.size();n++){if(mode.equals("FIRST")&&n>0)break;rows.add(new JSONObject().put("personId",group.get(n).getPersonId()).put("persondownloadResult","0"));}
            return Result.success(new JSONObject().put("total",mode.equals("PAGE200")?202:group.size()).put("list",rows).toString());
        });
        AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();AuthOperationSchedulerProperties.Instance i=new AuthOperationSchedulerProperties.Instance();i.setId(instance());i.setAccessType("ISC");i.setParks(Collections.singletonList(park));settings.getInstances().add(i);return new AuthOperationTransportFacade(transport,remote,null,settings);
    }
    private void read(SmtAuthTransportPhase p){facade.readReceiptExact(park,instance(),Collections.singletonList(p.getId()),1);}
    private Received ack(SmtAuthTransportPhase p,String key){return transport.receipt(park,instance(),p.getId(),p.getPersonId(),p.getDeviceId(),p.getExternalId(),key,true,"合成可信物理ACK");}
    private String state(SmtAuthTransportPhase p){return phases.byId(p.getId()).getState();}
    private String targetState(SmtAuthTransportPhase p){return jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,p.getTargetId());}
    private int sourceCount(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN(SELECT ID FROM SMT_STAFF WHERE COMP_ID=?)",Integer.class,"employee-test-"+park);}
    private int eventCount(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",Integer.class,park);}
    private SmtAuthSourceResource selectionForTarget(SmtAuthTransportPhase p){SmtAuthSourceResource c=new SmtAuthSourceResource();Map<String,Object> row=jdbc.queryForMap("SELECT SOURCE_COORD_ID,SOURCE_GENERATION FROM SMT_AUTH_SOURCE_RESOURCE WHERE TARGET_ID=? AND BINDING_REVISION=0",p.getTargetId());c.setSourceCoordId((String)row.get("SOURCE_COORD_ID"));c.setSourceGeneration(((Number)row.get("SOURCE_GENERATION")).longValue());return c;}
    private static boolean causes(Throwable t,String text){for(Throwable c=t;c!=null;c=c.getCause())if(c.getMessage()!=null&&c.getMessage().contains(text))return true;return false;}
}
