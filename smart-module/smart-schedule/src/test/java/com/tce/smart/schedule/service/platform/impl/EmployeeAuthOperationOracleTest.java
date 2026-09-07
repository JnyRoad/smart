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
public class EmployeeAuthOperationOracleTest {
    private static HikariDataSource pool;
    private JdbcTemplate jdbc;
    private AuthOperationWorkflowService service;
    private AuthOperationService operations;
    private AuthOperationVersionService versions;
    private int park;
    private long employeeId;
    private EmployeeAuthOperationService employee;
    private EmployeeAuthOperationMapper selection;
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
        ensureSelectionSchema();
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
            SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class,EmployeeAuthOperationMapper.class};
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
        enabled=new com.tce.smart.platform.core.config.AuthOperationProperties();enabled.setEnabled(true);enabled.setEnabledParks(Collections.singleton(park));
        adapter=proxy(new com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter(enabled,selection,employee) {
            @Override protected Set<Integer> allowedParks(){return Collections.singleton(park);}
        },tm);
        entry=new com.tce.smart.platform.service.impl.SmtStaffDeviceAuthServiceImpl(
          org.mockito.Mockito.mock(SmtStaffDeviceAuthMapper.class),org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtDeviceTaskService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtIscDeviceTaskService.class),org.mockito.Mockito.mock(com.tce.smart.platform.service.SmtParkBuService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.service.SmtDeviceAuthorityRelationService.class),org.mockito.Mockito.mock(com.tce.smart.platform.service.SmtStaffService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtDeviceTaskDetailService.class),org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtTaskDownRecordService.class),
          org.mockito.Mockito.mock(com.tce.smart.platform.core.service.SmtIscDownRecordService.class),org.mockito.Mockito.mock(com.tce.smart.dispatcher.api.feign.RemoteDispatcherService.class));
        org.springframework.test.util.ReflectionTestUtils.setField(entry,"employeeAuthOperationAdapter",adapter);
        org.springframework.test.util.ReflectionTestUtils.setField(entry,"baseMapper",generatedStaff);
        entry=proxy(entry,tm);

    }
    @SuppressWarnings("unchecked") private <T> T proxy(T raw,DataSourceTransactionManager tm) {
        ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();
    }
    @AfterClass public static void close() { if(pool!=null)pool.close(); }
    @After public void cleanup() {
        if(jdbc==null)return;
        jdbc.update("DELETE FROM SMT_TASK_DOWN_RECORD WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_DEVICE_TASK WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_ISC_DEVICE_TASK WHERE CARD_NO=?",String.valueOf(employeeId));
        jdbc.update("DELETE FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (?,?)",employeeId,employeeId+1);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_DEVICE WHERE PARK_ID=?",park);
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
    @Test public void actualEmployeeDeleteEntryFreezesThenQueuesWithoutDeletingRelation() {
        Assert.assertTrue(entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList()));
        Long batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);
        Assert.assertEquals(1,rowCount());Assert.assertTrue(employee.pendingSubject(park,String.valueOf(employeeId)));
        Assert.assertEquals(0,targets());
        try {entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());Assert.fail("受理未stage时也不能插入第二个意图");}catch(IllegalStateException expected){}
        Assert.assertEquals(1,rowCount());
        Assert.assertTrue(employee.stageNext(batch));Assert.assertFalse(employee.stageNext(batch));
        Assert.assertNotNull(employee.bindNextLane(batch,null));Assert.assertNull(employee.bindNextLane(batch,null));employee.finish(batch);
        Assert.assertEquals(1,targets());Assert.assertEquals("DELETE",jdbc.queryForObject("SELECT ACTION FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",String.class,batch));
        Assert.assertEquals("QUEUED",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,batch));Assert.assertEquals(1,rowCount());
        Long target=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",Long.class,batch);
        Assert.assertEquals("badge-"+park,employee.sourcesForTarget(target).get(0).getBadge());Assert.assertEquals("image-ref-"+park,employee.sourcesForTarget(target).get(0).getImageId());
    }
    @Test public void sourceTimestampOrWindowChangedCannotBeDeletedByFrozenHandler() {
        entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());Long batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);employee.stageNext(batch);
        SmtAuthSelectionSource s=selection.source(batch,1);
        Assert.assertEquals(123,s.getOldCreateTime().getTime()%1000);
        jdbc.update("UPDATE SMT_STAFF_DEVICE_AUTH SET END_TIME=TIMESTAMP '2026-10-01 00:00:00' WHERE ID=?",park);
        Assert.assertFalse(employee.apply(snapshot(s)));Assert.assertEquals(1,rowCount());
        jdbc.update("UPDATE SMT_STAFF_DEVICE_AUTH SET END_TIME=TIMESTAMP '2026-09-30 00:00:00' WHERE ID=?",park);
        // 此处仅验证业务 CAS；不把直接调用 handler 当设备证据验收。
        Assert.assertTrue(employee.apply(snapshot(s)));Assert.assertEquals(0,rowCount());
    }
    @Test public void selectionWriteFailureRollsBackAcceptedBatch() {
        Source bad=source(employeeId,park).toBuilder().imageId(String.join("",Collections.nCopies(400,"x"))).build();
        try {employee.accept("rollback-"+park,Collections.singletonList(bad),Collections.singleton(park));Assert.fail("超长冻结列必须整事务回滚");}catch(org.springframework.dao.DataAccessException expected){}
        Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Integer.class,park));Assert.assertEquals(1,rowCount());
    }
    @Test public void missingDeviceSubjectIsIsolatedFromHealthySubjectInSamePark() {
        jdbc.update("INSERT INTO SMT_STAFF(ID,STATUS) VALUES(?,1)",employeeId+1);
        Source missing=source(employeeId+1,park+1).toBuilder().clearResources().verificationReason("MISSING_DEVICE_COORDINATE").build();
        Accepted accepted=employee.accept("isolated-"+park,Arrays.asList(source(employeeId,park),missing),Collections.singleton(park));
        Assert.assertEquals(2,accepted.getBatches().get(park).size());
        Long healthy=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS='PREPARING'",Long.class,park);
        Long isolated=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=? AND STATUS='VERIFYING'",Long.class,park);
        Assert.assertEquals(1,employee.sourceVerificationCount(isolated));Assert.assertEquals(1,employee.verificationSources(isolated,0,10).size());
        Assert.assertEquals(Collections.singletonList(healthy),employee.pendingExpansionBatches(Collections.singletonList(park),null,10));
        employee.stageNext(healthy);employee.bindNextLane(healthy,null);employee.finish(healthy);Assert.assertEquals(1,targets());
        Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,isolated));Assert.assertEquals(1,rowCount());
    }
    @Test public void sameOperationRetryReturnsFrozenBatchAndConflictDoesNotMutate() {
        Source source=source(employeeId,park);Accepted first=employee.accept("retry-"+park,Collections.singletonList(source),Collections.singleton(park));
        Accepted retry=employee.accept("retry-"+park,Collections.singletonList(source),Collections.singleton(park));Assert.assertEquals(first.getBatches(),retry.getBatches());
        try{employee.accept("retry-"+park,Collections.singletonList(source.toBuilder().imageId("different").build()),Collections.singleton(park));Assert.fail("变更payload不能使用同幂等key");}catch(IllegalArgumentException expected){}
        Assert.assertEquals(1,rowCount());
    }
    @Test public void threeLegacyAutoIdsReturnActualOracleGeneratedKeys() {
        SmtStaffDeviceAuth staff=new SmtStaffDeviceAuth();staff.setStaffId(employeeId);staff.setAuthId(park+1);
        Assert.assertEquals(1,generatedStaff.insert(staff));Assert.assertNotNull(staff.getId());Assert.assertTrue(staff.getId()>=1500000000);
        SmtDeviceTask task=new SmtDeviceTask();task.setCardNo(String.valueOf(employeeId));task.setDeviceCode("employee-device-"+park);task.setDeviceType(1);task.setServiceType(1);
        Assert.assertEquals(1,generatedTask.insert(task));Assert.assertNotNull(task.getId());Assert.assertTrue(task.getId()>=1500000000);
        SmtTaskDownRecord record=new SmtTaskDownRecord();record.setCardNo(String.valueOf(employeeId));record.setDeviceCode(task.getDeviceCode());record.setDeviceType(1);record.setServiceType(1);record.setParkId(park);record.setTaskId(task.getId());
        Assert.assertEquals(1,generatedRecord.insert(record));Assert.assertNotNull(record.getId());Assert.assertTrue(record.getId()>=1500000000);
        Assert.assertEquals(task.getId(),jdbc.queryForObject("SELECT TASK_ID FROM SMT_TASK_DOWN_RECORD WHERE ID=?",Integer.class,record.getId()));
    }
    @Test public void inheritedEmployeeDeleteCannotBypassPendingSelection() {
        org.springframework.test.util.ReflectionTestUtils.setField(entry,"baseMapper",generatedStaff);
        Assert.assertTrue(entry.removeByIds(Collections.singletonList(park)));
        Assert.assertEquals(1,rowCount());Assert.assertTrue(employee.pendingSubject(park,String.valueOf(employeeId)));
    }
    @Test public void newEmployeeSourceFreezesPersonThenInsertsOnlyAtConvergence() {
        jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",employeeId);
        jdbc.update("UPDATE SMT_STAFF SET NAME=?,SEX=0 WHERE ID=?","合成员工",employeeId);
        SmtStaffDeviceAuth added=new SmtStaffDeviceAuth();added.setStaffId(employeeId);added.setAuthId(park);added.setAuthType(2);
        Assert.assertEquals(Integer.valueOf(1),entry.addAuth(added));Assert.assertEquals(0,rowCount());
        Long batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);
        SmtAuthSelectionSource frozen=selection.source(batch,1);
        cn.hutool.json.JSONObject person=cn.hutool.json.JSONUtil.parseObj(frozen.getPersonSnapshot());
        Assert.assertEquals("合成员工",person.getStr("personName"));Assert.assertEquals(Integer.valueOf(1),person.getInt("gender"));
        employee.stageNext(batch);employee.bindNextLane(batch,null);employee.finish(batch);Assert.assertEquals(0,rowCount());
        // 仅验证来源写回与 Oracle 默认主键，不将直接 handler 调用当成设备回执。
        Assert.assertTrue(employee.apply(snapshot(selection.source(batch,1))));Assert.assertEquals(1,rowCount());
        Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT AUTH_TYPE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId));
        Assert.assertTrue(jdbc.queryForObject("SELECT ID FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId)>=1500000000);
    }
    @Test public void legacyWrapperPendingGuardSurvivesRolloutOffWithoutUser() {
        entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList());enabled.setEnabled(false);
        try{entry.remove(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SmtStaffDeviceAuth>().eq("STAFF_ID",employeeId));Assert.fail("离职 wrapper 不得绕过历史 pending");}catch(IllegalStateException expected){}
        Assert.assertEquals(1,rowCount());
    }
    @Test public void addWithoutPhotoAndDevicesPersistsVerification() {missingPhotoAndDevices(1);}
    @Test public void overwriteWithoutPhotoAndDevicesPersistsVerification() {missingPhotoAndDevices(2);}
    private void missingPhotoAndDevices(int mode) {
        jdbc.update("UPDATE SMT_STAFF SET FACE_PIC_ID=NULL WHERE ID=?",employeeId);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE PARK_ID=?",park);
        com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO input=new com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO();
        input.setIds(Collections.singletonList(String.valueOf(employeeId)));input.setDeviceAuthIds(Collections.singletonList(park));
        Assert.assertNotNull(entry.updateAuthNew(mode,input));
        Long batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);
        Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,batch));
        Assert.assertEquals(1,rowCount());Assert.assertEquals(0,targets());Assert.assertEquals(0,selection.resourceCount(batch));
        String reason=selection.source(batch,1).getVerificationReason();Assert.assertTrue(reason.startsWith("MISSING_DEVICE"));Assert.assertTrue(reason.contains("MISSING_CREDENTIAL_REFERENCE"));
        Assert.assertEquals(1,employee.sourceVerificationCount(batch));Assert.assertTrue(employee.pendingSubject(park,String.valueOf(employeeId)));
        Assert.assertTrue(employee.pendingExpansionBatches(Collections.singletonList(park),null,10).isEmpty());
        try{employee.stageNext(batch);Assert.fail("缺设备核验批次不得展开并伪完成");}catch(IllegalStateException expected){}
        Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,batch));
    }
    @Test public void missingDirectTaskDeviceIsVisibleBesideHealthyDevice() {missingHistoricalTask(false,1,true);}
    @Test public void missingIscTaskDeviceIsVisibleBesideHealthyDevice() {missingHistoricalTask(true,1,true);}
    @Test public void visitorTaskWithSameNumericCardIsNotEmployeeHistory() {missingHistoricalTask(false,3,false);}
    private void missingHistoricalTask(boolean isc,int serviceType,boolean verifying) {
        String table=isc?"SMT_ISC_DEVICE_TASK":"SMT_DEVICE_TASK";
        long taskId=isc?employeeId+10:park;
        jdbc.update("INSERT INTO "+table+"(ID,CARD_NO,DEVICE_CODE,DEVICE_TYPE,SERVICE_TYPE) VALUES(?,?,?,1,?)",taskId,String.valueOf(employeeId),"deleted-device-"+park,serviceType);
        Assert.assertTrue(entry.removeAuthToDevice(Collections.singletonList(park),Collections.emptyList()));
        Long batch=jdbc.queryForObject("SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",Long.class,park);
        Assert.assertEquals(verifying?"VERIFYING":"PREPARING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,batch));
        Assert.assertEquals(1,rowCount());Assert.assertEquals(1,selection.resourceCount(batch));
        if(verifying){String reason=selection.source(batch,1).getVerificationReason();Assert.assertTrue(reason.contains(isc?"ISC":"DIRECT"));Assert.assertTrue(reason.contains("TASK="+taskId));Assert.assertEquals(1,employee.sourceVerificationCount(batch));}
    }
    @Test public void newSourceWaitsForAuthorityDeviceChangeThenFreezesNewDevice() throws Exception {
        jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID) VALUES(?,?,1,?)",employeeId+1,"employee-test-"+park,"image-ref-"+park);
        String replacement="replacement-"+park;jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,0)",replacement,park);
        java.util.concurrent.ExecutorService threads=java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CountDownLatch frozen=new java.util.concurrent.CountDownLatch(1),release=new java.util.concurrent.CountDownLatch(1),started=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Future<?> changing=threads.submit(()->outer.execute(tx->{
            Assert.assertTrue(adapter.authorityDevices(park,Collections.singletonList(replacement)));
            jdbc.update("UPDATE SMT_DEVICE_AUTHORITY_RELATION SET DEVICE_ID=? WHERE AUTHORITY_ID=?",replacement,park);
            frozen.countDown();try{Assert.assertTrue(release.await(10,java.util.concurrent.TimeUnit.SECONDS));}catch(InterruptedException e){throw new IllegalStateException(e);}return null;
        }));
        try {
            Assert.assertTrue(frozen.await(10,java.util.concurrent.TimeUnit.SECONDS));
            java.util.concurrent.Future<?> adding=threads.submit(()->{started.countDown();SmtStaffDeviceAuth row=new SmtStaffDeviceAuth();row.setStaffId(employeeId+1);row.setAuthId(park);entry.addAuth(row);});
            Assert.assertTrue(started.await(3,java.util.concurrent.TimeUnit.SECONDS));
            try{adding.get(500,java.util.concurrent.TimeUnit.MILLISECONDS);Assert.fail("新主体 ADD 在组设备变更提交前不得完成");}catch(java.util.concurrent.TimeoutException expected){}
            release.countDown();changing.get(10,java.util.concurrent.TimeUnit.SECONDS);adding.get(10,java.util.concurrent.TimeUnit.SECONDS);
            Assert.assertEquals(replacement,jdbc.queryForObject("SELECT DEVICE_ID FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=? AND SUBJECT_ID=?",String.class,park,String.valueOf(employeeId+1)));
        } finally {release.countDown();threads.shutdown();Assert.assertTrue(threads.awaitTermination(15,java.util.concurrent.TimeUnit.SECONDS));}
    }
    private Source source(long staff,int row) {
        SmtStaffDeviceAuth before=new SmtStaffDeviceAuth();before.setId(row);before.setStaffId(staff);before.setAuthId(park);
        ResourceInput resource=ResourceInput.builder().resource(ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(String.valueOf(staff)).deviceId("employee-device-"+park)
         .accessType("DIRECT").resourceType("PERSON").resourceId(String.valueOf(staff)).serviceType("1").credentialChannel("FACE").build()).participation("EXCLUDE").build();
        return Source.builder().parkId(park).subjectId(String.valueOf(staff)).authId(String.valueOf(park)).before(before).imageId("photo-reference").resource(resource).build();
    }
    private SourceSnapshot snapshot(SmtAuthSelectionSource s){return SourceSnapshot.builder().sourceId(s.getSourceCoordId()).generation(s.getSourceGeneration()).sourceRowId(s.getSourceRowId()).fingerprint(s.getFingerprint()).sourceKind("STAFF_AUTH").subjectId(s.getSubjectId()).stableKey(s.getStableKey()).build();}
    private int rowCount(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,employeeId);}
    private int targets(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",Integer.class,park);}
}
