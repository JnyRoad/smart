'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getDormExitPage, type DormExitRecord } from '@/features/dorm-services/api'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

/** status 2 = approved (green); 3 = rejected (red); others gray. */
function statusClass(status: number | undefined): string {
  if (status === 2) return 'text-[#16a673]'
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

export default function DormExitListPage() {
  const authorized = useRequireAuth()
  const router = useRouter()

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined

  const pager = useListPager<DormExitRecord, string | undefined>({
    filterKey: badge,
    fetchPage: async (page) => {
      if (!badge) return { rows: [], pages: 1 }
      const res = await getDormExitPage({ current: page, size: PAGE_SIZE, badge })
      if (res.code !== 0) {
        Toast.show(res.message ?? '网络错误')
        throw new Error(res.message ?? '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  const baseInfoFailed = baseInfo.isError || (baseInfo.isSuccess && baseInfo.data.code !== 0)

  return (
    <PageShell title="退宿申请">
      <SegmentTabs active="list" submitHref="/dorm-exit" listHref="/dorm-exit/list" />

      {baseInfoFailed && (
        <ErrorBlock status="default" title="加载失败" description="员工信息获取失败" className="py-8" />
      )}
      {!baseInfoFailed && (
      <PullToRefresh onRefresh={() => pager.refresh()}>
        {badge === undefined || !pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无退宿申请记录" />
        ) : (
          <div className="flex flex-col gap-3">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.id}-${index}`}
                type="button"
                onClick={() => router.push(`/dorm-exit/detail?id=${record.id}`)}
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.name}提交的退宿申请</span>
                  <span className={`flex-none text-[13px] font-semibold ${statusClass(record.status)}`}>
                    {record.statusDesc}
                  </span>
                </div>
                <FieldRow
                  label="房间信息"
                  value={
                    record.dorDetailStr?.length
                      ? record.dorDetailStr.map((line, i) => (
                          <span key={i} className="block">
                            {line}
                          </span>
                        ))
                      : undefined
                  }
                />
                <FieldRow label="退宿原因" value={record.quitReasonDesc} />
                <FieldRow label="预计离开时间" value={record.applyLeaveTime} />
                <FieldRow label="申请时间" value={record.createTime} />
              </button>
            ))}
          </div>
        )}
        <InfiniteScroll
          loadMore={pager.loadMore}
          hasMore={badge !== undefined && (!pager.loadedOnce || pager.hasMore)}
        />
      </PullToRefresh>
      )}
    </PageShell>
  )
}
