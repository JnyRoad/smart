package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSchedulerData.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.*;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/** 只验证合成 Oracle 上的真实领取、限额、游标及恢复 SQL；两万候选不是容量验收。 */
public class AuthOperationSchedulerOracleTest {
    private static com.zaxxer.hikari.HikariDataSource pool;
    @AfterClass public static void closePool(){if(pool!=null){pool.close();pool=null;}}
    private JdbcTemplate jdbc;
    private AuthOperationService operations;
    private AuthOperationSchedulerService ledger;
    private AuthOperationSchedulerMapper mapper;
    private SmtAuthOperationTargetMapper targets;
    private final List<Long> batches=new ArrayList<>();
    private final Set<String> routeInstances=new HashSet<>();
    private Policy policy;
    private DataSourceTransactionManager transactions;

    @Before public void setUp() throws Exception {
        String url=System.getenv("SMART_AUTH_ORACLE_URL");Assume.assumeTrue(url!=null&&!url.isEmpty());
        Assert.assertTrue(url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        if(pool==null){pool=new com.zaxxer.hikari.HikariDataSource();pool.setJdbcUrl(url);pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setConnectionTimeout(5000);pool.setPoolName("scheduler-oracle-fixture");}
        javax.sql.DataSource data=pool;jdbc=new JdbcTemplate(data);jdbc.setQueryTimeout(20);transactions=new DataSourceTransactionManager(data);
        MybatisConfiguration configuration=new MybatisConfiguration();configuration.setMapUnderscoreToCamelCase(true);
        for(Class<?> type:Arrays.asList(SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,AuthOperationSchedulerMapper.class))configuration.addMapper(type);
        MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(data);factory.setConfiguration(configuration);
        List<Resource> resources=new ArrayList<>();for(String name:Arrays.asList("SmtAuthOperationBatchMapper","SmtAuthDeleteRequestMapper","SmtAuthOperationTargetMapper","SmtAuthOperationAttemptMapper","SmtAuthResultEventMapper","AuthOperationSchedulerMapper"))resources.add(new ClassPathResource("mapper/"+name+".xml"));
        factory.setMapperLocations(resources.toArray(new Resource[0]));SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());
        targets=session.getMapper(SmtAuthOperationTargetMapper.class);mapper=session.getMapper(AuthOperationSchedulerMapper.class);
        operations=proxy(new AuthOperationService(session.getMapper(SmtAuthOperationBatchMapper.class),session.getMapper(SmtAuthDeleteRequestMapper.class),targets,session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(SmtAuthResultEventMapper.class)));
        ledger=proxy(new AuthOperationSchedulerService(mapper,operations));
        policy=new Policy();policy.setInstanceId("scheduler-test-"+UUID.randomUUID());policy.setAccessType("ISC");int park=100000+(int)(IdWorker.getId()%500000000);
        policy.setParks(Arrays.asList(park,park+1));policy.setHttpPerSecond(100);policy.setDeleteHttp(25);policy.setAddHttp(25);policy.setConfigHttp(15);policy.setReceiptHttp(25);policy.setBorrowHttp(10);
        policy.setMaxInflight(400);policy.setDeleteInflight(100);policy.setAddInflight(50);policy.setPerDeviceInflight(25);
        routeInstances.add(policy.getInstanceId());
    }
    @Test public void differentInstancesCannotConcurrentlyOwnSameParkAndAccess() throws Exception {
        Policy other=anotherPolicy("ISC");other.setParks(Arrays.asList(policy.getParks().get(1),policy.getParks().get(0)));
        ExecutorService workers=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try {
            Future<Boolean> first=workers.submit(()->{start.await();return tryRegister(policy);});
            Future<Boolean> second=workers.submit(()->{start.await();return tryRegister(other);});start.countDown();
            Assert.assertTrue("同园区接入只能有一个持久实例归属",first.get(20,TimeUnit.SECONDS)^second.get(20,TimeUnit.SECONDS));
        } finally {workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(10,TimeUnit.SECONDS));}
    }
    @Test public void sameInstanceRegistrationIsIdempotentAcrossParks() {
        Assert.assertNotNull(ledger.reserve(policy,"EXPIRE",200,30));Assert.assertNotNull(ledger.reserve(policy,"RECOVER",200,30));
        Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SCHEDULER_ROUTE WHERE INSTANCE_ID=?",Integer.class,policy.getInstanceId()));
    }
    @Test public void sameParkCanUseIndependentAccessTypes() {
        Assert.assertNotNull(ledger.reserve(policy,"EXPIRE",200,30));Policy direct=anotherPolicy("DIRECT");
        Assert.assertNotNull(ledger.reserve(direct,"EXPIRE",200,30));
        Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=?",Integer.class,policy.getParks().get(0)));
    }
    @Test public void conflictingRouteRollsBackNewMappingsAndNeverReassignsOwner() {
        List<Integer> both=new ArrayList<>(policy.getParks());policy.setParks(Collections.singletonList(both.get(1)));
        Assert.assertNotNull(ledger.reserve(policy,"EXPIRE",200,30));Policy other=anotherPolicy("ISC");other.setParks(both);
        Assert.assertFalse(tryRegister(other));
        Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=?",Integer.class,both.get(0)));
        Assert.assertEquals(policy.getInstanceId(),jdbc.queryForObject("SELECT INSTANCE_ID FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=? AND ACCESS_TYPE='ISC'",String.class,both.get(1)));
        Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID=?",Integer.class,other.getInstanceId()));
    }
    private boolean tryRegister(Policy candidate) {
        try {return ledger.reserve(candidate,"EXPIRE",200,30)!=null;}catch(IllegalStateException conflict) {
            Assert.assertTrue("只允许预期路由冲突",conflict.getMessage().contains("路由"));return false;
        }
    }
    private Policy anotherPolicy(String access) {
        Policy another=new Policy();org.springframework.beans.BeanUtils.copyProperties(policy,another);
        another.setInstanceId("scheduler-test-"+UUID.randomUUID());another.setParks(new ArrayList<>(policy.getParks()));another.setAccessType(access);
        routeInstances.add(another.getInstanceId());return another;
    }
    @Test public void exactCandidatesEnforceParkAccessQueueEmptyAndDuplicateIds() {
        long target=fixture(policy.getParks().get(0),"DELETE","ADD","a",1).get(0);
        Assert.assertTrue(targets.selectExactClaimCandidates(policy.getParks().get(0),"AUTH","ISC",Collections.emptyList(),LocalDateTime.now(),200).isEmpty());
        Assert.assertTrue(targets.selectExactClaimCandidates(policy.getParks().get(1),"AUTH","ISC",Arrays.asList(target),LocalDateTime.now(),200).isEmpty());
        Assert.assertTrue(targets.selectExactClaimCandidates(policy.getParks().get(0),"AUTH","DIRECT",Arrays.asList(target),LocalDateTime.now(),200).isEmpty());
        Assert.assertTrue(targets.selectExactClaimCandidates(policy.getParks().get(0),"OTHER","ISC",Arrays.asList(target),LocalDateTime.now(),200).isEmpty());
        List<AuthOperationClaimedTarget> claimed=operations.claim(AuthOperationClaimCommand.builder().parkId(policy.getParks().get(0)).operationQueue("AUTH").accessType("ISC").targetIds(Arrays.asList(target,target)).maxCount(200).leaseSeconds(30L).build());
        Assert.assertEquals(1,claimed.size());Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID=?",Integer.class,target));
    }
    @Test public void physicalAddFromDeleteBatchUsesDeleteReservation() {
        long id=fixture(policy.getParks().get(0),"DELETE","ADD","retained",1).get(0);
        Grant grant=ledger.reservePark(policy,"DELETE",policy.getParks().get(0),200,30);Assert.assertNotNull(grant);Assert.assertEquals(Long.valueOf(id),grant.getClaims().get(policy.getParks().get(0)).get(0).getTargetId());
        Assert.assertTrue(mapper.candidates(policy,"ADD",null,mapper.now(),200).isEmpty());
    }
    @Test public void verifyingInOneParkCannotConsumeHealthyParkMinimum() {
        int busy=policy.getParks().get(0),healthy=policy.getParks().get(1);
        fixture(busy,"ADD","ADD","unknown",100);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='VERIFYING' WHERE BATCH_ID=?",batches.get(0));
        fixture(busy,"ADD","ADD","another-device",1);long expected=fixture(healthy,"ADD","ADD","healthy",1).get(0);
        Grant blocked=ledger.reservePark(policy,"ADD",busy,200,30);
        Assert.assertTrue("单园区已满不能再占实例在途",blocked==null || blocked.getClaims().isEmpty());
        Grant available=ledger.reservePark(policy,"ADD",healthy,200,30);
        Assert.assertEquals(Long.valueOf(expected),available.getClaims().get(healthy).get(0).getTargetId());
        long deletion=fixture(busy,"DELETE","ADD","healthy-delete",1).get(0);
        Grant removal=ledger.reservePark(policy,"DELETE",busy,200,30);
        Assert.assertEquals(Long.valueOf(deletion),removal.getClaims().get(busy).get(0).getTargetId());
        Assert.assertEquals(Integer.valueOf(100),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=? AND STATE='VERIFYING'",Integer.class,batches.get(0)));
    }
    @Test public void addInflightCannotBorrowDeleteReservation() {
        policy.setPerParkDeleteInflight(2);policy.setPerParkAddInflight(1);policy.setPerDeviceDeleteInflight(2);policy.setPerDeviceAddInflight(1);policy.setPerParkInflight(8);policy.setMinParkInflight(1);policy.setMaxInflight(8);policy.setDeleteInflight(2);policy.setAddInflight(1);policy.setPerDeviceInflight(8);
        fixture(policy.getParks().get(0),"ADD","ADD","busy",6);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='VERIFYING' WHERE BATCH_ID=?",batches.get(0));
        fixture(policy.getParks().get(1),"ADD","ADD","healthy",1);
        Long removal=fixture(policy.getParks().get(1),"DELETE","ADD","shared-retained",1).get(0);
        Grant add=ledger.reservePark(policy,"ADD",policy.getParks().get(1),200,30);Assert.assertNull(add);
        Assert.assertNotNull(jdbc.queryForObject("SELECT NEXT_ATTEMPT_AT FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",java.sql.Timestamp.class,batches.get(1)));
        Grant deletion=ledger.reservePark(policy,"DELETE",policy.getParks().get(1),200,30);Assert.assertEquals(removal,deletion.getClaims().get(policy.getParks().get(1)).get(0).getTargetId());
    }
    @Test public void concurrentIndependentLanesShareOneInstanceRateWindow() throws Exception {
        phaseFixture(policy.getParks().get(0),"SUBMIT","ADD","add",1);
        phaseFixture(policy.getParks().get(0),"SUBMIT","DELETE","delete",1);
        phaseFixture(policy.getParks().get(0),"CONFIG_PROGRESS","ADD","config",1);
        phaseFixture(policy.getParks().get(0),"RECEIPT","ADD","receipt",1);
        ExecutorService workers=Executors.newFixedThreadPool(4);CountDownLatch start=new CountDownLatch(1);
        try {
            List<Future<Grant>> futures=new ArrayList<>();for(String lane:Arrays.asList("ADD","DELETE","CONFIG","RECEIPT"))futures.add(workers.submit(()->{start.await();return ledger.reservePark(policy,lane,policy.getParks().get(0),200,30);}));
            start.countDown();Map<String,Integer> windows=new HashMap<>();for(Future<Grant> f:futures){Grant g=f.get(20,TimeUnit.SECONDS);windows.merge(g.getWindowKey(),g.getHttpBudget(),Integer::sum);}
            Assert.assertFalse(windows.isEmpty());for(Integer count:windows.values())Assert.assertTrue("同数据库秒窗共享总额度",count<=100);
        } finally {workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(10,TimeUnit.SECONDS));}
    }
    @Test public void twoWorkersCannotDoubleClaimOrDoubleReserveOneLane() throws Exception {
        fixture(policy.getParks().get(0),"ADD","ADD","healthy",40);
        ExecutorService workers=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        try {
            Future<Grant> first=workers.submit(()->{start.await();return ledger.reservePark(policy,"ADD",policy.getParks().get(0),200,30);});
            Future<Grant> second=workers.submit(()->{start.await();return ledger.reservePark(policy,"ADD",policy.getParks().get(0),200,30);});start.countDown();
            Grant a=first.get(20,TimeUnit.SECONDS),b=second.get(20,TimeUnit.SECONDS);Assert.assertTrue((a==null)!=(b==null));
            Assert.assertEquals(Integer.valueOf(20),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?)",Integer.class,batches.get(0)));
            Assert.assertTrue(jdbc.queryForObject("SELECT TOTAL_USED FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID=?",Integer.class,policy.getInstanceId())<=35);
        } finally {workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(10,TimeUnit.SECONDS));}
    }
    @Test public void twoParksAndManyTargetsDoNotHideHealthyDevice() {
        List<Long> large=fixture(policy.getParks().get(0),"ADD","ADD","offline",1);
        long first=large.get(0),seed=IdWorker.getId();
        jdbc.update("INSERT INTO SMT_AUTH_OPERATION_TARGET(ID,BATCH_ID,REQUEST_ID,PARK_ID,TARGET_KEY,SUBJECT_TYPE,SUBJECT_ID,RESOURCE_TYPE,DEVICE_ID,RESOURCE_ID,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE,ACCEPTED_AT,CREATE_TIME,UPDATE_TIME) "+
            "SELECT ?+n.N,BATCH_ID,REQUEST_ID,PARK_ID,'extra-'||n.N,SUBJECT_TYPE,'subject-'||n.N,RESOURCE_TYPE,DEVICE_ID,'resource-'||n.N,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE,ACCEPTED_AT,CREATE_TIME,UPDATE_TIME FROM (SELECT * FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?) t CROSS JOIN (SELECT LEVEL N FROM DUAL CONNECT BY LEVEL<=20000) n",seed,first);
        long healthy=fixture(policy.getParks().get(0),"ADD","ADD","healthy",1).get(0);
        long otherPark=fixture(policy.getParks().get(1),"ADD","ADD","other-park",1).get(0);
        List<Candidate> page=mapper.candidates(policy,"ADD",null,mapper.now(),3);Set<Long> ids=new HashSet<>();for(Candidate c:page)ids.add(c.getId());
        Assert.assertTrue(ids.contains(healthy));Assert.assertTrue(ids.contains(otherPark));Assert.assertEquals(3,page.size());
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET NEXT_ATTEMPT_AT=SYSTIMESTAMP+INTERVAL '5' MINUTE WHERE BATCH_ID=?",batches.get(0));
        Assert.assertEquals(2,mapper.candidates(policy,"ADD",null,mapper.now(),200).size());
    }
    @Test public void restartPreservesEachParkCursorAndFailureDoesNotAdvance() {
        phaseFixture(policy.getParks().get(0),"RECEIPT","ADD","one",1);phaseFixture(policy.getParks().get(1),"RECEIPT","ADD","two",1);
        Grant first=ledger.reservePark(policy,"RECEIPT",policy.getParks().get(0),200,30);Assert.assertTrue(ledger.complete(first,true,101L,null,0,0));
        Grant second=ledger.reservePark(policy,"RECEIPT",policy.getParks().get(1),200,30);Assert.assertNull(second.getJob().getNumberCursor());ledger.complete(second,true,202L,null,0,0);
        jdbc.update("UPDATE SMT_AUTH_SCHEDULER_QUOTA SET WINDOW_KEY='old' WHERE INSTANCE_ID=?",policy.getInstanceId());
        Grant resumed=ledger.reservePark(policy,"RECEIPT",policy.getParks().get(0),200,30);Assert.assertEquals(Long.valueOf(101L),resumed.getJob().getNumberCursor());ledger.complete(resumed,false,999L,null,0,50);
        Assert.assertNull(ledger.reservePark(policy,"RECEIPT",policy.getParks().get(0),200,30));
        Assert.assertEquals(Long.valueOf(101L),jdbc.queryForObject("SELECT NUMBER_CURSOR FROM SMT_AUTH_SCHEDULER_JOB WHERE INSTANCE_ID=? AND LANE=?",Long.class,policy.getInstanceId(),"RECEIPT:"+policy.getParks().get(0)));
    }
    @Test public void expiredUnsubmittedClaimRequeuesButUnknownDoesNot() {
        fixture(policy.getParks().get(0),"DELETE","DELETE","device",2);Grant grant=ledger.reservePark(policy,"DELETE",policy.getParks().get(0),200,30);
        List<AuthOperationClaimedTarget> claims=grant.getClaims().get(policy.getParks().get(0));
        for(AuthOperationClaimedTarget c:claims)jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_UNTIL=SYSTIMESTAMP-INTERVAL '1' MINUTE WHERE ID=?",c.getTargetId());
        jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET STATUS='WAITING_CONFIRM' WHERE ID=?",claims.get(1).getAttemptId());
        Assert.assertTrue(ledger.expire(claims.get(0).getTargetId()));Assert.assertFalse(ledger.expire(claims.get(1).getTargetId()));
        Assert.assertEquals("QUEUED",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,claims.get(0).getTargetId()));
        Assert.assertEquals("EXECUTING",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,claims.get(1).getTargetId()));
        Assert.assertEquals(0,targets.markWaitingConfirmByLease(claims.get(0).getTargetId(),claims.get(0).getLeaseToken(),LocalDateTime.now()));
    }
    @Test public void recoveryQueriesStayBoundedAndBadItemIsIndependentlyDelayed() {
        Grant grant=ledger.reserve(policy,"RECOVER",200,30);Assert.assertTrue(ledger.recoveries(policy,null,2).isEmpty());Assert.assertTrue(ledger.convergences(policy,null,2).isEmpty());Assert.assertTrue(ledger.refreshTargets(policy,null,2).isEmpty());
        ledger.itemResult(policy.getInstanceId(),"B:bad",false,25);Assert.assertFalse(ledger.itemDue(policy.getInstanceId(),"B:bad"));Assert.assertTrue(ledger.itemDue(policy.getInstanceId(),"B:healthy"));ledger.complete(grant,true,null,null,0,0);
        Assert.assertEquals("RECOVER",ledger.snapshot(policy).getJobs().get(0).getLane());
    }
    private List<Long> phaseFixture(int park,String operation,String priority,String device,int count) {
        List<Long> targets=fixture(park,priority,"ADD",device,count),ids=new ArrayList<>();String request="group-"+IdWorker.getId();
        String phase="RECEIPT".equals(operation)||"DOWNLOAD".equals(operation)?"ISC_DOWNLOAD":"ISC_CONFIG";
        String state="SUBMIT".equals(operation)||"DOWNLOAD".equals(operation)?"PREPARED":"ACCEPTED";
        for(Long target:targets) {
            Long id=IdWorker.getId();ids.add(id);
            jdbc.update("INSERT INTO SMT_AUTH_TRANSPORT_PHASE(ID,TARGET_ID,ATTEMPT_ID,PARK_ID,INSTANCE_ID,ACCESS_TYPE,PHASE,STATE,TASK_ID,DEVICE_ID,ACTION,RESOURCE_TYPE,SERVICE_TYPE,CREDENTIAL_CHANNEL,CHANNEL_NO,START_TIME,OVER_TIME,REQUEST_KEY,PAGE_NO) VALUES(?,?,?,?,?,'ISC',?,?,?,?,'ADD','PERSON','1','FACE',0,0,0,?,1)",id,target,IdWorker.getId(),park,policy.getInstanceId(),phase,state,"task-"+id,device,request);
            jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='WAITING_CONFIRM' WHERE ID=?",target);
        }
        return ids;
    }
    private void freezeClock() {
        AuthOperationSchedulerMapper clock=org.mockito.Mockito.mock(AuthOperationSchedulerMapper.class,org.mockito.AdditionalAnswers.delegatesTo(mapper));LocalDateTime now=mapper.now();String window=mapper.windowKey();
        org.mockito.Mockito.doReturn(now).when(clock).now();org.mockito.Mockito.doReturn(window).when(clock).windowKey();
        ledger=proxy(new AuthOperationSchedulerService(clock,operations));
    }
    @Test public void deviceAndParkRatesRemainBoundedAfterFastConfirmations() {
        freezeClock();int park=policy.getParks().get(0);
        List<Long> group=phaseFixture(park,"SUBMIT","ADD","fast",3);
        Grant first=ledger.reservePark(policy,"ADD",park,200,30);Assert.assertEquals(group,first.getPhaseIds());Assert.assertEquals(3,first.getHttpBudget());ledger.complete(first,true,group.get(2),null,0,0);
        jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET STATE='FINISHED' WHERE DEVICE_ID='fast' AND INSTANCE_ID=?",policy.getInstanceId());
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='CONVERGED' WHERE BATCH_ID=?",batches.get(0));
        phaseFixture(park,"SUBMIT","ADD","fast",1);
        Assert.assertNull("快速ACK释放在途不能重置设备秒速率",ledger.reservePark(policy,"ADD",park,200,30));
        for(int n=0;n<3;n++) {phaseFixture(park,"SUBMIT","ADD","other-"+n,1);Grant g=ledger.reservePark(policy,"ADD",park,200,30);Assert.assertNotNull(g);ledger.complete(g,true,null,null,0,0);}
        phaseFixture(park,"SUBMIT","ADD","park-full",1);Assert.assertNull("园区ADD保留12HTTP已耗尽",ledger.reservePark(policy,"ADD",park,200,30));
        int other=policy.getParks().get(1);phaseFixture(other,"SUBMIT","ADD","healthy",1);Assert.assertNotNull("其他园区仍有保留HTTP",ledger.reservePark(policy,"ADD",other,200,30));
    }
    @Test public void deletePriorityAlternatesWithAddInConfigAndReceiptBacklogs() {
        freezeClock();int park=policy.getParks().get(0);
        for(String lane:Arrays.asList("CONFIG","RECEIPT")) {
            String operation="CONFIG".equals(lane)?"CONFIG_PROGRESS":"RECEIPT";
            phaseFixture(park,operation,"ADD","bulk-"+lane,20);
            List<Long> deletion=phaseFixture(park,operation,"DELETE","delete-"+lane,2);
            Grant priority=ledger.reservePark(policy,lane,park,200,30);Assert.assertEquals(deletion,priority.getPhaseIds());ledger.complete(priority,true,deletion.get(1),priority.getJob().getTextCursor(),0,0);
            Grant add=ledger.reservePark(policy,lane,park,200,30);Assert.assertNotNull("撤权不能反向饿死新增",add);Assert.assertEquals(20,add.getPhaseIds().size());Assert.assertEquals(1,add.getHttpBudget());
        }
    }
    @Test public void oneItemDownloadPageMustIncludeDeleteSeedOnSameDevice() {
        int park=policy.getParks().get(0);phaseFixture(park,"DOWNLOAD","ADD","shared-device",3);
        List<Long> deletion=phaseFixture(park,"DOWNLOAD","DELETE","shared-device",1);
        Grant grant=ledger.reservePark(policy,"CONFIG",park,1,30);
        Assert.assertEquals("物理ADD相同也不能让旧ADD挤掉本轮DELETE种子",deletion,grant.getPhaseIds());
    }
    @Test public void deviceReservesDeleteButLegacyParkHardLimitStillApplies() {
        int park=policy.getParks().get(0);
        fixture(park,"ADD","ADD","same-device",20);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='VERIFYING' WHERE BATCH_ID=?",batches.get(0));
        fixture(park,"ADD","ADD","same-device",1);
        List<Long> deletion=fixture(park,"DELETE","ADD","same-device",6);
        Assert.assertTrue(mapper.candidates(policy,"ADD",null,mapper.now(),200).isEmpty());
        List<Candidate> candidates=mapper.candidates(policy,"DELETE",null,mapper.now(),200);
        Assert.assertEquals("设备25硬界减20未知仅允许5个撤权",5,candidates.size());
        Grant removal=ledger.reservePark(policy,"DELETE",park,200,30);Assert.assertEquals(5,removal.getClaims().get(park).size());
        Assert.assertEquals(Integer.valueOf(20),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=? AND STATE='VERIFYING'",Integer.class,batches.get(0)));
    }
    @Test public void explicitLegacyPark100NeverExceedsItsConfiguredHardLimit() {
        policy.setPerParkInflight(100);int park=policy.getParks().get(0);
        fixture(park,"ADD","ADD","unknown",1);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE='VERIFYING' WHERE BATCH_ID=?",batches.get(0));
        long seed=IdWorker.getId();
        jdbc.update("INSERT INTO SMT_AUTH_OPERATION_TARGET(ID,BATCH_ID,REQUEST_ID,PARK_ID,TARGET_KEY,SUBJECT_TYPE,SUBJECT_ID,RESOURCE_TYPE,DEVICE_ID,RESOURCE_ID,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE,ACCEPTED_AT,CREATE_TIME,UPDATE_TIME) SELECT ?+n.N,BATCH_ID,REQUEST_ID,PARK_ID,'legacy-'||n.N,SUBJECT_TYPE,'subject-'||n.N,RESOURCE_TYPE,DEVICE_ID,'resource-'||n.N,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE,ACCEPTED_AT,CREATE_TIME,UPDATE_TIME FROM (SELECT * FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?) t CROSS JOIN (SELECT LEVEL N FROM DUAL CONNECT BY LEVEL<=99) n",seed,batches.get(0));
        fixture(park,"DELETE","ADD","healthy",1);
        Assert.assertNull("旧显式100已满时须调整配置或等待真实收敛，禁止伪造释放",ledger.reservePark(policy,"DELETE",park,200,30));
        Assert.assertEquals(Integer.valueOf(100),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=? AND STATE='VERIFYING'",Integer.class,batches.get(0)));
    }
    @Test public void acceptedOriginalRequestGroupIncludesDeleteSeedEvenWhenPageIsOne() {
        int park=policy.getParks().get(0);
        for(String lane:Arrays.asList("CONFIG","RECEIPT")) {
            String operation="CONFIG".equals(lane)?"CONFIG_PROGRESS":"RECEIPT";
            List<Long> additions=phaseFixture(park,operation,"ADD","shared-"+lane,3);
            List<Long> deletion=phaseFixture(park,operation,"DELETE","shared-"+lane,1);
            String key=UUID.randomUUID().toString();jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET REQUEST_KEY=? WHERE INSTANCE_ID=? AND DEVICE_ID=?",key,policy.getInstanceId(),"shared-"+lane);
            Grant grant=ledger.reservePark(policy,lane,park,1,30);
            Set<Long> expected=new HashSet<>(additions);expected.addAll(deletion);
            Assert.assertEquals("已接受原请求不可被页大小拆散",expected,new HashSet<>(grant.getPhaseIds()));
            Assert.assertEquals(deletion.get(0),grant.getPhaseIds().get(0));Assert.assertEquals(1,grant.getHttpBudget());
        }
    }
    @Test public void expansionPagesUseFrozenBusinessPriorityAndResumeStageAcrossProcesses() {
        int park=policy.getParks().get(0);
        List<Long> expansion=new ArrayList<>();
        for(String desired:Arrays.asList("ADD","DELETE")) {
            fixture(park,"ADD","ADD","expand-"+desired,1);Long batch=batches.get(batches.size()-1);expansion.add(batch);
            jdbc.update("UPDATE SMT_AUTH_OPERATION_BATCH SET STATUS='PREPARING' WHERE ID=?",batch);
            jdbc.update("INSERT INTO SMT_AUTH_SELECTION_SOURCE(BATCH_ID,ORDINAL,OPERATION_KEY,PARK_ID,SUBJECT_ID,AUTH_ID,STABLE_KEY,SOURCE_ROW_ID,FINGERPRINT,DESIRED_ACTION,STATE) VALUES(?,1,?,?, 'subject','auth','stable','row','fingerprint',?,'FROZEN')",batch,UUID.randomUUID().toString(),park,desired);
        }
        Assert.assertEquals(Collections.singletonList(expansion.get(0)),ledger.expansionBatches(policy,"ADD",null,1));
        Assert.assertEquals("混合ADD批次冻结DELETE来源必须走撤权展开类",Collections.singletonList(expansion.get(1)),ledger.expansionBatches(policy,"DELETE",null,1));
        Assert.assertTrue(ledger.expansionBatches(policy,"DELETE",expansion.get(1),1).isEmpty());
        Grant grant=ledger.reserve(policy,"EXPAND",200,30);Assert.assertNotNull(grant);
        ledger.advanceExpansionStage(policy.getInstanceId(),expansion.get(0),1);
        AuthOperationSchedulerService restarted=proxy(new AuthOperationSchedulerService(mapper,operations));
        Assert.assertEquals(1,restarted.expansionStage(policy.getInstanceId(),expansion.get(0)));
        restarted.advanceExpansionStage(policy.getInstanceId(),expansion.get(0),2);
        restarted.advanceExpansionStage(policy.getInstanceId(),expansion.get(0),1);
        Assert.assertEquals("旧进程不能回退已持久的绑定完成阶段",2,ledger.expansionStage(policy.getInstanceId(),expansion.get(0)));
        jdbc.update("UPDATE SMT_AUTH_OPERATION_BATCH SET STATUS='VERIFYING' WHERE ID=?",expansion.get(1));
        Assert.assertTrue(ledger.expansionBatches(policy,"DELETE",null,100).isEmpty());
    }
    private List<Long> fixture(int park,String intent,String action,String device,int count) {
        Long batch=operations.submit(AuthOperationSubmitCommand.builder().parkId(park).idempotencyKey(UUID.randomUUID().toString()).action(intent).sourceType("STAFF").sourceId("test").selectionSnapshot("scheduler-fixture").payloadFingerprint("test").expectedCount(count).build()).getBatchId();batches.add(batch);
        List<Long> ids=new ArrayList<>();for(int n=0;n<count;n++) {
            long request=IdWorker.getId(),target=IdWorker.getId();ids.add(target);
            operations.appendTargets(AuthOperationAppendCommand.builder().batchId(batch).previousCursor((long)n).nextCursor(n+1L)
                .request(AuthOperationRequestCommand.builder().id(request).parkId(park).subjectType("STAFF").sourceType("STAFF").sourceRowId("row-"+n).sourceIdentityKey("source-"+n).identitySnapshot("fixture").generation(1L).build())
                .target(AuthOperationTargetCommand.builder().id(target).requestId(request).parkId(park).targetKey("target-"+n).subjectType("STAFF").subjectId("subject-"+n).resourceType("PERSON").deviceId(device).resourceId("resource-"+n).accessType("ISC").operationQueue("AUTH").action(action).operationVersion(1L).build()).build());
        }operations.finishExpansion(batch,count);return ids;
    }
    @SuppressWarnings("unchecked") private <T>T proxy(T raw) {ProxyFactory factory=new ProxyFactory(raw);factory.setProxyTargetClass(true);factory.addAdvice(new TransactionInterceptor(transactions,new AnnotationTransactionAttributeSource()));return (T)factory.getProxy();}
    @After public void cleanUp() {
        if(jdbc==null)return;
        for(Long batch:batches) {
            jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE BATCH_ID=?",batch);
            jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_PHASE WHERE TARGET_ID IN(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?)",batch);
            jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?)",batch);
            jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?)",batch);
            jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",batch);jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE BATCH_ID=?",batch);
            jdbc.update("DELETE FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID=?",batch);jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",batch);
        }
        for(String instance:routeInstances) {
            jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_JOB WHERE INSTANCE_ID=?",instance);
            if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_SCHEDULER_ROUTE'",Integer.class)>0)jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_ROUTE WHERE INSTANCE_ID=?",instance);
            jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_QUOTA WHERE INSTANCE_ID=?",instance);
            jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID=?",instance);
        }
    }
}
