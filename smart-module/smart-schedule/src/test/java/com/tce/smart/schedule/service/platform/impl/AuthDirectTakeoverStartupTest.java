package com.tce.smart.schedule.service.platform.impl;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.service.impl.AuthOperationDirectTakeoverService;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import org.junit.*;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
/** DIRECT 仅在持久能力读取通过后创建线程。 */
public class AuthDirectTakeoverStartupTest {
    @Test public void missingOrRejectedCapabilityCreatesNoExecutors() {
        for(boolean installed:new boolean[]{false,true}) {
            AuthOperationScheduler s=scheduler("DIRECT");if(installed){AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);Mockito.doThrow(new IllegalStateException("cap0")).when(gate).assertDirectSendEnabled(7,"owner");s.setDirectTakeover(gate);}
            try{s.start();Assert.fail();}catch(IllegalStateException expected){}
            Assert.assertTrue(((Map<?,?>)ReflectionTestUtils.getField(s,"executors")).isEmpty());s.stop();
        }
    }
    @Test public void activatedRouteChecksExactOwnerAndIscIsUnaffected() {
        AuthOperationScheduler direct=scheduler("DIRECT");AuthOperationDirectTakeoverService gate=Mockito.mock(AuthOperationDirectTakeoverService.class);direct.setDirectTakeover(gate);
        try{direct.start();Mockito.verify(gate).assertDirectSendEnabled(7,"owner");Assert.assertEquals(6,((Map<?,?>)ReflectionTestUtils.getField(direct,"executors")).size());}finally{direct.stop();}
        AuthOperationScheduler isc=scheduler("ISC");try{isc.start();Assert.assertEquals(6,((Map<?,?>)ReflectionTestUtils.getField(isc,"executors")).size());}finally{isc.stop();}
    }
    private AuthOperationScheduler scheduler(String access) {
        AuthOperationSchedulerProperties p=new AuthOperationSchedulerProperties();p.setEnabled(true);AuthOperationSchedulerProperties.Instance i=new AuthOperationSchedulerProperties.Instance();i.setId("owner");i.setAccessType(access);i.setParks(Collections.singletonList(7));p.getInstances().add(i);
        AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(7));return new AuthOperationScheduler(p,core,null,null,null,null);
    }
}
