'use client'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { loadDetailSnapshot } from '@/features/good-release/detail-snapshot'
import type { PersonDetail } from '@/features/good-release/api'
import { useMounted } from '@/lib/use-mounted'

/** 详情只读链：放行人员清单（数据来自详情页写入的会话快照）。 */
export default function DetailPersonsPage() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const [persons] = useState<PersonDetail[] | null>(() =>
    typeof window === 'undefined' ? null : (loadDetailSnapshot()?.persons ?? null),
  )

  // 快照缺失（直接刷新/外链进入）→ 回域内安全页
  useEffect(() => {
    if (persons === null) router.replace('/good-release/work')
  }, [persons, router])

  if (!authorized || !mounted || persons === null) return null

  return (
    <PageShell title="放行人员">
      <p className="pb-3 text-[13px] font-semibold text-mid">放行人员（{persons.length}人）</p>
      <div className="flex flex-col gap-3">
        {persons.map((person, index) => (
          <button
            key={index}
            type="button"
            onClick={() => router.push(`/good-release/work/detail/persons/item?i=${index}`)}
            className="rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
          >
            <div className="border-b border-border-soft pb-2 text-[15px] font-bold">
              {person.xm} {person.gh}
            </div>
            <div className="flex gap-3 pt-1.5 text-[13px]">
              <span className="flex-none text-mid">离厂事由</span>
              <span className="min-w-0 flex-1 truncate">{person.lcsy}</span>
            </div>
            <div className="flex gap-3 pt-1.5 text-[13px]">
              <span className="flex-none text-mid">离厂时间</span>
              <span className="min-w-0 flex-1 truncate">
                {person.lcrq} {person.lcsj}
              </span>
            </div>
          </button>
        ))}
      </div>
    </PageShell>
  )
}
