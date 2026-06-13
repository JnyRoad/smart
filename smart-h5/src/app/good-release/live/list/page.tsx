'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getLiveReleasePage, type ReleaseListItem } from '@/features/good-release/api'
import { listStatusTone, type StatusTone } from '@/features/good-release/release-status'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

const TONE_CLASS: Record<StatusTone, string> = {
  success: 'text-[#16a673]',
  danger: 'text-[#d83b36]',
  muted: 'text-mid',
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

export default function GoodReleaseLiveListPage() {
  const authorized = useRequireAuth()
  const router = useRouter()

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined
  const baseFailed = baseInfo.isError || (baseInfo.isSuccess && baseInfo.data.code !== 0)

  const pager = useListPager<ReleaseListItem, string>({
    filterKey: badge ?? '',
    fetchPage: async (page) => {
      if (!badge) return { rows: [], pages: 0 }
      const res = await getLiveReleasePage({ badge, current: page, size: PAGE_SIZE })
      if (res.code !== 0) {
        Toast.show(res.message ?? '网络错误')
        throw new Error(res.message ?? '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="物品放行（生活区）">
      <SegmentTabs active="list" submitHref="/good-release/live" listHref="/good-release/live/list" />

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {baseFailed ? (
          <ErrorBlock status="default" title="加载失败" description="获取用户信息失败" />
        ) : badge === undefined || !pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无放行记录" />
        ) : (
          <div className="flex flex-col gap-3">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.id}-${index}`}
                type="button"
                onClick={() => router.push(`/good-release/live/detail?id=${record.id}`)}
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.name}提交的物品放行</span>
                  <span
                    className={`flex-none text-[13px] font-semibold ${TONE_CLASS[listStatusTone(record.status)]}`}
                  >
                    {record.oaNode}
                  </span>
                </div>
                <FieldRow label="物品类型" value={record.articlesTypeName} />
                <FieldRow label="物品名称" value={record.articlesDesc} />
                <FieldRow label="携带人" value={record.carrier} />
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
