'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { StatusIcon } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getExitApprovalPage, type ExitApprovalItem } from '@/features/backlog/api'
import { ApprovalTabs } from '@/features/backlog/approval-tabs'
import { StaffFilterPopup } from '@/features/backlog/staff-filter-popup'
import { getTenantConfig } from '@/lib/config/tenant'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

/** 已审批 Tab 配色：status 1 绿 / 3 红（旧 list.vue 事实）。 */
function statusClass(status: number | undefined): string {
  if (status === 1) return 'text-[#16a673]'
  if (status === 3) return 'text-[#d83b36]'
  return 'text-mid'
}

function FieldRow({ label, value }: { label: string; value?: React.ReactNode }) {
  if (!value) return null
  return (
    <div className="flex gap-3 pt-1.5 text-[13px]">
      <span className="flex-none text-mid">{label}</span>
      <span className="min-w-0 flex-1">{value}</span>
    </div>
  )
}

function DormExitApprovalInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const config = getTenantConfig()
  const [tab, setTab] = useState<0 | 1>(params.get('tab') === 'done' ? 1 : 0)
  const [search, setSearch] = useState<{ badge?: string; name?: string }>({})
  const [searchVisible, setSearchVisible] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const guardFlag = baseInfo.data?.code === 0 ? baseInfo.data.data?.isSecurityGuard : undefined
  const isSecurityGuard = guardFlag === 0

  const pager = useListPager<ExitApprovalItem, string>({
    filterKey: `${tab}|${guardFlag}|${JSON.stringify(search)}`,
    fetchPage: async (page) => {
      if (guardFlag === undefined) return { rows: [], pages: 0 }
      const res = await getExitApprovalPage({
        isSecurityGuard: Number(guardFlag),
        parkId: config.parkId,
        status: tab,
        current: page,
        size: PAGE_SIZE,
        ...search,
      })
      if (res.code !== 0) {
        Toast.show(res.message || '网络错误')
        throw new Error(res.message || '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="退宿审批">
      <ApprovalTabs
        active={tab}
        onChange={(next) => {
          setTab(next)
          setSearch({})
        }}
      />

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {guardFlag === undefined || !pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无审批记录" />
        ) : (
          <div className="flex flex-col gap-3 pb-20">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.id}-${index}`}
                type="button"
                onClick={() =>
                  router.push(`/backlog/dorm-exit/detail?id=${record.id}${tab === 1 ? '&tab=done' : ''}`)
                }
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.name}提交的退宿申请</span>
                  <span
                    className={`flex-none text-[13px] font-semibold ${tab === 1 ? statusClass(record.status) : 'text-mid'}`}
                  >
                    {record.statusDesc}
                  </span>
                </div>
                <FieldRow
                  label="房间信息"
                  value={record.dorDetailStr?.map((line, i) => (
                    <span key={i} className="block truncate">
                      {line}
                    </span>
                  ))}
                />
                <FieldRow label="退宿原因" value={record.quitReasonDesc} />
                <FieldRow label="预计离开时间" value={record.applyLeaveTime} />
                <FieldRow label="申请时间" value={record.createTime} />
              </button>
            ))}
          </div>
        )}
        <InfiniteScroll loadMore={pager.loadMore} hasMore={!pager.loadedOnce || pager.hasMore} />
      </PullToRefresh>

      {isSecurityGuard && (
        <div className="fixed inset-x-0 bottom-0 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
          <button
            type="button"
            onClick={() => setSearchVisible(true)}
            className="flex h-11 w-full items-center justify-center rounded-[12px] bg-surface text-sm font-bold text-mid"
          >
            <StatusIcon name="search" className="mr-1.5 h-5 w-5" />
            搜索
          </button>
        </div>
      )}

      {searchVisible && (
        <StaffFilterPopup visible onClose={() => setSearchVisible(false)} onSearch={setSearch} />
      )}
    </PageShell>
  )
}

export default function DormExitApprovalPage() {
  return (
    <Suspense>
      <DormExitApprovalInner />
    </Suspense>
  )
}
