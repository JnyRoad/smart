'use client'
import { ErrorBlock, InfiniteScroll, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { AppTileIcon } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getHelpQuestions, type HelpQuestion } from '@/features/help/api'
import { useListPager } from '@/lib/use-list-pager'

const PAGE_SIZE = 10

/** Help center: paginated FAQ list (pull-to-refresh + infinite scroll). */
export default function HelpPage() {
  const authorized = useRequireAuth()
  const router = useRouter()

  const pager = useListPager<HelpQuestion, string>({
    filterKey: '',
    fetchPage: async (page) => {
      const res = await getHelpQuestions({ current: page, size: PAGE_SIZE })
      if (res.code !== 0) {
        Toast.show(res.message ?? '网络错误')
        throw new Error(res.message ?? '网络错误')
      }
      return { rows: res.data?.records ?? [], pages: res.data?.pages ?? page }
    },
  })

  if (!authorized) return null

  return (
    <PageShell title="帮助中心">
      <PullToRefresh
        onRefresh={async () => {
          await pager.refresh()
          Toast.show('更新成功')
        }}
      >
        {/* 头部横幅 */}
        <div className="mb-3 flex items-center justify-between rounded-2xl bg-[linear-gradient(120deg,#ec6c00,#f08a2c_60%,#f6a95c)] p-5 text-white shadow-[0_12px_28px_rgba(236,108,0,0.25)]">
          <span className="text-lg font-bold">帮助中心</span>
          <AppTileIcon name="help" />
        </div>

        {!pager.loadedOnce ? (
          <div className="flex justify-center py-16">
            <SpinLoading color="primary" />
          </div>
        ) : pager.rows.length === 0 ? (
          <ErrorBlock status="empty" description="暂无帮助内容" />
        ) : (
          <div className="flex flex-col gap-3">
            {pager.rows.map((q, index) => (
              <button
                key={`${q.questionId}-${index}`}
                type="button"
                onClick={() => router.push(`/help/${q.questionId}`)}
                className="flex items-center gap-3 rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
              >
                <span className="min-w-0 flex-1 truncate text-[15px]">{q.questionTitle}</span>
                <span aria-hidden className="flex-none text-weak">
                  ›
                </span>
              </button>
            ))}
          </div>
        )}

        <InfiniteScroll loadMore={pager.loadMore} hasMore={!pager.loadedOnce || pager.hasMore} />
      </PullToRefresh>
    </PageShell>
  )
}
