package com.tce.smart.schedule.service.platform.impl;

import org.junit.*;
import java.util.*;
import java.util.concurrent.*;

/** 20k 真实新增冻结期间的 100 次单人单设备撤权；未具备 legacy repeat lane 不判完整验收通过。 */
public class AuthOperationFairnessCapacityTest extends AuthOperationSloFixture {
 @Test public void twentyThousandAddBacklogWithHundredIndependentDeletes() throws Exception {
  List<AuthOperationSloSamples.Sample> samples=new ArrayList<>();
  report.put("scenario","SC002");report.put("legacyRepeatDelivery","UNVERIFIED: production unified Timer has no legacy repeat consumption lane");
  report.put("maintenanceCoverage","real previously accepted attempts awaiting receipt/config progress; not original legacy repeat delivery");
  report.put("backgroundShape","20 independently accepted requests x 50 people x 20 devices = 20000 ADD targets");
  report.put("smallShape","100 independent requests x 1 person x 1 device, 4 healthy shared devices, distinct identities");
  try {
   seedBusiness(1116,20,1016,100);
   // 维护负载从真实受理与真实 Timer 形成，不能以未消费的原始 task 伪造竞争。
   for(int i=0;i<16;i++)maintenanceBatches.addAll(accept(Collections.singletonList(subject(1000+i)),Collections.singletonList(authority(1+i%4)),1,null));
   startTimer();long maintenanceStart=System.nanoTime();
   while(physicalAdds.get()<16 && elapsed(maintenanceStart)<120000){rethrowAsync();Thread.sleep(1000);}
   Assert.assertEquals("维护前置必须有16个真实已发送目标",16,physicalAdds.get());stopPipeline();
   report.put("maintenanceBatches",new ArrayList<>(maintenanceBatches));
   Assert.assertEquals("真实维护任务仍等待受控回执",Integer.valueOf(16),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND STATE='ACCEPTED' AND PHASE IN ('DIRECT_SEND','ISC_CONFIG')",Integer.class,park));
   long backgroundStart=System.nanoTime();
   for(int offset=0;offset<1000;offset+=50) {
    List<String> subjects=new ArrayList<>();for(int i=offset;i<offset+50;i++)subjects.add(subject(i));
    backgroundBatches.addAll(accept(subjects,Collections.singletonList(authority(0)),1,null));
   }
   report.put("backgroundAcceptMs",elapsed(backgroundStart));report.put("backgroundBatches",new ArrayList<>(backgroundBatches));
   Assert.assertEquals("真实受理的背景资源不能少于20k",Integer.valueOf(20000),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=? AND SUBJECT_ID>=? AND SUBJECT_ID<?",Integer.class,park,subject(0),subject(1000)));
   Assert.assertEquals("背景主体设备必须唯一",Integer.valueOf(20000),jdbc.queryForObject("SELECT COUNT(*) FROM (SELECT SUBJECT_ID,DEVICE_ID FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=? AND SUBJECT_ID>=? AND SUBJECT_ID<? GROUP BY SUBJECT_ID,DEVICE_ID)",Integer.class,park,subject(0),subject(1000)));
   Assert.assertEquals("正式测量前背景不能预先被消费",Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND SUBJECT_ID>=? AND SUBJECT_ID<?",Integer.class,park,subject(0),subject(1000)));
   int addsAtStart=physicalAdds.get();long start=System.nanoTime();report.put("measurementStartNanos",start);report.put("initialAddBacklog",20000);observe();
   requests=new AuthOperationSloSamples.OpenLoop(4);permitReceipts=true;
   for(int i=0;i<100;i++){AuthOperationSloSamples.Sample sample=new AuthOperationSloSamples.Sample("delete-"+i,subject(1016+i),device(i%4),"DELETE",access,start+TimeUnit.MILLISECONDS.toNanos(500L*i));samples.add(sample);samplesBySubject.put(sample.subjectId,sample);}
   startTimer();startObserver();
   for(int i=0;i<100;i++) {
    final int index=i;AuthOperationSloSamples.Sample sample=samples.get(i);
    waitUntil(sample.planned);requests.offer(sample,()->{try{accept(Collections.singletonList(subject(1016+index)),Collections.emptyList(),2,sample);}catch(Throwable failure){sample.error=failure.getClass().getName();}});rethrowAsync();
   }
   long deadline=start+TimeUnit.MILLISECONDS.toNanos(500L*99+120000);
   while(System.nanoTime()<deadline) {
    rethrowAsync();
    boolean done=true;for(AuthOperationSloSamples.Sample sample:samples)if(!sample.generatorRejected && (sample.returned<0 || sample.firstPhysical<0))done=false;
    if(done)break;Thread.sleep(1000);
   }
   Assert.assertTrue("HTTP工作线程必须在收尾前退出",requests.stop(35,TimeUnit.SECONDS));
   stopObserver();long end=System.nanoTime();for(AuthOperationSloSamples.Sample sample:samples)if(sample.firstPhysical<0 || sample.returned<0)sample.timeout=true;
   report.put("measurementEndNanos",end);report.put("generatorConcurrencyPeak",requests.peak());report.put("addPhysicalProgressDuringDeletes",physicalAdds.get()-addsAtStart);
   int maintenanceConverged=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? AND SUBJECT_ID>=? AND SUBJECT_ID<? AND STATE='CONVERGED'",Integer.class,park,subject(1000),subject(1016));
   int backgroundConfirmed=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND SUBJECT_ID>=? AND SUBJECT_ID<? AND STATE IN ('CONFIRMED','CONVERGED')",Integer.class,park,subject(0),subject(1000));
   report.put("maintenanceConvergedDuringDeletes",maintenanceConverged);report.put("backgroundConfirmedDuringDeletes",backgroundConfirmed);
   Map<String,Object> metric=AuthOperationSloSamples.fairness(samples,end);report.put("fairnessMetric",metric);
   Assert.assertTrue("小删除测量中新增必须实际推进",physicalAdds.get()>addsAtStart);
   Assert.assertTrue("新增推进必须包含可信确认",backgroundConfirmed>0);Assert.assertTrue("旧受理维护任务必须实际收敛，空维护查询不算负载",maintenanceConverged>0);
   Assert.assertEquals("小撤权时延门槛", "PASS",metric.get("metricVerdict"));
   // 时延通过仅是局部证据，当前生产缺失的原始历史投递要求仍未验证。
   report.put("verdict","UNVERIFIED");report.put("completedEvidence","SC002 latency and maintenance contention only");
  }catch(Throwable failure){report.put("verdict","FAIL");report.put("failure",failure.getClass().getName()+": "+failure.getMessage());throw failure;}
  finally {stopObserver();stopPipeline();for(AuthOperationSloSamples.Sample sample:samples)if(sample.firstPhysical<0 || sample.returned<0)sample.timeout=true;report.put("fairnessMetric",AuthOperationSloSamples.fairness(samples,System.nanoTime()));writeReport();if(requests!=null)Assert.assertTrue(requests.stop(35,TimeUnit.SECONDS));}
 }
 private static long elapsed(long start){return TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);}
 private static void waitUntil(long planned) throws InterruptedException {long remaining;while((remaining=planned-System.nanoTime())>0)TimeUnit.NANOSECONDS.sleep(remaining);}
}
