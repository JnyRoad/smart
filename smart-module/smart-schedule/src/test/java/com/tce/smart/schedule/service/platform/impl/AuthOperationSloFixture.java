package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.CardDTO;
import com.tce.smart.platform.api.dto.CardDelDTO;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import com.tce.smart.schedule.task.AuthOperationTimerTask;
import org.junit.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.mockito.Mockito.*;

/** 足量 SLO 专用夹具；复用真实事务装配，不改变既有容量测试和生产预算。 */
public abstract class AuthOperationSloFixture extends AuthOperationCapacityFixture {
 protected final Map<String,Object> report=new LinkedHashMap<>();
 protected final Map<String,AuthOperationSloSamples.Sample> samplesBySubject=new ConcurrentHashMap<>();
 protected final List<Map<String,Object>> observations=new ArrayList<>();
 protected final AtomicReference<Throwable> asynchronousFailure=new AtomicReference<>();
 protected final AtomicInteger timerTicks=new AtomicInteger(),physicalAdds=new AtomicInteger(),physicalDeletes=new AtomicInteger(),acknowledgements=new AtomicInteger();
 protected final List<Map<String,Object>> externalEvents=Collections.synchronizedList(new ArrayList<>());
 protected final Set<Long> maintenanceBatches=ConcurrentHashMap.newKeySet(),backgroundBatches=ConcurrentHashMap.newKeySet();
 protected AuthOperationSloSamples.OpenLoop requests;
 protected String access,instanceId;
 protected volatile boolean permitReceipts;
 protected long origin;
 private AuthOperationScheduler scheduler;
 private IscSloRemote iscRemote;
 private ScheduledExecutorService timerDriver,heapSampler,observer;
 private org.springframework.test.web.servlet.MockMvc httpEntry;
 private final AtomicLong maxHeap=new AtomicLong(),maxActive=new AtomicLong(),maxPending=new AtomicLong();
 private volatile boolean mayClean=true;

 @Override @Before public void setup() throws Exception {
  Assume.assumeTrue("足量 SLO 必须显式独占启用", "true".equals(System.getenv("SMART_AUTH_SLO_RUN")));
  access=System.getenv("SMART_AUTH_SLO_ACCESS");Assert.assertTrue("必须明确接入类型",Arrays.asList("DIRECT","ISC").contains(access));
  String reportPath=System.getenv("SMART_AUTH_SLO_REPORT");Assert.assertNotNull("必须先提供独立报告路径",reportPath);Assert.assertTrue(Paths.get(reportPath).isAbsolute());
  super.setup();discardBaseSeed();instanceId="capacity-"+park;origin=System.nanoTime();
  com.tce.smart.platform.controller.SmtStaffDeviceAuthController controller=new com.tce.smart.platform.controller.SmtStaffDeviceAuthController();
  org.springframework.test.util.ReflectionTestUtils.setField(controller,"smtStaffDeviceAuthService",entry);
  httpEntry=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
  report.put("verdict","UNVERIFIED");report.put("access",access);report.put("park",park);report.put("entryBoundary","controlled MockMvc JSON binding + real Adapter/Mapper/Oracle transaction; no socket or login filter");
  report.put("timerBoundary","real AuthOperationTimerTask invoked at fixed delay 1000ms; Spring annotation discovery not exercised");
  report.put("poolMaximum",4);report.put("budgets","production defaults unchanged");report.put("observations",observations);report.put("externalEvents",externalEvents);
  heapSampler=Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"slo-memory-"+park));
  heapSampler.scheduleWithFixedDelay(()->{maxHeap.accumulateAndGet(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory(),Math::max);maxActive.accumulateAndGet(pool.getHikariPoolMXBean().getActiveConnections(),Math::max);maxPending.accumulateAndGet(pool.getHikariPoolMXBean().getThreadsAwaitingConnection(),Math::max);},0,100,TimeUnit.MILLISECONDS);
 }
 private void discardBaseSeed() {
  // 固定容量基线尚无跳过 seed 的接口；仅移除该父夹具刚创建的六行业务前置，未创建任何可靠批次。
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Integer.class,park));
  Assert.assertEquals(1,jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE ID=? AND STAFF_ID=? AND AUTH_ID=?",park,employeeId,park));
  Assert.assertEquals(1,jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE ID=? AND AUTHORITY_ID=? AND DEVICE_ID=? AND PARK_ID=?",park,park,"employee-device-"+park,park));
  Assert.assertEquals(1,jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=? AND PARK_ID=?","employee-device-"+park,park));
  Assert.assertEquals(1,jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE ID=? AND PARK_ID=?",park,park));
  Assert.assertEquals(1,jdbc.update("DELETE FROM SMT_PARK_BU WHERE ID=? AND PARK_ID=? AND COMP_ID=?",park,park,"employee-test-"+park));
  Assert.assertEquals(1,jdbc.update("DELETE FROM SMT_STAFF WHERE ID=? AND COMP_ID=?",employeeId,"employee-test-"+park));
 }
 @Override protected void ensureSelectionSchema() {
  // 先检查父夹具所有自动建表入口；缺前置直接失败，禁止进入其 DDL 分支。
  for(String table:Arrays.asList("SMT_AUTH_SELECTION_SOURCE","SMT_AUTH_SELECTION_RESOURCE","SMT_AUTH_TRANSPORT_PHASE","SMT_AUTH_TRANSPORT_IDENTITY","SMT_AUTH_TRANSPORT_REVIEW","SMT_AUTH_SCHEDULER_JOB","SMT_AUTH_SCHEDULER_ROUTE","SMT_AUTH_SCHEDULER_QUOTA","SMT_AUTH_SCHEDULER_STATE","SMT_PARK_BU"))
   Assert.assertEquals("缺少已部署前置表 "+table,Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME=?",Integer.class,table));
  for(String column:Arrays.asList("PERSON_SNAPSHOT","BUSINESS_SNAPSHOT","SOURCE_KIND","SUBJECT_TYPE"))
   Assert.assertEquals("缺少选择来源列 "+column,Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SELECTION_SOURCE' AND COLUMN_NAME=?",Integer.class,column));
 }
 protected String device(int n){return "slo-device-"+park+"-"+n;}
 protected String companyId(){return "employee-test-"+park;}
 protected void cleanupExtraBusiness() { }
 protected void stopExtraThreads() throws Exception { }
 protected int authority(int n){return -park-n;}
 protected String subject(int n){return String.valueOf(employeeId+n);}
 protected void seedBusiness(int people,int devices,int oldDeleteOffset,int oldDeleteCount) {
  Assert.assertNotEquals(9001,park);
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF WHERE ID>=? AND ID<?",Integer.class,employeeId,employeeId+people));
  jdbc.update("INSERT INTO SMT_PARK_BU(ID,PARK_ID,COMP_ID) VALUES(?,?,?)",park,park,companyId());
  jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) SELECT ?+LEVEL-1,?,1,?,'slo-badge-'||?||'-'||(LEVEL-1),'合成员工'||LPAD(LEVEL,6,'0') FROM DUAL CONNECT BY LEVEL<=?",employeeId,companyId(),"image-ref-"+park,park,people);
  jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,1)",authority(0),park);
  for(int d=0;d<devices;d++) {
   jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC,CHANNEL_NO) VALUES(?,?,?,1)",device(d),park,"ISC".equals(access)?1:0);
   jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",authority(100+d),authority(0),device(d),park);
  }
  for(int d=0;d<4;d++) {
   jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,1)",authority(d+1),park);
   jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",authority(200+d),authority(d+1),device(d),park);
  }
  for(int i=0;i<oldDeleteCount;i++) {
   String id=subject(oldDeleteOffset+i);int d=i%4;
   jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 23:59:59',2)",Long.valueOf(id),authority(d+1));
   if("ISC".equals(access))jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,ACTION,DEVICE_TYPE,SERVICE_TYPE,DEVICE_CODE,CARD_NO,PERSON_ID,BADGE,IMAGE_ID,TASK_TYPE,CREATE_TIME,START_TIME,OVER_TIME) VALUES(?,?,1,1,1,?,?,?,?,?,1,TIMESTAMP '2026-09-01 09:00:00',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 23:59:59')",1000000000000L+park*1000L+i,park,device(d),id,"slo-person-"+id,"slo-badge-"+park+"-"+(oldDeleteOffset+i),"image-ref-"+park);
  }
 }
 protected Set<Long> accept(List<String> subjects,List<Integer> auths,int type,AuthOperationSloSamples.Sample sample) throws Exception {
  UpdateDeviceAuthDTO dto=new UpdateDeviceAuthDTO();dto.setIds(subjects);dto.setDeviceAuthIds(auths);dto.setStartTime("2026-09-01");dto.setEndTime("2026-09-30");
  String json=JSONUtil.toJsonStr(dto);if(sample!=null)sample.started(System.nanoTime());
  org.springframework.mock.web.MockHttpServletResponse response=httpEntry.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/staff/device/auth/updateAuth/"+type).contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(json)).andReturn().getResponse();
  long returned=System.nanoTime();
  if(sample!=null){sample.returned=returned;sample.status=response.getStatus();}
  Assert.assertEquals(200,response.getStatus());Assert.assertTrue("真实受理必须成功",JSONUtil.toBean(response.getContentAsString(),Result.class).isSuccess());
  // 每个请求使用从未受理过的独立主体；只查该主体的精确冻结操作，不能猜最新批次。
  List<Map<String,Object>> rows=jdbc.queryForList("SELECT DISTINCT OPERATION_KEY,BATCH_ID FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? AND SUBJECT_ID=?",park,subjects.get(0));
  Set<String> keys=new HashSet<>();Set<Long> batches=new LinkedHashSet<>();
  for(Map<String,Object> row:rows){keys.add((String)row.get("OPERATION_KEY"));batches.add(((Number)row.get("BATCH_ID")).longValue());}
  Assert.assertEquals("独立主体必须只对应本次受理操作",1,keys.size());Assert.assertFalse(batches.isEmpty());
  if(sample!=null)sample.response(returned,response.getStatus(),keys.iterator().next(),batches);
  return batches;
 }
 protected void startTimer() {
  if(scheduler==null) {
   AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();settings.setEnabled(true);
   AuthOperationSchedulerProperties.Instance instance=new AuthOperationSchedulerProperties.Instance();instance.setId(instanceId);instance.setAccessType(access);instance.setParks(Collections.singletonList(park));settings.getInstances().add(instance);
   AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(park));
   RemoteDispatcherService remote=mock(RemoteDispatcherService.class);SmtImageService images=mock(SmtImageService.class);when(images.getImageBase64ByCode("image-ref-"+park)).thenReturn("c3ludGhldGljLWZhY2U=");
   if("ISC".equals(access) && iscRemote==null)iscRemote=new IscSloRemote();
   IscSloRemote isc=iscRemote;
   when(remote.dispatch(any(),anyString())).thenAnswer(call->{try {
    Assert.assertFalse("外发不应持有事务",TransactionSynchronizationManager.isActualTransactionActive());DispatcherDTO<?> dto=call.getArgument(0);Assert.assertEquals(Integer.valueOf(park),dto.getParkId());
    long called=System.nanoTime();
    if(isc!=null)return isc.dispatch(dto,called);
    Assert.assertTrue(EventEnum.DEVICE_ADD_CARD.getCode().equals(dto.getEventType())||EventEnum.DEVICE_DELETE_CARD.getCode().equals(dto.getEventType()));
    recordPhysical(dto,"DIRECT_SEND",EventEnum.DEVICE_DELETE_CARD.getCode().equals(dto.getEventType()),called);return Result.success(null);
   }catch(Throwable failure){asynchronousFailure.compareAndSet(null,failure);throw failure;}});
   AuthOperationTransportFacade raw=new AuthOperationTransportFacade(transport,remote,images,settings);org.springframework.test.util.ReflectionTestUtils.setField(raw,"hfOrg","capacity-org-"+park);org.springframework.test.util.ReflectionTestUtils.setField(raw,"xcOrg","capacity-org-"+park);
   scheduler=new AuthOperationScheduler(settings,core,ledger,proxy(raw,new DataSourceTransactionManager(pool)),employee,service);scheduler.start();
  }
  AuthOperationTimerTask timer=new AuthOperationTimerTask(scheduler);timerDriver=Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"slo-timer-"+park));
  timerDriver.scheduleWithFixedDelay(()->{try{timerTicks.incrementAndGet();timer.advance();}catch(Throwable failure){asynchronousFailure.compareAndSet(null,failure);}},0,1000,TimeUnit.MILLISECONDS);
 }
 protected void pauseTimer() throws Exception {
  if(timerDriver!=null){timerDriver.shutdown();Assert.assertTrue(timerDriver.awaitTermination(35,TimeUnit.SECONDS));timerDriver=null;}
 }
 protected void stopPipeline() throws Exception {try{pauseTimer();}finally{if(scheduler!=null){stopScheduler(scheduler);scheduler=null;}}}
 protected void startObserver() {
  observer=Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"slo-observer-"+park));
  observer.scheduleWithFixedDelay(()->{try{acknowledgeDirect();observe();}catch(Throwable failure){asynchronousFailure.compareAndSet(null,failure);}},0,1000,TimeUnit.MILLISECONDS);
 }
 protected void stopObserver() throws InterruptedException {if(observer!=null){observer.shutdown();Assert.assertTrue("观察线程退出后才能冻结报告",observer.awaitTermination(35,TimeUnit.SECONDS));observer=null;}}
 protected void recordPhysical(DispatcherDTO<?> dto,String phase,boolean deletion,long called) {
  List<Map<String,Object>> rows=jdbc.queryForList("SELECT P.TARGET_ID,P.DEVICE_ID,P.CARD_NO,P.TASK_ID,P.SERIAL_NO,P.ACTION,T.SUBJECT_ID,T.BATCH_ID FROM SMT_AUTH_TRANSPORT_PHASE P JOIN SMT_AUTH_OPERATION_TARGET T ON T.ID=P.TARGET_ID WHERE P.PARK_ID=? AND P.INSTANCE_ID=? AND P.REQUEST_KEY=? AND P.PHASE=?",park,instanceId,dto.getEventId(),phase);
  Assert.assertFalse("物理请求必须关联真实持久目标",rows.isEmpty());
  if("DIRECT_SEND".equals(phase))Assert.assertEquals("单设备直连请求只能关联一个目标",1,rows.size());
  for(Map<String,Object> row:rows) {
   String subject=(String)row.get("SUBJECT_ID"),device=(String)row.get("DEVICE_ID");long batch=((Number)row.get("BATCH_ID")).longValue();
   Map<String,Object> event=new LinkedHashMap<>(row);event.put("calledNanos",called);event.put("phase",phase);event.put("requestKey",dto.getEventId());event.put("delete",deletion);externalEvents.add(event);
   if("DIRECT_SEND".equals(phase)) {
    try {
     Assert.assertEquals(Integer.valueOf(park),dto.getParkId());Assert.assertEquals(device,dto.getDeviceId());
     Assert.assertEquals(deletion?"DELETE":"ADD",row.get("ACTION"));
     Assert.assertEquals((deletion?EventEnum.DEVICE_DELETE_CARD:EventEnum.DEVICE_ADD_CARD).getCode(),dto.getEventType());
     String wireDevice,wireCard,wireSerial;Integer wireTask;
     if(deletion){Assert.assertTrue(dto.getData() instanceof CardDelDTO);CardDelDTO data=(CardDelDTO)dto.getData();wireDevice=data.getDeviceCode();wireCard=data.getCardNo();wireSerial=data.getSerialNo();wireTask=data.getReqId();}
     else{Assert.assertTrue(dto.getData() instanceof CardDTO);CardDTO data=(CardDTO)dto.getData();wireDevice=data.getDeviceCode();wireCard=data.getCardNo();wireSerial=data.getSerialNo();wireTask=data.getReqId();}
     event.put("wireDeviceCode",wireDevice);event.put("wireCardNo",wireCard);event.put("wireTaskId",wireTask);event.put("wireSerialNo",wireSerial);
     Assert.assertEquals(device,wireDevice);Assert.assertEquals(row.get("CARD_NO"),wireCard);Assert.assertEquals(String.valueOf(row.get("TASK_ID")),String.valueOf(wireTask));Assert.assertEquals(row.get("SERIAL_NO"),wireSerial);event.put("wireMatched",true);
    } catch(AssertionError failure) {
     event.put("wireMatched",false);AuthOperationSloSamples.Sample sample=samplesBySubject.get(subject);if(sample!=null)sample.error="DIRECT_WIRE_MISMATCH";asynchronousFailure.compareAndSet(null,failure);throw failure;
    }
   }
   if(deletion)physicalDeletes.incrementAndGet();else physicalAdds.incrementAndGet();
   AuthOperationSloSamples.Sample sample=samplesBySubject.get(subject);if(sample!=null && deletion=="DELETE".equals(sample.action))sample.physical(called,subject,device,phase,dto.getEventId(),batch);
  }
 }
 protected void acknowledgeDirect() {
  if(!"DIRECT".equals(access))return;
  for(Long id:jdbc.queryForList("SELECT ID FROM (SELECT ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND PHASE='DIRECT_SEND' AND STATE='ACCEPTED' ORDER BY ID) WHERE ROWNUM<=200",Long.class,park)) {
   SmtAuthTransportPhase p=phases.byId(id);transport.receipt(park,instanceId,id,null,p.getDeviceId(),p.getSerialNo(),"slo-ack-"+id,true,"受控设备确认");acknowledgements.incrementAndGet();
  }
 }
 protected void observe() {
  Map<String,Object> row=new LinkedHashMap<>();row.put("nanos",System.nanoTime());row.put("timerTicks",timerTicks.get());row.put("physicalAddTargets",physicalAdds.get());row.put("physicalDeleteTargets",physicalDeletes.get());row.put("directAcks",acknowledgements.get());
  row.put("batchProgress",jdbc.queryForList("SELECT ID,ACTION,STATUS,EXPECTED_COUNT,EXPANDED_COUNT,ACCEPTED_AT,EXPANSION_FINISHED_AT,CONVERGED_AT FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? ORDER BY ID",park));
  row.put("targetStates",jdbc.queryForList("SELECT DEVICE_ID,STATE,COUNT(*) N FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? GROUP BY DEVICE_ID,STATE",park));
  row.put("sourceStates",jdbc.queryForList("SELECT STATE,COUNT(*) N FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? GROUP BY STATE",park));
  row.put("inflightIncludingVerifying",jdbc.queryForList("SELECT DEVICE_ID,COUNT(*) N FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND STATE IN ('EXECUTING','WAITING_CONFIRM','VERIFYING') GROUP BY DEVICE_ID",park));
  row.put("oldestUnfinishedQueueAgeSeconds",jdbc.queryForObject("SELECT COALESCE(FLOOR((CAST(SYSTIMESTAMP AS DATE)-CAST(MIN(ACCEPTED_AT) AS DATE))*86400),0) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS<>'CONVERGED'",Long.class,park));
  if(!backgroundBatches.isEmpty()) {
   int confirmed=jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=? AND SUBJECT_ID>=? AND SUBJECT_ID<? AND STATE IN ('CONFIRMED','CONVERGED')",Integer.class,park,subject(0),subject(1000));
   row.put("backgroundConfirmedTargets",confirmed);row.put("backgroundRemainingTargets",20000-confirmed);
  }
  row.put("quota",jdbc.queryForList("SELECT * FROM SMT_AUTH_SCHEDULER_QUOTA WHERE INSTANCE_ID=?",instanceId));
  row.put("poolTotal",pool.getHikariPoolMXBean().getTotalConnections());row.put("poolActive",pool.getHikariPoolMXBean().getActiveConnections());row.put("poolPending",pool.getHikariPoolMXBean().getThreadsAwaitingConnection());observations.add(row);
  Map<String,AuthOperationSloSamples.Sample> byOperation=new HashMap<>();for(AuthOperationSloSamples.Sample sample:samplesBySubject.values())if(sample.operationKey!=null)byOperation.put(sample.operationKey,sample);
  for(Map<String,Object> state:jdbc.queryForList("SELECT B.SOURCE_ID OPERATION_KEY,COUNT(*) N,SUM(CASE WHEN T.STATE IN ('CONFIRMED','CONVERGED') THEN 1 ELSE 0 END) DONE FROM SMT_AUTH_OPERATION_TARGET T JOIN SMT_AUTH_OPERATION_BATCH B ON B.ID=T.BATCH_ID WHERE B.PARK_ID=? GROUP BY B.SOURCE_ID",park)) {
   AuthOperationSloSamples.Sample sample=byOperation.get(state.get("OPERATION_KEY"));if(sample!=null && sample.ack<0 && sample.firstPhysical>=0 && ((Number)state.get("N")).intValue()==((Number)state.get("DONE")).intValue())sample.ack=System.nanoTime();
  }
  for(Map<String,Object> state:jdbc.queryForList("SELECT OPERATION_KEY,COUNT(*) N,SUM(CASE WHEN STATE='CONVERGED' THEN 1 ELSE 0 END) DONE FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? GROUP BY OPERATION_KEY",park)) {
   AuthOperationSloSamples.Sample sample=byOperation.get(state.get("OPERATION_KEY"));if(sample!=null && sample.converged<0 && sample.firstPhysical>=0 && ((Number)state.get("N")).intValue()==((Number)state.get("DONE")).intValue())sample.converged=System.nanoTime();
  }
 }
 protected void rethrowAsync(){Throwable failure=asynchronousFailure.get();if(failure!=null)throw new AssertionError("异步调度/受控远端失败",failure);}
 protected void writeReport() throws Exception {
  report.put("observedMaxHeapBytes",maxHeap.get());report.put("observedMaxActiveConnections",maxActive.get());report.put("observedMaxPendingConnections",maxPending.get());report.put("heapSampleMs",100);report.put("sql",sqlTiming.snapshot());
  String output=System.getenv("SMART_AUTH_SLO_REPORT");Assert.assertNotNull("必须提供独立报告路径",output);Path file=Paths.get(output);Assert.assertTrue(file.isAbsolute());Files.write(file,JSONUtil.toJsonPrettyStr(report).getBytes(StandardCharsets.UTF_8));
 }
 @Override @After public void cleanup() {
  if(jdbc==null)return;
  List<String> failures=new ArrayList<>();
  try{stopExtraThreads();}catch(Throwable failure){failures.add("list:"+failure.getClass().getName());}
  try{if(requests!=null && !requests.stop(35,TimeUnit.SECONDS))throw new AssertionError("受理线程未退出");}catch(Throwable failure){failures.add("admission:"+failure.getClass().getName());}
  try{stopObserver();}catch(Throwable failure){failures.add("observer:"+failure.getClass().getName());}
  try{stopPipeline();}catch(Throwable failure){failures.add("timer/scheduler:"+failure.getClass().getName());}
  try{if(heapSampler!=null){heapSampler.shutdown();Assert.assertTrue(heapSampler.awaitTermination(5,TimeUnit.SECONDS));}}catch(Throwable failure){failures.add("heap:"+failure.getClass().getName());}
  mayClean=failures.isEmpty();report.put("threadsTerminated",mayClean);report.put("cleanupFailures",failures);
  // 所有生产者退出后才读取最终异常；业务失败不能跳过本用例数据清理。
  Throwable terminal=asynchronousFailure.get();
  if(terminal!=null)report.put("asynchronousFailure",terminal.getClass().getName());
  if(mayClean){
   try{cleanupExtraBusiness();super.cleanup();}catch(Throwable failure){failures.add("data:"+failure.getClass().getName());if(terminal==null)terminal=failure;}
   finally{if(pool!=null){try{if(!pool.isClosed() && !failures.isEmpty())pool.close();report.put("poolClosed",pool.isClosed());}catch(Throwable failure){failures.add("pool:"+failure.getClass().getName());if(terminal==null)terminal=failure;}}}
  } else if(terminal==null)terminal=new IllegalStateException("线程未退出，保留数据等待独占收尾");
  if(terminal!=null || !failures.isEmpty())report.put("verdict","FAIL");
  try{writeReport();}catch(Throwable failure){if(terminal==null)terminal=failure;else terminal.addSuppressed(failure);}
  if(terminal!=null)throw new AssertionError("SLO 最终收尾失败",terminal);
 }
    private class IscSloRemote {
        final Map<String,String> personsByBadge=new ConcurrentHashMap<>();
        final Set<String> knownPersons=ConcurrentHashMap.newKeySet();
        final Map<String,RemoteBatch> configs=new ConcurrentHashMap<>(),downloads=new ConcurrentHashMap<>();
        final Map<String,AtomicInteger> requests=new ConcurrentHashMap<>();
        final Map<String,Map<Integer,AtomicInteger>> peoplePerRequest=new ConcurrentHashMap<>();
        final Set<Long> detailReturnedTargets=ConcurrentHashMap.newKeySet();
        final AtomicInteger externalIds=new AtomicInteger(),correlationReads=new AtomicInteger();
        final AtomicLong correlationReadNanos=new AtomicLong();
        IscSloRemote() {
            for(Map<String,Object> row:jdbc.queryForList("SELECT DISTINCT BADGE,PERSON_ID FROM SMT_ISC_DOWN_RECORD WHERE PARK_ID=?",park)){String person=(String)row.get("PERSON_ID");personsByBadge.put((String)row.get("BADGE"),person);knownPersons.add(person);}
        }
        Result<String> dispatch(DispatcherDTO<?> dto,long called) {
            JSONObject data=JSONUtil.parseObj(dto.getData());Integer event=dto.getEventType();
            if(EventEnum.ISC_PERSON_GET.getCode().equals(event)) {
                JSONArray badges=data.getJSONArray("paramValue");Assert.assertEquals("jobNo",data.getStr("paramName"));Assert.assertTrue(badges.size()<=200);record("PERSON_GET",badges.size());
                JSONArray rows=new JSONArray();for(Object badge:badges){String person=personsByBadge.get(String.valueOf(badge));if(person!=null)rows.add(new JSONObject().put("jobNo",badge).put("personId",person).put("status",1).put("personPhoto",new JSONArray().put(new JSONObject().put("picUri","frozen"))));}
                return json(new JSONObject().put("list",rows));
            }
            if(EventEnum.ISC_PERSON_ADD.getCode().equals(event)) {
                record("PERSON_ADD",1);Assert.assertEquals("capacity-org-"+park,data.getStr("orgIndexCode"));Assert.assertNotNull(data.getStr("personName"));
                Assert.assertEquals("c3ludGhldGljLWZhY2U=",data.getJSONArray("faces").getJSONObject(0).getStr("faceData"));
                String badge=data.getStr("jobNo");Assert.assertNotNull(badge);String person="isc-created-"+park+"-"+badge;
                String previous=personsByBadge.putIfAbsent(badge,person);if(previous!=null)Assert.assertEquals("重复建人必须保持相同远端身份",previous,person);
                knownPersons.add(person);
                return json(new JSONObject().put("personId",person));
            }
            if(EventEnum.ISC_FACE_ADD.getCode().equals(event)) {
                record("FACE_ADD",1);Assert.assertTrue(knownPersons.contains(data.getStr("personId")));Assert.assertEquals("c3ludGhldGljLWZhY2U=",data.getStr("faceData"));
                return json(new JSONObject().put("personId",data.getStr("personId")).put("faceId","face-"+data.getStr("personId")));
            }
            if(EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(event)||EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(event)) {
                String device=resource(data.getJSONArray("resourceInfos").getJSONObject(0));JSONArray ids=data.getJSONArray("personDatas").getJSONObject(0).getJSONArray("indexCodes");
                RemoteBatch batch=members(dto.getEventId(),"ISC_CONFIG",device);Assert.assertEquals(new HashSet<>(batch.people),new HashSet<>(ids.toList(String.class)));
                recordPhysical(dto,"ISC_CONFIG",EventEnum.ISC_AUTH_CONFIG_DEL.getCode().equals(event),called);
                record(EventEnum.ISC_AUTH_CONFIG_ADD.getCode().equals(event)?"CONFIG_ADD":"CONFIG_DELETE",ids.size());String external="isc-capacity-config-"+park+"-"+externalIds.incrementAndGet();configs.put(external,batch);
                return json(new JSONObject().put("taskId",external));
            }
            if(EventEnum.ISC_AUTH_CONFIG_PROCESS_GET.getCode().equals(event)) {
                RemoteBatch batch=configs.get(data.getStr("taskId"));Assert.assertNotNull("进度只能引用远端实际返回的配置ID",batch);record("CONFIG_PROGRESS",batch.people.size());
                return json(new JSONObject().put("isFinished",permitReceipts).put("isConfigFinished",permitReceipts).put("failedNum",0).put("successedNum",permitReceipts?batch.people.size():0));
            }
            if(EventEnum.ISC_AUTH_CONFIG_DOWN.getCode().equals(event)) {
                Assert.assertEquals(Integer.valueOf(5),data.getInt("taskType"));String device=resource(data.getJSONArray("resourceInfos").getJSONObject(0));
                RemoteBatch batch=members(dto.getEventId(),"ISC_DOWNLOAD",device);record("DOWNLOAD",batch.people.size());String external="isc-capacity-download-"+park+"-"+externalIds.incrementAndGet();downloads.put(external,batch);
                return json(new JSONObject().put("taskId",external));
            }
            if(EventEnum.ISC_TASK_RECORD_DETAIL_GET.getCode().equals(event)) {
                RemoteBatch batch=downloads.get(data.getStr("taskId"));Assert.assertNotNull("明细只能引用远端实际返回的下载ID",batch);Assert.assertEquals(batch.device,resource(data.getJSONObject("resourceInfo")));
                Assert.assertEquals(Integer.valueOf(200),data.getInt("pageSize"));int page=data.getInt("pageNo");Assert.assertTrue(page>=1);int start=(page-1)*200,end=Math.min(start+200,batch.people.size());Assert.assertTrue("合法明细页不能越界",start<end);
                JSONArray rows=new JSONArray();for(int i=start;i<end;i++)rows.add(new JSONObject().put("personId",batch.people.get(i)).put("persondownloadResult","0"));
                record("DETAIL",rows.size());detailReturnedTargets.addAll(batch.targets);
                return json(new JSONObject().put("list",rows).put("total",batch.people.size()));
            }
            throw new AssertionError("未预期ISC容量协议事件："+event);
        }
        private RemoteBatch members(String requestKey,String phase,String device) {
            Assert.assertNotNull(requestKey);correlationReads.incrementAndGet();
            long readStarted=System.nanoTime();
            List<Map<String,Object>> rows=jdbc.queryForList("SELECT PERSON_ID,TARGET_ID,DEVICE_ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=? AND INSTANCE_ID=? AND REQUEST_KEY=? AND PHASE=?",park,"capacity-"+park,requestKey,phase);
            correlationReadNanos.addAndGet(System.nanoTime()-readStarted);
            Assert.assertTrue("外发请求成员必须存在并受200人上限约束",!rows.isEmpty()&&rows.size()<=200);RemoteBatch batch=new RemoteBatch(device);
            for(Map<String,Object> row:rows){Assert.assertEquals(device,row.get("DEVICE_ID"));String person=(String)row.get("PERSON_ID");Assert.assertNotNull(person);Assert.assertTrue("配置身份必须已由受控远端或可信历史证明",knownPersons.contains(person));batch.people.add(person);batch.targets.add(((Number)row.get("TARGET_ID")).longValue());}
            return batch;
        }
        private String resource(JSONObject resource) {
            String id=resource.getStr("resourceIndexCode");Assert.assertTrue(id.startsWith("slo-device-"+park+"-"));Assert.assertEquals("acsDevice",resource.getStr("resourceType"));Assert.assertEquals(Integer.valueOf(1),resource.getJSONArray("channelNos").getInt(0));return id;
        }
        private Result<String> json(JSONObject value){return Result.success(value.toString());}
        private void record(String event,int people){requests.computeIfAbsent(event,k->new AtomicInteger()).incrementAndGet();peoplePerRequest.computeIfAbsent(event,k->new ConcurrentHashMap<>()).computeIfAbsent(people,k->new AtomicInteger()).incrementAndGet();}
        Map<String,Integer> httpSnapshot(){Map<String,Integer> result=new TreeMap<>();requests.forEach((key,count)->result.put(key,count.get()));return result;}
        Map<String,Map<Integer,Integer>> peopleSnapshot(){Map<String,Map<Integer,Integer>> result=new TreeMap<>();peoplePerRequest.forEach((event,sizes)->{Map<Integer,Integer> counts=new TreeMap<>();sizes.forEach((size,count)->counts.put(size,count.get()));result.put(event,counts);});return result;}
    }
    private static class RemoteBatch {
        final String device;final List<String> people=new ArrayList<>();final Set<Long> targets=new HashSet<>();
        RemoteBatch(String device){this.device=device;}
    }

}
