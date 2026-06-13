'use client'

/**
 * 审批列表页内双 Tab（待我审批的/我审批的）。状态由调用方持有；
 * `?tab=done` 直达第二 Tab 的解析也由调用方完成（useSearchParams）。
 */
export function ApprovalTabs({
  active,
  onChange,
  todoLabel = '待我审批的',
  doneLabel = '我审批的',
}: {
  active: 0 | 1
  onChange: (tab: 0 | 1) => void
  todoLabel?: string
  doneLabel?: string
}) {
  return (
    <div
      role="tablist"
      aria-label="页面切换"
      className="mb-3 grid grid-cols-2 gap-1 rounded-[14px] border border-border-soft bg-surface p-1"
    >
      {([
        [0, todoLabel],
        [1, doneLabel],
      ] as const).map(([key, label]) => (
        <button
          key={key}
          type="button"
          role="tab"
          aria-selected={active === key}
          onClick={() => {
            if (active !== key) onChange(key)
          }}
          className={`min-h-11 flex-1 rounded-[10px] text-sm font-bold ${
            active === key
              ? 'bg-white text-accent-ink shadow-[0_6px_18px_rgba(89,87,87,0.08),inset_0_0_0_1px_rgba(236,108,0,0.24)]'
              : 'text-mid'
          }`}
        >
          {label}
        </button>
      ))}
    </div>
  )
}
