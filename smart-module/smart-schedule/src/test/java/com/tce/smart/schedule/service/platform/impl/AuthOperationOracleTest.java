package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationAppendCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationBatchResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationClaimedTarget;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationProgress;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationRequestCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmissionCommand;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationSubmissionResult;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationTargetCommand;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import com.tce.smart.platform.core.mapper.SmtAuthDeleteRequestMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationAttemptMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationBatchMapper;
import com.tce.smart.platform.core.mapper.SmtAuthOperationTargetMapper;
import com.tce.smart.platform.core.mapper.SmtAuthResultEventMapper;
import com.tce.smart.platform.core.service.impl.AuthOperationService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 仅在显式注入的本机合成 Oracle 上验证权限批次的真实事务、租约和回执门槛。
 * 没有环境变量时跳过，不创建数据库对象，也不连接实际设备。
 */
public class AuthOperationOracleTest {

	private JdbcTemplate jdbc;
	private DataSourceTransactionManager transactionManager;
	private AuthOperationService service;
	private SmtAuthOperationBatchMapper batchMapper;
	private SmtAuthDeleteRequestMapper requestMapper;
	private SmtAuthOperationTargetMapper targetMapper;
	private SmtAuthOperationAttemptMapper attemptMapper;
	private SmtAuthResultEventMapper eventMapper;
	private long batchId;
	private int parkId;

	/**
	 * 将真实新 Mapper XML 接到隔离库，并用生产事务注解代理持久服务。
	 */
	@Before
	public void setUp() throws Exception {
		String url = System.getenv("SMART_AUTH_ORACLE_URL");
		Assume.assumeTrue("仅显式启用时执行临时 Oracle 测试", url != null && !url.isEmpty());
		Assert.assertTrue("本用例只允许本机 Oracle", url.startsWith("jdbc:oracle:thin:@//127.0.0.1:"));
		Assert.assertEquals("必须使用本任务合成 schema", "SMART_AUTH_TEST",
				System.getenv("SMART_AUTH_ORACLE_USER"));

		DriverManagerDataSource dataSource = new DriverManagerDataSource(url,
				System.getenv("SMART_AUTH_ORACLE_USER"), System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
		dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
		jdbc = new JdbcTemplate(dataSource);
		transactionManager = new DataSourceTransactionManager(dataSource);

		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setMapUnderscoreToCamelCase(true);
		configuration.addMapper(SmtAuthOperationBatchMapper.class);
		configuration.addMapper(SmtAuthDeleteRequestMapper.class);
		configuration.addMapper(SmtAuthOperationTargetMapper.class);
		configuration.addMapper(SmtAuthOperationAttemptMapper.class);
		configuration.addMapper(SmtAuthResultEventMapper.class);
		MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setConfiguration(configuration);
		factory.setMapperLocations(new PathMatchingResourcePatternResolver()
				.getResources("classpath*:mapper/SmtAuth*.xml"));
		SqlSessionFactory sessionFactory = factory.getObject();
		SqlSessionTemplate session = new SqlSessionTemplate(sessionFactory);
		batchMapper = session.getMapper(SmtAuthOperationBatchMapper.class);
		requestMapper = session.getMapper(SmtAuthDeleteRequestMapper.class);
		targetMapper = session.getMapper(SmtAuthOperationTargetMapper.class);
		attemptMapper = session.getMapper(SmtAuthOperationAttemptMapper.class);
		eventMapper = session.getMapper(SmtAuthResultEventMapper.class);

		service = proxiedService(targetMapper, attemptMapper, eventMapper);

		batchId = positiveId();
		parkId = 10000 + (int) (positiveId() % 800000000L);
	}

	private AuthOperationService proxiedService(SmtAuthOperationTargetMapper targetMapperOverride,
			SmtAuthOperationAttemptMapper attemptMapperOverride,
			SmtAuthResultEventMapper eventMapperOverride) {
		AuthOperationService raw = new AuthOperationService(batchMapper, requestMapper,
				targetMapperOverride, attemptMapperOverride, eventMapperOverride);
		ProxyFactory proxy = new ProxyFactory(raw);
		proxy.setProxyTargetClass(true);
		proxy.addAdvice(new TransactionInterceptor(transactionManager,
				new AnnotationTransactionAttributeSource()));
		return (AuthOperationService) proxy.getProxy();
	}

	/**
	 * 领取查询相同候选时，两个真实事务只有一个条件更新能拿到令牌并创建尝试。
	 */
	@Test
	public void concurrentClaimsOnlyOneLeaseWins() throws Exception {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-race", "device-race", 0L, 1L);
		service.finishExpansion(batchId, 1);

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService workers = Executors.newFixedThreadPool(2);
		try {
			Future<List<AuthOperationClaimedTarget>> first = workers.submit(() -> {
				start.await();
				return claimOne();
			});
			Future<List<AuthOperationClaimedTarget>> second = workers.submit(() -> {
				start.await();
				return claimOne();
			});
			start.countDown();
			List<AuthOperationClaimedTarget> firstResult = first.get(20, TimeUnit.SECONDS);
			List<AuthOperationClaimedTarget> secondResult = second.get(20, TimeUnit.SECONDS);
			Assert.assertEquals("竞争领取必须恰好一个成功", 1,
					(firstResult.size() == 1 ? 1 : 0) + (secondResult.size() == 1 ? 1 : 0));
			Assert.assertEquals("同一目标只能有一个持久尝试", Integer.valueOf(1),
				jdbc.queryForObject("SELECT COUNT(1) FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID = ?",
						Integer.class, targetId));
			Assert.assertEquals("获租约目标必须进入执行中", "EXECUTING",
				jdbc.queryForObject("SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, targetId));
		} finally {
			workers.shutdownNow();
			Assert.assertTrue("并发测试线程必须退出", workers.awaitTermination(20, TimeUnit.SECONDS));
		}
	}

	/**
	 * 一个分片第二个目标触发唯一约束时，request、首个目标和批次游标全部回滚。
	 */
	@Test
	public void failedShardRollsBackRequestsTargetsAndCursor() {
		createBatch(2);
		long firstRequestId = positiveId();
		long secondRequestId = positiveId();
		long firstTargetId = positiveId();
		long secondTargetId = positiveId();
		AuthOperationRequestCommand firstRequest = request(firstRequestId, "source-1");
		AuthOperationRequestCommand secondRequest = request(secondRequestId, "source-2");
		try {
			service.appendTargets(AuthOperationAppendCommand.builder()
					.batchId(batchId).previousCursor(0L).nextCursor(2L)
					.request(firstRequest).request(secondRequest)
					.target(target(firstTargetId, firstRequestId, "target-1", "device-same"))
					.target(target(secondTargetId, secondRequestId, "target-2", "device-same"))
					.build());
				Assert.fail("同一物理目标唯一冲突必须回滚分片");
		} catch (RuntimeException expected) {
			Assert.assertTrue("应报告目标唯一冲突或持久化错误",
					expected.getMessage() == null || expected.getMessage().contains("目标")
							|| expected.getCause() != null);
		}
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_DELETE_REQUEST WHERE BATCH_ID = ?", Integer.class, batchId));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?", Integer.class, batchId));
		Assert.assertEquals(Long.valueOf(0L), jdbc.queryForObject(
				"SELECT EXPANSION_CURSOR FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ?", Long.class, batchId));
		Assert.assertEquals(Integer.valueOf(0), jdbc.queryForObject(
				"SELECT EXPANDED_COUNT FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ?", Integer.class, batchId));
	}

	/**
	 * 已持久分片的相同目标键若内容变化，必须冲突且不得推进第二次游标。
	 */
	@Test
	public void duplicateShardContentConflictDoesNotMoveCursor() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-retry", "device-original", 0L, 1L);
		try {
			service.appendTargets(AuthOperationAppendCommand.builder()
					.batchId(batchId).previousCursor(0L).nextCursor(1L)
					.request(request(requestId, "source-1"))
					.target(target(targetId, requestId, "target-retry", "device-changed"))
					.build());
			Assert.fail("重复分片内容变化必须拒绝");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("target-retry"));
		}
		Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?", Integer.class, batchId));
		Assert.assertEquals(Long.valueOf(1L), jdbc.queryForObject(
				"SELECT EXPANSION_CURSOR FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ?", Long.class, batchId));
	}

	/**
	 * 结束展开只能使用受理时冻结的 EXPECTED_COUNT，调用方不能用较小参数掩盖遗漏目标。
	 */
	@Test
	public void finishExpansionCannotLowerFrozenExpectedCount() {
		createBatch(100);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-frozen-count", "device-frozen", 0L, 1L);

		try {
			service.finishExpansion(batchId, 1);
			Assert.fail("结束展开不能降低受理时冻结的预期数量");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("预期"));
		}
		Assert.assertEquals("批次仍应处于展开中", "PREPARING", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ?", String.class, batchId));
		Assert.assertEquals("目标仍应处于准备中", "PREPARING", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, targetId));
		Assert.assertEquals(Long.valueOf(1L), jdbc.queryForObject(
				"SELECT EXPANSION_CURSOR FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ?", Long.class, batchId));
	}

	/**
	 * 缺少本次回执的完整外部标识时，即使带可信标记或 DEVICE_SUCCESS 也只能核验。
	 */
	@Test
	public void receiptWithoutExternalIdentifiersCannotConfirm() {
		AuthOperationClaimedTarget claimed = prepareAndMarkSubmitted();
		AuthOperationReceiptResult result = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC")
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-missing-external")
				.evidenceType("DEVICE_SUCCESS").resultStatus("DEVICE_SUCCESS")
				.evidenceBody("{\"ok\":true}").trustedDeviceEvidence(true).localConverged(true).build());
		Assert.assertFalse(result.isConfirmed());
		Assert.assertFalse(result.isConverged());
		Assert.assertEquals("VERIFYING", result.getState());
		Assert.assertEquals("VERIFYING", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, claimed.getTargetId()));
	}

	/**
	 * 回执版本过期时，完整外部映射和可信成功也不能确认当前操作版本。
	 */
	@Test
	public void receiptVersionMismatchCannotConfirm() {
		AuthOperationClaimedTarget claimed = prepareAndMarkSubmitted();
		AuthOperationReceiptResult result = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC")
				.externalBatchId("external-batch-1").externalCommandId("external-command-1")
				.operationVersion(999L).eventNamespace("ISC:receipt").eventKey("event-version-mismatch")
				.evidenceType("DEVICE_ACK").resultStatus("SUCCESS").evidenceBody("{\"ok\":true}")
				.trustedDeviceEvidence(true).build());
		Assert.assertFalse(result.isConfirmed());
		Assert.assertEquals("VERIFYING", result.getState());
	}

	/**
	 * 弱证据进入 VERIFYING 后保留当前尝试归属；租约到期可被有界扫描，晚到的可信回执仍可确认。
	 */
	@Test
	public void verifyingRetainsAttemptForLateTrustedReceipt() {
		AuthOperationClaimedTarget claimed = prepareAndMarkSubmitted();
		AuthOperationReceiptResult weak = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC")
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-weak-first")
				.evidenceType("DEVICE_ACK").resultStatus("SUCCESS").evidenceBody("{\"weak\":true}")
				.trustedDeviceEvidence(false).build());
		Assert.assertEquals("VERIFYING", weak.getState());
		Assert.assertEquals("租约仍应归属于当前尝试", claimed.getLeaseToken(), jdbc.queryForObject(
				"SELECT LEASE_TOKEN FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, claimed.getTargetId()));

		jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_UNTIL = ? WHERE ID = ?",
				java.sql.Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC()).minusMinutes(5)),
				claimed.getTargetId());
		List<com.tce.smart.platform.core.entity.SmtAuthOperationTarget> expired =
				service.findExpiredUnfinishedTargets(parkId, LocalDateTime.now(Clock.systemUTC()), 10);
		Assert.assertEquals("VERIFYING 到期目标应可有界恢复", 1, expired.size());

		AuthOperationReceiptResult lateTrusted = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC")
				.externalBatchId("external-batch-1").externalCommandId("external-command-1")
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-late-trusted")
				.evidenceType("DEVICE_SUCCESS").resultStatus("SUCCESS").evidenceBody("{\"ok\":true}")
				.trustedDeviceEvidence(true).build());
		Assert.assertTrue("晚到且归属匹配的可信回执应允许确认", lateTrusted.isConfirmed());
		Assert.assertEquals("CONFIRMED", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, claimed.getTargetId()));
	}

	/**
	 * ISC 只保存真实配置/下载任务号，不要求伪造直连命令号即可确认。
	 */
	@Test
	public void iscOnlyExternalBatchIdentifierCanConfirm() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		String taskId = "isc-task-" + targetId;
		appendOne(requestId, targetId, "target-isc-only", "device-isc", 0L, 1L, "ISC");
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		AuthOperationSubmissionCommand submission = AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC").taskId(taskId)
				.externalBatchId(taskId).build();
		Assert.assertTrue(service.markSubmitted(submission).isPersisted());
		AuthOperationReceiptResult result = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC").externalBatchId(taskId)
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("isc-only-success")
				.evidenceType("DEVICE_SUCCESS").resultStatus("SUCCESS").evidenceBody("{\"ok\":true}")
				.trustedDeviceEvidence(true).build());
		Assert.assertTrue(result.isConfirmed());
	}

	/**
	 * 直连只保存真实命令流水号，不要求伪造 ISC 批次号即可确认。
	 */
	@Test
	public void directOnlyExternalCommandIdentifierCanConfirm() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		String serialNo = "direct-serial-" + targetId;
		appendOne(requestId, targetId, "target-direct-only", "device-direct", 0L, 1L, "DIRECT");
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		AuthOperationSubmissionCommand submission = AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("DIRECT").taskId(serialNo)
				.externalCommandId(serialNo).build();
		Assert.assertTrue(service.markSubmitted(submission).isPersisted());
		AuthOperationReceiptResult result = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("DIRECT").externalCommandId(serialNo)
				.operationVersion(3L).eventNamespace("DIRECT:receipt").eventKey("direct-only-success")
				.evidenceType("DEVICE_SUCCESS").resultStatus("SUCCESS").evidenceBody("{\"ok\":true}")
				.trustedDeviceEvidence(true).build());
		Assert.assertTrue(result.isConfirmed());
	}

	/**
	 * ISC 接入的任务号必须是当前尝试的真实 taskId，不能用第二个伪造编号凑齐通用字段。
	 */
	@Test
	public void iscWrongExternalIdentifierCannotConfirm() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		String taskId = "isc-task-expected-" + targetId;
		appendOne(requestId, targetId, "target-isc-wrong", "device-isc-wrong", 0L, 1L, "ISC");
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		service.markSubmitted(AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC").taskId(taskId)
				.externalBatchId(taskId).externalCommandId("real-command")
				.build());
		try {
			service.markSubmitted(AuthOperationSubmissionCommand.builder()
					.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
					.leaseToken(claimed.getLeaseToken()).accessType("ISC").taskId(taskId)
					.externalBatchId("isc-task-wrong-" + targetId).externalCommandId("fabricated-command")
					.build());
			Assert.fail("ISC 不得用错误批次号或伪造命令号登记");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("task")
					|| expected.getMessage().contains("批次")
					|| expected.getMessage().contains("标识"));
		}
	}

	/**
	 * 直连命令流水号必须保留真实 serialNo，不能把不同接入的编号串入尝试。
	 */
	@Test
	public void directWrongExternalIdentifierCannotConfirm() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		String serialNo = "direct-serial-expected-" + targetId;
		appendOne(requestId, targetId, "target-direct-wrong", "device-direct-wrong", 0L, 1L, "DIRECT");
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		service.markSubmitted(AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("DIRECT").taskId(serialNo)
				.externalBatchId("real-batch").externalCommandId(serialNo)
				.build());
		try {
			service.markSubmitted(AuthOperationSubmissionCommand.builder()
					.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
					.leaseToken(claimed.getLeaseToken()).accessType("DIRECT").taskId(serialNo)
					.externalBatchId("fabricated-batch").externalCommandId("direct-serial-wrong-" + targetId)
					.build());
			Assert.fail("直连不得登记错误命令流水号");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("命令")
					|| expected.getMessage().contains("标识")
					|| expected.getMessage().contains("批次")
					|| expected.getMessage().contains("serial"));
		}
	}

	/**
	 * 未知接入没有可核对的外部标识命名空间，不能直接登记为待确认。
	 */
	@Test
	public void unknownAccessCannotBeRegistered() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-unknown-access", "device-unknown", 0L, 1L, "UNKNOWN");
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		try {
			service.markSubmitted(AuthOperationSubmissionCommand.builder()
					.targetId(targetId).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
					.leaseToken(claimed.getLeaseToken()).accessType("UNKNOWN").taskId("unknown-task")
					.externalBatchId("unknown-batch").externalCommandId("unknown-command").build());
			Assert.fail("未知接入不能直接登记外部受理");
		} catch (IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage().contains("未知")
					|| expected.getMessage().contains("接入"));
		}
	}

	/**
	 * DTO 与参数形式的 prepareSubmission 都必须在同一 REQUIRED 事务内，注入异常时不留下 SUBMITTING。
	 */
	@Test
	public void prepareSubmissionOverloadsRollbackOnInjectedMapperFailure() {
		AuthOperationClaimedTarget claimed = prepareClaimedTarget("ISC", "atomic-prepare");
		SmtAuthOperationAttemptMapper failingAttemptMapper = Mockito.mock(
				SmtAuthOperationAttemptMapper.class, AdditionalAnswers.delegatesTo(attemptMapper));
		Mockito.doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			attemptMapper.prepareSubmission((Long) args[0], (Long) args[1], (String) args[2],
					(String) args[3], (LocalDateTime) args[4]);
			throw new IllegalStateException("injected prepare failure");
		}).when(failingAttemptMapper).prepareSubmission(Mockito.anyLong(), Mockito.anyLong(),
				Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime.class));
		AuthOperationService failingService = proxiedService(targetMapper, failingAttemptMapper, eventMapper);

		try {
			failingService.prepareSubmission(claimed.getTargetId(), claimed.getAttemptId(), claimed.getAttemptNo(),
					claimed.getLeaseToken(), "ISC", "atomic-prepare");
			Assert.fail("参数形式 prepareSubmission 异常必须回滚");
		} catch (IllegalStateException expected) {
			Assert.assertEquals("injected prepare failure", expected.getMessage());
		}
		assertAttemptStillClaimed(claimed);

		AuthOperationSubmissionCommand command = AuthOperationSubmissionCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC").taskId("atomic-prepare").build();
		try {
			failingService.prepareSubmission(command);
			Assert.fail("DTO 形式 prepareSubmission 异常必须回滚");
		} catch (IllegalStateException expected) {
			Assert.assertEquals("injected prepare failure", expected.getMessage());
		}
		assertAttemptStillClaimed(claimed);
	}

	/**
	 * DTO 与参数形式的 markSubmitted 都必须保证尝试和目标状态原子更新。
	 */
	@Test
	public void markSubmittedOverloadsRollbackOnInjectedTargetFailure() {
		AuthOperationClaimedTarget claimed = prepareClaimedTarget("ISC", "atomic-submit");
		SmtAuthOperationTargetMapper failingTargetMapper = Mockito.mock(
				SmtAuthOperationTargetMapper.class, AdditionalAnswers.delegatesTo(targetMapper));
		Mockito.doThrow(new IllegalStateException("injected target failure"))
				.when(failingTargetMapper).markWaitingConfirmByLease(Mockito.anyLong(), Mockito.anyString(),
						Mockito.any(LocalDateTime.class));
		AuthOperationService failingService = proxiedService(failingTargetMapper, attemptMapper, eventMapper);

		try {
			failingService.markSubmitted(claimed.getTargetId(), claimed.getAttemptId(), claimed.getAttemptNo(),
					claimed.getLeaseToken(), "ISC", "atomic-submit", "atomic-submit", null);
			Assert.fail("参数形式 markSubmitted 异常必须回滚");
		} catch (IllegalStateException expected) {
			Assert.assertEquals("injected target failure", expected.getMessage());
		}
		assertAttemptStillClaimed(claimed);

		AuthOperationSubmissionCommand command = AuthOperationSubmissionCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC").taskId("atomic-submit")
				.externalBatchId("atomic-submit").build();
		try {
			failingService.markSubmitted(command);
			Assert.fail("DTO 形式 markSubmitted 异常必须回滚");
		} catch (IllegalStateException expected) {
			Assert.assertEquals("injected target failure", expected.getMessage());
		}
		assertAttemptStillClaimed(claimed);
	}

	/**
	 * 两连接交错提交强/弱证据时，attempt 成功状态和成功事件指针不能被弱证据降级。
	 */
	@Test
	public void concurrentStrongAndWeakReceiptsKeepConfirmedAttempt() throws Exception {
		AuthOperationClaimedTarget claimed = prepareAndMarkSubmitted();
		java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
		ExecutorService workers = Executors.newFixedThreadPool(2);
		try {
			Future<AuthOperationReceiptResult> strong = workers.submit(() -> {
				start.await();
				return service.recordReceipt(AuthOperationReceiptCommand.builder()
						.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId())
						.attemptNo(claimed.getAttemptNo()).leaseToken(claimed.getLeaseToken()).accessType("ISC")
						.externalBatchId("external-batch-1").externalCommandId("external-command-1")
						.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-strong-race")
						.evidenceType("DEVICE_SUCCESS").resultStatus("SUCCESS")
						.evidenceBody("{\"strong\":true}").trustedDeviceEvidence(true).build());
			});
			Future<AuthOperationReceiptResult> weak = workers.submit(() -> {
				start.await();
				return service.recordReceipt(AuthOperationReceiptCommand.builder()
						.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId())
						.attemptNo(claimed.getAttemptNo()).leaseToken(claimed.getLeaseToken()).accessType("ISC")
						.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-weak-race")
						.evidenceType("DEVICE_ACK").resultStatus("SUCCESS")
						.evidenceBody("{\"weak\":true}").trustedDeviceEvidence(false).build());
			});
			start.countDown();
			strong.get(30, TimeUnit.SECONDS);
			weak.get(30, TimeUnit.SECONDS);
		} finally {
			workers.shutdownNow();
			Assert.assertTrue("交错回执线程必须退出", workers.awaitTermination(30, TimeUnit.SECONDS));
		}
		Assert.assertEquals("CONFIRMED", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, claimed.getTargetId()));
		Assert.assertEquals("CONFIRMED", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, claimed.getAttemptId()));
		Long strongEventId = jdbc.queryForObject(
				"SELECT ID FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID = ? AND EVENT_KEY = ?", Long.class,
				claimed.getAttemptId(), "event-strong-race");
		Assert.assertEquals(Integer.valueOf(2), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID = ?", Integer.class,
				claimed.getAttemptId()));
		Assert.assertEquals(strongEventId, jdbc.queryForObject(
				"SELECT RESULT_EVENT_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", Long.class,
				claimed.getAttemptId()));
	}

	/**
	 * 终态目标收到新事件键时仍保存证据，但不改变当前状态或成功事件指针。
	 */
	@Test
	public void terminalTargetRetainsLateEvidenceWithoutStateChange() {
		AuthOperationClaimedTarget claimed = prepareAndMarkSubmitted();
		AuthOperationReceiptResult first = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC")
				.externalBatchId("external-batch-1").externalCommandId("external-command-1")
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-terminal-first")
				.evidenceType("DEVICE_SUCCESS").resultStatus("SUCCESS").evidenceBody("{\"ok\":true}")
				.trustedDeviceEvidence(true).build());
		Assert.assertTrue(first.isConfirmed());
		Long firstEventId = jdbc.queryForObject(
				"SELECT ID FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID = ? AND EVENT_KEY = ?", Long.class,
				claimed.getAttemptId(), "event-terminal-first");

		AuthOperationReceiptResult late = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC")
				.externalBatchId("external-batch-1").externalCommandId("external-command-1")
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("event-terminal-late")
				.evidenceType("DEVICE_ACK").resultStatus("FAILED").evidenceBody("{\"ok\":false}")
				.trustedDeviceEvidence(false).build());
		Assert.assertEquals("终态目标状态不可被迟到证据改变", "CONFIRMED", late.getState());
		Assert.assertEquals("CONFIRMED", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, claimed.getTargetId()));
		Assert.assertEquals("CONFIRMED", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, claimed.getAttemptId()));
		Assert.assertEquals(firstEventId, jdbc.queryForObject(
				"SELECT RESULT_EVENT_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", Long.class,
				claimed.getAttemptId()));
		Assert.assertEquals(Integer.valueOf(2), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID = ?", Integer.class,
				claimed.getAttemptId()));
		String lateReason = jdbc.queryForObject(
				"SELECT FAILURE_REASON FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID = ? AND EVENT_KEY = ?",
				String.class, claimed.getAttemptId(), "event-terminal-late");
		Assert.assertTrue("迟到证据应说明未参与归并", lateReason.contains("终态"));
	}

	/**
	 * 新尝试已经确认目标后，旧 VERIFYING 尝试的迟到失败证据只能留存，不能升级旧尝试。
	 */
	@Test
	public void terminalReceiptFromOlderAttemptDoesNotMutateAttemptHistory() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-attempt-history", "device-attempt-history", 0L, 1L);
		service.finishExpansion(batchId, 1);

		AuthOperationClaimedTarget older = claimOne().get(0);
		AuthOperationSubmissionCommand olderSubmission = AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(older.getAttemptId()).attemptNo(older.getAttemptNo())
				.leaseToken(older.getLeaseToken()).accessType("ISC").taskId("old-batch-" + targetId)
				.externalBatchId("old-batch-" + targetId).externalCommandId("old-command-" + targetId).build();
		Assert.assertTrue(service.markSubmitted(olderSubmission).isPersisted());
		AuthOperationReceiptResult olderWeak = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(targetId).attemptId(older.getAttemptId()).attemptNo(older.getAttemptNo())
				.leaseToken(older.getLeaseToken()).accessType("ISC")
				.externalBatchId("old-batch-" + targetId).externalCommandId("old-command-" + targetId)
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("old-weak-before-retry")
				.evidenceType("DEVICE_ACK").resultStatus("FAILED").evidenceBody("{\"ok\":false}")
				.trustedDeviceEvidence(false).build());
		Assert.assertEquals("VERIFYING", olderWeak.getState());
		String olderStatusBefore = jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, older.getAttemptId());
		Long olderEventBefore = jdbc.queryForObject(
				"SELECT RESULT_EVENT_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", Long.class,
				older.getAttemptId());
		String olderConfirmedAtBefore = jdbc.queryForObject(
				"SELECT TO_CHAR(CONFIRMED_AT, 'YYYY-MM-DD HH24:MI:SS.FF6') "
						+ "FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, older.getAttemptId());

		// 模拟恢复调度：旧尝试仍保留审计归属，但目标重新进入队列，由新尝试取得新租约。
		jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET STATE = 'QUEUED', "
				+ "LEASE_TOKEN = NULL, LEASE_UNTIL = NULL WHERE ID = ?", targetId);
		AuthOperationClaimedTarget newer = claimOne().get(0);
		Assert.assertEquals("重试必须创建第二次尝试", Integer.valueOf(2), newer.getAttemptNo());
		AuthOperationSubmissionCommand newerSubmission = AuthOperationSubmissionCommand.builder()
				.targetId(targetId).attemptId(newer.getAttemptId()).attemptNo(newer.getAttemptNo())
				.leaseToken(newer.getLeaseToken()).accessType("ISC").taskId("new-batch-" + targetId)
				.externalBatchId("new-batch-" + targetId).externalCommandId("new-command-" + targetId).build();
		Assert.assertTrue(service.markSubmitted(newerSubmission).isPersisted());
		AuthOperationReceiptResult newerSuccess = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(targetId).attemptId(newer.getAttemptId()).attemptNo(newer.getAttemptNo())
				.leaseToken(newer.getLeaseToken()).accessType("ISC")
				.externalBatchId("new-batch-" + targetId).externalCommandId("new-command-" + targetId)
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("new-success")
				.evidenceType("DEVICE_SUCCESS").resultStatus("SUCCESS").evidenceBody("{\"ok\":true}")
				.trustedDeviceEvidence(true).build());
		Assert.assertTrue(newerSuccess.isConfirmed());

		AuthOperationReceiptResult lateOlderFailure = service.recordReceipt(AuthOperationReceiptCommand.builder()
				.targetId(targetId).attemptId(older.getAttemptId()).attemptNo(older.getAttemptNo())
				.leaseToken(older.getLeaseToken()).accessType("ISC")
				.externalBatchId("old-batch-" + targetId).externalCommandId("old-command-" + targetId)
				.operationVersion(3L).eventNamespace("ISC:receipt").eventKey("old-failure-after-retry")
				.evidenceType("DEVICE_ACK").resultStatus("FAILED").evidenceBody("{\"ok\":false}")
				.trustedDeviceEvidence(false).build());
		Assert.assertEquals("终态目标状态必须保持确认", "CONFIRMED", lateOlderFailure.getState());
		Assert.assertEquals("CONFIRMED", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, targetId));
		Assert.assertEquals("新尝试必须保持确认", "CONFIRMED", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, newer.getAttemptId()));
		Assert.assertEquals("旧尝试状态不可被终态迟到失败提升", olderStatusBefore, jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, older.getAttemptId()));
		Assert.assertEquals("旧尝试事件指针不可被终态迟到失败改写", olderEventBefore, jdbc.queryForObject(
				"SELECT RESULT_EVENT_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", Long.class,
				older.getAttemptId()));
		Assert.assertEquals("旧尝试确认时间不可被终态迟到失败写入", olderConfirmedAtBefore, jdbc.queryForObject(
				"SELECT TO_CHAR(CONFIRMED_AT, 'YYYY-MM-DD HH24:MI:SS.FF6') "
						+ "FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, older.getAttemptId()));
		Assert.assertEquals("迟到证据仍须写入旧尝试事件历史", Integer.valueOf(2), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_RESULT_EVENT WHERE ATTEMPT_ID = ?", Integer.class,
				older.getAttemptId()));
	}

	/**
	 * 外部已受理的待确认目标到期时仍不可被普通领取重发，只进入待核验列表。
	 */
	@Test
	public void waitingConfirmTimeoutIsNotBlindlyResubmitted() {
		AuthOperationClaimedTarget claimed = prepareAndMarkSubmitted();
		jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET LEASE_UNTIL = ? WHERE ID = ?",
				java.sql.Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC()).minusMinutes(5)),
				claimed.getTargetId());
		Assert.assertTrue("WAITING_CONFIRM 不能回到普通领取队列", claimOne().isEmpty());
		List<com.tce.smart.platform.core.entity.SmtAuthOperationTarget> expired =
				service.findExpiredUnfinishedTargets(parkId, LocalDateTime.now(Clock.systemUTC()), 10);
		Assert.assertEquals(1, expired.size());
		Assert.assertEquals("WAITING_CONFIRM", expired.get(0).getState());
		Assert.assertEquals(Integer.valueOf(1), jdbc.queryForObject(
				"SELECT COUNT(1) FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID = ?",
				Integer.class, claimed.getTargetId()));
	}

	/**
	 * 只清理本用例 batch 下的子表行，保留 T010 基线和其他测试数据。
	 */
	@After
	public void tearDown() {
		if (jdbc == null || batchId == 0) {
			return;
		}
		jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN "
				+ "(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?)", batchId);
		jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN "
				+ "(SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?)", batchId);
		jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID = ?", batchId);
		jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE BATCH_ID = ?", batchId);
		jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE ID = ?", batchId);
	}

	private void createBatch(int expectedCount) {
		jdbc.update("INSERT INTO SMT_AUTH_OPERATION_BATCH "
				+ "(ID, PARK_ID, IDEMPOTENCY_KEY, ACTION, SOURCE_TYPE, SOURCE_ID, "
				+ "SELECTION_SNAPSHOT, PAYLOAD_FINGERPRINT, EXPECTED_COUNT, EXPANDED_COUNT, "
				+ "EXPANSION_CURSOR, STATUS) VALUES (?, ?, ?, 'DELETE', 'STAFF', 'staff-1', ?, ?, ?, 0, 0, 'PREPARING')",
				batchId, parkId, "oracle-auth-" + batchId,
				"{\"staffId\":\"staff-1\"}", "oracle-fingerprint-" + batchId, expectedCount);
	}

	private void appendOne(long requestId, long targetId, String targetKey, String deviceId,
			long previousCursor, long nextCursor) {
		appendOne(requestId, targetId, targetKey, deviceId, previousCursor, nextCursor, "ISC");
	}

	private void appendOne(long requestId, long targetId, String targetKey, String deviceId,
			long previousCursor, long nextCursor, String accessType) {
		service.appendTargets(AuthOperationAppendCommand.builder()
				.batchId(batchId).previousCursor(previousCursor).nextCursor(nextCursor)
				.request(request(requestId, "source-1"))
				.target(target(targetId, requestId, targetKey, deviceId, accessType))
				.build());
	}

	private AuthOperationRequestCommand request(long requestId, String sourceRowId) {
		return AuthOperationRequestCommand.builder()
				.id(requestId).parkId(parkId).subjectType("STAFF").sourceType("STAFF")
				.sourceRowId(sourceRowId).sourceIdentityKey("staff-" + sourceRowId)
				.identitySnapshot("{\"staffId\":\"staff-1\"}").generation(0L).build();
	}

	private AuthOperationTargetCommand target(long targetId, long requestId, String targetKey,
			String deviceId) {
		return target(targetId, requestId, targetKey, deviceId, "ISC");
	}

	private AuthOperationTargetCommand target(long targetId, long requestId, String targetKey,
			String deviceId, String accessType) {
		return AuthOperationTargetCommand.builder()
				.id(targetId).requestId(requestId).parkId(parkId).targetKey(targetKey)
				.subjectType("STAFF").subjectId("staff-1").subjectSnapshot("{\"staffId\":\"staff-1\"}")
				.resourceType("PERMISSION").deviceId(deviceId).resourceId("resource-1")
				.accessType(accessType).operationQueue("DELETE").action("DELETE")
				.operationVersion(3L).legacyTaskId("legacy-" + targetId).build();
	}

	private List<AuthOperationClaimedTarget> claimOne() {
		return service.claim(AuthOperationClaimCommand.builder()
				.parkId(parkId).operationQueue("DELETE").maxCount(1).leaseSeconds(60L).build());
	}

	private AuthOperationClaimedTarget prepareAndMarkSubmitted() {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-receipt", "device-receipt", 0L, 1L);
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		AuthOperationSubmissionCommand submission = AuthOperationSubmissionCommand.builder()
				.targetId(claimed.getTargetId()).attemptId(claimed.getAttemptId()).attemptNo(claimed.getAttemptNo())
				.leaseToken(claimed.getLeaseToken()).accessType("ISC").taskId("external-batch-1").build();
		AuthOperationSubmissionResult prepared = service.prepareSubmission(submission);
		Assert.assertTrue(prepared.isPersisted());
		AuthOperationSubmissionResult marked = service.markSubmitted(submission.toBuilder()
				.externalBatchId("external-batch-1").externalCommandId("external-command-1").build());
		Assert.assertTrue(marked.isPersisted());
		Assert.assertEquals("WAITING_CONFIRM", marked.getStatus());
		AuthOperationSubmissionResult repeated = service.markSubmitted(submission.toBuilder()
				.externalBatchId("external-batch-1").externalCommandId("external-command-1").build());
		Assert.assertTrue("响应丢失后的重复受理登记应保持幂等", repeated.isPersisted());
		Assert.assertEquals("WAITING_CONFIRM", repeated.getStatus());
		Assert.assertEquals("WAITING_CONFIRM", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, targetId));
		return claimed;
	}

	private AuthOperationClaimedTarget prepareClaimedTarget(String accessType, String taskId) {
		createBatch(1);
		long requestId = positiveId();
		long targetId = positiveId();
		appendOne(requestId, targetId, "target-atomic-" + targetId, "device-atomic-" + targetId,
				0L, 1L, accessType);
		service.finishExpansion(batchId, 1);
		AuthOperationClaimedTarget claimed = claimOne().get(0);
		Assert.assertEquals(targetId, claimed.getTargetId().longValue());
		return claimed;
	}

	private void assertAttemptStillClaimed(AuthOperationClaimedTarget claimed) {
		Assert.assertEquals("异常必须回滚尝试状态", "CLAIMED", jdbc.queryForObject(
				"SELECT STATUS FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, claimed.getAttemptId()));
		Assert.assertEquals("异常必须回滚原任务号", "legacy-" + claimed.getTargetId(), jdbc.queryForObject(
				"SELECT TASK_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class, claimed.getAttemptId()));
		Assert.assertNull("异常必须回滚外部批次号", jdbc.queryForObject(
				"SELECT EXTERNAL_BATCH_ID FROM SMT_AUTH_OPERATION_ATTEMPT WHERE ID = ?", String.class,
				claimed.getAttemptId()));
		Assert.assertEquals("目标必须仍为执行中", "EXECUTING", jdbc.queryForObject(
				"SELECT STATE FROM SMT_AUTH_OPERATION_TARGET WHERE ID = ?", String.class, claimed.getTargetId()));
	}

	private long positiveId() {
		long value = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
		return value == 0L ? 1L : value;
	}
}
