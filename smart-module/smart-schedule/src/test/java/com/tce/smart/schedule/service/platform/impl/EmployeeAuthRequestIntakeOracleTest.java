package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authoperation.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.*;
import com.tce.smart.platform.core.service.impl.*;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.time.LocalDateTime;
import java.util.*;

/** 真实本机 Oracle 上的组合原子性、来源共享、可信收敛与补偿验收。 */
public class EmployeeAuthRequestIntakeOracleTest {
    private static HikariDataSource pool;
    private JdbcTemplate jdbc;
    private AuthOperationWorkflowService service;
    private AuthOperationService operations;
    private AuthOperationVersionService versions;
    private int park;
    private long employeeId;
    private EmployeeAuthOperationService employee;
    private EmployeeAuthOperationMapper selection;
    private com.tce.smart.platform.service.impl.EmployeeAuthIntakeService intake;
    private AuthRequestIntakeMapper headerMapper;
    private Set<Integer> scope;
    private SmtStaffDeviceAuthMapper generatedStaff;
    private SmtDeviceTaskMapper generatedTask;
    private SmtTaskDownRecordMapper generatedRecord;
    private com.tce.smart.platform.service.impl.SmtStaffDeviceAuthServiceImpl entry;
    private com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter adapter;
    private com.tce.smart.platform.core.config.AuthOperationProperties enabled;

    private org.springframework.transaction.support.TransactionTemplate outer;

    @Before public void setup() throws Exception {
        String url=System.getenv("SMART_AUTH_ORACLE_URL");Assume.assumeTrue(url!=null);
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",url);
        Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        if(pool==null) { pool=new HikariDataSource();pool.setJdbcUrl(url);pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
            pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("auth-workflow-test"); }
        jdbc=new JdbcTemplate(pool);park=100000+(int)(Math.abs(UUID.randomUUID().getMostSignificantBits()%600000000));
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_REQUEST_INTAKE'",Integer.class));
        scope=new HashSet<>(Arrays.asList(park,park+1));
        employeeId=9000000000L+park;
        jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) VALUES(?,?,1,?,?,?)",employeeId,"intake-test-"+park,"image-ref-"+park,"badge-"+park,"合成员工");
        for(int p:scope) {
         jdbc.update("INSERT INTO SMT_PARK_BU(ID,PARK_ID,COMP_ID) VALUES(?,?,?)",p,p,"intake-test-"+park);
         jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,1)",p,p);
         jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,0)","intake-device-"+p,p);
         jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",p,p,"intake-device-"+p,p);
         jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",p,employeeId,p);
        }
        MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);
        Class<?>[] mappers={SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,
            SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,SmtAuthSubjectCoordMapper.class,
            SmtAuthSourceCoordMapper.class,SmtAuthResourceCoordMapper.class,SmtAuthSourceResourceMapper.class,
            SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class,EmployeeAuthOperationMapper.class,AuthRequestIntakeMapper.class};
        List<Resource> xml=new ArrayList<>();for(Class<?> type:mappers) { cfg.addMapper(type);xml.add(new ClassPathResource("mapper/"+type.getSimpleName()+".xml")); }
        cfg.addMapper(SmtStaffDeviceAuthMapper.class);cfg.addMapper(SmtDeviceTaskMapper.class);cfg.addMapper(SmtTaskDownRecordMapper.class);
        MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(cfg);
        factory.setMapperLocations(xml.toArray(new Resource[0]));SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());
        DataSourceTransactionManager tm=new DataSourceTransactionManager(pool);outer=new org.springframework.transaction.support.TransactionTemplate(tm);
        operations=proxy(new AuthOperationService(session.getMapper(SmtAuthOperationBatchMapper.class),session.getMapper(SmtAuthDeleteRequestMapper.class),
            session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(SmtAuthResultEventMapper.class)),tm);
        versions=proxy(new AuthOperationVersionService(session.getMapper(SmtAuthSubjectCoordMapper.class),session.getMapper(SmtAuthSourceCoordMapper.class),
            session.getMapper(SmtAuthResourceCoordMapper.class),session.getMapper(SmtAuthSourceResourceMapper.class),session.getMapper(SmtAuthIdentityAliasMapper.class)),tm);
        service=proxy(new AuthOperationWorkflowService(operations,versions,session.getMapper(SmtAuthOperationBatchMapper.class),
            session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(AuthOperationWorkflowMapper.class)),tm);
        selection=session.getMapper(EmployeeAuthOperationMapper.class);
        generatedStaff=session.getMapper(SmtStaffDeviceAuthMapper.class);generatedTask=session.getMapper(SmtDeviceTaskMapper.class);generatedRecord=session.getMapper(SmtTaskDownRecordMapper.class);
        employee=proxy(new EmployeeAuthOperationService(selection,service),tm);
        enabled=new com.tce.smart.platform.core.config.AuthOperationProperties();enabled.setEnabled(true);enabled.setEnabledParks(scope);
        adapter=proxy(new com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter(enabled,selection,employee) {
            @Override protected Set<Integer> allowedParks(){return scope;}
        },tm);
        headerMapper=session.getMapper(AuthRequestIntakeMapper.class);
        intake=new com.tce.smart.platform.service.impl.EmployeeAuthIntakeService(headerMapper,tm);
    }
    @SuppressWarnings("unchecked") private <T> T proxy(T raw,DataSourceTransactionManager tm) {
        ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();
    }
    @AfterClass public static void close() { if(pool!=null)pool.close(); }
    @After public void cleanup() {
      if(jdbc==null || scope==null)return;
      jdbc.update("DELETE FROM SMT_AUTH_REQUEST_INTAKE WHERE ACTOR_ID=?",park);
      for(int p:scope) {
       jdbc.update("DELETE FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",p);
       jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",p);
       jdbc.update("DELETE FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?)",p);
       jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",p);
       jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE AUTH_ID=? AND STAFF_ID=?",p,employeeId);
       jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE PARK_ID=?",p);
       jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE PARK_ID=?",p);
       jdbc.update("DELETE FROM SMT_DEVICE WHERE PARK_ID=?",p);
       jdbc.update("DELETE FROM SMT_PARK_BU WHERE PARK_ID=?",p);
      }
      jdbc.update("DELETE FROM SMT_STAFF WHERE ID=?",employeeId);
    }
    private com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand command(String key) {
      return com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand.builder().requestKey(key).requestKind("REMOVE_ROWS").authId(park).authorityType(1).rowId(park).build();
    }
    @Test public void realTwoParkSelectionBatchAndHeaderRollbackTogether() {
      java.util.concurrent.atomic.AtomicReference<String> operation=new java.util.concurrent.atomic.AtomicReference<>();
      try {
       intake.submit(command("rollback-intake-key"),park,scope,key->{
        operation.set(key);com.tce.smart.platform.dto.authoperation.AuthOperationIntakeAcceptance result=adapter.removeRowsOperation(Collections.singletonList(park),park,key);
        Assert.assertEquals(2,result.getBatchParks().size());
        Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE OPERATION_KEY=?",Integer.class,key));
        throw new IllegalStateException("callback-after-both-parks");
       });Assert.fail("回调失败必须回滚全部本库数据");
      } catch(IllegalStateException expected){Assert.assertEquals("callback-after-both-parks",expected.getMessage());}
      Assert.assertNull(headerMapper.find(park,"rollback-intake-key"));
      Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE SOURCE_ID=?",Integer.class,operation.get()));
      Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE OPERATION_KEY=?",Integer.class,operation.get()));
      Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID IN (?,?)",Integer.class,park,park+1));
      Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId));
    }
    @Test public void originalResponseRestoredAfterSourcesAndAuthorityRemovedAndRolloutClosed() {
      com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand request=command("lost-response-intake");
      com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt accepted=intake.submit(request,park,scope,key->adapter.removeRowsOperation(Collections.singletonList(park),park,key));
      Assert.assertEquals(2,accepted.getBatchParks().size());
      jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",employeeId);
      jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE AUTHORITY_ID=?",park);
      jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE ID=?",park);enabled.setEnabled(false);
      com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt replay=intake.submit(request,park,scope,key->{throw new AssertionError("重放不得再次读取业务来源或冻结");});
      Assert.assertTrue(replay.isReplayed());Assert.assertEquals(accepted.getOperationKey(),replay.getOperationKey());Assert.assertEquals(accepted.getBatchParks(),replay.getBatchParks());
      try{intake.submit(request,park,Collections.singleton(park),key->{throw new AssertionError();});Assert.fail("只授权一园区不能返回部分child");}catch(SecurityException expected){}
      Long child=Long.valueOf(accepted.getBatchParks().keySet().iterator().next());
      jdbc.update("UPDATE SMT_AUTH_OPERATION_BATCH SET EXPECTED_COUNT=EXPECTED_COUNT+1 WHERE ID=?",child);
      try{intake.submit(request,park,scope,key->{throw new AssertionError();});Assert.fail("child稳定摘要变化不得静默重放");}
      catch(com.tce.smart.platform.service.impl.EmployeeAuthIntakeService.IntakeException expected){Assert.assertEquals("INTAKE_INCOMPLETE",expected.getCode());}
    }
    @Test public void concurrentSameKeyCommitsOneEmptyIntakeAndReplaysAfterMemberAdded() throws Exception {
      jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",employeeId);
      com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand request=com.tce.smart.platform.dto.authoperation.AuthOperationIntakeCommand.builder()
        .requestKey("concurrent-empty-intake").requestKind("CLEAR_AUTHORITY").authId(park).authorityType(1).build();
      java.util.concurrent.atomic.AtomicInteger calls=new java.util.concurrent.atomic.AtomicInteger();
      java.util.concurrent.CountDownLatch start=new java.util.concurrent.CountDownLatch(1);
      java.util.concurrent.ExecutorService threads=java.util.concurrent.Executors.newFixedThreadPool(2);
      java.util.concurrent.Callable<com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt> task=()->{
        start.await();return intake.submit(request,park,scope,key->{calls.incrementAndGet();try{Thread.sleep(300);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
         return adapter.removeAuthorityOperation(park,key);});
      };
      try {
       java.util.concurrent.Future<com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt> a=threads.submit(task),b=threads.submit(task);start.countDown();
       com.tce.smart.platform.dto.authoperation.AuthOperationIntakeReceipt first=a.get(15,java.util.concurrent.TimeUnit.SECONDS),second=b.get(15,java.util.concurrent.TimeUnit.SECONDS);
       Assert.assertEquals(1,calls.get());Assert.assertNotEquals(first.isReplayed(),second.isReplayed());Assert.assertEquals("NO_CHANGE",first.getMode());
      } finally {threads.shutdownNow();Assert.assertTrue(threads.awaitTermination(15,java.util.concurrent.TimeUnit.SECONDS));}
      jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,AUTH_TYPE) VALUES(?,?,?,SYSTIMESTAMP,2)",park,employeeId,park);
      Assert.assertEquals("NO_CHANGE",intake.submit(request,park,scope,key->{throw new AssertionError("同键清空不得重新选择新增成员");}).getMode());
      Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId));
    }
}
