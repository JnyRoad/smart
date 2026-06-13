'use client'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getRepairApprovalPage, type RepairApprovalItem } from '@/features/backlog/api'
import { ApprovalTabs } from '@/features/backlog/approval-tabs'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

function FieldRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null
  return (
    <div className="flex gap-3 pt-1.5 text-[13px]">
      <span className="flex-none text-mid">{label}</span>
      <span className="min-w-0 flex-1 truncate">{value}</span>
    </div>
  )
}

function RepairApprovalInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const [tab, setTab] = useState<0 | 1>(params.get('tab') === 'done' ? 1 : 0)

  const pager = useListPager<RepairApprovalItem, string>({
    filterKey: String(tab),
    fetchPage: async (page) => {
      const res = await getRepairApprovalPage({ recordState: tab, current: page, size: PAGE_SIZE })
      if (res.code !== 0) {
        Toast.show(res.message || '网络错误')
        throw new Error(res.message || '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="园区报修审批">
      <ApprovalTabs active={tab} onChange={setTab} />

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {!pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无审批记录" />
        ) : (
          <div className="flex flex-col gap-3">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.approveId}-${index}`}
                type="button"
                onClick={() =>
                  router.push(`/backlog/repairs/detail?id=${record.approveId}${tab === 1 ? '&tab=done' : ''}`)
                }
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.approveName}</span>
                  <span className="flex-none text-[13px] font-semibold text-mid">{record.statusDesc}</span>
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

export default function RepairApprovalPage() {
  return (
    <Suspense>
      <RepairApprovalInner />
    </Suspense>
  )
}
