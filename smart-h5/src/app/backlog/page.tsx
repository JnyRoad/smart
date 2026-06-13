'use client'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'

const ENTRIES = [
  { icon: '📦', title: '物品放行（生活区）', href: '/backlog/release-live' },
  { icon: '🏠', title: '退宿审批', href: '/backlog/dorm-exit' },
  { icon: '🔧', title: '园区报修审批', href: '/backlog/repairs' },
] as const

/** 审批侧功能入口聚合页（旧版注释禁用的访客/办公区/离职/申诉入口不做）。 */
export default function BacklogPage() {
  const authorized = useRequireAuth()
  const router = useRouter()

  if (!authorized) return null

  return (
    <PageShell title="待办事项">
      <div className="flex flex-col gap-3">
        {ENTRIES.map((entry) => (
          <button
            key={entry.href}
            type="button"
            onClick={() => router.push(entry.href)}
            className="flex items-center gap-3 rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
          >
            <span aria-hidden className="grid h-10 w-10 place-items-center rounded-xl bg-accent-soft text-xl">
              {entry.icon}
            </span>
            <span className="flex-1 text-[15px] font-semibold">{entry.title}</span>
            <span aria-hidden className="text-weak">
              ›
            </span>
          </button>
        ))}
      </div>
    </PageShell>
  )
}
