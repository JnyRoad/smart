package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.AuthOperationDirectTakeoverMapper;
import org.junit.*;
import org.mockito.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.*;
import org.springframework.transaction.TransactionDefinition;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 独立手写语义期望，事务管理器仅模拟持久介质，不连接数据库。 */
public class AuthOperationDirectTakeoverServiceTest {
    AuthOperationDirectTakeoverMapper mapper; AuthOperationDirectTakeoverService service; MemoryTransactions tx; SmtDeviceTask task; RouteCapability route;
    @Before public void before() {
        mapper=mock(AuthOperationDirectTakeoverMapper.class);tx=new MemoryTransactions();service=new AuthOperationDirectTakeoverService(mapper,tx);
        task=new SmtDeviceTask();task.setId(7);task.setSerialNo("serial-7");task.setDeviceCode("device-a");task.setCardNo("42");
        task.setAction(1);task.setDeviceType(1);task.setServiceType(7);task.setCardType(1);task.setStatus(0);task.setGeneral("合成名称");task.setStartTime(10L);task.setOverTime(20L);
        route=new RouteCapability();route.setParkId(10);route.setInstanceId("owner");route.setDirectTakeoverVersion(0);
        when(mapper.task(7)).thenReturn(task);when(mapper.taskPhases("7")).thenReturn(Collections.emptyList());
        when(mapper.deviceParks("device-a")).thenReturn(Collections.singletonList(10));when(mapper.route(10)).thenReturn(route);
        when(mapper.review(anyString(),any(),anyString(),any(),any(),anyString())).thenAnswer(a->{tx.current.get().pending.add(a.getArgument(0));return 1;});
    }
    @Test public void knownZeroAllowsButCannotBegin() {
        assertEquals(Outcome.LEGACY_ALLOWED,service.admitLegacyDirect(7,LegacyIdentity.of(task)).getOutcome());
        try{service.assertDirectSendEnabled(10,"owner");fail();}catch(IllegalStateException expected){}
        assertTrue(tx.durable.isEmpty());
    }
    @Test public void capabilityOneAndUnknownNeverAllow() {
        for(Integer version:new Integer[]{1,2,null}) {route.setDirectTakeoverVersion(version);assertEquals(Outcome.VERIFYING,service.admitLegacyDirect(7,LegacyIdentity.of(task)).getOutcome());}
        when(mapper.route(10)).thenReturn(null);assertFalse(service.admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());
    }
    @Test public void startRequiresExactOwnerAndOne() {
        route.setDirectTakeoverVersion(1);service.assertDirectSendEnabled(10,"owner");
        try{service.assertDirectSendEnabled(10,"other");fail();}catch(IllegalStateException expected){}
    }
    @Test public void historyProtectsOtherCardsAndMovedDeviceWithoutCurrentParkRead() {
        when(mapper.deviceHistory("device-a")).thenReturn(1);task.setCardNo("another-family-card");
        assertEquals("DIRECT_DEVICE_HISTORY_PROTECTED",service.admitLegacyDirect(7,LegacyIdentity.of(task)).getReason());
        verify(mapper,never()).deviceParks(anyString());verify(mapper,never()).route(anyInt());
    }
    @Test public void boundHistoricalPhaseIgnoresCurrentDeviceAndRoute() {
        SmtAuthTransportPhase p=phase();p.setState("UNKNOWN");when(mapper.taskPhases("7")).thenReturn(Collections.singletonList(p));
        Decision result=service.admitLegacyReceipt(7,"serial-7",200,"digest");assertEquals(Outcome.OWNED_BY_TRANSPORT,result.getOutcome());assertSame(p,result.getPhase());
        verify(mapper,never()).deviceHistory(anyString());verify(mapper,never()).deviceParks(anyString());verify(mapper,never()).route(anyInt());
    }
    @Test public void exactVehiclePhaseOwnsSendButNeverEntersCardReceipt() {
        SmtAuthTransportPhase p=phase();p.setResourceType("VEHICLE");p.setCredentialChannel("PLATE");task.setDeviceType(2);
        when(mapper.taskPhases("7")).thenReturn(Collections.singletonList(p));assertEquals(Outcome.OWNED_BY_TRANSPORT,service.admitLegacyDirect(7,LegacyIdentity.of(task)).getOutcome());
        assertEquals(Outcome.VERIFYING,service.admitLegacyReceipt(7,"serial-7",200,"d").getOutcome());
        task.setDeviceType(1);assertEquals("LEGACY_PHASE_MISMATCH",service.admitLegacyReceipt(7,"serial-7",200,"d").getReason());
    }
    @Test public void ambiguousOrMismatchedPhaseIsReview() {
        SmtAuthTransportPhase p=phase();when(mapper.taskPhases("7")).thenReturn(Arrays.asList(p,p));assertFalse(service.admitLegacyReceipt(7,"serial-7",200,"d").legacyAllowed());
        p.setCardNo("wrong");when(mapper.taskPhases("7")).thenReturn(Collections.singletonList(p));assertEquals(Outcome.VERIFYING,service.admitLegacyReceipt(7,"serial-7",200,"d").getOutcome());
    }
    @Test public void callerCannotForgeTaskSerialActionCardDeviceTypeOrService() {
        LegacyIdentity i=LegacyIdentity.of(task);
        List<LegacyIdentity> mismatches=Arrays.asList(i.toBuilder().taskId(8).build(),i.toBuilder().serialNo("x").build(),i.toBuilder().action(2).build(),
            i.toBuilder().deviceId("x").build(),i.toBuilder().cardNo("x").build(),i.toBuilder().deviceType(2).build(),i.toBuilder().serviceType(9).build());
        for(LegacyIdentity bad:mismatches)assertEquals("LEGACY_COMMAND_MISMATCH",service.admitLegacyDirect(7,bad).getReason());
    }
    @Test public void exactWireParkSerialActionAndCardAreCheckedAgainstDatabase() {
        LegacyIdentity good=LegacyIdentity.of(task).toBuilder().wireOperation("CARD_ADD").wirePark(10).wireEnvelopeDevice("device-a").wireDevice("device-a").wireCard("42")
            .wireSerial("serial-7").wireTask(7).wireGeneral("合成名称").wireCardType(1).wireStart(10L).wireEnd(20L).build();
        assertTrue(service.admitLegacyDirect(7,good).legacyAllowed());
        for(LegacyIdentity bad:Arrays.asList(good.toBuilder().wirePark(11).build(),good.toBuilder().wireSerial("x").build(),good.toBuilder().wireTask(8).build(),
            good.toBuilder().wireOperation("CARD_DELETE").build(),good.toBuilder().wireDevice("x").build(),good.toBuilder().wireCard("x").build(),good.toBuilder().wireEnd(99L).build()))
            assertEquals("LEGACY_WIRE_MISMATCH",service.admitLegacyDirect(7,bad).getReason());
    }
    @Test public void unknownTaskDeviceOrParkCannotUseCallerPark() {
        when(mapper.task(7)).thenReturn(null);assertFalse(service.admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());when(mapper.task(7)).thenReturn(task);
        for(List<Integer> parks:Arrays.asList(Collections.<Integer>emptyList(),Arrays.asList(10,11),Collections.<Integer>singletonList(null))) {
            when(mapper.deviceParks("device-a")).thenReturn(parks);assertEquals("LEGACY_DEVICE_PARK_UNKNOWN",service.admitLegacyDirect(7,LegacyIdentity.of(task)).getReason());
        }
    }
    @Test public void cardCallbackCannotAcknowledgeVehicleOrWrongSerial() {
        assertFalse(service.admitLegacyReceipt(7,"wrong",200,"d").legacyAllowed());task.setDeviceType(2);
        assertEquals(Outcome.VERIFYING,service.admitLegacyReceipt(7,"serial-7",200,"d").getOutcome());
    }
    @Test public void databaseAndReviewFailuresNeverAllow() {
        when(mapper.deviceHistory("device-a")).thenThrow(new IllegalStateException("missing claim schema"));
        assertEquals("DIRECT_GATE_UNAVAILABLE",service.admitLegacyDirect(7,LegacyIdentity.of(task)).getReason());
        reset(mapper);when(mapper.task(7)).thenReturn(task);when(mapper.taskPhases("7")).thenReturn(Collections.emptyList());when(mapper.deviceHistory("device-a")).thenReturn(0);
        when(mapper.deviceParks("device-a")).thenReturn(Collections.singletonList(10));when(mapper.route(10)).thenThrow(new IllegalStateException("route row locked"));
        assertEquals("DIRECT_GATE_UNAVAILABLE",service.admitLegacyDirect(7,LegacyIdentity.of(task)).getReason());verify(mapper,never()).review(anyString(),any(),anyString(),any(),any(),anyString());
        reset(mapper);when(mapper.task(7)).thenReturn(task);when(mapper.taskPhases("7")).thenReturn(Collections.emptyList());when(mapper.deviceHistory("device-a")).thenReturn(0);
        route.setDirectTakeoverVersion(1);when(mapper.deviceParks("device-a")).thenReturn(Collections.singletonList(10));when(mapper.route(10)).thenReturn(route);
        doThrow(new IllegalStateException("write down")).when(mapper).review(anyString(),any(),anyString(),any(),any(),anyString());
        assertEquals(Outcome.VERIFYING,service.admitLegacyDirect(7,LegacyIdentity.of(task)).getOutcome());
    }
    @Test public void reviewSurvivesOuterRollbackAndStableKeyExcludesPark() {
        route.setDirectTakeoverVersion(1);
        try{new TransactionTemplate(tx).execute(s->{assertEquals(Outcome.VERIFYING,service.admitLegacyDirect(7,LegacyIdentity.of(task)).getOutcome());throw new IllegalStateException("caller rolls back");});fail();}
        catch(IllegalStateException expected){}
        assertEquals(1,tx.durable.size());assertEquals(1,tx.rollbacks);
        when(mapper.deviceParks("device-a")).thenReturn(Collections.singletonList(11));when(mapper.route(11)).thenReturn(route);
        assertFalse(service.admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());assertEquals(1,tx.durable.size());
    }
    @Test public void duplicateMergeRaceOnlyAcceptedWhenSameKeyExists() {
        route.setDirectTakeoverVersion(1);doThrow(new DuplicateKeyException("race")).when(mapper).review(anyString(),any(),anyString(),any(),any(),anyString());
        when(mapper.reviewExists(anyString())).thenReturn(1);assertEquals("DIRECT_PARK_PROTECTED",service.admitLegacyDirect(7,LegacyIdentity.of(task)).getReason());
        when(mapper.reviewExists(anyString())).thenReturn(0);assertEquals("DIRECT_GATE_UNAVAILABLE",service.admitLegacyDirect(7,LegacyIdentity.of(task)).getReason());
    }
    @Test public void independentInstancesShareProtectionWithoutLocalFlags() {
        route.setDirectTakeoverVersion(1);AuthOperationDirectTakeoverService second=new AuthOperationDirectTakeoverService(mapper,tx);
        assertFalse(service.admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());assertFalse(second.admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());assertEquals(1,tx.durable.size());
    }
    @Test public void twoSpringContextsWithOppositeFlagsBothUsePersistentCapability() {
        route.setDirectTakeoverVersion(1);
        org.springframework.context.support.GenericApplicationContext off=context(false),on=context(true);
        try {assertFalse(off.getBean(AuthOperationDirectTakeoverService.class).admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());
            assertFalse(on.getBean(AuthOperationDirectTakeoverService.class).admitLegacyDirect(7,LegacyIdentity.of(task)).legacyAllowed());assertEquals(1,tx.durable.size());
        } finally{off.close();on.close();}
    }
    private org.springframework.context.support.GenericApplicationContext context(boolean enabled) {
        org.springframework.context.support.GenericApplicationContext c=new org.springframework.context.support.GenericApplicationContext();
        com.tce.smart.platform.core.config.AuthOperationProperties properties=new com.tce.smart.platform.core.config.AuthOperationProperties();properties.setEnabled(enabled);
        c.getBeanFactory().registerSingleton("flags",properties);c.getBeanFactory().registerSingleton("mapper",mapper);c.getBeanFactory().registerSingleton("transactions",tx);
        org.springframework.beans.factory.support.RootBeanDefinition bean=new org.springframework.beans.factory.support.RootBeanDefinition(AuthOperationDirectTakeoverService.class);
        bean.setAutowireMode(org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);c.registerBeanDefinition("takeover",bean);c.refresh();return c;
    }
    @Test public void concurrentProtectedAdmissionsHaveOneStableReviewKey() throws Exception {
        route.setDirectTakeoverVersion(1);ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try {java.util.concurrent.Callable<Decision> call=()->{if(!start.await(2,TimeUnit.SECONDS))throw new IllegalStateException("gate timeout");return service.admitLegacyReceipt(7,"serial-7",200,"d");};
            Future<Decision> a=pool.submit(call),b=pool.submit(call);start.countDown();assertEquals(Outcome.VERIFYING,a.get(3,TimeUnit.SECONDS).getOutcome());assertEquals(Outcome.VERIFYING,b.get(3,TimeUnit.SECONDS).getOutcome());assertEquals(1,tx.durable.size());
        }finally{pool.shutdownNow();assertTrue(pool.awaitTermination(2,TimeUnit.SECONDS));}
    }
    @Test public void mappedStatementsParseOfflineAndKeepHistoryIndependentOfPark() throws Exception {
        org.apache.ibatis.session.Configuration c=new org.apache.ibatis.session.Configuration();c.setMapUnderscoreToCamelCase(true);
        String file="src/main/resources/mapper/AuthOperationDirectTakeoverMapper.xml";
        try(java.io.InputStream in=new java.io.FileInputStream(file)){new org.apache.ibatis.builder.xml.XMLMapperBuilder(in,c,file,c.getSqlFragments()).parse();}
        String ns="com.tce.smart.platform.core.mapper.AuthOperationDirectTakeoverMapper.";
        Map<String,Object> args=new HashMap<>();args.put("device","synthetic");
        String history=c.getMappedStatement(ns+"deviceHistory").getBoundSql(args).getSql().toUpperCase();assertTrue(history.contains("SMT_AUTH_DIRECT_CLAIM"));assertFalse(history.contains("PARK_ID"));
        assertEquals(Integer.valueOf(5),c.getMappedStatement(ns+"deviceHistory").getTimeout());
        String routeSql=c.getMappedStatement(ns+"route").getBoundSql(Collections.singletonMap("park",10)).getSql().toUpperCase();
        assertTrue(routeSql.contains("DIRECT_TAKEOVER_VERSION"));assertTrue(routeSql.contains("FOR UPDATE WAIT 5"));
        assertFalse(c.hasStatement(ns+"setReviewLockTimeout"));assertFalse(c.hasStatement(ns+"clearReviewLockTimeout"));
    }
    SmtAuthTransportPhase phase() {
        SmtAuthTransportPhase p=new SmtAuthTransportPhase();p.setId(9L);p.setTargetId(90L);p.setAttemptId(91L);p.setAttemptNo(1);p.setLeaseToken("lease");
        p.setParkId(99);p.setInstanceId("old-owner");p.setAccessType("DIRECT");p.setPhase("DIRECT_SEND");p.setTaskId("7");p.setSerialNo("serial-7");
        p.setDeviceId("device-a");p.setCardNo("42");p.setResourceType("PERSON");p.setCredentialChannel("FACE");p.setAction("ADD");p.setServiceType("7");p.setStartTime(10L);p.setOverTime(20L);return p;
    }
    /** 模拟提交介质，验证真实 Spring TransactionTemplate 的挂起、独立提交、恢复与回滚。 */
    static class MemoryTransactions extends AbstractPlatformTransactionManager {
        static class Tx {Set<String> pending=new HashSet<>();}
        static class Holder {Tx tx;Holder(Tx t){tx=t;}}
        final ThreadLocal<Tx> current=new ThreadLocal<>();final Set<String> durable=ConcurrentHashMap.newKeySet();int rollbacks;
        protected Object doGetTransaction(){return new Holder(current.get());}
        protected boolean isExistingTransaction(Object x){return ((Holder)x).tx!=null;}
        protected void doBegin(Object x,TransactionDefinition d){Holder h=(Holder)x;h.tx=new Tx();current.set(h.tx);}
        protected Object doSuspend(Object x){Tx t=current.get();current.remove();((Holder)x).tx=null;return t;}
        protected void doResume(Object x,Object resource){current.set((Tx)resource);if(x!=null)((Holder)x).tx=(Tx)resource;}
        protected void doCommit(DefaultTransactionStatus s){durable.addAll(((Holder)s.getTransaction()).tx.pending);}
        protected void doRollback(DefaultTransactionStatus s){rollbacks++;}
        protected void doCleanupAfterCompletion(Object x){current.remove();}
    }
}
