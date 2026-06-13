'use client'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { loadDetailSnapshot } from '@/features/good-release/detail-snapshot'
import type { ThingDetail } from '@/features/good-release/api'
import { useMounted } from '@/lib/use-mounted'

/** 详情只读链：放行物品清单（数据来自详情页写入的会话快照）。 */
export default function DetailGoodsPage() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const [goods] = useState<ThingDetail[] | null>(() =>
    typeof window === 'undefined' ? null : (loadDetailSnapshot()?.goods ?? null),
  )

  // 快照缺失（直接刷新/外链进入）→ 回域内安全页
  useEffect(() => {
    if (goods === null) router.replace('/good-release/work')
  }, [goods, router])

  if (!authorized || !mounted || goods === null) return null

  return (
    <PageShell title="放行物品">
      <p className="pb-3 text-[13px] font-semibold text-mid">放行物品（{goods.length}项）</p>
      <div className="flex flex-col gap-3">
        {goods.map((thing, index) => (
          <button
            key={index}
            type="button"
            onClick={() => router.push(`/good-release/work/detail/goods/item?i=${index}`)}
            className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
          >
            <div className="border-b border-border-soft pb-2 text-[15px] font-bold">
              {thing.wpmc} {thing.wpbm}
            </div>
            <div className="flex gap-3 pt-1.5 text-[13px]">
              <span className="flex-none text-mid">单位</span>
              <span className="min-w-0 flex-1 truncate">{thing.wpdw}</span>
            </div>
            <div className="flex gap-3 pt-1.5 text-[13px]">
              <span className="flex-none text-mid">放行日期</span>
              <span className="min-w-0 flex-1 truncate">{thing.fxrq}</span>
            </div>
          </button>
        ))}
      </div>
    </PageShell>
  )
}
