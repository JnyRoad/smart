package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.service.SmtProcessRecordService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 过程记录归一化写入组件单测 */
public class ProcessRecordWriterTest {

	private SmtProcessRecordService recordService;
	private ProcessRecordWriter writer;
	private OaFlowRecordSupport support;

	@Before
	public void setUp() {
		recordService = mock(SmtProcessRecordService.class);
		writer = new ProcessRecordWriter(recordService);
		support = new OaFlowRecordSupport(writer);
	}

	private WorkFlowRecordAO record(String logtype) {
		WorkFlowRecordAO ao = new WorkFlowRecordAO();
		ao.setLogtype(logtype);
		ao.setWorkcode("8033365");
		ao.setLastname("测试");
		ao.setNodename("01 提交申请");
		ao.setOperatedate("2026-07-02");
		ao.setOperatetime("09:46:09");
		ao.setRemark("<p>同意</p>");
		return ao;
	}

	@Test
	public void write_newRecord_savedWithHtmlStripped() {
		// 无重复记录 → 新建，remark 去 HTML
		when(recordService.getOne(any())).thenReturn(null);
		writer.write("28753680", ProcessRecordItem.fromCallback(record("2")));
		ArgumentCaptor<SmtProcessRecord> captor = ArgumentCaptor.forClass(SmtProcessRecord.class);
		verify(recordService).save(captor.capture());
		assertEquals("同意", captor.getValue().getRemark());
		assertEquals("28753680", captor.getValue().getProcessId());
	}

	@Test
	public void write_interventionRecord_skipped() {
		// 流程干预节点（logtype=i）不写入，等价原 processRecord 首行判断
		writer.write("28753680", ProcessRecordItem.fromCallback(record("i")));
		verify(recordService, never()).save(any());
	}

	@Test
	public void processAndDetectReturn_noReturn_flagTrue() {
		boolean flag = support.processAndDetectReturn("28753680",
				Arrays.asList(record("2"), record("0")));
		assertTrue(flag);
	}

	@Test
	public void processAndDetectReturn_hasReturn_flagFalse() {
		// 任一节点 logtype=3（退回）→ flag=false，且后续节点仍写记录
		boolean flag = support.processAndDetectReturn("28753680",
				Arrays.asList(record("2"), record("3"), record("0")));
		assertFalse(flag);
	}

	@Test
	public void fromOaLog_mapsAllUppercaseGetters() {
		// 验证全大写字段 DTO 映射正确（OA 查询接口返回的 WorkFlowLogDataDTO）
		WorkFlowLogDataDTO dto = new WorkFlowLogDataDTO();
		dto.setWORKCODE("8033365");
		dto.setLASTNAME("测试");
		dto.setNODENAME("01 提交申请");
		dto.setLOGTYPE("2");
		dto.setOPERATEDATE("2026-07-02");
		dto.setOPERATETIME("09:46:09");
		dto.setREMARK("<p>同意</p>");

		ProcessRecordItem item = ProcessRecordItem.fromOaLog(dto);

		assertEquals("8033365", item.getWorkcode());
		assertEquals("测试", item.getLastname());
		assertEquals("01 提交申请", item.getNodename());
		assertEquals("2", item.getLogtype());
		assertEquals("2026-07-02", item.getOperatedate());
		assertEquals("09:46:09", item.getOperatetime());
		assertEquals("<p>同意</p>", item.getRemark());
	}

	@Test
	public void processAndDetectReturn_hasReturn_stillWritesAllRecords() {
		// 验证回退节点（logtype=3）检测后返回 false，但全部 3 条记录仍逐一写入（无短路）
		when(recordService.getOne(any())).thenReturn(null);

		boolean flag = support.processAndDetectReturn("28753680",
				Arrays.asList(record("2"), record("3"), record("0")));

		assertFalse(flag);
		// recordService.getOne() 应被调用 3 次（每条记录写前先查重）
		verify(recordService, times(3)).getOne(any());
	}
}
