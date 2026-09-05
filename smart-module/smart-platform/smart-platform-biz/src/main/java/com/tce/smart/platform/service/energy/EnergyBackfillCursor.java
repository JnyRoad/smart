package com.tce.smart.platform.service.energy;

import java.time.LocalDate;
import java.time.YearMonth;

/** 补齐扫描的可序列化断点；每轮按月、日、来源、表 ID 单向推进。 */
public class EnergyBackfillCursor {
    public LocalDate month;
    public LocalDate date;
    public LocalDate through;
    public String source;
    public long afterId;
    public long scanned;
    public long accepted;
    public long failed;
    public LocalDate completedOn;

    /** 恢复已有断点，跨月先完成旧月；完整当月轮次在次日重新检查变化。 */
    public static EnergyBackfillCursor restore(String saved, LocalDate today) {
        if (saved == null) return start(YearMonth.from(today).atDay(1), today);
        String[] parts=saved.split("\\|",-1);
        EnergyBackfillCursor cursor=new EnergyBackfillCursor();
        cursor.month=LocalDate.parse(parts[0]); cursor.date=LocalDate.parse(parts[1]);
        cursor.through=LocalDate.parse(parts[2]); cursor.source=parts[3]; cursor.afterId=Long.parseLong(parts[4]);
        cursor.scanned=Long.parseLong(parts[5]); cursor.accepted=Long.parseLong(parts[6]); cursor.failed=Long.parseLong(parts[7]);
        cursor.completedOn=parts[8].isEmpty()?null:LocalDate.parse(parts[8]);
        LocalDate currentMonth=YearMonth.from(today).atDay(1);
        if (cursor.month.equals(currentMonth) && cursor.completedOn != null && !today.equals(cursor.completedOn))
            return start(currentMonth,today);
        LocalDate expandedThrough=cursor.month.isBefore(currentMonth)?YearMonth.from(cursor.month).atEndOfMonth():today;
        // 旧月扫描范围扩展后仍有待扫日期时，旧完成日不能代表本轮已完成。
        if (expandedThrough.isAfter(cursor.through) && !cursor.date.isAfter(expandedThrough)) cursor.completedOn=null;
        cursor.through=expandedThrough;
        if (cursor.date.isAfter(cursor.through)) {
            if (cursor.month.isBefore(currentMonth)) return start(cursor.month.plusMonths(1),today);
            if (!today.equals(cursor.completedOn)) return start(currentMonth,today);
        }
        return cursor;
    }

    /** 创建月份首日断点，历史月份以月末为上界，当月以调用日为上界。 */
    private static EnergyBackfillCursor start(LocalDate month, LocalDate today) {
        EnergyBackfillCursor cursor=new EnergyBackfillCursor();
        cursor.month=month; cursor.date=month; cursor.source="ELE";
        cursor.through=YearMonth.from(month).equals(YearMonth.from(today))?today:YearMonth.from(month).atEndOfMonth();
        return cursor;
    }

    /** 当前来源的有界分页结束后切换来源或下一业务日。 */
    public void nextSource() {
        afterId=0;
        if ("ELE".equals(source)) source="WATER";
        else { source="ELE"; date=date.plusDays(1); }
    }

    /** 以固定字段顺序持久化断点和累计统计，不依赖 JVM 对象字符串形式。 */
    public String encode() {
        return month+"|"+date+"|"+through+"|"+source+"|"+afterId+"|"+scanned+"|"+accepted+"|"+failed+"|"+(completedOn==null?"":completedOn);
    }
}
