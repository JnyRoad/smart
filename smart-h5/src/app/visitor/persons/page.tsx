'use client'
import { ErrorBlock } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { PageShell } from '@/components/page-shell'
import { useMounted } from '@/lib/use-mounted'

/** Fellow-person list; data lives only in the flow draft (no API). */
export default function FellowListPage() {
  const router = useRouter()
  const mounted = useMounted()
  const { fellows, removeFellow } = useVisitorFlow()

  if (!mounted) return null

  return (
    <PageShell title="随行人员">
      <p className="mb-3 px-1 text-sm text-mid">已添加随行人员（{fellows.length}人）</p>

      {fellows.length === 0 ? (
        <ErrorBlock
          status="empty"
          title="暂无随行人员信息"
          description="请点击下方按钮添加随行人员"
        />
      ) : (
        <div className="flex flex-col gap-3">
          {fellows.map((person, index) => (
            <div
              key={`${person.certNo}-${index}`}
              className="flex items-center gap-3 rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]"
            >
              <span className="grid h-12 w-12 flex-none place-items-center rounded-full bg-accent-soft text-lg font-bold text-brand">
                {person.fellowName.charAt(0)}
              </span>
              <span className="min-w-0 flex-1">
                <span className="block text-[15px] font-bold">{person.fellowName}</span>
                <span className="mt-0.5 block text-[13px] text-mid">{person.certNo}</span>
              </span>
              <button
                type="button"
                onClick={() => router.push(`/visitor/persons/add?index=${index}`)}
                className="min-h-11 px-2 text-sm font-semibold text-[#2376d9]"
              >
                编辑
              </button>
              {/* Legacy behavior: delete without a confirm dialog. */}
              <button
                type="button"
                onClick={() => removeFellow(index)}
                className="min-h-11 px-2 text-sm font-semibold text-[#d83b36]"
              >
                删除
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="mt-4 flex gap-3">
        <button
          type="button"
          onClick={() => router.push('/visitor/persons/add')}
          className="h-12 flex-1 rounded-[14px] border-[1.5px] border-brand bg-white text-[15px] font-semibold text-brand active:bg-accent-soft"
        >
          新增随行人员
        </button>
        <button
          type="button"
          onClick={() => router.back()}
          className="h-12 flex-1 rounded-[14px] bg-brand text-[15px] font-semibold text-white active:bg-[#d95f00]"
        >
          确 定
        </button>
      </div>
    </PageShell>
  )
}
