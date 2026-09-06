package com.tce.smart.schedule.service.platform.impl;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSchedulerData.*;
import com.tce.smart.platform.core.dto.authtransport.AuthTransport.Run;
import com.tce.smart.platform.core.service.impl.*;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import org.junit.*;
import org.mockito.Mockito;
import java.util.*;
import java.util.concurrent.*;

/** 定时入口行为测试只替换数据库和外部接入边界，真实执行有界线程调度。 */
public class AuthOperationSchedulerTest {
    @Test public void emptyRecoveryPagesCompleteOncePerKindAndStillServeAllKinds() throws Exception {
        RecoveryRound round=new RecoveryRound();round.run();
        for(String kind:RecoveryRound.KINDS) {
            Assert.assertEquals(kind+"空页后不得再次查同类",Collections.singletonList(null),round.queries.get(kind));
            Assert.assertEquals(Integer.valueOf(1),round.reserves.get(kind));
            Assert.assertEquals(Integer.valueOf(1),round.completes.get(kind));
            Assert.assertNull(round.jobs.get(kind).getNumberCursor());Assert.assertNull(round.jobs.get(kind).getTextCursor());
        }
    }
    @Test public void nonEmptyRecoveryPagesKeepForwardPagingWithinOriginalBudget() throws Exception {
        RecoveryRound round=new RecoveryRound();round.props.setPageSize(200);for(String kind:RecoveryRound.KINDS)round.rows.put(kind,Arrays.asList(10L,20L,30L));
        round.run();
        for(String kind:RecoveryRound.KINDS) {
            Assert.assertEquals(Arrays.asList(null,round.cursor(kind,10)),round.queries.get(kind));
            Assert.assertEquals(round.cursor(kind,20),round.savedCursor(kind));
            Assert.assertEquals(Integer.valueOf(2),round.completes.get(kind));
        }
    }
    @Test public void emptyTailResetsPersistentCursorButWrapsOnlyOnNextTick() throws Exception {
        RecoveryRound round=new RecoveryRound();for(String kind:RecoveryRound.KINDS) {
            round.rows.put(kind,Collections.singletonList(10L));round.saveCursor(kind,20);
        }
        round.run();
        for(String kind:RecoveryRound.KINDS) {
            Assert.assertEquals(Collections.singletonList(round.cursor(kind,20)),round.queries.get(kind));Assert.assertNull(round.savedCursor(kind));
        }
        round.run();
        for(String kind:RecoveryRound.KINDS) {
            Assert.assertEquals(Arrays.asList(round.cursor(kind,20),null,round.cursor(kind,10)),round.queries.get(kind));
            Assert.assertNull(round.savedCursor(kind));
        }
    }
    @Test public void positiveThenEmptyPageResetsOnlyItsOwnCursor() throws Exception {
        RecoveryRound round=new RecoveryRound();for(String kind:RecoveryRound.KINDS)round.rows.put(kind,Collections.singletonList(10L));
        round.run();
        for(String kind:RecoveryRound.KINDS) {
            Assert.assertEquals(Arrays.asList(null,round.cursor(kind,10)),round.queries.get(kind));Assert.assertNull(round.savedCursor(kind));
            Assert.assertEquals(Integer.valueOf(2),round.completes.get(kind));
        }
    }
    @Test public void unavailableFailureOrLostLeaseStopsOnlyTheCurrentRecoveryKind() throws Exception {
        for(String mode:Arrays.asList("unavailable","failure","lost")) {
            RecoveryRound round=new RecoveryRound();round.mode=mode;round.saveCursor("RECOVER",10);round.rows.put("RECOVER",Collections.singletonList(20L));round.run();
            Assert.assertEquals(mode+"后本tick不得重新领取",Integer.valueOf(1),round.reserves.get("RECOVER"));
            Assert.assertEquals(mode,"10",round.savedCursor("RECOVER"));
            Assert.assertEquals("unavailable".equals(mode)?0:1,round.queries.get("RECOVER").size());
            for(String other:Arrays.asList("EXPIRE","CONVERGE","REFRESH"))Assert.assertEquals(Integer.valueOf(1),round.completes.get(other));
            if("failure".equals(mode))Assert.assertEquals(Collections.singletonList(false),round.recoverySuccess);
        }
    }
    @Test public void deferredRecoveryMemberDoesNotBlockHealthyNeighbourOrOtherKinds() throws Exception {
        RecoveryRound round=new RecoveryRound();round.rows.put("RECOVER",Arrays.asList(10L,20L));
        Mockito.when(round.ledger.itemDue("shared","R:10")).thenReturn(false);round.run();
        Mockito.verify(round.workflow,Mockito.never()).recoverPending("source-10",1L,"resource-10");
        Mockito.verify(round.workflow).recoverPending("source-20",1L,"resource-20");
        Assert.assertEquals("20",round.savedCursor("RECOVER"));
        for(String other:Arrays.asList("EXPIRE","CONVERGE","REFRESH"))Assert.assertEquals(Integer.valueOf(1),round.completes.get(other));
    }
    /** 租约完成后复制游标，避免mock共享Job引用掩盖失败时的持久游标变化。 */
    private static final class RecoveryRound {
        private static final List<String> KINDS=Arrays.asList("EXPIRE","RECOVER","CONVERGE","REFRESH");
        private final AuthOperationSchedulerProperties props=config();
        private final AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);
        private final AuthOperationWorkflowService workflow=Mockito.mock(AuthOperationWorkflowService.class);
        private final Map<String,Job> jobs=new LinkedHashMap<>();
        private final Map<String,List<Long>> rows=new LinkedHashMap<>();
        private final Map<String,List<Object>> queries=new LinkedHashMap<>();
        private final Map<String,Integer> reserves=new LinkedHashMap<>(),completes=new LinkedHashMap<>();
        private final List<Boolean> recoverySuccess=new ArrayList<>();
        private String mode="normal";
        private RecoveryRound() {
            props.setPageSize(1);props.setRecoveryPages(2);
            for(String kind:KINDS){Job job=new Job();job.setInstanceId("shared");job.setLane(kind);jobs.put(kind,job);rows.put(kind,Collections.emptyList());queries.put(kind,new ArrayList<>());}
            Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
            Mockito.when(ledger.reserve(Mockito.any(),Mockito.anyString(),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(call->{
                String kind=call.getArgument(1);reserves.merge(kind,1,Integer::sum);
                if("RECOVER".equals(kind) && "unavailable".equals(mode))return null;
                Job saved=jobs.get(kind),job=new Job();job.setInstanceId("shared");job.setLane(kind);job.setLeaseToken(kind+reserves.get(kind));job.setNumberCursor(saved.getNumberCursor());job.setTextCursor(saved.getTextCursor());
                Grant grant=new Grant();grant.setJob(job);return grant;
            });
            Mockito.when(ledger.expired(Mockito.any(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->page("EXPIRE",call.getArgument(1)));
            Mockito.when(ledger.refreshTargets(Mockito.any(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->page("REFRESH",call.getArgument(1)));
            Mockito.when(ledger.recoveries(Mockito.any(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->contributions("RECOVER",call.getArgument(1)));
            Mockito.when(ledger.convergences(Mockito.any(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->contributions("CONVERGE",call.getArgument(1)));
            Mockito.when(ledger.complete(Mockito.any(),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenAnswer(call->{
                Grant grant=call.getArgument(0);String kind=grant.getJob().getLane();boolean success=call.getArgument(1);completes.merge(kind,1,Integer::sum);
                if("RECOVER".equals(kind)){recoverySuccess.add(success);if("lost".equals(mode))return false;}
                if(success){jobs.get(kind).setNumberCursor(call.getArgument(2));jobs.get(kind).setTextCursor(call.getArgument(3));}return true;
            });
        }
        private List<Long> page(String kind,Object after) {
            queries.get(kind).add(after);if("RECOVER".equals(kind) && "failure".equals(mode))throw new IllegalStateException("受控分页失败");
            for(Long id:rows.get(kind))if(after==null || id>Long.parseLong(after.toString()))return Collections.singletonList(id);
            return Collections.emptyList();
        }
        private List<com.tce.smart.platform.core.entity.SmtAuthSourceResource> contributions(String kind,Object after) {
            List<com.tce.smart.platform.core.entity.SmtAuthSourceResource> result=new ArrayList<>();
            for(Long id:page(kind,after)) {
                com.tce.smart.platform.core.entity.SmtAuthSourceResource c=new com.tce.smart.platform.core.entity.SmtAuthSourceResource();
                c.setId(id.toString());c.setSourceCoordId("source-"+id);c.setSourceGeneration(1L);c.setResourceCoordId("resource-"+id);result.add(c);
            }
            return result;
        }
        private Object cursor(String kind,long id){return Arrays.asList("EXPIRE","REFRESH").contains(kind)?Long.valueOf(id):Long.toString(id);}
        private Object savedCursor(String kind){return Arrays.asList("EXPIRE","REFRESH").contains(kind)?jobs.get(kind).getNumberCursor():jobs.get(kind).getTextCursor();}
        private void saveCursor(String kind,long id){if(Arrays.asList("EXPIRE","REFRESH").contains(kind))jobs.get(kind).setNumberCursor(id);else jobs.get(kind).setTextCursor(Long.toString(id));}
        private void run() throws Exception {
            AuthOperationScheduler scheduler=new AuthOperationScheduler(props,new AuthOperationProperties(),ledger,null,null,workflow);
            java.lang.reflect.Method method=AuthOperationScheduler.class.getDeclaredMethod("run",AuthOperationSchedulerProperties.Instance.class,String.class);method.setAccessible(true);
            method.invoke(scheduler,props.getInstances().get(0),"RECOVERY");
        }
    }
    @Test public void dedicatedClientBoundsTimeoutsAndDoesNotRetryUnknownIoFailure() {
        org.springframework.cloud.openfeign.FeignContext context=Mockito.mock(org.springframework.cloud.openfeign.FeignContext.class);
        java.util.concurrent.atomic.AtomicInteger calls=new java.util.concurrent.atomic.AtomicInteger();
        feign.Client client=(request,options)->{calls.incrementAndGet();Assert.assertEquals(2000,options.connectTimeoutMillis());Assert.assertEquals(5000,options.readTimeoutMillis());throw new java.io.IOException("受控网络结果未知");};
        Mockito.when(context.getInstance(Mockito.anyString(),Mockito.eq(feign.Client.class))).thenReturn(client);
        Mockito.when(context.getInstance(Mockito.anyString(),Mockito.eq(feign.Contract.class))).thenReturn(new org.springframework.cloud.openfeign.support.SpringMvcContract());
        Mockito.when(context.getInstance(Mockito.anyString(),Mockito.eq(feign.codec.Encoder.class))).thenReturn(Mockito.mock(feign.codec.Encoder.class));
        Mockito.when(context.getInstance(Mockito.anyString(),Mockito.eq(feign.codec.Decoder.class))).thenReturn(Mockito.mock(feign.codec.Decoder.class));
        com.tce.smart.dispatcher.api.feign.RemoteDispatcherService remote=new com.tce.smart.schedule.config.AuthOperationHttpConfiguration().dispatcher(context,client);
        try {remote.dispatch(new com.tce.smart.dispatcher.api.dto.req.DispatcherDTO<>(),"Y");Assert.fail("网络失败必须传播且不得自动重试");}catch(feign.RetryableException expected) { }
        Assert.assertEquals(1,calls.get());
    }
    @Test public void disabledSchedulerDoesNotAccessTablesOrTransport() {
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);
        AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        AuthOperationScheduler scheduler=new AuthOperationScheduler(new AuthOperationSchedulerProperties(),new AuthOperationProperties(),ledger,transport,null,null);
        scheduler.start();scheduler.tick();scheduler.stop();Mockito.verifyZeroInteractions(ledger,transport);
    }
    @Test public void missingAndAmbiguousInstanceMappingsFailBeforeScheduling() {
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(new HashSet<>(Arrays.asList(7,8)));
        AuthOperationSchedulerProperties props=config();
        try {props.validate(core);Assert.fail("未映射灰度园区必须拒绝启动");}catch(IllegalStateException expected) { }
        props.getInstances().get(0).setParks(Arrays.asList(7,8));props.validate(core);
        AuthOperationSchedulerProperties.Instance duplicate=new AuthOperationSchedulerProperties.Instance();duplicate.setId("second");duplicate.setAccessType("ISC");duplicate.setParks(Arrays.asList(7));props.getInstances().add(duplicate);
        try {props.validate(core);Assert.fail("重复实例映射不得放大额度");}catch(IllegalStateException expected) { }
    }
    @Test public void iscRequestBatchRequiresEnoughTargetSlotsAfterBothBusinessReservations() {
        AuthOperationSchedulerProperties props=config();AuthOperationSchedulerProperties.Instance instance=props.getInstances().get(0);
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        Assert.assertEquals("默认ISC批量不得越过新增或撤权保留位",20,props.dispatchPage(instance,"DELETE"));
        instance.setIscBatchTargetSize(21);
        try {props.validate(core);Assert.fail("超过设备反向保留后的批量必须在启动前拒绝");}catch(IllegalStateException expected) { }
        instance.setIscBatchTargetSize(200);instance.setHttpPerSecond(300);instance.setDeleteHttp(100);instance.setAddHttp(50);instance.setConfigHttp(30);instance.setReceiptHttp(30);instance.setBorrowHttp(20);
        instance.setMaxInflight(2000);instance.setDeleteInflight(200);instance.setAddInflight(200);instance.setPerParkInflight(2000);instance.setPerParkDeleteInflight(200);instance.setPerParkAddInflight(200);
        instance.setPerDeviceInflight(220);instance.setPerDeviceDeleteInflight(20);instance.setPerDeviceAddInflight(20);instance.setParkHttpPerSecond(200);instance.setDeviceHttpPerSecond(32);
        props.setDispatchWorkItems(32);props.setDispatchMillis(1000);
        props.validate(core);
        Assert.assertEquals("显式高峰配置必须让ISC准备阶段按200人请求分片",200,props.dispatchPage(instance,"DELETE"));
        Assert.assertEquals("ISC后续配置和回执必须沿用同一受控请求分片",200,props.dispatchPage(instance,"CONFIG"));
    }
    @Test public void blockedAddWorkerDoesNotStarveReceiptWorker() throws Exception {
        AuthOperationSchedulerProperties props=config();AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);
        AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        CountDownLatch addEntered=new CountDownLatch(1),release=new CountDownLatch(1),receipt=new CountDownLatch(1);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reservePark(Mockito.any(Policy.class),Mockito.anyString(),Mockito.anyInt(),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(invocation->{
            String lane=invocation.getArgument(1);if(!Arrays.asList("ADD","RECEIPT").contains(lane))return null;
            Grant g=new Grant();g.setPolicy(invocation.getArgument(0));g.setHttpBudget(10);g.setPhaseIds(Collections.singletonList(1L));g.setPhaseOperation("ADD".equals(lane)?"SUBMIT":"RECEIPT");Job j=new Job();j.setInstanceId("shared");j.setLane(lane);j.setLeaseToken(lane);g.setJob(j);return g;
        });
        Mockito.when(transport.submitPreparedExact(Mockito.eq(7),Mockito.eq("shared"),Mockito.anyList(),Mockito.eq(10))).thenAnswer(invocation->{addEntered.countDown();release.await(5,TimeUnit.SECONDS);return Run.builder().outcome("IDLE").build();});
        Mockito.when(transport.readReceiptExact(Mockito.eq(7),Mockito.eq("shared"),Mockito.anyList(),Mockito.eq(10))).thenAnswer(invocation->{receipt.countDown();throw new IllegalStateException("受控回执异常");});
        AuthOperationScheduler scheduler=new AuthOperationScheduler(props,core,ledger,transport,null,null);
        scheduler.start();try {scheduler.tick();Assert.assertTrue(addEntered.await(3,TimeUnit.SECONDS));Assert.assertTrue("新增阻塞时回执必须独立执行",receipt.await(3,TimeUnit.SECONDS));}finally{release.countDown();scheduler.stop();}
    }
    @Test public void oneBatchBorrowsFurtherSlicesInSameTickWithinGlobalStepBudget() throws Exception {
        expansionTick(false);
    }
    @Test public void newDeleteGetsAChanceBeforeLargeAddTakesAnotherSlice() throws Exception {
        expansionTick(true);
    }
    private void expansionTick(boolean newDelete) throws Exception {
        AuthOperationSchedulerProperties props=config();AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);EmployeeAuthOperationService employee=Mockito.mock(EmployeeAuthOperationService.class);
        List<Long> order=new CopyOnWriteArrayList<>();CountDownLatch completed=new CountDownLatch(1);
        Grant grant=new Grant();Job job=new Job();job.setInstanceId("shared");job.setLane("EXPAND");grant.setJob(job);
        Mockito.when(ledger.reserve(Mockito.any(),Mockito.eq("EXPAND"),Mockito.anyInt(),Mockito.anyLong())).thenReturn(grant);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(employee.pendingExpansionBatches(Mockito.anyList(),Mockito.any(),Mockito.anyInt())).thenReturn(Collections.singletonList(10L));
        Mockito.when(ledger.expansionBatches(Mockito.any(),Mockito.anyString(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->{
            String priority=call.getArgument(1);Long after=call.getArgument(2);long id="DELETE".equals(priority)?20L:10L;
            if("DELETE".equals(priority) && (!newDelete || order.isEmpty()))return Collections.emptyList();
            return after==null || after<id?Collections.singletonList(id):Collections.emptyList();
        });
        Mockito.when(employee.stageNext(Mockito.anyLong())).thenAnswer(call->{order.add(call.getArgument(0));return true;});
        Mockito.when(ledger.complete(Mockito.same(grant),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenAnswer(call->{completed.countDown();return true;});
        AuthOperationScheduler scheduler=new AuthOperationScheduler(props,core,ledger,null,employee,null);scheduler.start();
        try {
            scheduler.tick();Assert.assertTrue(completed.await(2,TimeUnit.SECONDS));
            Assert.assertEquals("单批应借用后续片，但总计不超过32步",32,order.size());
            if(newDelete)Assert.assertEquals("新DELETE必须在大ADD首个4步片之后获得机会",4,order.indexOf(20L));
            else for(Long id:order)Assert.assertEquals(Long.valueOf(10),id);
        } finally {scheduler.stop();}
    }
    @Test public void oneStepBudgetSurvivesRestartThroughStageBindAndFinish() throws Exception {
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);EmployeeAuthOperationService employee=Mockito.mock(EmployeeAuthOperationService.class);
        java.util.concurrent.atomic.AtomicInteger stage=new java.util.concurrent.atomic.AtomicInteger();List<String> calls=new CopyOnWriteArrayList<>();
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.expansionStage(Mockito.anyString(),Mockito.eq(10L))).thenAnswer(call->stage.get());
        Mockito.doAnswer(call->{stage.set(call.getArgument(2));return null;}).when(ledger).advanceExpansionStage(Mockito.anyString(),Mockito.eq(10L),Mockito.anyInt());
        Mockito.when(ledger.expansionBatches(Mockito.any(),Mockito.anyString(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->"ADD".equals(call.getArgument(1))?Collections.singletonList(10L):Collections.emptyList());
        Mockito.when(employee.stageNext(10L)).thenAnswer(call->{calls.add("stage");return false;});
        Mockito.when(employee.bindNextLane(Mockito.eq(10L),Mockito.any())).thenAnswer(call->{calls.add("bind");return null;});
        Mockito.doAnswer(call->{calls.add("finish");return null;}).when(employee).finish(10L);
        for(int n=0;n<3;n++) {
            AuthOperationSchedulerProperties props=config();props.setExpansionSteps(1);props.setExpansionBatchSteps(1);
            AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
            Grant grant=new Grant();Job job=new Job();job.setInstanceId("shared");job.setLane("EXPAND");grant.setJob(job);CountDownLatch done=new CountDownLatch(1);
            Mockito.when(ledger.reserve(Mockito.any(),Mockito.eq("EXPAND"),Mockito.anyInt(),Mockito.anyLong())).thenReturn(grant);
            Mockito.when(ledger.complete(Mockito.same(grant),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenAnswer(call->{done.countDown();return true;});
            AuthOperationScheduler scheduler=new AuthOperationScheduler(props,core,ledger,null,employee,null);scheduler.start();
            try{scheduler.tick();Assert.assertTrue(done.await(2,TimeUnit.SECONDS));Assert.assertEquals(n+1,calls.size());}finally{
                scheduler.stop();java.lang.reflect.Field field=AuthOperationScheduler.class.getDeclaredField("executors");field.setAccessible(true);
                for(Object executor:((Map<?,?>)field.get(scheduler)).values())Assert.assertTrue(((ExecutorService)executor).awaitTermination(2,TimeUnit.SECONDS));
            }
        }
        Assert.assertEquals(Arrays.asList("stage","bind","finish"),calls);
    }
    @Test public void elapsedCheckpointStopsStartingFurtherStepsWithoutPretendingToCancelCurrentOne() throws Exception {
        AuthOperationSchedulerProperties props=config();AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);EmployeeAuthOperationService employee=Mockito.mock(EmployeeAuthOperationService.class);
        java.util.concurrent.atomic.AtomicLong clock=new java.util.concurrent.atomic.AtomicLong();java.util.concurrent.atomic.AtomicInteger calls=new java.util.concurrent.atomic.AtomicInteger();CountDownLatch done=new CountDownLatch(1);
        Grant grant=new Grant();Job job=new Job();job.setInstanceId("shared");job.setLane("EXPAND");grant.setJob(job);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reserve(Mockito.any(),Mockito.eq("EXPAND"),Mockito.anyInt(),Mockito.anyLong())).thenReturn(grant);
        Mockito.when(ledger.expansionBatches(Mockito.any(),Mockito.anyString(),Mockito.any(),Mockito.anyInt())).thenAnswer(call->"ADD".equals(call.getArgument(1))?Collections.singletonList(10L):Collections.emptyList());
        Mockito.when(employee.stageNext(10L)).thenAnswer(call->{calls.incrementAndGet();clock.set(TimeUnit.MILLISECONDS.toNanos(501));return true;});
        Mockito.when(ledger.complete(Mockito.same(grant),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenAnswer(call->{done.countDown();return true;});
        AuthOperationScheduler scheduler=Mockito.spy(new AuthOperationScheduler(props,core,ledger,null,employee,null));Mockito.doAnswer(call->clock.get()).when(scheduler).expansionNow();scheduler.start();
        try {scheduler.tick();Assert.assertTrue(done.await(2,TimeUnit.SECONDS));Assert.assertEquals(1,calls.get());}finally{scheduler.stop();}
    }
    @Test public void oneTickPreparesThenSendsThreeAndStopsWhenQuotaIsEmpty() throws Exception {
        AuthOperationSchedulerProperties props=config();props.getInstances().get(0).setAccessType("DIRECT");
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        java.util.concurrent.atomic.AtomicInteger reservations=new java.util.concurrent.atomic.AtomicInteger(),http=new java.util.concurrent.atomic.AtomicInteger();CountDownLatch exhausted=new CountDownLatch(1);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reservePark(Mockito.any(),Mockito.eq("DELETE"),Mockito.eq(7),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(call->{
            int n=reservations.incrementAndGet();if(n>=3){exhausted.countDown();return null;}
            Grant grant=drainGrant(n==1?0:3);if(n==1)grant.getClaims().put(7,Collections.singletonList(com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget.builder().targetId(10L).build()));return grant;
        });
        Mockito.when(transport.submit(Mockito.eq(7),Mockito.eq("shared"),Mockito.anyList(),Mockito.eq(0))).thenReturn(Run.builder().outcome("IDLE").nextCursor(13L).build());
        Mockito.when(transport.submitPreparedExact(Mockito.eq(7),Mockito.eq("shared"),Mockito.anyList(),Mockito.eq(3))).thenAnswer(call->{http.addAndGet(3);return Run.builder().outcome("WAITING_CONFIRM").processed(3).httpUsed(3).build();});
        Mockito.when(ledger.complete(Mockito.any(),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenReturn(true);
        AuthOperationScheduler scheduler=new AuthOperationScheduler(props,core,ledger,transport,null,null);scheduler.setDirectTakeover(Mockito.mock(AuthOperationDirectTakeoverService.class));scheduler.start();
        try {scheduler.tick();Assert.assertTrue("一次tick应完成prepare0和发送3HTTP，而非等第二tick",exhausted.await(2,TimeUnit.SECONDS));}
        finally{stopAndAwait(scheduler);}
        Assert.assertEquals(3,http.get());Assert.assertEquals("配额为空后不得忙循环",3,reservations.get());
    }
    @Test public void continuousSuccessStillStopsAfterEightIndependentWorkItems() throws Exception {
        AuthOperationSchedulerProperties props=config();AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        java.util.concurrent.atomic.AtomicInteger reservations=new java.util.concurrent.atomic.AtomicInteger();CountDownLatch first=new CountDownLatch(1);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reservePark(Mockito.any(),Mockito.eq("DELETE"),Mockito.eq(7),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(call->{reservations.incrementAndGet();return drainGrant(3);});
        Mockito.when(transport.submitPreparedExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenReturn(Run.builder().outcome("WAITING_CONFIRM").processed(3).httpUsed(3).build());
        Mockito.when(ledger.complete(Mockito.any(),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenAnswer(call->{first.countDown();return true;});
        AuthOperationScheduler scheduler=new AuthOperationScheduler(props,core,ledger,transport,null,null);scheduler.start();
        try{scheduler.tick();Assert.assertTrue(first.await(2,TimeUnit.SECONDS));}finally{stopAndAwait(scheduler);}
        Assert.assertEquals(8,reservations.get());
    }
    @Test public void unknownBackoffAndUnchangedPollNeverStartAnotherWorkItem()throws Exception {
        Assert.assertEquals(1,drainScenario("SUBMIT",Run.builder().outcome("UNKNOWN").processed(3).httpUsed(3).build(),true,false));
        Assert.assertEquals(1,drainScenario("SUBMIT",Run.builder().outcome("BACKOFF").httpUsed(3).build(),true,false));
        Assert.assertEquals(1,drainScenario("SUBMIT",Run.builder().outcome("IDLE").build(),true,false));
        Assert.assertEquals(1,drainScenario("CONFIG_PROGRESS",Run.builder().outcome("WAITING_CONFIG").httpUsed(1).build(),true,false));
        Assert.assertEquals(1,drainScenario("RECEIPT",Run.builder().outcome("VERIFYING").processed(1).httpUsed(1).build(),true,false));
    }
    @Test public void lostCompletionLeaseStopsEvenAfterRemoteProgress()throws Exception {
        Assert.assertEquals(1,drainScenario("SUBMIT",Run.builder().outcome("WAITING_CONFIRM").processed(3).httpUsed(3).build(),false,false));
    }
    @Test public void successfulSlowCallCannotStartAnotherItemAfterCheckpoint()throws Exception {
        Assert.assertEquals(1,drainScenario("SUBMIT",Run.builder().outcome("WAITING_CONFIRM").processed(3).httpUsed(3).build(),true,true));
    }
    @Test public void stageSpecificProgressAllowsDownloadAndConfirmedConfigWithoutProcessedHttpCount()throws Exception {
        Assert.assertEquals(8,drainScenario("DOWNLOAD",Run.builder().outcome("WAITING_CONFIRM").httpUsed(1).build(),true,false));
        Assert.assertEquals(8,drainScenario("CONFIG_PROGRESS",Run.builder().outcome("IDLE").processed(3).httpUsed(1).build(),true,false));
        Assert.assertEquals(8,drainScenario("RECEIPT",Run.builder().outcome("MORE").nextPage(2).httpUsed(1).build(),true,false));
    }
    private int drainScenario(String operation,Run result,boolean completes,boolean elapsed)throws Exception {
        AuthOperationSchedulerProperties props=config();AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        java.util.concurrent.atomic.AtomicInteger reservations=new java.util.concurrent.atomic.AtomicInteger();java.util.concurrent.atomic.AtomicLong clock=new java.util.concurrent.atomic.AtomicLong();CountDownLatch done=new CountDownLatch(1);
        String lane="SUBMIT".equals(operation)?"DELETE":("RECEIPT".equals(operation)?"RECEIPT":"CONFIG");
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reservePark(Mockito.any(),Mockito.eq(lane),Mockito.eq(7),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(call->{reservations.incrementAndGet();Grant grant=drainGrant(3);grant.setPhaseOperation(operation);return grant;});
        org.mockito.stubbing.Answer<Run> response=call->{if(elapsed)clock.set(TimeUnit.MILLISECONDS.toNanos(501));return result;};
        Mockito.when(transport.submitPreparedExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenAnswer(response);
        Mockito.when(transport.advanceConfigExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenAnswer(response);
        Mockito.when(transport.downloadExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenAnswer(response);
        Mockito.when(transport.readReceiptExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenAnswer(response);
        Mockito.when(ledger.complete(Mockito.any(),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenAnswer(call->{done.countDown();return completes;});
        AuthOperationScheduler scheduler=Mockito.spy(new AuthOperationScheduler(props,core,ledger,transport,null,null));Mockito.doAnswer(call->clock.get()).when(scheduler).dispatchNow();scheduler.start();
        try{scheduler.tick();Assert.assertTrue(done.await(2,TimeUnit.SECONDS));}finally{stopAndAwait(scheduler);}
        return reservations.get();
    }
    @Test public void concurrentTicksCannotReplaceTheActiveDrainOwner()throws Exception {
        AuthOperationSchedulerProperties props=config();AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        java.util.concurrent.atomic.AtomicInteger reservations=new java.util.concurrent.atomic.AtomicInteger();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reservePark(Mockito.any(),Mockito.eq("DELETE"),Mockito.eq(7),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(call->{reservations.incrementAndGet();return drainGrant(3);});
        Mockito.when(transport.submitPreparedExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenAnswer(call->{entered.countDown();Assert.assertTrue(release.await(2,TimeUnit.SECONDS));return Run.builder().outcome("WAITING_CONFIRM").processed(3).httpUsed(3).build();});
        Mockito.when(ledger.complete(Mockito.any(),Mockito.anyBoolean(),Mockito.any(),Mockito.any(),Mockito.anyInt(),Mockito.anyInt())).thenReturn(true);
        AuthOperationScheduler scheduler=Mockito.spy(new AuthOperationScheduler(props,core,ledger,transport,null,null));Mockito.doReturn(0L).when(scheduler).dispatchNow();scheduler.start();
        try {scheduler.tick();Assert.assertTrue(entered.await(2,TimeUnit.SECONDS));scheduler.tick();scheduler.tick();Assert.assertEquals(1,reservations.get());}
        finally {release.countDown();stopAndAwait(scheduler);}
        Assert.assertEquals("同一active owner只能完成本轮8项，额外tick不得启动另一轮",8,reservations.get());
    }
    @Test public void mixedUnknownMemberIsNotSelectedAgainWhileHealthyGroupAdvancesInOracle()throws Exception {
        Assume.assumeTrue(System.getenv("SMART_AUTH_ORACLE_URL")!=null);
        // 复用固定版本测试夹具；反射只访问测试私有种子，不扩展生产API或修改另一测试文件。
        AuthOperationSchedulerOracleTest fixture=new AuthOperationSchedulerOracleTest();fixture.setUp();AuthOperationScheduler scheduler=null;
        try {
            Policy policy=fixtureField(fixture,"policy",Policy.class);int park=policy.getParks().get(0);
            org.springframework.jdbc.core.JdbcTemplate jdbc=fixtureField(fixture,"jdbc",org.springframework.jdbc.core.JdbcTemplate.class);
            AuthOperationSchedulerService ledger=fixtureField(fixture,"ledger",AuthOperationSchedulerService.class);
            com.tce.smart.platform.core.mapper.AuthOperationSchedulerMapper mapper=fixtureField(fixture,"mapper",com.tce.smart.platform.core.mapper.AuthOperationSchedulerMapper.class);
            java.lang.reflect.Method seed=AuthOperationSchedulerOracleTest.class.getDeclaredMethod("phaseFixture",int.class,String.class,String.class,String.class,int.class);seed.setAccessible(true);
            @SuppressWarnings("unchecked") List<Long> first=(List<Long>)seed.invoke(fixture,park,"SUBMIT","DELETE","a-mixed",3);
            @SuppressWarnings("unchecked") List<Long> healthy=(List<Long>)seed.invoke(fixture,park,"SUBMIT","DELETE","b-healthy",3);
            Long unknown=first.get(0);List<List<Long>> sent=new CopyOnWriteArrayList<>();CountDownLatch advanced=new CountDownLatch(1);
            AuthOperationSchedulerProperties props=config();props.getInstances().get(0).setId(policy.getInstanceId());props.getInstances().get(0).setParks(Collections.singletonList(park));
            AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(park));
            AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
            Mockito.when(transport.submitPreparedExact(Mockito.eq(park),Mockito.eq(policy.getInstanceId()),Mockito.anyList(),Mockito.eq(3))).thenAnswer(call->{
                List<Long> ids=new ArrayList<>((List<Long>)call.getArgument(2));sent.add(ids);
                for(Long id:ids) {
                    jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET STATE=? WHERE ID=?",id.equals(unknown)?"UNKNOWN":"ACCEPTED",id);
                    if(id.equals(unknown))jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='VERIFYING' WHERE ID=(SELECT TARGET_ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE ID=?)",id);
                }
                if(sent.size()==2)advanced.countDown();
                // 真实facade混合组的最后成功会覆盖前面的UNKNOWN，允许健康组继续但未知成员不可重发。
                return Run.builder().outcome("WAITING_CONFIG").processed(ids.size()).httpUsed(3).build();
            });
            scheduler=new AuthOperationScheduler(props,core,ledger,transport,null,null);scheduler.start();scheduler.tick();Assert.assertTrue(advanced.await(3,TimeUnit.SECONDS));stopAndAwait(scheduler);scheduler=null;
            Assert.assertEquals(2,sent.size());Assert.assertEquals(first,sent.get(0));Assert.assertEquals(healthy,sent.get(1));
            Assert.assertEquals("UNKNOWN",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_TRANSPORT_PHASE WHERE ID=?",String.class,unknown));
            Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=(SELECT TARGET_ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE ID=?)",String.class,unknown));
            Assert.assertTrue(mapper.phaseWork(policy,"DELETE","DELETE",null,null,mapper.windowKey(),3).isEmpty());
            Assert.assertTrue(mapper.candidates(policy,"DELETE",null,mapper.now(),200).isEmpty());
            Assert.assertEquals(6,mapper.parkCounts(policy).stream().mapToInt(Count::getTargetCount).sum());
        } finally {if(scheduler!=null)stopAndAwait(scheduler);try{fixture.cleanUp();}finally{AuthOperationSchedulerOracleTest.closePool();}}
    }
    private static <T>T fixtureField(Object fixture,String name,Class<T> type)throws Exception {
        java.lang.reflect.Field field=AuthOperationSchedulerOracleTest.class.getDeclaredField(name);field.setAccessible(true);return type.cast(field.get(fixture));
    }
    private static Grant drainGrant(int budget) {
        Grant grant=new Grant();Job job=new Job();job.setInstanceId("shared");job.setLane("DELETE:7");grant.setJob(job);grant.setHttpBudget(budget);grant.setPhaseOperation("SUBMIT");grant.setPhaseIds(Arrays.asList(11L,12L,13L));return grant;
    }
    private static void stopAndAwait(AuthOperationScheduler scheduler)throws Exception {
        scheduler.stop();java.lang.reflect.Field field=AuthOperationScheduler.class.getDeclaredField("executors");field.setAccessible(true);
        for(Object executor:((Map<?,?>)field.get(scheduler)).values())Assert.assertTrue(((ExecutorService)executor).awaitTermination(3,TimeUnit.SECONDS));
    }
    private static AuthOperationSchedulerProperties config() {
        AuthOperationSchedulerProperties props=new AuthOperationSchedulerProperties();props.setEnabled(true);
        AuthOperationSchedulerProperties.Instance i=new AuthOperationSchedulerProperties.Instance();i.setId("shared");i.setAccessType("ISC");i.setParks(Collections.singletonList(7));props.getInstances().add(i);return props;
    }
    @Test public void slowParkDoesNotBlockHealthyParkOrOtherInstanceInSameLane() throws Exception {
        AuthOperationSchedulerProperties props=config();props.getInstances().get(0).setParks(Arrays.asList(7,8));
        AuthOperationSchedulerProperties.Instance other=new AuthOperationSchedulerProperties.Instance();other.setId("other");other.setAccessType("ISC");other.setParks(Collections.singletonList(9));props.getInstances().add(other);
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(new HashSet<>(Arrays.asList(7,8,9)));
        AuthOperationSchedulerService ledger=Mockito.mock(AuthOperationSchedulerService.class);AuthOperationTransportFacade transport=Mockito.mock(AuthOperationTransportFacade.class);
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1),healthy=new CountDownLatch(2);
        Mockito.when(ledger.itemDue(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Mockito.when(ledger.reservePark(Mockito.any(Policy.class),Mockito.anyString(),Mockito.anyInt(),Mockito.anyInt(),Mockito.anyLong())).thenAnswer(call->{
            String lane=call.getArgument(1);if(!"RECEIPT".equals(lane))return null;
            Policy p=call.getArgument(0);Grant g=new Grant();g.setPolicy(p);g.setHttpBudget(1);g.setPhaseIds(Collections.singletonList(1L));g.setPhaseOperation("RECEIPT");Job j=new Job();j.setInstanceId(p.getInstanceId());j.setLane(lane);g.setJob(j);return g;
        });
        Mockito.when(transport.readReceiptExact(Mockito.anyInt(),Mockito.anyString(),Mockito.anyList(),Mockito.anyInt())).thenAnswer(call->{
            int park=call.getArgument(0);if(park==7){entered.countDown();Assert.assertTrue(release.await(5,TimeUnit.SECONDS));}else healthy.countDown();
            return Run.builder().outcome("IDLE").build();
        });
        AuthOperationScheduler scheduler=new AuthOperationScheduler(props,core,ledger,transport,null,null);scheduler.start();
        try {scheduler.tick();Assert.assertTrue(entered.await(2,TimeUnit.SECONDS));Assert.assertTrue("慢园区未返回时同lane健康园区及另一实例必须前进",healthy.await(2,TimeUnit.SECONDS));}
        finally {release.countDown();scheduler.stop();}
    }
}
