package com.tce.smart.platform.service.settlement.impl;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.mapper.SmtStaffStatementDetailMapper;
import com.tce.smart.platform.service.SmtDormitoryStaffHistoryService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 覆盖 getSDMeterreadWithDor（“按楼栋查询员工水电结算记录”，对应 /by-dor 导出接口）。
 * <p>
 * 生产事故复现链路：宿舍楼人数一多，原实现对结果集里的每一行都同步循环调用一次
 * getInRoomNum / getByBadge，实测 1280 行触发 1280 次额外查询，叠加到 45-50 秒，
 * 稳定超过前端 axios 30 秒超时导致“导出卡死无响应”。这里锁定“批量方法各调用一次”
 * 这一行为，防止再退化回按行循环查询。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtStaffStatementDetailServiceImplTest {

	@Before
	public void loginAsSmartUser() {
		SmartUser user = new SmartUser(1, 1, "tester", Arrays.asList(1), "N/A",
				true, true, true, true, Collections.emptyList());
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(user);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void getSDMeterreadWithDorBatchesInRoomNumAndNameLookupsInsteadOfPerRowQueries() throws Exception {
		SmtStaffStatementDetailMapper detailMapper = Mockito.mock(SmtStaffStatementDetailMapper.class);
		SmtDormitoryStaffHistoryService staffHistoryService = Mockito.mock(SmtDormitoryStaffHistoryService.class);
		SmtSdMeterreadService meterreadService = Mockito.mock(SmtSdMeterreadService.class);
		SmtStaffStatementDetailServiceImpl service = service(detailMapper, staffHistoryService);

		Date meterMonth = new SimpleDateFormat("yyyy-MM-dd").parse("2026-06-01");
		List<SmtStaffStatementDTO> rows = Arrays.asList(
				row("badge-1", "宿舍A", "101", null, meterMonth),
				row("badge-2", "宿舍A", "102", "李四", meterMonth),
				row("badge-3", "宿舍A", "103", null, meterMonth)
		);
		Mockito.when(detailMapper.getStaffSDStatementDetailWithDor(Mockito.anyList(), Mockito.eq(meterMonth), Mockito.anyList()))
				.thenReturn(rows);

		Map<String, Integer> inRoomNumMap = new HashMap<>();
		inRoomNumMap.put("badge-1", 1);
		inRoomNumMap.put("badge-2", 2);
		inRoomNumMap.put("badge-3", 1);
		Mockito.when(meterreadService.getInRoomNumBatch(Mockito.anyList(), Mockito.eq(meterMonth)))
				.thenReturn(inRoomNumMap);

		Map<String, String> nameMap = new HashMap<>();
		nameMap.put("badge-1", "张三");
		nameMap.put("badge-3", "王五");
		Mockito.when(staffHistoryService.getByBadgeBatch(Mockito.anyList())).thenReturn(nameMap);

		StaffStatementWithDorReqDTO query = StaffStatementWithDorReqDTO.builder()
				.dormitoryIds("5010481")
				.meterMonth(meterMonth)
				.build();

		List<SmtStaffStatementDTO> result = service.getSDMeterreadWithDor(query, meterreadService);

		// 核心回归点：批量方法各只调用一次，而不是按结果集行数循环调用
		Mockito.verify(meterreadService, Mockito.times(1)).getInRoomNumBatch(Mockito.anyList(), Mockito.eq(meterMonth));
		Mockito.verify(meterreadService, Mockito.never()).getInRoomNum(Mockito.anyString(), Mockito.any());
		Mockito.verify(staffHistoryService, Mockito.times(1)).getByBadgeBatch(Mockito.anyList());
		Mockito.verify(staffHistoryService, Mockito.never()).getByBadge(Mockito.anyString());

		Map<String, SmtStaffStatementDTO> byBadge = new HashMap<>();
		for (SmtStaffStatementDTO dto : result) {
			byBadge.put(dto.getBadge(), dto);
		}
		Assert.assertEquals("张三", byBadge.get("badge-1").getName());
		Assert.assertEquals(Integer.valueOf(1), byBadge.get("badge-1").getInRoomNum());
		Assert.assertEquals("李四", byBadge.get("badge-2").getName());
		Assert.assertEquals(Integer.valueOf(2), byBadge.get("badge-2").getInRoomNum());
		Assert.assertEquals("王五", byBadge.get("badge-3").getName());
	}

	@Test
	public void getSDMeterreadWithDorOnlyLooksUpNamesForBlankNameRows() throws Exception {
		SmtStaffStatementDetailMapper detailMapper = Mockito.mock(SmtStaffStatementDetailMapper.class);
		SmtDormitoryStaffHistoryService staffHistoryService = Mockito.mock(SmtDormitoryStaffHistoryService.class);
		SmtSdMeterreadService meterreadService = Mockito.mock(SmtSdMeterreadService.class);
		SmtStaffStatementDetailServiceImpl service = service(detailMapper, staffHistoryService);

		Date meterMonth = new SimpleDateFormat("yyyy-MM-dd").parse("2026-06-01");
		List<SmtStaffStatementDTO> rows = Collections.singletonList(row("badge-1", "宿舍A", "101", "已有姓名", meterMonth));
		Mockito.when(detailMapper.getStaffSDStatementDetailWithDor(Mockito.anyList(), Mockito.eq(meterMonth), Mockito.anyList()))
				.thenReturn(rows);
		Mockito.when(meterreadService.getInRoomNumBatch(Mockito.anyList(), Mockito.eq(meterMonth)))
				.thenReturn(Collections.singletonMap("badge-1", 1));

		StaffStatementWithDorReqDTO query = StaffStatementWithDorReqDTO.builder()
				.dormitoryIds("5010481")
				.meterMonth(meterMonth)
				.build();

		service.getSDMeterreadWithDor(query, meterreadService);

		// 已经有姓名的行不应该被塞进批量查姓名的入参里
		Mockito.verify(staffHistoryService, Mockito.times(1)).getByBadgeBatch(Collections.emptyList());
	}

	private SmtStaffStatementDTO row(String badge, String dormitoryName, String roomName, String name, Date meterMonth) {
		return SmtStaffStatementDTO.builder()
				.badge(badge)
				.dormitoryName(dormitoryName)
				.roomName(roomName)
				.name(name)
				.fee(BigDecimal.TEN)
				.inDays(10)
				.remarkDays(0)
				.meterMonth(meterMonth)
				.build();
	}

	private SmtStaffStatementDetailServiceImpl service(SmtStaffStatementDetailMapper detailMapper,
														SmtDormitoryStaffHistoryService staffHistoryService) throws Exception {
		SmtStaffStatementDetailServiceImpl service = new SmtStaffStatementDetailServiceImpl();
		setField(service, "smtStaffStatementDetailMapper", detailMapper);
		setField(service, "smtDormitoryStaffHistoryService", staffHistoryService);
		return service;
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
}
