package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import org.junit.*;
import java.util.*;
import static org.mockito.Mockito.*;

/** 真实Transport消费边界，真实Workflow可信门禁，显式替换Version与持久层；事务提交另由Oracle反例验证。 */
public class AuthTransportPhysicalSettlementTest {
    @Test public void trustedCurrentRecordFinishesPhysicalPhaseWhileSourceStillPending() throws Exception {
        Fixture f=new Fixture();Received result=f.receive(true);
        Assert.assertEquals("FINISHED",f.phase.getState());Assert.assertFalse(result.isSourceConverged());
        Assert.assertEquals("CONFIRMED",f.target.getState());
    }
    @Test public void confirmedOrConvergedTargetCannotStandInForThisEventsTrust() throws Exception {
        for(String state:Arrays.asList("CONFIRMED","CONVERGED")) {
            Fixture f=new Fixture();f.target.setState(state);f.outcomes=Collections.singletonList("UNTRUSTED");f.event.setEventNamespace("WF:U:TRANSPORT");f.receive(false);
            Assert.assertEquals("ACCEPTED",f.phase.getState());
        }
    }
    @Test public void everySharedContributionMustSettleNotOnlyTheLastResult() throws Exception {
        Fixture f=new Fixture();f.addShared(2);f.target.setState("CONVERGED");f.outcomes=Arrays.asList("UNTRUSTED","CURRENT_APPLIED");
        f.receive(true);Assert.assertEquals(2,f.calls);Assert.assertEquals("ACCEPTED",f.phase.getState());
    }
    @Test public void settledCurrentAndPersistedStaleSharedMembersCanFinishTogether() throws Exception {
        Fixture f=new Fixture();f.addShared(2);f.outcomes=Arrays.asList("CURRENT_APPLIED","STALE_COMPENSATE");
        f.receive(true);Assert.assertEquals("FINISHED",f.phase.getState());Assert.assertEquals("CONFIRMED",f.target.getState());
    }
    @Test public void staleCompensationAndReplayNeedTrustedVersionResultAndSuccessorGeneration() throws Exception {
        for(String outcome:Arrays.asList("STALE_COMPENSATE","STALE_REPLAY")) {
            Fixture good=new Fixture();good.outcomes=Collections.singletonList(outcome);good.receive(true);Assert.assertEquals("FINISHED",good.phase.getState());
            Fixture sameGeneration=new Fixture();sameGeneration.outcomes=Collections.singletonList(outcome);sameGeneration.staleGeneration=1;sameGeneration.receive(true);Assert.assertEquals("ACCEPTED",sameGeneration.phase.getState());
        }
    }
    @Test public void missingRecordOrMismatchedEventCannotFinishEvenATerminalTarget() throws Exception {
        for(String mutation:Arrays.asList("record","receiptTarget","receiptAttempt","version","lease","external","access")) {
            Fixture f=new Fixture();f.target.setState("CONVERGED");
            switch(mutation) {
                case "record":f.event.setConverged("N");break;
                case "receiptTarget":f.event.setTargetId(99L);break;
                case "receiptAttempt":f.event.setAttemptId(99L);break;
                case "version":f.target.setOperationVersion(99L);break;
                case "lease":f.attempt.setLeaseToken("other");break;
                case "external":f.attempt.setExternalBatchId("other");break;
                case "access":f.attempt.setAccessType("DIRECT");break;
                default:Assert.fail(mutation);
            }
            try{f.receive(true);}catch(IllegalArgumentException mismatch){Assert.assertEquals("external",mutation);}
            Assert.assertEquals(mutation,"ACCEPTED",f.phase.getState());
        }
    }
    @Test public void recordFailurePropagatesBeforeAnyPhaseFinish() throws Exception {
        Fixture f=new Fixture();f.event.setConverged("N");when(f.completion.completeSuccess(any(),anyString())).thenThrow(new IllegalStateException("受控record失败"));
        try{f.receive(true);Assert.fail("record失败须传播至外层事务回滚");}catch(IllegalStateException expected){Assert.assertEquals("受控record失败",expected.getMessage());}
        verify(f.phases,never()).transition(anyLong(),anyString(),eq("FINISHED"),anyString(),any());Assert.assertEquals("ACCEPTED",f.phase.getState());
    }
    @Test public void staleAcceptedSnapshotCanOnlyReuseAnIdenticalPersistedFinishedPhase() throws Exception {
        for(String mutation:Arrays.asList("none","state","attempt","target","external","lease","resource","generation","task","park","instance","request","person","device","phase","access")) {
            Fixture f=new Fixture();SmtAuthTransportPhase persisted=new SmtAuthTransportPhase();org.springframework.beans.BeanUtils.copyProperties(f.phase,persisted);persisted.setState("FINISHED");
            switch(mutation){
                case "state":persisted.setState("UNKNOWN");break;case "attempt":persisted.setAttemptId(999L);break;
                case "target":persisted.setTargetId(999L);break;case "external":persisted.setExternalId("different");break;
                case "lease":persisted.setLeaseToken("different");break;case "resource":persisted.setResourceId("different");break;
                case "generation":persisted.setResourceGeneration(999L);break;case "task":persisted.setTaskId("different");break;
                case "park":persisted.setParkId(999);break;case "instance":persisted.setInstanceId("different");break;
                case "request":persisted.setRequestKey("different");break;case "person":persisted.setPersonId("different");break;
                case "device":persisted.setDeviceId("different");break;case "phase":persisted.setPhase("ISC_CONFIG");break;
                case "access":persisted.setAccessType("DIRECT");break;default:break;
            }
            when(f.phases.byId(1L)).thenReturn(f.phase,persisted);
            when(f.phases.transition(eq(1L),eq("ACCEPTED"),eq("FINISHED"),eq("external"),any())).thenReturn(0);
            try{Received result=f.receive(true);Assert.assertEquals(mutation,"none",mutation);Assert.assertTrue(result.isPhysicalSettled());}
            catch(IllegalArgumentException conflict){if("none".equals(mutation))throw conflict;}
            Assert.assertEquals("旧快照不得被Mock原地同步","ACCEPTED",f.phase.getState());
        }
    }
    private static class Fixture {
        final AuthOperationTransportMapper phases=mock(AuthOperationTransportMapper.class);
        final AuthOperationService operations=mock(AuthOperationService.class);
        final AuthOperationVersionService versions=mock(AuthOperationVersionService.class);
        final AuthOperationWorkflowService workflow;
        final IscTaskCompletionService completion=mock(IscTaskCompletionService.class);
        final AuthOperationWorkflowMapper bindings=mock(AuthOperationWorkflowMapper.class);

        final SmtAuthOperationTargetMapper targets=mock(SmtAuthOperationTargetMapper.class);
        final SmtAuthOperationAttemptMapper attempts=mock(SmtAuthOperationAttemptMapper.class);
        final SmtAuthTransportPhase phase=new SmtAuthTransportPhase();
        final SmtAuthOperationTarget target=new SmtAuthOperationTarget();
        final SmtAuthOperationAttempt attempt=new SmtAuthOperationAttempt();
        final SmtAuthResultEvent event=new SmtAuthResultEvent();
        final List<SmtAuthSourceResource> shared=new ArrayList<>();
        final AuthOperationTransportService transport;
        List<String> outcomes=Collections.singletonList("CURRENT_APPLIED");int calls;long staleGeneration=2;
        Fixture() throws Exception {
            phase.setId(1L);phase.setParkId(101);phase.setInstanceId("physical-test");phase.setAccessType("ISC");phase.setPhase("ISC_DOWNLOAD");phase.setState("ACCEPTED");
            phase.setTargetId(11L);phase.setAttemptId(21L);phase.setAttemptNo(1);phase.setLeaseToken("lease");phase.setResourceId("resource");phase.setResourceGeneration(1L);phase.setPersonId("person");phase.setDeviceId("device");phase.setExternalId("external");
            target.setId(11L);target.setState("CONFIRMED");target.setAccessType("ISC");target.setOperationVersion(1L);target.setAction("DELETE");addShared(1);
            attempt.setId(21L);attempt.setTargetId(11L);attempt.setAttemptNo(1);attempt.setLeaseToken("lease");attempt.setAccessType("ISC");attempt.setExternalBatchId("external");
            when(phases.byId(1L)).thenReturn(phase);when(targets.selectById(11L)).thenReturn(target);when(attempts.selectByIdAndTarget(21L,11L)).thenReturn(attempt);when(bindings.targetContributions(11L)).thenReturn(shared);
            event.setId(31L);event.setTargetId(11L);event.setAttemptId(21L);event.setEventNamespace("WF:T:TRANSPORT");event.setEvidenceType("DEVICE_ACK");event.setResultStatus("SUCCESS");event.setAccessType("ISC");event.setOperationVersion(1L);event.setExternalBatchId("external");event.setConverged("Y");
            when(bindings.eventRecordConverged(31L)).thenAnswer(call->"Y".equals(event.getConverged())?1:0);
            when(bindings.exactBinding(anyString(),anyLong(),anyString(),anyLong(),anyLong())).thenAnswer(call->shared.stream().filter(c->c.getSourceCoordId().equals(call.getArgument(0))).findFirst().get());
            when(bindings.source(anyString())).thenAnswer(call->{SmtAuthSourceCoord source=new SmtAuthSourceCoord();source.setSourceKind("STAFF_AUTH");return source;});
            when(bindings.finishEvent(31L)).thenReturn(1);when(bindings.completedRecordEvent(any(),anyLong())).thenReturn(null);
            when(operations.recordReceipt(any())).thenAnswer(call->AuthOperationReceiptResult.builder().eventId(31L).targetId(event.getTargetId()).attemptId(event.getAttemptId()).build());
            when(versions.pendingSourceResources(anyString(),anyLong(),any(),anyInt())).thenReturn(Collections.singletonList("other-device"));
            SmtIscDeviceTask legacy=new SmtIscDeviceTask();legacy.setId(41L);SmtIscDeviceTaskMapper legacyTasks=mock(SmtIscDeviceTaskMapper.class);when(legacyTasks.selectById(41L)).thenReturn(legacy);phase.setTaskId("41");
            when(completion.completeSuccess(any(),anyString())).thenReturn(true);
            when(phases.transition(eq(1L),anyString(),anyString(),eq("external"),any())).thenAnswer(call->{phase.setState(call.getArgument(2));return 1;});
            when(versions.applyEvidence(any())).thenAnswer(call->{
                Evidence input=call.getArgument(0);String outcome=outcomes.get(Math.min(calls++,outcomes.size()-1));if(!input.isTrusted())outcome="UNTRUSTED";boolean current="CURRENT_APPLIED".equals(outcome);
                return EvidenceResult.builder().outcome(outcome).mayApply(current).compensationRequired("STALE_COMPENSATE".equals(outcome))
                    .current(ResourceDecision.builder().resourceId("resource").generation(current?1:staleGeneration).appliedGeneration(current?1:0).build()).build();
            });
            workflow=new AuthOperationWorkflowService(operations,versions,mock(SmtAuthOperationBatchMapper.class),targets,attempts,bindings);
            transport=new AuthOperationTransportService(phases,workflow,bindings,versions,targets,attempts,mock(SmtDeviceMapper.class),mock(SmtDeviceTaskMapper.class),legacyTasks,mock(EmployeeAuthOperationService.class),new AuthOperationProperties(),mock(DirectTaskCompletionService.class),completion);

        }
        void addShared(int count){shared.clear();for(int n=1;n<=count;n++){SmtAuthSourceResource c=new SmtAuthSourceResource();c.setSourceCoordId("source"+n);c.setSourceGeneration(1L);c.setResourceCoordId("resource");c.setResourceGeneration(1L);c.setTargetId(11L);c.setAttemptId(21L);c.setRequestId((long)n);shared.add(c);}}
        Received receive(boolean success){return transport.receipt(101,"physical-test",1L,"person","device","external","event",success,"受控设备证据");}
    }
}
