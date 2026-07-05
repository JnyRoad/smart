package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.exception.SmartException;
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

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Task 25 单测：手动下发 downDevice(Long applyId) 重写（spec §3.4，修 D5）。
 * 覆盖：申请单不存在、processId 缺失、oa_status 三态（PENDING 实时查 OA / AGREE 直接下发 / REFUSE 直接拒绝）
 * 以及 PENDING 分支下 OA 查询结果的四种终态（归档 / 退回 / 审批中 / 查询异常）。
 */
@SuppressWarnings("unchecked")
public class SecurityAuthManualDownTest {

	/** 手动预热 MyBatis-Plus lambda 缓存，做法同 SmtSecurityAuthApplyClaimTest/SecurityAuthOaReconcileTest */
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

	private SmtSecurityAuthApply apply(long id, String processId, Integer oaStatus) {
		SmtSecurityAuthApply authApply = new SmtSecurityAuthApply();
		authApply.setId(id);
		authApply.setProcessId(processId);
		authApply.setApplyBadge("badge-" + id);
		authApply.setOaStatus(oaStatus);
		return authApply;
	}

	// ========== 分支1：申请单不存在 ==========

	@Test
	public void applyNotFound_throwsSmartException() {
		doReturn(null).when(applyService).getById(999L);

		try {
			applyService.downDevice(999L);
			fail("应抛出SmartException");
		} catch (SmartException e) {
			assertEquals("申请单不存在", e.getMessage());
		}
		// 记录不存在，不应触发任何下发相关调用
		verify(applyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	// ========== 分支2：processId 为空 ==========

	@Test
	public void processIdBlank_throwsSmartException() {
		SmtSecurityAuthApply authApply = apply(1L, "", ApproveListStateEnum.PENDING.getCode());
		doReturn(authApply).when(applyService).getById(1L);

		try {
			applyService.downDevice(1L);
			fail("应抛出SmartException");
		} catch (SmartException e) {
			assertEquals("申请单缺少OA流程编号，无法下发", e.getMessage());
		}
		verify(applyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	// ========== 分支3：oa_status=0（PENDING）且 OA 归档（AGREE）==========

	@Test
	public void pending_oaArchived_claimsAndTriggersDownDeviceReturningItsResult() {
		SmtSecurityAuthApply authApply = apply(2L, "P-2", ApproveListStateEnum.PENDING.getCode());
		doReturn(authApply).when(applyService).getById(2L);

		WorkFlowLogDataDTO logData = new WorkFlowLogDataDTO();
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(logData));
		when(ioaWorkflowService.query("P-2")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.AGREE.getCode());
		// claim 抢占命中
		doReturn(true).when(applyService).claimOaFinalStatus(2L, ApproveListStateEnum.AGREE.getCode());
		// 下发本身返回 true，downDevice 应原样透传该结果
		doReturn(true).when(applyService).triggerDownDevice(any(SmtSecurityAuthApply.class));

		Boolean result = applyService.downDevice(2L);

		assertTrue(result);
		verify(applyService, times(1)).claimOaFinalStatus(2L, ApproveListStateEnum.AGREE.getCode());
		verify(processRecordWriter).write(eq("P-2"), any());
		verify(applyService, times(1)).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	// ========== 分支4：oa_status=0（PENDING）且 OA 退回（REFUSE）==========

	@Test
	public void pending_oaRefused_claimsRefuseAndThrows() {
		SmtSecurityAuthApply authApply = apply(3L, "P-3", ApproveListStateEnum.PENDING.getCode());
		doReturn(authApply).when(applyService).getById(3L);

		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-3")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(ApproveListStateEnum.REFUSE.getCode());
		doReturn(true).when(applyService).claimOaFinalStatus(3L, ApproveListStateEnum.REFUSE.getCode());

		try {
			applyService.downDevice(3L);
			fail("应抛出SmartException");
		} catch (SmartException e) {
			assertEquals("该申请已被OA退回，禁止下发", e.getMessage());
		}
		verify(applyService, times(1)).claimOaFinalStatus(3L, ApproveListStateEnum.REFUSE.getCode());
		verify(applyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	// ========== 分支5：oa_status=0（PENDING）且审批中（resolve 返回 null）==========

	@Test
	public void pending_oaStillInProgress_throwsWithoutClaim() {
		SmtSecurityAuthApply authApply = apply(4L, "P-4", ApproveListStateEnum.PENDING.getCode());
		doReturn(authApply).when(applyService).getById(4L);

		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setResultdata(Collections.singletonList(new WorkFlowLogDataDTO()));
		when(ioaWorkflowService.query("P-4")).thenReturn(dto);
		when(oaFinalStatusResolver.resolve(dto)).thenReturn(null);

		try {
			applyService.downDevice(4L);
			fail("应抛出SmartException");
		} catch (SmartException e) {
			assertEquals("OA审批未完成，禁止下发", e.getMessage());
		}
		verify(applyService, never()).claimOaFinalStatus(anyLong(), any());
		verify(applyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	// ========== 分支6：oa_status=0（PENDING）且 OA 查询异常 ==========

	@Test
	public void pending_oaQueryThrows_throwsSmartException() {
		SmtSecurityAuthApply authApply = apply(5L, "P-5", ApproveListStateEnum.PENDING.getCode());
		doReturn(authApply).when(applyService).getById(5L);

		when(ioaWorkflowService.query("P-5")).thenThrow(new RuntimeException("OA接口超时"));

		try {
			applyService.downDevice(5L);
			fail("应抛出SmartException");
		} catch (SmartException e) {
			assertEquals("OA状态查询失败，请稍后重试", e.getMessage());
		}
		verify(applyService, never()).claimOaFinalStatus(anyLong(), any());
		verify(applyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	// ========== 分支7：oa_status=1（AGREE）直接下发 ==========

	@Test
	public void oaStatusAgree_directlyTriggersDownDeviceWithoutQuery() {
		SmtSecurityAuthApply authApply = apply(6L, "P-6", ApproveListStateEnum.AGREE.getCode());
		doReturn(authApply).when(applyService).getById(6L);
		doReturn(true).when(applyService).triggerDownDevice(authApply);

		Boolean result = applyService.downDevice(6L);

		assertTrue(result);
		verify(ioaWorkflowService, never()).query(any());
		verify(applyService, never()).claimOaFinalStatus(anyLong(), any());
		verify(applyService, times(1)).triggerDownDevice(authApply);
	}

	// ========== 分支8：oa_status=2（REFUSE）直接异常 ==========

	@Test
	public void oaStatusRefuse_throwsWithoutQueryOrTrigger() {
		SmtSecurityAuthApply authApply = apply(7L, "P-7", ApproveListStateEnum.REFUSE.getCode());
		doReturn(authApply).when(applyService).getById(7L);

		try {
			applyService.downDevice(7L);
			fail("应抛出SmartException");
		} catch (SmartException e) {
			assertEquals("该申请已被OA退回，禁止下发", e.getMessage());
		}
		verify(ioaWorkflowService, never()).query(any());
		verify(applyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}
}
