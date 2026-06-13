'use client'
import { DatePicker, ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getWaterElecRecords, type StatementRecord } from '@/features/dorm/api'
import { normalizeCateInfos } from '@/features/dorm/water-elec-rules'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

function currentMonth(): string {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

/** Monthly water/electricity statements with month filter and pagination. */
export default function WaterElecPage() {
  const authorized = useRequireAuth()
  // '' = 查询全部 (statementMonth omitted-as-empty, legacy behavior)
  const [month, setMonth] = useState<string>(currentMonth)
  const [pickerVisible, setPickerVisible] = useState(false)

  const pager = useListPager<StatementRecord, string>({
    filterKey: month,
    fetchPage: async (page) => {
      const res = await getWaterElecRecords({ current: page, size: PAGE_SIZE, statementMonth: month })
      if (res.code !== 0) {
        Toast.show(res.message ?? '网络错误')
        throw new Error(res.message ?? '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="水电扣费明细">
      {/* 筛选条 */}
      <div className="mb-3 flex items-center justify-between rounded-2xl bg-white px-4 py-3 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <button type="button" onClick={() => setMonth('')} className="text-sm font-semibold text-brand">
          查询全部
        </button>
        <button
          type="button"
          onClick={() => setPickerVisible(true)}
          className="rounded-xl border border-border-soft bg-surface px-3.5 py-2 text-sm"
        >
          {month || '未选择'} ▾
        </button>
        <DatePicker
          visible={pickerVisible}
          onClose={() => setPickerVisible(false)}
          precision="month"
          max={new Date()}
          onConfirm={(d) => setMonth(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)}
        />
      </div>

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {!pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无数据" />
        ) : (
          <div className="flex flex-col gap-3">
            {pager.rows.map((record, index) => (
              <div
                key={`${record.statementDate}-${index}`}
                className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2.5">
                  <span className="text-[15px] font-bold">
                    {record.staffName}-{record.staffBadge}
                  </span>
                  <span className="text-xs text-mid">{record.statementDate}</span>
                </div>
                <div className="flex justify-between pt-2.5 text-[13px]">
                  <span className="text-mid">抄表月份</span>
                  <span>{record.meterMonth}</span>
                </div>
                {normalizeCateInfos(record.cateInfos).map((cate, cateIndex) => (
                  <div key={`${cate.cateName}-${cateIndex}`} className="flex justify-between pt-2 text-[13px]">
                    <span className="text-mid">房间{cate.cateName}费</span>
                    <span>¥{cate.fee}</span>
                  </div>
                ))}
                <div className="mt-2.5 flex justify-between border-t border-border-soft pt-2.5 text-sm font-bold">
                  <span>总计</span>
                  <span className="text-brand">¥{record.totalFee}</span>
                </div>
              </div>
            ))}
          </div>
        )}
        <InfiniteScroll loadMore={pager.loadMore} hasMore={!pager.loadedOnce || pager.hasMore} />
      </PullToRefresh>
    </PageShell>
  )
}
