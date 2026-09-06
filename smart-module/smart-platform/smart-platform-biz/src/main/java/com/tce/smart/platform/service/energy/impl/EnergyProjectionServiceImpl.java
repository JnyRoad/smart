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
import com.tce.smart.platform.service.energy.EnergyBackfillCursor;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import com.tce.smart.platform.service.energy.EnergyScopeDecision;
import com.tce.smart.platform.service.energy.EnergyMonthToDateQuality;
import com.tce.smart.platform.service.energy.EnergyParkDayQuality;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
import java.util.Objects;

/** 以原始抄表历史为来源生成可重算的园区日投影。 */
@Slf4j
@Service
public class EnergyProjectionServiceImpl implements EnergyProjectionService {
	private static final String ELE = "ELE";
	private static final String WATER = "WATER";
	private static final String READY = "READY";
	private static final String BACKFILL_CURSOR = "smart:energy:backfill:cursor:v1";
	/** 比较并推进断点，旧实例不得覆盖另一个批次已经提交的进度。 */
	private static final DefaultRedisScript<Long> SAVE_CURSOR = new DefaultRedisScript<>(
			"if (redis.call('get',KEYS[1]) or '') == ARGV[1] then redis.call('set',KEYS[1],ARGV[2]); return 1 else return 0 end", Long.class);
	private final SmtEnergyProjectionQueueMapper queueMapper;
	private final SmtEnergyMeterDayFactMapper factMapper;
	private final SmtEnergyMeterScopeRuleMapper ruleMapper;
	private final SmtEnergyParkDayItemMapper itemMapper;
	private final SmtEnergyParkDayMapper parkDayMapper;
	private final SmtEnergyParkDayLockMapper parkDayLockMapper;
	private final SmtEnergyMeterDayLockMapper meterDayLockMapper;
	private final PlatformTransactionManager transactionManager;
	private final StringRedisTemplate redisTemplate;

	public EnergyProjectionServiceImpl(SmtEnergyProjectionQueueMapper queueMapper, SmtEnergyMeterDayFactMapper factMapper,
			SmtEnergyMeterScopeRuleMapper ruleMapper, SmtEnergyParkDayItemMapper itemMapper, SmtEnergyParkDayMapper parkDayMapper,
			SmtEnergyParkDayLockMapper parkDayLockMapper, SmtEnergyMeterDayLockMapper meterDayLockMapper,
			PlatformTransactionManager transactionManager) {
		this.queueMapper = queueMapper;
		this.factMapper = factMapper;
		this.ruleMapper = ruleMapper;
		this.itemMapper = itemMapper;
		this.parkDayMapper = parkDayMapper;
		this.parkDayLockMapper = parkDayLockMapper;
		this.meterDayLockMapper = meterDayLockMapper;
		this.transactionManager = transactionManager;
	}

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

	/** 采集事件可增加请求版本，让正在执行的旧租约失效；参数非法时拒绝。 */
	@Override
	public void requestProjection(String meterSource, Long meterId, LocalDate businessDate) {
		if ((!ELE.equals(meterSource) && !WATER.equals(meterSource)) || meterId == null || businessDate == null) {
			throw new IllegalArgumentException("能耗投影请求参数不合法");
		}
		LocalDateTime requestedAt = now();
		if (queueMapper.insertIfAbsent(IdWorker.getId(), meterSource, meterId, businessDate, requestedAt) == 0) queueMapper.requeueExisting(meterSource, meterId, businessDate, requestedAt);
	}

	/** 近两日与历史各保留一半配额，空闲配额借给另一方；失败持久化后向调用方报告。 */
	@Override
	public void processPending() {
		LocalDateTime claimedAt = now();
		int failures = 0;
		for (SmtEnergyProjectionQueue queue : fairCandidates(claimedAt)) {
			String leaseToken = UUID.randomUUID().toString();
			if (queueMapper.claim(queue.getId(), queue.getRequestCount(), claimedAt, claimedAt.plusMinutes(15), leaseToken) != 1) continue;
			try {
				if (!projectClaimedInNewTransaction(queue, leaseToken)) log.info("能耗投影租约已失效，跳过副作用, queueId={}", queue.getId());
			} catch (Exception ex) {
				failures++;
				log.error("能耗日投影失败, queueId={}", queue.getId(), ex);
				if (queueMapper.failOrRetry(queue.getId(), queue.getRequestCount(), leaseToken, now(), now().plusMinutes(retryDelayMinutes), maxRetryCount, truncate(ex.getMessage())) != 1) log.info("能耗投影失败结果已被新租约或新请求接管, queueId={}", queue.getId());
			}
		}
		if (failures > 0) throw new IllegalStateException("能耗投影批次失败数=" + failures + "，失败项已按队列策略持久化重试");
	}

	/** 保留每日全表回算语义，但先持久入队，计算由短批消费者执行；入队失败不伪报成功。 */
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
					enqueueIfIdle(source, meterId, businessDate);
					afterId = meterId;
				}
			} while (meters.size() == 200);
		}
	}

	/** 按扫描数限制单批预算并逐项保存 Redis 断点，完成前不从月初重新扫描。 */
	@Override
	public int backfillCurrentMonthToDate() {
		if (backfillMaxRequests <= 0) throw new IllegalStateException("补齐单批扫描预算必须大于零");
		LocalDate today = LocalDate.now(zone());
		String saved = redisTemplate.opsForValue().get(BACKFILL_CURSOR);
		EnergyBackfillCursor cursor = EnergyBackfillCursor.restore(saved, today);
		int scanned = 0, accepted = 0, failures = 0;
		// 先检查并入队，再提交游标；进程中断时最多重复检查，活跃任务不会被覆盖。
		while (!cursor.date.isAfter(cursor.through) && scanned < backfillMaxRequests) {
			List<Map<String, Object>> meters = factMapper.selectActiveMeters(cursor.source, cursor.afterId,
					Math.min(200, backfillMaxRequests - scanned));
			if (meters.isEmpty()) { cursor.nextSource(); saved = saveCursor(saved, cursor); continue; }
			for (Map<String, Object> meter : meters) {
				Long meterId = longValue(meter, "ID");
				boolean needsProjection;
				try { needsProjection = needsReprojection(cursor.source, meterId, cursor.date); }
				catch (Exception ex) {
					// 检查单表异常时仍进入原队列，由其有限重试和 FAILED 状态承接，不阻塞后续日期。
					needsProjection = true; failures++; cursor.failed++;
					log.error("能耗补齐检查失败，转队列重试, source={}, meterId={}, date={}", cursor.source, meterId, cursor.date, ex);
				}
				if (needsProjection && enqueueIfIdle(cursor.source, meterId, cursor.date)) { accepted++; cursor.accepted++; }
				cursor.afterId = meterId; cursor.scanned++; scanned++;
				saved = saveCursor(saved, cursor);
			}
		}
		if (cursor.date.isAfter(cursor.through)) { cursor.completedOn = today; saveCursor(saved, cursor); }
		log.info("能耗补齐进度, month={}, date={}, source={}, afterId={}, scanned={}, accepted={}, failed={}, complete={}",
				cursor.month, cursor.date, cursor.source, cursor.afterId, cursor.scanned, cursor.accepted, cursor.failed, cursor.completedOn);
		if (failures > 0) throw new IllegalStateException("能耗补齐检查失败数=" + failures + "，已入队重试并保存扫描进度");
		return accepted;
	}

	/** 用数据库条件更新保护活跃请求，返回本次是否新增或重新启动了终态任务。 */
	private boolean enqueueIfIdle(String source, Long meterId, LocalDate date) {
		LocalDateTime requestedAt = now();
		return queueMapper.insertIfAbsent(IdWorker.getId(), source, meterId, date, requestedAt) == 1
				|| queueMapper.requeueIdle(source, meterId, date, requestedAt) == 1;
	}

	/** 使用乐观比较写入断点；并发批次或 Redis 故障会明确失败，禁止倒退覆盖。 */
	private String saveCursor(String saved, EnergyBackfillCursor cursor) {
		String encoded = cursor.encode();
		Long result = redisTemplate.execute(SAVE_CURSOR, Collections.singletonList(BACKFILL_CURSOR), saved == null ? "" : saved, encoded);
		if (!Long.valueOf(1L).equals(result)) throw new IllegalStateException("能耗补齐进度已被其他批次推进，请重试");
		return encoded;
	}

	/** 按当前业务边界重新检查正常事实；范围决策与明细沿用事实首次保存的园区快照，不随表计当前园区迁移。 */
	private boolean needsReprojection(String source, Long meterId, LocalDate date) {
		if (queueMapper.countActiveRequest(source, meterId, date) > 0) return false;
		SmtEnergyMeterDayFact fact = factMapper.selectFact(source, meterId, date);
		if (fact == null || !READY.equals(fact.getQualityCode())) return true;
		Map<String, Object> meter = factMapper.selectActiveMeter(source, meterId);
		if (meter == null || intValue(meter, "IS_DELETE") != 0) return true;
		BigDecimal multiplier = ELE.equals(source) ? decimal(meter, "MULTIPLIER") : BigDecimal.ONE;
		LocalDateTime startBoundary = date.atStartOfDay();
		LocalDateTime endBoundary = dayEndBoundary(date);
		Map<String, Object> start = factMapper.selectLatestReadingAtOrBefore(source, meterId, startBoundary);
		Map<String, Object> end = factMapper.selectLatestReadingAtOrBefore(source, meterId, endBoundary);
		// 当日实时快照跨日或容差收紧后，即使读数没有变化，也必须重新计算质量。
		if (!hasValidBoundaries(startBoundary, endBoundary, start, end)) return true;
		// 逐字段比较避免旧 SOURCE_HASH 使用 HashMap.toString 造成顺序差异或数字精度误判。
		if (!sameDecimal(multiplier, fact.getMultiplierSnapshot())
				|| !sameReading(start, fact.getDayStartHistoryId(), fact.getDayStartTime(), fact.getDayStartReading())
				|| !sameReading(end, fact.getDayEndHistoryId(), fact.getDayEndTime(), fact.getDayEndReading())) return true;
		SmtEnergyMeterScopeRule rule = ruleMapper.selectEffectiveRule(source, meterId, date);
		EnergyScopeDecision decision = EnergyScopeDecision.decide(rule, ruleMapper.selectEffectiveRulesForPark(fact.getParkId(), source, date));
		SmtEnergyParkDayItem item = itemMapper.selectMeterDayItem(fact.getParkId(), source, meterId, date);
		return item == null || !Objects.equals(item.getRuleId(), rule == null ? null : rule.getId())
				|| !Objects.equals(item.getRuleVersion(), rule == null ? null : rule.getRuleVersion())
				|| !Objects.equals(item.getRuleDecision(), decision.getDecision()) || !Objects.equals(item.getRuleReason(), decision.getReason());
	}

	/** 比较读数主键、采集时间和数值，覆盖修订原行及边界读数替换。 */
	private boolean sameReading(Map<String, Object> reading, Long id, LocalDateTime time, BigDecimal value) {
		return reading != null && Objects.equals(longValue(reading, "ID"), id)
				&& Objects.equals(time(reading, "COLLECT_TIME"), time) && sameDecimal(decimal(reading, "CURRENT_READING"), value);
	}

	/** 数值比较忽略数据库返回的小数位数差异，但不将空值视为零。 */
	private boolean sameDecimal(BigDecimal first, BigDecimal second) {
		return first == null ? second == null : second != null && first.compareTo(second) == 0;
	}

	/** 先给两类日期各一百个名额，再借用空闲配额，总候选不超过二百。 */
	private List<SmtEnergyProjectionQueue> fairCandidates(LocalDateTime claimedAt) {
		LocalDate recent = claimedAt.toLocalDate().minusDays(1);
		List<SmtEnergyProjectionQueue> realtime = queueMapper.selectCandidatesByDate(100, claimedAt, recent, null);
		List<SmtEnergyProjectionQueue> history = queueMapper.selectCandidatesByDate(200 - realtime.size(), claimedAt, null, recent);
		if (history.size() < 100 && realtime.size() == 100)
			realtime = queueMapper.selectCandidatesByDate(200 - history.size(), claimedAt, recent, null);
		List<SmtEnergyProjectionQueue> candidates = new ArrayList<>(realtime); candidates.addAll(history); return candidates;
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

	/** 在调用方持有表计日锁的事务内生成事实；合并后重读首次保存的园区快照，再刷新该历史园区的明细与汇总。 */
	private void project(String source, Long meterId, LocalDate date) {
		Map<String, Object> meter = factMapper.selectActiveMeter(source, meterId);
		if (meter == null || meter.isEmpty()) throw new IllegalStateException("表计不存在，无法建立园区归属事实");
		Long parkId = longValue(meter, "PARK_ID");
		String resource = ELE.equals(source) ? "ELECTRICITY" : WATER;
		String unit = ELE.equals(source) ? "kWh" : "m3";
		BigDecimal multiplier = ELE.equals(source) ? decimal(meter, "MULTIPLIER") : BigDecimal.ONE;
		LocalDateTime startBoundary = date.atStartOfDay();
		LocalDateTime endBoundary = dayEndBoundary(date);
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

	/** 依据两侧边界、配置容差和读数质量构造日事实；缺失边界保留为空，不写数据库。 */
	private SmtEnergyMeterDayFact buildFact(String source, Long meterId, Long parkId, String resource, String unit, LocalDate date, BigDecimal multiplier, LocalDateTime startBoundary, LocalDateTime endBoundary, Map<String, Object> start, Map<String, Object> end) {
		BigDecimal startReading = decimal(start, "CURRENT_READING");
		BigDecimal endReading = decimal(end, "CURRENT_READING");
		LocalDateTime startTime = time(start, "COLLECT_TIME");
		LocalDateTime endTime = time(end, "COLLECT_TIME");
		String quality = READY;
		String detail = null;
		if (!hasValidBoundaries(startBoundary, endBoundary, start, end)) { quality = "MISSING_BOUNDARY"; detail = "缺少业务日边界容差内的有效读数"; }
		else if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) { quality = "INVALID_MULTIPLIER"; detail = "电表倍率为空或不大于零"; }
		else if (startReading == null || endReading == null) { quality = "INVALID_READING"; detail = "读数不是有效数值"; }
		else if (endReading.compareTo(startReading) < 0) { quality = "NEGATIVE_DELTA"; detail = "日末读数小于日初读数"; }
		BigDecimal rawDelta = READY.equals(quality) ? endReading.subtract(startReading) : null;
		BigDecimal usage = READY.equals(quality) ? EnergyDailyUsageCalculator.calculate(startReading, endReading, multiplier) : null;
		return SmtEnergyMeterDayFact.builder().id(IdWorker.getId()).parkId(parkId).meterSource(source).meterId(meterId).resourceType(resource).unit(unit).statDate(date)
				.dayStartHistoryId(longValue(start, "ID")).dayStartTime(startTime).dayStartReading(startReading).dayEndHistoryId(longValue(end, "ID")).dayEndTime(endTime).dayEndReading(endReading)
				.multiplierSnapshot(multiplier == null ? BigDecimal.ZERO : multiplier).rawDelta(rawDelta).usageValue(usage).qualityCode(quality).qualityDetail(detail).sourceHash(hash(start, end, multiplier)).calculatedAt(now()).build();
	}

	/** 按同一次业务时钟确定日末，当日取当前时刻，历史日取次日零点。 */
	private LocalDateTime dayEndBoundary(LocalDate date) {
		LocalDateTime asOf = now();
		return date.equals(asOf.toLocalDate()) ? asOf : date.plusDays(1).atStartOfDay();
	}

	/** 补齐过期检查与事实质量计算共用日界和配置容差，避免两条路径判断漂移。 */
	private boolean hasValidBoundaries(LocalDateTime startBoundary, LocalDateTime endBoundary,
			Map<String, Object> start, Map<String, Object> end) {
		return start != null && end != null && within(time(start, "COLLECT_TIME"), startBoundary)
				&& within(time(end, "COLLECT_TIME"), endBoundary);
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
	/** 空读数字段保留为空，避免三元数值提升将 null 自动拆箱后破坏缺失边界事实。 */
	private static Long longValue(Map<String,Object> map, String key) {
		Object value = value(map,key);
		if (value == null) return null;
		return value instanceof Number ? Long.valueOf(((Number)value).longValue()) : Long.valueOf(value.toString());
	}
	private static int intValue(Map<String,Object> map, String key) { Long v=longValue(map,key); return v==null?0:v.intValue(); }
	private static BigDecimal decimal(Map<String,Object> map, String key) { Object v=value(map,key); if(v instanceof BigDecimal)return (BigDecimal)v; try{return v==null?null:new BigDecimal(v.toString());}catch(NumberFormatException ex){return null;} }
	private static Object value(Map<String,Object> map,String key){ if(map==null)return null; Object value=map.get(key); return value!=null?value:map.get(key.toLowerCase(Locale.ROOT)); }
	private static LocalDateTime time(Map<String,Object> map,String key){ Object v=value(map,key); if(v instanceof Timestamp)return ((Timestamp)v).toLocalDateTime(); if(v instanceof LocalDateTime)return (LocalDateTime)v; return null; }
	private static String hash(Object... values) { try { MessageDigest digest=MessageDigest.getInstance("SHA-256"); for(Object value:values) digest.update(String.valueOf(value).getBytes(StandardCharsets.UTF_8)); StringBuilder out=new StringBuilder(); for(byte b:digest.digest())out.append(String.format("%02x",b)); return out.toString(); } catch(Exception ex) { throw new IllegalStateException(ex); } }
}
