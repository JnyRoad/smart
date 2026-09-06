package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.Shard;
import com.tce.smart.platform.core.service.impl.AuthOperationWorkflowService;
import com.tce.smart.schedule.config.AuthOperationSchedulerProperties;
import com.tce.smart.schedule.task.AuthOperationTimerTask;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Assert;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.lang.management.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** SC004专用子JVM；进程终止由持有该进程句柄的父驱动执行，恢复绝不重新造数。 */
public class AuthOperationJvmRecoveryWorker extends AuthOperationCapacityTest {
    private final Path directory=Paths.get(required("SMART_AUTH_CRASH_DIRECTORY"));
    private final String nonce=required("SMART_AUTH_CRASH_NONCE"), mode=required("SMART_AUTH_CRASH_MODE");
    private final String driver=value("SMART_AUTH_CRASH_DRIVER","SERVICE_LOOP");
    private final int ownedPark=Integer.parseInt(required("SMART_AUTH_CRASH_PARK"));
    private final int people=Integer.parseInt(value("SMART_AUTH_CRASH_PEOPLE","10000")),devices=Integer.parseInt(value("SMART_AUTH_CRASH_DEVICES","10"));
    private final long started=System.nanoTime(),seconds=Long.parseLong(value("SMART_AUTH_CRASH_SECONDS","900"));
    private final String hook=value("SMART_AUTH_CRASH_HOOK","NONE");
    private final ThreadLocal<CommitScope> scope=new ThreadLocal<>();
    private final AtomicLong ticks=new AtomicLong();
    private ScheduledExecutorService samples,timer;
    private AuthOperationScheduler expansionScheduler;
    private long batch;
    private volatile long observedHeap,observedThreads,observedConnections,observedActive,observedPending,observedQueue;
    private static final String FIELDS="PARK_ID,SUBJECT_TYPE,SUBJECT_ID,DEVICE_ID,ACCESS_TYPE,RESOURCE_TYPE,RESOURCE_ID,SERVICE_TYPE,CREDENTIAL_CHANNEL";

    @Override protected int fixturePark(){Assert.assertTrue(ownedPark>100000 && ownedPark<700000000 && ownedPark!=9001);return ownedPark;}
    @Override protected boolean seedInitialFixture(){
        if(!"INIT".equals(mode))return false;
        Assert.assertEquals("已有批次禁止重新seed",Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Integer.class,park));
        Assert.assertEquals("已有员工禁止重新seed",Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF WHERE COMP_ID=?",Integer.class,"employee-test-"+park));return true;
    }
    @Override protected void ensureSelectionSchema(){Assert.assertEquals("驱动只用既有schema，禁止自动DDL",Integer.valueOf(5),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('SMT_AUTH_SELECTION_SOURCE','SMT_AUTH_SELECTION_RESOURCE','SMT_AUTH_TRANSPORT_PHASE','SMT_AUTH_SCHEDULER_QUOTA','SMT_PARK_BU')",Integer.class));}
    @Override protected DataSourceTransactionManager fixtureTransactionManager(){return new DataSourceTransactionManager(pool){
        @Override protected void doCommit(DefaultTransactionStatus status){
            CommitScope current=scope.get();checkpoint("BEFORE",current);super.doCommit(status);checkpoint("AFTER",current);
        }
    };}
    @SuppressWarnings("unchecked") @Override protected <T>T proxy(T raw,DataSourceTransactionManager tm){
        ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);
        if(raw instanceof AuthOperationWorkflowService)p.addAdvice((MethodInterceptor)call->{
            CommitScope old=scope.get(),next=null;Object[] a=call.getArguments();String name=call.getMethod().getName();
            if("stage".equals(name)){Shard s=(Shard)a[0];next=new CommitScope("STAGE",s.getBatchId(),s.getPreviousCursor(),s.getNextCursor());}
            if("bindLane".equals(name))next=new CommitScope("BIND",(Long)a[0],((Number)a[2]).longValue(),((Number)a[3]).longValue());
            if("finish".equals(name))next=new CommitScope("FINISH",(Long)a[0],selection.cursor((Long)a[0]),selection.cursor((Long)a[0]));
            if(next!=null)scope.set(next);try{return call.proceed();}finally{if(old==null)scope.remove();else scope.set(old);}
        });
        p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();
    }
    private void checkpoint(String side,CommitScope point){
        if(point==null || "NONE".equals(hook))return;String[] chosen=hook.split(":");
        if(chosen.length!=3 || !chosen[0].equals(point.phase) || !chosen[1].equals(side) || point.next<Long.parseLong(chosen[2]))return;
        Assert.assertEquals("钩子只接受真实工作流事务", "com.tce.smart.platform.core.service.impl.AuthOperationWorkflowService."+("STAGE".equals(point.phase)?"stage":"BIND".equals(point.phase)?"bindLane":"finish"),TransactionSynchronizationManager.getCurrentTransactionName());
        Map<String,Object> m=base();m.put("phase",point.phase);m.put("side",side);m.put("batch",point.batch);m.put("previousCursor",point.previous);m.put("nextCursor",point.next);m.put("transactionName",TransactionSynchronizationManager.getCurrentTransactionName());
        atomic(directory.resolve("hook.json"),m);System.out.println("CRASH_HOOK "+JSONUtil.toJsonStr(m));
        long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);while(System.nanoTime()<end)try{Thread.sleep(100);}catch(InterruptedException e){throw new AssertionError(e);}
        throw new AssertionError("父进程未在30秒内终止本子JVM，保留数据");
    }
    private void initialize()throws Exception{
        int targets=Math.multiplyExact(people,devices);Assert.assertTrue(targets>0 && targets<=100000);
        java.lang.reflect.Method seed=AuthOperationCapacityTest.class.getDeclaredMethod("seed",int.class,int.class,boolean.class);seed.setAccessible(true);seed.invoke(this,people,devices,false);
        java.lang.reflect.Method history=AuthOperationCapacityTest.class.getDeclaredMethod("seedIscHistory",boolean.class);history.setAccessible(true);history.invoke(this,false);
        com.tce.smart.platform.controller.SmtStaffDeviceAuthController c=new com.tce.smart.platform.controller.SmtStaffDeviceAuthController();org.springframework.test.util.ReflectionTestUtils.setField(c,"smtStaffDeviceAuthService",entry);
        org.springframework.test.web.servlet.MockMvc mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(c).build();
        List<String> ids=new ArrayList<>();for(int i=0;i<people;i++)ids.add(String.valueOf(employeeId+i));UpdateDeviceAuthDTO dto=new UpdateDeviceAuthDTO();dto.setIds(ids);dto.setDeviceAuthIds(Collections.emptyList());dto.setStartTime("2026-09-01");dto.setEndTime("2026-09-30");
        long accept=System.nanoTime();org.springframework.mock.web.MockHttpServletResponse response=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/staff/device/auth/updateAuth/2").contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(JSONUtil.toJsonStr(dto))).andReturn().getResponse();Assert.assertEquals(200,response.getStatus());Assert.assertTrue(JSONUtil.toBean(response.getContentAsString(),com.tce.smart.common.core.model.Result.class).isSuccess());
        batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);Map<String,Object> m=base();m.put("batch",batch);m.put("people",people);m.put("devices",devices);m.put("targets",targets);m.put("acceptMs",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-accept));
        Map<String,Object> snapshot=conservation(false);m.put("selectionHash",snapshot.get("selectionHash"));m.put("selectionCount",snapshot.get("expected"));
        // 初始受理后的独立业务笛卡尔集合验证，恢复仅使用上述冻结hash，不再读当前权限组。
        String business="SELECT "+park+" PARK_ID,'STAFF' SUBJECT_TYPE,TO_CHAR(S.ID) SUBJECT_ID,D.ID DEVICE_ID,'ISC' ACCESS_TYPE,'PERSON' RESOURCE_TYPE,TO_CHAR(S.ID) RESOURCE_ID,'1' SERVICE_TYPE,'FACE' CREDENTIAL_CHANNEL FROM SMT_STAFF S CROSS JOIN SMT_DEVICE D WHERE S.COMP_ID=? AND D.PARK_ID=? ORDER BY 1,2,3,4,5,6,7,8,9";
        try(Connection cx=pool.getConnection();PreparedStatement ps=cx.prepareStatement(business)){ps.setQueryTimeout(30);ps.setFetchSize(200);ps.setString(1,"employee-test-"+park);ps.setInt(2,park);try(ResultSet rs=ps.executeQuery()){DigestSet d=digest(rs);Assert.assertEquals(targets,d.count);Assert.assertEquals(m.get("selectionHash"),d.hash);m.put("initialBusinessTupleHash",d.hash);}}
        atomic(directory.resolve("state.json"),m);System.out.println("CRASH_INITIALIZED "+JSONUtil.toJsonStr(m));
    }
    private void resumeState()throws Exception{
        Map<String,Object> m=JSONUtil.toBean(new String(Files.readAllBytes(directory.resolve("state.json")),StandardCharsets.UTF_8),Map.class);Assert.assertEquals(nonce,m.get("nonce"));Assert.assertEquals(ownedPark,((Number)m.get("park")).intValue());batch=((Number)m.get("batch")).longValue();Assert.assertEquals(people,((Number)m.get("people")).intValue());Assert.assertEquals(devices,((Number)m.get("devices")).intValue());
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE ID=? AND PARK_ID=?",Integer.class,batch,park));
        Map<String,Object> actual=conservation(false);Assert.assertEquals(m.get("selectionHash"),actual.get("selectionHash"));
        String evidence=value("SMART_AUTH_CRASH_PREVIOUS_HOOK","");if(!evidence.isEmpty()){
            Map<String,Object> h=JSONUtil.toBean(new String(Files.readAllBytes(Paths.get(evidence)),StandardCharsets.UTF_8),Map.class);Assert.assertEquals(nonce,h.get("nonce"));Assert.assertEquals(batch,((Number)h.get("batch")).longValue());
            if(!"FINISH".equals(h.get("phase"))){long expected=((Number)h.get("AFTER".equals(h.get("side"))?"nextCursor":"previousCursor")).longValue();Assert.assertEquals("真实提交前回滚或提交后持久化",expected,selection.cursor(batch).longValue());}
        }
        System.out.println("CRASH_RESUMED "+JSONUtil.toJsonStr(actual));
    }
    private void expand()throws Exception{
        if("SERVICE_LOOP".equals(driver)){
            while(employee.stageNext(batch))budget();while(employee.bindNextLane(batch,null)!=null)budget();budget();employee.finish(batch);
        }else{
            Assert.assertEquals("EXPAND_ONLY_TIMER",driver);AuthOperationSchedulerProperties settings=new AuthOperationSchedulerProperties();settings.setEnabled(true);AuthOperationSchedulerProperties.Instance i=new AuthOperationSchedulerProperties.Instance();i.setId("capacity-"+park);i.setParks(Collections.singletonList(park));i.setAccessType("ISC");settings.getInstances().add(i);AuthOperationProperties core=new AuthOperationProperties();core.setEnabled(true);core.setEnabledParks(Collections.singleton(park));
            AuthOperationTransportFacade blocked=org.mockito.Mockito.mock(AuthOperationTransportFacade.class,call->{throw new AssertionError("EXPAND_ONLY驱动禁止调用发送接口");});
            java.lang.reflect.Method enqueue=AuthOperationScheduler.class.getDeclaredMethod("enqueue",AuthOperationSchedulerProperties.Instance.class,String.class,Integer.class);enqueue.setAccessible(true);
            expansionScheduler=new AuthOperationScheduler(settings,core,ledger,blocked,employee,service){@Override public void tick(){try{enqueue.invoke(this,i,"EXPAND",null);}catch(Exception e){throw new AssertionError(e);}}};expansionScheduler.start();AuthOperationTimerTask task=new AuthOperationTimerTask(expansionScheduler);
            timer=Executors.newSingleThreadScheduledExecutor();timer.scheduleWithFixedDelay(()->{ticks.incrementAndGet();task.advance();},0,1,TimeUnit.SECONDS);
            while("PREPARING".equals(jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,batch))){budget();Thread.sleep(1000);}
            stopExpansion();
        }
        Map<String,Object> result=conservation(true);result.putAll(base());result.put("result","EXPANSION_PROCESS_RECOVERY_COMPLETE_NOT_FULL_TIMER");result.put("resources",resourceSummary());atomic(directory.resolve("complete.json"),result);System.out.println("CRASH_COMPLETE "+JSONUtil.toJsonStr(result));
    }
    private Map<String,Object> conservation(boolean complete)throws Exception{
        String expected="SELECT DISTINCT "+FIELDS+" FROM SMT_AUTH_SELECTION_RESOURCE WHERE BATCH_ID=? ORDER BY "+FIELDS;
        String actual="SELECT DISTINCT "+Arrays.stream(FIELDS.split(",")).map(x->"R."+x).collect(java.util.stream.Collectors.joining(","))+",T.ID FROM SMT_AUTH_OPERATION_TARGET T JOIN SMT_AUTH_SOURCE_RESOURCE C ON C.TARGET_ID=T.ID JOIN SMT_AUTH_RESOURCE_COORD R ON R.ID=C.RESOURCE_COORD_ID WHERE T.BATCH_ID=? ORDER BY "+Arrays.stream(FIELDS.split(",")).map(x->"R."+x).collect(java.util.stream.Collectors.joining(","))+",T.ID";
        Map<String,Object> result=new LinkedHashMap<>();try(Connection c=pool.getConnection();PreparedStatement es=c.prepareStatement(expected);PreparedStatement as=c.prepareStatement(actual)){
            for(PreparedStatement p:Arrays.asList(es,as)){p.setQueryTimeout(30);p.setFetchSize(200);p.setLong(1,batch);}try(ResultSet er=es.executeQuery();ResultSet ar=as.executeQuery()){
                MessageDigest hash=MessageDigest.getInstance("SHA-256");Tuple a=ar.next()?Tuple.read(ar):null,previousActual=null;long count=0,bound=0,unbound=0;
                while(er.next()){Tuple e=Tuple.read(er);hash.update(e.bytes());count++;if(a!=null && a.compareTo(e)<0)throw new AssertionError("存在不在冻结集合中的目标");if(a!=null && a.compareTo(e)==0){if(previousActual!=null&&a.compareTo(previousActual)==0)throw new AssertionError("相同坐标重复目标");previousActual=a;bound++;a=ar.next()?Tuple.read(ar):null;if(a!=null&&a.compareTo(previousActual)==0)throw new AssertionError("相同坐标重复目标");}else unbound++;}
                Assert.assertNull("目标集合必须是冻结集合子集",a);Assert.assertEquals((long)people*devices,count);Assert.assertEquals(count,bound+unbound);result.put("expected",count);result.put("bound",bound);result.put("unbound",unbound);result.put("duplicates",0);result.put("selectionHash",hex(hash.digest()));if(complete)Assert.assertEquals(0,unbound);
                Assert.assertEquals("一个目标不能关联不同坐标",bound,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",Long.class,batch).longValue());
            }
        }
        Assert.assertEquals(Integer.valueOf(people),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE BATCH_ID=?",Integer.class,batch));Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=?",Integer.class,park));result.put("http",0);result.put("httpScope","EXPAND_ONLY; no HTTP lane started or remote transport wired");
        Map<String,Object> b=jdbc.queryForMap("SELECT EXPECTED_COUNT,EXPANDED_COUNT,EXPANSION_CURSOR,STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",batch);result.put("batchState",b);Assert.assertEquals((long)people*devices,((Number)b.get("EXPECTED_COUNT")).longValue());if(complete){Assert.assertEquals((long)people*devices,((Number)b.get("EXPANDED_COUNT")).longValue());Assert.assertNotEquals("PREPARING",b.get("STATUS"));}return result;
    }
    private void startSamples(){samples=Executors.newSingleThreadScheduledExecutor();samples.scheduleWithFixedDelay(()->{try{
        long heap=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();observedHeap=Math.max(observedHeap,heap);observedThreads=Math.max(observedThreads,ManagementFactory.getThreadMXBean().getThreadCount());
        if(pool!=null&&pool.getHikariPoolMXBean()!=null){observedConnections=Math.max(observedConnections,pool.getHikariPoolMXBean().getTotalConnections());observedActive=Math.max(observedActive,pool.getHikariPoolMXBean().getActiveConnections());observedPending=Math.max(observedPending,pool.getHikariPoolMXBean().getThreadsAwaitingConnection());}
        Map<String,Object> m=base();m.putAll(resourceSummary());Files.write(directory.resolve("samples-"+pid()+".jsonl"),(JSONUtil.toJsonStr(m)+"\n").getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    }catch(Exception e){System.err.println("CRASH_SAMPLE_ERROR "+e.getClass().getSimpleName());}},0,100,TimeUnit.MILLISECONDS);}
    private Map<String,Object> resourceSummary(){Map<String,Object> r=new LinkedHashMap<>();r.put("heapObservedMaxBytes",observedHeap);r.put("xmxBytes",Runtime.getRuntime().maxMemory());r.put("threadsObservedMax",observedThreads);r.put("poolTotalObservedMax",observedConnections);r.put("poolActiveObservedMax",observedActive);r.put("poolPendingObservedMax",observedPending);r.put("sampleEveryMs",100);r.put("observationLimit","sampled maxima; RSS sampled by parent; no continuous peak claim");List<Map<String,Object>> gc=new ArrayList<>();for(GarbageCollectorMXBean b:ManagementFactory.getGarbageCollectorMXBeans()){Map<String,Object> x=new LinkedHashMap<>();x.put("name",b.getName());x.put("collections",b.getCollectionCount());x.put("totalPauseMs",b.getCollectionTime());gc.add(x);}r.put("gc",gc);return r;}
    private void stopExpansion()throws Exception{if(timer!=null){timer.shutdown();Assert.assertTrue(timer.awaitTermination(30,TimeUnit.SECONDS));timer=null;}if(expansionScheduler!=null){stopScheduler(expansionScheduler);expansionScheduler=null;}}
    private void budget(){if(System.nanoTime()-started>TimeUnit.SECONDS.toNanos(seconds))throw new AssertionError("独立SC004诊断预算耗尽，保留同批数据");}
    private Map<String,Object> base(){Map<String,Object> m=new LinkedHashMap<>();m.put("nonce",nonce);m.put("pid",pid());m.put("park",ownedPark);m.put("driver",driver);m.put("childElapsedMs",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started));m.put("utcEpochMs",System.currentTimeMillis());m.put("ticks",ticks.get());return m;}
    static String pid(){return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];}
    static String required(String k){String v=System.getenv(k);if(v==null||v.isEmpty())throw new IllegalArgumentException("缺少参数 "+k);return v;}
    static String value(String k,String d){String v=System.getenv(k);return v==null?d:v;}
    static void atomic(Path p,Map<String,Object> m){try{Path tmp=p.resolveSibling(p.getFileName()+".tmp");byte[] b=JSONUtil.toJsonStr(m).getBytes(StandardCharsets.UTF_8);try(FileChannel f=FileChannel.open(tmp,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)){f.write(ByteBuffer.wrap(b));f.force(true);}Files.move(tmp,p,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(Exception e){throw new AssertionError(e);}}
    static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x&255));return s.toString();}
    static DigestSet digest(ResultSet r)throws Exception{MessageDigest h=MessageDigest.getInstance("SHA-256");long n=0;Tuple previous=null;while(r.next()){Tuple t=Tuple.read(r);if(previous!=null&&previous.compareTo(t)>=0)throw new AssertionError("预期集合乱序或重复");h.update(t.bytes());previous=t;n++;}return new DigestSet(n,hex(h.digest()));}
    static class DigestSet{final long count;final String hash;DigestSet(long c,String h){count=c;hash=h;}}
    static class CommitScope{final String phase;final long batch,previous,next;CommitScope(String p,long b,long o,long n){phase=p;batch=b;previous=o;next=n;}}
    static class Tuple implements Comparable<Tuple>{final String[] f;Tuple(String[] x){f=x;}static Tuple read(ResultSet r)throws SQLException{String[] x=new String[9];for(int i=0;i<9;i++)x[i]=r.getString(i+1);return new Tuple(x);}public int compareTo(Tuple b){for(int i=0;i<f.length;i++){int c=f[i]==null?(b.f[i]==null?0:-1):b.f[i]==null?1:f[i].compareTo(b.f[i]);if(c!=0)return c;}return 0;}byte[] bytes(){StringBuilder s=new StringBuilder();for(String x:f)s.append(x==null?"-1:":x.getBytes(StandardCharsets.UTF_8).length+":"+x);return s.append('\n').toString().getBytes(StandardCharsets.UTF_8);}}
    public static void main(String[] args)throws Exception{
        AuthOperationJvmRecoveryWorker w=new AuthOperationJvmRecoveryWorker();w.startSamples();boolean okay=false;
        try{Assert.assertTrue(w.seconds>0&&w.seconds<=7200);w.setup();if("INIT".equals(w.mode))w.initialize();else{w.resumeState();if("RUN".equals(w.mode))w.expand();else if("CLEANUP".equals(w.mode))w.cleanup();else throw new IllegalArgumentException("未知mode");}okay=true;}
        finally{w.stopExpansion();w.samples.shutdown();Assert.assertTrue(w.samples.awaitTermination(30,TimeUnit.SECONDS));if(w.pool!=null&&!w.pool.isClosed())w.pool.close();Map<String,Object> end=w.base();end.put("okay",okay);end.put("poolClosed",w.pool==null||w.pool.isClosed());end.put("resources",w.resourceSummary());atomic(w.directory.resolve("exit-"+pid()+".json"),end);System.out.println("CRASH_WORKER_EXIT "+JSONUtil.toJsonStr(end));}
    }
}
