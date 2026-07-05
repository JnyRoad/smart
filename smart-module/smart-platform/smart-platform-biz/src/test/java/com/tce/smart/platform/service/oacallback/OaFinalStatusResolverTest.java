package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.tool.constant.WorkFlowLogConstants;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * OA 终态解析单测（spec §3.1.2：排序、乱序、退回后重提、异常输入）
 */
public class OaFinalStatusResolverTest {

	private final OaFinalStatusResolver resolver = new OaFinalStatusResolver();

	private WorkFlowLogDataDTO rec(String nodeType, String date, String time) {
		WorkFlowLogDataDTO d = new WorkFlowLogDataDTO();
		d.setCURRENTNODETYPE(nodeType);
		d.setOPERATEDATE(date);
		d.setOPERATETIME(time);
		return d;
	}

	private WorkFlowLogDTO dto(WorkFlowLogDataDTO... records) {
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setType(WorkFlowLogConstants.SUCCESS);
		dto.setResultdata(Arrays.asList(records));
		return dto;
	}

	@Test
	public void archived_returnsAgree() {
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:46:09"), rec("3", "2026-07-02", "09:57:36"))));
	}

	@Test
	public void returned_returnsRefuse() {
		assertEquals(Integer.valueOf(2), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:46:09"), rec("0", "2026-07-02", "10:00:00"))));
	}

	@Test
	public void inProgress_returnsNull() {
		assertNull(resolver.resolve(dto(rec("1", "2026-07-02", "09:46:09"))));
	}

	@Test
	public void unordered_latestByTimeWins() {
		// 乱序：最新记录（归档）排在中间，仍应命中
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:46:09"),
				rec("3", "2026-07-02", "09:57:36"),
				rec("1", "2026-07-02", "09:50:00"))));
	}

	@Test
	public void returnedThenResubmitted_latestArchiveWins() {
		// 退回后再提交并归档 → 以最新记录为准
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("0", "2026-07-01", "10:00:00"), rec("3", "2026-07-02", "09:57:36"))));
	}

	@Test
	public void emptyData_returnsNull() {
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setType(WorkFlowLogConstants.SUCCESS);
		dto.setResultdata(Collections.emptyList());
		assertNull(resolver.resolve(dto));
	}

	@Test
	public void queryFailed_returnsNull() {
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setType(999);
		assertNull(resolver.resolve(dto));
		assertNull(resolver.resolve(null));
	}

	@Test
	public void sameTimestamp_stableOrderLastWins() {
		// 两条记录时间戳完全相同，稳定排序下保持原顺序后取最后一条
		// 情形 1：第一条审批中（"1"）、第二条归档（"3"），取第二条返回 1（AGREE）
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:57:36"),
				rec("3", "2026-07-02", "09:57:36"))));

		// 情形 2：反向验证 - 第一条归档（"3"）、第二条审批中（"1"），取第二条返回 null（非终态）
		// 这个案例验证 reduce((a,b)->b) 确实取的是列表中靠后那条
		assertNull(resolver.resolve(dto(
				rec("3", "2026-07-02", "09:57:36"),
				rec("1", "2026-07-02", "09:57:36"))));
	}
}
