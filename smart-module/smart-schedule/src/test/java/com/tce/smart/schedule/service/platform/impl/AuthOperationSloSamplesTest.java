package com.tce.smart.schedule.service.platform.impl;

import org.junit.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static com.tce.smart.schedule.service.platform.impl.AuthOperationSloSamples.*;

public class AuthOperationSloSamplesTest {
 @Test public void nearestRankUsesRequestsNotTargetsAndKeepsTail() {
  List<Long> hundred=new ArrayList<>();for(long i=1;i<=100;i++)hundred.add(i);
  Assert.assertEquals(95,percentile(hundred,.95));Assert.assertEquals(99,percentile(hundred,.99));Assert.assertEquals(100,percentile(hundred,1));
 }
 @Test public void identityLookupCannotStartDeleteLatency() {
  Sample sample=sample("one");sample.started(1000000);sample.physical(2000000,"s-one","d","ISC_PERSON_GET","lookup",7);
  Assert.assertEquals(-1,sample.firstPhysical);sample.physical(8000000,"s-one","d","ISC_CONFIG","config",7);
  Assert.assertEquals(8000000,sample.firstPhysical);
 }
 @Test public void unrelatedSubjectDeviceOrWrongProtocolCannotMatch() {
  Sample sample=sample("one");sample.started(1000000);
  sample.physical(2000000,"other","d","ISC_CONFIG","k",7);sample.physical(3000000,"s-one","other","ISC_CONFIG","k",7);sample.physical(4000000,"s-one","d","DIRECT_SEND","k",7);
  Assert.assertEquals(-1,sample.firstPhysical);
 }
 @Test public void submissionBeforeResponseIsPreservedAndDuplicateDoesNotRestartClock() {
  Sample sample=sample("one");sample.started(1000000);sample.physical(3000000,"s-one","d","ISC_CONFIG","first",7);sample.response(5000000,200,"op",Collections.singleton(7L));sample.physical(9000000,"s-one","d","ISC_CONFIG","retry",7);
  Assert.assertEquals(3000000,sample.firstPhysical);Assert.assertEquals("first",sample.physicalRequestKey);
  Assert.assertEquals(Boolean.TRUE,sample.row(10000000).get("correlationVerified"));Assert.assertEquals(2L,sample.row(10000000).get("firstPhysicalMs"));
 }
 @Test public void missingAndTimeoutSamplesCannotDisappearFromPercentiles() {
  List<Sample> all=new ArrayList<>();for(int i=0;i<100;i++){Sample s=sample(String.valueOf(i));s.started(1000000);s.response(2000000,200,"op-"+i,Collections.singleton((long)i));if(i<99)s.physical(3000000,s.subjectId,"d","ISC_CONFIG","p"+i,i);all.add(s);}
  all.get(99).timeout=true;Map<String,Object> report=fairness(all,TimeUnit.SECONDS.toNanos(121));
  Assert.assertEquals(100,report.get("plannedRequests"));Assert.assertEquals(99,report.get("physicalSamples"));Assert.assertEquals(1,report.get("missingPhysical"));Assert.assertEquals("FAIL",report.get("metricVerdict"));
  Assert.assertEquals(100,((List<?>)report.get("samples")).size());
 }
 @Test public void oneRequestWithManyTargetsCannotClaimSloDistribution() {
  Sample s=sample("one");s.started(1000000);s.response(2000000,200,"op",Collections.singleton(7L));s.physical(3000000,s.subjectId,"d","ISC_CONFIG","k",7);
  Assert.assertEquals("UNVERIFIED",fairness(Collections.singletonList(s),4000000).get("metricVerdict"));
 }
 @Test public void httpErrorAndWrongBatchAreFailuresEvenWhenExternalEventExists() {
  Sample s=sample("one");s.started(1000000);s.physical(2000000,s.subjectId,"d","ISC_CONFIG","k",7);s.response(3000000,500,"op",Collections.singleton(8L));
  Map<String,Object> row=s.row(4000000);Assert.assertEquals(Boolean.FALSE,row.get("correlationVerified"));Assert.assertEquals(Boolean.FALSE,row.get("successful"));
 }
 @Test public void boundedOpenLoopRetainsRejectedPlansAndNeverExceedsFourWorkers() throws Exception {
  OpenLoop loop=new OpenLoop(4);CountDownLatch entered=new CountDownLatch(4),release=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger();List<Sample> all=new ArrayList<>();
  try {
   for(int i=0;i<4;i++){Sample s=sample("accepted"+i);all.add(s);Assert.assertTrue(loop.offer(s,()->{calls.incrementAndGet();entered.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}}));}
   Assert.assertTrue(entered.await(2,TimeUnit.SECONDS));Sample rejected=sample("overflow");all.add(rejected);Assert.assertFalse(loop.offer(rejected,()->Assert.fail("拥塞请求不能暗中排队")));
   Assert.assertTrue(rejected.generatorRejected);Assert.assertNotNull(rejected.error);Assert.assertEquals(4,loop.peak());Assert.assertEquals(4,calls.get());Assert.assertEquals(5,all.size());
  }finally{release.countDown();Assert.assertTrue(loop.stop(5,TimeUnit.SECONDS));}
 }
 @Test public void intakeDistributionUsesResponsesAndNeedsNoDeviceAck() {
  List<Sample> all=new ArrayList<>();for(int i=0;i<200;i++){Sample s=sample("in"+i);s.started(1000000);s.response(1000000+TimeUnit.MILLISECONDS.toNanos(i<198?1900:4900),200,"op"+i,Collections.singleton((long)i));all.add(s);}
  Map<String,Object> result=intake(all,TimeUnit.SECONDS.toNanos(6));Assert.assertEquals("PASS",result.get("metricVerdict"));Assert.assertEquals(200,result.get("plannedRequests"));Assert.assertEquals(1900L,result.get("observedP99Ms"));
 }
 @Test public void intakeKeepsGeneratorRejectionsAndTimeouts() {
  List<Sample> all=new ArrayList<>();for(int i=0;i<100;i++){Sample s=sample("in"+i);s.started(0);s.response(1000000,200,"op"+i,Collections.singleton((long)i));all.add(s);}
  all.get(99).generatorRejected=true;all.get(99).returned=-1;
  Map<String,Object> result=intake(all,2000000);Assert.assertEquals("FAIL",result.get("metricVerdict"));Assert.assertEquals(1,result.get("missingResponses"));Assert.assertEquals(100,((List<?>)result.get("samples")).size());
 }
 @Test public void listRatioRequiresBothRealPopulationsAndRetainsFailures() {
  List<Long> idle=new ArrayList<>(),loaded=new ArrayList<>();for(int i=0;i<200;i++){idle.add(100L);loaded.add(201L);}
  Assert.assertEquals("FAIL",listRatio(idle,loaded,0).get("metricVerdict"));Collections.fill(loaded,199L);Assert.assertEquals("PASS",listRatio(idle,loaded,0).get("metricVerdict"));
  Assert.assertEquals("FAIL",listRatio(idle,loaded,1).get("metricVerdict"));Assert.assertEquals("UNVERIFIED",listRatio(idle.subList(0,1),loaded,0).get("metricVerdict"));
 }
 @Test public void directWireMismatchCannotBecomeSuccessfulPhysicalSample() {
  for(String wrong:Arrays.asList("dispatcherDevice","deviceCode","cardNo","reqId","serialNo","action")) {
   AuthOperationSloFixture fixture=wireFixture();Sample sample=new Sample("wire","subject","device","DELETE","DIRECT",0);sample.started(1);sample.response(2,200,"op",Collections.singleton(7L));fixture.samplesBySubject.put("subject",sample);
   com.tce.smart.dispatcher.api.dto.req.DispatcherDTO<Object> dto=deleteWire();com.tce.smart.platform.api.dto.CardDelDTO data=(com.tce.smart.platform.api.dto.CardDelDTO)dto.getData();
   if("dispatcherDevice".equals(wrong))dto.setDeviceId("other");if("deviceCode".equals(wrong))data.setDeviceCode("other");if("cardNo".equals(wrong))data.setCardNo("other");if("reqId".equals(wrong))data.setReqId(999);if("serialNo".equals(wrong))data.setSerialNo("other");if("action".equals(wrong))dto.setEventType(com.tce.smart.dispatcher.api.enums.EventEnum.DEVICE_ADD_CARD.getCode());
   try{fixture.recordPhysical(dto,"DIRECT_SEND",true,3000000);Assert.fail("错误实际载荷不能当预期物理外发: "+wrong);}catch(AssertionError expected){Assert.assertEquals(-1,sample.firstPhysical);}
   Assert.assertEquals(0,fixture.physicalDeletes.get());Assert.assertEquals("FAIL",fairness(Collections.singletonList(sample),4000000).get("metricVerdict"));
  }
 }
 @Test public void validDirectWireRecordsActualIdentityOnlyAfterFullCorrelation() {
  AuthOperationSloFixture fixture=wireFixture();Sample sample=new Sample("wire","subject","device","DELETE","DIRECT",0);sample.started(1);sample.response(2,200,"op",Collections.singleton(7L));fixture.samplesBySubject.put("subject",sample);
  fixture.recordPhysical(deleteWire(),"DIRECT_SEND",true,3000000);Assert.assertEquals(3000000,sample.firstPhysical);Assert.assertEquals(1,fixture.physicalDeletes.get());Assert.assertEquals("subject",fixture.externalEvents.get(0).get("wireCardNo"));Assert.assertEquals("device",fixture.externalEvents.get(0).get("wireDeviceCode"));
 }
 @Test public void lateShutdownErrorFailsAfterExactCleanupAndFinalReport() throws Exception {
  AtomicInteger deletes=new AtomicInteger();Map<String,Object> saved=new HashMap<>();AuthOperationSloFixture fixture=new AuthOperationSloFixture(){@Override protected void writeReport(){saved.clear();saved.putAll(report);}};
  fixture.park=123;fixture.jdbc=new org.springframework.jdbc.core.JdbcTemplate(){@Override public int update(String sql,Object... args){Assert.assertTrue(sql.startsWith("DELETE FROM "));deletes.incrementAndGet();return 0;}};fixture.pool=org.mockito.Mockito.mock(com.zaxxer.hikari.HikariDataSource.class);fixture.report.put("verdict","PASS_CONTROLLED_MVC_DB");
  ScheduledExecutorService driver=Executors.newSingleThreadScheduledExecutor();CountDownLatch entered=new CountDownLatch(1);AtomicBoolean afterShutdown=new AtomicBoolean();org.springframework.test.util.ReflectionTestUtils.setField(fixture,"timerDriver",driver);
  driver.schedule(()->{entered.countDown();try{long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);while(!driver.isShutdown() && System.nanoTime()<deadline)Thread.sleep(1);afterShutdown.set(driver.isShutdown());throw new IllegalStateException("shutdown callback failed");}catch(Throwable error){fixture.asynchronousFailure.set(error);}},0,TimeUnit.MILLISECONDS);
  try{Assert.assertTrue(entered.await(2,TimeUnit.SECONDS));fixture.rethrowAsync();try{fixture.cleanup();Assert.fail("停机期间捕获的错必须使JUnit失败");}catch(AssertionError expected){Assert.assertEquals("FAIL",saved.get("verdict"));Assert.assertNotNull(expected.getCause());}Assert.assertTrue(afterShutdown.get());Assert.assertTrue(driver.isTerminated());Assert.assertTrue(deletes.get()>0);org.mockito.Mockito.verify(fixture.pool).close();}
  finally{driver.shutdownNow();Assert.assertTrue(driver.awaitTermination(2,TimeUnit.SECONDS));}
 }
 private static AuthOperationSloFixture wireFixture(){AuthOperationSloFixture fixture=new AuthOperationSloFixture(){};fixture.park=123;fixture.instanceId="capacity-123";fixture.jdbc=new org.springframework.jdbc.core.JdbcTemplate(){@Override public List<Map<String,Object>> queryForList(String sql,Object... args){Map<String,Object> row=new HashMap<>();row.put("TARGET_ID",7L);row.put("BATCH_ID",7L);row.put("SUBJECT_ID","subject");row.put("DEVICE_ID","device");row.put("CARD_NO","subject");row.put("TASK_ID","55");row.put("SERIAL_NO","serial");row.put("ACTION","DELETE");return Collections.singletonList(row);}};return fixture;}
 private static com.tce.smart.dispatcher.api.dto.req.DispatcherDTO<Object> deleteWire(){com.tce.smart.platform.api.dto.CardDelDTO data=new com.tce.smart.platform.api.dto.CardDelDTO();data.setCardNo("subject");data.setDeviceCode("device");data.setReqId(55);data.setSerialNo("serial");com.tce.smart.dispatcher.api.dto.req.DispatcherDTO<Object> dto=new com.tce.smart.dispatcher.api.dto.req.DispatcherDTO<>();dto.setParkId(123);dto.setDeviceId("device");dto.setEventId("key");dto.setEventType(com.tce.smart.dispatcher.api.enums.EventEnum.DEVICE_DELETE_CARD.getCode());dto.setData(data);return dto;}
 private Sample sample(String id){return new Sample(id,"s-"+id,"d","DELETE","ISC",0);}
}
