package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtIscDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtIscDownRecordMapper;
import com.tce.smart.platform.core.service.impl.IscTaskCompletionService;
import com.tce.smart.platform.core.service.impl.SmtIscDeviceTaskServiceImpl;
import com.tce.smart.platform.core.service.impl.SmtIscDownRecordServiceImpl;
import com.tce.smart.platform.core.service.impl.StaffDeviceAuthSyncService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 仅对显式指定的本机合成 Oracle 验证 ISC 完成器的真实事务与旧批次保护。
 * 普通单测没有数据库环境变量时跳过，不创建数据库或连接实际设备。
 */
public class IscTaskCompletionOracleTest {

    private JdbcTemplate jdbc;
    private SmtIscDeviceTaskMapper taskMapper;
    private IscTaskCompletionService completion;
    private FailingDownRecordService records;
    private TransactionTemplate transaction;
    private long taskId;
    private String deviceId;

    /**
     * 将真实 Mapper 接到已授权的隔离库，并使用生产事务注解创建完成器代理。
     */
    @Before
    public void setUp() throws Exception {
        String url = System.getenv("SMART_AUTH_ORACLE_URL");
        Assume.assumeTrue("仅显式启用时执行临时 Oracle 测试", url != null && !url.isEmpty());
        Assert.assertTrue("本用例只允许本机 Oracle", url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));
        Assert.assertEquals("必须使用本任务合成 schema", "SMART_AUTH_TEST", System.getenv("SMART_AUTH_ORACLE_USER"));

        DriverManagerDataSource dataSource = new DriverManagerDataSource(url,
                System.getenv("SMART_AUTH_ORACLE_USER"), System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        jdbc = new JdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transaction = new TransactionTemplate(transactionManager);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.addMapper(SmtIscDeviceTaskMapper.class);
        configuration.addMapper(SmtIscDownRecordMapper.class);
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(configuration);
        SqlSessionTemplate session = new SqlSessionTemplate(factory.getObject());
        taskMapper = session.getMapper(SmtIscDeviceTaskMapper.class);
        SmtIscDeviceTaskServiceImpl tasks = new SmtIscDeviceTaskServiceImpl();
        ReflectionTestUtils.setField(tasks, "baseMapper", taskMapper);
        records = new FailingDownRecordService();
        ReflectionTestUtils.setField(records, "baseMapper", session.getMapper(SmtIscDownRecordMapper.class));

        // 外层代理读取生产类注解；去掉完成器事务会导致本用例观察到已提交的错误状态。
        ProxyFactory proxy = new ProxyFactory(new IscTaskCompletionService(tasks, records));
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(transactionManager,
                new AnnotationTransactionAttributeSource()));
        completion = (IscTaskCompletionService) proxy.getProxy();
        taskId = Math.abs(UUID.randomUUID().getMostSignificantBits() >>> 1);
        deviceId = "receipt-test-" + UUID.randomUUID().toString();
        jdbc.update("INSERT INTO SMT_ISC_DEVICE_TASK "
                        + "(ID, STATUS, ACTION, DEVICE_TYPE, SERVICE_TYPE, DEVICE_CODE, CARD_NO, ISC_TASK_ID) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                taskId, DeviceTaskStatusEnum.DOING.getCode(), DeviceTaskActionEnum.DEL.getCode(),
                DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT, deviceId, "synthetic-card", "batch-current");
        jdbc.update("INSERT INTO SMT_ISC_DOWN_RECORD "
                        + "(ID, TASK_ID, DEVICE_CODE, CARD_NO, DEVICE_TYPE, SERVICE_TYPE) VALUES (?, ?, ?, ?, ?, ?)",
                taskId, taskId, deviceId, "synthetic-card", DeviceTaskConstants.CARD, DeviceTaskConstants.CARD_STAFF_IMPORT);
    }

    /**
     * 删除记录后发生异常时任务与记录均回滚，重新读取持久任务后可以成功收敛。
     */
    @Test
    public void recordFailureRollsBackTaskAndRecordThenRetryConverges() {
        records.failAfterWrite = true;
        try {
            completion.completeSuccess(taskMapper.selectById(taskId), "合成设备已确认");
            Assert.fail("本地下发记录处理失败必须向调用方抛出");
        } catch (IllegalStateException expected) {
            Assert.assertEquals("合成收敛失败", expected.getMessage());
        }
        Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), taskMapper.selectById(taskId).getStatus());
        Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
                "SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", Integer.class, taskId));

        records.failAfterWrite = false;
        Assert.assertTrue(completion.completeSuccess(taskMapper.selectById(taskId), "合成设备已确认"));
        Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), taskMapper.selectById(taskId).getStatus());
        Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                "SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", Integer.class, taskId));
    }

    /**
     * 同任务已指向新 ISC 批次时，旧成功回执不能更改任务或删除记录。
     */
    @Test
    public void staleExternalBatchCannotConvergeCurrentAttempt() {
        SmtIscDeviceTask oldReceipt = taskMapper.selectById(taskId);
        oldReceipt.setIscTaskId("batch-old");
        Assert.assertFalse(completion.completeSuccess(oldReceipt, "旧批次结果"));
        Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), taskMapper.selectById(taskId).getStatus());
        Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
                "SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", Integer.class, taskId));
    }

    /**
     * 调用方后续收敛失败时，完成器必须参与同一事务，不能提前独立提交成功。
     */
    @Test
    public void callerRollbackAlsoRestoresTaskAndRecord() {
        transaction.execute(status -> {
            Assert.assertTrue(completion.completeSuccess(taskMapper.selectById(taskId), "合成设备已确认"));
            Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), taskMapper.selectById(taskId).getStatus());
            Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                    "SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", Integer.class, taskId));
            status.setRollbackOnly();
            return null;
        });
        Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), taskMapper.selectById(taskId).getStatus());
        Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
                "SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", Integer.class, taskId));
    }

    /**
     * 两个执行者持有同一处理中快照时，真实数据库只允许一个完成并维护记录。
     */
    @Test
    public void concurrentReceiptsConvergeOnlyOnce() throws Exception {
        SmtIscDeviceTask firstSnapshot = taskMapper.selectById(taskId);
        SmtIscDeviceTask secondSnapshot = taskMapper.selectById(taskId);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executors = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executors.submit(() -> {
                start.await();
                return completion.completeSuccess(firstSnapshot, "合成并发回执一");
            });
            Future<Boolean> second = executors.submit(() -> {
                start.await();
                return completion.completeSuccess(secondSnapshot, "合成并发回执二");
            });
            start.countDown();
            boolean firstCompleted = first.get(15, TimeUnit.SECONDS);
            boolean secondCompleted = second.get(15, TimeUnit.SECONDS);
            Assert.assertTrue("同一任务必须恰好一次收敛", firstCompleted ^ secondCompleted);
            Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), taskMapper.selectById(taskId).getStatus());
            Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
                    "SELECT COUNT(*) FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", Integer.class, taskId));
        } finally {
            executors.shutdownNow();
            Assert.assertTrue("并发测试线程必须退出", executors.awaitTermination(15, TimeUnit.SECONDS));
        }
    }

    /**
     * 仅回收本用例创建的两行合成数据，保留其他测试和容器。
     */
    @After
    public void tearDown() {
        if (jdbc != null && taskId != 0) {
            jdbc.update("DELETE FROM SMT_ISC_DOWN_RECORD WHERE ID = ?", taskId);
            jdbc.update("DELETE FROM SMT_ISC_DEVICE_TASK WHERE ID = ?", taskId);
        }
    }

    /**
     * 保持生产记录维护和真实数据库写入，仅在写入之后注入可控异常。
     */
    private static class FailingDownRecordService extends SmtIscDownRecordServiceImpl {
        private boolean failAfterWrite;

        /**
         * 与本测试无关的设备读取和授权组收敛用替身隔离，数据库记录维护保持真实。
         */
        private FailingDownRecordService() {
            super(Mockito.mock(SmtDeviceMapper.class), Mockito.mock(StaffDeviceAuthSyncService.class));
        }

        /**
         * 先执行真实下发记录维护，再模拟后续本地收敛异常。
         */
        @Override
        public void handleTaskDownRecord(SmtIscDeviceTask task) {
            super.handleTaskDownRecord(task);
            if (failAfterWrite) {
                throw new IllegalStateException("合成收敛失败");
            }
        }
    }
}
