package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimCommand;
import com.tce.smart.platform.core.mapper.*;
import org.junit.*;
import org.mockito.Mockito;
import java.lang.reflect.Method;
import java.util.*;

/** 精确候选集合为空时禁止退回园区广查。 */
public class AuthOperationSchedulerClaimTest {
    @Test public void oversizedSelectionIsRejectedBeforeQuery() {
        SmtAuthOperationTargetMapper targets=Mockito.mock(SmtAuthOperationTargetMapper.class);
        AuthOperationService service=new AuthOperationService(Mockito.mock(SmtAuthOperationBatchMapper.class),Mockito.mock(SmtAuthDeleteRequestMapper.class),targets,Mockito.mock(SmtAuthOperationAttemptMapper.class),Mockito.mock(SmtAuthResultEventMapper.class));
        List<Long> ids=new ArrayList<>();for(long i=1;i<=201;i++)ids.add(i);
        try {service.claim(AuthOperationClaimCommand.builder().parkId(7).operationQueue("AUTH").accessType("ISC").targetIds(ids).maxCount(200).leaseSeconds(30L).build());Assert.fail("Oracle精确IN集合必须限制200");}catch(IllegalArgumentException expected) { }
        Mockito.verifyZeroInteractions(targets);
    }
    @Test public void emptySelectionDoesNotClaimUnrelatedTargets() throws Exception {
        SmtAuthOperationTargetMapper targets=Mockito.mock(SmtAuthOperationTargetMapper.class);
        AuthOperationService service=new AuthOperationService(Mockito.mock(SmtAuthOperationBatchMapper.class),
            Mockito.mock(SmtAuthDeleteRequestMapper.class),targets,Mockito.mock(SmtAuthOperationAttemptMapper.class),
            Mockito.mock(SmtAuthResultEventMapper.class));
        Object builder=AuthOperationClaimCommand.builder().parkId(7).operationQueue("ADD").maxCount(200).leaseSeconds(30L);
        Method method;
        try { method=builder.getClass().getMethod("targetIds",List.class); }
        catch(NoSuchMethodException missing) { Assert.fail("领取命令尚不能表达精确空集合，会退回广查");return; }
        method.invoke(builder,Collections.emptyList());
        builder.getClass().getMethod("accessType",String.class).invoke(builder,"ISC");
        Assert.assertTrue(service.claim((AuthOperationClaimCommand)builder.getClass().getMethod("build").invoke(builder)).isEmpty());
        Mockito.verifyZeroInteractions(targets);
    }
}
