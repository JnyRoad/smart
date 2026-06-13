'use client'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { InfoRow } from '@/features/good-release/detail-blocks'
import { loadDetailSnapshot, snapshotItemAt } from '@/features/good-release/detail-snapshot'
import type { PersonDetail } from '@/features/good-release/api'
import { useMounted } from '@/lib/use-mounted'

function DetailPersonItemInner() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const params = useSearchParams()
  const [person] = useState<PersonDetail | null>(() =>
    typeof window === 'undefined'
      ? null
      : params.get('i') === null
        ? null
        : snapshotItemAt(loadDetailSnapshot()?.persons, Number(params.get('i'))),
  )

  // 快照缺失 / 下标越界 → 回域内安全页
  useEffect(() => {
    if (person === null) router.replace('/good-release/work')
  }, [person, router])

  if (!authorized || !mounted || person === null) return null

  return (
    <PageShell title="人员详情">
      <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <InfoRow label="工号" value={person.gh} />
        <InfoRow label="姓名" value={person.xm} />
        <InfoRow label="离厂事由" value={person.lcsy} />
        <InfoRow label="离厂日期" value={`${person.lcrq ?? ''} ${person.lcsj ?? ''}`.trim() || undefined} />
      </div>
    </PageShell>
  )
}

export default function DetailPersonItemPage() {
  return (
    <Suspense>
      <DetailPersonItemInner />
    </Suspense>
  )
}
