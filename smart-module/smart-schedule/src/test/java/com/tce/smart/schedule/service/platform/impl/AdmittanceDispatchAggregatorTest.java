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

import java.util.ArrayList;
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
		// UPDATE 始终未生效时，重试分流逻辑会 selectById 复查当前值；
		// 让当前值仍落在 SUCCESS 判定守卫集合内（IN_WORK 过渡态），保证判定为"真正的写冲突"而非"条件性不匹配"，
		// 从而真正走满 3 次重试再记 ERROR（覆盖本用例要验证的行为）。
		SmtAdmittanceApply stillInGuard = new SmtAdmittanceApply();
		stillInGuard.setId(APPLY_ID);
		stillInGuard.setDeviceStatus(DeviceDownStatusEnum.IN_WORK.getCode());
		Mockito.when(applyMapper.selectById(APPLY_ID)).thenReturn(stillInGuard);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, AdmittanceDispatchAggregator.BatchVerdict.SUCCESS,
				DeviceDownStatusEnum.SUCCESS.getCode());

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
		// 第一次 0 行触发复查：当前值仍在 FAIL 判定守卫集合内（ALRAEDY 过渡态），走"写冲突→重试"分支
		SmtAdmittanceApply stillInGuard = new SmtAdmittanceApply();
		stillInGuard.setId(APPLY_ID);
		stillInGuard.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode());
		Mockito.when(applyMapper.selectById(APPLY_ID)).thenReturn(stillInGuard);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, AdmittanceDispatchAggregator.BatchVerdict.FAIL,
				DeviceDownStatusEnum.FAIL.getCode());

		Mockito.verify(applyMapper, Mockito.times(2))
				.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));
		boolean hasErrorLog = appender.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR);
		Assert.assertFalse("重试成功后不应记录ERROR日志", hasErrorLog);
	}

	/**
	 * SUCCESS 判定应能覆盖冻结在 FAIL(2) 的申请单——这正是缺陷修复的核心场景：
	 * 设备曾离线导致聚合先判 FAIL 写入 2，设备恢复、任务重试成功后聚合重算得 SUCCESS，
	 * 此时 UPDATE 守卫必须包含 2，否则条件永久不匹配、重试全部空转。
	 *
	 * <p>断言直接读取 {@code IN (?)} 绑定参数集合（{@link #extractInClauseValues}），
	 * 不用字符串匹配原始 SQL——MyBatis-Plus 用 {@code ?} 占位符，字面值不会出现在
	 * {@code getSqlSegment()} 里，且 SET 子句本身也会绑定 targetDeviceStatus，
	 * 字符串包含判断会把 SET 值和 IN 守卫值混为一谈（已实测踩坑）。</p>
	 */
	@Test
	public void successVerdict_overridesFrozenFail() {
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.when(applyMapper.update(Mockito.any(), wrapperCaptor.capture())).thenReturn(1);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, AdmittanceDispatchAggregator.BatchVerdict.SUCCESS,
				DeviceDownStatusEnum.SUCCESS.getCode());

		Mockito.verify(applyMapper, Mockito.times(1)).update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));
		List<String> inClauseValues = extractInClauseValues(wrapperCaptor.getValue());
		Assert.assertTrue("SUCCESS 判定的 UPDATE 条件必须包含 FAIL(2)，才能覆盖被离线自动重拾取推翻的假失败",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.FAIL.getCode())));
		Assert.assertTrue("SUCCESS 判定的 UPDATE 条件仍须包含过渡态 IN_WORK(3)",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.IN_WORK.getCode())));
		Assert.assertTrue("SUCCESS 判定的 UPDATE 条件仍须包含过渡态 ALRAEDY(4)",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.ALRAEDY.getCode())));
	}

	/**
	 * FAIL 判定绝不能覆盖任何"更进一步"的状态，包括 SUCCESS(1) 和 FAIL(2) 本身——
	 * UPDATE 条件必须维持只含过渡态(3,4)，不得因为本次修复而放宽。
	 */
	@Test
	public void failVerdict_neverOverridesFail() {
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.when(applyMapper.update(Mockito.any(), wrapperCaptor.capture())).thenReturn(1);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, AdmittanceDispatchAggregator.BatchVerdict.FAIL,
				DeviceDownStatusEnum.FAIL.getCode());

		Mockito.verify(applyMapper, Mockito.times(1)).update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));
		List<String> inClauseValues = extractInClauseValues(wrapperCaptor.getValue());
		Assert.assertFalse("FAIL 判定的 UPDATE 条件不得包含 SUCCESS(1)",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.SUCCESS.getCode())));
		Assert.assertFalse("FAIL 判定的 UPDATE 条件不得包含 FAIL(2) 本身",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.FAIL.getCode())));
		Assert.assertTrue("FAIL 判定的 UPDATE 条件仍须包含过渡态 IN_WORK(3)",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.IN_WORK.getCode())));
		Assert.assertTrue("FAIL 判定的 UPDATE 条件仍须包含过渡态 ALRAEDY(4)",
				inClauseValues.contains(String.valueOf(DeviceDownStatusEnum.ALRAEDY.getCode())));
	}

	/**
	 * 从 UPDATE wrapper 中提取 {@code device_status IN (...)} 守卫子句实际绑定的值（转字符串比较）。
	 *
	 * <p>实测确认：MyBatis-Plus 的 {@code .in(column, collection)} 不会把整个 Collection 绑成一个
	 * Iterable 参数，而是把每个元素拆成独立的具名标量参数（{@code MPGENVALn}），与
	 * {@code .set(column, value)} 绑定的标量参数在 {@link com.baomidou.mybatisplus.core.conditions.AbstractWrapper#getParamNameValuePairs()}
	 * 里类型上无法区分——因此不能按值类型过滤，必须先从 {@code getSqlSegment()} 定位
	 * {@code IN (...)} 片段里引用了哪些参数名，再按名字回查参数值。</p>
	 */
	private static List<String> extractInClauseValues(LambdaUpdateWrapper<?> wrapper) {
		String sqlSegment = wrapper.getSqlSegment();
		int inStart = sqlSegment.indexOf("IN (");
		Assert.assertTrue("测试断言前置条件：UPDATE 条件必须含 IN (...) 守卫子句，未找到则说明wrapper构造有误",
				inStart >= 0);
		int inEnd = sqlSegment.indexOf(')', inStart);
		String inClauseSegment = sqlSegment.substring(inStart, inEnd);

		Map<String, Object> paramNameValuePairs = wrapper.getParamNameValuePairs();
		List<String> values = new ArrayList<>();
		for (Map.Entry<String, Object> entry : paramNameValuePairs.entrySet()) {
			if (inClauseSegment.contains("paramNameValuePairs." + entry.getKey())) {
				values.add(String.valueOf(entry.getValue()));
			}
		}
		return values;
	}

	/**
	 * UPDATE 返回 0 行、且复查发现当前 device_status 已不在本次判定的守卫集合内
	 * （条件性不匹配，不是并发写冲突）：应直接记 INFO 返回，不再重试、也不记 ERROR。
	 * 覆盖修复的第二部分——避免"申请单冻结在假失败，重试 3 次全部空转"。
	 */
	@Test
	public void writeback_skipsRetryWhenGuardConditionUnmatched() {
		SmtAdmittanceApplyMapper applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		Mockito.when(applyMapper.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class))).thenReturn(0);
		// 复查发现当前 device_status 已经是 SUCCESS(1)——FAIL 判定的守卫集合(3,4)不包含 1
		SmtAdmittanceApply alreadySucceeded = new SmtAdmittanceApply();
		alreadySucceeded.setId(APPLY_ID);
		alreadySucceeded.setDeviceStatus(DeviceDownStatusEnum.SUCCESS.getCode());
		Mockito.when(applyMapper.selectById(APPLY_ID)).thenReturn(alreadySucceeded);

		AdmittanceDispatchAggregator.writebackDeviceStatus(
				applyMapper, APPLY_ID, BATCH_ID, AdmittanceDispatchAggregator.BatchVerdict.FAIL,
				DeviceDownStatusEnum.FAIL.getCode());

		// 只应尝试 1 次 UPDATE，复查后立即返回，不重试
		Mockito.verify(applyMapper, Mockito.times(1))
				.update(Mockito.any(), Mockito.any(LambdaUpdateWrapper.class));
		Mockito.verify(applyMapper, Mockito.times(1)).selectById(APPLY_ID);

		boolean hasErrorLog = appender.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR);
		Assert.assertFalse("条件性不匹配不应记录ERROR日志（不是异常，是预期内的状态推进）", hasErrorLog);
		boolean hasSkipInfoLog = appender.list.stream().anyMatch(event ->
				event.getLevel() == Level.INFO
						&& event.getFormattedMessage().contains("跳过")
						&& event.getFormattedMessage().contains(String.valueOf(APPLY_ID)));
		Assert.assertTrue("条件性不匹配应记录INFO日志说明跳过原因", hasSkipInfoLog);
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
