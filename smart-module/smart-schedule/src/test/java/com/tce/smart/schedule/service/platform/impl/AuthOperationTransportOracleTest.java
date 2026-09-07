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
public class AuthOperationTransportOracleTest {
    private static HikariDataSource pool;
    private JdbcTemplate jdbc;
    private AuthOperationTransportService transport;
    private AuthOperationTransportMapper phases;
    private AuthOperationPersonOwnerMapper personOwners;
    private FailingRecords down;

    private AuthOperationWorkflowService service;
    private AuthOperationService operations;
    private AuthOperationVersionService versions;
    private int park;
    private int otherPark;
    private String iscInstance,directInstance,otherInstance;
    private long employeeId;
    private EmployeeAuthOperationService employee;
    private EmployeeAuthOperationMapper selection;
    private SmtStaffDeviceAuthMapper generatedStaff;
    private SmtDeviceTaskMapper generatedTask;
    private SmtTaskDownRecordMapper generatedRecord;
    private com.tce.smart.platform.service.impl.SmtStaffDeviceAuthServiceImpl entry;

    private org.springframework.transaction.support.TransactionTemplate outer;

    @Before public void setup() throws Exception {
        String url=System.getenv("SMART_AUTH_ORACLE_URL");Assume.assumeTrue(url!=null);
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",url);
        Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        if(pool==null) { pool=new HikariDataSource();pool.setJdbcUrl(url);pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
            pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("auth-workflow-test"); }
        jdbc=new JdbcTemplate(pool);park=100000+(int)(Math.abs(UUID.randomUUID().getMostSignificantBits()%600000000));
        otherPark=park+1;iscInstance="transport-isc-"+park;directInstance="transport-direct-"+park;otherInstance="transport-other-"+park;
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_SCHEDULER_ROUTE'",Integer.class));
        for(String instance:Arrays.asList(iscInstance,directInstance,otherInstance))jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_STATE(INSTANCE_ID) VALUES(?)",instance);
        jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES(?,'ISC',?)",park,iscInstance);
        jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES(?,'DIRECT',?)",park,directInstance);
        jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES(?,'ISC',?)",otherPark,iscInstance);
        ensureSelectionSchema();
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE'",Integer.class)==0)
            new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(new ClassPathResource("authoperation/transport-schema.sql")).execute(pool);

        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_IDENTITY'",Integer.class)==0)jdbc.execute("CREATE TABLE SMT_AUTH_TRANSPORT_IDENTITY(INSTANCE_ID VARCHAR2(128) NOT NULL,PERSON_ID VARCHAR2(128) NOT NULL,SUBJECT_TYPE VARCHAR2(32) NOT NULL,SUBJECT_ID VARCHAR2(128) NOT NULL,PARK_ID NUMBER(10) NOT NULL,PHASE_ID NUMBER(19) NOT NULL,CREATE_TIME TIMESTAMP,CONSTRAINT PK_AUTH_TRANSPORT_IDENTITY PRIMARY KEY(INSTANCE_ID,PERSON_ID))");
        if("N".equals(jdbc.queryForObject("SELECT NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_REVIEW' AND COLUMN_NAME='PARK_ID'",String.class)))jdbc.execute("ALTER TABLE SMT_AUTH_TRANSPORT_REVIEW MODIFY PARK_ID NULL");
        for(String column:Arrays.asList("PERSON_OPERATION_KEY","PERSON_IDENTITY_HASH","PERSON_PROOF_PHASE_ID"))if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE' AND COLUMN_NAME=?",Integer.class,column)==0)jdbc.execute("ALTER TABLE SMT_AUTH_TRANSPORT_PHASE ADD "+column+(column.equals("PERSON_PROOF_PHASE_ID")?" NUMBER(19)":column.equals("PERSON_IDENTITY_HASH")?" VARCHAR2(64)":" VARCHAR2(128)"));
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_PERSON_OWNER'",Integer.class)==0)new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(new ClassPathResource("authoperation/person-owner-schema.sql")).execute(pool);
        employeeId=9000000000L+park;
        jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) VALUES(?,?,1,?,?,?)",employeeId,"employee-test-"+park,"image-ref-"+park,"badge-"+park,"合成员工");
        jdbc.update("INSERT INTO SMT_PARK_BU(ID,PARK_ID,COMP_ID) VALUES(?,?,?)",park,park,"employee-test-"+park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",park,park);
        jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,0)","employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park,park,"employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",park,employeeId,park);
        MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);
        Class<?>[] mappers={SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,
            SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,SmtAuthSubjectCoordMapper.class,
            SmtAuthSourceCoordMapper.class,SmtAuthResourceCoordMapper.class,SmtAuthSourceResourceMapper.class,
            SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class,EmployeeAuthOperationMapper.class,AuthOperationTransportMapper.class,AuthOperationPersonOwnerMapper.class};
        List<Resource> xml=new ArrayList<>();for(Class<?> type:mappers) { cfg.addMapper(type);xml.add(new ClassPathResource("mapper/"+type.getSimpleName()+".xml")); }
        cfg.addMapper(SmtDeviceMapper.class);cfg.addMapper(SmtIscDeviceTaskMapper.class);cfg.addMapper(SmtIscDownRecordMapper.class);cfg.addMapper(SmtStaffDeviceAuthMapper.class);cfg.addMapper(SmtDeviceTaskMapper.class);cfg.addMapper(SmtTaskDownRecordMapper.class);
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
        com.tce.smart.platform.core.config.AuthOperationProperties enabled=new com.tce.smart.platform.core.config.AuthOperationProperties();enabled.setEnabled(true);enabled.setEnabledParks(Collections.singleton(park));
        com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter adapter=proxy(new com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter(enabled,selection,employee) {
            @Override protected Set<Integer> allowedParks(){return Collections.singleton(park);}
        },tm);
        entry=new com.tce.smart.platform.service.impl.SmtStaffDeviceAuthServiceImpl(
          org.mockito.Mockito.mock(SmtStaffDeviceAuthMapper.class),org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtDeviceTaskService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtIscDeviceTaskService.class),org.mockito.Mockito.mock(com.tce.smart.platform.service.SmtParkBuService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.service.SmtDeviceAuthorityRelationService.class),org.mockito.Mockito.mock(com.tce.smart.platform.service.SmtStaffService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtDeviceTaskDetailService.class),org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtTaskDownRecordService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtIscDownRecordService.class),org.mockito.Mockito.mock(com.tce.smart.dispatcher.api.feign.RemoteDispatcherService.class));
        org.springframework.test.util.ReflectionTestUtils.setField(entry,"employeeAuthOperationAdapter",adapter);
        phases=session.getMapper(AuthOperationTransportMapper.class);personOwners=session.getMapper(AuthOperationPersonOwnerMapper.class);
        SmtDeviceMapper devices=session.getMapper(SmtDeviceMapper.class);
        down=new FailingRecords(devices);org.springframework.test.util.ReflectionTestUtils.setField(down,"baseMapper",generatedRecord);
        SmtDeviceTaskServiceImpl dt=new SmtDeviceTaskServiceImpl(down,null,null,devices,null);org.springframework.test.util.ReflectionTestUtils.setField(dt,"baseMapper",generatedTask);
        SmtIscDownRecordServiceImpl ir=new SmtIscDownRecordServiceImpl(devices,org.mockito.Mockito.mock(StaffDeviceAuthSyncService.class));org.springframework.test.util.ReflectionTestUtils.setField(ir,"baseMapper",session.getMapper(SmtIscDownRecordMapper.class));
        SmtIscDeviceTaskServiceImpl it=new SmtIscDeviceTaskServiceImpl();org.springframework.test.util.ReflectionTestUtils.setField(it,"baseMapper",session.getMapper(SmtIscDeviceTaskMapper.class));
        DirectTaskCompletionService dc=proxy(new DirectTaskCompletionService(dt,down),tm);IscTaskCompletionService ic=proxy(new IscTaskCompletionService(it,ir),tm);
        AuthOperationTransportService rawTransport=new AuthOperationTransportService(phases,service,session.getMapper(AuthOperationWorkflowMapper.class),versions,session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),devices,generatedTask,session.getMapper(SmtIscDeviceTaskMapper.class),employee,enabled,dc,ic);org.springframework.test.util.ReflectionTestUtils.setField(rawTransport,"personOwners",personOwners);transport=proxy(rawTransport,tm);


    }
    @SuppressWarnings("unchecked") private <T> T proxy(T raw,DataSourceTransactionManager tm) {
        ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();
    }
    @AfterClass public static void close() { if(pool!=null)pool.close(); }
    @After public void cleanup() {
        if(jdbc==null)return;
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_ROUTE WHERE INSTANCE_ID IN(?,?,?)",iscInstance,directInstance,otherInstance);
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID IN(?,?,?)",iscInstance,directInstance,otherInstance);
        jdbc.update("DELETE FROM SMT_AUTH_IDENTITY_ALIAS WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_ISC_DOWN_RECORD WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_SUBJECT_COORD WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?)",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",otherPark);
        jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_IDENTITY WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_REVIEW WHERE PARK_ID=? OR (PARK_ID IS NULL AND DEVICE_ID=?)",park,"employee-device-"+park);
        jdbc.update("DELETE FROM SMT_AUTH_PERSON_OWNER WHERE INSTANCE_ID IN(?,?,?)",iscInstance,directInstance,otherInstance);
        jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_ISC_DOWN_RECORD WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_ISC_DEVICE_TASK WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_TASK_DOWN_RECORD WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_DEVICE_TASK WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (?,?)",employeeId,employeeId+1);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_DEVICE WHERE PARK_ID=? OR ID=?",park,"employee-device-"+park);
        jdbc.update("DELETE FROM SMT_PARK_BU WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_STAFF WHERE ID IN (?,?)",employeeId,employeeId+1);
        jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_IDENTITY_ALIAS WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SUBJECT_COORD WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",park);
    }

    private void ensureSelectionSchema() throws Exception {
        int tables=jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('SMT_AUTH_SELECTION_SOURCE','SMT_AUTH_SELECTION_RESOURCE')",Integer.class);
        Assert.assertTrue(tables==0 || tables==2);
        if(tables==0)try(java.sql.Connection c=pool.getConnection()){org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(c,new ClassPathResource("authoperation/selection-schema.sql"));}
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SELECTION_SOURCE' AND COLUMN_NAME='PERSON_SNAPSHOT'",Integer.class)==0)jdbc.execute("ALTER TABLE SMT_AUTH_SELECTION_SOURCE ADD PERSON_SNAPSHOT VARCHAR2(4000 CHAR)");
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME='IX_AUTH_SEL_AUTH'",Integer.class)==0)jdbc.execute("CREATE INDEX IX_AUTH_SEL_AUTH ON SMT_AUTH_SELECTION_SOURCE(PARK_ID,AUTH_ID,STATE)");
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_PARK_BU'",Integer.class)==0)
            jdbc.execute("CREATE TABLE SMT_PARK_BU(ID NUMBER(10) PRIMARY KEY,PARK_ID NUMBER(10),COMP_ID VARCHAR2(512 CHAR),CREATE_TIME TIMESTAMP)");
        for(String table:Arrays.asList("SMT_STAFF_DEVICE_AUTH","SMT_DEVICE_TASK","SMT_TASK_DOWN_RECORD")) {
            String seq=table.equals("SMT_STAFF_DEVICE_AUTH")?"AUTH_TEST_STAFF_AUTO":table.equals("SMT_DEVICE_TASK")?"AUTH_TEST_TASK_AUTO":"AUTH_TEST_DOWN_AUTO";
            int identities=jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_IDENTITY_COLS WHERE TABLE_NAME=? AND COLUMN_NAME='ID'",Integer.class,table);
            if(identities>0)continue;
            String existing=jdbc.queryForObject("SELECT DATA_DEFAULT FROM USER_TAB_COLUMNS WHERE TABLE_NAME=? AND COLUMN_NAME='ID'",String.class,table);
            if(existing!=null && !existing.trim().isEmpty()){Assert.assertTrue(existing.toUpperCase().contains(seq));continue;}
            Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE ID>=1500000000",Integer.class));
            if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME=?",Integer.class,seq)==0)jdbc.execute("CREATE SEQUENCE "+seq+" START WITH 1500000000 INCREMENT BY 1 MAXVALUE 2147483647 NOCYCLE");
            jdbc.execute("ALTER TABLE "+table+" MODIFY ID DEFAULT "+seq+".NEXTVAL");
        }
    }

    @Test public void deleteWithoutRecordCreatesRealAutoTaskAndConvergesExactSource() {
        SmtAuthTransportPhase p=prepared("DIRECT");
        Assert.assertTrue(Integer.parseInt(p.getTaskId())>=1500000000);
        Assert.assertEquals(p.getTaskId(),jdbc.queryForObject("SELECT TASK_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,p.getAttemptId()));
        transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);
        Assert.assertEquals(p.getSerialNo(),jdbc.queryForObject("SELECT EXTERNAL_COMMAND_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,p.getAttemptId()));
        Received r=transport.receipt(park,directInstance,p.getId(),null,p.getDeviceId(),p.getSerialNo(),"direct-ok",true,"合成设备成功");
        Assert.assertTrue(r.isSourceConverged());Assert.assertTrue(generatedTask.selectById(Integer.valueOf(p.getTaskId())).isSuccess());Assert.assertEquals(0,sourceRows());Assert.assertEquals(Integer.valueOf(1),generatedTask.selectById(Integer.valueOf(p.getTaskId())).getStatus());
        Assert.assertTrue(transport.receipt(park,directInstance,p.getId(),null,p.getDeviceId(),p.getSerialNo(),"direct-ok",true,"合成设备成功").getReceipt().isDuplicate());
    }
    @Test public void recordFailureRollsBackEventTaskAndSourceThenSameEventReplays() {
        SmtAuthTransportPhase p=prepared("DIRECT");
        jdbc.update("INSERT INTO SMT_TASK_DOWN_RECORD (TASK_ID,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE,PARK_ID) VALUES (?,?,?,1,1,?)",Integer.valueOf(p.getTaskId()),p.getDeviceId(),p.getCardNo(),park);
        transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);down.fail=true;
        try{transport.receipt(park,directInstance,p.getId(),null,p.getDeviceId(),p.getSerialNo(),"rollback",true,"合成设备成功");Assert.fail();}catch(IllegalStateException expected){}
        Assert.assertEquals(1,sourceRows());Assert.assertEquals(Integer.valueOf(3),generatedTask.selectById(Integer.valueOf(p.getTaskId())).getStatus());Assert.assertEquals(0,eventCount());
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_TASK_DOWN_RECORD WHERE CARD_NO=?",Integer.class,p.getCardNo()));
        down.fail=false;Assert.assertTrue(transport.receipt(park,directInstance,p.getId(),null,p.getDeviceId(),p.getSerialNo(),"rollback",true,"合成设备成功").isSourceConverged());Assert.assertEquals(0,sourceRows());
    }
    @Test public void iscTwoActualIdsRemainSeparateAndExpiredVerifyingOwnerCanFinishStage() {
        SmtAuthTransportPhase config=prepared("ISC");SmtAuthTransportPhase download=download(config);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET LEASE_UNTIL=SYS_EXTRACT_UTC(SYSTIMESTAMP)-INTERVAL '1' MINUTE,STATUS='VERIFYING' WHERE ID=?",config.getAttemptId());
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_UNTIL=SYS_EXTRACT_UTC(SYSTIMESTAMP)-INTERVAL '1' MINUTE,STATE='VERIFYING' WHERE ID=?",config.getTargetId());
        transport.begin(park,iscInstance,Collections.singletonList(download.getId()),Collections.singletonMap(download.getId(),"person-real"));
        transport.accepted(park,iscInstance,Collections.singletonList(download.getId()),"download-real");
        Assert.assertEquals("config-real",phases.byId(config.getId()).getExternalId());Assert.assertEquals("download-real",phases.byId(download.getId()).getExternalId());
        Assert.assertEquals("download-real",jdbc.queryForObject("SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,config.getAttemptId()));
        Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,config.getAttemptId()));
        Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,config.getTargetId()));
        Assert.assertEquals("download-real",jdbc.queryForObject("SELECT ISC_TASK_ID FROM SMT_ISC_DEVICE_TASK WHERE ID=?",String.class,Long.valueOf(config.getTaskId())));
        Assert.assertTrue(transport.receipt(park,iscInstance,download.getId(),"person-real",config.getDeviceId(),"download-real","isc-ok",true,"合成设备下载成功").isSourceConverged());
    }
    @Test public void unknownDownloadAndConcurrentStageClaimsNeverSendTwice() throws Exception {
        SmtAuthTransportPhase c=prepared("ISC"),d=download(c);java.util.concurrent.ExecutorService workers=java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch latch=new java.util.concurrent.CountDownLatch(1);java.util.concurrent.atomic.AtomicInteger sends=new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.Callable<Boolean> send=()->{latch.await();try{transport.begin(park,iscInstance,Collections.singletonList(d.getId()),Collections.singletonMap(d.getId(),"person-real"));sends.incrementAndGet();return true;}catch(IllegalArgumentException expected){return false;}};
        try{java.util.concurrent.Future<Boolean>a=workers.submit(send),b=workers.submit(send);latch.countDown();Assert.assertTrue(a.get(20,java.util.concurrent.TimeUnit.SECONDS)^b.get(20,java.util.concurrent.TimeUnit.SECONDS));Assert.assertEquals(1,sends.get());}finally{workers.shutdownNow();}
        transport.unknown(park,iscInstance,Collections.singletonList(d.getId()),"SYNTHETIC_CRASH");
        try{transport.begin(park,iscInstance,Collections.singletonList(d.getId()),Collections.singletonMap(d.getId(),"person-real"));Assert.fail();}catch(IllegalArgumentException expected){}
        Assert.assertEquals("UNKNOWN",phases.byId(d.getId()).getState());
    }
    @Test public void changedOwnerCannotMutateCurrentAttemptOnLateAssociation() {
        SmtAuthTransportPhase c=prepared("ISC"),d=download(c);transport.begin(park,iscInstance,Collections.singletonList(d.getId()),Collections.singletonMap(d.getId(),"person-real"));
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_TOKEN='another-owner' WHERE ID=?",c.getTargetId());
        transport.accepted(park,iscInstance,Collections.singletonList(d.getId()),"late-download");
        Assert.assertEquals("late-download",phases.byId(d.getId()).getExternalId());Assert.assertNull(jdbc.queryForObject("SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
    }
    @Test public void acceptedHistoricalDownloadAppendsOnlyOldIdAndSchedulesCompensation() {
        SmtAuthTransportPhase c=prepared("ISC"),d=download(c);
        transport.begin(park,iscInstance,Collections.singletonList(d.getId()),Collections.singletonMap(d.getId(),"person-real"));
        ResourceKey key=versions.currentDesired(c.getResourceId()).getResource();
        long batch=service.accept(Selection.builder().parkId(park).idempotencyKey("replacement").action("ADD").sourceType("1")
            .snapshot("synthetic-replacement").expectedCount(1).sourceCount(1).build()).getBatchId();
        Window window=Window.builder().from(LocalDateTime.of(2026,10,1,0,0)).to(LocalDateTime.of(2026,11,1,0,0)).build();
        SourceIntent next=SourceIntent.builder().parkId(park).batchId(batch).sourceKind("STAFF_AUTH")
            .stableKey(AuthWorkflow.staffStableKey(String.valueOf(employeeId),String.valueOf(park+1))).subjectType("STAFF").subjectId(String.valueOf(employeeId))
            .sourceRowId("replacement-row").sourceFingerprint("replacement-fingerprint").intentKey("replacement-intent")
            .action("ADD").payloadSnapshot("synthetic-replacement").window(window).build();
        service.stage(Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(next).staffAuthId(String.valueOf(park+1))
            .finalSourcePage(true).resource(ResourceInput.builder().resource(key).participation("INCLUDE").window(window).build()).build());
        Binding current=service.bindLane(batch,c.getResourceId(),1,2).get(0);service.finish(batch);
        AuthOperationClaimedTarget claim=service.claim(AuthOperationClaimCommand.builder().parkId(park).accessType("ISC").operationQueue("AUTH").maxCount(1)
            .leaseSeconds(300L).targetIds(Collections.singletonList(current.getTargetId())).build()).get(0);
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_TOKEN='another-owner' WHERE ID=?",c.getTargetId());
        transport.accepted(park,iscInstance,Collections.singletonList(d.getId()),"late-download");
        Map<String,Object> currentTarget=jdbc.queryForMap("SELECT STATE,LEASE_TOKEN,LEGACY_TASK_ID,OPERATION_VERSION FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",current.getTargetId());
        Map<String,Object> currentAttempt=jdbc.queryForMap("SELECT STATUS,LEASE_TOKEN,LEASE_UNTIL,EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",claim.getAttemptId());
        Map<String,Object> oldAttempt=jdbc.queryForMap("SELECT STATUS,LEASE_TOKEN,LEASE_UNTIL FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",c.getAttemptId());
        Assert.assertNull(jdbc.queryForObject("SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
        SmtAuthTransportPhase accepted=phases.byId(d.getId());
        accepted.setInstanceId("wrong-instance");Assert.assertEquals(0,phases.historicalExternal(accepted,"late-download"));
        accepted.setInstanceId(iscInstance);Assert.assertEquals(1,phases.historicalExternal(accepted,"late-download"));
        Assert.assertEquals(oldAttempt,jdbc.queryForMap("SELECT STATUS,LEASE_TOKEN,LEASE_UNTIL FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",c.getAttemptId()));
        jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET EXTERNAL_BATCH_ID=NULL WHERE ID=?",c.getAttemptId());
        Received received=transport.receipt(park,iscInstance,d.getId(),"person-real",d.getDeviceId(),"late-download","late-success",true,"合成旧下载成功");
        Assert.assertTrue(received.getEvidence().isCompensationRequired());Assert.assertFalse(received.getEvidence().isMayApply());
        Assert.assertEquals("late-download",jdbc.queryForObject("SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
        Assert.assertEquals(currentTarget,jdbc.queryForMap("SELECT STATE,LEASE_TOKEN,LEGACY_TASK_ID,OPERATION_VERSION FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",current.getTargetId()));
        Assert.assertEquals(currentAttempt,jdbc.queryForMap("SELECT STATUS,LEASE_TOKEN,LEASE_UNTIL,EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",claim.getAttemptId()));
        Assert.assertEquals(1,sourceRows());Assert.assertEquals(1,eventCount());
    }
    @Test public void unknownPhaseAndConflictingAttemptIdCannotBecomeTrustedReceipt() {
        SmtAuthTransportPhase c=prepared("ISC"),d=download(c);
        transport.begin(park,iscInstance,Collections.singletonList(d.getId()),Collections.singletonMap(d.getId(),"person-real"));
        transport.unknown(park,iscInstance,Collections.singletonList(d.getId()),"SYNTHETIC_UNKNOWN");
        Assert.assertEquals(0,phases.historicalExternal(phases.byId(d.getId()),"unproven"));
        try{transport.receipt(park,iscInstance,d.getId(),"person-real",d.getDeviceId(),null,"unproven",true,"synthetic");Assert.fail();}catch(IllegalArgumentException expected){}
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_TOKEN='another-owner' WHERE ID=?",c.getTargetId());
        transport.accepted(park,iscInstance,Collections.singletonList(d.getId()),"download-real");
        jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET EXTERNAL_BATCH_ID='conflicting-id' WHERE ID=?",c.getAttemptId());
        Assert.assertEquals(0,phases.historicalExternal(phases.byId(d.getId()),"download-real"));
        try{transport.receipt(park,iscInstance,d.getId(),"person-real",d.getDeviceId(),"download-real","conflict",true,"synthetic");Assert.fail();}catch(IllegalArgumentException expected){}
        Assert.assertEquals("conflicting-id",jdbc.queryForObject("SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
        Assert.assertEquals(0,eventCount());Assert.assertEquals(1,sourceRows());
    }
    @Test public void wrongParkAndInstanceCannotReceiveSameExternalId() {
        SmtAuthTransportPhase c=prepared("ISC"),d=download(c);transport.begin(park,iscInstance,Collections.singletonList(d.getId()),Collections.singletonMap(d.getId(),"person-real"));transport.accepted(park,iscInstance,Collections.singletonList(d.getId()),"download-real");
        try{transport.receipt(park+1,iscInstance,d.getId(),"person-real",d.getDeviceId(),"download-real","wrong-park",true,"synthetic");Assert.fail();}catch(IllegalArgumentException expected){}
        try{transport.receipt(park,"other-instance",d.getId(),"person-real",d.getDeviceId(),"download-real","wrong-instance",true,"synthetic");Assert.fail();}catch(IllegalArgumentException expected){}
        Assert.assertEquals(0,eventCount());Assert.assertEquals(1,sourceRows());
    }
    @Test public void onePhysicalReceiptConvergesEveryFrozenSourceSharingTheTarget() {
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",park+1,park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park+1,park+1,"employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",employeeId,park+1);
        Integer second=jdbc.queryForObject("SELECT ID FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=? AND AUTH_ID=?",Integer.class,employeeId,park+1);
        SmtAuthTransportPhase p=prepared("DIRECT",Arrays.asList(park,second));
        transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);
        transport.receipt(park,directInstance,p.getId(),null,p.getDeviceId(),p.getSerialNo(),"shared-ok",true,"合成设备成功");
        Assert.assertEquals(0,sourceRows());Assert.assertEquals(1,eventCount());
    }
    @Test public void installedTablesAloneDoNotProtectUntouchedParkButPendingSelectionDoes() {
        com.tce.smart.platform.core.config.AuthOperationProperties off=new com.tce.smart.platform.core.config.AuthOperationProperties();
        AuthOperationTransportGuard guard=new AuthOperationTransportGuard(pool,off);
        Assert.assertFalse(guard.protectSource("employee-device-"+park,String.valueOf(employeeId),null));
        entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());
        Assert.assertTrue(guard.protectSource("employee-device-"+park,String.valueOf(employeeId),null));
        Assert.assertTrue(guard.protectSource("employee-device-"+park,String.valueOf(employeeId),null));
        Assert.assertEquals(1,guard.reviews(park,null,200).size());Assert.assertEquals(1,sourceRows());
    }
    @Test public void crashedIntentBecomesDurableVerificationAndCannotResend() {
        SmtAuthTransportPhase p=prepared("DIRECT");transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);
        jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET UPDATE_TIME=SYS_EXTRACT_UTC(SYSTIMESTAMP)-INTERVAL '3' MINUTE WHERE ID=?",p.getId());
        Assert.assertEquals(1,transport.expireIntents(park,directInstance,200));
        Assert.assertEquals("UNKNOWN",phases.byId(p.getId()).getState());Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,p.getTargetId()));
        try{transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);Assert.fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void blockedUnsubmittedFollowerWaitsThenReusesOwnerEvidenceWithoutNewTask() throws Exception {
        SmtAuthTransportPhase owner=prepared("DIRECT");ResourceKey key=versions.currentDesired(owner.getResourceId()).getResource();
        SmtStaffDeviceAuth row=generatedStaff.selectById(park);row.setId(null);row.setAuthId(park+1);Assert.assertEquals(1,generatedStaff.insert(row));
        Source frozen=Source.builder().parkId(park).subjectId(String.valueOf(employeeId)).authId(String.valueOf(park+1)).before(row).badge("badge-"+park).imageId("image-ref-"+park)
            .resource(ResourceInput.builder().resource(key).participation("EXCLUDE").build()).build();
        // 模拟已持久化的历史 sibling batch；当前受理入口禁止同主体并发受理，不能绕过该门禁来构造测试。
        long batch=service.accept(Selection.builder().parkId(park).idempotencyKey("historic-follower").action("DELETE").sourceType("1").snapshot("historic-frozen-follower").expectedCount(1).sourceCount(1).build()).getBatchId();
        java.lang.reflect.Method fingerprint=EmployeeAuthOperationService.class.getDeclaredMethod("fingerprint",Source.class);fingerprint.setAccessible(true);String fp=(String)fingerprint.invoke(null,frozen);
        String stable=AuthWorkflow.staffStableKey(String.valueOf(employeeId),String.valueOf(park+1));
        SmtAuthSelectionSource selected=new SmtAuthSelectionSource();org.springframework.beans.BeanUtils.copyProperties(employee.sourcesForTarget(owner.getTargetId()).get(0),selected);
        selected.setBatchId(batch);selected.setOrdinal(1L);selected.setOperationKey("historic-follower-"+park);selected.setAuthId(String.valueOf(park+1));selected.setStableKey(stable);selected.setOldId(row.getId());selected.setSourceRowId(String.valueOf(row.getId()));selected.setFingerprint(fp);selected.setSourceCoordId(null);selected.setSourceGeneration(null);selected.setState("PENDING");
        Assert.assertEquals(1,selection.insertSources(Collections.singletonList(selected)));
        SourceIntent intent=SourceIntent.builder().parkId(park).batchId(batch).sourceKind("STAFF_AUTH").stableKey(stable).subjectType("STAFF").subjectId(String.valueOf(employeeId))
            .sourceRowId(String.valueOf(row.getId())).sourceFingerprint(fp).intentKey("historic-follower-"+park).action("DELETE").payloadSnapshot("historic-frozen-follower").build();
        Expanded expanded=service.stage(Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(intent).staffAuthId(String.valueOf(park+1)).finalSourcePage(true).resource(ResourceInput.builder().resource(key).participation("EXCLUDE").build()).build());
        Assert.assertEquals(1,selection.bindSource(batch,1,expanded.getSource().getSourceId(),expanded.getSource().getGeneration()));
        Binding b=service.bindLane(batch,owner.getResourceId(),1,2).get(0);service.finish(batch);
        AuthOperationClaimCommand command=AuthOperationClaimCommand.builder().parkId(park).accessType("DIRECT").operationQueue("AUTH").maxCount(1).leaseSeconds(300L).targetIds(Collections.singletonList(b.getTargetId())).build();
        AuthOperationClaimedTarget first=service.claim(command).get(0);Assert.assertEquals("BLOCKED",transport.reuseBeforePrepare(park,first));transport.deferClaim(park,first);
        Assert.assertEquals("QUEUED",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,b.getTargetId()));
        Assert.assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE TARGET_ID=?",Integer.class,b.getTargetId()).intValue());
        transport.begin(park,directInstance,Collections.singletonList(owner.getId()),null);transport.receipt(park,directInstance,owner.getId(),null,owner.getDeviceId(),owner.getSerialNo(),"owner-success",true,"合成成功");
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET NEXT_ATTEMPT_AT=SYS_EXTRACT_UTC(SYSTIMESTAMP)-INTERVAL '1' SECOND WHERE ID=?",b.getTargetId());
        AuthOperationClaimedTarget retry=service.claim(command).get(0);Assert.assertEquals("REUSE_APPLIED",transport.reuseBeforePrepare(park,retry));
        Assert.assertEquals("CONVERGED",jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID=?",String.class,b.getTargetId()));
        Assert.assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_DEVICE_TASK WHERE CARD_NO=?",Integer.class,String.valueOf(employeeId)).intValue());
    }
    @Test public void preparedAfterLeaseExpiryCanSendOnlyOnceAndInvalidOwnerLeavesRecoveryPage(){
        SmtAuthTransportPhase p=prepared("DIRECT");jdbc.update("UPDATE SMT_AUTH_OPERATION_ATTEMPT SET LEASE_UNTIL=SYS_EXTRACT_UTC(SYSTIMESTAMP)-INTERVAL '1' MINUTE WHERE ID=?",p.getAttemptId());
        Assert.assertTrue(transport.resumeReady(park,directInstance,p.getId()));transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);
        Assert.assertFalse(transport.resumeReady(park,directInstance,p.getId()));
        try{transport.begin(park,directInstance,Collections.singletonList(p.getId()),null);Assert.fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void invalidPreparedOwnerIsPersistentlyIsolated(){
        SmtAuthTransportPhase p=prepared("DIRECT");jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_TOKEN='new-owner' WHERE ID=?",p.getTargetId());
        Assert.assertFalse(transport.resumeReady(park,directInstance,p.getId()));Assert.assertEquals("VERIFYING",phases.byId(p.getId()).getState());Assert.assertTrue(transport.prepared(park,directInstance,"DELETE",null,200).isEmpty());
    }
    @Test public void deletedDeviceOldRecordPathRetainsSourceAndUnknownParkReview(){
        entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=?","employee-device-"+park);
        AuthOperationTransportGuard guard=new AuthOperationTransportGuard(pool,new com.tce.smart.platform.core.config.AuthOperationProperties());
        StaffDeviceAuthSyncService sync=new StaffDeviceAuthSyncService(generatedStaff,null,generatedRecord,null,null);sync.setTransportGuard(guard);
        SmtTaskDownRecordServiceImpl records=new SmtTaskDownRecordServiceImpl(null,sync);org.springframework.test.util.ReflectionTestUtils.setField(records,"baseMapper",generatedRecord);
        SmtDeviceTask task=new SmtDeviceTask();task.setId(123);task.setAction(2);task.setDeviceCode("employee-device-"+park);task.setCardNo(String.valueOf(employeeId));task.setDeviceType(1);task.setServiceType(1);
        records.handleTaskDownRecord(task);records.handleTaskDownRecord(task);Assert.assertEquals(1,sourceRows());
        Assert.assertEquals(1,guard.unknownReviews(null,200).stream().filter(r->task.getDeviceCode().equals(r.get("DEVICE_ID"))).count());
    }
    @Test public void historicalPersonIsFrozenAndConflictingSubjectCannotClaimIt(){
        jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE,PERSON_ID) VALUES(?,?,?,?,1,1,?)",90000000000L+park,park,"employee-device-"+park,String.valueOf(employeeId),"person-real");
        SmtAuthTransportPhase p=prepared("ISC");Assert.assertEquals("person-real",p.getPersonId());
        try{transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-B"));Assert.fail();}catch(IllegalArgumentException expected){}
        Assert.assertEquals("PREPARED",phases.byId(p.getId()).getState());transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));
        SmtAuthTransportPhase other=new SmtAuthTransportPhase();org.springframework.beans.BeanUtils.copyProperties(p,other);other.setSubjectId(String.valueOf(employeeId+1));
        phases.claimPerson(other,"person-real");Assert.assertEquals(0,phases.ownsPerson(other,"person-real"));Assert.assertEquals(1,phases.ownsPerson(p,"person-real"));
    }
    @Test public void sharedInstanceOtherParkLegacyRecordBlocksBeforeNewIdentityClaim(){
        SmtAuthTransportPhase p=prepared("ISC");jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE,PERSON_ID) VALUES(?,?,?,?,1,1,?)",90000000000L+otherPark,otherPark,"other-device-"+park,String.valueOf(employeeId+1),"person-real");
        Assert.assertEquals(1,phases.conflictingPerson(p,"person-real"));
        try{transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));Assert.fail();}catch(IllegalArgumentException expected){}
        Assert.assertEquals(0,phases.ownsPerson(p,"person-real"));Assert.assertEquals("PREPARED",phases.byId(p.getId()).getState());
    }
    @Test public void sharedInstanceOtherParkLegacyAliasBlocksExactMember(){
        SmtAuthTransportPhase p=prepared("ISC");
        ResourceKey key=versions.currentDesired(p.getResourceId()).getResource().toBuilder().parkId(otherPark).subjectId(String.valueOf(employeeId+1)).resourceId(String.valueOf(employeeId+1)).deviceId("other-device-"+park).build();
        Long batch=service.accept(Selection.builder().parkId(otherPark).idempotencyKey("other-alias").action("DELETE").sourceType("1").snapshot("synthetic-alias").expectedCount(1).sourceCount(1).build()).getBatchId();
        SourceIntent source=SourceIntent.builder().parkId(otherPark).batchId(batch).sourceKind("STAFF_AUTH").stableKey(AuthWorkflow.staffStableKey(String.valueOf(employeeId+1),String.valueOf(otherPark))).subjectType("STAFF").subjectId(String.valueOf(employeeId+1)).sourceRowId("other-row").sourceFingerprint("other-fp").intentKey("other-intent").action("DELETE").payloadSnapshot("synthetic-alias").build();
        service.stage(Shard.builder().batchId(batch).previousCursor(0).nextCursor(1).source(source).staffAuthId(String.valueOf(otherPark)).finalSourcePage(true).resource(ResourceInput.builder().resource(key).participation("EXCLUDE").build()).build());
        versions.rememberAlias(AliasCommand.builder().resource(key).resourceGeneration(1).aliasKind("PERSON_ID").aliasValue("person-real").build());
        Assert.assertEquals(1,phases.conflictingPerson(p,"person-real"));
        try{transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));Assert.fail();}
        catch(AuthOperationTransportService.PhaseRejected expected){Assert.assertEquals(p.getId(),expected.getPhaseId());Assert.assertEquals("ISC_IDENTITY_CONFLICT",expected.getMessage());transport.rejectPrepared(park,iscInstance,expected.getPhaseId(),expected.getMessage());}
        Assert.assertEquals("VERIFYING",phases.byId(p.getId()).getState());Assert.assertEquals(0,phases.ownsPerson(p,"person-real"));
    }
    @Test public void missingCurrentInstanceRouteHoldsWithoutSending(){
        SmtAuthTransportPhase p=prepared("ISC");jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=? AND ACCESS_TYPE='ISC'",park);
        try{transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));Assert.fail();}
        catch(AuthOperationTransportService.PhaseRejected expected){Assert.assertEquals(p.getId(),expected.getPhaseId());Assert.assertEquals("ISC_INSTANCE_SCOPE_UNVERIFIED",expected.getMessage());transport.rejectPrepared(park,iscInstance,p.getId(),expected.getMessage());}
        Assert.assertEquals("VERIFYING",phases.byId(p.getId()).getState());Assert.assertNull(phases.byId(p.getId()).getRequestKey());Assert.assertEquals(0,phases.ownsPerson(p,"person-real"));
    }
    @Test public void loserRejectionCannotOverwriteAnotherWorkersIntent(){
        SmtAuthTransportPhase p=prepared("ISC");transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));
        String request=phases.byId(p.getId()).getRequestKey();
        try{transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));Assert.fail();}
        catch(AuthOperationTransportService.PhaseRejected expected){transport.rejectPrepared(park,iscInstance,expected.getPhaseId(),expected.getMessage());}
        Assert.assertEquals("INTENT",phases.byId(p.getId()).getState());Assert.assertEquals(request,phases.byId(p.getId()).getRequestKey());
    }
    @Test public void samePersonInDifferentInstanceIsNotAConflict(){
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_ROUTE WHERE PARK_ID=? AND ACCESS_TYPE='ISC'",otherPark);jdbc.update("INSERT INTO SMT_AUTH_SCHEDULER_ROUTE(PARK_ID,ACCESS_TYPE,INSTANCE_ID) VALUES(?,'ISC',?)",otherPark,otherInstance);
        SmtAuthTransportPhase p=prepared("ISC");jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD(ID,PARK_ID,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE,PERSON_ID) VALUES(?,?,?,?,1,1,?)",90000000000L+otherPark,otherPark,"other-device-"+park,String.valueOf(employeeId+1),"person-real");
        Assert.assertEquals(0,phases.conflictingPerson(p,"person-real"));transport.begin(park,iscInstance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));Assert.assertEquals(1,phases.ownsPerson(p,"person-real"));
    }
    @Test public void acceptedAssetProofIsReusableOnlyForExactFrozenImage(){
        SmtAuthTransportPhase config=prepared("ISC");jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET ACTION='ADD' WHERE ID=?",config.getId());
        SmtAuthTransportPhase asset=transport.prepareAsset(park,iscInstance,config.getId(),"ISC_PERSON","frozen-org");
        transport.begin(park,iscInstance,Collections.singletonList(asset.getId()),null);transport.accepted(park,iscInstance,Collections.singletonList(asset.getId()),"created-person");
        Assert.assertEquals("created-person",transport.acceptedAsset(park,iscInstance,config.getId(),"ISC_PERSON").getExternalId());Assert.assertEquals(asset.getId(),transport.prepareAsset(park,iscInstance,config.getId(),"ISC_PERSON","different-org").getId());
        jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET IMAGE_ID='other-image' WHERE ID=?",asset.getId());
        try{transport.acceptedAsset(park,iscInstance,config.getId(),"ISC_PERSON");Assert.fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void concurrentPersonClaimsCanNeverAssignOneExternalPersonToTwoSubjects() throws Exception {
        SmtAuthTransportPhase a=prepared("ISC"),b=new SmtAuthTransportPhase();org.springframework.beans.BeanUtils.copyProperties(a,b);b.setSubjectId(String.valueOf(employeeId+1));
        java.util.concurrent.ExecutorService workers=java.util.concurrent.Executors.newFixedThreadPool(2);java.util.concurrent.CountDownLatch start=new java.util.concurrent.CountDownLatch(1);
        try{List<java.util.concurrent.Future<?>> pending=new ArrayList<>();for(SmtAuthTransportPhase p:Arrays.asList(a,b))pending.add(workers.submit(()->{try{start.await();phases.claimPerson(p,"same-external-person");}catch(org.springframework.dao.DuplicateKeyException expected){}catch(InterruptedException e){throw new IllegalStateException(e);}}));start.countDown();for(java.util.concurrent.Future<?> f:pending)f.get(20,java.util.concurrent.TimeUnit.SECONDS);
            Assert.assertEquals(1,phases.ownsPerson(a,"same-external-person")+phases.ownsPerson(b,"same-external-person"));
        }finally{workers.shutdownNow();}
    }
    @Test public void frozenDirectAddAckWorksAfterDeviceDeletion(){assertFrozenAdd("DIRECT",true);}
    @Test public void frozenIscAddAckUsesOriginalParkAfterDeviceMoves(){assertFrozenAdd("ISC",false);}
    private void assertFrozenAdd(String access,boolean deleted){
        SmtStaffDeviceAuth after=generatedStaff.selectById(park);generatedStaff.deleteById(park);after.setId(null);
        jdbc.update("UPDATE SMT_DEVICE SET IS_SYNC=?,CHANNEL_NO=1 WHERE ID=?","ISC".equals(access)?1:0,"employee-device-"+park);
        ResourceKey key=ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(String.valueOf(employeeId)).deviceId("employee-device-"+park).accessType(access).resourceType("PERSON").resourceId(String.valueOf(employeeId)).serviceType("1").credentialChannel("FACE").build();
        Window window=Window.builder().from(LocalDateTime.of(2026,9,1,0,0)).to(LocalDateTime.of(2026,9,30,0,0)).build();
        Source source=Source.builder().parkId(park).subjectId(String.valueOf(employeeId)).authId(String.valueOf(park)).after(after).badge("badge-"+park).imageId("image-ref-"+park).personSnapshot("{\"personName\":\"合成\",\"gender\":0}")
            .resource(ResourceInput.builder().resource(key).participation("INCLUDE").window(window).build()).build();
        Long batch=employee.accept("add-"+park,Collections.singletonList(source),Collections.singleton(park)).getBatches().get(park).get(0);
        while(employee.stageNext(batch)){}employee.bindNextLane(batch,null);employee.finish(batch);
        Long target=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",Long.class,batch);
        AuthOperationClaimedTarget c=service.claim(AuthOperationClaimCommand.builder().parkId(park).accessType(access).operationQueue("AUTH").maxCount(1).leaseSeconds(300L).targetIds(Collections.singletonList(target)).build()).get(0);
        String instance="ISC".equals(access)?iscInstance:directInstance;SmtAuthTransportPhase p=transport.prepare(park,instance,c);
        if("ISC".equals(access)){p=download(p);transport.begin(park,instance,Collections.singletonList(p.getId()),Collections.singletonMap(p.getId(),"person-real"));transport.accepted(park,instance,Collections.singletonList(p.getId()),"download-real");}
        else transport.begin(park,instance,Collections.singletonList(p.getId()),null);
        if(deleted)jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=?",p.getDeviceId());else jdbc.update("UPDATE SMT_DEVICE SET PARK_ID=? WHERE ID=?",park+1,p.getDeviceId());
        Assert.assertTrue(transport.receipt(park,instance,p.getId(),"ISC".equals(access)?"person-real":null,p.getDeviceId(),"ISC".equals(access)?"download-real":p.getSerialNo(),"frozen-add",true,"合成成功").isSourceConverged());
        Assert.assertEquals(Integer.valueOf(park),jdbc.queryForObject("SELECT PARK_ID FROM "+("ISC".equals(access)?"SMT_ISC_DOWN_RECORD":"SMT_TASK_DOWN_RECORD")+" WHERE CARD_NO=?",Integer.class,String.valueOf(employeeId)));
        if(!deleted)jdbc.update("UPDATE SMT_DEVICE SET PARK_ID=? WHERE ID=?",park,p.getDeviceId());
    }
    @Test public void personOwnerTwentyDevicesUseOneCreationAndNineteenOwnFaces() {
        List<SmtAuthTransportPhase> configs=personConfigs(20);java.util.concurrent.atomic.AtomicInteger get=new java.util.concurrent.atomic.AtomicInteger(),create=new java.util.concurrent.atomic.AtomicInteger(),face=new java.util.concurrent.atomic.AtomicInteger(),config=new java.util.concurrent.atomic.AtomicInteger();
        com.tce.smart.dispatcher.api.feign.RemoteDispatcherService remote=org.mockito.Mockito.mock(com.tce.smart.dispatcher.api.feign.RemoteDispatcherService.class);
        org.mockito.Mockito.when(remote.dispatch(org.mockito.Mockito.any(),org.mockito.Mockito.anyString())).thenAnswer(inv->{com.tce.smart.dispatcher.api.dto.req.DispatcherDTO d=inv.getArgument(0);int event=d.getEventType();
            if(event==com.tce.smart.dispatcher.api.enums.EventEnum.ISC_PERSON_GET.getCode()){get.incrementAndGet();return com.tce.smart.common.core.model.Result.success("{\"list\":[]}");}
            if(event==com.tce.smart.dispatcher.api.enums.EventEnum.ISC_PERSON_ADD.getCode()){create.incrementAndGet();return com.tce.smart.common.core.model.Result.success("{\"personId\":\"owner-created\"}");}
            if(event==com.tce.smart.dispatcher.api.enums.EventEnum.ISC_FACE_ADD.getCode()){face.incrementAndGet();return com.tce.smart.common.core.model.Result.success("{}");}
            config.incrementAndGet();return com.tce.smart.common.core.model.Result.success("{\"taskId\":\"config-"+d.getDeviceId()+"\"}");});
        com.tce.smart.platform.core.service.SmtImageService images=org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtImageService.class);org.mockito.Mockito.when(images.getImageBase64ByCode(org.mockito.Mockito.anyString())).thenReturn("synthetic-per-attempt-image");
        com.tce.smart.schedule.config.AuthOperationSchedulerProperties props=new com.tce.smart.schedule.config.AuthOperationSchedulerProperties();com.tce.smart.schedule.config.AuthOperationSchedulerProperties.Instance instance=new com.tce.smart.schedule.config.AuthOperationSchedulerProperties.Instance();instance.setId(iscInstance);instance.setAccessType("ISC");instance.setParks(Collections.singletonList(park));props.setInstances(Collections.singletonList(instance));
        AuthOperationTransportFacade facade=new AuthOperationTransportFacade(transport,remote,images,props);org.springframework.test.util.ReflectionTestUtils.setField(facade,"hfOrg","frozen-org");
        for(SmtAuthTransportPhase c:configs){com.tce.smart.platform.core.dto.authtransport.AuthTransport.Run r=facade.submitPreparedExact(park,iscInstance,Collections.singletonList(c.getId()),3);Assert.assertEquals(1,r.getProcessed());Assert.assertTrue(r.getHttpUsed()<=3);}
        Assert.assertEquals(1,get.get());Assert.assertEquals(1,create.get());Assert.assertEquals(19,face.get());Assert.assertEquals(20,config.get());
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_PERSON_OWNER WHERE INSTANCE_ID=? AND STATE='ACCEPTED'",Integer.class,iscInstance));
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=? AND PHASE='ISC_PERSON'",Integer.class,iscInstance));
        Assert.assertEquals(Integer.valueOf(19),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=? AND PHASE='ISC_FACE' AND STATE='ACCEPTED'",Integer.class,iscInstance));
        Assert.assertEquals(Integer.valueOf(19),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=? AND PHASE='ISC_CONFIG' AND PERSON_PROOF_PHASE_ID IS NOT NULL",Integer.class,iscInstance));
    }
    @Test public void concurrentPersonOwnersUseOnePhaseAndExpiredIntentCannotTransfer() throws Exception {
        List<SmtAuthTransportPhase> configs=personConfigs(2);java.util.concurrent.ExecutorService workers=java.util.concurrent.Executors.newFixedThreadPool(2);java.util.concurrent.CountDownLatch start=new java.util.concurrent.CountDownLatch(1);
        try{List<java.util.concurrent.Future<com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity>> results=new ArrayList<>();for(SmtAuthTransportPhase c:configs)results.add(workers.submit(()->{start.await();return transport.preparePersonIdentity(park,iscInstance,c.getId(),"frozen-org");}));start.countDown();
            int owner=0,wait=0;Long proof=null;for(java.util.concurrent.Future<com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity> f:results){com.tce.smart.platform.core.dto.authtransport.AuthTransport.PersonIdentity r=f.get(15,java.util.concurrent.TimeUnit.SECONDS);if("OWNER_NEEDS_LOOKUP".equals(r.getOutcome()))owner++;if("WAITING_PERSON".equals(r.getOutcome()))wait++;proof=r.getProofPhaseId();}Assert.assertEquals(1,owner);Assert.assertEquals(1,wait);
            Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_PERSON_OWNER WHERE INSTANCE_ID=?",Integer.class,iscInstance));transport.begin(park,iscInstance,Collections.singletonList(proof),null);
            jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET UPDATE_TIME=SYS_EXTRACT_UTC(SYSTIMESTAMP)-INTERVAL '3' MINUTE WHERE ID=?",proof);Assert.assertEquals(1,transport.expireIntents(park,iscInstance,200));
            for(SmtAuthTransportPhase c:configs)Assert.assertEquals("WAITING_PERSON",transport.preparePersonIdentity(park,iscInstance,c.getId(),"frozen-org").getOutcome());
            try{transport.begin(park,iscInstance,Collections.singletonList(proof),null);Assert.fail();}catch(IllegalArgumentException expected){}
            transport.accepted(park,iscInstance,Collections.singletonList(proof),"late-owner-person");for(SmtAuthTransportPhase c:configs)Assert.assertEquals("REUSE_CREATED_IDENTITY",transport.preparePersonIdentity(park,iscInstance,c.getId(),"frozen-org").getOutcome());
        }finally{start.countDown();workers.shutdownNow();Assert.assertTrue(workers.awaitTermination(15,java.util.concurrent.TimeUnit.SECONDS));}
    }
    @Test public void personAcceptedOwnerIdentityAndPhaseRollBackTogether(){
        SmtAuthTransportPhase config=personConfigs(1).get(0);Long proof=transport.preparePersonIdentity(park,iscInstance,config.getId(),"frozen-org").getProofPhaseId();transport.begin(park,iscInstance,Collections.singletonList(proof),null);
        jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET STATE='VERIFYING' WHERE ID=?",proof);
        try{transport.accepted(park,iscInstance,Collections.singletonList(proof),"must-rollback");Assert.fail();}catch(IllegalArgumentException expected){}
        Assert.assertEquals("INTENT",personOwners.lockByPhase(proof).getState());Assert.assertNull(personOwners.lockByPhase(proof).getPersonId());Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_IDENTITY WHERE INSTANCE_ID=? AND PERSON_ID='must-rollback'",Integer.class,iscInstance));
        jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET STATE='UNKNOWN' WHERE ID=?",proof);transport.accepted(park,iscInstance,Collections.singletonList(proof),"committed-person");Assert.assertEquals("ACCEPTED",personOwners.lockByPhase(proof).getState());Assert.assertEquals("committed-person",phases.byId(proof).getExternalId());
    }
    private List<SmtAuthTransportPhase> personConfigs(int count){
        List<ResourceInput> resources=new ArrayList<>();SmtStaffDeviceAuth relation=generatedStaff.selectById(park);
        for(int n=0;n<count;n++){String device="person-device-"+park+"-"+n;jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC,CHANNEL_NO) VALUES(?,?,1,1)",device,park);
            resources.add(ResourceInput.builder().resource(ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(String.valueOf(employeeId)).deviceId(device).accessType("ISC").resourceType("PERSON").resourceId(String.valueOf(employeeId)).serviceType("1").credentialChannel("FACE").build()).participation("INCLUDE").window(Window.builder().from(LocalDateTime.of(2026,9,1,0,0)).to(LocalDateTime.of(2026,9,30,0,0)).build()).build());}
        Source source=Source.builder().parkId(park).subjectId(String.valueOf(employeeId)).authId(String.valueOf(park)).before(relation).after(relation).badge("badge-"+park).imageId("image-ref-"+park).personSnapshot("{\"personName\":\"合成员工\",\"gender\":1}").resources(resources).build();
        Long batch=employee.accept("person-owner-"+park,Collections.singletonList(source),Collections.singleton(park)).getBatches().get(park).get(0);while(employee.stageNext(batch)){}String cursor=null;while(true){String next=employee.bindNextLane(batch,cursor);if(next==null)break;cursor=next;}employee.finish(batch);
        List<Long> ids=jdbc.queryForList("SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=? ORDER BY ID",Long.class,batch);Assert.assertEquals(count,ids.size());List<AuthOperationClaimedTarget> claims=service.claim(AuthOperationClaimCommand.builder().parkId(park).accessType("ISC").operationQueue("AUTH").maxCount(count).leaseSeconds(30L).targetIds(ids).build());Assert.assertEquals(count,claims.size());List<SmtAuthTransportPhase> phases=new ArrayList<>();for(AuthOperationClaimedTarget claim:claims)phases.add(transport.prepare(park,iscInstance,claim));return phases;
    }
    private SmtAuthTransportPhase prepared(String access) {return prepared(access,Collections.singletonList(park));}
    private SmtAuthTransportPhase prepared(String access,List<Integer> rows) {
        jdbc.update("UPDATE SMT_DEVICE SET IS_SYNC=?,CHANNEL_NO=1 WHERE ID=?","ISC".equals(access)?1:0,"employee-device-"+park);
        entry.removeAuthToDevice(rows,Collections.emptyList());Long batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);
        while(employee.stageNext(batch)){}employee.bindNextLane(batch,null);employee.finish(batch);
        Long targetId=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",Long.class,batch);
        AuthOperationClaimedTarget claim=service.claim(AuthOperationClaimCommand.builder().parkId(park).accessType(access).operationQueue("AUTH").maxCount(1).leaseSeconds(30L).targetIds(Collections.singletonList(targetId)).build()).get(0);
        return transport.prepare(park,"ISC".equals(access)?iscInstance:directInstance,claim);
    }
    private SmtAuthTransportPhase download(SmtAuthTransportPhase c) {
        transport.begin(park,iscInstance,Collections.singletonList(c.getId()),Collections.singletonMap(c.getId(),"person-real"));transport.accepted(park,iscInstance,Collections.singletonList(c.getId()),"config-real");
        Assert.assertNull(jdbc.queryForObject("SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID=?",String.class,c.getAttemptId()));
        return transport.prepareDownload(park,iscInstance,Collections.singletonList(c.getId())).get(0);
    }
    private int sourceRows(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId);}
    private int eventCount(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",Integer.class,park);}
    private static class FailingRecords extends SmtTaskDownRecordServiceImpl {
        boolean fail;FailingRecords(SmtDeviceMapper devices){super(devices,org.mockito.Mockito.mock(StaffDeviceAuthSyncService.class));}
        @Override public void handleTaskDownRecord(SmtDeviceTask task){super.handleTaskDownRecord(task);if(fail)throw new IllegalStateException("合成记录失败");}
    }
}
