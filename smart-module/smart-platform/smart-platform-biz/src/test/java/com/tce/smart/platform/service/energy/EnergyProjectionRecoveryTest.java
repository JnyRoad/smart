package com.tce.smart.platform.service.energy;

import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterDayFact;
import com.tce.smart.platform.core.entity.energy.SmtEnergyParkDayItem;
import com.tce.smart.platform.core.entity.energy.SmtEnergyProjectionQueue;
import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterScopeRule;
import com.tce.smart.platform.core.mapper.energy.*;
import com.tce.smart.platform.service.energy.impl.EnergyProjectionServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 以外部存储替身验证真实服务的恢复行为，不连接实际数据库或 Redis。 */
public class EnergyProjectionRecoveryTest {
    @Mock private SmtEnergyProjectionQueueMapper queueMapper;
    @Mock private SmtEnergyMeterDayFactMapper factMapper;
    @Mock private SmtEnergyMeterScopeRuleMapper ruleMapper;
    @Mock private SmtEnergyParkDayItemMapper itemMapper;
    @Mock private SmtEnergyParkDayMapper parkDayMapper;
    @Mock private SmtEnergyParkDayLockMapper parkDayLockMapper;
    @Mock private SmtEnergyMeterDayLockMapper meterDayLockMapper;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> values;
    private EnergyProjectionServiceImpl service;
    private final Map<String,String> progress = new HashMap<>();

    /** 初始化有界分页存储；新增查询默认为空，入队结果可被独立计数。 */
    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        java.lang.reflect.Constructor<?> constructor=EnergyProjectionServiceImpl.class.getConstructors()[0];
        Object[] args=new Object[constructor.getParameterCount()];
        Object[] dependencies={queueMapper,factMapper,ruleMapper,itemMapper,parkDayMapper,parkDayLockMapper,meterDayLockMapper,transactionManager,redisTemplate};
        for(int n=0;n<args.length;n++) {
            Class<?> type=constructor.getParameterTypes()[n];
            if(type==int.class) args[n]=1000;
            else if(type==long.class) args[n]=5L;
            else if(type==String.class) args[n]="Asia/Shanghai";
            else for(Object dependency:dependencies) if(type.isInstance(dependency)) args[n]=dependency;
        }
        service=(EnergyProjectionServiceImpl)constructor.newInstance(args);
        ReflectionTestUtils.setField(service,"zoneId","Asia/Shanghai");
        ReflectionTestUtils.setField(service,"boundaryToleranceMinutes",120L);
        ReflectionTestUtils.setField(service,"backfillMaxRequests",1000);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenAnswer(i -> progress.get(i.getArgument(0)));
        doAnswer(i -> { progress.put(i.getArgument(0), i.getArgument(1)); return null; }).when(values).set(anyString(),anyString());
        when(redisTemplate.execute(any(RedisScript.class),anyList(),any(),any())).thenAnswer(i -> {
            String key=((List<String>)i.getArgument(1)).get(0);
            String previous=i.getArgument(2); String next=i.getArgument(3);
            if (!Objects.equals(progress.getOrDefault(key,""),previous)) return 0L;
            progress.put(key,next); return 1L;
        });
        when(queueMapper.insertIfAbsent(anyLong(),anyString(),anyLong(),any(),any())).thenReturn(1);
        when(factMapper.selectActiveMeters(anyString(),anyLong(),anyInt())).thenReturn(Collections.emptyList());
    }

    /** 首批扫描 1000 个表计日后必须从第 1001 个继续，不能重复扫描已完成部分。 */
    @Test public void secondBatchResumesAfterThousandWithoutRescanning() {
        when(factMapper.selectActiveMeters(eq("ELE"),anyLong(),anyInt())).thenAnswer(i -> {
            long after = i.getArgument(1); int limit = i.getArgument(2);
            List<Map<String,Object>> meters = new ArrayList<>();
            for(long id=after+1;id<=1100 && meters.size()<limit;id++) meters.add(meter(id));
            return meters;
        });
        assertEquals(1000,service.backfillCurrentMonthToDate());
        clearInvocations(factMapper);
        service.backfillCurrentMonthToDate();
        org.mockito.ArgumentCaptor<Long> afterIds=org.mockito.ArgumentCaptor.forClass(Long.class);
        verify(factMapper,atLeastOnce()).selectActiveMeters(eq("ELE"),afterIds.capture(),anyInt());
        assertEquals(Long.valueOf(1000L),afterIds.getAllValues().get(0));
    }

    /** 已有缺失边界事实不能因为“存在事实”永久跳过。 */
    @Test public void missingBoundaryFactCanBeRequeued() {
        when(factMapper.selectActiveMeters(eq("ELE"),eq(0L),anyInt())).thenReturn(Collections.singletonList(meter(1L)));
        when(factMapper.existsFactOrActiveQueue(anyString(),anyLong(),any())).thenReturn(1);
        when(factMapper.selectFact(anyString(),anyLong(),any())).thenReturn(SmtEnergyMeterDayFact.builder().qualityCode("MISSING_BOUNDARY").build());
        assertTrue(service.backfillCurrentMonthToDate()>0);
    }

    /** 每日回算应先形成持久队列，计算失败才有重试记录，不直接同步计算。 */
    @Test public void reconcileDurablyEnqueuesInsteadOfSwallowingProjectionFailure() {
        LocalDate date=LocalDate.of(2026,8,31);
        when(factMapper.selectActiveMeters(eq("ELE"),eq(0L),anyInt())).thenReturn(Collections.singletonList(meter(1L)));
        service.reconcile(date);
        verify(queueMapper).insertIfAbsent(anyLong(),eq("ELE"),eq(1L),eq(date),any());
        verify(factMapper,never()).selectActiveMeter(anyString(),anyLong());
    }

    /** 活跃任务保留租约和退避时间，补齐不得再次写入请求。 */
    @Test public void activeRequestIsNeverOverwrittenByBackfill() {
        when(factMapper.selectActiveMeters(eq("ELE"),eq(0L),anyInt())).thenReturn(Collections.singletonList(meter(1L)));
        when(queueMapper.countActiveRequest(anyString(),anyLong(),any())).thenReturn(1);
        assertEquals(0,service.backfillCurrentMonthToDate());
        verify(queueMapper,never()).insertIfAbsent(anyLong(),anyString(),anyLong(),any(),any());
        verify(factMapper,never()).selectFact(anyString(),anyLong(),any());
    }

    /** 没有变化的完整历史事实不会重复入队，小数位数差异也不算源变化。 */
    @Test public void unchangedReadyFactIsSkipped() {
        readyFixture();
        assertEquals(0,service.backfillCurrentMonthToDate());
        verify(queueMapper,never()).insertIfAbsent(anyLong(),anyString(),anyLong(),any(),any());
    }

    /** 当日中午的 READY 快照在停报后不再满足完整日末边界，漏跑每日回算也必须补救。 */
    @Test public void middayReadySnapshotIsRequeuedAfterDayBecomesHistorical() {
        LocalDate date=readyFixture();
        SmtEnergyMeterDayFact fact=factMapper.selectFact("ELE",1L,date);
        fact.setDayEndTime(date.atTime(12,0));
        Map<String,Object> unchangedReading=reading(12L,date,"12");
        unchangedReading.put("COLLECT_TIME",date.atTime(12,0));
        when(factMapper.selectLatestReadingAtOrBefore("ELE",1L,date.plusDays(1).atStartOfDay())).thenReturn(unchangedReading);

        assertEquals(1,service.backfillCurrentMonthToDate());
        verify(queueMapper).insertIfAbsent(anyLong(),eq("ELE"),eq(1L),eq(date),any());
    }

    /** 原先在两小时容差内的相同读数，在配置收紧至一小时后必须重新判定质量。 */
    @Test public void tighterBoundaryToleranceRequeuesUnchangedReadyFact() {
        LocalDate date=readyFixture();
        SmtEnergyMeterDayFact fact=factMapper.selectFact("ELE",1L,date);
        fact.setDayEndTime(date.plusDays(1).atStartOfDay().minusMinutes(90));
        Map<String,Object> unchangedReading=reading(12L,date,"12");
        unchangedReading.put("COLLECT_TIME",fact.getDayEndTime());
        when(factMapper.selectLatestReadingAtOrBefore("ELE",1L,date.plusDays(1).atStartOfDay())).thenReturn(unchangedReading);
        ReflectionTestUtils.setField(service,"boundaryToleranceMinutes",60L);

        assertEquals(1,service.backfillCurrentMonthToDate());
    }

    /** 同一读数行数值修订必须触发重算，不能只比较历史 ID。 */
    @Test public void changedReadingValueRequeuesReadyFact() {
        LocalDate date=readyFixture();
        when(factMapper.selectLatestReadingAtOrBefore("ELE",1L,date.plusDays(1).atStartOfDay())).thenReturn(reading(12L,date.plusDays(1),"15"));
        assertEquals(1,service.backfillCurrentMonthToDate());
    }

    /** 仅倍率修订也使历史日事实过期。 */
    @Test public void changedMultiplierRequeuesReadyFact() {
        readyFixture();
        Map<String,Object> changed=meter(1); changed.put("MULTIPLIER",new BigDecimal("2"));
        when(factMapper.selectActiveMeter("ELE",1L)).thenReturn(changed);
        assertEquals(1,service.backfillCurrentMonthToDate());
    }

    /** 当前表规则未换版本但父规则改为纳入时，完整决策变化仍触发重算。 */
    @Test public void ancestorRuleChangeRequeuesReadyFact() {
        LocalDate date=readyFixture();
        SmtEnergyMeterScopeRule current=SmtEnergyMeterScopeRule.builder().id(10L).meterId(1L).parentMeterId(2L).includeFlag(1).ruleVersion(1).build();
        SmtEnergyMeterScopeRule parent=SmtEnergyMeterScopeRule.builder().id(20L).meterId(2L).includeFlag(1).ruleVersion(2).build();
        when(ruleMapper.selectEffectiveRule("ELE",1L,date)).thenReturn(current);
        when(ruleMapper.selectEffectiveRulesForPark(1L,"ELE",date)).thenReturn(Arrays.asList(current,parent));
        when(itemMapper.selectMeterDayItem(1L,"ELE",1L,date)).thenReturn(SmtEnergyParkDayItem.builder().ruleId(10L).ruleVersion(1).ruleDecision("INCLUDED").build());
        assertEquals(1,service.backfillCurrentMonthToDate());
    }

    /** 单表计算异常先写入有限重试状态，再向调用方返回失败。 */
    @Test public void pendingFailureIsPersistedAndReported() {
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Shanghai"));
        SmtEnergyProjectionQueue queue=SmtEnergyProjectionQueue.builder().id(1L).meterSource("ELE").meterId(1L).statDate(today).requestCount(1).build();
        when(queueMapper.selectCandidatesByDate(anyInt(),any(),any(),any())).thenReturn(Collections.emptyList());
        when(queueMapper.selectCandidatesByDate(eq(100),any(),eq(today.minusDays(1)),isNull())).thenReturn(Collections.singletonList(queue));
        when(queueMapper.claim(eq(1L),eq(1),any(),any(),anyString())).thenReturn(1);
        when(queueMapper.verifyCurrentLeaseForUpdate(eq(1L),eq(1),anyString())).thenReturn(1L);
        when(queueMapper.failOrRetry(eq(1L),eq(1),anyString(),any(),any(),anyInt(),anyString())).thenReturn(1);
        try { service.processPending(); fail("批次不能伪报成功"); } catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("失败数=1")); }
        verify(queueMapper).failOrRetry(eq(1L),eq(1),anyString(),any(),any(),anyInt(),contains("表计不存在"));
    }

    /** 没有实时任务时历史任务可使用全部二百个名额。 */
    @Test public void historyBorrowsUnusedRealtimeQuota() {
        LocalDate recent=LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1);
        List<SmtEnergyProjectionQueue> history=new ArrayList<>();
        for(long id=1;id<=200;id++) history.add(SmtEnergyProjectionQueue.builder().id(id).requestCount(1).build());
        when(queueMapper.selectCandidatesByDate(eq(100),any(),eq(recent),isNull())).thenReturn(Collections.emptyList());
        when(queueMapper.selectCandidatesByDate(eq(200),any(),isNull(),eq(recent))).thenReturn(history);
        service.processPending();
        verify(queueMapper,times(200)).claim(anyLong(),eq(1),any(),any(),anyString());
    }

    /** 旧月断点恢复到旧月末，完成后才开始新月，不能沿用旧月的日期上限。 */
    @Test public void monthSwitchPreservesUnfinishedPreviousMonth() {
        EnergyBackfillCursor cursor=EnergyBackfillCursor.restore("2026-08-01|2026-08-28|2026-08-28|ELE|1000|1000|1000|0|",LocalDate.of(2026,9,1));
        assertEquals(LocalDate.of(2026,8,31),cursor.through);
        assertEquals(1000,cursor.afterId);
        cursor.date=LocalDate.of(2026,9,1); cursor.completedOn=LocalDate.of(2026,9,1);
        EnergyBackfillCursor next=EnergyBackfillCursor.restore(cursor.encode(),LocalDate.of(2026,9,1));
        assertEquals(LocalDate.of(2026,9,1),next.month);
        assertEquals(0,next.afterId);
    }

    /** 旧月中旬已完成的扫描跨月扩展到月末后必须清除完成标记，并保留原续扫位置。 */
    @Test public void expandedPreviousMonthClearsObsoleteCompletionMarker() {
        EnergyBackfillCursor cursor=EnergyBackfillCursor.restore("2026-08-01|2026-08-16|2026-08-15|ELE|0|15000|1000|2|2026-08-15",LocalDate.of(2026,9,1));
        assertEquals(LocalDate.of(2026,8,1),cursor.month);
        assertEquals(LocalDate.of(2026,8,16),cursor.date);
        assertEquals(LocalDate.of(2026,8,31),cursor.through);
        assertEquals("ELE",cursor.source);
        assertEquals(0,cursor.afterId);
        assertEquals(15000,cursor.scanned);
        assertNull(cursor.completedOn);
        assertNull(EnergyBackfillCursor.restore(cursor.encode(),LocalDate.of(2026,9,1)).completedOn);
    }

    /** 检查完成的当月不在同一天反复扫，次日才开启新一轮变化检查。 */
    @Test public void completedSweepWaitsUntilFollowingDay() {
        EnergyBackfillCursor sameDay=EnergyBackfillCursor.restore("2026-09-01|2026-09-06|2026-09-05|ELE|0|5000|0|0|2026-09-05",LocalDate.of(2026,9,5));
        assertTrue(sameDay.date.isAfter(sameDay.through));
        EnergyBackfillCursor nextDay=EnergyBackfillCursor.restore(sameDay.encode(),LocalDate.of(2026,9,6));
        assertEquals(LocalDate.of(2026,9,1),nextDay.date);
    }

    /** 数据库入队失败不能推进断点，恢复后重试同一个表计日。 */
    @Test public void enqueueFailureRetainsCursorForRetry() {
        ReflectionTestUtils.setField(service,"backfillMaxRequests",1);
        when(factMapper.selectActiveMeters(eq("ELE"),eq(0L),anyInt())).thenReturn(Collections.singletonList(meter(1)));
        when(queueMapper.insertIfAbsent(anyLong(),eq("ELE"),eq(1L),any(),any())).thenThrow(new IllegalStateException("database unavailable")).thenReturn(1);
        try { service.backfillCurrentMonthToDate(); fail("入队失败必须报告"); } catch(IllegalStateException expected) { assertEquals("database unavailable",expected.getMessage()); }
        assertTrue(progress.isEmpty());
        assertEquals(1,service.backfillCurrentMonthToDate());
        verify(queueMapper,times(2)).insertIfAbsent(anyLong(),eq("ELE"),eq(1L),any(),any());
    }

    /** 另一个实例抢先推进断点时当前批次明确失败，不覆盖新进度。 */
    @Test public void cursorCompareAndSetConflictIsReported() {
        when(redisTemplate.execute(any(RedisScript.class),anyList(),any(),any())).thenReturn(0L);
        try { service.backfillCurrentMonthToDate(); fail("并发推进必须重试"); }
        catch(IllegalStateException expected) { assertTrue(expected.getMessage().contains("其他批次")); }
    }

    /** 两类队列均满时各取一百，历史压力不能挤掉实时配额。 */
    @Test public void fullQueuesEachReceiveHalfQuota() {
        LocalDate recent=LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1);
        List<SmtEnergyProjectionQueue> realtime=queues(1,100), history=queues(101,100);
        when(queueMapper.selectCandidatesByDate(eq(100),any(),eq(recent),isNull())).thenReturn(realtime);
        when(queueMapper.selectCandidatesByDate(eq(100),any(),isNull(),eq(recent))).thenReturn(history);
        service.processPending();
        verify(queueMapper,times(200)).claim(anyLong(),eq(1),any(),any(),anyString());
        verify(queueMapper).claim(eq(1L),eq(1),any(),any(),anyString());
        verify(queueMapper).claim(eq(200L),eq(1),any(),any(),anyString());
    }

    /** 历史任务为空时实时任务同样可以借满二百个名额。 */
    @Test public void realtimeBorrowsUnusedHistoryQuota() {
        LocalDate recent=LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1);
        when(queueMapper.selectCandidatesByDate(eq(100),any(),eq(recent),isNull())).thenReturn(queues(1,100));
        when(queueMapper.selectCandidatesByDate(eq(100),any(),isNull(),eq(recent))).thenReturn(Collections.emptyList());
        when(queueMapper.selectCandidatesByDate(eq(200),any(),eq(recent),isNull())).thenReturn(queues(1,200));
        service.processPending();
        verify(queueMapper,times(200)).claim(anyLong(),eq(1),any(),any(),anyString());
    }

    /** 扫描期间表计被删除时仍可将该项交给消费者落不可用事实。 */
    @Test public void meterDeletedDuringScanIsRequeued() {
        readyFixture();
        Map<String,Object> deleted=meter(1); deleted.put("IS_DELETE",1);
        when(factMapper.selectActiveMeter("ELE",1L)).thenReturn(deleted);
        assertEquals(1,service.backfillCurrentMonthToDate());
    }

    /** 缺失读数必须写成可恢复质量事实，不能在提取空历史 ID 时发生拆箱异常。 */
    @Test public void missingReadingProducesRecoverableQualityFact() {
        LocalDate today=LocalDate.now(ZoneId.of("Asia/Shanghai"));
        SmtEnergyProjectionQueue queue=SmtEnergyProjectionQueue.builder().id(1L).meterSource("ELE").meterId(1L).statDate(today).requestCount(1).build();
        when(queueMapper.selectCandidatesByDate(anyInt(),any(),any(),any())).thenReturn(Collections.emptyList());
        when(queueMapper.selectCandidatesByDate(eq(100),any(),eq(today.minusDays(1)),isNull())).thenReturn(Collections.singletonList(queue));
        when(queueMapper.claim(eq(1L),eq(1),any(),any(),anyString())).thenReturn(1);
        when(queueMapper.verifyCurrentLeaseForUpdate(eq(1L),eq(1),anyString())).thenReturn(1L);
        when(transactionManager.getTransaction(any())).thenReturn(new org.springframework.transaction.support.SimpleTransactionStatus());
        Map<String,Object> meter=meter(1); meter.put("MULTIPLIER",BigDecimal.ONE);
        when(factMapper.selectActiveMeter("ELE",1L)).thenReturn(meter);
        List<SmtEnergyMeterDayFact> written=new ArrayList<>();
        when(factMapper.mergeFact(any())).thenAnswer(i -> { written.add(i.getArgument(0)); return 1; });
        when(factMapper.selectFact("ELE",1L,today)).thenAnswer(i -> written.get(0));
        Map<String,Object> summary=new HashMap<>();
        for(String key:Arrays.asList("INCLUDED_COUNT","EXCLUDED_COUNT","INVALID_COUNT","MISSING_COUNT","PROJECTED_COUNT")) summary.put(key,0);
        when(parkDayMapper.summarizeItems(anyLong(),any(),anyString(),anyString())).thenReturn(summary);
        when(queueMapper.finish(eq(1L),eq(1),eq("DONE"),any(),isNull(),anyString())).thenReturn(1);
        try { service.processPending(); } catch(IllegalStateException ex) { fail("缺失边界应保存质量事实而非抛出批次失败: "+ex.getMessage()); }
        assertEquals("MISSING_BOUNDARY",written.get(0).getQualityCode());
        assertNull(written.get(0).getDayStartHistoryId());
    }

    /** 构造候选列表，claim 默认未抢到，不发生计算副作用。 */
    private List<SmtEnergyProjectionQueue> queues(long first,int count) {
        List<SmtEnergyProjectionQueue> queues=new ArrayList<>();
        for(long id=first;id<first+count;id++) queues.add(SmtEnergyProjectionQueue.builder().id(id).requestCount(1).build());
        return queues;
    }

    /** 构造固定历史日的一条正常事实和规则快照，扫描预算为一。 */
    private LocalDate readyFixture() {
        LocalDate date=YearMonth.now(ZoneId.of("Asia/Shanghai")).minusMonths(1).atDay(15);
        progress.put("smart:energy:backfill:cursor:v1",date.withDayOfMonth(1)+"|"+date+"|"+date+"|ELE|0|0|0|0|");
        ReflectionTestUtils.setField(service,"backfillMaxRequests",1);
        when(factMapper.selectActiveMeters(eq("ELE"),eq(0L),anyInt())).thenReturn(Collections.singletonList(meter(1L)));
        Map<String,Object> meter=meter(1); meter.put("MULTIPLIER",new BigDecimal("1.00"));
        when(factMapper.selectActiveMeter("ELE",1L)).thenReturn(meter);
        when(factMapper.selectLatestReadingAtOrBefore("ELE",1L,date.atStartOfDay())).thenReturn(reading(11L,date,"10"));
        when(factMapper.selectLatestReadingAtOrBefore("ELE",1L,date.plusDays(1).atStartOfDay())).thenReturn(reading(12L,date.plusDays(1),"12"));
        when(factMapper.selectFact("ELE",1L,date)).thenReturn(SmtEnergyMeterDayFact.builder().parkId(1L).qualityCode("READY").multiplierSnapshot(BigDecimal.ONE)
                .dayStartHistoryId(11L).dayStartTime(date.atStartOfDay()).dayStartReading(new BigDecimal("10.000000"))
                .dayEndHistoryId(12L).dayEndTime(date.plusDays(1).atStartOfDay()).dayEndReading(new BigDecimal("12.000000")).build());
        when(itemMapper.selectMeterDayItem(1L,"ELE",1L,date)).thenReturn(SmtEnergyParkDayItem.builder().ruleDecision("DEFAULT_INCLUDED").ruleReason("未配置范围规则，默认纳入").build());
        return date;
    }

    /** 测试读数保留实际查询的三个来源字段。 */
    private Map<String,Object> reading(long id,LocalDate date,String value) {
        Map<String,Object> reading=new HashMap<>(); reading.put("ID",id); reading.put("COLLECT_TIME",date.atStartOfDay()); reading.put("CURRENT_READING",new BigDecimal(value)); return reading;
    }

    /** 测试表计包含分页所需的 ID 与固定园区。 */
    private static Map<String,Object> meter(long id) {
        Map<String,Object> meter=new HashMap<>(); meter.put("ID",id); meter.put("PARK_ID",1L); meter.put("IS_DELETE",0); return meter;
    }
}
