package com.tce.smart.platform.core.service.impl;
import org.junit.*;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSchedulerData.*;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.mapper.AuthOperationSchedulerMapper;
import org.mockito.Mockito;
import java.util.*;
import java.time.LocalDateTime;

/** 新增洪峰不能借走撤权在途保留，回执独占预算不受新增使用影响。 */
public class AuthOperationSchedulerQuotaTest {
    @Test public void sameParkUnknownAddLeavesRoomForDelete() {
        Policy p=policy();AuthOperationSchedulerMapper mapper=Mockito.mock(AuthOperationSchedulerMapper.class);
        AuthOperationService operations=Mockito.mock(AuthOperationService.class);State state=new State();state.setInstanceId("shared");Job job=new Job();job.setInstanceId("shared");job.setLane("DELETE:7");
        Mockito.when(mapper.lockState("shared")).thenReturn(state);Mockito.when(mapper.lockJob(Mockito.anyString(),Mockito.anyString())).thenReturn(job);
        Route route=new Route();route.setParkId(7);route.setInstanceId("shared");route.setAccessType("ISC");Mockito.when(mapper.routes(Mockito.anyList(),Mockito.anyString())).thenReturn(Collections.singletonList(route));
        Mockito.when(mapper.now()).thenReturn(LocalDateTime.of(2026,9,5,0,0));Mockito.when(mapper.windowKey()).thenReturn("20260905000000");
        Count count=new Count();count.setParkId(7);count.setPriority("ADD");count.setState("VERIFYING");count.setTargetCount(100);Mockito.when(mapper.parkCounts(Mockito.any(Policy.class))).thenReturn(Collections.singletonList(count));
        Candidate candidate=new Candidate();candidate.setId(1L);candidate.setParkId(7);candidate.setDeviceId("healthy");candidate.setOperationQueue("AUTH");candidate.setFairKey("7:healthy");
        Mockito.when(mapper.candidates(Mockito.any(Policy.class),Mockito.anyString(),Mockito.any(),Mockito.any(),Mockito.anyInt())).thenReturn(Collections.singletonList(candidate));
        Mockito.when(mapper.saveState(Mockito.any())).thenReturn(1);Mockito.when(mapper.saveJob(Mockito.any())).thenReturn(1);
        Mockito.when(operations.claim(Mockito.any())).thenReturn(Collections.singletonList(AuthOperationClaimedTarget.builder().targetId(1L).build()));
        Grant grant=new AuthOperationSchedulerService(mapper,operations).reservePark(p,"DELETE",7,200,30);
        Assert.assertNotNull("原100个ADD未知仍占位时，同园区健康撤权必须有保留空间",grant);
    }
    private static Policy policy() {
        Policy p=new Policy();p.setInstanceId("shared");p.setAccessType("ISC");p.setParks(Collections.singletonList(7));
        p.setMaxInflight(400);p.setDeleteInflight(100);p.setAddInflight(50);p.setPerDeviceInflight(25);
        p.setHttpPerSecond(100);p.setAddHttp(25);p.setDeleteHttp(25);p.setConfigHttp(15);p.setReceiptHttp(25);p.setBorrowHttp(10);return p;
    }
    @Test public void saturatedAddLeavesDeleteReservation() {
        Assert.assertEquals(0,AuthOperationSchedulerQuota.inflight(400,300,100,300,200));
        Assert.assertEquals(100,AuthOperationSchedulerQuota.inflight(400,300,50,0,200));
    }
    @Test public void sharedBorrowCannotConsumeOtherLanesReservedHttp() {
        Assert.assertEquals(35,AuthOperationSchedulerQuota.http(100,0,25,0,10,0,10));
        Assert.assertEquals(0,AuthOperationSchedulerQuota.http(100,35,25,35,10,10,10));
        Assert.assertEquals(25,AuthOperationSchedulerQuota.http(100,35,25,0,10,10,10));
    }
    @Test public void exhaustedWindowRejectsSecondWorkerEvenWithLocalBudget() {
        Assert.assertEquals(0,AuthOperationSchedulerQuota.http(100,100,25,0,10,0,10));
    }
    @Test public void offlineBackoffIsBoundedAndJittered() {
        Assert.assertEquals(1100,AuthOperationSchedulerQuota.backoffMillis(1,100));
        Assert.assertEquals(300000,AuthOperationSchedulerQuota.backoffMillis(40,999));
    }
}
