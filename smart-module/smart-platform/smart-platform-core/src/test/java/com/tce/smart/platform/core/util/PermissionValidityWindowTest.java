package com.tce.smart.platform.core.util;

import com.tce.smart.common.core.exception.SmartException;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;

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
		PermissionValidityWindow window = PermissionValidityWindow.resolve(null, null);

		Assert.assertEquals(LocalDate.now(), window.getStartDate());
		Assert.assertEquals(LocalDate.of(2030, 12, 31), window.getEndDate());
	}

	/**
	 * 结束日期早于开始日期必须被服务端拒绝，避免任何后续写入。
	 */
	@Test(expected = SmartException.class)
	public void rejectsEndDateBeforeStartDate() {
		PermissionValidityWindow.resolve("2026-09-05", "2026-09-03");
	}
}
