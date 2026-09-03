package com.tce.smart.platform.core.util;

import com.tce.smart.common.core.exception.SmartException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;

/**
 * 手动下发权限的有效期。
 *
 * <p>HTTP 请求以日期字符串表达边界；设备任务使用 Unix 秒级时间戳，
 * 员工权限关联记录保留日期边界供界面展示。本类集中处理两套表达之间的归一化，
 * 以保证两个手动下发入口具有相同的默认值和校验语义。</p>
 */
public final class PermissionValidityWindow {

	/** 默认结束日期。 */
	public static final LocalDate DEFAULT_END_DATE = LocalDate.of(2030, 12, 31);

	/** 严格的日期输入格式，拒绝 2026-02-30 这类被宽松解析的日期。 */
	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
			.appendPattern("uuuu-MM-dd")
			.toFormatter()
			.withResolverStyle(ResolverStyle.STRICT);

	private final LocalDate startDate;
	private final LocalDate endDate;

	private PermissionValidityWindow(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/**
	 * 解析外部请求的日期范围。
	 *
	 * @param startTime 起始日期（yyyy-MM-dd），为空时取当天
	 * @param endTime 结束日期（yyyy-MM-dd），为空时取 2030-12-31
	 * @return 可用于关联记录和设备任务的有效期
	 */
	public static PermissionValidityWindow resolve(String startTime, String endTime) {
		LocalDate startDate = parseOrDefault(startTime, LocalDate.now(), "开始日期");
		LocalDate endDate = parseOrDefault(endTime, DEFAULT_END_DATE, "结束日期");
		if (endDate.isBefore(startDate)) {
			throw new SmartException("权限结束日期不能早于开始日期");
		}
		return new PermissionValidityWindow(startDate, endDate);
	}

	/**
	 * 返回起始日零点对应的设备任务秒级时间戳。
	 */
	public long getStartTime() {
		return startDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
	}

	/**
	 * 返回结束日最后一秒对应的设备任务秒级时间戳。
	 */
	public long getOverTime() {
		return endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;
	}

	/**
	 * 返回用于关联记录展示的起始日期零点。
	 */
	public Date getStartDateTime() {
		return Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * 返回用于关联记录展示的结束日期最后一秒。
	 */
	public Date getEndDateTime() {
		return Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusSeconds(1).toInstant());
	}

	/**
	 * 返回起始日期，供业务校验和测试使用。
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * 返回结束日期，供业务校验和测试使用。
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * 按统一格式解析日期；缺省值保持旧调用兼容。
	 */
	private static LocalDate parseOrDefault(String value, LocalDate defaultValue, String fieldName) {
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return LocalDate.parse(value, DATE_FORMATTER);
		} catch (DateTimeParseException exception) {
			throw new SmartException(fieldName + "格式必须为yyyy-MM-dd");
		}
	}
}
