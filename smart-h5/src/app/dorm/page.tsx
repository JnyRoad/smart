'use client'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'

const ENTRIES = [
  {
    icon: '🔐',
    title: '门锁动态码',
    subtitle: '智能门锁动态码开门',
    href: '/dorm/lock',
  },
  {
    icon: '💧',
    title: '水电扣费明细',
    subtitle: '查询每月宿舍水电扣费明细',
    href: '/dorm/water-elec',
  },
]

/** Dorm hub: a static entry list (no API, matching the legacy page). */
export default function DormPage() {
  const authorized = useRequireAuth()
  const router = useRouter()

  if (!authorized) return null

  return (
    <PageShell title="我的宿舍">
      <div className="mb-3 flex items-center justify-between rounded-2xl bg-[linear-gradient(120deg,#ec6c00,#f08a2c_60%,#f6a95c)] p-5 text-white shadow-[0_12px_28px_rgba(236,108,0,0.25)]">
        <span className="text-lg font-bold">我的宿舍</span>
        <span aria-hidden className="text-3xl">
          🏠
        </span>
      </div>

      <div className="flex flex-col gap-3">
        {ENTRIES.map((entry) => (
          <button
            key={entry.href}
            type="button"
            onClick={() => router.push(entry.href)}
            className="flex items-center gap-3.5 rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
          >
            <span aria-hidden className="grid h-10 w-10 flex-none place-items-center rounded-xl bg-accent-soft text-lg">
              {entry.icon}
            </span>
            <span className="min-w-0 flex-1">
              <span className="block text-[15px] font-bold">{entry.title}</span>
              <span className="mt-0.5 block text-[13px] text-mid">{entry.subtitle}</span>
            </span>
            <span aria-hidden className="flex-none text-weak">
              ›
            </span>
          </button>
        ))}
      </div>
    </PageShell>
  )
}
