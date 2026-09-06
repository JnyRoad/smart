package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.*;
import org.junit.*;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** 使用既有合成表验证直连成功事务，不建表、不访问真实业务数据或设备。 */
public class DirectTaskCompletionOracleTest {
    private JdbcTemplate jdbc;
    private SmtDeviceTaskMapper taskMapper;
    private DirectTaskCompletionService completion;
    private FailingRecords records;
    private TransactionTemplate transaction;
    private int taskId;
    private int derivedId;
    private String deviceId;

    @Before public void setUp() throws Exception {
        String url = System.getenv("SMART_AUTH_ORACLE_URL");
        Assume.assumeTrue("仅显式启用本任务合成 Oracle", url != null && !url.isEmpty());
        Assert.assertTrue(url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));
        Assert.assertEquals("SMART_AUTH_TEST", System.getenv("SMART_AUTH_ORACLE_USER"));
        DriverManagerDataSource ds = new DriverManagerDataSource(url, System.getenv("SMART_AUTH_ORACLE_USER"),
                System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        jdbc = new JdbcTemplate(ds);
        DataSourceTransactionManager manager = new DataSourceTransactionManager(ds);
        transaction = new TransactionTemplate(manager);
        MybatisConfiguration config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        config.addMapper(SmtDeviceTaskMapper.class);
        config.addMapper(SmtTaskDownRecordMapper.class);
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(ds); factory.setConfiguration(config);
        SqlSessionTemplate session = new SqlSessionTemplate(factory.getObject());
        taskMapper = session.getMapper(SmtDeviceTaskMapper.class);
        SmtDeviceMapper devices = Mockito.mock(SmtDeviceMapper.class);
        SmtDevice device = new SmtDevice(); device.setParkId(ThreadLocalRandom.current().nextInt(100000, 900000));
        Mockito.when(devices.selectById(Mockito.any())).thenReturn(device);
        records = new FailingRecords(devices);
        ReflectionTestUtils.setField(records, "baseMapper", session.getMapper(SmtTaskDownRecordMapper.class));
        SmtDeviceTaskServiceImpl tasks = new SmtDeviceTaskServiceImpl(records, null, null, devices, null);
        ReflectionTestUtils.setField(tasks, "baseMapper", taskMapper);
        ProxyFactory proxy = new ProxyFactory(new DirectTaskCompletionService(tasks, records));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        completion = (DirectTaskCompletionService) proxy.getProxy();
        taskId = ThreadLocalRandom.current().nextInt(100000000, 900000000);
        derivedId = taskId + 1000000000;
        deviceId = "direct-test-" + UUID.randomUUID();
        jdbc.update("INSERT INTO SMT_DEVICE_TASK (ID, STATUS, ACTION, DEVICE_TYPE, SERVICE_TYPE, DEVICE_CODE, CARD_NO, SERIAL_NO, START_TIME, OVER_TIME) VALUES (?,0,2,1,1,?,?,?,1780000000,1790000000)",
                taskId, deviceId, "synthetic-card", "current-command");
        jdbc.update("INSERT INTO SMT_TASK_DOWN_RECORD (ID,TASK_ID,DEVICE_CODE,CARD_NO,DEVICE_TYPE,SERVICE_TYPE) VALUES (?,?,?,?,1,1)",
                taskId, taskId, deviceId, "synthetic-card");
    }

    @Test public void recordFailureRollsBackStatusAndRecordAndAllowsSameSnapshotRetry() {
        SmtDeviceTask snapshot = taskMapper.selectById(taskId);
        records.fail = true;
        try { completion.completeSuccess(snapshot, 200, "成功", null); Assert.fail(); }
        catch (IllegalStateException expected) { Assert.assertEquals("合成记录失败", expected.getMessage()); }
        assertState(0, 1);
        records.fail = false;
        Assert.assertTrue(completion.completeSuccess(snapshot, 200, "成功", null));
        assertState(1, 0);
    }

    @Test public void derivedWriteFailureRollsBackAllThreeWrites() {
        try {
            completion.completeSuccess(taskMapper.selectById(taskId), 200, "成功", completed -> {
                // 合成表无自增主键：以实际记录删除加显式派生 ID 验证三项写入回滚，不声称验证新增任务主键生成。
                jdbc.update("INSERT INTO SMT_DEVICE_TASK (ID,STATUS,ACTION,DEVICE_CODE,SERIAL_NO) VALUES (?,0,2,?,?)",
                        derivedId, deviceId, "derived-command");
                throw new IllegalStateException("合成派生失败");
            });
            Assert.fail();
        } catch (IllegalStateException expected) { Assert.assertEquals("合成派生失败", expected.getMessage()); }
        assertState(0, 1);
        Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject("SELECT COUNT(*) FROM SMT_DEVICE_TASK WHERE ID=?", Integer.class, derivedId));
    }

    @Test public void staleSerialAndStaleFailureCannotOverwriteSuccessOrCancel() {
        SmtDeviceTask stale = taskMapper.selectById(taskId); stale.setSerialNo("old-command");
        Assert.assertFalse(completion.completeSuccess(stale, 200, "旧成功", null));
        Assert.assertFalse(completion.recordResult(stale, 2, 402, "旧失败", 1L));
        assertState(0, 1);
        SmtDeviceTask initial = taskMapper.selectById(taskId);
        Assert.assertTrue(completion.completeSuccess(initial, 200, "成功", null));
        Assert.assertFalse(completion.recordResult(initial, 2, 402, "迟到失败", 1L));
        assertState(1, 0);
        jdbc.update("UPDATE SMT_DEVICE_TASK SET STATUS=4 WHERE ID=?", taskId);
        Assert.assertFalse(completion.recordResult(initial, 2, 402, "已取消后的失败", 1L));
        Assert.assertEquals(Integer.valueOf(4), taskMapper.selectById(taskId).getStatus());
    }

    @Test public void concurrentReceiptsProduceExactlyOneLocalCompletion() throws Exception {
        SmtDeviceTask first = taskMapper.selectById(taskId), second = taskMapper.selectById(taskId);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger derived = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> a = pool.submit(() -> { start.await(); return completion.completeSuccess(first, 200, "成功", t -> derived.incrementAndGet()); });
            Future<Boolean> b = pool.submit(() -> { start.await(); return completion.completeSuccess(second, 200, "成功", t -> derived.incrementAndGet()); });
            start.countDown();
            Assert.assertTrue(a.get(20, TimeUnit.SECONDS) ^ b.get(20, TimeUnit.SECONDS));
            Assert.assertEquals(1, derived.get()); assertState(1, 0);
        } finally { pool.shutdownNow(); Assert.assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS)); }
    }

    @Test public void requiredTransactionRollsBackWithOuterCaller() {
        transaction.execute(status -> {
            Assert.assertTrue(completion.completeSuccess(taskMapper.selectById(taskId), 200, "成功", null));
            status.setRollbackOnly(); return null;
        });
        assertState(0, 1);
    }

    @Test public void lateAcceptanceCannotResetSuccessfulTask() {
        SmtDeviceTask pending = taskMapper.selectById(taskId);
        Assert.assertTrue(completion.completeSuccess(pending, 200, "成功", null));
        Assert.assertFalse(completion.recordResult(pending, null, 200, "受理", 2L));
        assertState(1, 0);
    }

    private void assertState(int status, int records) {
        Assert.assertEquals(Integer.valueOf(status), taskMapper.selectById(taskId).getStatus());
        Assert.assertEquals(Integer.valueOf(records), jdbc.queryForObject("SELECT COUNT(*) FROM SMT_TASK_DOWN_RECORD WHERE DEVICE_CODE=?", Integer.class, deviceId));
    }

    @After public void tearDown() {
        if (jdbc != null && deviceId != null) {
            jdbc.update("DELETE FROM SMT_TASK_DOWN_RECORD WHERE DEVICE_CODE=?", deviceId);
            jdbc.update("DELETE FROM SMT_DEVICE_TASK WHERE DEVICE_CODE=?", deviceId);
        }
    }

    /** 保留真实 Mapper 写入，仅在记录处理后注入异常。 */
    private static class FailingRecords extends SmtTaskDownRecordServiceImpl {
        private boolean fail;
        private FailingRecords(SmtDeviceMapper devices) { super(devices, Mockito.mock(StaffDeviceAuthSyncService.class)); }
        @Override public void handleTaskDownRecord(SmtDeviceTask task) {
            super.handleTaskDownRecord(task);
            if (fail) { throw new IllegalStateException("合成记录失败"); }
        }
    }
}
