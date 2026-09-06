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

/** 容量与 ISC 管道专用组装；真实 Mapper、事务、来源同步和完成器，按唯一园区收尾。 */
public abstract class AuthOperationCapacityFixture {
    protected AuthOperationSchedulerService ledger;
    protected HikariDataSource pool;
    protected JdbcTemplate jdbc;
    protected AuthOperationTransportService transport;
    protected AuthOperationTransportMapper phases;
    protected SmtTaskDownRecordServiceImpl down;
    protected com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter adapter;
    protected int staffCount=1;
    protected final SqlTiming sqlTiming=new SqlTiming();

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
        jdbc=new JdbcTemplate(pool);jdbc.setQueryTimeout(30);park=fixturePark();
        ensureSelectionSchema();
        if(jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE'",Integer.class)==0)
            new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(new ClassPathResource("authoperation/transport-schema.sql")).execute(pool);

        employeeId=9000000000L+park;
        if(seedInitialFixture()) {
        jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,BADGE,NAME) VALUES(?,?,1,?,?,?)",employeeId,"employee-test-"+park,"image-ref-"+park,"badge-"+park,"合成员工");
        jdbc.update("INSERT INTO SMT_PARK_BU(ID,PARK_ID,COMP_ID) VALUES(?,?,?)",park,park,"employee-test-"+park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,2)",park,park);
        jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,0)","employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park,park,"employee-device-"+park,park);
        jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00.123',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",park,employeeId,park);
        }
        MybatisConfiguration cfg=new MybatisConfiguration();cfg.setDefaultStatementTimeout(30);cfg.addInterceptor(sqlTiming);cfg.setMapUnderscoreToCamelCase(true);
        Class<?>[] mappers={SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,
            SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,SmtAuthSubjectCoordMapper.class,
            SmtAuthSourceCoordMapper.class,SmtAuthResourceCoordMapper.class,SmtAuthSourceResourceMapper.class,
            SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class,EmployeeAuthOperationMapper.class,AuthOperationTransportMapper.class,AuthOperationSchedulerMapper.class};
        List<Resource> xml=new ArrayList<>();for(Class<?> type:mappers) { cfg.addMapper(type);xml.add(new ClassPathResource("mapper/"+type.getSimpleName()+".xml")); }
        cfg.addMapper(SmtDeviceAuthorityRelationMapper.class);cfg.addMapper(SmtStaffMapper.class);cfg.addMapper(SmtDeviceMapper.class);cfg.addMapper(SmtIscDeviceTaskMapper.class);cfg.addMapper(SmtIscDownRecordMapper.class);cfg.addMapper(SmtStaffDeviceAuthMapper.class);cfg.addMapper(SmtDeviceTaskMapper.class);cfg.addMapper(SmtTaskDownRecordMapper.class);
        MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(cfg);
        factory.setMapperLocations(xml.toArray(new Resource[0]));SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());
        DataSourceTransactionManager tm=fixtureTransactionManager();outer=new org.springframework.transaction.support.TransactionTemplate(tm);
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
        SmtIscDownRecordServiceImpl ir=new SmtIscDownRecordServiceImpl(devices,sync);org.springframework.test.util.ReflectionTestUtils.setField(ir,"baseMapper",session.getMapper(SmtIscDownRecordMapper.class));
        SmtIscDeviceTaskServiceImpl it=new SmtIscDeviceTaskServiceImpl();org.springframework.test.util.ReflectionTestUtils.setField(it,"baseMapper",session.getMapper(SmtIscDeviceTaskMapper.class));
        DirectTaskCompletionService dc=proxy(new DirectTaskCompletionService(dt,down),tm);IscTaskCompletionService ic=proxy(new IscTaskCompletionService(it,ir),tm);
        transport=proxy(new AuthOperationTransportService(phases,service,session.getMapper(AuthOperationWorkflowMapper.class),versions,session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),devices,generatedTask,session.getMapper(SmtIscDeviceTaskMapper.class),employee,enabled,dc,ic),tm);


    }
    /** 跨 JVM 驱动只覆盖测试装配：恢复园区、跳过造数及真实提交边界钩子。 */
    protected int fixturePark() {return 100000+(int)(Math.abs(UUID.randomUUID().getMostSignificantBits()%600000000));}
    protected boolean seedInitialFixture() {return true;}
    protected DataSourceTransactionManager fixtureTransactionManager() {return new DataSourceTransactionManager(pool);}

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

    protected void ensureSelectionSchema() throws Exception {
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


    protected void stopScheduler(AuthOperationScheduler scheduler) throws Exception {
        scheduler.stop();
        java.lang.reflect.Field field=AuthOperationScheduler.class.getDeclaredField("executors");field.setAccessible(true);
        for(Object executor:((Map<?,?>)field.get(scheduler)).values())
            Assert.assertTrue("调度线程必须退出后才能清理本用例数据",((java.util.concurrent.ThreadPoolExecutor)executor).awaitTermination(30,java.util.concurrent.TimeUnit.SECONDS));
    }
    /** 只汇总语句标识与耗时，避免容量日志包含绑定数据或逐行调试开销。 */
    @org.apache.ibatis.plugin.Intercepts({
        @org.apache.ibatis.plugin.Signature(type=org.apache.ibatis.executor.Executor.class,method="update",args={org.apache.ibatis.mapping.MappedStatement.class,Object.class}),
        @org.apache.ibatis.plugin.Signature(type=org.apache.ibatis.executor.Executor.class,method="query",args={org.apache.ibatis.mapping.MappedStatement.class,Object.class,org.apache.ibatis.session.RowBounds.class,org.apache.ibatis.session.ResultHandler.class})
    })
    protected static class SqlTiming implements org.apache.ibatis.plugin.Interceptor {
        private final Map<String,long[]> stats=new java.util.concurrent.ConcurrentHashMap<>();
        private volatile long deadlineNanos;
        protected void deadlineAfterSeconds(int seconds){deadlineNanos=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(seconds);}
        protected void clearDeadline(){deadlineNanos=0;}
        @Override public Object intercept(org.apache.ibatis.plugin.Invocation invocation) throws Throwable {
            if(deadlineNanos>0 && System.nanoTime()>deadlineNanos)throw new IllegalStateException("容量阶段超过配置的诊断时限，回滚当前事务并停止");
            String id=((org.apache.ibatis.mapping.MappedStatement)invocation.getArgs()[0]).getId();long start=System.nanoTime();
            try{return invocation.proceed();}finally{
                long elapsed=System.nanoTime()-start;long[] row=stats.computeIfAbsent(id,k->new long[3]);
                synchronized(row){row[0]++;row[1]+=elapsed;row[2]=Math.max(row[2],elapsed);}
            }
        }
        @Override public Object plugin(Object target){return org.apache.ibatis.plugin.Plugin.wrap(target,this);}
        @Override public void setProperties(Properties properties){}
        protected List<Map<String,Object>> snapshot(){
            List<Map<String,Object>> rows=new ArrayList<>();
            for(Map.Entry<String,long[]> e:stats.entrySet()){Map<String,Object> row=new LinkedHashMap<>();long[] n=e.getValue();synchronized(n){row.put("statement",e.getKey());row.put("calls",n[0]);row.put("totalMs",n[1]/1000000);row.put("maxMs",n[2]/1000000);}rows.add(row);}
            rows.sort((a,b)->Long.compare((Long)b.get("totalMs"),(Long)a.get("totalMs")));return rows;
        }
    }

}
