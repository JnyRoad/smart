'use client'
import { ErrorBlock } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { StatusIcon } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { useWorkDraft } from '@/features/good-release/work-draft'
import { useMounted } from '@/lib/use-mounted'

/** 申请编辑链：已添加放行人员（增删改）。 */
export default function WorkPersonsPage() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const persons = useWorkDraft((s) => s.persons)
  const removePerson = useWorkDraft((s) => s.removePerson)

  if (!authorized || !mounted) return null

  return (
    <PageShell title="放行人员">
      <div className="flex flex-col gap-3 pb-24">
        {persons.length === 0 ? (
          <ErrorBlock status="empty" title="暂无放行人员信息" description="请点击下方按钮添加放行人员" />
        ) : (
          <>
            <p className="text-[13px] font-semibold text-mid">已添加放行人员（{persons.length}人）</p>
            {persons.map((person, index) => (
              <div key={index} className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
                <div className="flex items-center justify-between border-b border-border-soft pb-2">
                  <span className="text-[15px] font-bold">
                    {person.name} {person.gh}
                  </span>
                  <span className="flex gap-3">
                    <button
                      type="button"
                      aria-label={`编辑${person.name}`}
                      onClick={() => router.push(`/good-release/work/persons/edit?index=${index}`)}
                      className="grid h-7 w-7 place-items-center rounded-full text-brand"
                    >
                      <StatusIcon name="edit" className="h-5 w-5" />
                    </button>
                    <button
                      type="button"
                      aria-label={`删除${person.name}`}
                      onClick={() => removePerson(index)}
                      className="grid h-7 w-7 place-items-center rounded-full text-[#d83b36]"
                    >
                      <StatusIcon name="delete" className="h-5 w-5" />
                    </button>
                  </span>
                </div>
                <div className="flex gap-3 pt-1.5 text-[13px]">
                  <span className="flex-none text-mid">离厂事由</span>
                  <span className="min-w-0 flex-1 truncate">{person.lcsy}</span>
                </div>
                <div className="flex gap-3 pt-1.5 text-[13px]">
                  <span className="flex-none text-mid">离厂时间</span>
                  <span className="min-w-0 flex-1 truncate">{person.lcDate}</span>
                </div>
              </div>
            ))}
          </>
        )}
      </div>

      <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
        <button
          type="button"
          onClick={() => router.push('/good-release/work/persons/edit')}
          className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-brand text-base font-semibold text-brand"
        >
          新增人员
        </button>
        <button
          type="button"
          onClick={() => router.push('/good-release/work')}
          className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          确 定
        </button>
      </div>
    </PageShell>
  )
}
