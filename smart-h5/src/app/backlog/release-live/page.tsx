'use client'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getLiveApprovalPage, type ApprovalSearch, type LiveApprovalItem } from '@/features/backlog/api'
import { ApprovalTabs } from '@/features/backlog/approval-tabs'
import { StaffFilterPopup } from '@/features/backlog/staff-filter-popup'
import { useIsSecurityGuard } from '@/features/backlog/use-is-security-guard'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

function approveStateClass(state: string | undefined): string {
  if (state === '1') return 'text-[#16a673]'
  if (state === '2') return 'text-[#d83b36]'
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

function ReleaseLiveApprovalInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const [tab, setTab] = useState<0 | 1>(params.get('tab') === 'done' ? 1 : 0)
  const [search, setSearch] = useState<ApprovalSearch>({})
  const [searchVisible, setSearchVisible] = useState(false)
  const isSecurityGuard = useIsSecurityGuard(authorized)

  const pager = useListPager<LiveApprovalItem, string>({
    filterKey: `${tab}|${JSON.stringify(search)}`,
    fetchPage: async (page) => {
      const res = await getLiveApprovalPage({ recordState: tab, current: page, size: PAGE_SIZE, ...search })
      if (res.code !== 0) {
        Toast.show(res.message || '网络错误')
        throw new Error(res.message || '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="物品放行（生活区）审批">
      <ApprovalTabs
        active={tab}
        onChange={(next) => {
          setTab(next)
          setSearch({})
        }}
      />

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {!pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无审批记录" />
        ) : (
          <div className="flex flex-col gap-3 pb-20">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.approveId}-${index}`}
                type="button"
                onClick={() =>
                  router.push(
                    `/backlog/release-live/detail?id=${record.approveId}&sort=${record.sort ?? ''}${tab === 1 ? '&tab=done' : ''}`,
                  )
                }
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.approveName}</span>
                  {tab === 0 ? (
                    <span className="flex-none text-[13px] font-semibold text-mid">{record.approveNodeDesc}</span>
                  ) : (
                    <span className={`flex-none text-[13px] font-semibold ${approveStateClass(record.approveState)}`}>
                      {record.approveDesc}
                    </span>
                  )}
                </div>
                <FieldRow label="物品类型" value={record.articlesTypeDesc} />
                <FieldRow label="物品名称" value={record.articleName} />
                <FieldRow label="携带人名称" value={record.carrier} />
                <FieldRow label="房间信息" value={record.roomInfo} />
                <FieldRow label="申请时间" value={record.createTime} />
              </button>
            ))}
          </div>
        )}
        <InfiniteScroll loadMore={pager.loadMore} hasMore={!pager.loadedOnce || pager.hasMore} />
      </PullToRefresh>

      {/* 旧版仅看保安身份（dataList.length 条件已被旧码注释），空结果也能改条件 */}
      {isSecurityGuard && (
        <div className="fixed inset-x-0 bottom-0 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
          <button
            type="button"
            onClick={() => setSearchVisible(true)}
            className="flex h-11 w-full items-center justify-center rounded-[12px] bg-surface text-sm font-bold text-mid"
          >
            🔍 搜 索
          </button>
        </div>
      )}

      {searchVisible && (
        <StaffFilterPopup visible onClose={() => setSearchVisible(false)} onSearch={setSearch} withTimeRange />
      )}
    </PageShell>
  )
}

export default function ReleaseLiveApprovalPage() {
  return (
    <Suspense>
      <ReleaseLiveApprovalInner />
    </Suspense>
  )
}
