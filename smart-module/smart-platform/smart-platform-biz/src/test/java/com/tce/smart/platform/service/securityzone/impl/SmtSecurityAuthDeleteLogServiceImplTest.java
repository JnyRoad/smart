package com.tce.smart.platform.service.securityzone.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteLogPageQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogTaskRespDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteLogPageDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteLogTaskDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteTaskRef;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteLog;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteTask;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteLogMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteTaskMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 保密区权限自动删除报表服务的业务边界测试。
 *
 * <p>这些测试只验证报表服务自身的状态、授权参数和导出边界，不连接真实数据库或设备。</p>
 */
public class SmtSecurityAuthDeleteLogServiceImplTest {

	private SmtSecurityAuthDeleteLogMapper logMapper;
	private SmtSecurityAuthDeleteTaskMapper taskMapper;
	private SmtSecurityAuthDeleteLogServiceImpl service;

	@Before
	public void setUp() {
		logMapper = mock(SmtSecurityAuthDeleteLogMapper.class);
		taskMapper = mock(SmtSecurityAuthDeleteTaskMapper.class);
		service = new SmtSecurityAuthDeleteLogServiceImpl(logMapper, taskMapper);
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	/** 验证空园区范围拒绝查询，避免令牌无数据范围时读到全部记录。 */
	@Test(expected = IllegalArgumentException.class)
	public void validateQuery_emptyParkScope_isRejected() {
		SmtSecurityAuthDeleteLogServiceImpl.validateQuery(new Page<>(1, 20),
				new SecurityAuthDeleteLogPageQueryReqDTO(), Collections.emptyList());
	}

	/** 验证日期反向、页大小越界和非法结果码均被拒绝。 */
	@Test
	public void validateQuery_invalidInput_isRejected() {
		SecurityAuthDeleteLogPageQueryReqDTO query = new SecurityAuthDeleteLogPageQueryReqDTO();
		query.setStartTime(LocalDateTime.of(2026, 9, 2, 0, 0));
		query.setEndTime(LocalDateTime.of(2026, 9, 1, 0, 0));
		assertEquals("开始时间不能晚于结束时间", rejectionMessage(query));

		query.setStartTime(null);
		query.setEndTime(null);
		query.setResult("NOT_A_RESULT");
		assertEquals("结果代码不合法", rejectionMessage(query));

		assertEquals("分页大小必须在1到100之间", rejectionMessage(new Page<>(1, 101), new SecurityAuthDeleteLogPageQueryReqDTO()));
	}

	/** 验证导出值以单引号隔离表格公式前缀，避免CSV打开时执行公式。 */
	@Test
	public void csvValue_formulaPrefix_isEscaped() {
		assertEquals("'=SUM(A1:A2)", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("=SUM(A1:A2)"));
		assertEquals("'+123", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("+123"));
		assertEquals("'\t=SUM(A1:A2)", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("\t=SUM(A1:A2)"));
		assertEquals("'\r=SUM(A1:A2)", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("\r=SUM(A1:A2)"));
		assertEquals("'\n=SUM(A1:A2)", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("\n=SUM(A1:A2)"));
		assertEquals("'\u0000=SUM(A1:A2)", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("\u0000=SUM(A1:A2)"));
		assertEquals("' =SUM(A1:A2)", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue(" =SUM(A1:A2)"));
		assertEquals("普通文本", SmtSecurityAuthDeleteLogServiceImpl.escapeCsvValue("普通文本"));
	}

	/** 验证导出超过一万条时明确失败，不能静默截断。 */
	@Test(expected = IllegalArgumentException.class)
	public void validateExportCount_overLimit_isRejected() {
		SmtSecurityAuthDeleteLogServiceImpl.validateExportCount(10001);
	}

	/** 验证记录服务在同一调用事务内写入标准任务和 ISC 任务两种来源。 */
	@Test
	public void record_writesNormalAndIscTaskRefs() {
		SmtSecurityAuthDeleteLog log = processingLog();
		doAnswer(invocation -> {
			invocation.<SmtSecurityAuthDeleteLog>getArgument(0).setId(101L);
			return 1;
		}).when(logMapper).insert(any(SmtSecurityAuthDeleteLog.class));
		List<SmtSecurityAuthDeleteTask> savedTasks = new ArrayList<>();
		doAnswer(invocation -> {
			savedTasks.add(invocation.<SmtSecurityAuthDeleteTask>getArgument(0));
			return 1;
		}).when(taskMapper).insert(any(SmtSecurityAuthDeleteTask.class));

		service.record(log, Arrays.asList(taskRef("NORMAL", "12"), taskRef("ISC", "9007199254740991")));

		assertEquals(101L, log.getId().longValue());
		assertEquals(2, savedTasks.size());
		assertEquals("NORMAL", savedTasks.get(0).getTaskSource());
		assertEquals("12", savedTasks.get(0).getTaskId());
		assertEquals("ISC", savedTasks.get(1).getTaskSource());
		assertEquals("9007199254740991", savedTasks.get(1).getTaskId());
	}

	/** 验证任一任务关联写入失败都会抛错，交由调用方事务回滚。 */
	@Test
	public void record_taskInsertFailure_throws() {
		SmtSecurityAuthDeleteLog log = processingLog();
		doAnswer(invocation -> {
			invocation.<SmtSecurityAuthDeleteLog>getArgument(0).setId(102L);
			return 1;
		}).when(logMapper).insert(any(SmtSecurityAuthDeleteLog.class));
		when(taskMapper.insert(any(SmtSecurityAuthDeleteTask.class))).thenReturn(0);

		try {
			service.record(log, Collections.singletonList(taskRef("NORMAL", "13")));
			fail("任务关联写入失败必须抛错");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("任务关联"));
		}
	}

	/** 验证分页查询实际从登录令牌取园区范围，并把结束秒转换为排他上界。 */
	@Test
	public void page_usesTokenParkScopeAndInclusiveSecondBoundary() {
		setUserParks(Arrays.asList(10, 11));
		when(logMapper.selectPageWithTaskSummary(any(Page.class), any(SecurityAuthDeleteLogPageQueryReqDTO.class), anyList()))
				.thenReturn(new Page<SecurityAuthDeleteLogPageDTO>(1, 20));
		SecurityAuthDeleteLogPageQueryReqDTO query = new SecurityAuthDeleteLogPageQueryReqDTO();
		query.setEndTime(LocalDateTime.of(2026, 9, 4, 23, 59, 59));

		service.page(new Page<>(1, 20), query);

		ArgumentCaptor<List> parks = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<SecurityAuthDeleteLogPageQueryReqDTO> queryCaptor =
				ArgumentCaptor.forClass(SecurityAuthDeleteLogPageQueryReqDTO.class);
		verify(logMapper).selectPageWithTaskSummary(any(Page.class), queryCaptor.capture(), parks.capture());
		assertEquals(Arrays.asList(10, 11), parks.getValue());
		assertEquals(LocalDateTime.of(2026, 9, 5, 0, 0), queryCaptor.getValue().getEndTime());
	}

	/** 验证页面保留生产写入的中文触发原因原文，同时保留结果代码供前端筛选和标签渲染。 */
	@Test
	public void page_preservesProductionChineseTriggerReason() {
		setUserParks(Collections.singletonList(10));
		SecurityAuthDeleteLogPageDTO row = new SecurityAuthDeleteLogPageDTO();
		row.setTriggerReason("达到自动删除条件");
		row.setResult("SUCCESS");
		Page<SecurityAuthDeleteLogPageDTO> sourcePage = new Page<>(1, 20);
		sourcePage.setRecords(Collections.singletonList(row));
		when(logMapper.selectPageWithTaskSummary(any(Page.class), any(SecurityAuthDeleteLogPageQueryReqDTO.class), anyList()))
				.thenReturn(sourcePage);

		SecurityAuthDeleteLogRespDTO response = service.page(new Page<>(1, 20),
				new SecurityAuthDeleteLogPageQueryReqDTO()).getRecords().get(0);

		assertEquals("达到自动删除条件", response.getTriggerReason());
		assertEquals("SUCCESS", response.getResult());
	}

	/** 验证登录令牌没有园区范围时，分页服务直接拒绝且不访问 Mapper。 */
	@Test
	public void page_emptyTokenParkScope_isRejectedBeforeMapper() {
		setUserParks(Collections.emptyList());
		try {
			service.page(new Page<>(1, 20), new SecurityAuthDeleteLogPageQueryReqDTO());
			fail("空园区范围必须拒绝");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().contains("园区数据范围"));
		}
		verify(logMapper, never()).selectPageWithTaskSummary(any(Page.class), any(), anyList());
	}

	/** 验证导出查询把令牌园区范围传入 Mapper，并在输出前拒绝超过一万条。 */
	@Test
	public void export_overLimit_usesTokenParkScopeAndRejectsBeforeResponse() {
		setUserParks(Collections.singletonList(10));
		Page<SecurityAuthDeleteLogPageDTO> overLimit = new Page<>(1, 10001);
		overLimit.setTotal(10001);
		when(logMapper.selectPageWithTaskSummary(any(Page.class), any(SecurityAuthDeleteLogPageQueryReqDTO.class), anyList()))
				.thenReturn(overLimit);

		try {
			service.export(new SecurityAuthDeleteLogPageQueryReqDTO(), mock(HttpServletResponse.class));
			fail("超过导出上限必须拒绝");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().contains("10000"));
		}
		ArgumentCaptor<List> parks = ArgumentCaptor.forClass(List.class);
		verify(logMapper).selectPageWithTaskSummary(any(Page.class), any(), parks.capture());
		assertEquals(Collections.singletonList(10), parks.getValue());
	}

	/** 验证合法导出不受内部10001探测页影响，并写出BOM、中文表头和中文业务文案。 */
	@Test
	public void export_validFilter_writesBomAndChineseLabels() throws Exception {
		setUserParks(Collections.singletonList(10));
		SecurityAuthDeleteLogPageDTO row = new SecurityAuthDeleteLogPageDTO();
		row.setId(9007199254740991L);
		row.setParkId(10);
		row.setTriggerReason("达到自动删除条件");
		row.setResult("SUCCESS");
		row.setStaffName("\t=SUM(A1:A2)");
		row.setDepartment("\r=CMD(2)");
		row.setAuthName("\n@CMD(3)");
		row.setRemark("\u0000=CMD(4)");
		row.setTaskCount(1);
		row.setSuccessCount(1);
		Page<SecurityAuthDeleteLogPageDTO> sourcePage = new Page<>(1, 10001);
		sourcePage.setTotal(1);
		sourcePage.setRecords(Collections.singletonList(row));
		when(logMapper.selectPageWithTaskSummary(any(Page.class), any(SecurityAuthDeleteLogPageQueryReqDTO.class), anyList()))
				.thenReturn(sourcePage);
		MockHttpServletResponse response = new MockHttpServletResponse();

		service.export(new SecurityAuthDeleteLogPageQueryReqDTO(), response);

		byte[] body = response.getContentAsByteArray();
		assertTrue(body.length > 3);
		assertEquals((byte) 0xEF, body[0]);
		assertEquals((byte) 0xBB, body[1]);
		assertEquals((byte) 0xBF, body[2]);
		String csv = new String(body, java.nio.charset.StandardCharsets.UTF_8);
		assertTrue(csv.contains("触发原因"));
		assertTrue(csv.contains("达到自动删除条件"));
		assertTrue(csv.contains("任务记录成功"));
		assertTrue(containsBytes(body, "'\t=SUM(A1:A2)"));
		assertTrue(containsBytes(body, "'\r=CMD(2)"));
		assertTrue(containsBytes(body, "'\n@CMD(3)"));
		assertTrue(containsBytes(body, "'\u0000=CMD(4)"));
	}

	/** 验证任务详情先用令牌园区范围授权主记录，再返回两表统一的任务 ID 文本。 */
	@Test
	 public void tasks_checksAuthorizedLogWithTokenParkScope() {
		setUserParks(Collections.singletonList(10));
		SmtSecurityAuthDeleteLog authorizedLog = new SmtSecurityAuthDeleteLog();
		authorizedLog.setId(55L);
		when(logMapper.selectAuthorizedLog(eq(55L), anyList())).thenReturn(authorizedLog);
		SecurityAuthDeleteLogTaskDTO task = new SecurityAuthDeleteLogTaskDTO();
		task.setTaskSource("ISC");
		task.setTaskId("9007199254740991");
		task.setStatus(1);
		when(logMapper.selectTasks(eq(55L), anyList())).thenReturn(Collections.singletonList(task));

		List<SecurityAuthDeleteLogTaskRespDTO> result = service.tasks("55");

		assertEquals(1, result.size());
		assertEquals("9007199254740991", result.get(0).getTaskId());
		ArgumentCaptor<List> parks = ArgumentCaptor.forClass(List.class);
		verify(logMapper).selectAuthorizedLog(eq(55L), parks.capture());
		assertEquals(Collections.singletonList(10), parks.getValue());
	}

	/** 验证未授权主记录在读取任务明细前即拒绝，不向详情 Mapper 发起查询。 */
	@Test
	public void tasks_unauthorizedLog_doesNotReadTaskDetails() {
		setUserParks(Collections.singletonList(10));
		when(logMapper.selectAuthorizedLog(eq(55L), anyList())).thenReturn(null);

		try {
			service.tasks("55");
			fail("未授权记录必须拒绝");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage().contains("无权访问"));
		}
		verify(logMapper, never()).selectTasks(anyLong(), anyList());
	}

	/** 验证 XML 在空园区范围下生成恒假条件，并直接按 NUMBER 主键关联任务表。 */
	@Test
	public void mapperBoundSql_failsClosedAndUsesNativeTaskIdJoin() throws Exception {
		MybatisConfiguration configuration = new MybatisConfiguration();
		String resource = "mapper/SmtSecurityAuthDeleteLogMapper.xml";
		try (InputStream input = Resources.getResourceAsStream(resource)) {
			new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
		}
		MappedStatement pageStatement = configuration.getMappedStatement(
				"com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteLogMapper.selectPageWithTaskSummary");
		Map<String, Object> emptyParams = new HashMap<>();
		emptyParams.put("query", new SecurityAuthDeleteLogPageQueryReqDTO());
		emptyParams.put("parkIds", Collections.emptyList());
		String emptySql = normalizeSql(pageStatement.getBoundSql(emptyParams).getSql());
		assertTrue(emptySql.contains("1 = 0"));

		Map<String, Object> scopedParams = new HashMap<>();
		scopedParams.put("query", new SecurityAuthDeleteLogPageQueryReqDTO());
		scopedParams.put("parkIds", Collections.singletonList(10));
		String scopedSql = normalizeSql(pageStatement.getBoundSql(scopedParams).getSql());
		assertTrue(scopedSql.contains("N.ID = T.TASK_ID"));
		assertTrue(scopedSql.contains("I.ID = T.TASK_ID"));
		assertTrue(scopedSql.contains("IN (2, 4, 5)"));
		assertTrue(scopedSql.contains("IN (0, 3, 6)"));
		assertTrue(scopedSql.contains("= 1 THEN 1 ELSE 0 END"));

		Map<String, Object> detailParams = new HashMap<>();
		detailParams.put("logId", 55L);
		detailParams.put("parkIds", Collections.emptyList());
		for (String statementId : Arrays.asList("selectTasks", "selectAuthorizedLog")) {
			MappedStatement detailStatement = configuration.getMappedStatement(
					"com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteLogMapper." + statementId);
			String detailSql = normalizeSql(detailStatement.getBoundSql(detailParams).getSql());
			assertTrue(statementId + " 空园区范围必须恒假", detailSql.contains("1 = 0"));
		}
	}

	/** 构造正式处理中主记录。 */
	private SmtSecurityAuthDeleteLog processingLog() {
		SmtSecurityAuthDeleteLog log = new SmtSecurityAuthDeleteLog();
		log.setParkId(10);
		log.setExecTime(LocalDateTime.of(2026, 9, 4, 12, 0));
		log.setResult("PROCESSING");
		return log;
	}

	/** 构造跨设备任务表的任务引用。 */
	private SecurityAuthDeleteTaskRef taskRef(String source, String taskId) {
		SecurityAuthDeleteTaskRef ref = new SecurityAuthDeleteTaskRef();
		ref.setTaskSource(source);
		ref.setTaskId(taskId);
		ref.setDeviceCode("D-1");
		ref.setAction(1);
		return ref;
	}

	/** 在测试安全上下文中放入指定园区范围。 */
	private void setUserParks(List<Integer> parkIds) {
		SmartUser user = new SmartUser(1, 1, "report-test", parkIds, "",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "credentials", user.getAuthorities()));
	}

	/** 折叠 SQL 空白，便于验证结构性条件而不依赖 XML 缩进。 */
	private String normalizeSql(String sql) {
		return sql.replaceAll("\\s+", " ").trim();
	}

	/** 验证导出响应的原始字节包含已加单引号的危险前缀。 */
	private boolean containsBytes(byte[] body, String expected) {
		byte[] needle = expected.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		for (int start = 0; start <= body.length - needle.length; start++) {
			boolean matched = true;
			for (int offset = 0; offset < needle.length; offset++) {
				if (body[start + offset] != needle[offset]) {
					matched = false;
					break;
				}
			}
			if (matched) {
				return true;
			}
		}
		return false;
	}

	/** 将查询拒绝转为稳定断言文本，避免测试依赖异常实现类型。 */
	private String rejectionMessage(SecurityAuthDeleteLogPageQueryReqDTO query) {
		return rejectionMessage(new Page<>(1, 20), query);
	}

	/** 执行查询参数校验并返回异常信息。 */
	private String rejectionMessage(Page<?> page, SecurityAuthDeleteLogPageQueryReqDTO query) {
		try {
			SmtSecurityAuthDeleteLogServiceImpl.validateQuery(page, query, Collections.singletonList(1));
			return "未拒绝";
		} catch (IllegalArgumentException ex) {
			return ex.getMessage();
		}
	}
}
