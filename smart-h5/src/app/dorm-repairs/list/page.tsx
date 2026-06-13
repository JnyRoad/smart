'use client'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getRepairRecords, type RepairRecord } from '@/features/dorm-services/api'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

/** status 3 = resolved (green); 4 / 6 = failed (red); others gray. */
function statusClass(status: number | undefined): string {
  if (status === 3) return 'text-[#16a673]'
  if (status === 4 || status === 6) return 'text-[#d83b36]'
  return 'text-mid'
}

function FieldRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null
  return (
    <div className="flex gap-3 pt-1.5 text-[13px]">
      <span className="flex-none text-mid">{label}</span>
      <span className="min-w-0 flex-1 truncate">{value}</span>
    </div>
  )
}

export default function DormRepairsListPage() {
  const authorized = useRequireAuth()
  const router = useRouter()

  const pager = useListPager<RepairRecord, string>({
    filterKey: '',
    fetchPage: async (page) => {
      const res = await getRepairRecords({ current: page, size: PAGE_SIZE })
      if (res.code !== 0) {
        Toast.show(res.message ?? '网络错误')
        throw new Error(res.message ?? '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="宿舍报修">
      <SegmentTabs active="list" submitHref="/dorm-repairs" listHref="/dorm-repairs/list" />

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {!pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无报修记录" />
        ) : (
          <div className="flex flex-col gap-3">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.id}-${index}`}
                type="button"
                onClick={() => router.push(`/dorm-repairs/detail?id=${record.id}`)}
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.name}提交的园区报修</span>
                  <span className={`flex-none text-[13px] font-semibold ${statusClass(record.status)}`}>
                    {record.statusDesc}
                  </span>
                </div>
                <FieldRow label="维修区域" value={record.rangeTypeDesc} />
                <FieldRow label="维修类别" value={record.repairTypeDesc} />
                <FieldRow label="所在楼栋" value={record.dormitoryName} />
                <FieldRow label="故障描述" value={record.faultDesc} />
                <FieldRow label="申请时间" value={record.createTime} />
              </button>
            ))}
          </div>
        )}
        <InfiniteScroll loadMore={pager.loadMore} hasMore={!pager.loadedOnce || pager.hasMore} />
      </PullToRefresh>
    </PageShell>
  )
}
