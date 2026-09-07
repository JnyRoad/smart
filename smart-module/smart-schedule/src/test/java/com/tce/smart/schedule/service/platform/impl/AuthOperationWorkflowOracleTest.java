package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.*;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.time.LocalDateTime;
import java.util.*;

/** 真实本机 Oracle 上的组合原子性、来源共享、可信收敛与补偿验收。 */
public class AuthOperationWorkflowOracleTest {
    private static HikariDataSource pool;
    private JdbcTemplate jdbc;
    private AuthOperationWorkflowService service;
    private AuthOperationService operations;
    private AuthOperationVersionService versions;
    private AuthOperationWorkflowMapper workflow;
    private int park;
    private org.springframework.transaction.support.TransactionTemplate outer;

    @Before public void setup() throws Exception {
        String url=System.getenv("SMART_AUTH_ORACLE_URL");Assume.assumeTrue(url!=null);
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",url);
        Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        if(pool==null) { pool=new HikariDataSource();pool.setJdbcUrl(url);pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
            pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("auth-workflow-test"); }
        jdbc=new JdbcTemplate(pool);park=100000+(int)(Math.abs(UUID.randomUUID().getMostSignificantBits()%600000000));
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_WORKFLOW_SHARD'",Integer.class)==0)
            new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(new ClassPathResource("auth-operation/workflow-schema.sql")).execute(pool);
        MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);
        Class<?>[] mappers={SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,
            SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,SmtAuthSubjectCoordMapper.class,
            SmtAuthSourceCoordMapper.class,SmtAuthResourceCoordMapper.class,SmtAuthSourceResourceMapper.class,
            SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class};
        List<Resource> xml=new ArrayList<>();for(Class<?> type:mappers) { cfg.addMapper(type);xml.add(new ClassPathResource("mapper/"+type.getSimpleName()+".xml")); }
        MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(cfg);
        factory.setMapperLocations(xml.toArray(new Resource[0]));SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());
        DataSourceTransactionManager tm=new DataSourceTransactionManager(pool);outer=new org.springframework.transaction.support.TransactionTemplate(tm);
        operations=proxy(new AuthOperationService(session.getMapper(SmtAuthOperationBatchMapper.class),session.getMapper(SmtAuthDeleteRequestMapper.class),
            session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(SmtAuthResultEventMapper.class)),tm);
        versions=proxy(new AuthOperationVersionService(session.getMapper(SmtAuthSubjectCoordMapper.class),session.getMapper(SmtAuthSourceCoordMapper.class),
            session.getMapper(SmtAuthResourceCoordMapper.class),session.getMapper(SmtAuthSourceResourceMapper.class),session.getMapper(SmtAuthIdentityAliasMapper.class)),tm);
        workflow=session.getMapper(AuthOperationWorkflowMapper.class);
        service=proxy(new AuthOperationWorkflowService(operations,versions,session.getMapper(SmtAuthOperationBatchMapper.class),
            session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(AuthOperationWorkflowMapper.class)),tm);
    }
    @SuppressWarnings("unchecked") private <T> T proxy(T raw,DataSourceTransactionManager tm) {
        ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();
    }
    @AfterClass public static void close() { if(pool!=null){pool.close();pool=null;} }
    @After public void cleanup() {
        if(jdbc==null)return;
        jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_IDENTITY_ALIAS WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SUBJECT_COORD WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",park);
    }
    @Test public void sharedSourcesHaveOneTargetAndTwoRequestsWithIdempotentShardReplay() {
        long batch=accept("shared","ADD",1,2);
        Expanded a=stage(batch,"a","ADD",0,1);
        try {service.bindLane(batch,a.getBindings().get(0).getResourceId(),1,2);Assert.fail("未齐来源不能绑定中间版本");}
        catch(IllegalArgumentException expected) { }
        Expanded b=stage(batch,"b","ADD",1,2);
        Expanded replay=stage(batch,"b","ADD",1,2);
        List<Binding> lane=service.bindLane(batch,a.getBindings().get(0).getResourceId(),2,3);
        Assert.assertEquals(2,lane.size());
        Assert.assertEquals(lane.get(0).getTargetId(),lane.get(1).getTargetId());
        Assert.assertEquals(b.getRequestId(),replay.getRequestId());
        Assert.assertEquals(1,count("SMT_AUTH_OPERATION_TARGET"));Assert.assertEquals(2,count("SMT_AUTH_DELETE_REQUEST"));
        Assert.assertEquals(1L,b.getSource().getGeneration());service.finish(batch);
    }
    @Test public void sameBatchDifferentOverlappingWindowsCreateOnlyFinalPhysicalTarget() {
        long batch=accept("different-windows","ADD",1,2);
        Expanded a=stage(batch,"a","ADD",0,1);
        SourceIntent second=intent(batch,"b","ADD").toBuilder().clearWindows()
            .window(Window.builder().from(LocalDateTime.of(2026,9,20,0,0)).to(LocalDateTime.of(2026,10,10,0,0)).build()).build();
        service.stage(Shard.builder().batchId(batch).previousCursor(1).nextCursor(2).source(second).staffAuthId("b").resource(input("device-1")).finalSourcePage(true).build());
        Assert.assertEquals(0,count("SMT_AUTH_OPERATION_TARGET"));
        List<Binding> bindings=service.bindLane(batch,a.getBindings().get(0).getResourceId(),2,3);
        Assert.assertEquals(2,bindings.size());Assert.assertEquals(1,count("SMT_AUTH_OPERATION_TARGET"));
        Assert.assertEquals(LocalDateTime.of(2026,10,10,0,0),jdbc.queryForObject("SELECT VALID_TO FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",java.sql.Timestamp.class,bindings.get(0).getTargetId()).toLocalDateTime());
        service.finish(batch);
    }
    @Test public void invalidSecondResourceRollsBackSourceRequestTargetsAndCursor() {
        long batch=accept("rollback","ADD",2);
        SourceIntent s=intent(batch,"a","ADD");
        Shard bad=Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(s).staffAuthId("a").finalSourcePage(true)
            .resource(input("device-1")).resource(ResourceInput.builder().resource(key("device-2").toBuilder().subjectId("another").build()).build()).build();
        try { service.stage(bad);Assert.fail("资源主体不符应整片回滚"); } catch(IllegalArgumentException expected) { }
        Assert.assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID=?",Integer.class,batch).intValue());
        Assert.assertEquals(0,count("SMT_AUTH_SOURCE_COORD"));Assert.assertEquals(0,count("SMT_AUTH_DELETE_REQUEST"));
        Assert.assertEquals(0,count("SMT_AUTH_OPERATION_TARGET"));
        Assert.assertEquals(0L,jdbc.queryForObject("SELECT EXPANSION_CURSOR FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",Long.class,batch).longValue());
        Expanded fixed=service.stage(bad.toBuilder().clearResources().resource(input("device-1")).resource(input("device-2")).build());
        service.bindLane(batch,fixed.getBindings().get(0).getResourceId(),1,2);
        service.bindLane(batch,fixed.getBindings().get(1).getResourceId(),2,3);service.finish(batch);
    }
    @Test public void currentAckAndConditionalBusinessFailureRollbackTogetherThenReplayConverges() {
        Expanded e=expand(accept("ack","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));
        Binding sent=send(e.getBindings().get(0));AuthOperationReceiptCommand ack=ack(sent,"ok","SUCCESS",true);
        try { service.receive(sent,ack,x->true,s->{jdbc.update("UPDATE SMT_AUTH_DELETE_REQUEST SET FAILURE_REASON=? WHERE ID=?","callback-write",e.getRequestId());return false;});Assert.fail("业务 CAS 失败应回滚"); } catch(IllegalStateException expected) { }
        Assert.assertEquals(0,eventCount());Assert.assertNull(jdbc.queryForObject("SELECT FAILURE_REASON FROM SMT_AUTH_DELETE_REQUEST WHERE ID=?",String.class,e.getRequestId()));Assert.assertEquals(0L,versions.currentDesired(sent.getResourceId()).getAppliedGeneration());
        Received r=service.receive(sent,ack,x->true,s->{ Assert.assertEquals("row-a",s.getSourceRowId());return true; });
        Assert.assertTrue(r.isSourceConverged());Assert.assertEquals(0,operations.getProgress(batchOf(e)).getUnfinishedCount().intValue());
        Received again=service.receive(sent,ack,x->true,s->{throw new AssertionError("重复事件不能重复执行业务");});
        Assert.assertTrue(again.getReceipt().isDuplicate());Assert.assertEquals(1,eventCount());
    }
    @Test public void platformAbsenceAndOldFailureNeverUseConfirmedTargetAsTrust() {
        Expanded e=expand(accept("absence","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));Binding sent=send(e.getBindings().get(0));
        Received absent=service.receive(sent,ack(sent,"absent","PLATFORM_NOT_FOUND",false),null);
        Assert.assertFalse(absent.getEvidence().isMayApply());Assert.assertNotNull(versions.currentDesired(sent.getResourceId()).getBlockingAttemptId());
        service.receive(sent,ack(sent,"good","SUCCESS",true),x->true,null);
        Received oldFail=service.receive(sent,ack(sent,"late-fail","FAIL",true),s->{throw new AssertionError("旧FAIL不应写业务");});
        Assert.assertFalse(oldFail.getEvidence().isMayApply());Assert.assertEquals(3,eventCount());
    }
    @Test public void crossBatchOwnerWaitsThenReusesAppliedEvidenceWithoutSecondSubmission() {
        Expanded a=expand(accept("owner","ADD",1),"a","ADD",0,1);
        Expanded b=expand(accept("follower","ADD",1),"b","ADD",0,1);service.finish(batchOf(a));service.finish(batchOf(b));
        Assert.assertEquals("REUSE_PENDING",service.reuse(b.getBindings().get(0)).getOutcome());
        Binding sent=send(a.getBindings().get(0));AuthOperationReceiptCommand ownerAck=ack(sent,"owner-ok","SUCCESS",true);
        service.receive(sent,ownerAck,(ConvergenceHandler)null);
        Assert.assertEquals("REUSE_PENDING",service.reuse(b.getBindings().get(0)).getOutcome());
        Assert.assertFalse(service.convergeSource(snapshot(b),s->{throw new AssertionError("共享资源record尚未完成");}));
        Assert.assertFalse(service.refreshTarget(b.getBindings().get(0).getTargetId()));
        service.receive(sent,ownerAck,x->true,s->true);
        Assert.assertEquals("REUSE_APPLIED",service.reuse(b.getBindings().get(0)).getOutcome());
        service.convergeSource(snapshot(b),s->true);service.refreshTarget(b.getBindings().get(0).getTargetId());
        Assert.assertEquals(0,operations.getProgress(batchOf(b)).getUnfinishedCount().intValue());
    }
    @Test public void deletingOneSharedSourceKeepsPhysicalAddAndBothActionsHaveRequests() {
        Expanded a=expand(accept("a-add","ADD",1),"a","ADD",0,1);
        Expanded b=expand(accept("b-add","ADD",1),"b","ADD",0,1);service.finish(batchOf(a));service.finish(batchOf(b));
        Binding sent=send(a.getBindings().get(0));service.receive(sent,ack(sent,"a-ok","SUCCESS",true),x->true,s->true);
        service.reuse(b.getBindings().get(0));service.convergeSource(snapshot(b),s->true);
        Expanded deleted=expand(accept("a-delete","DELETE",1),"a","DELETE",0,1);service.finish(batchOf(deleted));
        Assert.assertEquals("ADD",jdbc.queryForObject("SELECT ACTION FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,deleted.getBindings().get(0).getTargetId()));
        Assert.assertNotNull(deleted.getRequestId());Assert.assertEquals("REUSE_APPLIED",service.reuse(deleted.getBindings().get(0)).getOutcome());
        service.convergeSource(snapshot(deleted),s->true);Assert.assertEquals(3,count("SMT_AUTH_DELETE_REQUEST"));
    }
    @Test public void deferredUnsentFollowerCanConfirmFromCurrentOwnerEvidence() {
        assertDeferredFollowerCanConverge(false);
    }
    @Test public void deferredUnsentFollowerCanSettleButExpiredSideEffectsStillBlock() {
        assertDeferredFollowerCanConverge(true);
    }
    private void assertDeferredFollowerCanConverge(boolean retained) {
        Expanded owner=expand(accept("deferred-owner","ADD",1),"a","ADD",0,1);
        Expanded follower=expand(accept("deferred-follower","ADD",1),"b","ADD",0,1);
        service.finish(batchOf(owner));service.finish(batchOf(follower));Binding sent=send(owner.getBindings().get(0));
        Binding binding=follower.getBindings().get(0);Long target=binding.getTargetId();
        Long attempt=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID=?",Long.class,target);
        // 冻结真实 defer 协议留下的未发送等待状态；此测试不创建 transport phase 或任务。
        jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET STATUS='EXPIRED',ERROR_CODE='WAITING_RESOURCE_OWNER' WHERE ID=?",attempt);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='QUEUED',LEASE_TOKEN=NULL,LEASE_UNTIL=NULL WHERE ID=?",target);
        Assert.assertEquals("BLOCKED",service.reuse(binding).getOutcome());
        service.receive(sent,ack(sent,"deferred-owner-ok","SUCCESS",true),x->true,x->true);
        service.confirmResource(snapshot(follower),binding.getResourceId());Assert.assertTrue(service.convergeSource(snapshot(follower),x->true));
        if(retained) {
            String[][] unsafe={{"TASK_ID","'task'"},{"EXTERNAL_BATCH_ID","'batch'"},{"EXTERNAL_COMMAND_ID","'command'"},
                {"DISPATCHED_AT","SYS_EXTRACT_UTC(SYSTIMESTAMP)"},{"RESULT_EVENT_ID","1"},{"CONFIRMED_AT","SYS_EXTRACT_UTC(SYSTIMESTAMP)"},
                {"CONVERGED_AT","SYS_EXTRACT_UTC(SYSTIMESTAMP)"},{"ERROR_CODE","NULL"},{"ERROR_CODE","'TIMEOUT'"},{"STATUS","'UNKNOWN'"}};
            for(String[] changed:unsafe) {
                jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET "+changed[0]+"="+changed[1]+" WHERE ID=?",attempt);
                Assert.assertEquals(changed[0],0,workflow.confirmReused(target,binding.getResourceGeneration(),LocalDateTime.now()));
                Assert.assertEquals(changed[0],0,workflow.settleRetained(target,"current-evidence",LocalDateTime.now()));
                String restored="ERROR_CODE".equals(changed[0])?"'WAITING_RESOURCE_OWNER'":"STATUS".equals(changed[0])?"'EXPIRED'":"NULL";
                jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET "+changed[0]+"="+restored+" WHERE ID=?",attempt);
            }
        } else {
            Assert.assertEquals("REUSE_APPLIED",service.reuse(binding).getOutcome());
            Assert.assertEquals("CONFIRMED",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,target));
        }
        Assert.assertTrue(service.refreshTarget(target));
        Assert.assertEquals(0,operations.getProgress(batchOf(follower)).getUnfinishedCount().intValue());
        Assert.assertEquals("EXPIRED",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,attempt));
        Assert.assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",Integer.class,park).intValue());
    }
    @Test public void sourceRowReplacementCannotRunOldSnapshotHandler() {
        Expanded e=expand(accept("replace","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));
        Binding sent=send(e.getBindings().get(0));service.receive(sent,ack(sent,"ok","SUCCESS",true),x->true,null);
        SourceSnapshot wrong=snapshot(e).toBuilder().sourceRowId("row-replacement").build();
        try { service.convergeSource(wrong,s->{throw new AssertionError("旧快照不能运行handler");});Assert.fail("不同物理行必须拒绝"); }
        catch(IllegalArgumentException expected) { }
        Assert.assertEquals(1,operations.getProgress(batchOf(e)).getUnfinishedCount().intValue());
    }
    @Test public void lateTrustedAddCreatesIndependentDurableCompensationForTombstone() {
        Expanded add=expand(accept("late-add","ADD",1),"a","ADD",0,1);service.finish(batchOf(add));Binding addSent=send(add.getBindings().get(0));
        service.receive(addSent,ack(addSent,"add-ok","SUCCESS",true),x->true,s->true);
        Expanded del=expand(accept("delete","DELETE",1),"a","DELETE",0,1);service.finish(batchOf(del));Binding delSent=send(del.getBindings().get(0));
        service.receive(delSent,ack(delSent,"del-ok","SUCCESS",true),x->true,s->true);
        Received late=service.receive(addSent,ack(addSent,"late-add-physical","SUCCESS",true),s->{throw new AssertionError("旧证据不能写当前业务");});
        Assert.assertTrue(late.getEvidence().isCompensationRequired());Assert.assertEquals(1,service.pendingRecovery(park,null,200).size());
        Recovery recovery=service.recoverPending(del.getSource().getSourceId(),del.getSource().getGeneration(),del.getBindings().get(0).getResourceId());
        Assert.assertNotNull(recovery.getCompensationBatchId());Assert.assertNotEquals(Long.valueOf(batchOf(del)),recovery.getCompensationBatchId());
        Assert.assertEquals(1,operations.getProgress(batchOf(del)).getExpectedCount().intValue());
        Recovery replay=service.recover(ContributionCommand.builder().sourceId(del.getSource().getSourceId()).sourceGeneration(del.getSource().getGeneration())
            .resource(key("device-1")).requestId(del.getRequestId()).participation("EXCLUDE").build());
        Assert.assertEquals(recovery.getCompensationBatchId(),replay.getCompensationBatchId());
        Binding repair=send(recovery.getBinding());
        service.receive(repair,ack(repair,"repair-ok","SUCCESS",true),x->true,snapshot->true);
        Assert.assertEquals(0,operations.getProgress(recovery.getCompensationBatchId()).getUnfinishedCount().intValue());
    }
    @Test public void untrustedSuccessEventCannotBePromotedByChangingOnlyTrustOnReplay() {
        Expanded e=expand(accept("trust-replay","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));Binding sent=send(e.getBindings().get(0));
        AuthOperationReceiptCommand untrusted=ack(sent,"one-event","SUCCESS",false);
        service.receive(sent,untrusted,null);
        try { service.receive(sent,untrusted.toBuilder().trustedDeviceEvidence(true).build(),x->{throw new AssertionError("不能升级同事件");});Assert.fail("同事件可信类别改变应拒绝"); }
        catch(IllegalArgumentException expected) { }
        Assert.assertEquals(1,eventCount());Assert.assertEquals(0L,versions.currentDesired(sent.getResourceId()).getAppliedGeneration());
    }
    @Test public void disjointWindowsRemainPersistedForVerificationWithoutBroaderTarget() {
        long batch=accept("disjoint","ADD",1,2);Expanded a=stage(batch,"a","ADD",0,1);
        SourceIntent second=intent(batch,"b","ADD").toBuilder().clearWindows()
            .window(Window.builder().from(LocalDateTime.of(2026,11,1,0,0)).to(LocalDateTime.of(2026,11,2,0,0)).build()).build();
        service.stage(Shard.builder().batchId(batch).previousCursor(1).nextCursor(2).source(second).staffAuthId("b").resource(input("device-1")).finalSourcePage(true).build());
        try {service.bindLane(batch,a.getBindings().get(0).getResourceId(),2,3);Assert.fail("不支持的两个窗口不能拉宽");}
        catch(IllegalArgumentException expected) {Assert.assertTrue(expected.getMessage().contains("MULTI_WINDOW_UNSUPPORTED"));}
        Assert.assertEquals(2,count("SMT_AUTH_DELETE_REQUEST"));Assert.assertEquals(0,count("SMT_AUTH_OPERATION_TARGET"));Assert.assertTrue(service.pendingRecovery(park,null,200).isEmpty());
        Assert.assertEquals(1,operations.getProgress(batch).getUnfinishedCount().intValue());
    }
    @Test public void moreThan200ResourcesConvergeThroughBoundedPagesAndOneBusinessCallback() {
        long started=System.nanoTime();long batch=accept("large-source","ADD",201);SourceIntent source=intent(batch,"a","ADD");
        Shard.ShardBuilder first=Shard.builder().batchId(batch).source(source).staffAuthId("a").previousCursor(0).nextCursor(1).finalSourcePage(false);
        for(int n=0;n<200;n++)first.resource(input("device-"+n));
        Expanded a=service.stage(first.build());Expanded last=service.stage(Shard.builder().batchId(batch).source(source).staffAuthId("a").previousCursor(1).nextCursor(2)
            .finalSourcePage(true).resource(input("device-200")).build());
        long stageDone=System.nanoTime();List<Binding> all=new ArrayList<>();List<Binding> staged=new ArrayList<>(a.getBindings());staged.addAll(last.getBindings());long cursor=2;
        for(Binding b:staged)all.addAll(service.bindLane(batch,b.getResourceId(),cursor,++cursor));service.finish(batch);long bindDone=System.nanoTime();
        List<String> firstPage=versions.pendingSourceResources(a.getSource().getSourceId(),a.getSource().getGeneration(),null,200);
        Assert.assertEquals(200,firstPage.size());Assert.assertEquals(1,versions.pendingSourceResources(a.getSource().getSourceId(),a.getSource().getGeneration(),firstPage.get(199),200).size());
        Map<Long,Binding> byTarget=new HashMap<>();for(Binding b:all)byTarget.put(b.getTargetId(),b);
        final int[] callbacks={0};int received=0;long claimNs=0,prepareNs=0,receiveNs=0;
        while(received<201) {
            long claimStarted=System.nanoTime();List<AuthOperationClaimedTarget> claimed=service.claim(AuthOperationClaimCommand.builder().parkId(park).operationQueue("AUTH").maxCount(200).leaseSeconds(1800L).build());
            claimNs+=System.nanoTime()-claimStarted;Assert.assertFalse(claimed.isEmpty());
            for(AuthOperationClaimedTarget c:claimed) {
                Binding b=byTarget.get(c.getTargetId()).toBuilder().attemptId(c.getAttemptId()).build();
                AuthOperationSubmissionCommand command=AuthOperationSubmissionCommand.builder().targetId(c.getTargetId()).attemptId(c.getAttemptId()).attemptNo(c.getAttemptNo())
                    .leaseToken(c.getLeaseToken()).accessType("DIRECT").taskId("task-"+c.getAttemptId()).build();
                long prepareStarted=System.nanoTime();Assert.assertEquals("READY",service.prepare(b,command).getOutcome());service.submitted(command.toBuilder().externalCommandId("external-"+c.getAttemptId()).build());
                prepareNs+=System.nanoTime()-prepareStarted;long receiveStarted=System.nanoTime();service.receive(b,ack(b,"ack-"+c.getAttemptId(),"SUCCESS",true),x->true,snapshot->{callbacks[0]++;return true;});receiveNs+=System.nanoTime()-receiveStarted;received++;
                Assert.assertEquals(received==201?1:0,callbacks[0]);
            }
        }
        long settlementStarted=System.nanoTime();List<Long> targets=service.sourceTargets(snapshot(a),null,200);Assert.assertEquals(200,targets.size());
        List<Long> tail=service.sourceTargets(snapshot(a),targets.get(199),200);Assert.assertEquals(1,tail.size());
        for(Long t:targets)service.refreshTarget(t);for(Long t:tail)service.refreshTarget(t);
        Assert.assertEquals(0,operations.getProgress(batch).getUnfinishedCount().intValue());
        System.out.println("WORKFLOW_CAPACITY resources=201 stageMs="+(stageDone-started)/1000000+" bindMs="+(bindDone-stageDone)/1000000
            +" claimMs="+claimNs/1000000+" prepareAndSubmissionMs="+prepareNs/1000000+" receiveAndSourceMs="+receiveNs/1000000
            +" targetSettlementMs="+(System.nanoTime()-settlementStarted)/1000000);
    }
    @Test public void unsentOldLogicalTargetSettlesOnlyAfterCurrentAppliedEvidenceAndBusinessConvergence() {
        Expanded old=expand(accept("old-unsent","ADD",1),"a","ADD",0,1);service.finish(batchOf(old));
        long batch=accept("new-window","ADD",1);
        SourceIntent changed=intent(batch,"b","ADD").toBuilder().clearWindows()
            .window(Window.builder().from(LocalDateTime.of(2026,9,1,0,0)).to(LocalDateTime.of(2026,10,10,0,0)).build()).build();
        Expanded staged=service.stage(Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(changed).staffAuthId("b")
            .resource(input("device-1")).finalSourcePage(true).build());
        Binding current=service.bindLane(batch,staged.getBindings().get(0).getResourceId(),1,2).get(0);service.finish(batch);
        Binding sent=send(current);service.receive(sent,ack(sent,"new-applied","SUCCESS",true),x->true,x->true);
        Recovery recovery=service.recoverPending(old.getSource().getSourceId(),old.getSource().getGeneration(),old.getBindings().get(0).getResourceId());
        Assert.assertEquals("REUSE_APPLIED",service.reuse(recovery.getBinding()).getOutcome());
        Assert.assertFalse(service.refreshTarget(old.getBindings().get(0).getTargetId()));
        service.convergeSource(snapshot(old),x->true);
        Assert.assertTrue(service.refreshTarget(old.getBindings().get(0).getTargetId()));
        Assert.assertTrue(service.refreshTarget(recovery.getBinding().getTargetId()));
        Assert.assertEquals(0,operations.getProgress(batchOf(old)).getUnfinishedCount().intValue());
        String summary=jdbc.queryForObject("SELECT RESULT_SUMMARY FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,old.getBindings().get(0).getTargetId());
        Assert.assertTrue(summary.startsWith("RETAINED_BY_CURRENT_EVIDENCE;"));Assert.assertTrue(summary.contains(";event="));
        Assert.assertEquals(1,eventCount());
        Assert.assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID=? AND STATUS!='CLAIMED'",Integer.class,old.getBindings().get(0).getTargetId()).intValue());
    }
    @Test public void acceptanceRollsBackWithOuterFrozenSelectionTransaction() {
        try {outer.execute(status->{
            service.acceptWithinTransaction(Selection.builder().parkId(park).idempotencyKey("outer-rollback").action("ADD").sourceType("1")
                .snapshot("server-selection").sourceCount(1).expectedCount(1).build());
            throw new IllegalStateException("模拟冻结选择写入失败");
        });Assert.fail("外层应失败");}catch(IllegalStateException expected) { }
        Assert.assertEquals(0,count("SMT_AUTH_OPERATION_BATCH"));
    }
    @Test public void deleteBusinessBatchCanFreezeRetainedAddSourceAlongsideDeleteSource() {
        long batch=accept("mixed-sources","DELETE",1,2);Expanded removed=stage(batch,"a","DELETE",0,1);
        Expanded retained=stage(batch,"b","ADD",1,2);
        List<Binding> bindings=service.bindLane(batch,removed.getBindings().get(0).getResourceId(),2,3);service.finish(batch);
        Assert.assertEquals(2,bindings.size());Assert.assertEquals(1,count("SMT_AUTH_OPERATION_TARGET"));
        Assert.assertEquals("ADD",jdbc.queryForObject("SELECT ACTION FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,bindings.get(0).getTargetId()));
        Binding sent=send(bindings.get(0));service.receive(sent,ack(sent,"mixed-ok","SUCCESS",true),x->true,null);
        service.convergeSource(snapshot(removed),x->true);service.convergeSource(snapshot(retained),x->true);
        Assert.assertTrue(service.refreshTarget(sent.getTargetId()));
        Assert.assertEquals(0,operations.getProgress(batch).getUnfinishedCount().intValue());
    }
    @Test public void perTargetRecordCallbackFailureRollsBackEventAndVersionBeforeFinalSource() {
        Expanded e=expand(accept("target-callback","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));Binding sent=send(e.getBindings().get(0));
        AuthOperationReceiptCommand event=ack(sent,"record-ok","SUCCESS",true);
        try {service.receive(sent,event.toBuilder().eventNamespace(null).build(),null);Assert.fail("事件必须有真实命名空间");}
        catch(IllegalArgumentException expected) { }
        Assert.assertEquals(0,eventCount());
        try {service.receiveWithinTransaction(sent,event,current->{
            jdbc.update("UPDATE SMT_AUTH_DELETE_REQUEST SET FAILURE_REASON=? WHERE ID=?","record-write",e.getRequestId());return false;
        },null);Assert.fail("目标record CAS失败应回滚整条事件");}catch(IllegalStateException expected) { }
        Assert.assertEquals(0,eventCount());Assert.assertEquals(0L,versions.currentDesired(sent.getResourceId()).getAppliedGeneration());
        Assert.assertNull(jdbc.queryForObject("SELECT FAILURE_REASON FROM SMT_AUTH_DELETE_REQUEST WHERE ID=?",String.class,e.getRequestId()));
        final int[] writes={0};service.receive(sent,event,current->{writes[0]++;Assert.assertEquals("task-"+sent.getAttemptId(),current.getTaskId());return true;},null);
        service.receive(sent,event,current->{throw new AssertionError("同事件不能重复写record");},null);
        service.receive(sent,ack(sent,"old-fail","FAIL",true),current->{throw new AssertionError("旧FAIL不能写当前record");},null);
        Assert.assertEquals(1,writes[0]);Assert.assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID=? AND CONVERGED='Y'",Integer.class,sent.getTargetId()).intValue());
    }
    @Test public void requiredReceiptJoinsLegacyOuterRollback() {
        Expanded e=expand(accept("outer-receipt","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));Binding sent=send(e.getBindings().get(0));
        try {outer.execute(status->{service.receiveWithinTransaction(sent,ack(sent,"ack","SUCCESS",true),x->true,null);
            throw new IllegalStateException("legacy写失败");});Assert.fail("外层应回滚");}catch(IllegalStateException expected) { }
        Assert.assertEquals(0,eventCount());Assert.assertEquals(0L,versions.currentDesired(sent.getResourceId()).getAppliedGeneration());
    }
    @Test public void requiredSubmittedRollsBackWithLegacyExternalIdentifierWrite() {
        Expanded e=expand(accept("outer-submitted","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));
        AuthOperationClaimedTarget c=service.claim(AuthOperationClaimCommand.builder().parkId(park).operationQueue("AUTH").maxCount(1).leaseSeconds(300L).build()).get(0);
        Binding bound=e.getBindings().get(0).toBuilder().attemptId(c.getAttemptId()).build();
        AuthOperationSubmissionCommand command=AuthOperationSubmissionCommand.builder().targetId(c.getTargetId()).attemptId(c.getAttemptId()).attemptNo(c.getAttemptNo())
            .leaseToken(c.getLeaseToken()).accessType("DIRECT").taskId("local-task").externalCommandId("real-device-command").build();
        Assert.assertEquals("READY",service.prepare(bound,command.toBuilder().externalCommandId(null).build()).getOutcome());
        try {outer.execute(status->{jdbc.update("UPDATE SMT_AUTH_DELETE_REQUEST SET FAILURE_REASON=? WHERE ID=?","legacy-external-id",e.getRequestId());
            service.submittedWithinTransaction(command);throw new IllegalStateException("legacy downloadId CAS failed");});Assert.fail("受理号必须随本库写入回滚");}
        catch(IllegalStateException expected) { }
        Assert.assertNull(jdbc.queryForObject("SELECT FAILURE_REASON FROM SMT_AUTH_DELETE_REQUEST WHERE ID=?",String.class,e.getRequestId()));
        Assert.assertEquals("SUBMITTING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
        Assert.assertNull(jdbc.queryForObject("SELECT EXTERNAL_COMMAND_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
    }
    @Test public void requiredPrepareRollsBackWithLocalLegacyTaskCreationAndClaimCannotJoinOuterTransaction() {
        Expanded e=expand(accept("outer-prepare","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));
        AuthOperationClaimCommand claim=AuthOperationClaimCommand.builder().parkId(park).operationQueue("AUTH").maxCount(1).leaseSeconds(300L).build();
        try {outer.execute(status->service.claim(claim));Assert.fail("claim不能持外层事务等待主体锁");}
        catch(org.springframework.transaction.IllegalTransactionStateException expected) { }
        AuthOperationClaimedTarget c=service.claim(claim).get(0);Binding bound=e.getBindings().get(0).toBuilder().attemptId(c.getAttemptId()).build();
        AuthOperationSubmissionCommand command=AuthOperationSubmissionCommand.builder().targetId(c.getTargetId()).attemptId(c.getAttemptId()).attemptNo(c.getAttemptNo())
            .leaseToken(c.getLeaseToken()).accessType("DIRECT").taskId("new-local-task").build();
        try {outer.execute(status->{jdbc.update("UPDATE SMT_AUTH_DELETE_REQUEST SET FAILURE_REASON=? WHERE ID=?","legacy-create",e.getRequestId());
            Assert.assertEquals("READY",service.prepareWithinTransaction(bound,command).getOutcome());throw new IllegalStateException("legacy insert failed");});Assert.fail("应整体回滚");}
        catch(IllegalStateException expected) { }
        Assert.assertNull(jdbc.queryForObject("SELECT FAILURE_REASON FROM SMT_AUTH_DELETE_REQUEST WHERE ID=?",String.class,e.getRequestId()));
        Assert.assertEquals("CLAIMED",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
        Assert.assertNull(versions.currentDesired(bound.getResourceId()).getBlockingAttemptId());
    }
    @Test public void missingLocalRecordBlocksEveryCompletionPathUntilSameEventCallbackReplay() {
        Expanded e=expand(accept("record-pending","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));Binding sent=send(e.getBindings().get(0));
        AuthOperationReceiptCommand event=ack(sent,"pending-record","SUCCESS",true);final int[] writes={0};
        Assert.assertFalse(service.receive(sent,event,x->{writes[0]++;return true;}).isSourceConverged());
        Assert.assertEquals(0,writes[0]);service.confirmResource(snapshot(e),sent.getResourceId());service.reuse(sent);
        Assert.assertFalse(service.convergeSource(snapshot(e),x->{writes[0]++;return true;}));
        Assert.assertFalse(service.refreshTarget(sent.getTargetId()));Assert.assertEquals(0,writes[0]);
        Assert.assertEquals(1,operations.getProgress(batchOf(e)).getUnfinishedCount().intValue());
        Assert.assertTrue(service.receive(sent,event,x->true,x->{writes[0]++;return true;}).isSourceConverged());
        Assert.assertEquals(1,writes[0]);Assert.assertEquals(0,operations.getProgress(batchOf(e)).getUnfinishedCount().intValue());
    }
    @Test public void firstResourceWithoutRecordCannotConvergeWhenLastResourceHasRecord() {
        long batch=accept("two-records","ADD",2);Expanded e=service.stage(Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(intent(batch,"a","ADD"))
            .staffAuthId("a").finalSourcePage(true).resource(input("device-1")).resource(input("device-2")).build());
        List<Binding> bindings=new ArrayList<>();long cursor=1;for(Binding b:e.getBindings())bindings.addAll(service.bindLane(batch,b.getResourceId(),cursor,++cursor));service.finish(batch);
        Map<Long,Binding> byTarget=new HashMap<>();for(Binding b:bindings)byTarget.put(b.getTargetId(),b);List<Binding> sent=new ArrayList<>();
        for(AuthOperationClaimedTarget c:service.claim(AuthOperationClaimCommand.builder().parkId(park).operationQueue("AUTH").maxCount(2).leaseSeconds(300L).build())) {
            Binding b=byTarget.get(c.getTargetId()).toBuilder().attemptId(c.getAttemptId()).build();
            AuthOperationSubmissionCommand command=AuthOperationSubmissionCommand.builder().targetId(c.getTargetId()).attemptId(c.getAttemptId()).attemptNo(c.getAttemptNo())
                .leaseToken(c.getLeaseToken()).accessType("DIRECT").taskId("task-"+c.getAttemptId()).build();
            service.prepare(b,command);service.submitted(command.toBuilder().externalCommandId("external-"+c.getAttemptId()).build());sent.add(b);
        }
        Assert.assertEquals(2,sent.size());final int[] sourceWrites={0};ConvergenceHandler source=x->{sourceWrites[0]++;return true;};
        AuthOperationReceiptCommand first=ack(sent.get(0),"first","SUCCESS",true);service.receive(sent.get(0),first,source);
        Assert.assertFalse(service.receive(sent.get(1),ack(sent.get(1),"last","SUCCESS",true),x->true,source).isSourceConverged());
        Assert.assertEquals(0,sourceWrites[0]);Assert.assertFalse(service.convergeSource(snapshot(e),source));
        Assert.assertTrue(service.receive(sent.get(0),first,x->true,source).isSourceConverged());
        for(Binding b:sent)service.refreshTarget(b.getTargetId());Assert.assertEquals(1,sourceWrites[0]);Assert.assertEquals(0,operations.getProgress(batch).getUnfinishedCount().intValue());
    }
    @Test public void consumedStageCursorRejectsDifferentResourceWithoutPersistingIt() {
        long batch=accept("changed-page","ADD",2);Shard page=Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(intent(batch,"a","ADD"))
            .staffAuthId("a").resource(input("device-1")).build();service.stage(page);
        try {service.stage(page.toBuilder().clearResources().resource(input("device-2")).build());Assert.fail("相同游标不同资源必须冲突");}
        catch(IllegalArgumentException expected) { }
        Assert.assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)",Integer.class,park).intValue());
        Assert.assertEquals(0,jdbc.queryForObject("SELECT EXPANDED FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",Integer.class,park).intValue());
        Assert.assertEquals(1L,jdbc.queryForObject("SELECT EXPANSION_CURSOR FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",Long.class,batch).longValue());
    }
    @Test public void consumedStageCursorRejectsChangedFinalFlagWithoutSealingSource() {
        long batch=accept("changed-final","ADD",1);Shard page=Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(intent(batch,"a","ADD"))
            .staffAuthId("a").resource(input("device-1")).build();service.stage(page);
        try {service.stage(page.toBuilder().finalSourcePage(true).build());Assert.fail("相同游标不能改变封口标志");}
        catch(IllegalArgumentException expected) { }
        Assert.assertEquals(0,jdbc.queryForObject("SELECT EXPANDED FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",Integer.class,park).intValue());
    }
    @Test public void canonicalPageReplayAcceptsResourceOrderButRejectsWindowParticipationAndCursorChanges() {
        long batch=accept("canonical-page","ADD",2);Shard page=Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(intent(batch,"a","ADD"))
            .staffAuthId("a").resource(input("device-1")).resource(input("device-2")).build();Expanded first=service.stage(page);
        Expanded replay=service.stage(page.toBuilder().clearResources().resource(input("device-2")).resource(input("device-1")).build());
        Assert.assertEquals(first.getRequestId(),replay.getRequestId());Assert.assertEquals(2,replay.getBindings().size());
        for(Shard changed:Arrays.asList(page.toBuilder().nextCursor(2).build(),
            page.toBuilder().clearResources().resource(input("device-1").toBuilder().participation("EXCLUDE").build()).resource(input("device-2")).build(),
            page.toBuilder().clearResources().resource(input("device-1").toBuilder().window(Window.builder().from(LocalDateTime.of(2026,9,2,0,0)).to(LocalDateTime.of(2026,9,20,0,0)).build()).build()).resource(input("device-2")).build())) {
            try {service.stage(changed);Assert.fail("同页规范内容变化应拒绝");}catch(IllegalArgumentException expected) { }
        }
        Assert.assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID=?",Integer.class,batch).intValue());
        Assert.assertEquals(0,jdbc.queryForObject("SELECT EXPANDED FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",Integer.class,park).intValue());
    }
    @Test public void distinctTrustedAckEventsReuseExactCompletedRecordWithoutRepeatingEitherCallback() {
        Expanded e=expand(accept("duplicate-physical-ack","ADD",1),"a","ADD",0,1);service.finish(batchOf(e));Binding sent=send(e.getBindings().get(0));
        final int[] recordWrites={0},sourceWrites={0};AuthOperationReceiptCommand first=ack(sent,"ack-first","SUCCESS",true).toBuilder().evidenceBody("first raw ACK").build();
        Received firstResult=service.receive(sent,first,x->{recordWrites[0]++;return true;},x->{sourceWrites[0]++;return true;});Assert.assertTrue(firstResult.isSourceConverged());
        Assert.assertNull(workflow.completedRecordEvent(sent,firstResult.getReceipt().getEventId())); // 当前事件不能自证继承。
        Assert.assertEquals(firstResult.getReceipt().getEventId(),workflow.completedRecordEvent(sent,Long.MAX_VALUE));
        Assert.assertNull(workflow.completedRecordEvent(sent.toBuilder().targetId(sent.getTargetId()+1).build(),Long.MAX_VALUE));
        Assert.assertNull(workflow.completedRecordEvent(sent.toBuilder().attemptId(sent.getAttemptId()+1).build(),Long.MAX_VALUE));
        Assert.assertNull(workflow.completedRecordEvent(sent.toBuilder().resourceGeneration(sent.getResourceGeneration()+1).build(),Long.MAX_VALUE));
        Assert.assertNull(workflow.completedRecordEvent(sent.toBuilder().resourceId("another-resource").build(),Long.MAX_VALUE));
        TargetEvidenceHandler noSecondRecord=x->{recordWrites[0]++;return false;};ConvergenceHandler noSecondSource=x->{sourceWrites[0]++;return false;};
        service.receive(sent,first,noSecondRecord,noSecondSource);
        AuthOperationReceiptCommand second=first.toBuilder().eventKey("ack-second").evidenceBody("second raw ACK").build();
        Received next=service.receive(sent,second,noSecondRecord,noSecondSource);service.receive(sent,second,noSecondRecord,noSecondSource);
        Assert.assertTrue(next.isSourceConverged());Assert.assertEquals(1,recordWrites[0]);Assert.assertEquals(1,sourceWrites[0]);Assert.assertEquals(2,eventCount());
        Assert.assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID=? AND CONVERGED='Y'",Integer.class,sent.getTargetId()).intValue());
        Assert.assertEquals("second raw ACK",jdbc.queryForObject("SELECT EVIDENCE_BODY FROM SMT_AUTH_RESULT_EVENT WHERE ID=?",String.class,next.getReceipt().getEventId()));
        service.receive(sent,ack(sent,"late-failure","FAIL",true),noSecondRecord,noSecondSource);
        Assert.assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID=? AND CONVERGED='N'",Integer.class,sent.getTargetId()).intValue());
        Assert.assertEquals(0,operations.getProgress(batchOf(e)).getUnfinishedCount().intValue());
    }
    @Test public void anotherAttemptAndVersionCannotReuseEarlierRecordCompletion() {
        Expanded add=expand(accept("record-generation-add","ADD",1),"a","ADD",0,1);service.finish(batchOf(add));Binding first=send(add.getBindings().get(0));
        service.receive(first,ack(first,"add-complete","SUCCESS",true),x->true,x->true);
        Expanded removed=expand(accept("record-generation-delete","DELETE",1),"a","DELETE",0,1);service.finish(batchOf(removed));Binding second=send(removed.getBindings().get(0));
        Assert.assertNotEquals(first.getAttemptId(),second.getAttemptId());Assert.assertNotEquals(first.getResourceGeneration(),second.getResourceGeneration());
        AuthOperationReceiptCommand next=ack(second,"new-version","SUCCESS",true);final int[] writes={0};
        try {service.receive(second,next,x->{writes[0]++;return false;},x->{throw new AssertionError("新代record失败不能删源");});Assert.fail("新代必须执行record CAS");}
        catch(IllegalStateException expected) { }
        Assert.assertEquals(1,writes[0]);Assert.assertEquals(1,eventCount());
        Assert.assertTrue(service.receive(second,next,x->true,x->true).isSourceConverged());
    }
    @Test public void recoveryInterleavingWithLastSharedSourceDeleteDoesNotStealCurrentBusinessOwner() {
        assertRecoveryRespectsLastSharedSourceDelete(false);
    }
    @Test public void recoveryInterleavingWithNullRowOrphanDeleteDoesNotStealCurrentBusinessOwner() {
        assertRecoveryRespectsLastSharedSourceDelete(true);
    }
    private void assertRecoveryRespectsLastSharedSourceDelete(boolean orphan) {
        long firstBatch=accept("shared-first-delete","DELETE",1,2);Expanded removed=stage(firstBatch,"a","DELETE",0,1);Expanded retained=stage(firstBatch,"b","ADD",1,2);
        List<Binding> shared=service.bindLane(firstBatch,removed.getBindings().get(0).getResourceId(),2,3);service.finish(firstBatch);
        Binding first=send(shared.get(0));service.receive(first,ack(first,"shared-add","SUCCESS",true),x->true,null);
        service.convergeSource(snapshot(removed),x->true);service.convergeSource(snapshot(retained),x->true);service.refreshTarget(first.getTargetId());
        long lastBatch=service.accept(Selection.builder().parkId(park).idempotencyKey("shared-last-delete").action("DELETE")
            .sourceType(orphan?"7":"1").snapshot("server-projection:shared-last-delete").expectedCount(1).sourceCount(1).build()).getBatchId();
        SourceIntent lastIntent=intent(lastBatch,"b","DELETE");
        if(orphan)lastIntent=lastIntent.toBuilder().sourceRowId(null).build();
        Expanded last=service.stage(Shard.builder().batchId(lastBatch).previousCursor(0).nextCursor(1).source(lastIntent)
            .staffAuthId("b").resource(input("device-1")).finalSourcePage(true).build());String resource=first.getResourceId();
        if(orphan)assertNullRowRequiresBothSides(last);
        Assert.assertEquals("CURRENT_INTENT_PENDING",service.recoverPending(removed.getSource().getSourceId(),removed.getSource().getGeneration(),resource).getOutcome());
        Assert.assertEquals(2,count("SMT_AUTH_OPERATION_BATCH"));Assert.assertEquals(1,count("SMT_AUTH_OPERATION_TARGET"));
        Binding finalTarget=service.bindLane(lastBatch,resource,1,2).get(0);service.finish(lastBatch);
        Assert.assertEquals("CURRENT_INTENT_PENDING",service.recover(ContributionCommand.builder().sourceId(removed.getSource().getSourceId())
            .sourceGeneration(removed.getSource().getGeneration()).resource(key("device-1")).requestId(removed.getRequestId()).participation("EXCLUDE").build()).getOutcome());
        Assert.assertEquals(2,count("SMT_AUTH_OPERATION_BATCH"));Assert.assertEquals(2,count("SMT_AUTH_OPERATION_TARGET"));
        Binding sent=send(finalTarget);service.receive(sent,ack(sent,"last-delete","SUCCESS",true),x->true,x->true);
        Recovery applied=service.recoverPending(removed.getSource().getSourceId(),removed.getSource().getGeneration(),resource);
        Assert.assertEquals("REUSE_APPLIED",applied.getOutcome());Assert.assertNull(applied.getCompensationBatchId());
        Assert.assertEquals(2,count("SMT_AUTH_OPERATION_BATCH"));Assert.assertEquals(0,operations.getProgress(lastBatch).getUnfinishedCount().intValue());
        Received stale=service.receive(first,ack(first,"late-shared-add","SUCCESS",true),x->{throw new AssertionError("迟到旧ADD不能写record");},null);
        Assert.assertTrue(stale.getEvidence().isCompensationRequired());
        Recovery repair=service.recoverPending(removed.getSource().getSourceId(),removed.getSource().getGeneration(),resource);
        Assert.assertNotNull(repair.getCompensationBatchId());Assert.assertEquals(3,count("SMT_AUTH_OPERATION_BATCH"));
        Binding repairSent=send(repair.getBinding());service.receive(repairSent,ack(repairSent,"repair-delete","SUCCESS",true),x->true,x->true);
        Assert.assertEquals(0,operations.getProgress(repair.getCompensationBatchId()).getUnfinishedCount().intValue());
    }
    private void assertNullRowRequiresBothSides(Expanded staged) {
        Binding binding=staged.getBindings().get(0);String sourceId=staged.getSource().getSourceId();
        Assert.assertNull(staged.getSource().getSourceRowId());
        // 仅改变自有合成来源的行号快照，分别证明任一单侧为空都不能冒充当前同一来源。
        Assert.assertEquals(1,jdbc.update("UPDATE SMT_AUTH_SOURCE_RESOURCE SET SOURCE_ROW_ID='one-sided-row' WHERE SOURCE_COORD_ID=? AND SOURCE_GENERATION=? AND BINDING_REVISION=0",
            sourceId,staged.getSource().getGeneration()));
        Assert.assertEquals(0,workflow.currentIntentCount(binding.getResourceId(),binding.getResourceGeneration()));
        jdbc.update("UPDATE SMT_AUTH_SOURCE_RESOURCE SET SOURCE_ROW_ID=NULL WHERE SOURCE_COORD_ID=? AND SOURCE_GENERATION=? AND BINDING_REVISION=0",
            sourceId,staged.getSource().getGeneration());
        Assert.assertEquals(1,jdbc.update("UPDATE SMT_AUTH_SOURCE_COORD SET SOURCE_ROW_ID='one-sided-row' WHERE ID=? AND PARK_ID=?",sourceId,park));
        Assert.assertEquals(0,workflow.currentIntentCount(binding.getResourceId(),binding.getResourceGeneration()));
        jdbc.update("UPDATE SMT_AUTH_SOURCE_COORD SET SOURCE_ROW_ID=NULL WHERE ID=? AND PARK_ID=?",sourceId,park);
    }
    @Test public void appliedCurrentRecordDoesNotAbandonOldPendingSourceRequest() {
        long initial=accept("pending-shared","DELETE",1,2);Expanded removed=stage(initial,"a","DELETE",0,1);Expanded retained=stage(initial,"b","ADD",1,2);
        Binding shared=service.bindLane(initial,removed.getBindings().get(0).getResourceId(),2,3).get(0);service.finish(initial);
        Binding first=send(shared);service.receive(first,ack(first,"pending-shared-add","SUCCESS",true),x->true,null);
        service.convergeSource(snapshot(retained),x->true);
        Expanded last=expand(accept("pending-last-delete","DELETE",1),"b","DELETE",0,1);service.finish(batchOf(last));
        Binding sent=send(last.getBindings().get(0));service.receive(sent,ack(sent,"pending-last-ok","SUCCESS",true),x->true,x->true);
        Recovery recovery=service.recoverPending(removed.getSource().getSourceId(),removed.getSource().getGeneration(),shared.getResourceId());
        Assert.assertEquals("REUSE_APPLIED",recovery.getOutcome());Assert.assertNotNull("未完成旧请求仍需持久的当前版本收敛目标",recovery.getCompensationBatchId());
        Assert.assertEquals("REUSE_APPLIED",service.reuse(recovery.getBinding()).getOutcome());
        Assert.assertTrue(service.convergeSource(snapshot(removed),x->true));service.refreshTarget(recovery.getBinding().getTargetId());
        Assert.assertEquals(0,operations.getProgress(recovery.getCompensationBatchId()).getUnfinishedCount().intValue());
    }
    private long accept(String id,String action,int count) { return accept(id,action,count,1); }
    private long accept(String id,String action,int count,int sources) { return service.accept(Selection.builder().parkId(park).idempotencyKey(id).action(action)
        .sourceType("1").snapshot("server-projection:"+id).expectedCount(count).sourceCount(sources).build()).getBatchId(); }
    private SourceIntent intent(long batch,String name,String action) { SourceIntent.SourceIntentBuilder b=SourceIntent.builder().parkId(park).batchId(batch)
        .sourceKind("STAFF_AUTH").stableKey(AuthWorkflow.staffStableKey("staff",name)).subjectType("STAFF").subjectId("staff").sourceRowId("row-"+name)
        .sourceFingerprint("fp-"+name+"-"+action).intentKey("intent-"+batch+"-"+name).action(action).payloadSnapshot("frozen-"+name);
        if("ADD".equals(action))b.window(Window.builder().from(LocalDateTime.of(2026,9,1,0,0)).to(LocalDateTime.of(2026,9,30,0,0)).build());return b.build(); }
    private ResourceKey key(String device) { return ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId("staff").accessType("DIRECT")
        .deviceId(device).resourceType("PERMISSION").resourceId("door").serviceType("ACCESS").credentialChannel("CARD").build(); }
    private ResourceInput input(String device) { return ResourceInput.builder().resource(key(device)).build(); }
    private Expanded stage(long batch,String name,String action,long before,long after) { return service.stage(Shard.builder().batchId(batch)
        .previousCursor(before).nextCursor(after).source(intent(batch,name,action)).staffAuthId(name).resource(input("device-1")).finalSourcePage(true).build()); }
    private Expanded expand(long batch,String name,String action,long before,long after) {
        Expanded staged=stage(batch,name,action,before,after);
        List<Binding> bound=service.bindLane(batch,staged.getBindings().get(0).getResourceId(),after,after+1);
        return Expanded.builder().source(staged.getSource()).requestId(staged.getRequestId()).expansion(staged.getExpansion()).bindings(bound).build();
    }
    private Binding send(Binding b) {
        List<AuthOperationClaimedTarget> list=service.claim(AuthOperationClaimCommand.builder().parkId(park).operationQueue("AUTH").maxCount(200).leaseSeconds(300L).build());
        AuthOperationClaimedTarget c=list.stream().filter(x->x.getTargetId().equals(b.getTargetId())).findFirst().orElseThrow(()->new AssertionError("目标未领到"));
        Binding bound=b.toBuilder().attemptId(c.getAttemptId()).build();
        AuthOperationSubmissionCommand command=AuthOperationSubmissionCommand.builder().targetId(c.getTargetId()).attemptId(c.getAttemptId())
            .attemptNo(c.getAttemptNo()).leaseToken(c.getLeaseToken()).accessType(c.getAccessType()).taskId("task-"+c.getAttemptId()).build();
        Assert.assertEquals("READY",service.prepare(bound,command).getOutcome());service.submitted(command.toBuilder().externalCommandId("external-"+c.getAttemptId()).build());return bound;
    }
    private AuthOperationReceiptCommand ack(Binding b,String event,String result,boolean trusted) {
        Map<String,Object> a=jdbc.queryForMap("SELECT ATTEMPT_NO,LEASE_TOKEN FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",b.getAttemptId());
        return AuthOperationReceiptCommand.builder().targetId(b.getTargetId()).attemptId(b.getAttemptId()).attemptNo(((Number)a.get("ATTEMPT_NO")).intValue())
            .leaseToken((String)a.get("LEASE_TOKEN")).accessType("DIRECT").externalCommandId("external-"+b.getAttemptId()).operationVersion(b.getResourceGeneration())
            .eventNamespace("direct").eventKey(event).evidenceType("DEVICE_ACK").resultStatus(result).trustedDeviceEvidence(trusted).build(); }
    private SourceSnapshot snapshot(Expanded e) { return SourceSnapshot.builder().sourceId(e.getSource().getSourceId()).generation(e.getSource().getGeneration())
        .sourceRowId(e.getSource().getSourceRowId()).fingerprint(e.getSource().getSourceFingerprint()).build(); }
    private long batchOf(Expanded e) { return jdbc.queryForObject("SELECT BATCH_ID FROM SMT_AUTH_DELETE_REQUEST WHERE ID=?",Long.class,e.getRequestId()); }
    private int count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE PARK_ID=?",Integer.class,park); }
    private int eventCount() { return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",Integer.class,park); }
}
