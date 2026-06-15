'use client'
import { DatePicker, ErrorBlock, InfiniteScroll, Popup, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { StatusIcon } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getBackPage, type BackListItem, type BackSearch } from '@/features/good-release/api'
import { formatReturnFactorySearchDateTime } from '@/features/good-release/return-factory-search'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

function FieldRow({ label, value }: { label: string; value?: string }) {
  return (
    <div className="flex gap-3 pt-1.5 text-[13px]">
      <span className="flex-none text-mid">{label}</span>
      <span className="min-w-0 flex-1 truncate">{value || '-'}</span>
    </div>
  )
}

/** 搜索弹层：工号 / 携带人姓名 / 申请开始·结束时间（旧 search-by-staff 字段）。 */
function SearchPopup({
  visible,
  onClose,
  onSearch,
}: {
  visible: boolean
  onClose: () => void
  onSearch: (search: BackSearch) => void
}) {
  const [badge, setBadge] = useState('')
  const [name, setName] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [startVisible, setStartVisible] = useState(false)
  const [endVisible, setEndVisible] = useState(false)

  return (
    <Popup visible={visible} onMaskClick={onClose} bodyStyle={{ borderRadius: '16px 16px 0 0' }}>
      <div className="p-4">
        <div className="flex items-center justify-between pb-3">
          <span className="text-[15px] font-bold">输入员工信息查询</span>
          <button
            type="button"
            onClick={() => {
              onSearch({
                ...(badge ? { badge } : {}),
                ...(name ? { name } : {}),
                ...(startTime ? { startTime } : {}),
                ...(endTime ? { endTime } : {}),
              })
              onClose()
            }}
            className="text-sm font-semibold text-brand"
          >
            确定
          </button>
        </div>
        <div className="flex flex-col gap-3">
          <input placeholder="工号" value={badge} onChange={(e) => setBadge(e.target.value)} className={SMS_INPUT_CLASS} />
          <input placeholder="携带人姓名" value={name} onChange={(e) => setName(e.target.value)} className={SMS_INPUT_CLASS} />
          <button type="button" onClick={() => setStartVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {startTime || <span className="text-weak">申请开始时间</span>}
          </button>
          <DatePicker
            visible={startVisible}
            onClose={() => setStartVisible(false)}
            precision="minute"
            onConfirm={(d) => setStartTime(formatReturnFactorySearchDateTime(d))}
          />
          <button type="button" onClick={() => setEndVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {endTime || <span className="text-weak">申请结束时间</span>}
          </button>
          <DatePicker
            visible={endVisible}
            onClose={() => setEndVisible(false)}
            precision="minute"
            onConfirm={(d) => setEndTime(formatReturnFactorySearchDateTime(d))}
          />
        </div>
      </div>
    </Popup>
  )
}

function ReturnFactoryInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  // 新路由约定：?tab=confirmed 直达「我确认的」（旧版 curTabIndex=1 不复用）。
  const [tab, setTab] = useState<0 | 1>(params.get('tab') === 'confirmed' ? 1 : 0)
  const [search, setSearch] = useState<BackSearch>({})
  const [searchVisible, setSearchVisible] = useState(false)

  const pager = useListPager<BackListItem, string>({
    filterKey: `${tab}|${JSON.stringify(search)}`,
    fetchPage: async (page) => {
      const res = await getBackPage({ approvalStatus: tab, current: page, size: PAGE_SIZE, ...search })
      if (res.code !== 0) {
        Toast.show(res.message || '网络错误')
        throw new Error(res.message || '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="返厂确认">
      <div
        role="tablist"
        aria-label="页面切换"
        className="mb-3 grid grid-cols-2 gap-1 rounded-[14px] border border-border-soft bg-surface p-1"
      >
        {([
          [0, '待确认的'],
          [1, '我确认的'],
        ] as const).map(([key, label]) => (
          <button
            key={key}
            type="button"
            role="tab"
            aria-selected={tab === key}
            onClick={() => {
              if (tab !== key) {
                setTab(key)
                setSearch({})
              }
            }}
            className={`min-h-11 flex-1 rounded-[10px] text-sm font-bold ${
              tab === key
                ? 'bg-white text-accent-ink shadow-[0_6px_18px_rgba(89,87,87,0.08),inset_0_0_0_1px_rgba(236,108,0,0.24)]'
                : 'text-mid'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      <PullToRefresh onRefresh={() => pager.refresh()}>
        {!pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无放行条" />
        ) : (
          <div className="flex flex-col gap-3 pb-20">
            {pager.rows.map((record, index) => (
              <button
                key={`${record.id}-${index}`}
                type="button"
                onClick={() => router.push(`/return-factory/detail?id=${record.id}`)}
                className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">{record.name}提交的放行条</span>
                  <span className="flex-none text-[13px] font-semibold text-mid">{record.backStatus}</span>
                </div>
                <FieldRow label="申请部门" value={record.deptName} />
                <FieldRow label="放行事项" value={record.releaseItemDesc} />
                <FieldRow label="申请时间" value={record.createTime} />
                <FieldRow label="OA节点" value={record.oaNode} />
              </button>
            ))}
          </div>
        )}
        <InfiniteScroll loadMore={pager.loadMore} hasMore={!pager.loadedOnce || pager.hasMore} />
      </PullToRefresh>

      {pager.rows.length > 0 && (
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

      {/* 条件挂载：每次打开都是干净的查询条件（旧版 start() 同语义） */}
      {searchVisible && (
        <SearchPopup visible onClose={() => setSearchVisible(false)} onSearch={setSearch} />
      )}
    </PageShell>
  )
}

export default function ReturnFactoryPage() {
  return (
    <Suspense>
      <ReturnFactoryInner />
    </Suspense>
  )
}
