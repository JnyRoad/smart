package com.tce.smart.schedule.service.platform.impl;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** 仅供容量验收：请求是样本单位，保留拒绝、错误、未外发和超时，不以目标数冒充样本数。 */
final class AuthOperationSloSamples {
 static final class Sample {
  final String requestId,subjectId,deviceId,action,access;final long planned;
  volatile long entered=-1,returned=-1,firstPhysical=-1,ack=-1,converged=-1;
  volatile Integer status;volatile String error,operationKey,physicalPhase,physicalRequestKey;volatile Long physicalBatch;
  final Set<Long> batches=new LinkedHashSet<>();volatile boolean timeout,generatorRejected;
  List<String> subjectIds,deviceIds;
  Sample(String id,String subject,String device,String action,String access,long planned){this.requestId=id;this.subjectId=subject;this.deviceId=device;this.action=action;this.access=access;this.planned=planned;}
  synchronized void started(long now){entered=now;}
  synchronized void scope(List<String> subjects,List<String> devices){subjectIds=new ArrayList<>(subjects);deviceIds=new ArrayList<>(devices);}
  synchronized void response(long now,int status,String operation,Collection<Long> batchIds){returned=now;this.status=status;operationKey=operation;batches.addAll(batchIds);}
  synchronized void physical(long now,String subject,String device,String phase,String requestKey,long batch){
   if(!Objects.equals(subjectId,subject) || !Objects.equals(deviceId,device) || entered<0 || now<entered || requestKey==null)return;
   if(!("DIRECT".equals(access)?"DIRECT_SEND":"ISC_CONFIG").equals(phase))return;
   if(firstPhysical<0){firstPhysical=now;physicalPhase=phase;physicalRequestKey=requestKey;physicalBatch=batch;}
  }
  synchronized Map<String,Object> row(long now){
   Map<String,Object> row=new LinkedHashMap<>();row.put("requestId",requestId);row.put("subjectId",subjectId);row.put("deviceId",deviceId);row.put("subjects",subjectIds==null?1:subjectIds.size());row.put("devices",deviceIds==null?1:deviceIds.size());row.put("subjectIds",subjectIds);row.put("deviceIds",deviceIds);row.put("action",action);row.put("access",access);
   row.put("plannedNanos",planned);row.put("enteredNanos",nullable(entered));row.put("returnedNanos",nullable(returned));row.put("firstPhysicalNanos",nullable(firstPhysical));
   row.put("generatorDelayMs",elapsed(planned,entered));row.put("responseMs",elapsed(entered,returned));row.put("firstPhysicalMs",elapsed(entered,firstPhysical));row.put("responseToPhysicalMs",returned<0 || firstPhysical<0?null:TimeUnit.NANOSECONDS.toMillis(firstPhysical-returned));
   row.put("firstPhysicalPhase",physicalPhase);row.put("physicalRequestKey",physicalRequestKey);row.put("physicalBatch",physicalBatch);row.put("operationKey",operationKey);row.put("batchIds",new ArrayList<>(batches));
   row.put("httpStatus",status);row.put("error",error);row.put("timeout",timeout);row.put("generatorRejected",generatorRejected);row.put("ackNanos",nullable(ack));row.put("convergedNanos",nullable(converged));
   boolean correlated=operationKey!=null && physicalBatch!=null && batches.contains(physicalBatch);
   boolean successful=correlated && Integer.valueOf(200).equals(status) && error==null && !timeout && !generatorRejected;
   row.put("correlationVerified",correlated);row.put("successful",successful);row.put("censoredLowerBoundMs",firstPhysical<0?elapsed(entered,now):null);return row;
  }
 }
 private static Long nullable(long n){return n<0?null:n;}
 private static Long elapsed(long from,long to){return from<0 || to<0?null:TimeUnit.NANOSECONDS.toMillis(to-from);}
 static long percentile(List<Long> values,double percentile){
  if(values.isEmpty() || percentile<=0 || percentile>1)throw new IllegalArgumentException("分位数需要非空样本和合法概率");
  List<Long> ordered=new ArrayList<>(values);Collections.sort(ordered);return ordered.get((int)Math.ceil(percentile*ordered.size())-1);
 }
 static Map<String,Object> fairness(List<Sample> samples,long now){
  List<Map<String,Object>> rows=new ArrayList<>();List<Long> latencies=new ArrayList<>();int failures=0,missing=0;
  for(Sample s:samples){Map<String,Object> row=s.row(now);rows.add(row);Long latency=(Long)row.get("firstPhysicalMs");if(latency==null)missing++;else latencies.add(latency);if(!Boolean.TRUE.equals(row.get("successful")))failures++;}
  Map<String,Object> report=new LinkedHashMap<>();report.put("plannedRequests",samples.size());report.put("physicalSamples",latencies.size());report.put("missingPhysical",missing);report.put("failedRequests",failures);report.put("samples",rows);report.put("percentileDefinition","nearest-rank ceil(p*n), unit=request; missing/errors retained and forbid PASS");
  if(!latencies.isEmpty()){report.put("observedP50Ms",percentile(latencies,.50));report.put("observedP95Ms",percentile(latencies,.95));report.put("observedP99Ms",percentile(latencies,.99));report.put("observedMaxMs",percentile(latencies,1));}
  boolean exceeds=!latencies.isEmpty() && (percentile(latencies,.95)>30000 || percentile(latencies,1)>120000);
  report.put("metricVerdict",failures>0 || exceeds?"FAIL":samples.size()>=100?"PASS":"UNVERIFIED");return report;
 }
 static Map<String,Object> intake(List<Sample> samples,long now) {
  List<Map<String,Object>> rows=new ArrayList<>();List<Long> latencies=new ArrayList<>();int errors=0,missing=0;
  for(Sample sample:samples){Map<String,Object> row=sample.row(now);rows.add(row);Long elapsed=(Long)row.get("responseMs");if(elapsed==null)missing++;else latencies.add(elapsed);
   boolean ok=elapsed!=null && sample.status!=null && sample.status==200 && sample.operationKey!=null && !sample.batches.isEmpty() && sample.error==null && !sample.generatorRejected && !sample.timeout;row.put("intakeSuccessful",ok);if(!ok)errors++;}
  Map<String,Object> report=new LinkedHashMap<>();report.put("plannedRequests",samples.size());report.put("responseSamples",latencies.size());report.put("missingResponses",missing);report.put("failedRequests",errors);report.put("samples",rows);report.put("percentileDefinition","nearest-rank ceil(p*n), unit=independent request");
  if(!latencies.isEmpty()){report.put("observedP50Ms",percentile(latencies,.50));report.put("observedP95Ms",percentile(latencies,.95));report.put("observedP99Ms",percentile(latencies,.99));report.put("observedMaxMs",percentile(latencies,1));}
  boolean exceeds=!latencies.isEmpty() && (percentile(latencies,.95)>2000 || percentile(latencies,.99)>5000);
  report.put("metricVerdict",errors>0 || exceeds?"FAIL":samples.size()>=100?"PASS":"UNVERIFIED");return report;
 }
 static Map<String,Object> listRatio(List<Long> idle,List<Long> loaded,int errors) {
  Map<String,Object> report=new LinkedHashMap<>();report.put("idleSamples",idle.size());report.put("loadedSamples",loaded.size());report.put("errors",errors);
  Long base=idle.isEmpty()?null:percentile(idle,.95),load=loaded.isEmpty()?null:percentile(loaded,.95);report.put("idleP95Nanos",base);report.put("loadedP95Nanos",load);
  Double ratio=base==null || base<=0 || load==null?null:(double)load/base;report.put("ratio",ratio);
  report.put("metricVerdict",errors>0 || ratio!=null && ratio>2?"FAIL":ratio==null || idle.size()<100 || loaded.size()<100?"UNVERIFIED":"PASS");return report;
 }
 static final class OpenLoop {
  private final ThreadPoolExecutor workers;private final AtomicInteger active=new AtomicInteger(),peak=new AtomicInteger();
  OpenLoop(int concurrent){
   if(concurrent<1 || concurrent>4)throw new IllegalArgumentException("受理并发最多4");
   workers=new ThreadPoolExecutor(concurrent,concurrent,0,TimeUnit.MILLISECONDS,new SynchronousQueue<>(),r->new Thread(r,"slo-http-admission"),new ThreadPoolExecutor.AbortPolicy());
  }
  boolean offer(Sample sample,Runnable request){
   try {workers.execute(()->{int n=active.incrementAndGet();peak.accumulateAndGet(n,Math::max);try{request.run();}catch(Throwable failure){sample.error=failure.getClass().getName();}finally{active.decrementAndGet();}});return true;}
   catch(RejectedExecutionException full){sample.generatorRejected=true;sample.error="GENERATOR_BACKPRESSURE";return false;}
  }
  boolean stop(long timeout,TimeUnit unit)throws InterruptedException{workers.shutdown();return workers.awaitTermination(timeout,unit);}
  int peak(){return peak.get();}
 }
}
