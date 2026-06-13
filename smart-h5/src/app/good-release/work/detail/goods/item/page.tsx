'use client'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { InfoRow } from '@/features/good-release/detail-blocks'
import { loadDetailSnapshot, snapshotItemAt } from '@/features/good-release/detail-snapshot'
import { ysfsLabel } from '@/features/good-release/dicts'
import type { ThingDetail } from '@/features/good-release/api'
import { useMounted } from '@/lib/use-mounted'

function DetailGoodItemInner() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const params = useSearchParams()
  const [thing] = useState<ThingDetail | null>(() =>
    typeof window === 'undefined'
      ? null
      : params.get('i') === null
        ? null
        : snapshotItemAt(loadDetailSnapshot()?.goods, Number(params.get('i'))),
  )

  // 快照缺失 / 下标越界 → 回域内安全页
  useEffect(() => {
    if (thing === null) router.replace('/good-release/work')
  }, [thing, router])

  if (!authorized || !mounted || thing === null) return null

  return (
    <PageShell title="物品详情">
      <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <InfoRow label="资产编码" value={thing.wpbm} />
        <InfoRow label="名称" value={thing.wpmc} />
        <InfoRow label="单位" value={thing.wpdw} />
        <InfoRow label="数量" value={thing.wpsl !== undefined ? String(thing.wpsl) : undefined} />
        <InfoRow label="接收单位" value={thing.jsdw} />
        <InfoRow label="放行日期" value={thing.fxrq} />
        <InfoRow label="备注(原因)" value={thing.bz} />
        <InfoRow label="运输方式" value={thing.ysfsDesc || ysfsLabel(thing.ysfs) || undefined} />
        <InfoRow label="姓名" value={thing.xm} />
        <InfoRow label="车牌号" value={thing.cph} />
      </div>
    </PageShell>
  )
}

export default function DetailGoodItemPage() {
  return (
    <Suspense>
      <DetailGoodItemInner />
    </Suspense>
  )
}
