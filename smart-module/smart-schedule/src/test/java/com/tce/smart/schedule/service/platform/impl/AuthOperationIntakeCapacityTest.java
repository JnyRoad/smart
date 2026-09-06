package com.tce.smart.schedule.service.platform.impl;

import org.junit.*;
import java.util.*;
import java.util.concurrent.*;

/** 200 次真实业务受理和两个真实普通列表的同数据基线/负载对照；禁止把20k目标当20k样本。 */
public class AuthOperationIntakeCapacityTest extends AuthOperationSloFixture {
 private final List<Map<String,Object>> listSamples=Collections.synchronizedList(new ArrayList<>());
 private ExecutorService listWorkers;
 private boolean seededPark,seededOrganization;
 @Override protected String companyId(){return String.valueOf(5000000000000L+park);}
 @Test public void twoHundredRequestsAndRealNormalLists() throws Exception {
  List<AuthOperationSloSamples.Sample> samples=new ArrayList<>();report.put("scenario","SC003");report.put("normalListSamples",listSamples);
  report.put("requestShape","200 independent requests x 5 subjects x 20 devices = 20000 targets; maximum 4 concurrent requests; fixed 250ms plans");
  report.put("largeSingleJson","UNVERIFIED: separate 10k single request diagnostic is not this population");
  try {
   AuthOperationNormalListProbe.requireSchema(this);seedBusiness(1000,20,0,0);
   Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PARK WHERE ID=?",Integer.class,park));
   jdbc.update("INSERT INTO SMT_PARK(ID,PARK_NAME) VALUES(?,?)",park,"slo-park-"+park);seededPark=true;
   jdbc.update("INSERT INTO SMT_ORGANIZE_RELATION(ID,PARK_ID,COMP_NAME,COMP_ID) VALUES(?,?,?,?)",Long.valueOf(companyId()),park,"合成组织",companyId());seededOrganization=true;
   jdbc.update("UPDATE SMT_DEVICE_AUTHORITY SET AUTHORITY_NAME=? WHERE PARK_ID=?","合成权限",park);
   AuthOperationNormalListProbe probe=new AuthOperationNormalListProbe(this,1000);
   // 预热与基线单列；两端点的负载阶段使用完全相同的参数、数据和计划间隔。
   List<Future<?>> warm=launchLists(probe,"warmup",20,System.nanoTime());awaitLists(warm);
   List<Future<?>> idle=launchLists(probe,"idle",200,System.nanoTime());awaitLists(idle);
   assertListSamples("idle",200);
   long start=System.nanoTime();report.put("loadStartNanos",start);permitReceipts=true;startTimer();startObserver();requests=new AuthOperationSloSamples.OpenLoop(4);
   List<Future<?>> loaded=launchLists(probe,"loaded",200,start);
   List<String> allDevices=new ArrayList<>();for(int d=0;d<20;d++)allDevices.add(device(d));
   for(int i=0;i<200;i++) {
    List<String> ids=new ArrayList<>();for(int j=0;j<5;j++)ids.add(subject(i*5+j));
    AuthOperationSloSamples.Sample sample=new AuthOperationSloSamples.Sample("intake-"+i,ids.get(0),device(0),"ADD",access,start+TimeUnit.MILLISECONDS.toNanos(250L*i));sample.scope(ids,allDevices);samples.add(sample);samplesBySubject.put(sample.subjectId,sample);
   }
   for(AuthOperationSloSamples.Sample sample:samples) {
    waitUntil(sample.planned);requests.offer(sample,()->{try{accept(sample.subjectIds,Collections.singletonList(authority(0)),1,sample);}catch(Throwable failure){sample.error=failure.getClass().getName();}});rethrowAsync();
   }
   Assert.assertTrue("受理线程未退出",requests.stop(35,TimeUnit.SECONDS));long injectionEnd=System.nanoTime();report.put("injectionEndNanos",injectionEnd);report.put("actualInjectionWindowMs",TimeUnit.NANOSECONDS.toMillis(injectionEnd-start));report.put("generatorConcurrencyPeak",requests.peak());
   awaitLists(loaded);stopObserver();observe();rethrowAsync();report.put("loadEndNanos",System.nanoTime());
   for(AuthOperationSloSamples.Sample sample:samples)if(sample.returned<0)sample.timeout=true;
   Map<String,Object> intake=AuthOperationSloSamples.intake(samples,System.nanoTime());report.put("intakeMetric",intake);Map<String,Object> ratio=listMetrics();report.put("listMetrics",ratio);
   Assert.assertEquals("真实受理时延门槛","PASS",intake.get("metricVerdict"));
   Assert.assertTrue("注入窗口门槛不得隐藏",TimeUnit.NANOSECONDS.toMillis(injectionEnd-start)<=60000);
   Assert.assertEquals("独立20k受理不得漏目标",Integer.valueOf(20000),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",Integer.class,park));
   Assert.assertTrue("负载阶段必须真正外发并推进",physicalAdds.get()>0);Assert.assertTrue("真实Timer必须推进",timerTicks.get()>0);
   assertListSamples("loaded",200);for(Object value:ratio.values())Assert.assertEquals("同数据普通列表负载P95不得超过基线2倍","PASS",((Map<?,?>)value).get("metricVerdict"));
   report.put("verdict","PASS_CONTROLLED_MVC_DB");report.put("notCovered",Arrays.asList("real HTTP socket/login stack","10k single JSON latency","production terminal acknowledgement"));
  }catch(Throwable failure){report.put("verdict","FAIL");report.put("failure",failure.getClass().getName()+": "+failure.getMessage());throw failure;}
  finally {stopLists();stopObserver();stopPipeline();if(requests!=null)Assert.assertTrue(requests.stop(35,TimeUnit.SECONDS));for(AuthOperationSloSamples.Sample sample:samples)if(sample.returned<0)sample.timeout=true;report.put("intakeMetric",AuthOperationSloSamples.intake(samples,System.nanoTime()));report.put("listMetrics",listMetrics());writeReport();}
 }
 private List<Future<?>> launchLists(AuthOperationNormalListProbe probe,String phase,int count,long start) {
  if(listWorkers==null)listWorkers=Executors.newFixedThreadPool(2,r->new Thread(r,"slo-normal-list-"+park));
  List<Future<?>> result=new ArrayList<>();for(String endpoint:Arrays.asList("staff","authority")) {
   List<Map<String,Object>> plans=new ArrayList<>();for(int i=0;i<count;i++){Map<String,Object> row=new LinkedHashMap<>();row.put("endpoint",endpoint);row.put("phase",phase);row.put("sample",i);row.put("plannedNanos",start+TimeUnit.SECONDS.toNanos(i));row.put("contentVerified",false);row.put("error","NOT_ENTERED");plans.add(row);listSamples.add(row);}
   result.add(listWorkers.submit(()->{for(int i=0;i<count;i++){Map<String,Object> row=plans.get(i);long planned=(Long)row.get("plannedNanos");try{waitUntil(planned);Map<String,Object> response=probe.request(endpoint,phase,i,planned);row.clear();row.putAll(response);}catch(InterruptedException failure){row.put("error","GENERATOR_CANCELLED");Thread.currentThread().interrupt();throw new IllegalStateException(failure);}}}));
  }return result;
 }
 private static void awaitLists(List<Future<?>> futures) throws Exception {for(Future<?> future:futures)future.get(240,TimeUnit.SECONDS);}
 private void stopLists() throws InterruptedException {if(listWorkers!=null){listWorkers.shutdown();if(!listWorkers.awaitTermination(35,TimeUnit.SECONDS)){listWorkers.shutdownNow();Assert.assertTrue("普通列表线程未退出，禁止数据清理",listWorkers.awaitTermination(35,TimeUnit.SECONDS));}}}
 @Override protected void stopExtraThreads() throws Exception {stopLists();}
 private void assertListSamples(String phase,int count) {
  for(String endpoint:Arrays.asList("staff","authority")){int n=0;for(Map<String,Object> row:listSamples)if(endpoint.equals(row.get("endpoint")) && phase.equals(row.get("phase"))){n++;Assert.assertEquals("普通列表必须真实且无错误",true,row.get("contentVerified"));}Assert.assertEquals(count,n);}
 }
 private Map<String,Object> listMetrics() {
  Map<String,Object> result=new LinkedHashMap<>();synchronized(listSamples){for(String endpoint:Arrays.asList("staff","authority")) {
   List<Long> idle=new ArrayList<>(),loaded=new ArrayList<>();int errors=0;
   for(Map<String,Object> row:listSamples)if(endpoint.equals(row.get("endpoint")) && !"warmup".equals(row.get("phase"))) {
    if(!Boolean.TRUE.equals(row.get("contentVerified"))){errors++;continue;}
    ("idle".equals(row.get("phase"))?idle:loaded).add(((Number)row.get("durationNanos")).longValue());
   }
   result.put(endpoint,AuthOperationSloSamples.listRatio(idle,loaded,errors));
  }}return result;
 }
 @Override protected void cleanupExtraBusiness() {
  jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (SELECT ID FROM SMT_STAFF WHERE COMP_ID=?)",companyId());
  jdbc.update("DELETE FROM SMT_STAFF WHERE COMP_ID=?",companyId());
  if(seededOrganization)jdbc.update("DELETE FROM SMT_ORGANIZE_RELATION WHERE ID=? AND PARK_ID=?",Long.valueOf(companyId()),park);
  if(seededPark)jdbc.update("DELETE FROM SMT_PARK WHERE ID=?",park);
 }
 private static void waitUntil(long planned) throws InterruptedException {long remaining;while((remaining=planned-System.nanoTime())>0)TimeUnit.NANOSECONDS.sleep(remaining);}
}
