package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptCommand;
import com.tce.smart.platform.core.entity.*;
import org.junit.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/** 本条证据的可信门禁不能被目标既有 CONFIRMED 状态替代。 */
public class AuthOperationWorkflowTest {
    private SmtAuthOperationTarget target() {
        SmtAuthOperationTarget t=new SmtAuthOperationTarget();t.setId(10L);t.setOperationVersion(3L);
        t.setAccessType("DIRECT");t.setState("CONFIRMED");return t;
    }
    private SmtAuthOperationAttempt attempt() {
        SmtAuthOperationAttempt a=new SmtAuthOperationAttempt();a.setId(20L);a.setTargetId(10L);
        a.setAttemptNo(1);a.setLeaseToken("lease");a.setAccessType("DIRECT");a.setExternalCommandId("command");return a;
    }
    private AuthOperationReceiptCommand event() {
        return AuthOperationReceiptCommand.builder().targetId(10L).attemptId(20L).attemptNo(1)
            .leaseToken("lease").accessType("DIRECT").externalCommandId("command").operationVersion(3L)
            .trustedDeviceEvidence(true).evidenceType("DEVICE_ACK").resultStatus("SUCCESS").build();
    }
    @Test public void trustedCurrentSuccessIsEligibleEvenAfterTargetIsConfirmed() {
        Assert.assertTrue(AuthOperationWorkflowService.trustedSuccess(event(),target(),attempt()));
    }
    @Test public void confirmedTargetDoesNotMakeOldFailureOrPlatformAbsenceTrusted() {
        for(String status:new String[]{"FAIL","PLATFORM_NOT_FOUND","EMPTY","CANCELLED"})
            Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().resultStatus(status).build(),target(),attempt()));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().trustedDeviceEvidence(false).build(),target(),attempt()));
    }
    @Test public void exactAttemptLeaseExternalVersionAndAccessAreAllRequired() {
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().attemptId(21L).build(),target(),attempt()));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().leaseToken("old").build(),target(),attempt()));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().externalCommandId(null).build(),target(),attempt()));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().operationVersion(2L).build(),target(),attempt()));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(event().toBuilder().accessType("ISC").build(),target(),attempt()));
    }    @Test public void iscNeedsExactPersistedExternalBatchForTrustedSuccess() {
        SmtAuthOperationTarget t=target();t.setAccessType("ISC");
        SmtAuthOperationAttempt a=attempt();a.setAccessType("ISC");a.setExternalBatchId("isc-batch");
        AuthOperationReceiptCommand e=event().toBuilder().accessType("ISC").externalBatchId("isc-batch").externalCommandId(null).build();
        Assert.assertTrue(AuthOperationWorkflowService.trustedSuccess(e,t,a));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(e.toBuilder().externalBatchId(null).build(),t,a));
        Assert.assertFalse(AuthOperationWorkflowService.trustedSuccess(e.toBuilder().externalBatchId("another").build(),t,a));
    }

    @Test public void liveClaimedAttemptsBlockReuseAndRetainedSettlement() throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mapper/AuthOperationWorkflowMapper.xml")) {
            Assert.assertNotNull(input);
            String mapperXml = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
            Assert.assertFalse(mapperXml.contains("A.STATUS!='CLAIMED'"));
            Assert.assertTrue(mapperXml.contains("A.STATUS!='EXPIRED'"));
        }
    }

}
