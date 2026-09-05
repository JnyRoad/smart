package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 保密区权限自动删除的天数阈值回归测试。
 */
public class SmtSecurityAuthDeleteServiceImplTest {

	/**
	 * 验证白名单开关关闭时，未达到配置删除天数的权限不能被误判为应删除。
	 *
	 * @throws Exception 反射调用私有的纯计算方法失败时抛出。
	 */
	@Test
	public void freeDay_usesDeleteDayInsteadOfWhiteListFlag() throws Exception {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setDeleteDay(30);
		config.setIsWhiteList(0);

		boolean shouldDelete = invokeFreeDay(config,
				DateUtil.parse("2026-09-01 00:00:00"),
				DateUtil.parse("2026-09-11 00:00:00"));

		assertFalse("未满 deleteDay 天的权限不应因白名单开关为 0 而删除", shouldDelete);
	}

	/**
	 * 验证间隔恰好等于删除天数时，权限仍处于允许保留的边界内。
	 *
	 * @throws Exception 反射调用私有的纯计算方法失败时抛出。
	 */
	@Test
	public void freeDay_atConfiguredDayBoundary_doesNotDelete() throws Exception {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setDeleteDay(1);
		config.setIsWhiteList(0);
		Date boundaryTime = DateUtil.parse("2026-09-01 00:00:00");

		boolean shouldDelete = invokeFreeDay(config, boundaryTime, boundaryTime);

		assertFalse("间隔等于 deleteDay 时不应删除", shouldDelete);
	}

	/**
	 * 验证超过管理员配置的删除天数后，权限会被判定为应删除。
	 *
	 * @throws Exception 反射调用私有的纯计算方法失败时抛出。
	 */
	@Test
	public void freeDay_afterConfiguredDayBoundary_deletes() throws Exception {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setDeleteDay(3);
		config.setIsWhiteList(0);

		boolean shouldDelete = invokeFreeDay(config,
				DateUtil.parse("2026-09-01 00:00:00"),
				DateUtil.parse("2026-09-11 00:00:00"));

		assertTrue("超过 deleteDay 天且无过滤项时应删除", shouldDelete);
	}

	/**
	 * 调用自动删除任务的纯天数判定逻辑，避免依赖数据库、远程服务或设备任务。
	 *
	 * @param config 自动删除配置，包含删除天数与过滤开关。
	 * @param startTime 最后一次进出或权限创建时间。
	 * @param endTime 本次任务的判定时间。
	 * @return 是否达到删除条件。
	 * @throws Exception 反射调用失败时抛出。
	 */
	private boolean invokeFreeDay(SmtSecurityAuthDelete config, Date startTime, Date endTime) throws Exception {
		Method freeDay = SmtSecurityAuthDeleteServiceImpl.class.getDeclaredMethod("freeDay",
				SmtSecurityAuthDelete.class, String.class, Date.class, Date.class);
		freeDay.setAccessible(true);
		return (Boolean) freeDay.invoke(new SmtSecurityAuthDeleteServiceImpl(), config, "test-badge", startTime, endTime);
	}
}
