package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.OaFinalStatusResolver;
import com.tce.smart.platform.service.oacallback.ProcessRecordWriter;
import com.tce.smart.platform.service.securityzone.impl.SmtSecurityAuthApplyServiceImpl;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Task 23 单测：updateOaStatusTask 保密门禁 OA 对账任务（spec §3.1.3/§3.1.4）。
 * 覆盖场景1（回调丢失，游标扫描 + query/resolve/claim）与场景2（审批已过但下发未执行）。
 */
@SuppressWarnings("unchecked")
public class SecurityAuthOaReconcileTest {

	/** 手动预热 MyBatis-Plus lambda 缓存，做法同 SmtSecurityAuthApplyClaimTest */
	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityAuthApply.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityTaskDetails.class);
	}

	private SmtSecurityAuthApplyMapper applyMapper;
	private SmtSecurityAuthApplyServiceImpl applyService;
	private IOAWorkflowService ioaWorkflowService;
	private OaFinalStatusResolver oaFinalStatusResolver;
	private ProcessRecordWriter processRecordWriter;
	private StringRedisTemplate stringRedisTemplate;
	private ValueOperations<String, String> valueOperations;

	@Before
	public void setUp() throws Exception {
		applyMapper = mock(SmtSecurityAuthApplyMapper.class);
		applyService = spy(new SmtSecurityAuthApplyServiceImpl());
		setField(applyService, "baseMapper", applyMapper);

		ioaWorkflowService = mock(IOAWorkflowService.class);
		setField(applyService, "ioaWorkflowService", ioaWorkflowService);

		oaFinalStatusResolver = mock(OaFinalStatusResolver.class);
		setField(applyService, "oaFinalStatusResolver", oaFinalStatusResolver);

		processRecordWriter = mock(ProcessRecordWriter.class);
		setField(applyService, "processRecordWriter", processRecordWriter);

		stringRedisTemplate = mock(StringRedisTemplate.class);
		valueOperations = mock(ValueOperations.class);
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		setField(applyService, "stringRedisTemplate", stringRedisTemplate);

		// 场景2 默认返回空列表，避免各用例互相干扰；具体用例按需覆盖
		when(applyMapper.selectPage(any(), any())).thenReturn(emptyPage());
	}

	private Page<SmtSecurityAuthApply> emptyPage() {
		Page<SmtSecurityAuthApply> page = new Page<>();
		page.setRecords(Collections.emptyList());
		return page;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}

	private SmtSecurityAuthApply pendingApply(long id, String processId) {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(id);
		apply.setProcessId(processId);
		apply.setApplyBadge("badge-" + id);
		apply.setOaStatus(ApproveListStateEnum.PENDING.getCode());
		apply.setCreateTime(LocalDateTime.now().minusHours(1));
		return apply;
	}

	/** 场景1 分页返回指定申请单，场景2 分页固定为空（除非用例内覆盖） */
	private void stubScene1Page(List<SmtSecurityAuthApply> records) {
		Page<SmtSecurityAuthApply> page = new Page<>();
		page.setRecords(records);
		// 场景1（游标扫描）走 selectPage，第一次调用命中场景1，之后场景2默认空
		when(applyMapper.selectPage(any(), any())).thenReturn(page, emptyPage());
	}

	// ========== 场景1：回调丢失 ==========

	@Test
	public void pendingApply_oaArchived_claimsAgreeTriggersDownDeviceAndWritesProcessRecord() {
		SmtSecurityAuthApply apply = pendingApply(1L, "P-1");
		stubScene1Page(Collections.singletonList(apply));

		WorkFlowLogDataDTO logData = new WorkFlowLogDataDTO();
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(logData));
		when(ioaWorkflowService.query("P-1")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.AGREE.getCode());
		// claim CAS 命中
		when(applyMapper.update(any(), any())).thenReturn(1);
		doReturn(true).when(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));

		applyService.updateOaStatusTask();

		verify(applyService).claimOaFinalStatus(eq(1L), eq(ApproveListStateEnum.AGREE.getCode()));
		verify(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));
		verify(processRecordWriter).write(eq("P-1"), any());
	}

	@Test
	public void pendingApply_oaRefused_claimsRefuseDoesNotTriggerDownDevice() {
		SmtSecurityAuthApply apply = pendingApply(2L, "P-2");
		stubScene1Page(Collections.singletonList(apply));

		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-2")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.REFUSE.getCode());
		when(applyMapper.update(any(), any())).thenReturn(1);

		applyService.updateOaStatusTask();

		verify(applyService).claimOaFinalStatus(eq(2L), eq(ApproveListStateEnum.REFUSE.getCode()));
		verify(applyService, never()).triggerDownDevice(any());
		verify(processRecordWriter, never()).write(anyString(), any());
	}

	@Test
	public void pendingApply_oaStillInProgress_resolveReturnsNull_doesNotClaim() {
		SmtSecurityAuthApply apply = pendingApply(3L, "P-3");
		stubScene1Page(Collections.singletonList(apply));

		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-3")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(null);

		applyService.updateOaStatusTask();

		verify(applyService, never()).claimOaFinalStatus(anyLong(), any());
		verify(applyMapper, never()).update(any(), any());
	}

	@Test
	public void queryThrows_skipsApplyButContinuesWithNextOneInBatch() {
		SmtSecurityAuthApply first = pendingApply(4L, "P-4");
		SmtSecurityAuthApply second = pendingApply(5L, "P-5");
		stubScene1Page(java.util.Arrays.asList(first, second));

		when(ioaWorkflowService.query("P-4")).thenThrow(new RuntimeException("OA接口超时"));
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-5")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.AGREE.getCode());
		when(applyMapper.update(any(), any())).thenReturn(1);
		doReturn(true).when(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));

		applyService.updateOaStatusTask();

		// 第一单查询异常被跳过，不应对其 claim
		verify(applyService, never()).claimOaFinalStatus(eq(4L), any());
		// 第二单应正常处理
		verify(applyService).claimOaFinalStatus(eq(5L), eq(ApproveListStateEnum.AGREE.getCode()));
		verify(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	@Test
	public void claimFails_concurrentCallbackWonRace_doesNotTriggerDownDevice() {
		SmtSecurityAuthApply apply = pendingApply(6L, "P-6");
		stubScene1Page(Collections.singletonList(apply));

		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-6")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.AGREE.getCode());
		// claim 失败：回调已先抢占
		when(applyMapper.update(any(), any())).thenReturn(0);

		applyService.updateOaStatusTask();

		verify(applyService, never()).triggerDownDevice(any());
	}

	// ========== 场景2：审批已过但下发未执行 ==========

	@Test
	public void scene2Apply_triggersDownDeviceDirectly() {
		// 场景1批为空，场景2批返回一条待下发的单
		when(applyMapper.selectPage(any(), any())).thenReturn(emptyPage());
		SmtSecurityAuthApply apply = pendingApply(7L, "P-7");
		apply.setOaStatus(ApproveListStateEnum.AGREE.getCode());
		Page<SmtSecurityAuthApply> scene2Page = new Page<>();
		scene2Page.setRecords(Collections.singletonList(apply));
		when(applyMapper.selectPage(any(), any())).thenReturn(emptyPage(), scene2Page);
		doReturn(true).when(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));

		applyService.updateOaStatusTask();

		verify(applyService).triggerDownDevice(apply);
	}

	// ========== 游标推进 / 归零 ==========

	@Test
	public void emptyBatchWithPositiveCursor_resetsCursorToZero() {
		when(valueOperations.get("oa:security:auth:cursor")).thenReturn("100");
		when(applyMapper.selectPage(any(), any())).thenReturn(emptyPage());

		applyService.updateOaStatusTask();

		verify(stringRedisTemplate.opsForValue()).set(eq("oa:security:auth:cursor"), eq("0"));
	}

	@Test
	public void nonEmptyBatch_advancesCursorToMaxId() {
		SmtSecurityAuthApply apply = pendingApply(8L, "P-8");
		stubScene1Page(Collections.singletonList(apply));
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-8")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(null);

		applyService.updateOaStatusTask();

		ArgumentCaptor<String> cursorCaptor = ArgumentCaptor.forClass(String.class);
		verify(valueOperations).set(eq("oa:security:auth:cursor"), cursorCaptor.capture());
		assertEquals("8", cursorCaptor.getValue());
	}

	// ========== 超24小时告警（不阻断其余行为） ==========

	@Test
	public void pendingOver24Hours_stillProcessedNormally() {
		SmtSecurityAuthApply apply = pendingApply(9L, "P-9");
		apply.setCreateTime(LocalDateTime.now().minusHours(30));
		stubScene1Page(Collections.singletonList(apply));

		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-9")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.AGREE.getCode());
		when(applyMapper.update(any(), any())).thenReturn(1);
		doReturn(true).when(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));

		applyService.updateOaStatusTask();

		// 超24小时仅打 warn 日志，不影响正常 claim + 下发流程
		verify(applyService).claimOaFinalStatus(eq(9L), eq(ApproveListStateEnum.AGREE.getCode()));
		verify(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}
}
