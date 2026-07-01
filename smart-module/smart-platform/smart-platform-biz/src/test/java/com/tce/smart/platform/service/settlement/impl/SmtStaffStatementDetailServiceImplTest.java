package com.tce.smart.platform.service.settlement.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.SmtStaffStatementReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.mapper.SmtStaffStatementDetailMapper;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryService;
import com.tce.smart.platform.service.SmtDormitoryStaffHistoryService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	/**
	 * 覆盖 getStaffSDStatementDetailNew（/platform/dormitory/staff/statementdetail/new-page 分页接口）。
	 * <p>
	 * 原实现在 forEach 里对每一行都同步调用一次 smtDormitoryService.getById(dormitoryId)，
	 * 属于按行循环查询数据库的 N+1 反模式。这里锁定“listByIds 只调用一次、getById 一次都不调用”
	 * 这一行为，防止再退化回按行循环查询。
	 */
	@Test
	public void getStaffSDStatementDetailNewBatchesDormitoryLookupInsteadOfPerRowQueries() throws Exception {
		SmtStaffStatementDetailMapper detailMapper = Mockito.mock(SmtStaffStatementDetailMapper.class);
		SmtDormitoryRoomService dormitoryRoomService = Mockito.mock(SmtDormitoryRoomService.class);
		SmtDormitoryService dormitoryService = Mockito.mock(SmtDormitoryService.class);
		SmtSdMeterreadService meterreadService = Mockito.mock(SmtSdMeterreadService.class);
		SmtStaffStatementDetailServiceImpl service = serviceForStatementDetailNew(detailMapper, dormitoryRoomService, dormitoryService);

		Date meterMonth = new SimpleDateFormat("yyyy-MM-dd").parse("2026-06-01");

		// 房间101/102同属宿舍楼10，房间201属于宿舍楼20 —— 同时覆盖同楼栋去重与跨楼栋两种情形
		List<SmtDormitoryRoom> rooms = Arrays.asList(
				room(101, 10),
				room(102, 10),
				room(201, 20)
		);
		Mockito.when(dormitoryRoomService.list(Mockito.any())).thenReturn(rooms);

		List<SmtStaffStatementDTO> rows = Arrays.asList(
				statementRow("badge-1", 101, 9999, meterMonth),
				statementRow("badge-2", 102, 9999, meterMonth),
				statementRow("badge-3", 201, 9999, meterMonth)
		);
		Page<SmtStaffStatementDTO> mapperResult = new Page<>(1, 10);
		mapperResult.setRecords(rows);
		mapperResult.setTotal(rows.size());
		Mockito.when(detailMapper.getStaffSDStatementDetailNew(Mockito.any(), Mockito.any(), Mockito.anyList(), Mockito.anyList()))
				.thenReturn(mapperResult);

		Mockito.when(meterreadService.getInRoomNumBatch(Mockito.anyList(), Mockito.eq(meterMonth)))
				.thenReturn(new HashMap<>());

		Mockito.when(dormitoryService.listByIds(Mockito.anyCollection())).thenReturn(Arrays.asList(
				dormitory(10, "宿舍A"),
				dormitory(20, "宿舍B")
		));

		SmtStaffStatementReqDTO query = SmtStaffStatementReqDTO.builder()
				.dormitoryId(1)
				.parkId(9999)
				.meterMonth(meterMonth)
				.build();

		Page page = new Page(1, 10);
		IPage<SmtStaffStatementDTO> result = service.getStaffSDStatementDetailNew(page, query, meterreadService);

		// 核心回归点：批量方法只调用一次，而不是按结果集行数循环调用 getById
		ArgumentCaptor<Collection> idsCaptor = ArgumentCaptor.forClass(Collection.class);
		Mockito.verify(dormitoryService, Mockito.times(1)).listByIds(idsCaptor.capture());
		Mockito.verify(dormitoryService, Mockito.never()).getById(Mockito.any());
		Set<Object> capturedIds = new HashSet<>(idsCaptor.getValue());
		Assert.assertEquals(new HashSet<>(Arrays.asList(10, 20)), capturedIds);

		Map<Integer, SmtStaffStatementDTO> byRoomId = new HashMap<>();
		for (SmtStaffStatementDTO dto : result.getRecords()) {
			byRoomId.put(dto.getRoomId(), dto);
		}
		Assert.assertEquals("宿舍A", byRoomId.get(101).getDormitoryName());
		Assert.assertEquals("宿舍A", byRoomId.get(102).getDormitoryName());
		Assert.assertEquals("宿舍B", byRoomId.get(201).getDormitoryName());
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

	private SmtStaffStatementDTO statementRow(String badge, Integer roomId, Integer parkId, Date meterMonth) {
		return SmtStaffStatementDTO.builder()
				.badge(badge)
				.roomId(roomId)
				.parkId(parkId)
				.fee(BigDecimal.TEN)
				.inDays(10)
				.remarkDays(0)
				.meterMonth(meterMonth)
				.build();
	}

	private SmtDormitoryRoom room(Integer id, Integer dormitoryId) {
		return SmtDormitoryRoom.builder()
				.id(id)
				.dormitoryId(dormitoryId)
				.roomName(id)
				.build();
	}

	private SmtDormitory dormitory(Integer id, String dormitoryName) {
		SmtDormitory dormitory = new SmtDormitory();
		dormitory.setId(id);
		dormitory.setDormitoryName(dormitoryName);
		return dormitory;
	}

	private SmtStaffStatementDetailServiceImpl serviceForStatementDetailNew(SmtStaffStatementDetailMapper detailMapper,
																			SmtDormitoryRoomService dormitoryRoomService,
																			SmtDormitoryService dormitoryService) throws Exception {
		SmtStaffStatementDetailServiceImpl service = new SmtStaffStatementDetailServiceImpl();
		setField(service, "smtStaffStatementDetailMapper", detailMapper);
		setField(service, "smtDormitoryRoomService", dormitoryRoomService);
		setField(service, "smtDormitoryService", dormitoryService);
		setField(service, "xcParkId", 0);
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
