'use client'
import { useRouter } from 'next/navigation'

/**
 * 「发起提交｜查看数据」double-segment tabs shared by the dorm-service modules.
 * Routes are passed explicitly by the caller — this also fixes the legacy
 * dorm-exit bug where the submit tab jumped to the repairs module.
 */
export function SegmentTabs({
  active,
  submitHref,
  listHref,
}: {
  active: 'submit' | 'list'
  submitHref: string
  listHref: string
}) {
  const router = useRouter()

  function segment(key: 'submit' | 'list', label: string, href: string) {
    return (
      <button
        type="button"
        role="tab"
        aria-selected={active === key}
        onClick={() => {
          if (active !== key) router.push(href)
        }}
        className={`min-h-11 flex-1 rounded-[10px] text-sm font-bold ${
          active === key
            ? 'bg-white text-accent-ink shadow-[0_6px_18px_rgba(89,87,87,0.08),inset_0_0_0_1px_rgba(236,108,0,0.24)]'
            : 'text-mid'
        }`}
      >
        {label}
      </button>
    )
  }

  return (
    <div
      role="tablist"
      aria-label="页面切换"
      className="mb-3 grid grid-cols-2 gap-1 rounded-[14px] border border-border-soft bg-surface p-1"
    >
      {segment('submit', '发起提交', submitHref)}
      {segment('list', '查看数据', listHref)}
    </div>
  )
}
