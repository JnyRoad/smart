package com.tce.smart.platform.service.energy.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.platform.api.dto.resp.energy.ParkUtilityUsageMonthToDateRespDTO;
import com.tce.smart.platform.api.dto.resp.energy.UtilityUsageItemRespDTO;
import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterDayFact;
import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterScopeRule;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDay;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDayItem;
import com.tce.smart.platform.core.entity.energy.SmtEnergyProjectionQueue;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyMeterDayFactMapper;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyMeterScopeRuleMapper;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyParkDayItemMapper;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyParkDayMapper;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyParkDayLockMapper;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyMeterDayLockMapper;
import com.tce.smart.platform.core.mapper.energy.SmtEnergyProjectionQueueMapper;
import com.tce.smart.platform.service.energy.EnergyDailyUsageCalculator;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import com.tce.smart.platform.service.energy.EnergyScopeDecision;
import com.tce.smart.platform.service.energy.EnergyMonthToDateQuality;
import com.tce.smart.platform.service.energy.EnergyParkDayQuality;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 以原始抄表历史为来源生成可重算的园区日投影。 */
@Slf4j
@Service
@AllArgsConstructor
public class EnergyProjectionServiceImpl implements EnergyProjectionService {
	private static final String ELE = "ELE";
	private static final String WATER = "WATER";
	private static final String READY = "READY";
	private final SmtEnergyProjectionQueueMapper queueMapper;
	private final SmtEnergyMeterDayFactMapper factMapper;
	private final SmtEnergyMeterScopeRuleMapper ruleMapper;
	private final SmtEnergyParkDayItemMapper itemMapper;
	private final SmtEnergyParkDayMapper parkDayMapper;
	private final SmtEnergyParkDayLockMapper parkDayLockMapper;
	private final SmtEnergyMeterDayLockMapper meterDayLockMapper;
	private final PlatformTransactionManager transactionManager;

	@Value("${smart.energy.zone-id:Asia/Shanghai}")
	private String zoneId;
	@Value("${smart.energy.boundary-tolerance-minutes:120}")
	private long boundaryToleranceMinutes;
	@Value("${smart.energy.backfill.max-requests:1000}")
	private int backfillMaxRequests;
	@Value("${smart.energy.projection.max-retry-count:3}")
	private int maxRetryCount;
	@Value("${smart.energy.projection.retry-delay-minutes:5}")
	private long retryDelayMinutes;

	@Override
	public void requestProjection(String meterSource, Long meterId, LocalDate businessDate) {
		if ((!ELE.equals(meterSource) && !WATER.equals(meterSource)) || meterId == null || businessDate == null) {
			throw new IllegalArgumentException("能耗投影请求参数不合法");
		}
		LocalDateTime requestedAt = now();
		if (queueMapper.insertIfAbsent(IdWorker.getId(), meterSource, meterId, businessDate, requestedAt) == 0) queueMapper.requeueExisting(meterSource, meterId, businessDate, requestedAt);
	}

	@Override
	public void processPending() {
		LocalDateTime claimedAt = now();
		for (SmtEnergyProjectionQueue queue : queueMapper.selectCandidates(200, claimedAt)) {
			String leaseToken = UUID.randomUUID().toString();
			if (queueMapper.claim(queue.getId(), queue.getRequestCount(), claimedAt, claimedAt.plusMinutes(15), leaseToken) != 1) continue;
			try {
				if (!projectClaimedInNewTransaction(queue, leaseToken)) log.info("能耗投影租约已失效，跳过副作用, queueId={}", queue.getId());
			} catch (Exception ex) {
				log.error("能耗日投影失败, queueId={}", queue.getId(), ex);
				if (queueMapper.failOrRetry(queue.getId(), queue.getRequestCount(), leaseToken, now(), now().plusMinutes(retryDelayMinutes), maxRetryCount, truncate(ex.getMessage())) != 1) log.info("能耗投影失败结果已被新租约或新请求接管, queueId={}", queue.getId());
			}
		}
	}

	@Override
	public void reconcile(LocalDate businessDate) {
		if (businessDate == null) throw new IllegalArgumentException("业务日期不能为空");
		for (String source : new String[]{ELE, WATER}) {
			long afterId = 0L;
			List<Map<String, Object>> meters;
			do {
				meters = factMapper.selectActiveMeters(source, afterId, 200);
				for (Map<String, Object> meter : meters) {
					Long meterId = longValue(meter, "ID");
					try { inNewTransaction(source, meterId, businessDate); } catch (Exception ex) { log.error("能耗日回填失败, source={}, meterId={}, date={}", source, meterId, businessDate, ex); }
					afterId = meterId;
				}
			} while (meters.size() == 200);
		}
	}

	@Override
	public int backfillCurrentMonthToDate() {
		int accepted = 0; LocalDate today = LocalDate.now(zone()); LocalDate start = YearMonth.from(today).atDay(1);
		for (LocalDate date = start; !date.isAfter(today) && accepted < backfillMaxRequests; date = date.plusDays(1)) for (String source : new String[]{ELE, WATER}) {
			long afterId = 0L; List<Map<String, Object>> meters;
			do {
				meters = factMapper.selectActiveMeters(source, afterId, 200);
				for (Map<String, Object> meter : meters) { Long meterId = longValue(meter, "ID"); afterId = meterId; if (accepted < backfillMaxRequests && factMapper.existsFactOrActiveQueue(source, meterId, date) == 0) { requestProjection(source, meterId, date); accepted++; } }
			} while (meters.size() == 200 && accepted < backfillMaxRequests);
		}
		return accepted;
	}

	@Override
	public ParkUtilityUsageMonthToDateRespDTO getCurrentMonthToDate(Long parkId) {
		if (parkId == null) throw new IllegalArgumentException("园区不能为空");
		LocalDate today = LocalDate.now(zone());
		List<SmtEnergyParkDay> days = parkDayMapper.selectMonthToDate(parkId, YearMonth.from(today).atDay(1), today);
		ParkUtilityUsageMonthToDateRespDTO response = new ParkUtilityUsageMonthToDateRespDTO();
		response.setParkId(parkId);
		response.setBusinessMonth(YearMonth.from(today).atDay(1));
		response.setAsOf(now());
		response.setTimezone(zone().getId());
		response.setWater(toUsage(days, WATER, "m3"));
		response.setElectricity(toUsage(days, "ELECTRICITY", "kWh"));
		return response;
	}

	/** 每条投影在独立事务中执行，单表失败不会回滚同批其他表计。 */
	private void inNewTransaction(final String source, final Long meterId, final LocalDate date) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		template.execute(status -> { lockMeterDay(source, meterId, date); project(source, meterId, date); return null; });
	}

	/** 队列路径固定按“表计日锁→队列行→园区日锁”顺序执行并在同一事务完成 DONE。 */
	private boolean projectClaimedInNewTransaction(final SmtEnergyProjectionQueue queue, final String leaseToken) {
		TransactionTemplate template = new TransactionTemplate(transactionManager); template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		Boolean result = template.execute(status -> {
			lockMeterDay(queue.getMeterSource(), queue.getMeterId(), queue.getStatDate());
			if (queueMapper.verifyCurrentLeaseForUpdate(queue.getId(), queue.getRequestCount(), leaseToken) == null) return Boolean.FALSE;
			project(queue.getMeterSource(), queue.getMeterId(), queue.getStatDate());
			if (queueMapper.finish(queue.getId(), queue.getRequestCount(), "DONE", now(), null, leaseToken) != 1) throw new IllegalStateException("队列租约提交失败");
			return Boolean.TRUE;
		});
		return Boolean.TRUE.equals(result);
	}

	private void lockMeterDay(String source, Long meterId, LocalDate date) { meterDayLockMapper.ensureAnchor(source, meterId, date); meterDayLockMapper.lockForUpdate(source, meterId, date); }

	private void project(String source, Long meterId, LocalDate date) {
		Map<String, Object> meter = factMapper.selectActiveMeter(source, meterId);
		if (meter == null || meter.isEmpty()) throw new IllegalStateException("表计不存在，无法建立园区归属事实");
		Long parkId = longValue(meter, "PARK_ID");
		String resource = ELE.equals(source) ? "ELECTRICITY" : WATER;
		String unit = ELE.equals(source) ? "kWh" : "m3";
		BigDecimal multiplier = ELE.equals(source) ? decimal(meter, "MULTIPLIER") : BigDecimal.ONE;
		LocalDateTime startBoundary = date.atStartOfDay();
		LocalDateTime endBoundary = date.equals(LocalDate.now(zone())) ? now() : date.plusDays(1).atStartOfDay();
		Map<String, Object> start = factMapper.selectLatestReadingAtOrBefore(source, meterId, startBoundary);
		Map<String, Object> end = factMapper.selectLatestReadingAtOrBefore(source, meterId, endBoundary);
		SmtEnergyMeterDayFact fact = buildFact(source, meterId, parkId, resource, unit, date, multiplier, startBoundary, endBoundary, start, end);
		if (intValue(meter, "IS_DELETE") != 0) { fact.setQualityCode("METER_UNAVAILABLE"); fact.setQualityDetail("表计已逻辑删除"); fact.setUsageValue(null); fact.setRawDelta(null); }
		factMapper.mergeFact(fact);
		fact = factMapper.selectFact(source, meterId, date);
		parkId = fact.getParkId();
		SmtEnergyMeterScopeRule rule = ruleMapper.selectEffectiveRule(source, meterId, date);
		EnergyScopeDecision decision = EnergyScopeDecision.decide(rule, ruleMapper.selectEffectiveRulesForPark(parkId, source, date));
		boolean included = decision.isIncluded();
		itemMapper.mergeItem(SmtEnergyParkDayItem.builder().id(IdWorker.getId()).parkId(parkId).statDate(date).resourceType(resource).unit(unit)
				.meterSource(source).meterId(meterId).meterDayFactId(fact.getId()).usageValue(included && READY.equals(fact.getQualityCode()) ? fact.getUsageValue() : null)
				.ruleId(rule == null ? null : rule.getId()).ruleVersion(rule == null ? null : rule.getRuleVersion()).ruleDecision(decision.getDecision())
				.ruleReason(decision.getReason()).calculatedAt(now()).build());
		// 锁锚点和汇总刷新都处于当前 REQUIRES_NEW 事务，避免并发小计覆盖。
		parkDayLockMapper.ensureAnchor(parkId, date, resource, unit);
		parkDayLockMapper.lockForUpdate(parkId, date, resource, unit);
		refreshParkDay(parkId, date, resource, unit, source);
	}

	private SmtEnergyMeterDayFact buildFact(String source, Long meterId, Long parkId, String resource, String unit, LocalDate date, BigDecimal multiplier, LocalDateTime startBoundary, LocalDateTime endBoundary, Map<String, Object> start, Map<String, Object> end) {
		BigDecimal startReading = decimal(start, "CURRENT_READING");
		BigDecimal endReading = decimal(end, "CURRENT_READING");
		LocalDateTime startTime = time(start, "COLLECT_TIME");
		LocalDateTime endTime = time(end, "COLLECT_TIME");
		String quality = READY;
		String detail = null;
		if (start == null || end == null || !within(startTime, startBoundary) || !within(endTime, endBoundary)) { quality = "MISSING_BOUNDARY"; detail = "缺少业务日边界容差内的有效读数"; }
		else if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) { quality = "INVALID_MULTIPLIER"; detail = "电表倍率为空或不大于零"; }
		else if (startReading == null || endReading == null) { quality = "INVALID_READING"; detail = "读数不是有效数值"; }
		else if (endReading.compareTo(startReading) < 0) { quality = "NEGATIVE_DELTA"; detail = "日末读数小于日初读数"; }
		BigDecimal rawDelta = READY.equals(quality) ? endReading.subtract(startReading) : null;
		BigDecimal usage = READY.equals(quality) ? EnergyDailyUsageCalculator.calculate(startReading, endReading, multiplier) : null;
		return SmtEnergyMeterDayFact.builder().id(IdWorker.getId()).parkId(parkId).meterSource(source).meterId(meterId).resourceType(resource).unit(unit).statDate(date)
				.dayStartHistoryId(longValue(start, "ID")).dayStartTime(startTime).dayStartReading(startReading).dayEndHistoryId(longValue(end, "ID")).dayEndTime(endTime).dayEndReading(endReading)
				.multiplierSnapshot(multiplier == null ? BigDecimal.ZERO : multiplier).rawDelta(rawDelta).usageValue(usage).qualityCode(quality).qualityDetail(detail).sourceHash(hash(start, end, multiplier)).calculatedAt(now()).build();
	}

	private void refreshParkDay(Long parkId, LocalDate date, String resource, String unit, String meterSource) {
		Map<String, Object> summary = parkDayMapper.summarizeItems(parkId, date, resource, unit);
		int included = intValue(summary, "INCLUDED_COUNT"); int excluded = intValue(summary, "EXCLUDED_COUNT"); int invalid = intValue(summary, "INVALID_COUNT"); int missing = intValue(summary, "MISSING_COUNT"); int projected = intValue(summary, "PROJECTED_COUNT"); int expected = factMapper.countActiveMeters(meterSource, parkId); int unprojected = Math.max(expected - projected, 0);
		String status = EnergyParkDayQuality.status(included, excluded, invalid, missing, expected, projected);
		BigDecimal usage = "READY".equals(status) || "PARTIAL".equals(status) ? decimal(summary, "USAGE_VALUE") : null;
		parkDayMapper.mergeParkDay(SmtEnergyParkDay.builder().id(IdWorker.getId()).parkId(parkId).statDate(date).resourceType(resource).unit(unit).usageValue(usage).meterCount(included).qualitySummary(status + "|included=" + included + ",excluded=" + excluded + ",invalid=" + invalid + ",missing=" + missing + ",expected=" + expected + ",projected=" + projected + ",unprojected=" + unprojected).calculatedAt(now()).build());
	}

	private UtilityUsageItemRespDTO toUsage(List<SmtEnergyParkDay> days, String resource, String unit) {
		UtilityUsageItemRespDTO dto = new UtilityUsageItemRespDTO(); dto.setUnit(unit); LocalDateTime last = null; List<LocalDate> dates = new ArrayList<>(); List<String> statuses = new ArrayList<>(); List<SmtEnergyParkDay> matchingDays = new ArrayList<>();
		for (SmtEnergyParkDay day : days == null ? Collections.<SmtEnergyParkDay>emptyList() : days) if (resource.equals(day.getResourceType()) && unit.equals(day.getUnit())) {
			String dayStatus = quality(day.getQualitySummary()); dates.add(day.getStatDate()); statuses.add(dayStatus);
			matchingDays.add(day);
			if (last == null || (day.getCalculatedAt() != null && day.getCalculatedAt().isAfter(last))) last = day.getCalculatedAt();
		}
		String status = EnergyMonthToDateQuality.evaluate(YearMonth.from(LocalDate.now(zone())).atDay(1), LocalDate.now(zone()), dates, statuses);
		dto.setUsageValue(sumReadyDays(matchingDays)); dto.setQualityStatus(status); dto.setLastCalculatedAt(last); return dto;
	}
	/** 月累计只取完整日，避免用局部数据冒充已就绪口径。 */
	public static BigDecimal sumReadyDays(List<SmtEnergyParkDay> days) {
		BigDecimal sum = null;
		for (SmtEnergyParkDay day : days) if ("READY".equals(day.getQualitySummary() == null ? "NO_DATA" : day.getQualitySummary().split("\\|")[0]) && day.getUsageValue() != null) sum = sum == null ? day.getUsageValue() : sum.add(day.getUsageValue());
		return sum;
	}
	private ZoneId zone() { return ZoneId.of(zoneId); }
	private LocalDateTime now() { return LocalDateTime.now(zone()); }
	private boolean within(LocalDateTime reading, LocalDateTime boundary) { return reading != null && !reading.isAfter(boundary) && Duration.between(reading, boundary).toMinutes() <= boundaryToleranceMinutes; }
	private String quality(String summary) { return summary == null ? "NO_DATA" : summary.split("\\|")[0]; }
	private static String truncate(String value) { return value == null ? "未知错误" : value.substring(0, Math.min(value.length(), 1000)); }
	private static Long longValue(Map<String,Object> map, String key) { Object v = value(map,key); return v instanceof Number ? ((Number)v).longValue() : v == null ? null : Long.valueOf(v.toString()); }
	private static int intValue(Map<String,Object> map, String key) { Long v=longValue(map,key); return v==null?0:v.intValue(); }
	private static BigDecimal decimal(Map<String,Object> map, String key) { Object v=value(map,key); if(v instanceof BigDecimal)return (BigDecimal)v; try{return v==null?null:new BigDecimal(v.toString());}catch(NumberFormatException ex){return null;} }
	private static Object value(Map<String,Object> map,String key){ if(map==null)return null; Object value=map.get(key); return value!=null?value:map.get(key.toLowerCase(Locale.ROOT)); }
	private static LocalDateTime time(Map<String,Object> map,String key){ Object v=value(map,key); if(v instanceof Timestamp)return ((Timestamp)v).toLocalDateTime(); if(v instanceof LocalDateTime)return (LocalDateTime)v; return null; }
	private static String hash(Object... values) { try { MessageDigest digest=MessageDigest.getInstance("SHA-256"); for(Object value:values) digest.update(String.valueOf(value).getBytes(StandardCharsets.UTF_8)); StringBuilder out=new StringBuilder(); for(byte b:digest.digest())out.append(String.format("%02x",b)); return out.toString(); } catch(Exception ex) { throw new IllegalStateException(ex); } }
}
