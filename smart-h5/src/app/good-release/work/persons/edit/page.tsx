'use client'
import { DatePicker, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { StaffSearchPopup } from '@/features/good-release/staff-search-popup'
import { useWorkDraft, type WorkPerson } from '@/features/good-release/work-draft'
import { useMounted } from '@/lib/use-mounted'

function formatDateTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function WorkPersonEditInner() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const params = useSearchParams()
  const indexParam = params.get('index')
  const editIndex = indexParam === null ? null : Number(indexParam)
  const persons = useWorkDraft((s) => s.persons)
  const addPerson = useWorkDraft((s) => s.addPerson)
  const updatePerson = useWorkDraft((s) => s.updatePerson)
  const editing = editIndex !== null ? (persons[editIndex] ?? null) : null

  const [staff, setStaff] = useState<{ gh: string; name: string; xm: string | number } | null>(
    editing ? { gh: editing.gh, name: editing.name, xm: editing.xm } : null,
  )
  const [lcsy, setLcsy] = useState(editing?.lcsy ?? '')
  const [lcDate, setLcDate] = useState(editing?.lcDate ?? '')
  const [searchVisible, setSearchVisible] = useState(false)
  const [timeVisible, setTimeVisible] = useState(false)

  function handleSubmit() {
    if (!staff) return Toast.show('请查询并选择员工')
    if (!lcsy) return Toast.show('请输入离厂事由')
    if (!lcDate) return Toast.show('请选择离厂日期')
    const person: WorkPerson = { gh: staff.gh, xm: staff.xm, name: staff.name, lcsy, lcDate }
    if (editIndex !== null && editing) {
      updatePerson(editIndex, person)
    } else {
      addPerson(person)
    }
    router.push('/good-release/work/persons')
  }

  if (!authorized || !mounted) return null

  return (
    <PageShell title={editing ? '修改放行人员' : '添加放行人员'}>
      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)] pb-24">
        <div className="mt-1">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            工号 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setSearchVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {staff?.gh ?? <span className="text-weak">请输入</span>}
          </button>
        </div>
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            姓名 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setSearchVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {staff?.name ?? <span className="text-weak">请输入</span>}
          </button>
        </div>
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="p-lcsy">
            离厂事由 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="p-lcsy"
            placeholder="请输入离厂事由"
            value={lcsy}
            onChange={(e) => setLcsy(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        </div>
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            离厂日期 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setTimeVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {lcDate || <span className="text-weak">请选择离厂日期</span>}
          </button>
          <DatePicker
            visible={timeVisible}
            onClose={() => setTimeVisible(false)}
            precision="minute"
            onConfirm={(d) => setLcDate(formatDateTime(d))}
          />
        </div>
      </section>

      <div className="fixed inset-x-0 bottom-0 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
        <button
          type="button"
          onClick={handleSubmit}
          className="flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          {editing ? '确认修改放行人员' : '确认添加放行人员'}
        </button>
      </div>

      {searchVisible && (
        <StaffSearchPopup
          visible
          onClose={() => setSearchVisible(false)}
          onPicked={({ gh, name, id }) => setStaff({ gh, name, xm: id })}
        />
      )}
    </PageShell>
  )
}

export default function WorkPersonEditPage() {
  return (
    <Suspense>
      <WorkPersonEditInner />
    </Suspense>
  )
}
