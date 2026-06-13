'use client'

const STEPS = ['被访信息', '访客信息', '提交信息'] as const

/** Three-step indicator for the visitor application flow. */
export function VisitorSteps({ current }: { current: 1 | 2 | 3 }) {
  return (
    <div className="mb-3 flex items-center justify-center gap-0 rounded-2xl bg-white px-4 py-3.5 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
      {STEPS.map((label, index) => {
        const step = index + 1
        const active = step <= current
        return (
          <div key={label} className="flex items-center">
            {index > 0 && (
              <span
                aria-hidden
                className={`mx-2 h-px w-8 ${step <= current ? 'bg-brand' : 'bg-light-gray'}`}
                style={{ background: step <= current ? 'var(--brand-orange)' : 'var(--light-gray)' }}
              />
            )}
            <span className="flex items-center gap-1.5">
              <span
                className={`grid h-5 w-5 place-items-center rounded-full text-[11px] font-bold ${
                  active ? 'bg-brand text-white' : 'bg-surface text-weak'
                }`}
              >
                {step}
              </span>
              <span className={`text-xs font-semibold ${active ? 'text-ink' : 'text-weak'}`}>
                {label}
              </span>
            </span>
          </div>
        )
      })}
    </div>
  )
}
