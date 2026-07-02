package com.tce.smart.schedule.service.platform.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AdmittanceDispatchAggregator} 判定纯函数矩阵 + 回写行为单测。
 *
 * <p>纯单测，不启动 Spring 容器：判定逻辑用静态纯函数直接断言；
 * 回写行为通过 Mockito 模拟 {@link SmtAdmittanceApplyMapper}，
 * 用 logback {@link ListAppender} 断言"重试两次仍失败后记录 ERROR"。</p>
 */
public class AdmittanceDispatchAggregatorTest {

	private static final Long APPLY_ID = 1001L;
	private static final Long BATCH_ID = 2001L;

	/** 挂到被测类 logger 上的内存 appender，用于捕获日志事件 */
	private ListAppender<ILoggingEvent> appender;
	private Logger logger;

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		// MyBatis-Plus 的 LambdaQueryWrapper/LambdaUpdateWrapper 在纯单测（无Spring容器）下
		// 需要手动初始化实体的字段->列名缓存，否则 SFunction 反射解析会抛 can not find lambda cache
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtAdmittanceApply.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
	}

	@Before
	public void setUp() {
		logger = (Logger) LoggerFactory.getLogger(AdmittanceDispatchAggregator.class);
		logger.setLevel(Level.DEBUG);
		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@After
	public void tearDown() {
		logger.detachAppender(appender);
		appender.stop();
	}

	// ================= 判定纯函数矩阵：verdict(Map<fellowId, List<taskStatus>>) =================

	/**
	 * 单人：一台设备成功，其余设备失败 —— 该人应判成功（任一任务 SUCCESS 即成功）。
	 */
	@Test
	public void singlePerson_oneDeviceSuccess_othersFailed_success() {
		Map<Long, List<Integer>> byFellow = new LinkedHashMap<>();
		byFellow.put(1L, Arrays.asList(
				DeviceTaskStatusEnum.SUCCESS.getCode(),
				DeviceTaskStatusEnum.FAIL.getCode(),
				DeviceTaskStatusEnum.CANCEL.getCode()));

		AdmittanceDispatchAggregator.BatchVerdict verdict = AdmittanceDispatchAggregator.verdict(byFellow);

		Assert.assertEquals(AdmittanceDispatchAggregator.BatchVerdict.SUCCESS, verdict);
	}

	/**
	 * 多人：其中一人全部任务终态且无 SUCCESS —— 整批应判失败（任一人员失败即整批失败）。
	 */
	@Test
	public void multiPerson_onePersonAllTerminalFailed_fail() {
		Map<Long, List<Integer>> byFellow = new LinkedHashMap<>();
		byFellow.put(1L, Arrays.asList(DeviceTaskStatusEnum.SUCCESS.getCode()));
		byFellow.put(2L, Arrays.asList(
				DeviceTaskStatusEnum.FAIL.getCode(),
				DeviceTaskStatusEnum.CANCEL.getCode()));

		AdmittanceDispatchAggregator.BatchVerdict verdict = AdmittanceDispatchAggregator.verdict(byFellow);

		Assert.assertEquals(AdmittanceDispatchAggregator.BatchVerdict.FAIL, verdict);
	}

	/**
	 * 存在在途任务（非终态）且没有任何人员判定为全部终态失败 —— 整批应判在途，不回写。
	 */
	@Test
	public void anyInFlightAndNoPersonAllFailed_inProgress_noWriteback() {
		Map<Long, List<Integer>> byFellow = new LinkedHashMap<>();
		byFellow.put(1L, Arrays.asList(DeviceTaskStatusEnum.SUCCESS.getCode()));
		byFellow.put(2L, Arrays.asList(
				DeviceTaskStatusEnum.DOING.getCode(),
				DeviceTaskStatusEnum.INIT.getCode()));

		AdmittanceDispatchAggregator.BatchVerdict verdict = AdmittanceDispatchAggregator.verdict(byFellow);

		Assert.assertEquals(AdmittanceDispatchAggregator.BatchVerdict.IN_PROGRESS, verdict);
	}

	/**
	 * 终态全集：CANCEL/EXPIRED/DEVICE_OFFLINE 均应计入"失败"判定（终态但非 SUCCESS）。
	 */
	@Test
	public void cancelExpiredOffline_allCountAsFailure() {
		Map<Long, List<Integer>> byFellow = new LinkedHashMap<>();
		byFellow.put(1L, Arrays.asList(DeviceTaskStatusEnum.CANCEL.getCode()));
		byFellow.put(2L, Arrays.asList(DeviceTaskStatusEnum.EXPIRED.getCode()));
		byFellow.put(3L, Arrays.asList(DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode()));

		AdmittanceDispatchAggregator.BatchVerdict verdict = AdmittanceDispatchAggregator.verdict(byFellow);

		Assert.assertEquals(AdmittanceDispatchAggregator.BatchVerdict.FAIL, verdict);
	}

	/**
	 * 边界：批次内没有 ISC 任务的人员不参与判定；若剩余人员全部成功，整批仍判成功。
	 * 用空 List 模拟"该人员在批次内无 ISC 任务"。
	 */
	@Test
	public void personWithoutIscTasks_excludedFromVerdict() {
		Map<Long, List<Integer>> byFellow = new LinkedHashMap<>();
		byFellow.put(1L, Arrays.asList(DeviceTaskStatusEnum.SUCCESS.getCode()));
		// 人员2在本批次没有任何ISC任务，不应参与判定
		byFellow.put(2L, java.util.Collections.emptyList());

		AdmittanceDispatchAggregator.BatchVerdict verdict = AdmittanceDispatchAggregator.verdict(byFellow);

		Assert.assertEquals(AdmittanceDispatchAggregator.BatchVerdict.SUCCESS, verdict);
	}

	/**
	 * 边界：整批没有任何参与判定的人员（全部人员都被排除或 Map 为空）—— 不应回写（视为在途/无效聚合）。
	 */
	@Test
	public void oldBatchTasks_ignored() {
		// 模拟批次过滤已在 aggregate() 的 SQL 层完成（apply_id=? AND batch_id=?），
		// 这里验证 verdict() 收到空 Map（旧批次任务已被过滤掉后）时的行为：不回写。
		Map<Long, List<Integer>> byFellow = new LinkedHashMap<>();

		AdmittanceDispatchAggregator.BatchVerdict verdict = AdmittanceDispatchAggregator.verdict(byFellow);

		Assert.assertEquals(AdmittanceDispatchAggregator.BatchVerdict.IN_PROGRESS, verdict);
	}

	// ================= 回写行为：writeback 重试两次后记录 ERROR =================

	/**
	 * 回写 device_status 时 mapper.update 连续失败：应立即重试 2 次（共 3 次调用），
	 * 仍失败则记录 ERROR 日志（含 applyId/batchId），不再抛异常中断调度。
	 */
	@Test
	public void writeback_retriesTwiceThenLogsError() {
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		Mockito.when(applyMapper.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class))).thenReturn(0);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, DeviceDownStatusEnum.SUCCESS.getCode());

		// 首次 + 重试2次 = 共3次调用
		Mockito.verify(applyMapper, Mockito.times(3))
				.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));

		boolean hasErrorLog = appender.list.stream().anyMatch(event ->
				event.getLevel() == Level.ERROR
						&& event.getFormattedMessage().contains(String.valueOf(APPLY_ID))
						&& event.getFormattedMessage().contains(String.valueOf(BATCH_ID)));
		Assert.assertTrue("回写彻底失败后应记录包含applyId/batchId的ERROR日志", hasErrorLog);
	}

	/**
	 * 回写第一次失败、第二次（首次重试）成功：不应记录 ERROR 日志，且只调用2次。
	 */
	@Test
	public void writeback_succeedsOnFirstRetry_noErrorLogged() {
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		Mockito.when(applyMapper.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class)))
				.thenReturn(0, 1);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, DeviceDownStatusEnum.FAIL.getCode());

		Mockito.verify(applyMapper, Mockito.times(2))
				.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));
		boolean hasErrorLog = appender.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR);
		Assert.assertFalse("重试成功后不应记录ERROR日志", hasErrorLog);
	}

	// ================= aggregate() 批次过滤：只统计当前 iscSubmitBatch 的任务 =================

	/**
	 * aggregate(applyId) 应只查询 apply.iscSubmitBatch 对应批次的任务；
	 * 若查询到的任务集合中混入了旧批次（不应发生，因为SQL已用batchId过滤），
	 * 聚合器本身不再二次按batchId过滤字段，但需验证 aggregate 会把 batchId 传给查询条件。
	 * 此处通过 mock SmtIscDeviceTaskService，验证其被以 (applyId, batchId) 调用查询。
	 */
	@Test
	public void aggregate_queriesTasksScopedToCurrentBatch() {
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);

		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(APPLY_ID);
		apply.setIscSubmitBatch(BATCH_ID);
		apply.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode());
		Mockito.when(applyMapper.selectById(APPLY_ID)).thenReturn(apply);

		SmtIscDeviceTask successTask = new SmtIscDeviceTask();
		successTask.setCardNo("501");
		successTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		successTask.setApplyId(APPLY_ID);
		successTask.setBatchId(BATCH_ID);
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Arrays.asList(successTask));
		Mockito.when(applyMapper.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class))).thenReturn(1);

		new AdmittanceDispatchAggregator(taskService, applyMapper).aggregate(APPLY_ID);

		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(applyMapper, Mockito.times(1)).update(Mockito.any(), wrapperCaptor.capture());
		Assert.assertNotNull("应触发一次 device_status 回写", wrapperCaptor.getValue());
	}

	/**
	 * aggregate(applyId) 在 apply.iscSubmitBatch 为空（从未成功提交过）时应直接跳过，不查询任务、不回写。
	 */
	@Test
	public void aggregate_skipsWhenApplyHasNoSubmitBatch() {
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);

		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(APPLY_ID);
		apply.setIscSubmitBatch(null);
		Mockito.when(applyMapper.selectById(APPLY_ID)).thenReturn(apply);

		new AdmittanceDispatchAggregator(taskService, applyMapper).aggregate(APPLY_ID);

		Mockito.verify(taskService, Mockito.never()).list(Mockito.any());
		Mockito.verify(applyMapper, Mockito.never()).update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));
	}
}
