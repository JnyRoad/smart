package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.*;
import com.tce.smart.platform.core.service.impl.DirectTaskCompletionService;
import com.tce.smart.platform.core.service.impl.AuthOperationDirectTakeoverService;
import com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard;
import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.*;
import java.util.Date;

/** 直连车辆完成与访客派生任务回归。 */
public class DeviceTaskServiceImplTest {
    @Mock private SmtDeviceTaskService tasks;
    @Mock private SmtTaskDownRecordService records;
    @Mock private SmtVisitorService visitors;
    @Mock private SmtDeviceService devices;
    @Mock private com.tce.smart.dispatcher.api.feign.RemoteDispatcherService dispatcher;
    @Mock private SmtImageService images;
    @Mock private com.tce.smart.platform.api.feign.RemoteStaffService staff;
    @InjectMocks private DeviceTaskServiceImpl service;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(staff.getSimpleSttaffById(Mockito.anyString(),Mockito.anyString())).thenReturn(com.tce.smart.common.core.model.Result.success(null));
        AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);
        Mockito.when(gate.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());
        DirectTaskCompletionService completion=new DirectTaskCompletionService(tasks,records);completion.setDirectTakeover(gate);
        ReflectionTestUtils.setField(service,"directTaskCompletionService",completion);
        AuthOperationTransportGuard guard=Mockito.mock(AuthOperationTransportGuard.class);service.setTransportGuard(guard);
        Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtVisitor.class);
        SmtVisitor visitor = new SmtVisitor();
        visitor.setEndTime(new Date(1790000000000L));
        Mockito.when(visitors.getOne(Mockito.any())).thenReturn(visitor);
        Mockito.when(tasks.save(Mockito.any(SmtDeviceTask.class))).thenReturn(true);
        Mockito.when(tasks.update(Mockito.any())).thenReturn(true);
    }

    @Test public void workflowTaskNeverEntersLegacyDispatcher() {
        com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard.class);
        service.setTransportGuard(guard);Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.OWNED_BY_TRANSPORT).reason("owned").build());
        service.down(task(1));service.del(task(2));Mockito.verifyZeroInteractions(dispatcher);
    }
    @Test public void publicVehicleAddCannotBypassBoundGuard() { publicBoundExit(1, 2); }
    @Test public void publicVehicleDeleteCannotBypassBoundGuard() { publicBoundExit(2, 2); }
    @Test public void publicPersonDeleteCannotBypassBoundGuard() { publicBoundExit(2, 1); }
    private void publicBoundExit(int action,int type) {
        com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard.class);
        service.setTransportGuard(guard); Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.OWNED_BY_TRANSPORT).reason("owned").build());
        Mockito.when(dispatcher.dispatch(Mockito.any(),Mockito.anyString())).thenReturn(com.tce.smart.common.core.model.Result.success(null));
        SmtDeviceTask task=task(action);task.setDeviceType(type);
        com.tce.smart.common.core.model.Result result=type==1?service.delCard(task,900002):action==1?service.downCarCard(task,900002):service.delCarCard(task,900002);
        Mockito.verifyZeroInteractions(dispatcher,records);
        Assert.assertFalse(result.isSuccess());
    }

    @Test public void vehicleAddCreatesDeleteWithNewIdentity() {
        SmtDeviceTask original = task(DeviceTaskActionEnum.DOWN.getCode());
        service.downCarResultHandle(original);
        ArgumentCaptor<SmtDeviceTask> generated = ArgumentCaptor.forClass(SmtDeviceTask.class);
        Mockito.verify(tasks).save(generated.capture());
        Assert.assertNull("新删除任务不能复用新增任务主键", generated.getValue().getId());
        Assert.assertNotEquals(original.getSerialNo(), generated.getValue().getSerialNo());
        Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), generated.getValue().getAction());
    }

    @Test public void vehicleDeleteDoesNotGenerateAnotherDelete() {
        service.downCarResultHandle(task(DeviceTaskActionEnum.DEL.getCode()));
        Mockito.verify(tasks, Mockito.never()).save(Mockito.any(SmtDeviceTask.class));
    }

    @Test(expected = IllegalStateException.class)
    public void derivedDeleteSaveFailureMustPropagate() {
        Mockito.when(tasks.save(Mockito.any(SmtDeviceTask.class))).thenReturn(false);
        service.downCarResultHandle(task(DeviceTaskActionEnum.DOWN.getCode()));
    }

    @Test public void terminalTaskCannotBeRewrittenBeforeDispatch() {
        SmtDeviceTask task = task(1); task.setStatus(1); task.setTimes(0);
        service.down(task);
        Mockito.verify(tasks, Mockito.never()).updateById(Mockito.any(SmtDeviceTask.class));
        Mockito.verifyZeroInteractions(dispatcher);
    }

    @Test public void cardAcceptanceNeverCompletesOrResetsTask() {
        com.tce.smart.common.core.model.Result result = com.tce.smart.common.core.model.Result.success(true);
        ReflectionTestUtils.invokeMethod(service, "delCardResultHandle", task(2), result, 8L);
        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper> update =
                ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class);
        Mockito.verify(tasks).update(update.capture());
        Assert.assertFalse(update.getValue().getSqlSet().toUpperCase().contains("STATUS="));
        Mockito.verifyZeroInteractions(records);
    }

    @Test public void missingVehicleExecutionEvidenceCannotReusePriorSuccessCode() {
        Mockito.when(dispatcher.dispatch(Mockito.any(), Mockito.anyString()))
                .thenReturn(com.tce.smart.common.core.model.Result.success(null));
        service.delCarCard(task(2), 900001);
        Mockito.verifyZeroInteractions(records);
        Mockito.verify(tasks, Mockito.never()).save(Mockito.any(SmtDeviceTask.class));
    }

    @Test public void fourthPersonAddExitAlsoRejectsBeforeResultMutation() {
        AuthOperationTransportGuard guard=Mockito.mock(AuthOperationTransportGuard.class);service.setTransportGuard(guard);
        Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.VERIFYING).reason("protected").build());
        SmtDeviceTask t=task(1);t.setDeviceType(1);
        com.tce.smart.common.core.model.Result r=ReflectionTestUtils.invokeMethod(service,"downCard",t,900001);
        Assert.assertEquals(Integer.valueOf(DeviceTaskServiceImpl.DIRECT_REVIEW_CODE),r.getCode());Mockito.verifyZeroInteractions(dispatcher,records);
        Mockito.verify(tasks,Mockito.never()).update(Mockito.any());
    }
    @Test public void fourLegacyExitsPassActualWireIdentityThroughFinalGate() {
        AuthOperationTransportGuard guard=Mockito.mock(AuthOperationTransportGuard.class);service.setTransportGuard(guard);
        Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());
        Mockito.when(dispatcher.dispatch(Mockito.any(),Mockito.anyString())).thenReturn(com.tce.smart.common.core.model.Result.success(null));
        service.downCarCard(task(1),900001);service.delCarCard(task(2),900001);
        SmtDeviceTask person=task(2);person.setDeviceType(1);service.delCard(person,900001);person.setAction(1);ReflectionTestUtils.invokeMethod(service,"downCard",person,900001);
        ArgumentCaptor<LegacyIdentity> identities=ArgumentCaptor.forClass(LegacyIdentity.class);Mockito.verify(guard,Mockito.times(4)).admitLegacyDirect(Mockito.eq(17),identities.capture());
        java.util.List<LegacyIdentity> all=identities.getAllValues();Assert.assertEquals("CAR_ADD",all.get(0).getWireOperation());Assert.assertEquals("CAR_DELETE",all.get(1).getWireOperation());
        Assert.assertEquals("CARD_DELETE",all.get(2).getWireOperation());Assert.assertEquals("CARD_ADD",all.get(3).getWireOperation());
        for(LegacyIdentity i:all){Assert.assertEquals("synthetic-device",i.getWireDevice());Assert.assertEquals("123",i.getWireCard());Assert.assertEquals(Integer.valueOf(900001),i.getWirePark());}
        Assert.assertEquals("current-command",all.get(3).getWireSerial());Assert.assertEquals(Integer.valueOf(17),all.get(3).getWireTask());Mockito.verify(dispatcher,Mockito.times(4)).dispatch(Mockito.any(),Mockito.anyString());
    }
    @Test public void protectedDeleteDoesNotSendSmsAndHealthyTaskOnSamePageContinues() {
        AuthOperationTransportGuard guard=Mockito.mock(AuthOperationTransportGuard.class);service.setTransportGuard(guard);
        Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenAnswer(a->Decision.builder().outcome(Integer.valueOf(17).equals(a.getArgument(0))?Outcome.VERIFYING:Outcome.LEGACY_ALLOWED).reason("scope").build());
        SmtDeviceTask blocked=task(2);blocked.setDeviceType(1);SmtDeviceTask healthy=task(2);healthy.setId(18);healthy.setCardNo("456");healthy.setDeviceType(1);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SmtDeviceTask> normal=new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();normal.setRecords(java.util.Arrays.asList(blocked,healthy));
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SmtDeviceTask> delayed=new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();delayed.setRecords(java.util.Collections.emptyList());
        Mockito.when(tasks.getDel(Mockito.any(),Mockito.anyLong(),Mockito.eq(1))).thenReturn(normal);Mockito.when(tasks.getDelayDel(Mockito.any(),Mockito.anyLong(),Mockito.eq(1))).thenReturn(delayed);
        com.tce.smart.platform.core.entity.SmtDevice device=new com.tce.smart.platform.core.entity.SmtDevice();device.setParkId(900001);device.setConnectStatus(com.tce.smart.tool.constant.DeviceConstants.ON_LINE);Mockito.when(devices.getOne(Mockito.any())).thenReturn(device);
        Mockito.when(dispatcher.dispatch(Mockito.any(),Mockito.anyString())).thenReturn(com.tce.smart.common.core.model.Result.success(null));
        service.delCard();Mockito.verify(dispatcher,Mockito.times(1)).dispatch(Mockito.any(),Mockito.anyString());Mockito.verify(visitors).updateSmsCode(456L);Mockito.verify(visitors,Mockito.never()).updateSmsCode(123L);Mockito.verifyZeroInteractions(records);
    }
    @Test public void publicVehicleResultCannotDeriveTaskInProtectedScope() {
        AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);Mockito.when(gate.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.VERIFYING).build());
        DirectTaskCompletionService completion=new DirectTaskCompletionService(tasks,records);completion.setDirectTakeover(gate);ReflectionTestUtils.setField(service,"directTaskCompletionService",completion);
        Assert.assertFalse(service.downCarResultHandle(task(1)));Mockito.verifyZeroInteractions(records);Mockito.verify(tasks,Mockito.never()).update(Mockito.any());Mockito.verify(tasks,Mockito.never()).save(Mockito.any(SmtDeviceTask.class));
    }
    @Test public void activationAfterLegacyAllowCannotRecallAlreadyAdmittedHttp() throws Exception {
        AuthOperationTransportGuard guard=Mockito.mock(AuthOperationTransportGuard.class);service.setTransportGuard(guard);
        java.util.concurrent.CountDownLatch admitted=new java.util.concurrent.CountDownLatch(1),resume=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger capability=new java.util.concurrent.atomic.AtomicInteger(0);
        Mockito.when(guard.admitLegacyDirect(Mockito.any(),Mockito.any())).thenAnswer(a->{
            Decision captured=Decision.builder().outcome(capability.get()==0?Outcome.LEGACY_ALLOWED:Outcome.VERIFYING).build();
            admitted.countDown();if(!resume.await(2,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("gate timeout");return captured;
        });
        Mockito.when(dispatcher.dispatch(Mockito.any(),Mockito.anyString())).thenReturn(com.tce.smart.common.core.model.Result.success(null));
        java.util.concurrent.ExecutorService worker=java.util.concurrent.Executors.newSingleThreadExecutor();
        try {java.util.concurrent.Future<?> running=worker.submit(()->service.delCarCard(task(2),900001));
            Assert.assertTrue(admitted.await(2,java.util.concurrent.TimeUnit.SECONDS));capability.set(1);resume.countDown();running.get(3,java.util.concurrent.TimeUnit.SECONDS);
            Mockito.verify(dispatcher).dispatch(Mockito.any(),Mockito.anyString());
        } finally{resume.countDown();worker.shutdownNow();Assert.assertTrue(worker.awaitTermination(2,java.util.concurrent.TimeUnit.SECONDS));}
    }
    private SmtDeviceTask task(Integer action) {
        SmtDeviceTask task = new SmtDeviceTask();
        task.setId(17); task.setSerialNo("current-command"); task.setStatus(0); task.setCode(200);
        task.setAction(action); task.setDeviceType(DeviceTaskConstants.CAR); task.setServiceType(DeviceTaskConstants.CAR_VISITOR);
        task.setCardNo("123"); task.setDeviceCode("synthetic-device"); task.setStartTime(1780000000L); task.setOverTime(1790000000L);
        return task;
    }
}
