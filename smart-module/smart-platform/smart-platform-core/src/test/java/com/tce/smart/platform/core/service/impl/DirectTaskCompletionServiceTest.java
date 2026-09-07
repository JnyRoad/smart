package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.*;
import org.mockito.*;
import java.util.concurrent.atomic.AtomicInteger;

/** 直连完成的状态、命令身份与副作用顺序回归。 */
public class DirectTaskCompletionServiceTest {
    private SmtDeviceTaskService tasks;
    private SmtTaskDownRecordService records;
    private DirectTaskCompletionService service;
    @Before public void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
        tasks = Mockito.mock(SmtDeviceTaskService.class);
        records = Mockito.mock(SmtTaskDownRecordService.class);
        service = new DirectTaskCompletionService(tasks, records);
        AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);
        Mockito.when(gate.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Decision.builder()
            .outcome(com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Outcome.LEGACY_ALLOWED).build());service.setDirectTakeover(gate);
    }
    @Test public void initialTaskCompletesWithFrozenCommandAndCopiedSnapshot() {
        SmtDeviceTask task = task(0);
        Mockito.when(tasks.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);
        AtomicInteger derived = new AtomicInteger();
        Assert.assertTrue(service.completeSuccess(task, 200, "成功", completed -> {
            Assert.assertEquals(Integer.valueOf(1), completed.getStatus()); derived.incrementAndGet();
        }));
        ArgumentCaptor<LambdaUpdateWrapper> update = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        Mockito.verify(tasks).update(update.capture());
        String sql = update.getValue().getSqlSegment().toUpperCase();
        Assert.assertTrue(sql.contains("ID") && sql.contains("STATUS") && sql.contains("SERIAL_NO") && sql.contains("ACTION"));
        Assert.assertTrue(update.getValue().getParamNameValuePairs().containsValue("command-17"));
        Assert.assertEquals(Integer.valueOf(0), task.getStatus());
        Assert.assertEquals(1, derived.get());
        Mockito.verify(records).handleTaskDownRecord(Mockito.argThat(t -> t.getStatus().equals(1)));
    }
    @Test public void casMissDoesNotTouchRecordOrDerivedTask() {
        AtomicInteger derived = new AtomicInteger();
        Assert.assertFalse(service.completeSuccess(task(0), 200, "成功", t -> derived.incrementAndGet()));
        Assert.assertEquals(0, derived.get());
        Mockito.verifyZeroInteractions(records);
    }
    @Test public void terminalAndUnknownStatesRejectSuccessAndFailure() {
        for (Integer status : new Integer[]{1,4,5,99}) {
            Assert.assertFalse(service.completeSuccess(task(status), 200, "成功", t -> Assert.fail()));
            Assert.assertFalse(service.recordResult(task(status), 2, 402, "迟到失败", 1L));
        }
        Mockito.verifyZeroInteractions(tasks, records);
    }
    @Test public void missingSerialCannotClaimCompletion() {
        SmtDeviceTask task = task(0); task.setSerialNo(null);
        Assert.assertFalse(service.completeSuccess(task, 200, "成功", t -> Assert.fail()));
        Mockito.verifyZeroInteractions(tasks, records);
    }
    @Test(expected = IllegalStateException.class) public void recordFailurePropagatesBeforeDerivedTask() {
        Mockito.when(tasks.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);
        Mockito.doThrow(new IllegalStateException("记录失败")).when(records).handleTaskDownRecord(Mockito.any());
        service.completeSuccess(task(0), 200, "成功", t -> Assert.fail("记录失败后不得派生"));
    }
    @Test(expected = IllegalStateException.class) public void derivedFailurePropagates() {
        Mockito.when(tasks.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);
        service.completeSuccess(task(0), 200, "成功", t -> { throw new IllegalStateException("派生失败"); });
    }
    @Test public void retryResultPreservesStatusAndDoesNotMaintainRecords() {
        Mockito.when(tasks.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);
        Assert.assertTrue(service.recordResult(task(0), null, 503, "等待重试", 10L));
        ArgumentCaptor<LambdaUpdateWrapper> update = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        Mockito.verify(tasks).update(update.capture());
        Assert.assertFalse(update.getValue().getSqlSet().toUpperCase().contains("STATUS="));
        Mockito.verifyZeroInteractions(records);
    }
    @Test public void protectedRawSuccessCannotReachCasRecordOrDerivedTask() {
        AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);service.setDirectTakeover(gate);
        Mockito.when(gate.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Decision.builder()
            .outcome(com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.Outcome.VERIFYING).reason("protected").build());
        Assert.assertFalse(service.completeSuccess(task(0),200,"raw",t->Assert.fail()));Mockito.verifyZeroInteractions(tasks,records);
    }
    @Test public void arbitraryContextCannotBypassProtectedSuccess() {
        AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);service.setDirectTakeover(gate);
        com.tce.smart.platform.core.entity.SmtAuthTransportPhase p=new com.tce.smart.platform.core.entity.SmtAuthTransportPhase();p.setTaskId("17");p.setAccessType("DIRECT");
        try(AuthOperationTransportRecordContext context=AuthOperationTransportRecordContext.open(p)) {
            Assert.assertFalse(service.completeSuccess(task(0),200,"raw",t->Assert.fail()));
        }
        Mockito.verify(gate).admitLegacyDirect(Mockito.eq(17),Mockito.any());Mockito.verifyZeroInteractions(tasks,records);
    }
    @Test public void completeExactTransportContextRequiresActualTransactionAndFrozenIdentity() {
        AuthOperationDirectTakeoverServiceTest fixture=new AuthOperationDirectTakeoverServiceTest();fixture.before();
        AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);service.setDirectTakeover(gate);
        com.tce.smart.platform.core.entity.SmtAuthTransportPhase p=fixture.phase();SmtDeviceTask task=fixture.task;
        Mockito.when(tasks.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);
        try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)) {
            Assert.assertFalse(service.completeSuccess(task,200,"no transaction",t->Assert.fail()));
        }
        Mockito.clearInvocations(gate,tasks,records);
        new org.springframework.transaction.support.TransactionTemplate(fixture.tx).execute(status->{
            try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)) {Assert.assertTrue(service.completeSuccess(task,200,"verified",null));}return null;
        });
        Mockito.verifyZeroInteractions(gate);Mockito.verify(records).handleTaskDownRecord(Mockito.any());Mockito.clearInvocations(gate,tasks,records);
        for(String mismatch:new String[]{"serial","device","card","action","service","time"}) {
            SmtDeviceTask bad=new SmtDeviceTask();org.springframework.beans.BeanUtils.copyProperties(task,bad);
            if("serial".equals(mismatch))bad.setSerialNo("other");if("device".equals(mismatch))bad.setDeviceCode("other");if("card".equals(mismatch))bad.setCardNo("other");
            if("action".equals(mismatch))bad.setAction(2);if("service".equals(mismatch))bad.setServiceType(9);if("time".equals(mismatch))bad.setOverTime(99L);
            new org.springframework.transaction.support.TransactionTemplate(fixture.tx).execute(status->{
                try(AuthOperationTransportRecordContext ignored=AuthOperationTransportRecordContext.open(p)) {Assert.assertFalse(service.completeSuccess(bad,200,"wrong",t->Assert.fail()));}return null;
            });
        }
        Mockito.verify(gate,Mockito.times(6)).admitLegacyDirect(Mockito.eq(7),Mockito.any());Mockito.verifyZeroInteractions(tasks,records);
    }
    private SmtDeviceTask task(Integer status) {
        SmtDeviceTask task = new SmtDeviceTask();
        task.setId(17); task.setSerialNo("command-17"); task.setStatus(status); task.setAction(1);
        return task;
    }
}
