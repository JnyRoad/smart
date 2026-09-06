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

/** 独立物理结算Oracle组装，复制固定siee84d5边界并注入真实回滚点；不改容量fixture。 */
public abstract class AuthTransportPhysicalSettlementFixture {
    protected AuthOperationSchedulerService ledger;
    protected HikariDataSource pool;
    protected JdbcTemplate jdbc;
    protected AuthOperationTransportService transport;
    protected AuthOperationTransportMapper phases;
    protected SmtTaskDownRecordServiceImpl down;
    protected com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter adapter;
    protected int staffCount=1;
    protected boolean failRecord,failFinish;
    protected volatile Long gatedPhase;
    protected volatile java.util.concurrent.CountDownLatch phaseReaders;

    protected AuthOperationWorkflowService service;
    protected AuthOperationService operations;
    protected AuthOperationVersionService versions;
    protected int park;
    protected long employeeId;
    protected EmployeeAuthOperationService employee;
    protected EmployeeAuthOperationMapper selection;
    protected SmtStaffDeviceAuthMapper generatedStaff;
    protected SmtDeviceTaskMapper generatedTask;
    protected SmtTaskDownRecordMapper generatedRecord;
    protected com.tce.smart.platform.service.impl.SmtStaffDeviceAuthServiceImpl entry;

    protected org.springframework.transaction.support.TransactionTemplate outer;

    @Before public void setup() throws Exception {
        String url=System.getenv("SMART_AUTH_ORACLE_URL");Assume.assumeTrue(url!=null);
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",url);
        Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
        if(pool==null) { pool=new HikariDataSource();pool.setJdbcUrl(url);pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
            pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("auth-workflow-test"); }
        jdbc=new JdbcTemplate(pool);jdbc.setQueryTimeout(30);park=100000+(int)(Math.abs(UUID.randomUUID().getMostSignificantBits()%600000000));
        ensureSelectionSchema();
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE'",Integer.class));

        employeeId=9000000000L+park;
        jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) VALUES(?,?,1,?,?,?)",employeeId,"employee-test-"+park,"image-ref-"+park,"badge-"+park,"合成员工");
        jdbc.update("INSERT INTO SMT_PARK_BU(ID,PARK_ID,COMP_ID) VALUES(?,?,?)",park,park,"employee-test-"+park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",park,park);
        jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,0)","employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park,park,"employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",park,employeeId,park);
        MybatisConfiguration cfg=new MybatisConfiguration();cfg.setDefaultStatementTimeout(30);cfg.addInterceptor(new FinishFailure());cfg.setMapUnderscoreToCamelCase(true);
        Class<?>[] mappers={SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,
            SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,SmtAuthSubjectCoordMapper.class,
            SmtAuthSourceCoordMapper.class,SmtAuthResourceCoordMapper.class,SmtAuthSourceResourceMapper.class,
            SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class,EmployeeAuthOperationMapper.class,AuthOperationTransportMapper.class,AuthOperationSchedulerMapper.class,AuthOperationPersonOwnerMapper.class};
        List<Resource> xml=new ArrayList<>();for(Class<?> type:mappers) { cfg.addMapper(type);xml.add(new ClassPathResource("mapper/"+type.getSimpleName()+".xml")); }
        cfg.addMapper(SmtDeviceAuthorityRelationMapper.class);cfg.addMapper(SmtStaffMapper.class);cfg.addMapper(SmtDeviceMapper.class);cfg.addMapper(SmtIscDeviceTaskMapper.class);cfg.addMapper(SmtIscDownRecordMapper.class);cfg.addMapper(SmtStaffDeviceAuthMapper.class);cfg.addMapper(SmtDeviceTaskMapper.class);cfg.addMapper(SmtTaskDownRecordMapper.class);
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
        phases=session.getMapper(AuthOperationTransportMapper.class);
        ledger=proxy(new AuthOperationSchedulerService(session.getMapper(AuthOperationSchedulerMapper.class),operations),tm);
        SmtDeviceMapper devices=session.getMapper(SmtDeviceMapper.class);
        StaffDeviceAuthSyncService sync=new StaffDeviceAuthSyncService(generatedStaff,session.getMapper(SmtDeviceAuthorityRelationMapper.class),generatedRecord,session.getMapper(SmtIscDownRecordMapper.class),session.getMapper(SmtStaffMapper.class));
        sync.setTransportGuard(new AuthOperationTransportGuard(pool,enabled));
        down=new SmtTaskDownRecordServiceImpl(devices,sync);org.springframework.test.util.ReflectionTestUtils.setField(down,"baseMapper",generatedRecord);
        SmtDeviceTaskServiceImpl dt=new SmtDeviceTaskServiceImpl(down,null,null,devices,null);org.springframework.test.util.ReflectionTestUtils.setField(dt,"baseMapper",generatedTask);
        SmtIscDownRecordServiceImpl ir=new SmtIscDownRecordServiceImpl(devices,sync){@Override public void handleTaskDownRecord(SmtIscDeviceTask task){super.handleTaskDownRecord(task);if(failRecord)throw new IllegalStateException("合成record失败");}};org.springframework.test.util.ReflectionTestUtils.setField(ir,"baseMapper",session.getMapper(SmtIscDownRecordMapper.class));
        SmtIscDeviceTaskServiceImpl it=new SmtIscDeviceTaskServiceImpl();org.springframework.test.util.ReflectionTestUtils.setField(it,"baseMapper",session.getMapper(SmtIscDeviceTaskMapper.class));
        DirectTaskCompletionService dc=proxy(new DirectTaskCompletionService(dt,down),tm);IscTaskCompletionService ic=proxy(new IscTaskCompletionService(it,ir),tm);
        AuthOperationTransportService rawTransport=new AuthOperationTransportService(phases,service,session.getMapper(AuthOperationWorkflowMapper.class),versions,session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),devices,generatedTask,session.getMapper(SmtIscDeviceTaskMapper.class),employee,enabled,dc,ic);org.springframework.test.util.ReflectionTestUtils.setField(rawTransport,"personOwners",session.getMapper(AuthOperationPersonOwnerMapper.class));transport=proxy(rawTransport,tm);


    }
    @SuppressWarnings("unchecked") protected <T> T proxy(T raw,DataSourceTransactionManager tm) {
        ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();
    }

    @After public void cleanup() {
        if(jdbc==null)return;
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_JOB WHERE INSTANCE_ID=?","capacity-"+park);
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_ROUTE WHERE INSTANCE_ID=?","capacity-"+park);
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_QUOTA WHERE INSTANCE_ID=?","capacity-"+park);
        jdbc.update("DELETE FROM SMT_AUTH_SCHEDULER_STATE WHERE INSTANCE_ID=?","capacity-"+park);
        jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_IDENTITY WHERE INSTANCE_ID=? AND PARK_ID=?","capacity-"+park,park);
        jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_REVIEW WHERE PARK_ID=? OR (PARK_ID IS NULL AND DEVICE_ID IN (SELECT ID FROM SMT_DEVICE WHERE PARK_ID=?))",park,park);
        jdbc.update("DELETE FROM SMT_AUTH_PERSON_OWNER WHERE INSTANCE_ID=?","capacity-"+park);
        jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_PHASE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_ISC_DOWN_RECORD WHERE DEVICE_CODE IN (SELECT ID FROM SMT_DEVICE WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_ISC_DEVICE_TASK WHERE DEVICE_CODE IN (SELECT ID FROM SMT_DEVICE WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_TASK_DOWN_RECORD WHERE DEVICE_CODE IN (SELECT ID FROM SMT_DEVICE WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_DEVICE_TASK WHERE DEVICE_CODE IN (SELECT ID FROM SMT_DEVICE WHERE PARK_ID=?)",park);
        jdbc.update("DELETE FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (SELECT ID FROM SMT_STAFF WHERE COMP_ID=?)","employee-test-"+park);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_DEVICE WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_PARK_BU WHERE PARK_ID=?",park);
        jdbc.update("DELETE FROM SMT_STAFF WHERE COMP_ID=?","employee-test-"+park);
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
        pool.close();
    }

    protected void ensureSelectionSchema() {
        Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN ('SMT_AUTH_SELECTION_SOURCE','SMT_AUTH_SELECTION_RESOURCE')",Integer.class));
        Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_PERSON_OWNER'",Integer.class));
    }
    /** 在真实FINISHED SQL已执行后注入异常，证明外层事务连同event/record一起回滚。 */
    @org.apache.ibatis.plugin.Intercepts({@org.apache.ibatis.plugin.Signature(type=org.apache.ibatis.executor.Executor.class,method="update",args={org.apache.ibatis.mapping.MappedStatement.class,Object.class}),
        @org.apache.ibatis.plugin.Signature(type=org.apache.ibatis.executor.Executor.class,method="query",args={org.apache.ibatis.mapping.MappedStatement.class,Object.class,org.apache.ibatis.session.RowBounds.class,org.apache.ibatis.session.ResultHandler.class})})
    protected class FinishFailure implements org.apache.ibatis.plugin.Interceptor {
        @Override public Object intercept(org.apache.ibatis.plugin.Invocation call)throws Throwable {
            Object result=call.proceed();String id=((org.apache.ibatis.mapping.MappedStatement)call.getArgs()[0]).getId();
            Object parameter=call.getArgs()[1];
            if(gatedPhase!=null && id.endsWith("AuthOperationTransportMapper.byId") && result instanceof List && !((List<?>)result).isEmpty()){
                SmtAuthTransportPhase phase=(SmtAuthTransportPhase)((List<?>)result).get(0);
                if(gatedPhase.equals(phase.getId()) && "ACCEPTED".equals(phase.getState())){phaseReaders.countDown();if(!phaseReaders.await(3,java.util.concurrent.TimeUnit.SECONDS))throw new IllegalStateException("两个回执未同时读取ACCEPTED");}
            }
            if(failFinish && id.endsWith("AuthOperationTransportMapper.transition") && parameter instanceof Map && "FINISHED".equals(((Map<?,?>)parameter).get("state")))throw new IllegalStateException("合成phase结束提交前失败");
            return result;
        }
        @Override public Object plugin(Object target){return org.apache.ibatis.plugin.Plugin.wrap(target,this);}
        @Override public void setProperties(Properties properties){}
    }
}
