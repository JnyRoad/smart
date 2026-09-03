package com.tce.smart.platform.core.util;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		return create(startDate, endDate);
	}

	/**
	 * 按设备选择同一人员最近一次授权的有效期。
	 *
	 * <p>设备任务只能承载一个起止区间。当多个权限组映射到同一设备时，采用最近一次
	 * 授权关系的起止日期，直接覆盖此前任务窗口；不合并历史窗口，也不因日期断档拒绝
	 * 人工操作。关联创建时间相同则以关联主键判定先后；本次待保存关系优先于历史关系。</p>
	 *
	 * @param staffDeviceAuths 员工当前及本次待保存的权限关联
	 * @param authorityRelations 权限组到设备的映射
	 * @return 设备编号到最新授权有效期的映射
	 */
	public static Map<String, PermissionValidityWindow> resolveByDevice(
			List<SmtStaffDeviceAuth> staffDeviceAuths,
			List<SmtDeviceAuthorityRelation> authorityRelations) {
		if (staffDeviceAuths == null || staffDeviceAuths.isEmpty()
				|| authorityRelations == null || authorityRelations.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Integer, List<SmtStaffDeviceAuth>> authsByAuthority = new LinkedHashMap<>();
		for (SmtStaffDeviceAuth staffDeviceAuth : staffDeviceAuths) {
			if (staffDeviceAuth == null || staffDeviceAuth.getAuthId() == null) {
				continue;
			}
			authsByAuthority.computeIfAbsent(staffDeviceAuth.getAuthId(), ignored -> new ArrayList<>())
					.add(staffDeviceAuth);
		}

		Map<String, SmtStaffDeviceAuth> latestAuthByDevice = new LinkedHashMap<>();
		for (SmtDeviceAuthorityRelation authorityRelation : authorityRelations) {
			if (authorityRelation == null || authorityRelation.getAuthorityId() == null
					|| authorityRelation.getDeviceId() == null || authorityRelation.getDeviceId().trim().isEmpty()) {
				continue;
			}
			List<SmtStaffDeviceAuth> matchingAuths = authsByAuthority.get(authorityRelation.getAuthorityId());
			if (matchingAuths != null) {
				for (SmtStaffDeviceAuth matchingAuth : matchingAuths) {
					SmtStaffDeviceAuth existingAuth = latestAuthByDevice.get(authorityRelation.getDeviceId());
					if (existingAuth == null || isLatest(matchingAuth, existingAuth)) {
						latestAuthByDevice.put(authorityRelation.getDeviceId(), matchingAuth);
					}
				}
			}
		}

		Map<String, PermissionValidityWindow> result = new LinkedHashMap<>();
		for (Map.Entry<String, SmtStaffDeviceAuth> entry : latestAuthByDevice.entrySet()) {
			result.put(entry.getKey(), fromRelation(entry.getValue()));
		}
		return result;
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

	/**
	 * 将关联记录中的日期转换为窗口；历史空值按原有缺省规则解释。
	 */
	private static PermissionValidityWindow fromRelation(SmtStaffDeviceAuth staffDeviceAuth) {
		LocalDate startDate = staffDeviceAuth.getStartTime() == null
				? LocalDate.now() : staffDeviceAuth.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = staffDeviceAuth.getEndTime() == null
				? DEFAULT_END_DATE : staffDeviceAuth.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return create(startDate, endDate);
	}

	/**
	 * 判断候选关联是否比当前关联更新。创建时间相同时按主键选择较大值；
	 * 尚未落库的本次关系没有主键，优先于已落库关系。两者均无主键时按传入顺序取后者。
	 */
	private static boolean isLatest(SmtStaffDeviceAuth candidate, SmtStaffDeviceAuth current) {
		Date candidateCreateTime = candidate.getCreateTime();
		Date currentCreateTime = current.getCreateTime();
		if (candidateCreateTime == null || currentCreateTime == null) {
			return candidateCreateTime != null || currentCreateTime == null;
		}
		int createTimeComparison = candidateCreateTime.compareTo(currentCreateTime);
		if (createTimeComparison != 0) {
			return createTimeComparison > 0;
		}
		Integer candidateId = candidate.getId();
		Integer currentId = current.getId();
		if (candidateId == null && currentId != null) {
			return true;
		}
		if (candidateId != null && currentId == null) {
			return false;
		}
		if (candidateId == null) {
			return true;
		}
		return candidateId >= currentId;
	}

	/**
	 * 创建并校验日期窗口。
	 */
	private static PermissionValidityWindow create(LocalDate startDate, LocalDate endDate) {
		if (endDate.isBefore(startDate)) {
			throw new SmartException("权限结束日期不能早于开始日期");
		}
		return new PermissionValidityWindow(startDate, endDate);
	}
}
