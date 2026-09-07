package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.*;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.mapper.AuthOperationDirectTakeoverMapper;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.platform.core.service.impl.*;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.zaxxer.hikari.HikariDataSource;
import com.sun.net.httpserver.HttpServer;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.*;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 只在父任务单独授予的本机独占窗口运行；不自动建表、建索引或采集SYS视图。 */
public class AuthDirectTakeoverOracleTest {
    private static HikariDataSource firstPool,secondPool;
    private static Path evidence; private static String token; private static long started;
    private JdbcTemplate jdbc,other; private RuntimeGate first,second; private String instance,device;
    private int park,taskId; private final List<Integer> taskIds=new ArrayList<>();
    private final List<String> stateIds=new ArrayList<>();
    private final List<ExecutorService> workers=new ArrayList<>();
    private final AtomicInteger gateCommits=new AtomicInteger();
    private final Set<String> gateSids=ConcurrentHashMap.newKeySet();
    private volatile boolean observeTransactions;

    @BeforeClass public static void openPools() throws Exception {
        Assume.assumeTrue("仅显式授权的DIRECT专用Oracle运行", "true".equals(System.getenv("SMART_AUTH_DIRECT_TAKEOVER_ORACLE")));
        assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",System.getenv("SMART_AUTH_ORACLE_URL"));
        assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        token=System.getenv("SMART_AUTH_DIRECT_TAKEOVER_TOKEN");assertTrue(token!=null&&token.matches("[a-z0-9_]{8,24}"));
        evidence=Paths.get(Objects.requireNonNull(System.getenv("SMART_AUTH_DIRECT_TAKEOVER_EVIDENCE")));Files.createDirectories(evidence);
        firstPool=pool("dtko-first");secondPool=pool("dtko-second");
        JdbcTemplate j=new JdbcTemplate(firstPool);j.setQueryTimeout(5);assertEquals("SMART_AUTH_TEST",j.queryForObject("SELECT USER FROM DUAL",String.class));
        assertEquals(Integer.valueOf(4),j.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SCHEDULER_ROUTE'",Integer.class));
        assertEquals(Integer.valueOf(44),j.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE'",Integer.class));
        assertEquals(Integer.valueOf(13),j.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_DIRECT_CLAIM'",Integer.class));
        assertEquals(Integer.valueOf(15),j.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_DEVICE_TASK' AND COLUMN_NAME IN ('ID','ACTION','STATUS','SERIAL_NO','DEVICE_CODE','CARD_NO','DEVICE_TYPE','SERVICE_TYPE','CARD_TYPE','GENERAL','START_TIME','OVER_TIME','TIMES','REMARK','IMAGE_ID')",Integer.class));
        assertEquals(Integer.valueOf(2),j.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_DEVICE' AND COLUMN_NAME IN ('ID','PARK_ID')",Integer.class));
        assertEquals(Integer.valueOf(8),j.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_REVIEW' AND COLUMN_NAME IN ('ID','PARK_ID','ACCESS_TYPE','DEVICE_ID','TASK_KEY','REASON','STATE','CREATE_TIME')",Integer.class));
        Map<String,Object> column=j.queryForMap("SELECT DATA_TYPE,DATA_PRECISION,DATA_SCALE,NULLABLE,DATA_DEFAULT,DEFAULT_ON_NULL FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SCHEDULER_ROUTE' AND COLUMN_NAME='DIRECT_TAKEOVER_VERSION'");
        assertEquals("NUMBER",column.get("DATA_TYPE"));assertEquals("2",String.valueOf(column.get("DATA_PRECISION")));assertEquals("0",String.valueOf(column.get("DATA_SCALE")));
        assertEquals("N",column.get("NULLABLE"));assertEquals("0",String.valueOf(column.get("DATA_DEFAULT")).trim());assertEquals("NO",column.get("DEFAULT_ON_NULL"));
        assertEquals(Integer.valueOf(1),j.queryForObject("SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE TABLE_NAME='SMT_AUTH_SCHEDULER_ROUTE' AND CONSTRAINT_NAME='CK_AUTH_ROUTE_DIRECT_CAP' AND STATUS='ENABLED' AND VALIDATED='VALIDATED'",Integer.class));
        assertEquals(Integer.valueOf(5),j.queryForObject("SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE TABLE_NAME='SMT_AUTH_DIRECT_CLAIM' AND CONSTRAINT_NAME IN ('PK_AUTH_DIRECT_CLAIM','UK_AUTH_DIRECT_CLAIM','FK_AUTH_DIRECT_PHASE','CK_AUTH_DIRECT_KEY','CK_AUTH_DIRECT_PROOF') AND STATUS='ENABLED' AND VALIDATED='VALIDATED'",Integer.class));
        List<Map<String,Object>> indexes=j.queryForList("SELECT I.TABLE_NAME,I.INDEX_NAME,I.STATUS,C.COLUMN_NAME,C.COLUMN_POSITION FROM USER_INDEXES I JOIN USER_IND_COLUMNS C ON C.INDEX_NAME=I.INDEX_NAME WHERE I.TABLE_NAME IN ('SMT_AUTH_TRANSPORT_PHASE','SMT_AUTH_DIRECT_CLAIM','SMT_AUTH_SCHEDULER_ROUTE') ORDER BY I.TABLE_NAME,I.INDEX_NAME,C.COLUMN_POSITION");
        write("schema-preflight.txt",column.toString()+"\n"+indexes.toString()+"\n"+j.queryForList("SELECT TABLE_NAME,NUM_ROWS,BLOCKS,LAST_ANALYZED,STALE_STATS FROM USER_TAB_STATISTICS WHERE TABLE_NAME IN ('SMT_AUTH_TRANSPORT_PHASE','SMT_AUTH_DIRECT_CLAIM')").toString()+"\n");
    }
    private static HikariDataSource pool(String name) {
        HikariDataSource p=new HikariDataSource();p.setJdbcUrl(System.getenv("SMART_AUTH_ORACLE_URL"));p.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));p.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
        p.setMaximumPoolSize(3);p.setMinimumIdle(0);p.setConnectionTimeout(5000);p.setValidationTimeout(3000);p.setPoolName(name+"-"+token);
        p.addDataSourceProperty("oracle.net.CONNECT_TIMEOUT","5000");p.addDataSourceProperty("oracle.jdbc.ReadTimeout","15000");return p;
    }
    @Before public void seed() throws Exception {
        started=System.nanoTime();
        jdbc=new JdbcTemplate(firstPool);jdbc.setQueryTimeout(5);other=new JdbcTemplate(secondPool);other.setQueryTimeout(5);
        instance="dtko-"+token+"-"+UUID.randomUUID().toString().substring(0,8);device=instance;
        park=ThreadLocalRandom.current().nextInt(100000000,800000000);taskId=ThreadLocalRandom.current().nextInt(100000000,800000000);assertNotEquals(9001,park);assertNotEquals(9001,taskId);
        assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM SMT_DEVICE_TASK WHERE ID IN (?,?))+(SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE TASK_ID IN (?,?))+(SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_REVIEW WHERE TASK_KEY IN (?,?)) FROM DUAL",Integer.class,taskId,taskId+1,String.valueOf(taskId),String.valueOf(taskId+1),String.valueOf(taskId),String.valueOf(taskId+1)));
        assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID=?)+(SELECT COUNT(*) FROM SMT_DEVICE WHERE ID=?) FROM DUAL",Integer.class,instance,device));
        write(instance+"-fixture.txt","park="+park+" task="+taskId+" optionalTask="+(taskId+1)+" device="+device+" owner="+instance+" optionalOwner="+instance+"-moved\n");
        stateIds.add(instance);jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_STATE(INSTANCE_ID) VALUES (?)",instance);
        jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES (?,'DIRECT',?)",park,instance);
        jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID) VALUES (?,?)",device,park);
        task(taskId,device,1,1);first=runtime(firstPool,"");second=runtime(secondPool,"");
    }
    private RuntimeGate runtime(DataSource data,String historyTag) throws Exception {
        MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);cfg.addMapper(AuthOperationDirectTakeoverMapper.class);
        if(!historyTag.isEmpty())cfg.addInterceptor(new HistoryPlanTag(historyTag));
        MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(data);factory.setConfiguration(cfg);
        String root=Objects.requireNonNull(System.getenv("SMART_AUTH_DIRECT_TAKEOVER_PRODUCTION"));
        factory.setMapperLocations(new org.springframework.core.io.Resource[]{new FileSystemResource(root+"/smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/AuthOperationDirectTakeoverMapper.xml")});
        SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());AuthOperationDirectTakeoverMapper real=session.getMapper(AuthOperationDirectTakeoverMapper.class);
        AuthOperationDirectTakeoverMapper tracked=(AuthOperationDirectTakeoverMapper)Proxy.newProxyInstance(real.getClass().getClassLoader(),new Class<?>[]{AuthOperationDirectTakeoverMapper.class},(proxy,method,args)->{
            if(observeTransactions&&"task".equals(method.getName())) {
                assertTrue(TransactionSynchronizationManager.isActualTransactionActive());Connection c=DataSourceUtils.getConnection(data);
                try {assertFalse(c.getAutoCommit());try(Statement s=c.createStatement()){s.setQueryTimeout(5);try(ResultSet r=s.executeQuery("SELECT SYS_CONTEXT('USERENV','SID') FROM DUAL")){assertTrue(r.next());gateSids.add(r.getString(1));}}}
                finally {DataSourceUtils.releaseConnection(c,data);}
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){@Override public void afterCommit(){gateCommits.incrementAndGet();}});
            }
            try{return method.invoke(real,args);}catch(InvocationTargetException failed){throw failed.getCause();}
        });
        DataSourceTransactionManager manager=new DataSourceTransactionManager(data);return new RuntimeGate(new AuthOperationDirectTakeoverService(tracked,manager),real,new TransactionTemplate(manager));
    }
    private SmtDeviceTask task(int id,String targetDevice,int type,int action) {
        taskIds.add(id);jdbc.update("INSERT INTO SMT_DEVICE_TASK(ID,ACTION,STATUS,SERIAL_NO,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE,CARD_TYPE,GENERAL,START_TIME,OVER_TIME,TIMES) VALUES (?,?,0,?,?,?,?,?,1,?,10,20,0)",
            id,action,"serial-"+id,targetDevice,String.valueOf(id),type,type==1?1:4,type==1?"合成人员":"合成车牌");return read(id);
    }
    private SmtDeviceTask read(int id) {return first==null?readTask(jdbc,id):first.mapper.task(id);}
    private static SmtDeviceTask readTask(JdbcTemplate j,int id) {
        return j.queryForObject("SELECT ID,ACTION,STATUS,SERIAL_NO,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE,CARD_TYPE,GENERAL,START_TIME,OVER_TIME FROM SMT_DEVICE_TASK WHERE ID=?",(r,n)->{
            SmtDeviceTask t=new SmtDeviceTask();t.setId(r.getInt(1));t.setAction(r.getInt(2));t.setStatus(r.getInt(3));t.setSerialNo(r.getString(4));t.setDeviceCode(r.getString(5));t.setCardNo(r.getString(6));t.setDeviceType(r.getInt(7));t.setServiceType(r.getInt(8));t.setCardType(r.getInt(9));t.setGeneral(r.getString(10));t.setStartTime(r.getLong(11));t.setOverTime(r.getLong(12));return t;
        },id);
    }
    private long phase(SmtDeviceTask t,String phaseDevice,String phaseTask,int phasePark) {
        long id=IdWorker.getId();jdbc.update("INSERT INTO SMT_AUTH_TRANSPORT_PHASE(ID,TARGET_ID,ATTEMPT_ID,ATTEMPT_NO,LEASE_TOKEN,PARK_ID,INSTANCE_ID,ACCESS_TYPE,PHASE,STATE,TASK_ID,SERIAL_NO,DEVICE_ID,CARD_NO,ACTION,SERVICE_TYPE,RESOURCE_TYPE,CREDENTIAL_CHANNEL,START_TIME,OVER_TIME,SUBJECT_TYPE,SUBJECT_ID,RESOURCE_ID) VALUES (?,?,?,1,'synthetic-lease',?,?,'DIRECT','DIRECT_SEND','UNKNOWN',?,?,?,?,? ,?,'PERSON','FACE',10,20,'STAFF',?,?)",
            id,id,id,phasePark,instance,phaseTask,t.getSerialNo(),phaseDevice,t.getCardNo(),t.getAction()==2?"DELETE":"ADD",String.valueOf(t.getServiceType()),t.getCardNo(),"resource-"+id);return id;
    }
    private void claim(long phase,String targetDevice,String card) {
        jdbc.update("INSERT INTO SMT_AUTH_DIRECT_CLAIM(ID,DEVICE_ID,KEY_KIND,KEY_VALUE,SUBJECT_TYPE,SUBJECT_ID,PARK_ID,INSTANCE_ID,RESOURCE_ID,WIRE_HASH,FIRST_PHASE_ID,PROOF_KIND,CREATE_TIME) VALUES (?,?,'CARD_NO',?,'STAFF',?,?,?,?,?,?,'PHASE_HISTORY',SYS_EXTRACT_UTC(SYSTIMESTAMP))",
            "claim-"+phase,targetDevice,card,card,park,instance,"resource-"+phase,"synthetic-wire",phase);
    }
    @Test(timeout=30000) public void explicitZeroOneOwnerAndRealConstraintContract() {
        SmtDeviceTask t=read(taskId);assertTrue(first.service.admitLegacyDirect(taskId,LegacyIdentity.of(t)).legacyAllowed());
        try{first.service.assertDirectSendEnabled(park,instance);fail();}catch(IllegalStateException expected){}
        assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT DIRECT_TAKEOVER_VERSION FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=? AND ACCESS_TYPE='DIRECT'",Integer.class,park));
        jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES (?,'ISC',?)",park,instance);
        for(String sql:Arrays.asList("UPDATE SMT_AUTH_SCHEDULER_ROUTE SET DIRECT_TAKEOVER_VERSION=2 WHERE PARK_ID=?","UPDATE SMT_AUTH_SCHEDULER_ROUTE SET DIRECT_TAKEOVER_VERSION=NULL WHERE PARK_ID=?","UPDATE SMT_AUTH_SCHEDULER_ROUTE SET DIRECT_TAKEOVER_VERSION=1 WHERE ACCESS_TYPE='ISC' AND PARK_ID=?")) {
            try{jdbc.update(sql,park);fail("约束应拒绝非法能力");}catch(org.springframework.dao.DataAccessException expected){}
        }
        jdbc.update("UPDATE SMT_AUTH_SCHEDULER_ROUTE SET DIRECT_TAKEOVER_VERSION=1 WHERE PARK_ID=? AND ACCESS_TYPE='DIRECT'",park);
        first.service.assertDirectSendEnabled(park,instance);try{second.service.assertDirectSendEnabled(park,"wrong-owner");fail();}catch(IllegalStateException expected){}
        assertEquals(Outcome.VERIFYING,first.service.admitLegacyDirect(taskId,LegacyIdentity.of(t)).getOutcome());
        assertEquals(Outcome.VERIFYING,second.service.admitLegacyDirect(taskId,LegacyIdentity.of(t)).getOutcome());assertEquals(1,reviews());
    }
    @Test(timeout=30000) public void historicalPhaseAndClaimProtectDeviceAcrossDeletionMoveAndOtherCard() {
        SmtDeviceTask t=read(taskId);long phase=phase(t,device,String.valueOf(taskId),park+1);claim(phase,device,t.getCardNo());
        assertEquals(2,first.mapper.deviceHistory(device));
        String moved=instance+"-moved";stateIds.add(moved);jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_STATE(INSTANCE_ID) VALUES (?)",moved);
        jdbc.update("UPDATE SMT_AUTH_SCHEDULER_ROUTE SET INSTANCE_ID=? WHERE PARK_ID=? AND ACCESS_TYPE='DIRECT'",moved,park);
        jdbc.update("UPDATE SMT_DEVICE SET PARK_ID=? WHERE ID=?",park+2,device);
        Decision bound=second.service.admitLegacyReceipt(taskId,t.getSerialNo(),200,"synthetic");assertEquals(Outcome.OWNED_BY_TRANSPORT,bound.getOutcome());assertEquals(Long.valueOf(phase),bound.getPhase().getId());assertEquals(instance,bound.getPhase().getInstanceId());
        jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=?",device);assertEquals(Outcome.OWNED_BY_TRANSPORT,first.service.admitLegacyDirect(taskId,LegacyIdentity.of(t)).getOutcome());
        SmtDeviceTask another=task(taskId+1,device,1,1);assertEquals("DIRECT_DEVICE_HISTORY_PROTECTED",second.service.admitLegacyDirect(another.getId(),LegacyIdentity.of(another)).getReason());
        assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_REVIEW WHERE DEVICE_ID=? AND PARK_ID IS NULL",Integer.class,device));
    }
    @Test(timeout=30000) public void callerRollbackCannotEraseIndependentReview() {
        activate();SmtDeviceTask t=read(taskId);
        try{first.transaction.execute(status->{jdbc.update("UPDATE SMT_DEVICE_TASK SET REMARK='outer-uncommitted' WHERE ID=?",taskId);assertEquals(Outcome.VERIFYING,first.service.admitLegacyDirect(taskId,LegacyIdentity.of(t)).getOutcome());throw new IllegalStateException("caller rollback");});fail();}
        catch(IllegalStateException expected){assertEquals("caller rollback",expected.getMessage());}
        assertNull(other.queryForObject("SELECT REMARK FROM SMT_DEVICE_TASK WHERE ID=?",String.class,taskId));assertEquals(1,reviews());
        assertEquals(Integer.valueOf(0),other.queryForObject("SELECT STATUS FROM SMT_DEVICE_TASK WHERE ID=?",Integer.class,taskId));
    }
    @Test(timeout=30000) public void twoConnectionsSerializeAtRouteAndKeepOneStableReviewWithoutBusinessWrites() throws Exception {
        activate();SmtDeviceTask t=read(taskId);observeTransactions=true;ExecutorService e=worker(2);
        try{Future<Decision> a=e.submit(()->first.service.admitLegacyReceipt(taskId,t.getSerialNo(),200,"one"));Future<Decision> b=e.submit(()->second.service.admitLegacyReceipt(taskId,t.getSerialNo(),200,"two"));
            assertEquals("DIRECT_PARK_PROTECTED",a.get(12,TimeUnit.SECONDS).getReason());assertEquals("DIRECT_PARK_PROTECTED",b.get(12,TimeUnit.SECONDS).getReason());
            assertEquals(2,gateSids.size());assertEquals(2,gateCommits.get());assertEquals(1,reviews());assertEquals(Integer.valueOf(0),other.queryForObject("SELECT STATUS FROM SMT_DEVICE_TASK WHERE ID=?",Integer.class,taskId));
            write(instance+"-merge.txt","distinctOracleSids="+gateSids.size()+" committedGates="+gateCommits+" stableReviews=1\n");
        }finally{observeTransactions=false;}
    }
    @Test(timeout=30000) public void realRouteLockWaitsAtMostFiveSecondsAndFinalDispatcherRemainsUnused() throws Exception {
        activate();jdbc.update("UPDATE SMT_DEVICE_TASK SET DEVICE_TYPE=2,SERVICE_TYPE=4,ACTION=2 WHERE ID=?",taskId);SmtDeviceTask t=read(taskId);
        Connection blocker=secondPool.getConnection();blocker.setAutoCommit(false);ExecutorService e=worker(1);Future<Result> running=null;
        RemoteDispatcherService dispatch=mock(RemoteDispatcherService.class);DeviceTaskServiceImpl sender=sender(first.service,dispatch);
        try{try(PreparedStatement s=blocker.prepareStatement("SELECT PARK_ID FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=? AND ACCESS_TYPE='DIRECT' FOR UPDATE")){s.setQueryTimeout(5);s.setInt(1,park);try(ResultSet result=s.executeQuery()){assertTrue(result.next());}}
            long start=System.nanoTime();running=e.submit(()->sender.delCarCard(t,park));Result result=running.get(12,TimeUnit.SECONDS);long millis=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);
            assertEquals(Integer.valueOf(DeviceTaskServiceImpl.DIRECT_REVIEW_CODE),result.getCode());assertEquals("DIRECT_GATE_UNAVAILABLE",result.getMsg());assertTrue("实际DIRECT路由锁等待应触发5秒边界: "+millis,millis>=3500&&millis<11000);verifyZeroInteractions(dispatch);
            write(instance+"-timeout.txt","elapsedMs="+millis+" result="+result.getCode()+" http=0\n");
        }finally{blocker.rollback();blocker.close();if(running!=null&&!running.isDone())running.get(5,TimeUnit.SECONDS);}
    }
    @Test(timeout=30000) public void lastGateCommitsBeforeActualLoopbackHttp() throws Exception {
        jdbc.update("UPDATE SMT_DEVICE_TASK SET DEVICE_TYPE=2,SERVICE_TYPE=4,ACTION=2 WHERE ID=?",taskId);SmtDeviceTask t=read(taskId);observeTransactions=true;
        AtomicInteger requests=new AtomicInteger();ExecutorService httpWorkers=worker(1);HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.setExecutor(httpWorkers);
        server.createContext("/synthetic-dispatch",exchange->{try{while(exchange.getRequestBody().read()!=-1){}requests.incrementAndGet();byte[] response="{\"code\":0}".getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);}finally{exchange.close();}});server.start();
        RemoteDispatcherService dispatch=mock(RemoteDispatcherService.class);
        when(dispatch.dispatch(any(),anyString())).thenAnswer(call->{assertFalse(TransactionSynchronizationManager.isActualTransactionActive());assertEquals(1,gateCommits.get());
            URL url=new URL("http://127.0.0.1:"+server.getAddress().getPort()+"/synthetic-dispatch");HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(1000);c.setReadTimeout(1000);c.setRequestMethod("POST");c.setDoOutput(true);
            try{c.getOutputStream().write("synthetic-only".getBytes(StandardCharsets.UTF_8));assertEquals(200,c.getResponseCode());while(c.getInputStream().read()!=-1){}return Result.success(null);}finally{c.disconnect();}});
        try{assertTrue(sender(first.service,dispatch).delCarCard(t,park).isSuccess());assertEquals(1,requests.get());assertEquals(1,gateSids.size());assertEquals(0,reviews());write(instance+"-http.txt","gateCommitted=1 gateTransactionActive=true httpCallerTransactionActive=false loopbackRequests=1\n");}
        finally{server.stop(0);}
    }
    @Test(timeout=75000) public void missingDeviceHistoryCollectsExactTaggedCursorAfterSyntheticGrowth() throws Exception {
        final int rows=1000;Connection c=firstPool.getConnection();c.setAutoCommit(false);
        try(PreparedStatement p=c.prepareStatement("INSERT INTO SMT_AUTH_TRANSPORT_PHASE(ID,TARGET_ID,ATTEMPT_ID,PARK_ID,INSTANCE_ID,ACCESS_TYPE,PHASE,STATE,TASK_ID,DEVICE_ID,ATTEMPT_NO,LEASE_TOKEN,SERIAL_NO,CARD_NO,SUBJECT_TYPE,SUBJECT_ID,RESOURCE_ID,ACTION,SERVICE_TYPE,RESOURCE_TYPE,CREDENTIAL_CHANNEL,START_TIME,OVER_TIME) VALUES (?,?,?, ?,?,'DIRECT','DIRECT_SEND','UNKNOWN',?,?,1,'synthetic-lease',?,?,'STAFF',?,?,'ADD','1','PERSON','FACE',10,20)");PreparedStatement q=c.prepareStatement("INSERT INTO SMT_AUTH_DIRECT_CLAIM(ID,DEVICE_ID,KEY_KIND,KEY_VALUE,SUBJECT_TYPE,SUBJECT_ID,PARK_ID,INSTANCE_ID,RESOURCE_ID,WIRE_HASH,FIRST_PHASE_ID,PROOF_KIND,CREATE_TIME) VALUES (?,?,'CARD_NO',?,'STAFF',?,?,?,?,'synthetic-wire',?,'PHASE_HISTORY',SYS_EXTRACT_UTC(SYSTIMESTAMP))")) {
            p.setQueryTimeout(5);q.setQueryTimeout(5);
            for(int i=0;i<rows;i++){long id=IdWorker.getId();String d=device+"-h"+i,card=String.valueOf(i+1);
                p.setLong(1,id);p.setLong(2,id);p.setLong(3,id);p.setInt(4,park);p.setString(5,instance);p.setString(6,"history-"+id);p.setString(7,d);p.setString(8,"history-serial-"+id);p.setString(9,card);p.setString(10,card);p.setString(11,"resource-"+id);p.addBatch();
                q.setString(1,"claim-"+id);q.setString(2,d);q.setString(3,card);q.setString(4,card);q.setInt(5,park);q.setString(6,instance);q.setString(7,"resource-"+id);q.setLong(8,id);q.addBatch();
                if((i+1)%100==0){p.executeBatch();q.executeBatch();}
            }c.commit();
        }catch(Exception failed){c.rollback();throw failed;}finally{c.close();}
        write("history-stats.txt",jdbc.queryForList("SELECT TABLE_NAME,NUM_ROWS,BLOCKS,LAST_ANALYZED,STALE_STATS FROM USER_TAB_STATISTICS WHERE TABLE_NAME IN ('SMT_AUTH_TRANSPORT_PHASE','SMT_AUTH_DIRECT_CLAIM')").toString()+"\n");
        String marker="DTKO_"+token+"_PLAN";RuntimeGate plan=runtime(firstPool,marker);for(int i=0;i<3;i++)assertEquals(0,plan.mapper.deviceHistory(device+"-absent"));
        write("history-plan-request.json","{\"marker\":\""+marker+"\",\"executions\":3,\"phaseFixtureRows\":1000,\"claimFixtureRows\":1000,\"result\":0,\"format\":\"ALLSTATS LAST +PREDICATE +ALIAS\"}\n");
        write("history-plan-input.sql",HistoryPlanTag.lastSql+"\n");
        System.out.println("PLAN_READY marker="+marker+" evidence="+evidence);
        Path ack=evidence.resolve("history-plan.ack");long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(45);while(!Files.exists(ack)&&System.nanoTime()<deadline)Thread.sleep(100);
        assertTrue("仅等父按唯一comment完成授权SYS只读计划后写ack，最多45秒",Files.exists(ack));
        String receipt=new String(Files.readAllBytes(ack),StandardCharsets.UTF_8);
        assertTrue("ack必须绑定本次唯一计划标记",receipt.contains("marker="+marker));assertTrue("ack必须记录SYS计划SQL ID",receipt.matches("(?s).*sql_id=[a-z0-9]{13}.*"));
        assertTrue("ack前必须留下SYS只读计划证据",Files.isRegularFile(evidence.resolve("history-plan-sys.txt")));
    }
    private void activate(){jdbc.update("UPDATE SMT_AUTH_SCHEDULER_ROUTE SET DIRECT_TAKEOVER_VERSION=1 WHERE PARK_ID=? AND ACCESS_TYPE='DIRECT'",park);}
    private int reviews(){return other.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_REVIEW WHERE DEVICE_ID=?",Integer.class,device);}
    private ExecutorService worker(int n){ExecutorService e=Executors.newFixedThreadPool(n);workers.add(e);return e;}
    private DeviceTaskServiceImpl sender(AuthOperationDirectTakeoverService service,RemoteDispatcherService dispatcher) {
        DeviceTaskServiceImpl sender=new DeviceTaskServiceImpl(mock(SmtDeviceTaskService.class),mock(SmtDeviceService.class),dispatcher,mock(SmtImageService.class),mock(SmtVisitorService.class),mock(DirectTaskCompletionService.class),mock(RemoteStaffService.class));
        AuthOperationProperties flags=new AuthOperationProperties();flags.setEnabled(false);AuthOperationTransportGuard guard=new AuthOperationTransportGuard(firstPool,flags);guard.setDirectTakeover(service);sender.setTransportGuard(guard);return sender;
    }
    @After public void cleanup() throws Exception {
        observeTransactions=false;for(ExecutorService e:workers)e.shutdownNow();for(ExecutorService e:workers)assertTrue("线程退出前不能释放Oracle窗口",e.awaitTermination(8,TimeUnit.SECONDS));
        if(jdbc==null||instance==null)return;
        jdbc.update("DELETE FROM SMT_AUTH_DIRECT_CLAIM WHERE INSTANCE_ID=?",instance);jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=?",instance);
        for(Integer id:taskIds)jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_REVIEW WHERE ACCESS_TYPE='DIRECT' AND TASK_KEY=?",String.valueOf(id));
        jdbc.update("DELETE FROM SMT_DEVICE_TASK WHERE DEVICE_CODE=?",device);jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=?",device);
        for(String owner:stateIds){jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_ROUTE WHERE INSTANCE_ID=?",owner);jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID=?",owner);}
        assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM SMT_AUTH_DIRECT_CLAIM WHERE INSTANCE_ID=?)+(SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=?)+(SELECT COUNT(*) FROM SMT_DEVICE_TASK WHERE DEVICE_CODE=?)+(SELECT COUNT(*) FROM SMT_DEVICE WHERE ID=?) FROM DUAL",Integer.class,instance,instance,device,device));
        assertEquals(0,reviews());write(instance+"-cleanup.txt","ownSyntheticRows=0\n");
    }
    @AfterClass public static void closePools() {
        if(firstPool!=null)firstPool.close();if(secondPool!=null)secondPool.close();
        if(firstPool!=null&&secondPool!=null){assertTrue(firstPool.isClosed());assertTrue(secondPool.isClosed());System.out.println("DIRECT_TAKEOVER_POOLS_CLOSED=true");}
    }
    private static void write(String name,String value) throws java.io.IOException {Files.write(evidence.resolve(name),value.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);}
    private static final class RuntimeGate {final AuthOperationDirectTakeoverService service;final AuthOperationDirectTakeoverMapper mapper;final TransactionTemplate transaction;RuntimeGate(AuthOperationDirectTakeoverService s,AuthOperationDirectTakeoverMapper m,TransactionTemplate t){service=s;mapper=m;transaction=t;transaction.setTimeout(8);}}
    /** 只增加本轮唯一标签与行源采样，不改变访问路径、不flush或改统计。 */
    @Intercepts(@Signature(type=StatementHandler.class,method="prepare",args={Connection.class,Integer.class}))
    public static class HistoryPlanTag implements Interceptor {
        private final String marker;static volatile String lastSql;
        public HistoryPlanTag(String marker){this.marker=marker;}
        public Object intercept(Invocation invocation) throws Throwable {
            StatementHandler s=(StatementHandler)invocation.getTarget();String sql=s.getBoundSql().getSql().trim();
            if(sql.contains("SMT_AUTH_DIRECT_CLAIM")&&sql.contains("ROWNUM=1")){lastSql=sql.replaceFirst("SELECT","SELECT /*+ GATHER_PLAN_STATISTICS */ /*"+marker+"*/");SystemMetaObject.forObject(s.getBoundSql()).setValue("sql",lastSql);}
            return invocation.proceed();
        }
        public Object plugin(Object target){return Plugin.wrap(target,this);}public void setProperties(Properties properties){}
    }
}
