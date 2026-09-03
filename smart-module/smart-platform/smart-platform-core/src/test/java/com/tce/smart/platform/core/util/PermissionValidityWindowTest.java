package com.tce.smart.platform.core.util;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 手动下发权限有效期归一化规则测试。
 */
public class PermissionValidityWindowTest {

	/**
	 * 自定义日期应归一化为首日零点和末日最后一秒。
	 */
	@Test
	public void resolvesCustomDatesToInclusiveTaskWindow() {
		PermissionValidityWindow window = PermissionValidityWindow.resolve("2026-09-03", "2026-09-05");

		Assert.assertEquals(LocalDate.of(2026, 9, 3).atStartOfDay(ZoneId.systemDefault()).toEpochSecond(),
				window.getStartTime());
		Assert.assertEquals(LocalDate.of(2026, 9, 6).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1,
				window.getOverTime());
	}

	/**
	 * 未携带日期的旧请求应继续使用当天至 2030 年末的默认窗口。
	 */
	@Test
	public void defaultsMissingDatesForCompatibleRequests() {
		LocalDate beforeResolve = LocalDate.now();
		PermissionValidityWindow window = PermissionValidityWindow.resolve(null, null);
		LocalDate afterResolve = LocalDate.now();

		Assert.assertFalse(window.getStartDate().isBefore(beforeResolve));
		Assert.assertFalse(window.getStartDate().isAfter(afterResolve));
		Assert.assertEquals(LocalDate.of(2030, 12, 31), window.getEndDate());
	}

	/**
	 * 同一人员同一设备存在多个权限组时，最新授权应直接覆盖历史窗口。
	 */
	@Test
	public void usesLatestWindowForSameDevice() {
		Map<String, PermissionValidityWindow> windows = PermissionValidityWindow.resolveByDevice(
				Arrays.asList(staffAuth(100, "2026-09-01", "2026-09-30", "2026-09-01T08:00:00"),
						staffAuth(200, "2026-09-15", "2026-10-31", "2026-09-02T08:00:00")),
				Arrays.asList(deviceRelation(100, "device-1"), deviceRelation(200, "device-1")));

		Assert.assertEquals(LocalDate.of(2026, 9, 15), windows.get("device-1").getStartDate());
		Assert.assertEquals(LocalDate.of(2026, 10, 31), windows.get("device-1").getEndDate());
	}

	/**
	 * 同一设备存在日期断档时也应允许最新授权覆盖，避免人工操作被阻塞。
	 */
	@Test
	public void usesLatestWindowWhenSameDeviceHasDiscontinuousWindows() {
		Map<String, PermissionValidityWindow> windows = PermissionValidityWindow.resolveByDevice(
				Arrays.asList(staffAuth(100, "2026-09-01", "2026-09-10", "2026-09-01T08:00:00"),
						staffAuth(200, "2026-09-20", "2026-09-30", "2026-09-02T08:00:00")),
				Arrays.asList(deviceRelation(100, "device-1"), deviceRelation(200, "device-1")));
		Assert.assertEquals(LocalDate.of(2026, 9, 20), windows.get("device-1").getStartDate());
		Assert.assertEquals(LocalDate.of(2026, 9, 30), windows.get("device-1").getEndDate());
	}

	/**
	 * Oracle DATE 的创建时间精度为秒；同秒新增的关系用更大的主键判定为最后一次授权。
	 */
	@Test
	public void usesHigherRelationIdWhenLatestAuthorizationsShareCreateTime() {
		SmtStaffDeviceAuth oldAuth = staffAuth(100, "2026-09-01", "2026-09-10", "2026-09-01T08:00:00");
		oldAuth.setId(1);
		SmtStaffDeviceAuth latestAuth = staffAuth(200, "2026-09-20", "2026-09-30", "2026-09-01T08:00:00");
		latestAuth.setId(2);
		Map<String, PermissionValidityWindow> windows = PermissionValidityWindow.resolveByDevice(
				Arrays.asList(oldAuth, latestAuth),
				Arrays.asList(deviceRelation(100, "device-1"), deviceRelation(200, "device-1")));
		Assert.assertEquals(LocalDate.of(2026, 9, 20), windows.get("device-1").getStartDate());
	}

	/**
	 * 结束日期早于开始日期必须被服务端拒绝，避免任何后续写入。
	 */
	@Test(expected = SmartException.class)
	public void rejectsEndDateBeforeStartDate() {
		PermissionValidityWindow.resolve("2026-09-05", "2026-09-03");
	}

	private SmtStaffDeviceAuth staffAuth(Integer authId, String startDate, String endDate, String createTime) {
		SmtStaffDeviceAuth auth = new SmtStaffDeviceAuth();
		auth.setAuthId(authId);
		auth.setStartTime(dateAtStartOfDay(startDate));
		auth.setEndTime(dateAtStartOfDay(endDate));
		auth.setCreateTime(Date.from(java.time.LocalDateTime.parse(createTime).atZone(ZoneId.systemDefault()).toInstant()));
		return auth;
	}

	private SmtDeviceAuthorityRelation deviceRelation(Integer authId, String deviceId) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(authId);
		relation.setDeviceId(deviceId);
		return relation;
	}

	private Date dateAtStartOfDay(String value) {
		return Date.from(LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
}
